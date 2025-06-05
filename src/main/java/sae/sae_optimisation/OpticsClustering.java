package sae.sae_optimisation;

import java.awt.image.BufferedImage;
import java.awt.Color;
import java.util.*;

public class OpticsClustering {

    public static BufferedImage detecterEcosystemesOPTICS(BufferedImage image, int[] groupes, String[] etiquettes, String biomeCible) {
        int largeur = image.getWidth();
        int hauteur = image.getHeight();

        BufferedImage fondEclairci = EcosystemDetector.eclaircirImage(image, 75);
        BufferedImage resultat = new BufferedImage(largeur, hauteur, BufferedImage.TYPE_INT_RGB);

        for (int y = 0; y < hauteur; y++) {
            for (int x = 0; x < largeur; x++) {
                resultat.setRGB(x, y, fondEclairci.getRGB(x, y));
            }
        }

        // Récupération des pixels du biome ciblé
        List<int[]> points = new ArrayList<>();
        int idx = 0;
        for (int y = 0; y < hauteur; y++) {
            for (int x = 0; x < largeur; x++, idx++) {
                if (etiquettes[groupes[idx]].equals(biomeCible)) {
                    points.add(new int[]{x, y});
                }
            }
        }

        // Paramètres OPTICS
        int eps = 6; // Distance maximale pour considérer deux points comme voisins
        int minPts = 10; // Nombre minimal de points pour qu'un point soit considéré comme noyau
        double seuil = 10.0; // Seuil de distance pour séparer les clusters

        List<OpticsPoint> ordered = optics(points, eps, minPts);

        // Création de clusters à partir de reachability distances
        int clusterId = 0;
        Map<Integer, List<OpticsPoint>> clusters = new HashMap<>();

        for (OpticsPoint p : ordered) {
            if (p.reachability == -1 || p.reachability > seuil) {
                // Si la distance est trop grande, on commence un nouveau cluster
                clusterId++;
            } else {
                clusters.computeIfAbsent(clusterId, k -> new ArrayList<>()).add(p);
            }
        }

        // Palette de couleurs pour afficher les clusters
        Color[] palette = new Color[] {
            Color.BLUE, Color.RED, Color.GREEN, Color.MAGENTA, Color.CYAN,
            Color.ORANGE, Color.PINK, Color.YELLOW,
            new Color(128, 0, 128), new Color(0, 128, 128), new Color(128, 128, 0),
            new Color(0, 0, 128), new Color(128, 0, 0), new Color(0, 128, 0),
            new Color(255, 105, 180), new Color(255, 215, 0), new Color(0, 191, 255),
            new Color(160, 32, 240)
        };

        // Coloration des clusters détectés
        int nbClusters = 0;
        for (Map.Entry<Integer, List<OpticsPoint>> entry : clusters.entrySet()) {
            Color color = palette[nbClusters % palette.length];
            for (OpticsPoint p : entry.getValue()) {
                resultat.setRGB(p.x, p.y, color.getRGB());
            }
            nbClusters++;
        }

        System.out.println("→ " + nbClusters + " écosystèmes détectés avec OPTICS pour " + biomeCible);
        return resultat;
    }


    private static List<OpticsPoint> optics(List<int[]> coords, int eps, int minPts) {
        int n = coords.size();
        boolean[] visited = new boolean[n];
        List<OpticsPoint> output = new ArrayList<>();

        // Init points
        List<OpticsPoint> points = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            int[] c = coords.get(i);
            points.add(new OpticsPoint(i, c[0], c[1]));
        }

        // Création de la grille
        Map<String, List<OpticsPoint>> grille = new HashMap<>();
        for (OpticsPoint p : points) {
            String key = (p.x / eps) + "_" + (p.y / eps);
            grille.computeIfAbsent(key, k -> new ArrayList<>()).add(p);
        }

        for (OpticsPoint p : points) {
            if (visited[p.index]) continue;
            visited[p.index] = true;

            List<OpticsPoint> voisins = voisinsGrille(p, grille, eps);

            p.coreDistance = coreDistance(p, voisins, minPts);
            output.add(p);

            if (p.coreDistance == -1) continue;

            PriorityQueue<OpticsPoint> queue = new PriorityQueue<>(Comparator.comparingDouble(o -> o.reachability));
            updateOPTICS(queue, voisins, p, eps, minPts, visited, grille);

            while (!queue.isEmpty()) {
                OpticsPoint q = queue.poll();
                if (visited[q.index]) continue;
                visited[q.index] = true;

                List<OpticsPoint> voisinsQ = voisinsGrille(q, grille, eps);
                q.coreDistance = coreDistance(q, voisinsQ, minPts);
                output.add(q);

                if (q.coreDistance != -1) {
                    updateOPTICS(queue, voisinsQ, q, eps, minPts, visited, grille);
                }
            }
        }

        return output;
    }

    private static Map<Integer, List<OpticsPoint>> extraireClusters(List<OpticsPoint> points, double seuil) {
        Map<Integer, List<OpticsPoint>> clusters = new HashMap<>();
        int clusterId = -1;

        for (OpticsPoint p : points) {
            if (p.reachability == -1 || p.reachability > seuil) {
                // Nouvelle séparation → début d’un nouveau cluster
                clusterId++;
                continue;
            }
            // Ajout au cluster actuel
            clusters.computeIfAbsent(clusterId, k -> new ArrayList<>()).add(p);
        }
        return clusters;
    }


    private static List<OpticsPoint> voisinsGrille(OpticsPoint p, Map<String, List<OpticsPoint>> grille, int eps) {
        int cx = p.x / eps;
        int cy = p.y / eps;
        List<OpticsPoint> voisins = new ArrayList<>();
        int eps2 = eps * eps; // distance au carré

        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                String key = (cx + dx) + "_" + (cy + dy);
                List<OpticsPoint> candidats = grille.getOrDefault(key, Collections.emptyList());
                for (OpticsPoint o : candidats) {
                    if (p == o) continue;
                    int dxp = p.x - o.x;
                    int dyp = p.y - o.y;
                    if (dxp * dxp + dyp * dyp <= eps2) {
                        voisins.add(o);
                    }
                }
            }
        }
        return voisins;
    }


    private static double coreDistance(OpticsPoint p, List<OpticsPoint> voisins, int minPts) {
        if (voisins.size() < minPts) return -1;
        voisins.sort(Comparator.comparingDouble(o -> Math.hypot(p.x - o.x, p.y - o.y)));
        return Math.hypot(p.x - voisins.get(minPts - 1).x, p.y - voisins.get(minPts - 1).y);
    }

    private static void updateOPTICS(PriorityQueue<OpticsPoint> queue, List<OpticsPoint> voisins, OpticsPoint p, int eps, int minPts, boolean[] visited, Map<String, List<OpticsPoint>> grille) {
        for (OpticsPoint o : voisins) {
            if (visited[o.index]) continue;

            double dist = Math.hypot(p.x - o.x, p.y - o.y);
            double reach = Math.max(p.coreDistance, dist);

            if (o.reachability == -1 || reach < o.reachability) {
                o.reachability = reach;
                queue.add(o); // on ne fait pas de remove, car reach est mis à jour et re-trié
            }
        }
    }


    static class OpticsPoint {
        int index;
        int x, y;
        double reachability = -1;
        double coreDistance = -1;

        OpticsPoint(int index, int x, int y) {
            this.index = index;
            this.x = x;
            this.y = y;
        }
    }
}

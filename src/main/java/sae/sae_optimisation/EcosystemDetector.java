package sae.sae_optimisation;

import java.awt.image.BufferedImage;
import java.awt.Color;
import java.util.*;

public class EcosystemDetector {

    /**
     * Détecte les écosystèmes (clusters) pour un biome spécifique sur une image donnée.
     * L'algorithme DBSCAN spatial est utilisé pour regrouper les pixels proches.
     * Les résultats sont affichés avec une image de fond éclaircie + couleurs par cluster.
     */
    public static BufferedImage detecterEcosystemes(BufferedImage image, int[] groupes, String[] etiquettes, String biomeCible) {
        int largeur = image.getWidth();
        int hauteur = image.getHeight();

        // Crée une image éclaircie (75% plus clair) pour servir de fond
        BufferedImage fondEclairci = eclaircirImage(image, 75);
        BufferedImage resultat = new BufferedImage(largeur, hauteur, BufferedImage.TYPE_INT_RGB);

        // Copie du fond éclairci dans l'image résultat
        for (int y = 0; y < hauteur; y++) {
            for (int x = 0; x < largeur; x++) {
                resultat.setRGB(x, y, fondEclairci.getRGB(x, y));
            }
        }

        // Récupère les pixels appartenant uniquement au biome cible
        List<int[]> points = new ArrayList<>();
        int idx = 0;
        for (int y = 0; y < hauteur; y++) {
            for (int x = 0; x < largeur; x++, idx++) {
                if (etiquettes[groupes[idx]].equals(biomeCible)) {
                    points.add(new int[]{x, y});
                }
            }
        }

        // Paramètres DBSCAN
        int eps = 6;       // Distance maximale entre deux points pour être considérés voisins
        int minPts = 10;   // Nombre minimal de voisins pour former un cluster

        // Lance DBSCAN optimisé sur les pixels du biome
        int[] labels = dbscanAvecGrille(points, eps, minPts);

        // Palette de couleurs pour distinguer les clusters
        Color[] palette = new Color[] {
                Color.BLUE, Color.RED, Color.GREEN, Color.MAGENTA, Color.CYAN,
                Color.ORANGE, Color.PINK, Color.YELLOW, Color.LIGHT_GRAY, Color.GRAY,
                new Color(128, 0, 128), new Color(0, 128, 128), new Color(128, 128, 0),
                new Color(0, 0, 128), new Color(128, 0, 0), new Color(0, 128, 0),
                new Color(255, 105, 180), new Color(255, 215, 0), new Color(0, 191, 255),
                new Color(160, 32, 240)
        };

        // Coloration des points appartenant à un cluster (label >= 0)
        for (int i = 0; i < points.size(); i++) {
            int label = labels[i];
            if (label == -1) continue; // bruit
            int[] p = points.get(i);
            Color c = palette[label % palette.length];
            resultat.setRGB(p[0], p[1], c.getRGB());
        }

        // Affichage du nombre de clusters détectés
        int nbClusters = Arrays.stream(labels).max().orElse(-1) + 1;
        System.out.println("→ " + nbClusters + " écosystèmes détectés pour " + biomeCible);
        return resultat;
    }

    /**
     * Éclaircit une image selon un pourcentage donné (0 = aucune modif, 100 = totalement blanc).
     */
    public static BufferedImage eclaircirImage(BufferedImage image, double pourcentage) {
        int w = image.getWidth();
        int h = image.getHeight();
        BufferedImage resultat = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);

        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int rgb = image.getRGB(x, y);
                int r = (rgb >> 16) & 0xFF;
                int g = (rgb >> 8) & 0xFF;
                int b = rgb & 0xFF;

                // Éclaircissement des composantes RGB
                int nr = (int) Math.round(r + pourcentage / 100.0 * (255 - r));
                int ng = (int) Math.round(g + pourcentage / 100.0 * (255 - g));
                int nb = (int) Math.round(b + pourcentage / 100.0 * (255 - b));

                int newRGB = (nr << 16) | (ng << 8) | nb;
                resultat.setRGB(x, y, newRGB);
            }
        }
        return resultat;
    }

    /**
     * DBSCAN spatial avec grille : accélère la recherche de voisins en découpant l'espace.
     */
    private static int[] dbscanAvecGrille(List<int[]> points, int eps, int minPts) {
        int n = points.size();
        int[] labels = new int[n];       // -1 = bruit, ≥0 = ID de cluster
        Arrays.fill(labels, -1);
        boolean[] visited = new boolean[n];
        int clusterId = 0;

        // Construction d'une grille : chaque cellule contient les indices des points
        Map<String, List<Integer>> grille = new HashMap<>();
        for (int i = 0; i < n; i++) {
            int[] p = points.get(i);
            String key = (p[0] / eps) + "_" + (p[1] / eps);
            grille.computeIfAbsent(key, k -> new ArrayList<>()).add(i);
        }

        // Parcours de chaque point
        for (int i = 0; i < n; i++) {
            if (visited[i]) continue;
            visited[i] = true;

            // Recherche des voisins de i
            List<Integer> voisins = voisinsGrille(points, grille, i, eps);
            if (voisins.size() < minPts) continue;  // bruit

            // Crée un nouveau cluster
            labels[i] = clusterId;
            Queue<Integer> queue = new LinkedList<>(voisins);

            // Expansion du cluster à partir du point i
            while (!queue.isEmpty()) {
                int j = queue.poll();
                if (!visited[j]) {
                    visited[j] = true;
                    List<Integer> voisinsJ = voisinsGrille(points, grille, j, eps);
                    if (voisinsJ.size() >= minPts) {
                        queue.addAll(voisinsJ);
                    }
                }
                if (labels[j] == -1) {
                    labels[j] = clusterId;
                }
            }
            clusterId++;
        }
        return labels;
    }

    /**
     * Recherche des voisins proches via la grille (on ne teste que les cases voisines).
     */
    private static List<Integer> voisinsGrille(List<int[]> points, Map<String, List<Integer>> grille, int index, int eps) {
        int[] p = points.get(index);
        int cx = p[0] / eps;
        int cy = p[1] / eps;
        List<Integer> voisins = new ArrayList<>();

        // On teste les 9 cellules (celle du point et ses voisines)
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                String key = (cx + dx) + "_" + (cy + dy);
                List<Integer> candidats = grille.getOrDefault(key, Collections.emptyList());
                for (int i : candidats) {
                    int[] q = points.get(i);
                    double dist = Math.hypot(p[0] - q[0], p[1] - q[1]);
                    if (dist <= eps) {
                        voisins.add(i);
                    }
                }
            }
        }
        return voisins;
    }
}
package sae.sae_optimisation;

import java.util.*;

public class BiomeLabeler {

    // Correspondance entre noms de biomes et leurs couleurs caractéristiques [R, G, B]
    private static final Map<String, int[]> BIOMES_REFERENCES = new LinkedHashMap<>();

    static {
        BIOMES_REFERENCES.put("Toundra", new int[]{71, 70, 61});
        BIOMES_REFERENCES.put("Taïga", new int[]{43, 50, 35});
        BIOMES_REFERENCES.put("Forêt tempérée", new int[]{59, 66, 43});
        BIOMES_REFERENCES.put("Forêt tropicale", new int[]{46, 64, 34});
        BIOMES_REFERENCES.put("Savane", new int[]{84, 106, 70});
        BIOMES_REFERENCES.put("Prairie", new int[]{104, 95, 82});
        BIOMES_REFERENCES.put("Désert", new int[]{152, 140, 120});
        BIOMES_REFERENCES.put("Glacier", new int[]{200, 200, 200});
        BIOMES_REFERENCES.put("Eau peu profonde", new int[]{49, 83, 100});
        BIOMES_REFERENCES.put("Eau profonde", new int[]{12, 31, 47});
        BIOMES_REFERENCES.put("Montagne", new int[]{100, 100, 100});
    }

    /**
     * Associe chaque centre de cluster à l'étiquette de biome la plus proche.
     * @param moyennes tableau de centroïdes RGB (1 par cluster)
     * @return tableau des noms de biomes affectés à chaque cluster
     */
    public static String[] etiqueterBiomes(double[][] moyennes) {
        String[] etiquettes = new String[moyennes.length];

        for (int i = 0; i < moyennes.length; i++) {
            double minDist = Double.MAX_VALUE;
            String meilleurBiome = "Inconnu";

            for (Map.Entry<String, int[]> entree : BIOMES_REFERENCES.entrySet()) {
                double dist = distance(moyennes[i], entree.getValue());
                if (dist < minDist) {
                    minDist = dist;
                    meilleurBiome = entree.getKey();
                }
            }

            etiquettes[i] = meilleurBiome;

            // Affichage console pour vérification
            System.out.printf(
                    "Cluster %d -> RGB: [%.0f, %.0f, %.0f] -> %s\n",
                    i, moyennes[i][0], moyennes[i][1], moyennes[i][2], meilleurBiome
            );
        }

        return etiquettes;
    }

    /**
     * Calcule la distance euclidienne entre deux couleurs RGB.
     * @param a couleur réelle (double)
     * @param b couleur de référence (int)
     * @return distance euclidienne entre les deux couleurs
     */
    private static double distance(double[] a, int[] b) {
        double somme = 0;
        for (int i = 0; i < a.length; i++) {
            somme += Math.pow(a[i] - b[i], 2);
        }
        return Math.sqrt(somme);
    }
}
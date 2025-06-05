package sae.sae_optimisation;

import java.util.Random;
import java.util.Arrays;

public class KMeans {

    /**
     * Applique l'algorithme K-Means pour regrouper les données en k clusters.
     * @param data Données à regrouper (n objets × d caractéristiques)
     * @param k Nombre de clusters à former
     * @return Tableau des étiquettes (label de cluster pour chaque point)
     */
    public static int[] clusteriser(double[][] data, int k) {
        int n = data.length;           // Nombre de points
        int d = data[0].length;        // Dimension (ex: 3 pour RGB)
        double[][] centres = new double[k][d];  // Centroïdes des clusters
        int[] labels = new int[n];     // Étiquettes des points
        Random rand = new Random();

        // 1. Initialisation aléatoire des centres parmi les données
        for (int i = 0; i < k; i++) {
            centres[i] = data[rand.nextInt(n)].clone();
        }

        boolean changements = true;
        int maxIterations = 100;
        int iteration = 0;

        while (changements && iteration++ < maxIterations) {
            changements = false;

            // 2. Affectation des points au centre le plus proche
            for (int i = 0; i < n; i++) {
                int meilleur = 0;
                double minDist = distance(data[i], centres[0]);
                for (int j = 1; j < k; j++) {
                    double dist = distance(data[i], centres[j]);
                    if (dist < minDist) {
                        minDist = dist;
                        meilleur = j;
                    }
                }
                if (labels[i] != meilleur) {
                    changements = true;
                    labels[i] = meilleur;
                }
            }

            // 3. Recalcul des centres (moyenne des points de chaque cluster)
            double[][] nouveauxCentres = new double[k][d];
            int[] compte = new int[k];

            for (int i = 0; i < n; i++) {
                int label = labels[i];
                for (int j = 0; j < d; j++) {
                    nouveauxCentres[label][j] += data[i][j];
                }
                compte[label]++;
            }

            for (int i = 0; i < k; i++) {
                if (compte[i] == 0) {
                    // 4. Si un cluster est vide, on le réinitialise aléatoirement
                    nouveauxCentres[i] = data[rand.nextInt(n)].clone();
                } else {
                    for (int j = 0; j < d; j++) {
                        nouveauxCentres[i][j] /= compte[i];
                    }
                }
                centres[i] = nouveauxCentres[i];
            }
        }

        return labels;
    }

    /**
     * Calcule la distance euclidienne entre deux points.
     */
    private static double distance(double[] a, double[] b) {
        double somme = 0;
        for (int i = 0; i < a.length; i++) {
            somme += Math.pow(a[i] - b[i], 2);
        }
        return Math.sqrt(somme);
    }

    /**
     * Calcule les centroïdes à partir des affectations finales.
     * @param data Les données d'origine
     * @param labels Étiquettes des points (index du cluster)
     * @param k Nombre total de clusters
     * @return Tableau des centroïdes (1 par cluster)
     */
    public static double[][] calculerCentroides(double[][] data, int[] labels, int k) {
        int d = data[0].length;
        double[][] centres = new double[k][d];
        int[] compte = new int[k];

        for (int i = 0; i < data.length; i++) {
            int label = labels[i];
            for (int j = 0; j < d; j++) {
                centres[label][j] += data[i][j];
            }
            compte[label]++;
        }

        for (int i = 0; i < k; i++) {
            if (compte[i] > 0) {
                for (int j = 0; j < d; j++) {
                    centres[i][j] /= compte[i];
                }
            }
        }

        return centres;
    }
}
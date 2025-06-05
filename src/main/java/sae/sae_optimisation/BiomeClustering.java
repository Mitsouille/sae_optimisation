package sae.sae_optimisation;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.HashSet;
import java.util.Set;

public class BiomeClustering {

    /**
     * Analyse une image pour détecter les biomes (via KMeans) puis les écosystèmes (via DBSCAN ou OPTICS).
     * @param image L'image d'entrée à analyser (souvent floutée pour lisser les détails).
     * @param dossier Le dossier de base pour stocker les résultats.
     * @param nomImage Le nom de l'image analysée.
     * @param algo L'algorithme à utiliser pour les écosystèmes : "DBSCAN" ou "OPTICS"
     */
    public static void analyserImage(BufferedImage image, String dossier, String nomImage, String algo) {
        try {

            //Floute l'image
             image = TraitementImage.appliquerFlouGaussien5x5(image);

            // Vide les anciens résultats
            FileUtils.viderDossier(dossier + "biomes/");
            FileUtils.viderDossier(dossier + "ecosystemes/");

            // Étape 1 : Extraction des données RGB de l'image
            double[][] pixels = ImageProcessor.extraireRGB(image);

            // Étape 2 : Clustering KMeans sur les couleurs pour regrouper les zones en biomes
            int k = 8; // nombre de clusters/biomes
            int[] affectations = KMeans.clusteriser(pixels, k);
            double[][] moyennes = KMeans.calculerCentroides(pixels, affectations, k);
            String[] etiquettesBiomes = BiomeLabeler.etiqueterBiomes(moyennes);

            // Étape 3 : Sauvegarde des images de chaque biome
            for (int i = 0; i < k; i++) {
                BufferedImage biome = TraitementImage.afficherBiome(image, affectations, moyennes, i, etiquettesBiomes[i], 75);
                ImageIO.write(biome, "png", new File(dossier + "biomes/biome_" + etiquettesBiomes[i] + ".png"));
            }

            // Étape 4 : Détection des écosystèmes (groupes spatiaux) dans chaque biome
            Set<String> biomesVisites = new HashSet<>();
            for (String biome : etiquettesBiomes) {
                if (biomesVisites.contains(biome)) continue;
                biomesVisites.add(biome);

                System.out.println("Détection des écosystèmes pour : " + biome);
                BufferedImage img;

                // Choix de l'algorithme pour détecter les écosystèmes
                if (algo.equals("OPTICS")) {
                    img = OpticsClustering.detecterEcosystemesOPTICS(image, affectations, etiquettesBiomes, biome);
                } else {
                    img = EcosystemDetector.detecterEcosystemes(image, affectations, etiquettesBiomes, biome);
                }

                // Sauvegarde de l'image des écosystèmes détectés pour le biome
                ImageIO.write(img, "png", new File(dossier + "ecosystemes/ecosystemes_" + biome + ".png"));
            }

            System.out.println("Tous les résultats ont été générés dans : " + dossier);

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Erreur lors de l’analyse de l’image : " + e.getMessage());
        }
    }
}

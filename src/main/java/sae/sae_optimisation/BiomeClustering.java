package sae.sae_optimisation;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.HashSet;
import java.util.Set;

public class BiomeClustering {

    public static void analyserImage(BufferedImage image, String dossier, String nomImage) {
        try {
            FileUtils.viderDossier(dossier + "biomes/");
            FileUtils.viderDossier(dossier + "ecosystemes/");

            // Étape 1 : extraction des vecteurs [R, G, B]
            double[][] pixels = ImageProcessor.extraireRGB(image);

            // Étape 2.1 : clustering K-Means
            int k = 12;
            int[] affectations = KMeans.clusteriser(pixels, k);

            // Étape 2.2 : calcul des centroïdes + étiquettes
            double[][] moyennes = KMeans.calculerCentroides(pixels, affectations, k);
            String[] etiquettesBiomes = BiomeLabeler.etiqueterBiomes(moyennes);

            // Étape 2.3 : visualisation globale des biomes
            //BufferedImage recoloree = TraitementImage.recolorerImage(image, pixels, affectations, moyennes);
            //ImageIO.write(recoloree, "png", new File(dossier + "biomes_detectes.png"));

            // Images par biome (éclaircies sauf pixels du biome)
            for (int i = 0; i < k; i++) {
                BufferedImage biome = TraitementImage.afficherBiome(image, affectations, moyennes, i, etiquettesBiomes[i], 75);
                ImageIO.write(biome, "png", new File(dossier + "biomes/biome_" + etiquettesBiomes[i] + ".png"));
            }

            // Étape 3 : détection des écosystèmes dans chaque biome
            Set<String> biomesVisites = new HashSet<>();
            for (String biome : etiquettesBiomes) {
                if (biomesVisites.contains(biome)) continue;
                biomesVisites.add(biome);

                System.out.println("Détection des écosystèmes pour : " + biome);
                BufferedImage img = EcosystemDetector.detecterEcosystemes(image, affectations, etiquettesBiomes, biome);
                ImageIO.write(img, "png", new File(dossier + "ecosystemes/ecosystemes_" + biome + ".png"));
            }

            System.out.println("Tous les résultats ont été générés dans : " + dossier);

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Erreur lors de l’analyse de l’image : " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        try {
            String dossier = TraitementImage.PATH_IMG;
            ImageIO.write(TraitementImage.appliquerFlouMoyenne(ImageIO.read(new File(dossier + "Planete 1.jpg"))), "jpg", new File(dossier + "flou/flou_moyenne.jpg"));
            String nomImage = "flou/flou_moyenne.jpg";
            System.out.println("Analyse de l'image : " + dossier + nomImage);
            BufferedImage image = ImageIO.read(new File(dossier + nomImage));
            analyserImage(image, dossier, nomImage);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
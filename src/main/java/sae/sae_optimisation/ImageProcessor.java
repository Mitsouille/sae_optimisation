package sae.sae_optimisation;

import java.awt.image.BufferedImage;

public class ImageProcessor {
    public static double[][] extraireRGB(BufferedImage image) {
        int largeur = image.getWidth();
        int hauteur = image.getHeight();
        double[][] donnees = new double[largeur * hauteur][3];

        int index = 0;
        for (int y = 0; y < hauteur; y++) {
            for (int x = 0; x < largeur; x++) {
                int rgb = image.getRGB(x, y);
                donnees[index][0] = (rgb >> 16) & 0xFF; // Rouge
                donnees[index][1] = (rgb >> 8) & 0xFF;  // Vert
                donnees[index][2] = rgb & 0xFF;         // Bleu
                index++;
            }
        }
        return donnees;
    }

    public static double[][] extraireRGBReduit(BufferedImage image, int i) {
        int largeur = image.getWidth();
        int hauteur = image.getHeight();
        double[][] donnees = new double[(largeur * hauteur) / i][3];

        int index = 0;
        for (int y = 0; y < hauteur; y += i) {
            for (int x = 0; x < largeur; x += i) {
                if (x < largeur && y < hauteur) {
                    int rgb = image.getRGB(x, y);
                    donnees[index][0] = (rgb >> 16) & 0xFF; // Rouge
                    donnees[index][1] = (rgb >> 8) & 0xFF;  // Vert
                    donnees[index][2] = rgb & 0xFF;         // Bleu
                    index++;
                }
            }
        }
        return donnees;
    }
}
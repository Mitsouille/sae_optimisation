package sae.sae_optimisation;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class TraitementImage {

    public static void main(String[] args) {
        try {

            String base = "img/";
            BufferedImage image = ImageIO.read(new File(base + "Planete 1.jpg"));

            BufferedImage flouMoyenne = appliquerFlouMoyenne(image);

            BufferedImage flouGaussien = image;

            flouGaussien = appliquerFlouGaussien5x5(flouGaussien);

            ImageIO.write(flouMoyenne, "jpg", new File("img/flou_moyenne.jpg"));
            ImageIO.write(flouGaussien, "jpg", new File("img/flou_gaussien.jpg"));

            System.out.println("Filtres appliqués et images enregistrées.");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static BufferedImage appliquerFlouMoyenne(BufferedImage img) {
        int width = img.getWidth();
        int height = img.getHeight();
        BufferedImage result = new BufferedImage(width, height, img.getType());
        int taille = 5;
        int kernelSum = taille * taille;
        for (int x = 2; x < width - 2; x++) {
            for (int y = 2; y < height - 2; y++) {
                int r = 0, g = 0, b = 0;

                for (int i = -2; i <= 2; i++) {
                    for (int j = -2; j <= 2; j++) {
                        int rgb = img.getRGB(x + i, y + j);
                        r += (rgb >> 16) & 0xFF;
                        g += (rgb >> 8) & 0xFF;
                        b += rgb & 0xFF;
                    }
                }

                r /= kernelSum;
                g /= kernelSum;
                b /= kernelSum;

                int newRGB = (r << 16) | (g << 8) | b;
                result.setRGB(x, y, newRGB);
            }
        }

        return result;
    }

    public static BufferedImage appliquerFlouGaussien3x3(BufferedImage img) {
        int width = img.getWidth();
        int height = img.getHeight();
        BufferedImage result = new BufferedImage(width, height, img.getType());

        int[][] kernel = {
                { 1, 2, 1 },
                { 2, 4, 2 },
                { 1, 2, 1 }
        };
        int kernelSum = 16;
        for (int x = 1; x < width - 1; x++) {
            for (int y = 1; y < height - 1; y++) {
                int r = 0, g = 0, b = 0;

                for (int i = 0; i < 3; i++) {
                    for (int j = 0; j < 3; j++) {
                        int rgb = img.getRGB(x + i - 1, y + j - 1);
                        r += ((rgb >> 16) & 0xFF) * kernel[i][j];
                        g += ((rgb >> 8) & 0xFF) * kernel[i][j];
                        b += (rgb & 0xFF) * kernel[i][j];
                    }
                }

                r /= kernelSum;
                g /= kernelSum;
                b /= kernelSum;

                int newRGB = (r << 16) | (g << 8) | b;
                result.setRGB(x, y, newRGB);
            }
        }

        return result;
    }

    public static BufferedImage appliquerFlouGaussien5x5(BufferedImage img) {
        int width = img.getWidth();
        int height = img.getHeight();
        BufferedImage result = new BufferedImage(width, height, img.getType());

        int[][] kernel = {
                { 1, 4, 6, 4, 1 },
                { 4, 16, 24, 16, 4 },
                { 6, 24, 36, 24, 6 },
                { 4, 16, 24, 16, 4 },
                { 1, 4, 6, 4, 1 }
        };
        int kernelSum = 256;

        for (int x = 2; x < width - 2; x++) {
            for (int y = 2; y < height - 2; y++) {
                int r = 0, g = 0, b = 0;

                for (int i = 0; i < 5; i++) {
                    for (int j = 0; j < 5; j++) {
                        int rgb = img.getRGB(x + i - 2, y + j - 2);
                        r += ((rgb >> 16) & 0xFF) * kernel[i][j];
                        g += ((rgb >> 8) & 0xFF) * kernel[i][j];
                        b += (rgb & 0xFF) * kernel[i][j];
                    }
                }

                r /= kernelSum;
                g /= kernelSum;
                b /= kernelSum;

                int newRGB = (r << 16) | (g << 8) | b;
                result.setRGB(x, y, newRGB);
            }
        }

        return result;
    }
}

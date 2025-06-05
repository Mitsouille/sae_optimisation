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
                double dist = distanceHumain(moyennes[i], entree.getValue());
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

    /**
     * Calcule la distance avec la perception entre deux couleurs RGB.
     * @param testedColor couleur réelle (double)
     * @param biomeColor couleur de référence (int)
     * @return distance euclidienne entre les deux couleurs
     */
    private static double distanceHumain(double[] testedColor, int[] biomeColor){
        int r = (int) testedColor[0];
        int g = (int) testedColor[1];
        int b = (int) testedColor[2];

        int rB = biomeColor[0];
        int gB = biomeColor[1];
        int bB = biomeColor[2];

        double[] testedLab = rgb2lab(r,g,b);
        double[] biomeLab = rgb2lab(rB, gB, bB);

        double deltaL = testedLab[0] - biomeLab[0];
        double C1 = Math.sqrt(Math.pow(testedLab[1],2) + Math.pow(testedLab[2],2));
        double C2 = Math.sqrt(Math.pow(biomeLab[1],2) + Math.pow(biomeLab[2],2));

        double deltaC = C1 - C2;

        double aCarre = Math.pow(testedLab[1] - biomeLab[1], 2);
        double bCarre = Math.pow(testedLab[2] - biomeLab[2], 2);

        double deltaH = Math.sqrt((aCarre + bCarre - Math.pow(deltaC,2)));

        double sC = 1 + 0.045 * C1;
        double sH = 1 + 0.015 * C1;



        double e94 = Math.sqrt(Math.pow((deltaL / 1), 2) + Math.pow((deltaC / sC), 2) + Math.pow((deltaH / sH), 2));

        return e94;
    }

    public static double[] rgb2lab(int R, int G, int B) {
        // http://www.brucelindbloom.com

        float r, g, b, X, Y, Z, fx, fy, fz, xr, yr, zr;
        float Ls, as, bs;
        float eps = 216.f / 24389.f;
        float k = 24389.f / 27.f;

        float Xr = 0.964221f; // reference white D50
        float Yr = 1.0f;
        float Zr = 0.825211f;

        // RGB to XYZ
        r = R / 255.f; // R 0..1
        g = G / 255.f; // G 0..1
        b = B / 255.f; // B 0..1

        // assuming sRGB (D65)
        if (r <= 0.04045)
            r = (float) (r / 12.92);/*from  www. jav a2 s.com*/
        else
            r = (float) Math.pow((r + 0.055) / 1.055, 2.4);

        if (g <= 0.04045)
            g = (float) (g / 12.92);
        else
            g = (float) Math.pow((g + 0.055) / 1.055, 2.4);

        if (b <= 0.04045)
            b = b / 12;
        else
            b = (float) Math.pow((b + 0.055) / 1.055, 2.4);

        X = 0.436052025f * r + 0.385081593f * g + 0.143087414f * b;
        Y = 0.222491598f * r + 0.71688606f * g + 0.060621486f * b;
        Z = 0.013929122f * r + 0.097097002f * g + 0.71418547f * b;

        // XYZ to Lab
        xr = X / Xr;
        yr = Y / Yr;
        zr = Z / Zr;

        if (xr > eps)
            fx = (float) Math.pow(xr, 1 / 3.);
        else
            fx = (float) ((k * xr + 16.) / 116.);

        if (yr > eps)
            fy = (float) Math.pow(yr, 1 / 3.);
        else
            fy = (float) ((k * yr + 16.) / 116.);

        if (zr > eps)
            fz = (float) Math.pow(zr, 1 / 3.);
        else
            fz = (float) ((k * zr + 16.) / 116);

        Ls = (116 * fy) - 16;
        as = 500 * (fx - fy);
        bs = 200 * (fy - fz);

        double[] lab = new double[3];
        lab[0] = Ls;
        lab[1] = as;
        lab[2] = bs;
        return lab;
    }
}
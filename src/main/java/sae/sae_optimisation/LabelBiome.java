package sae.sae_optimisation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class LabelBiome {
    public static HashMap<List<Integer>, String> RGB_TO_BIOME = creerMapRGBtoBiome();

    private static HashMap<List<Integer>, String> creerMapRGBtoBiome(){
        HashMap<List<Integer>, String> res = new HashMap<>();

        res.put(Arrays.asList(71, 70, 61), "Tundra");
        res.put(Arrays.asList(43, 50, 35), "Taïga");
        res.put(Arrays.asList(59, 66, 43), "Forêt tempérée");
        res.put(Arrays.asList(46, 64, 34),"Forêt tropicale");
        res.put(Arrays.asList(84, 106, 70),"Savane");
        res.put(Arrays.asList(104, 95, 82),"Prairie");
        res.put(Arrays.asList(152, 140, 120),"Désert");
        res.put(Arrays.asList(200, 200, 200),"Glacier");
        res.put(Arrays.asList(49, 83, 100),"Eau peu profonde");
        res.put(Arrays.asList(12, 31, 47),"Eau profonde");

        return res;
    }

    public static String getBiomeFromRGB(int rgb){
        double dMin = Double.MAX_VALUE;
        List<Integer> bestColor = new ArrayList<>();
        for(List<Integer> colorBiome : RGB_TO_BIOME.keySet()){
            int rb = colorBiome.get(0);
            int gb = colorBiome.get(1);
            int bb = colorBiome.get(2);

            int r = (rgb >> 16) & 0xFF;
            int g = (rgb >> 8) & 0xFF;
            int b = rgb & 0xFF;

            double dist = Math.pow(rb - r, 2) + Math.pow(gb - g, 2) + Math.pow(bb - b, 2);

            if(dMin > dist){
                dMin = dist;
                bestColor = colorBiome;
            }
        }
        return RGB_TO_BIOME.get(bestColor);
    }
}



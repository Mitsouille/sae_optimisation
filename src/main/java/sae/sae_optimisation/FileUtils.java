package sae.sae_optimisation;

import java.io.File;

public class FileUtils {
    public static void viderDossier(String chemin) {
        File dossier = new File(chemin);
        if (dossier.exists() && dossier.isDirectory()) {
            for (File fichier : dossier.listFiles()) {
                if (fichier.isFile()) {
                    fichier.delete();
                }
            }
        }
    }
}
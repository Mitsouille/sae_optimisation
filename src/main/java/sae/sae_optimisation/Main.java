package sae.sae_optimisation;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.*;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class Main extends Application {

    private static final String BASE_PATH = "src/main/resources/planete/";
    private static final String FLOU_PATH = BASE_PATH + "flou/";
    private static final String BIOME_PATH = BASE_PATH + "biomes/";
    private static final String ECOSYS_PATH = BASE_PATH + "ecosystemes/";

    private File selectedImage;
    private VBox imageDisplayBox;
    private Label statusLabel = new Label("Image non sélectionnée.");

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Explorateur de biomes et écosystèmes");

        TreeView<String> fileTree = new TreeView<>();
        TreeItem<String> rootItem = new TreeItem<>("Images disponibles");
        rootItem.setExpanded(true);
        fileTree.setRoot(rootItem);

        buildFileTree(new File(BASE_PATH), rootItem);

        fileTree.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && newVal.isLeaf()) {
                TreeItem<String> parent = newVal.getParent();
                if (parent != null && parent.getValue().equals("Images disponibles")) {
                    File file = new File(BASE_PATH + newVal.getValue());
                    if (file.exists() && file.isFile()) {
                        selectedImage = file;
                        showSelectedImage(file);
                        statusLabel.setText("Image sélectionnée : " + file.getName());
                    }
                }
            }
        });

        Button detectButton = new Button("Analyser image");
        detectButton.setOnAction(e -> detectBiomesAndEcosystems());

        VBox controls = new VBox(10, detectButton, statusLabel);
        controls.setPadding(new Insets(10));

        imageDisplayBox = new VBox(20);
        imageDisplayBox.setPadding(new Insets(10));
        ScrollPane scrollPane = new ScrollPane(imageDisplayBox);
        scrollPane.setFitToWidth(true);

        HBox layout = new HBox(10, fileTree, controls, scrollPane);
        layout.setPadding(new Insets(10));

        primaryStage.setScene(new Scene(layout, 1400, 800));
        primaryStage.show();
    }

    private void buildFileTree(File dir, TreeItem<String> parent) {
        for (File file : dir.listFiles()) {
            if (file.isFile() && file.getName().matches(".*\\.(png|jpg|jpeg)")) {
                if (!file.getName().startsWith("biomes_detectes")) {
                    parent.getChildren().add(new TreeItem<>(file.getName()));
                }
            }
        }
    }

    private void showSelectedImage(File file) {
        imageDisplayBox.getChildren().clear();
        imageDisplayBox.getChildren().add(new Label("Image d'origine : " + file.getName()));
        imageDisplayBox.getChildren().add(loadImageView(file));
    }

    private void detectBiomesAndEcosystems() {
        if (selectedImage == null) {
            statusLabel.setText("Veuillez sélectionner une image d'abord.");
            return;
        }

        try {
            BufferedImage img = ImageIO.read(selectedImage);
            if (img == null) {
                statusLabel.setText("Erreur : image non lisible.");
                return;
            }

            BiomeClustering.analyserImage(img, BASE_PATH, selectedImage.getName());

            imageDisplayBox.getChildren().add(new Label("\nBiomes détectés :"));
            for (File f : new File(BIOME_PATH).listFiles()) {
                if (f.getName().endsWith(".png")) {
                    imageDisplayBox.getChildren().add(new Label(f.getName()));
                    imageDisplayBox.getChildren().add(loadImageView(f));
                }
            }

            imageDisplayBox.getChildren().add(new Label("\nÉcosystèmes détectés :"));
            for (File f : new File(ECOSYS_PATH).listFiles()) {
                if (f.getName().endsWith(".png")) {
                    imageDisplayBox.getChildren().add(new Label(f.getName()));
                    imageDisplayBox.getChildren().add(loadImageView(f));
                }
            }

            statusLabel.setText("Analyse terminée. Résultats affichés ci-dessous.");

        } catch (IOException e) {
            e.printStackTrace();
            statusLabel.setText("Erreur pendant l'analyse.");
        }
    }

    private ImageView loadImageView(File file) {
        try {
            Image img = new Image(new FileInputStream(file));
            ImageView iv = new ImageView(img);
            iv.setFitWidth(600);
            iv.setPreserveRatio(true);
            return iv;
        } catch (Exception e) {
            System.err.println("Erreur chargement image : " + file.getName());
            return new ImageView();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
module org.example.sae_optimisation {
    requires javafx.controls;
    requires javafx.fxml;


    opens org.example.sae_optimisation to javafx.fxml;
    exports org.example.sae_optimisation;
}
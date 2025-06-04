module sae.sae_optimisation {
    requires javafx.controls;
    requires javafx.fxml;


    opens sae.sae_optimisation to javafx.fxml;
    exports sae.sae_optimisation;
}
module com.paint021 {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.swing;

    requires org.controlsfx.controls;
    requires org.kordamp.bootstrapfx.core;

    opens com.paint021 to javafx.fxml;
    exports com.paint021;
}
module org.pcsoft.app.aighost.ui {
    requires javafx.controls;
    requires javafx.fxml;
    requires kotlin.stdlib;

    requires org.controlsfx.controls;

    opens org.pcsoft.app.aighost.app to javafx.fxml;
    exports org.pcsoft.app.aighost.app;
}

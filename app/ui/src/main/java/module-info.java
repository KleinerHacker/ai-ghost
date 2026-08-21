module org.pcsoft.app.aighost.ui {
    requires javafx.controls;
    requires javafx.fxml;
    requires kotlin.stdlib;

    requires org.controlsfx.controls;
    requires de.saxsys.mvvmfx;

    opens org.pcsoft.app.aighost.app to javafx.fxml, de.saxsys.mvvmfx;
    opens org.pcsoft.app.aighost.app.ui.window to javafx.fxml, de.saxsys.mvvmfx;

    exports org.pcsoft.app.aighost.app;
    exports org.pcsoft.app.aighost.app.ui.window;
}

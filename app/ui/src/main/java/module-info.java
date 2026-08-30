module org.pcsoft.app.aighost.ui {
    requires javafx.controls;
    requires javafx.fxml;
    requires kotlin.stdlib;
    requires org.slf4j;
    requires org.apache.commons.lang3;

    requires org.controlsfx.controls;
    requires de.saxsys.mvvmfx;

    requires org.pcsoft.app.aighost.model;
    // The views bind onto the property models of the open project and the preferences.
    requires org.pcsoft.app.aighost.fx.model;
    // The prompt area estimates the token cost of what the user wrote.
    requires org.pcsoft.app.aighost.ai;

    opens org.pcsoft.app.aighost.app to javafx.fxml, de.saxsys.mvvmfx;
    opens org.pcsoft.app.aighost.app.ui.window to javafx.fxml, de.saxsys.mvvmfx;
    opens org.pcsoft.app.aighost.app.ui.dialog to javafx.fxml, de.saxsys.mvvmfx;
    opens org.pcsoft.app.aighost.app.ui.component to javafx.fxml, de.saxsys.mvvmfx;

    exports org.pcsoft.app.aighost.app;
    // The font catalogue, the resolution of a design font and the JavaFX backed text measuring.
    exports org.pcsoft.app.aighost.app.font;
    exports org.pcsoft.app.aighost.app.ui;
    exports org.pcsoft.app.aighost.app.ui.window;
    exports org.pcsoft.app.aighost.app.ui.dialog;
    exports org.pcsoft.app.aighost.app.ui.component;
}

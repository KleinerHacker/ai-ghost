module org.pcsoft.app.aighost.ui {
    requires javafx.controls;
    requires javafx.fxml;
    requires kotlin.stdlib;
    requires org.slf4j;
    requires org.apache.commons.lang3;

    requires org.controlsfx.controls;
    requires de.saxsys.mvvmfx;
    // The startup area scans its step package for StartupStep implementations.
    requires io.github.classgraph;

    requires org.pcsoft.app.aighost.model;
    // The views bind onto the property models of the open project and the preferences.
    requires org.pcsoft.app.aighost.fx.model;
    // The prompt area estimates the token cost of what the user wrote.
    requires org.pcsoft.app.aighost.ai;
    // The JavaFX text measuring implements the measuring interface of the layout core.
    requires transitive org.pcsoft.app.aighost.layouting;
    // The fingerprint a font family is recognised by is measured by the component library.
    requires org.pcsoft.app.aighost.layouting.fx;

    opens org.pcsoft.app.aighost.app to javafx.fxml, de.saxsys.mvvmfx;
    opens org.pcsoft.app.aighost.app.ui.window to javafx.fxml, de.saxsys.mvvmfx;
    opens org.pcsoft.app.aighost.app.ui.dialog to javafx.fxml, de.saxsys.mvvmfx;
    opens org.pcsoft.app.aighost.app.ui.component to javafx.fxml, de.saxsys.mvvmfx;
    // ClassGraph reads the class files of the startup steps to discover them.
    opens org.pcsoft.app.aighost.app.startup.step to io.github.classgraph;

    exports org.pcsoft.app.aighost.app;
    // The font catalogue, the resolution of a design font and the JavaFX backed text measuring.
    exports org.pcsoft.app.aighost.app.font;
    exports org.pcsoft.app.aighost.app.ui;
    exports org.pcsoft.app.aighost.app.ui.window;
    exports org.pcsoft.app.aighost.app.ui.dialog;
    exports org.pcsoft.app.aighost.app.ui.component;
}

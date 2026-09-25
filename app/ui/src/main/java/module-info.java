module org.pcsoft.app.aighost.ui {
    requires javafx.controls;
    requires javafx.fxml;
    requires kotlin.stdlib;
    // AiProviderRegistry/ConfigModelReader (org.pcsoft.app.aighost.app.plugin) reflect on a
    // provider's configuration class.
    requires kotlin.reflect;
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
    // AiProviderExtensionConfig, the "ai" extension point pluggiat resolves a plugin's manifest
    // entries against - and transitively AiProvider/AiProviderConfig/etc. from the plugin API.
    requires org.pcsoft.app.aighost.plugin.system;
    // PluginLoadStartupStep builds and runs pluggiat's own PluginManager directly to discover and
    // register AI provider plugins before the first window.
    requires pluggiat;
    // The block builders turn a book part, the title page and the copyright page into layout input;
    // its `requires transitive` on simPlay's engine module makes simPlay's raw model types (Font,
    // FontFingerprint, ...) visible here as well.
    requires org.pcsoft.app.aighost.layouting.model;
    // The JavaFX font probe: checks family availability, stamps and verifies the measurement
    // fingerprint stored beside a design's fonts.
    requires org.pcsoft.framework.simplay.fx;
    // FontAvailability, the outcome enum FxFontProbe.checkAvailability answers with.
    requires org.pcsoft.framework.simplay.common;

    opens org.pcsoft.app.aighost.app to javafx.fxml, de.saxsys.mvvmfx;
    opens org.pcsoft.app.aighost.app.ui.window to javafx.fxml, de.saxsys.mvvmfx;
    opens org.pcsoft.app.aighost.app.ui.dialog to javafx.fxml, de.saxsys.mvvmfx;
    opens org.pcsoft.app.aighost.app.ui.component to javafx.fxml, de.saxsys.mvvmfx;
    // ClassGraph reads the class files of the startup steps to discover them.
    opens org.pcsoft.app.aighost.app.startup.step to io.github.classgraph;

    exports org.pcsoft.app.aighost.app;
    // The font identity check of a design and the translation to the renderer library types.
    exports org.pcsoft.app.aighost.app.font;
    exports org.pcsoft.app.aighost.app.ui;
    exports org.pcsoft.app.aighost.app.ui.window;
    exports org.pcsoft.app.aighost.app.ui.dialog;
    exports org.pcsoft.app.aighost.app.ui.component;
}

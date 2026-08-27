module org.pcsoft.app.aighost.fx.model {
    requires kotlin.stdlib;

    // The JavaFX property system is the reason this module exists, so consumers see it transitively.
    requires transitive javafx.base;

    // The observable wrappers expose the plain model types, so consumers need them as well.
    requires transitive org.pcsoft.app.aighost.model;

    // The storage entry point and the property models it hands out are the public surface.
    exports org.pcsoft.app.aighost.fx.model;
    exports org.pcsoft.app.aighost.fx.model.pref;
    exports org.pcsoft.app.aighost.fx.model.project;

    // A plugin builds the property model of its own project part on these wrappers.
    exports org.pcsoft.app.aighost.fx.model.property.common;
}

module org.pcsoft.app.aighost.fx.model {
    requires kotlin.stdlib;

    // The JavaFX property system is the reason this module exists, so consumers see it transitively.
    requires transitive javafx.base;

    // The observable wrappers expose the plain model types, so consumers need them as well.
    requires transitive org.pcsoft.app.aighost.model;

    // The property models mirroring the plain model types are the public surface.
    exports org.pcsoft.app.aighost.fx.model.pref;
    exports org.pcsoft.app.aighost.fx.model.project;

    // The manuscript is edited in the user interface, so its property model is part of that surface.
    exports org.pcsoft.app.aighost.fx.model.project.book;

    // Every nested property model is handed out with its own type, so the packages carrying those
    // types belong to the public surface as well - otherwise the module system rejects them.
    exports org.pcsoft.app.aighost.fx.model.common;
    exports org.pcsoft.app.aighost.fx.model.project.common;
    exports org.pcsoft.app.aighost.fx.model.project.design;
    exports org.pcsoft.app.aighost.fx.model.project.meta;
}

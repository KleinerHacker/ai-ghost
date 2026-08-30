module org.pcsoft.app.aighost.model {
    requires kotlin.stdlib;

    // The plugin API is part of the model's public surface.
    requires transitive org.pcsoft.app.aighost.plugin.api;

    // Jackson is part of the public API: consumers create their own ObjectMapper for these types.
    // The Kotlin module publishes itself as "com.fasterxml.jackson.kotlin", not by its artifact name.
    requires transitive com.fasterxml.jackson.annotation;
    requires transitive com.fasterxml.jackson.core;
    requires transitive com.fasterxml.jackson.databind;
    requires transitive com.fasterxml.jackson.kotlin;

    // Arrow's Either appears in the signatures of the storage, so consumers need it as well.
    requires transitive arrow.core;

    requires org.slf4j;

    exports org.pcsoft.app.aighost.model;
    exports org.pcsoft.app.aighost.model.common;
    exports org.pcsoft.app.aighost.model.pref;
    exports org.pcsoft.app.aighost.model.project;
    exports org.pcsoft.app.aighost.model.project.book;
    exports org.pcsoft.app.aighost.model.project.common;
    exports org.pcsoft.app.aighost.model.project.design;
    exports org.pcsoft.app.aighost.model.project.meta;

    // Jackson reflects on the data classes when reading and writing JSON.
    opens org.pcsoft.app.aighost.model.common to com.fasterxml.jackson.databind, com.fasterxml.jackson.kotlin;
    opens org.pcsoft.app.aighost.model.pref to com.fasterxml.jackson.databind, com.fasterxml.jackson.kotlin;
    opens org.pcsoft.app.aighost.model.project to com.fasterxml.jackson.databind, com.fasterxml.jackson.kotlin;
    opens org.pcsoft.app.aighost.model.project.book to com.fasterxml.jackson.databind, com.fasterxml.jackson.kotlin;
    opens org.pcsoft.app.aighost.model.project.common to com.fasterxml.jackson.databind, com.fasterxml.jackson.kotlin;
    opens org.pcsoft.app.aighost.model.project.design to com.fasterxml.jackson.databind, com.fasterxml.jackson.kotlin;
    opens org.pcsoft.app.aighost.model.project.meta to com.fasterxml.jackson.databind, com.fasterxml.jackson.kotlin;
}

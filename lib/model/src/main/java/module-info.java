module org.pcsoft.app.aighost.model {
    requires kotlin.stdlib;

    // Jackson is part of the public API: consumers create their own ObjectMapper for these types.
    // The Kotlin module publishes itself as "com.fasterxml.jackson.kotlin", not by its artifact name.
    requires transitive com.fasterxml.jackson.annotation;
    requires transitive com.fasterxml.jackson.databind;
    requires transitive com.fasterxml.jackson.kotlin;

    // Arrow's Either appears in the signatures of the storage, so consumers need it as well.
    // requires transitive arrow.core;

    exports org.pcsoft.app.aighost.model;
    exports org.pcsoft.app.aighost.model.common;
    exports org.pcsoft.app.aighost.model.pref;
    exports org.pcsoft.app.aighost.model.project;

    // Jackson reflects on the data classes when reading and writing JSON.
    opens org.pcsoft.app.aighost.model.common to com.fasterxml.jackson.databind, com.fasterxml.jackson.kotlin;
    opens org.pcsoft.app.aighost.model.pref to com.fasterxml.jackson.databind, com.fasterxml.jackson.kotlin;
    opens org.pcsoft.app.aighost.model.project to com.fasterxml.jackson.databind, com.fasterxml.jackson.kotlin;
}

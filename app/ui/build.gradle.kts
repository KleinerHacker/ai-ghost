plugins {
    application
    id("org.javamodularity.moduleplugin")
    id("org.openjfx.javafxplugin")
    id("org.beryx.jlink")
}

application {
    mainModule.set("org.pcsoft.app.aighost.app")
    mainClass.set("org.pcsoft.app.aighost.app.LauncherKt")
}

javafx {
    version = "25.0.1"
    modules = listOf("javafx.controls", "javafx.fxml")
}

dependencies {
    implementation("org.controlsfx:controlsfx:11.2.1")
}

jlink {
    imageZip.set(layout.buildDirectory.file("/distributions/app-${javafx.platform.classifier}.zip"))
    options.set(listOf("--strip-debug", "--compress", "zip-6", "--no-header-files", "--no-man-pages"))
    launcher {
        name = "app"
    }
}

plugins {
    application
    id("org.javamodularity.moduleplugin") version "2.0.1"
    id("org.openjfx.javafxplugin") version "0.1.0"
    id("org.beryx.jlink") version "4.1.1"
}

application {
    mainModule.set("org.pcsoft.app.aighost.ui")
    mainClass.set("org.pcsoft.app.aighost.app.LauncherKt")
    applicationName = "ghost-ui"
}

javafx {
    version = "25.0.1"
    modules = listOf("javafx.controls", "javafx.fxml")
}

dependencies {
    implementation("org.controlsfx:controlsfx:11.2.1")
    implementation("de.saxsys:mvvmfx:1.8.0")
}

jlink {
    imageZip.set(layout.buildDirectory.file("/distributions/ghost-ui-image-${javafx.platform.classifier}.zip"))
    options.set(listOf("--strip-debug", "--compress", "zip-6", "--no-header-files", "--no-man-pages"))
    launcher {
        name = "ghost-ui"
    }
}

// The shipped archive: the two start scripts at the root, every JAR below "libs". The layout the
// application plugin produces (bin/ + lib/, wrapped in a version folder) is not what we ship, so its
// archive tasks are disabled in favour of `packageDist`.
tasks.named("distZip") { enabled = false }
tasks.named("distTar") { enabled = false }

val packageDist = tasks.register<Zip>("packageDist") {
    group = "distribution"
    description = "Packs the application into a ZIP: start scripts at the root, all JARs in libs"

    archiveBaseName.set("ghost-ui")
    archiveVersion.set(project.version.toString())
    // JavaFX resolves to the classifier of the building host, so the archive names that host.
    archiveClassifier.set(javafx.platform.classifier)
    destinationDirectory.set(layout.buildDirectory.dir("distributions"))

    // No version folder inside the archive - the scripts sit directly at the root.
    into("")

    from(layout.projectDirectory.dir("src/dist")) {
        filePermissions { unix("0755") }
    }

    into("libs") {
        from(tasks.named("jar"))
        from(configurations.named("runtimeClasspath"))
    }
}

tasks.named("assemble") {
    dependsOn(packageDist)
}

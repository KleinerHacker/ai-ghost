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

val testFxVersion = "4.0.18"

dependencies {
    implementation(project(":lib:ai-ghost-model"))
    implementation("io.arrow-kt:arrow-core:2.1.2")

    implementation("org.controlsfx:controlsfx:11.2.4")
    implementation("de.saxsys:mvvmfx:1.8.0")

    testImplementation("org.testfx:testfx-core:${testFxVersion}")
    // TestFX still pulls the JUnit artifacts of its own generation, which collide with the platform used here.
    testImplementation("org.testfx:testfx-junit5:${testFxVersion}") {
        exclude(group = "org.junit.jupiter")
        exclude(group = "org.junit.platform")
    }
    // Monocle provides the headless glass platform TestFX runs on, so no window is opened.
    testImplementation("org.testfx:openjfx-monocle:21.0.2")
}

tasks.withType<Test> {
    // Monocle reaches into internals of javafx.graphics, which the module path does not allow, and
    // ResourceBundle.Control is unsupported inside a named module - so the tests run on the classpath.
    extensions.getByType(org.javamodularity.moduleplugin.extensions.TestModuleOptions::class.java)
        .runOnClasspath = true

    // The UI tests must not open a window.
    systemProperty("testfx.robot", "glass")
    systemProperty("testfx.headless", "true")
    systemProperty("glass.platform", "Monocle")
    systemProperty("monocle.platform", "Headless")
    systemProperty("prism.order", "sw")
    systemProperty("java.awt.headless", "true")
}

// The Ghost Writer font is drawn by the vector generator in `tools/font`. The generated file is also
// checked in, so a machine without Python still builds: the task then keeps the committed font.
val fontGeneratorDir = rootProject.layout.projectDirectory.dir("tools/font")
val fontFile = layout.projectDirectory.file("src/main/resources/fonts/GhostWriter-Regular.ttf")

val generateFont = tasks.register("generateFont") {
    group = "build"
    description = "Generates the Ghost Writer TrueType font from the vector generator in tools/font"

    inputs.dir(fontGeneratorDir).withPropertyName("generator")
    outputs.file(fontFile).withPropertyName("font")

    val generatorDir = fontGeneratorDir.asFile
    val target = fontFile.asFile

    doLast {
        val interpreter = listOf("python", "python3").firstOrNull { candidate ->
            runCatching {
                ProcessBuilder(candidate, "-c", "import fontTools")
                    .redirectErrorStream(true)
                    .start()
                    .waitFor() == 0
            }.getOrDefault(false)
        }

        if (interpreter == null) {
            logger.lifecycle("Font generation skipped: no Python with fontTools found, keeping the committed font")
            return@doLast
        }

        val process = ProcessBuilder(interpreter, "build_font.py", target.absolutePath)
            .directory(generatorDir)
            .redirectErrorStream(true)
            .start()
        val output = process.inputStream.bufferedReader().readText()
        check(process.waitFor() == 0) { "Font generation failed:\n$output" }
        logger.lifecycle(output.trim())
    }
}

tasks.named("processResources") {
    dependsOn(generateFont)
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

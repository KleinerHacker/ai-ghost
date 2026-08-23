/*
 * Copyright (c) KleinerHacker alias Pfeiffer C Soft 2026.
 * This work is licensed under the Apache License, Version 2.0.
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at:
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, this software is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations.
 */

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
val log4jVersion = "2.25.2"

dependencies {
    implementation(project(":lib:ai-ghost-model"))
    implementation(project(":lib:ai-ghost-fx-model"))

    implementation("io.arrow-kt:arrow-core:2.1.2")
    implementation("org.apache.commons:commons-lang3:3.19.0")

    implementation("org.controlsfx:controlsfx:11.2.4")
    implementation("de.saxsys:mvvmfx:1.8.0")

    // The application logs against the SLF4J API only; Log4j 2 is the implementation behind it and
    // is bound through the SLF4J provider, so no code ever touches a Log4j type.
    implementation("org.slf4j:slf4j-api:2.0.17")
    runtimeOnly("org.apache.logging.log4j:log4j-core:${log4jVersion}")
    runtimeOnly("org.apache.logging.log4j:log4j-slf4j2-impl:${log4jVersion}")

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

// The Ghost Writer font is drawn by the vector generator in `tools/font`. The generated file is
// checked in and is what every build ships, so the generator is only run when somebody asks for it:
// the task hangs on no other task and has to be called on purpose after the generator was changed.
val fontGeneratorDir = rootProject.layout.projectDirectory.dir("tools/font")
val fontFile = layout.projectDirectory.file("src/main/resources/fonts/GhostWriter-Regular.ttf")

tasks.register("generateFont") {
    group = "font"
    description = "Regenerates the checked in Ghost Writer TrueType font from the vector generator " +
            "in tools/font. Needs Python with fontTools and is never run by another task."

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

        // The task is only started on purpose, so doing nothing would leave the caller with an
        // unchanged font and no idea why.
        checkNotNull(interpreter) {
            "Font generation needs Python with fontTools, see tools/font/requirements.txt"
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

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

import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    `java-library`
    id("org.openjfx.javafxplugin") version "0.1.0"
}

// The one library module allowed to carry JavaFX: it draws the result of the layout core onto a
// canvas, so the toolkit appears in its own signatures. Every other library stays free of it.
javafx {
    version = "25.0.1"
    modules = listOf("javafx.controls", "javafx.graphics")
}

val testFxVersion = "4.0.18"

dependencies {
    // The blocks and the laid out result of the core appear in the signatures of the renderer, so
    // the core is part of the API of this module.
    api(project(":lib:ai-ghost-layouting"))

    testImplementation("org.testfx:testfx-core:${testFxVersion}")
    // TestFX still pulls the JUnit artifacts of its own generation, which collide with the platform used here.
    testImplementation("org.testfx:testfx-junit5:${testFxVersion}") {
        exclude(group = "org.junit.jupiter")
        exclude(group = "org.junit.platform")
    }
    // Monocle provides the headless glass platform TestFX runs on, so no window is opened.
    testImplementation("org.testfx:openjfx-monocle:21.0.2")
}

// The module descriptor is the only Java source, while the classes it exports are written in Kotlin.
// javac only sees them as part of the module when both compilers write into the same output
// directory - otherwise the exported package is reported as empty.
val compileKotlin = tasks.named<KotlinCompile>("compileKotlin")

tasks.named<JavaCompile>("compileJava") {
    destinationDirectory.set(compileKotlin.flatMap { it.destinationDirectory })
}

tasks.withType<Test> {
    // Unlike app/ui this module does not apply the module plugin, so the tests already run on the
    // classpath: the test source set carries no module descriptor of its own. Monocle reaching into
    // internals of javafx.graphics therefore needs no further opening here.

    // The tests must not open a window.
    systemProperty("testfx.robot", "glass")
    systemProperty("testfx.headless", "true")
    systemProperty("glass.platform", "Monocle")
    systemProperty("monocle.platform", "Headless")
    systemProperty("prism.order", "sw")
    systemProperty("java.awt.headless", "true")
}

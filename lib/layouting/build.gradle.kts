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
}

// The layout core knows nothing about ai-ghost and nothing about a UI toolkit: it sets blocks of
// text against a column width and hands back plain numbers. It therefore carries no dependency at
// all beyond the Kotlin standard library the root build applies.

// The module descriptor is the only Java source, while the classes it exports are written in Kotlin.
// javac only sees them as part of the module when both compilers write into the same output
// directory - otherwise the exported package is reported as empty.
val compileKotlin = tasks.named<KotlinCompile>("compileKotlin")

tasks.named<JavaCompile>("compileJava") {
    destinationDirectory.set(compileKotlin.flatMap { it.destinationDirectory })
}

// IP-06: regression tests ("...RT") pin the page structure of the layout core against checked in
// golden files. They run apart from the plain developer tests so they can be re-run on their own,
// but `check` - and with it `build` - always exercises them as well.
tasks.named<Test>("test") {
    filter { excludeTestsMatching("*RT") }
}

val regressionTest = tasks.register<Test>("regressionTest") {
    group = "verification"
    description = "Runs the layout regression tests (golden files) of IP-06"
    testClassesDirs = sourceSets.test.get().output.classesDirs
    classpath = sourceSets.test.get().runtimeClasspath
    filter { includeTestsMatching("*RT") }

    // The test runs forked in its own JVM, which does not inherit the -D properties of the build's
    // own JVM automatically; the golden file update mode of GoldenFileSupport therefore has to be
    // relayed by hand, or `-DlayoutGoldenFiles.update=true` on the command line would never reach it.
    System.getProperty("layoutGoldenFiles.update")?.let { systemProperty("layoutGoldenFiles.update", it) }
}

tasks.named("check") {
    dependsOn(regressionTest)
}

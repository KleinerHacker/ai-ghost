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

// Pinned once in the root build; the coordinate itself is documented there.
val simplayVersion: String by rootProject.extra

dependencies {
    // The document model goes into the builders and comes back out in every builder signature, so it
    // stays part of the API. No JavaFX is involved: nothing here is observed, everything is read once
    // and translated.
    api(project(":lib:ai-ghost-model"))

    // The layout raw model - Document, Page, TextBlock, TextStyle, geometry - the builders produce.
    // Both sides show up in the builder signatures, so it is part of the API. simPlay's Kotlin
    // Multiplatform engine is consumed through its JVM coordinate `simplay-engine-jvm` directly:
    // the multiplatform aggregator `simplay-engine` publishes no jar of its own, so javac's
    // module-path inference cannot form a module for it and `requires simplay.engine.jvm` fails to
    // resolve.
    api("org.pcsoft.framework:simplay-engine-jvm:$simplayVersion")
}

// The module descriptor is the only Java source, while the classes it exports are written in Kotlin.
// javac only sees them as part of the module when both compilers write into the same output
// directory - otherwise the exported package is reported as empty.
val compileKotlin = tasks.named<KotlinCompile>("compileKotlin")

tasks.named<JavaCompile>("compileJava") {
    destinationDirectory.set(compileKotlin.flatMap { it.destinationDirectory })
}

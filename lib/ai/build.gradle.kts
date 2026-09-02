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

// Arrow's Either appears in the signatures of the action port, so it is part of the API as well.
val arrowVersion = "2.1.2"

dependencies {
    // The limit check reads Preferences.Ai, so the model is part of the public API.
    api(project(":lib:ai-ghost-model"))

    api("io.arrow-kt:arrow-core:${arrowVersion}")

    implementation("org.slf4j:slf4j-api:2.0.17")
}

// The module descriptor is the only Java source, while the classes it exports are written in Kotlin.
// javac only sees them as part of the module when both compilers write into the same output
// directory - otherwise the exported package is reported as empty.
val compileKotlin = tasks.named<KotlinCompile>("compileKotlin")

tasks.named<JavaCompile>("compileJava") {
    destinationDirectory.set(compileKotlin.flatMap { it.destinationDirectory })
}

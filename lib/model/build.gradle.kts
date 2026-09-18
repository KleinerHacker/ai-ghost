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
    id("org.jetbrains.kotlin.plugin.serialization")
}

// Jackson is exposed through the model API, so consumers can serialise the model themselves.
val jacksonVersion = "2.22.2"

// Arrow's Either appears in the signatures of the storage, so it is part of the API as well.
val arrowVersion = "2.1.2"

// Pinned once in the root build; the coordinate itself is documented there.
val simplayVersion: String by rootProject.extra

// Same version simPlay itself builds against (see simPlay's gradle/libs.versions.toml), so the
// `Document` this module encodes and the one simPlay decodes agree on the wire format.
val kotlinxSerializationVersion = "1.11.0"

dependencies {
    // The plugin API is exposed through the model API, so consumers of the model see the plugin types.
    api(project(":lib:plugin:ai-ghost-plugin-api"))

    api("com.fasterxml.jackson.core:jackson-databind:${jacksonVersion}")
    api("com.fasterxml.jackson.module:jackson-module-kotlin:${jacksonVersion}")
    api("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:${jacksonVersion}")
    api("io.arrow-kt:arrow-core:${arrowVersion}")

    // `Book.document` carries simPlay's raw document model, so it is part of this module's API.
    // simPlay's Kotlin Multiplatform engine is consumed through its JVM coordinate directly, see
    // `lib/layouting-model/build.gradle.kts` for why the multiplatform aggregator cannot be used.
    api("org.pcsoft.framework:simplay-engine-jvm:$simplayVersion")

    // `Document` is kotlinx-serialization-only (no Jackson bridge), so this module encodes it to a
    // plain JSON string of its own and hands only that string to Jackson - implementation detail of
    // `DocumentCodec`, not part of the public API.
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:${kotlinxSerializationVersion}")

    implementation("org.slf4j:slf4j-api:2.0.19")
}

// The module descriptor is the only Java source, while the classes it exports are written in Kotlin.
// javac only sees them as part of the module when both compilers write into the same output
// directory - otherwise the exported package is reported as empty.
val compileKotlin = tasks.named<KotlinCompile>("compileKotlin")

tasks.named<JavaCompile>("compileJava") {
    destinationDirectory.set(compileKotlin.flatMap { it.destinationDirectory })
}

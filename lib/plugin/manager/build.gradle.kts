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

// Same Jackson line lib/model already uses for preferences.yml.
val jacksonVersion = "2.22.3"

dependencies {
    api(project(":lib:plugin:ai-ghost-plugin-api"))

    // Providers are discovered through java.util.ServiceLoader over each plugin's own URLClassLoader
    // (META-INF/services/org.pcsoft.app.aighost.plugin.api.provider.AiProvider) - no bytecode-scanning
    // library needed.
    implementation(kotlin("reflect"))

    // Reads a plugin's META-INF/plugin.yml (or .yaml); kept internal, not part of this module's API.
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:${jacksonVersion}")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:${jacksonVersion}")

    implementation("org.slf4j:slf4j-api:2.0.19")
}

// The module descriptor is the only Java source, while the classes it exports are written in Kotlin.
// javac only sees them as part of the module when both compilers write into the same output
// directory - otherwise the exported package is reported as empty.
val compileKotlin = tasks.named<KotlinCompile>("compileKotlin")

tasks.named<JavaCompile>("compileJava") {
    destinationDirectory.set(compileKotlin.flatMap { it.destinationDirectory })
}

// Fixture plugin JARs used only by this module's tests: each one built from its own package of the
// "fixtures" source set below, packaged the same way a real plugin would be - compiled solely
// against ai-ghost-plugin-api - so PluginLoader is exercised against real, isolated JARs instead of
// classes already sitting on the test classpath.
val fixtures: SourceSet = sourceSets.create("fixtures")

dependencies {
    "fixturesImplementation"(project(":lib:plugin:ai-ghost-plugin-api"))
}

val fixturesJarDir = layout.buildDirectory.dir("fixtures/jars")

/**
 * Packages one fixture plugin: the classes of [fixturePackage], plus its own
 * `<package>/META-INF/plugin.yml` (or `.yaml`) resource re-rooted to the JAR's own `META-INF/`, the
 * way a real plugin's build would place it.
 */
fun registerFixtureJar(taskName: String, jarName: String, fixturePackage: String) = tasks.register<Jar>(taskName) {
    val folder = fixturePackage.replace('.', '/')

    group = "fixtures"
    archiveFileName.set(jarName)
    destinationDirectory.set(fixturesJarDir)

    // fixtures.output (not .classesDirs / .resourcesDir alone) is what carries the dependency onto
    // compileFixturesKotlin and processFixturesResources - a bare File reference does not.
    from(fixtures.output) {
        include("$folder/**")
        exclude("$folder/META-INF/**")
    }
    from(fixtures.output) {
        include("$folder/META-INF/**")
        eachFile { path = path.removePrefix("$folder/") }
        includeEmptyDirs = false
    }
}

val goodProviderJar = registerFixtureJar(
    "goodProviderFixtureJar", "good-provider.jar",
    "org.pcsoft.app.aighost.plugin.manager.fixtures.good"
)
val wrongContractVersionJar = registerFixtureJar(
    "wrongContractVersionFixtureJar", "wrong-contract-version-provider.jar",
    "org.pcsoft.app.aighost.plugin.manager.fixtures.wrongcontract"
)
val throwingProviderJar = registerFixtureJar(
    "throwingProviderFixtureJar", "throwing-provider.jar",
    "org.pcsoft.app.aighost.plugin.manager.fixtures.throwing"
)
val missingManifestJar = registerFixtureJar(
    "missingManifestFixtureJar", "missing-manifest-provider.jar",
    "org.pcsoft.app.aighost.plugin.manager.fixtures.missingmanifest"
)
val incompleteManifestJar = registerFixtureJar(
    "incompleteManifestFixtureJar", "incomplete-manifest-provider.jar",
    "org.pcsoft.app.aighost.plugin.manager.fixtures.incompletemanifest"
)
val unsupportedFieldJar = registerFixtureJar(
    "unsupportedFieldFixtureJar", "unsupported-field-provider.jar",
    "org.pcsoft.app.aighost.plugin.manager.fixtures.unsupportedfield"
)
// A second, independent build of the "good" provider's id, to prove the "first loaded wins" rule
// between two user-directory plugins.
val duplicateIdProviderJar = registerFixtureJar(
    "duplicateIdProviderFixtureJar", "duplicate-id-provider.jar",
    "org.pcsoft.app.aighost.plugin.manager.fixtures.duplicateid"
)

val fixtureJarTasks = listOf(
    goodProviderJar, wrongContractVersionJar, throwingProviderJar,
    missingManifestJar, incompleteManifestJar, unsupportedFieldJar, duplicateIdProviderJar,
)

tasks.named<Test>("test") {
    dependsOn(fixtureJarTasks)
    systemProperty("ai-ghost.fixtures.dir", fixturesJarDir.get().asFile.absolutePath)
}

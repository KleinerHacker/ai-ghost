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
    `java-library`
    // pluggiat (below) carries neither a module-info.class nor an Automatic-Module-Name manifest
    // entry, so plain Gradle refuses to put it on the module path and `requires pluggiat;` in
    // module-info.java fails with "module not found". This plugin is already used the same way in
    // app/ui (there for mvvmfx/arrow/typetools): it forces such dependencies onto the module path
    // under the JDK's own filename-derived automatic module name, the same name `java --module-path`
    // would compute - matching the `requires pluggiat;` in this module's module-info.java.
    id("org.javamodularity.moduleplugin") version "2.1.0"
}

dependencies {
    api(project(":lib:plugin:ai-ghost-plugin-api"))

    // AiProviderExtensionConfig declares the "ai" extension point pluggiat resolves a plugin's
    // extensions.ai[] manifest entries against. Resolved from GitHub Packages, see the root
    // build.gradle.kts repository block.
    implementation("org.pcsoft.framework:pluggiat:0.1.0")
}

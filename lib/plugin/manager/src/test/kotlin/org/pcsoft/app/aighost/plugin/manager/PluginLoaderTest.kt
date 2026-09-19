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

package org.pcsoft.app.aighost.plugin.manager

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotSame
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import java.nio.file.Path

/**
 * Exercises [PluginLoader] directly against the fixture plugin JARs the build packages under
 * `build/fixtures/jars` (see `build.gradle.kts`), each compiled solely against
 * `ai-ghost-plugin-api`, the way a real, third-party plugin would be.
 */
class PluginLoaderTest {

    private val fixturesDir = Path.of(System.getProperty("ai-ghost.fixtures.dir"))

    private fun fixture(name: String): Path = fixturesDir.resolve(name)

    /** A well-formed plugin JAR is loaded: its manifest and its provider, with derived config fields. */
    @Test
    fun `load loads a well-formed plugin`() {
        val loaded = PluginLoader.load(fixture("good-provider.jar"))

        assertEquals("good-fixture", loaded.manifest.id)
        assertEquals(1, loaded.providers.size)
        assertEquals("good", loaded.providers.single().id)
        assertEquals(listOf("responseText"), loaded.providers.single().configFields.map { it.name })
    }

    /** Each loaded plugin runs through its own class loader, isolated from every other plugin. */
    @Test
    fun `load isolates each plugin through its own class loader`() {
        val first = PluginLoader.load(fixture("good-provider.jar"))
        val second = PluginLoader.load(fixture("good-provider.jar"))

        assertNotSame(
            first.providers.single().provider.javaClass.classLoader,
            second.providers.single().provider.javaClass.classLoader,
        )
    }

    /** A provider whose declared contract version does not match the supported one fails to load. */
    @Test
    fun `load throws when the only provider has a mismatched contract version`() {
        assertThrows(PluginLoadException::class.java) {
            PluginLoader.load(fixture("wrong-contract-version-provider.jar"))
        }
    }

    /** A provider that throws during construction fails to load, and the plugin has no usable provider left. */
    @Test
    fun `load throws when the only provider fails to instantiate`() {
        assertThrows(PluginLoadException::class.java) {
            PluginLoader.load(fixture("throwing-provider.jar"))
        }
    }

    /** A plugin JAR without a manifest file fails to load, before its provider is even looked at. */
    @Test
    fun `load throws when the plugin ships no manifest`() {
        val exception = assertThrows(PluginLoadException::class.java) {
            PluginLoader.load(fixture("missing-manifest-provider.jar"))
        }

        assertEquals(true, exception.message?.contains("plugin.yml"))
    }

    /** A plugin JAR whose manifest is missing a required field fails to load. */
    @Test
    fun `load throws when the plugin manifest is incomplete`() {
        val exception = assertThrows(PluginLoadException::class.java) {
            PluginLoader.load(fixture("incomplete-manifest-provider.jar"))
        }

        assertEquals(true, exception.message?.contains("name"))
    }

    /** A provider whose configuration model carries an unsupported field type fails to load. */
    @Test
    fun `load throws when the only provider has an unsupported config field type`() {
        assertThrows(PluginLoadException::class.java) {
            PluginLoader.load(fixture("unsupported-field-provider.jar"))
        }
    }
}

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
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.net.URLClassLoader
import java.nio.file.Path
import java.util.Base64
import kotlin.io.path.createDirectories
import kotlin.io.path.writeText

class PluginManifestReaderTest {

    @TempDir
    lateinit var dir: Path

    /** A minimal, valid `providers.ai` block, appended to every fixture expected to parse. */
    private val validProviders = """
        providers:
          ai:
            - id: sample-provider
              implementation: org.pcsoft.app.aighost.plugin.manager.SampleProvider
              name: text.sampleProvider.name
              contractVersion: 1
    """.trimIndent()

    private fun classLoaderOf(dir: Path): URLClassLoader = URLClassLoader(arrayOf(dir.toUri().toURL()), null)

    private fun writeManifest(text: String, fileName: String = "plugin.yml") {
        val metaInf = dir.resolve("META-INF").createDirectories()
        metaInf.resolve(fileName).writeText(text)
    }

    /** A complete manifest is read into a matching [org.pcsoft.app.aighost.plugin.api.manifest.PluginManifest]. */
    @Test
    fun `read parses a complete manifest`() {
        writeManifest(
            """
            id: sample
            name: Sample Plugin
            description: A sample plugin
            author: Someone
            version: "1.2.3"
            copyright: Copyright (c) Someone
            """.trimIndent() + "\n" + validProviders
        )

        val manifest = PluginManifestReader.read(classLoaderOf(dir))

        assertEquals("sample", manifest.id)
        assertEquals("Sample Plugin", manifest.name)
        assertEquals("A sample plugin", manifest.description)
        assertEquals("Someone", manifest.author)
        assertEquals("1.2.3", manifest.version)
        assertEquals("Copyright (c) Someone", manifest.copyright)
    }

    /** The `providers.ai` entries are parsed into matching [org.pcsoft.app.aighost.plugin.api.manifest.AiProviderDeclaration]s. */
    @Test
    fun `read parses the providers ai declarations`() {
        writeManifest("id: sample\nname: Sample\nversion: \"1.0\"\n$validProviders\n")

        val manifest = PluginManifestReader.read(classLoaderOf(dir))

        assertEquals(1, manifest.aiProviders.size)
        val declaration = manifest.aiProviders.single()
        assertEquals("sample-provider", declaration.id)
        assertEquals("org.pcsoft.app.aighost.plugin.manager.SampleProvider", declaration.implementation)
        assertEquals("text.sampleProvider.name", declaration.name)
        assertEquals(1, declaration.contractVersion)
    }

    /** A `plugin.yaml` (instead of `plugin.yml`) is read just the same. */
    @Test
    fun `read accepts the yaml extension as well`() {
        writeManifest("id: sample\nname: Sample\nversion: \"1.0\"\n$validProviders\n", fileName = "plugin.yaml")

        val manifest = PluginManifestReader.read(classLoaderOf(dir))

        assertEquals("sample", manifest.id)
    }

    /** A missing manifest file is reported with a message naming both possible file names. */
    @Test
    fun `read throws a detailed exception when no manifest file exists`() {
        val exception = assertThrows(PluginLoadException::class.java) {
            PluginManifestReader.read(classLoaderOf(dir))
        }

        assertEquals(true, exception.message?.contains("plugin.yml"))
        assertEquals(true, exception.message?.contains("plugin.yaml"))
    }

    /** A manifest missing the required `name` field is reported with the field's name. */
    @Test
    fun `read throws a detailed exception for a missing required field`() {
        writeManifest("id: sample\nversion: \"1.0\"\n$validProviders\n")

        val exception = assertThrows(PluginLoadException::class.java) {
            PluginManifestReader.read(classLoaderOf(dir))
        }

        assertEquals(true, exception.message?.contains("name"))
    }

    /** A manifest without any `providers.ai` entry is reported as such. */
    @Test
    fun `read throws a detailed exception for a missing providers ai field`() {
        writeManifest("id: sample\nname: Sample\nversion: \"1.0\"\n")

        val exception = assertThrows(PluginLoadException::class.java) {
            PluginManifestReader.read(classLoaderOf(dir))
        }

        assertEquals(true, exception.message?.contains("providers.ai"))
    }

    /** A `providers.ai` entry missing one of its own required fields is reported naming that field. */
    @Test
    fun `read throws a detailed exception for an incomplete providers ai entry`() {
        writeManifest(
            """
            id: sample
            name: Sample
            version: "1.0"
            providers:
              ai:
                - id: sample-provider
                  implementation: org.pcsoft.app.aighost.plugin.manager.SampleProvider
                  contractVersion: 1
            """.trimIndent()
        )

        val exception = assertThrows(PluginLoadException::class.java) {
            PluginManifestReader.read(classLoaderOf(dir))
        }

        assertEquals(true, exception.message?.contains("providers.ai[0].name"))
    }

    /** Malformed YAML is reported as such, not as a missing field. */
    @Test
    fun `read throws a detailed exception for malformed yaml`() {
        writeManifest(":: not valid yaml ::")

        val exception = assertThrows(PluginLoadException::class.java) {
            PluginManifestReader.read(classLoaderOf(dir))
        }

        assertEquals(true, exception.message?.contains("YAML"))
    }

    /** An `icon` value that is not valid Base64 is reported as such. */
    @Test
    fun `read throws a detailed exception for a non-base64 icon`() {
        writeManifest("id: sample\nname: Sample\nversion: \"1.0\"\nicon: \"not base64!!\"\n$validProviders\n")

        val exception = assertThrows(PluginLoadException::class.java) {
            PluginManifestReader.read(classLoaderOf(dir))
        }

        assertEquals(true, exception.message?.contains("Base64"))
    }

    /** An `icon` that decodes to bytes recognizable as none of SVG, PNG or JPG is reported as such. */
    @Test
    fun `read throws a detailed exception for an icon that is not a recognizable image`() {
        val icon = Base64.getEncoder().encodeToString("just some plain text".toByteArray())
        writeManifest("id: sample\nname: Sample\nversion: \"1.0\"\nicon: \"$icon\"\n$validProviders\n")

        val exception = assertThrows(PluginLoadException::class.java) {
            PluginManifestReader.read(classLoaderOf(dir))
        }

        assertEquals(true, exception.message?.contains("icon"))
    }

    /** A valid PNG icon is accepted and kept as the Base64 string it was given as. */
    @Test
    fun `read accepts a valid png icon`() {
        val pngBytes = byteArrayOf(
            0x89.toByte(), 'P'.code.toByte(), 'N'.code.toByte(), 'G'.code.toByte(),
            0x0D, 0x0A, 0x1A, 0x0A, 0, 0, 0
        )
        val icon = Base64.getEncoder().encodeToString(pngBytes)
        writeManifest("id: sample\nname: Sample\nversion: \"1.0\"\nicon: \"$icon\"\n$validProviders\n")

        val manifest = PluginManifestReader.read(classLoaderOf(dir))

        assertEquals(icon, manifest.icon)
    }

    /** A valid SVG icon (plain XML text) is accepted as well. */
    @Test
    fun `read accepts a valid svg icon`() {
        val icon = Base64.getEncoder().encodeToString("<svg></svg>".toByteArray())
        writeManifest("id: sample\nname: Sample\nversion: \"1.0\"\nicon: \"$icon\"\n$validProviders\n")

        val manifest = PluginManifestReader.read(classLoaderOf(dir))

        assertEquals(icon, manifest.icon)
    }
}

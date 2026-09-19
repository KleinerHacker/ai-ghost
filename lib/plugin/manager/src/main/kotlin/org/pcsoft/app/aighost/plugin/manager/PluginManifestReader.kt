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

import com.fasterxml.jackson.dataformat.yaml.YAMLMapper
import com.fasterxml.jackson.module.kotlin.kotlinModule
import org.pcsoft.app.aighost.plugin.api.manifest.AiProviderDeclaration
import org.pcsoft.app.aighost.plugin.api.manifest.PluginManifest
import java.util.Base64

/**
 * Reads the `META-INF/plugin.yml` (or `META-INF/plugin.yaml`) of a plugin, the way [PluginLoader]
 * needs it before a plugin's providers are registered.
 *
 * Every problem - the file is missing, its YAML cannot be parsed, a required field is blank, the
 * icon is not valid Base64 or not a recognizable image - is reported as a [PluginLoadException] that
 * names the exact reason, so a plugin author (and the log a user sees) is not left with a bare
 * "manifest defect".
 */
internal object PluginManifestReader {
    private val yamlMapper = YAMLMapper.builder().addModule(kotlinModule()).build()

    private const val YML_PATH = "META-INF/plugin.yml"
    private const val YAML_PATH = "META-INF/plugin.yaml"

    private val PNG_SIGNATURE = byteArrayOf(
        0x89.toByte(), 'P'.code.toByte(), 'N'.code.toByte(), 'G'.code.toByte(),
        0x0D, 0x0A, 0x1A, 0x0A
    )
    private val JPG_SIGNATURE = byteArrayOf(0xFF.toByte(), 0xD8.toByte(), 0xFF.toByte())

    /**
     * Reads and validates the manifest visible through [classLoader].
     *
     * @param classLoader The isolated class loader [PluginLoader] built for the plugin JAR.
     * @return The validated manifest.
     * @throws PluginLoadException The manifest is missing, malformed, incomplete, or its icon is
     *                              neither valid Base64 nor a recognizable SVG, PNG or JPG image.
     */
    fun read(classLoader: ClassLoader): PluginManifest {
        val resource = classLoader.getResource(YML_PATH) ?: classLoader.getResource(YAML_PATH)
            ?: throw PluginLoadException(
                "no plugin manifest found: neither '$YML_PATH' nor '$YAML_PATH' exists in the plugin JAR"
            )

        val raw = try {
            resource.openStream().use { yamlMapper.readValue(it, RawManifest::class.java) }
        } catch (e: Exception) {
            throw PluginLoadException("plugin manifest at '$resource' is not valid YAML: ${e.message}", e)
        }

        val id = requireField(raw.id, "id")
        val name = requireField(raw.name, "name")
        val version = requireField(raw.version, "version")
        val aiProviders = raw.providers?.ai
            ?.mapIndexed { index, provider -> provider.toDeclaration(index) }
            ?.takeIf { it.isNotEmpty() }
            ?: throw PluginLoadException("plugin manifest is missing the required, non-empty field 'providers.ai'")
        raw.icon?.let(::checkIconFormat)

        return PluginManifest(
            id = id,
            name = name,
            description = raw.description,
            icon = raw.icon,
            author = raw.author,
            version = version,
            copyright = raw.copyright,
            aiProviders = aiProviders,
        )
    }

    private fun requireField(value: String?, field: String): String =
        value?.takeIf { it.isNotBlank() }
            ?: throw PluginLoadException("plugin manifest is missing the required field '$field'")

    /** Validates that [icon] is Base64 and decodes to a recognizable SVG, PNG or JPG image. */
    private fun checkIconFormat(icon: String) {
        val bytes = try {
            Base64.getDecoder().decode(icon)
        } catch (e: IllegalArgumentException) {
            throw PluginLoadException("plugin manifest field 'icon' is not valid Base64", e)
        }

        val recognized = bytes.startsWith(PNG_SIGNATURE) ||
                bytes.startsWith(JPG_SIGNATURE) ||
                looksLikeSvg(bytes)
        if (!recognized) {
            throw PluginLoadException(
                "plugin manifest field 'icon' does not decode to a recognizable SVG, PNG or JPG image"
            )
        }
    }

    private fun ByteArray.startsWith(signature: ByteArray): Boolean =
        size >= signature.size && signature.indices.all { this[it] == signature[it] }

    private fun looksLikeSvg(bytes: ByteArray): Boolean {
        val text = bytes.decodeToString(0, minOf(bytes.size, 256)).trimStart()
        return text.startsWith("<?xml") || text.startsWith("<svg")
    }

    /** Plain deserialization target for the manifest YAML, before its required fields are checked. */
    private data class RawManifest(
        val id: String? = null,
        val name: String? = null,
        val description: String? = null,
        val icon: String? = null,
        val author: String? = null,
        val version: String? = null,
        val copyright: String? = null,
        val providers: RawProviders? = null,
    )

    /** The `providers` object of the manifest YAML, grouped by kind - today only `ai`. */
    private data class RawProviders(
        val ai: List<RawAiProvider>? = null,
    )

    /** One entry of `providers.ai`, before its required fields are checked. */
    private data class RawAiProvider(
        val id: String? = null,
        val implementation: String? = null,
        val name: String? = null,
        val contractVersion: Int? = null,
    ) {
        /** Validates this entry and turns it into an [AiProviderDeclaration]. */
        fun toDeclaration(index: Int): AiProviderDeclaration {
            fun requireEntryField(value: String?, field: String): String =
                value?.takeIf { it.isNotBlank() }
                    ?: throw PluginLoadException("plugin manifest field 'providers.ai[$index].$field' is missing")

            val declaredContractVersion = contractVersion
                ?: throw PluginLoadException("plugin manifest field 'providers.ai[$index].contractVersion' is missing")

            return AiProviderDeclaration(
                id = requireEntryField(id, "id"),
                implementation = requireEntryField(implementation, "implementation"),
                name = requireEntryField(name, "name"),
                contractVersion = declaredContractVersion,
            )
        }
    }
}

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

package org.pcsoft.app.aighost.model

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.databind.json.JsonMapper
import com.fasterxml.jackson.module.kotlin.kotlinModule
import org.pcsoft.app.aighost.model.util.logger
import org.pcsoft.app.aighost.plugin.api.model.project.ProjectPart
import org.pcsoft.app.aighost.plugin.api.model.project.ProjectPartRegistry
import java.io.File
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

/**
 * The Jackson mapper every storage of this module reads and writes its documents with.
 *
 * Preferences and projects are stored the same way, so they share one mapper instead of configuring
 * their own: the Kotlin module makes the data classes readable with their default values, and the
 * indentation keeps every stored document editable by hand.
 *
 * The mapper is thread safe once it is built, so the storages use it as it is.
 */
internal object StorageIO {
    private val log = logger<StorageIO>()

    /** The configured mapper, shared by all storages of this module. */
    val mapper: JsonMapper = JsonMapper.builder()
        .addModule(kotlinModule())
        .enable(SerializationFeature.INDENT_OUTPUT)
        // A project is a zip archive of several entries written through one and the same stream, so
        // the mapper must not close it after a single entry - the next one would find it closed.
        .disable(JsonGenerator.Feature.AUTO_CLOSE_TARGET)
        .disable(JsonParser.Feature.AUTO_CLOSE_SOURCE)
        .build()

    /**
     * Saves the provided project parts into a zip file.
     *
     * Each project part is serialized and written into a separate entry within the zip file. The entry
     * is named after the identifier the [ProjectPartRegistry] knows the part class under, so a part
     * that is only held in memory is written just like a registered one.
     *
     * @param file The output zip file where the project parts will be saved.
     * @param parts A vararg of `ProjectPart` instances to be included in the zip file.
     */
    fun saveToZip(file: File, vararg parts: ProjectPart) {
        log.debug("Save to ZIP: {}", file.absolutePath)

        ZipOutputStream(file.outputStream()).use { stream ->
            for (part in parts) {
                val entryName = ProjectPartRegistry.identifierOf(part::class)
                log.trace("> store part {} with entry name {}", part::class.simpleName, entryName)

                stream.putNextEntry(ZipEntry(entryName))
                mapper.writeValue(stream, part)
                stream.closeEntry()
            }
        }
    }

    /**
     * Loads and deserializes project parts from a zip archive file.
     *
     * The class an entry is read into comes from the [ProjectPartRegistry], so a part a plugin
     * registered is read like one of the parts the application ships with. An entry no class is
     * registered for is skipped with a warning instead of failing the whole document, so a project
     * written by a newer version - or by a plugin that is not installed here - still opens with the
     * parts this application knows.
     *
     * @param file The zip archive file containing the serialized project parts.
     * @return The deserialized project parts by the entry name they were stored under.
     */
    fun loadFromZip(file: File): Map<String, ProjectPart> {
        log.debug("Load from ZIP: {}", file.absolutePath)

        return ZipInputStream(file.inputStream()).use { stream ->
            val parts = mutableMapOf<String, ProjectPart>()

            var entry = stream.nextEntry
            while (entry != null) {
                val entryName = entry.name
                val partClass = ProjectPartRegistry.partClassOf(entryName)
                if (partClass == null) {
                    log.warn("Unable to read project part for entry name '{}': no model class found", entryName)
                } else {
                    log.trace("> read part {} from entry name {}", partClass.simpleName, entryName)
                    parts[entryName] = mapper.readValue(stream, partClass.java)
                }

                stream.closeEntry()
                entry = stream.nextEntry
            }

            parts
        }
    }
}

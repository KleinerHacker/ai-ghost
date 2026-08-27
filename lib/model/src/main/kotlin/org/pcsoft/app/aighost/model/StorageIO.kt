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
import org.pcsoft.app.aighost.plugin.api.model.project.ProjectPartInfo
import java.io.File
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream
import kotlin.reflect.KClass

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

    /**
     * The extension every entry of a project archive carries, so the content of the archive is
     * recognizable as JSON in any archive tool.
     *
     * A document written before this extension existed carries the bare identifier as its entry name,
     * which is why reading takes the extension off instead of expecting it.
     */
    private const val ENTRY_SUFFIX = ".json"

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
     * What a project archive was read into.
     *
     * The two sides are kept apart on purpose: [parts] is what the application can work with, while
     * [unknownParts] is text it only carries along.
     *
     * @property parts The parts that were read into a model class, by their identifier.
     * @property unknownParts The stored text of every entry no model class was named for, by its identifier.
     */
    data class Content(
        val parts: Map<String, ProjectPart>,
        val unknownParts: Map<String, String>
    )

    /**
     * Saves the given project parts into a zip file.
     *
     * Each part is serialized into an entry of its own, named after the identifier the part declares
     * through [ProjectPartInfo] and carrying the [ENTRY_SUFFIX]. Every entry of [unknownParts] is
     * written back exactly as it was read, under the same name, so a part this application cannot
     * read survives the save untouched.
     *
     * @param file The output zip file where the project parts will be saved.
     * @param parts The parts to serialize into the archive.
     * @param unknownParts The stored text of the parts to write back unchanged, by their identifier.
     */
    fun saveToZip(
        file: File,
        parts: Collection<ProjectPart>,
        unknownParts: Map<String, String> = emptyMap()
    ) {
        log.debug("Save to ZIP: {}", file.absolutePath)

        ZipOutputStream(file.outputStream()).use { stream ->
            for (part in parts) {
                val entryName = entryNameOf(identifierOf(part::class))
                log.trace("> store part {} with entry name {}", part::class.simpleName, entryName)

                stream.putNextEntry(ZipEntry(entryName))
                mapper.writeValue(stream, part)
                stream.closeEntry()
            }

            for ((identifier, json) in unknownParts) {
                val entryName = entryNameOf(identifier)
                log.trace("> store unread part with entry name {}", entryName)

                stream.putNextEntry(ZipEntry(entryName))
                stream.write(json.toByteArray())
                stream.closeEntry()
            }
        }
    }

    /**
     * Loads and deserializes project parts from a zip archive file.
     *
     * An entry no class was named for is not thrown away: its text is kept in
     * [Content.unknownParts], so a project written by a newer version - or by a plugin that is not
     * installed here - opens with the parts this application knows and loses nothing on the next
     * save.
     *
     * @param file The zip archive file containing the serialized project parts.
     * @param partClasses The types of project parts to read, everything else is kept as text.
     * @return The parts that could be read and the text of the ones that could not.
     */
    fun loadFromZip(file: File, vararg partClasses: KClass<out ProjectPart>): Content {
        log.debug("Load from ZIP: {}", file.absolutePath)

        val classesByIdentifier = partClasses.associateBy { identifierOf(it) }

        return ZipInputStream(file.inputStream()).use { stream ->
            val parts = mutableMapOf<String, ProjectPart>()
            val unknownParts = mutableMapOf<String, String>()

            var entry = stream.nextEntry
            while (entry != null) {
                val identifier = identifierIn(entry.name)
                val partClass = classesByIdentifier[identifier]
                if (partClass == null) {
                    log.warn("No model class for project part '{}', keeping it as it was stored", identifier)
                    unknownParts[identifier] = stream.readBytes().decodeToString()
                } else {
                    log.trace("> read part {} from entry name {}", partClass.simpleName, entry.name)
                    parts[identifier] = mapper.readValue(stream, partClass.java)
                }

                stream.closeEntry()
                entry = stream.nextEntry
            }

            Content(parts, unknownParts)
        }
    }

    /**
     * The identifier a project part is stored under: the one it declares through [ProjectPartInfo],
     * or the simple name of its class when it declares none.
     */
    private fun identifierOf(partClass: KClass<out ProjectPart>): String =
        partClass.java.getAnnotation(ProjectPartInfo::class.java)?.identifier ?: partClass.simpleName!!

    /** The name the entry of the part with this identifier is stored under. */
    private fun entryNameOf(identifier: String): String = identifier + ENTRY_SUFFIX

    /**
     * The identifier behind an entry name, with the extension taken off. A document written before
     * the extension existed carries the bare identifier, which passes through unchanged.
     */
    private fun identifierIn(entryName: String): String = entryName.removeSuffix(ENTRY_SUFFIX)
}

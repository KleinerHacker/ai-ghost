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

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.databind.json.JsonMapper
import com.fasterxml.jackson.module.kotlin.kotlinModule
import org.pcsoft.app.aighost.model.project.Project
import org.pcsoft.app.aighost.model.project.meta.Meta
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
     * An entry without it is not part of the document format and is passed over when reading.
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
     * Saves the given project into a zip file.
     *
     * Every readable part of [project] is serialized into an entry of its own, named after the
     * identifier it is stored under and carrying the [ENTRY_SUFFIX]. Every part the project only
     * carries as text is written back exactly as it was read, under the same name, so a part this
     * application cannot read survives the save untouched.
     *
     * The meta data of [project] takes over the identifiers of everything written beside the three
     * standard parts before it is serialized, so the document says itself which entries belong to
     * it and reading it can tell whether it still holds all of them.
     *
     * @param file The output zip file where the project will be saved.
     * @param project The project to serialize into the archive.
     */
    fun saveToZip(file: File, project: Project) {
        log.debug("Save to ZIP: {}", file.absolutePath)

        project.meta.additionalParts = additionalPartsOf(project)

        ZipOutputStream(file.outputStream()).use { stream ->
            for ((identifier, part) in project.parts) {
                val entryName = entryNameOf(identifier)
                log.trace("> store part {} with entry name {}", part::class.simpleName, entryName)

                stream.putNextEntry(ZipEntry(entryName))
                mapper.writeValue(stream, part)
                stream.closeEntry()
            }

            for ((identifier, json) in project.unknownParts) {
                val entryName = entryNameOf(identifier)
                log.trace("> store unread part with entry name {}", entryName)

                stream.putNextEntry(ZipEntry(entryName))
                stream.write(json.toByteArray())
                stream.closeEntry()
            }
        }
    }

    /**
     * Loads a project from a zip archive file.
     *
     * An entry no class was named for is not thrown away: its text is kept in
     * [Project.unknownParts], so a project written by a newer version - or by a plugin that is not
     * installed here - opens with the parts this application knows and loses nothing on the next
     * save. An entry that does not carry the [ENTRY_SUFFIX] is not a project part at all and is
     * passed over.
     *
     * A project document holds the three standard parts, so an archive missing one of them is
     * corrupt - a file that is not an archive at all carries no entry and would otherwise open as a
     * project of pure defaults. Such a file is answered with [Error.Corrupt] instead of a project.
     *
     * @param file The zip archive file containing the serialized project parts.
     * @param partClasses The types of project parts to read, everything else is kept as text.
     * @return The project read from the archive, or the reason why the file holds none.
     */
    fun loadFromZip(file: File, vararg partClasses: KClass<out ProjectPart>): Either<Error, Project> {
        log.debug("Load from ZIP: {}", file.absolutePath)

        val classesByIdentifier = partClasses.associateBy { identifierOf(it) }

        return ZipInputStream(file.inputStream()).use { stream ->
            val parts = mutableMapOf<String, ProjectPart>()
            val unknownParts = mutableMapOf<String, String>()

            var entry = stream.nextEntry
            while (entry != null) {
                val entryName = entry.name
                if (!entryName.endsWith(ENTRY_SUFFIX)) {
                    log.warn("Passing over entry '{}': a project part is stored as a '{}' entry", entryName, ENTRY_SUFFIX)
                } else {
                    val identifier = entryName.removeSuffix(ENTRY_SUFFIX)
                    val partClass = classesByIdentifier[identifier]
                    if (partClass == null) {
                        log.warn("No model class for project part '{}', keeping it as it was stored", identifier)
                        unknownParts[identifier] = stream.readBytes().decodeToString()
                    } else {
                        log.trace("> read part {} from entry name {}", partClass.simpleName, entryName)
                        parts[identifier] = mapper.readValue(stream, partClass.java)
                    }
                }

                stream.closeEntry()
                entry = stream.nextEntry
            }

            val missing = missingPartsOf(parts, unknownParts)
            if (missing.isNotEmpty()) {
                log.warn("The archive is missing the project part(s) {}", missing)
                Error.Corrupt(missing).left()
            } else {
                Project.fromParts(parts, unknownParts).right()
            }
        }
    }

    /**
     * The identifiers of everything [project] stores beside the three standard parts, in a stable
     * order so the same project is written the same way twice.
     */
    private fun additionalPartsOf(project: Project): List<String> =
        (project.extensionParts.keys + project.unknownParts.keys).sorted()

    /**
     * The parts a complete document holds but the archive did not, so an empty answer means the
     * archive is complete.
     *
     * The three standard parts belong to every document. Everything beyond them is named by the meta
     * data of the document itself, which is why a part that got lost is noticed at all - an entry
     * that is gone leaves nothing behind that could be missed.
     *
     * @param parts The parts that were read from the archive, by their identifier.
     * @param unknownParts The text of the entries no class was named for, by their identifier.
     */
    private fun missingPartsOf(
        parts: Map<String, ProjectPart>,
        unknownParts: Map<String, String>
    ): Set<String> {
        val standard = Project.STANDARD_IDENTIFIERS - parts.keys
        if (standard.isNotEmpty())
            return standard

        val announced = (parts[Project.PART_META] as? Meta)?.additionalParts.orEmpty()
        return announced.toSet() - parts.keys - unknownParts.keys
    }

    /**
     * Reason why an archive could not be read as a project.
     *
     * Everything that goes wrong in the file system or in the parser is thrown, so the caller can
     * tell those apart from a file that was read without trouble but simply is not a project.
     */
    sealed interface Error {

        /**
         * The file was read, but it does not hold all three standard parts a project document is
         * made of, so it is corrupt.
         *
         * @property missing The identifiers of the standard parts the archive does not hold.
         */
        data class Corrupt(val missing: Set<String>) : Error
    }

    /**
     * The identifier a project part is stored under: the one it declares through [ProjectPartInfo],
     * or the simple name of its class when it declares none.
     */
    private fun identifierOf(partClass: KClass<out ProjectPart>): String =
        partClass.java.getAnnotation(ProjectPartInfo::class.java)?.identifier ?: partClass.simpleName!!

    /** The name the entry of the part with this identifier is stored under. */
    private fun entryNameOf(identifier: String): String = identifier + ENTRY_SUFFIX
}

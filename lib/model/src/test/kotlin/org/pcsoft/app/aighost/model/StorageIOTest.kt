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
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import org.pcsoft.app.aighost.model.project.Project
import org.pcsoft.app.aighost.model.project.book.Book
import org.pcsoft.app.aighost.model.project.design.Design
import org.pcsoft.app.aighost.model.project.meta.Meta
import org.pcsoft.app.aighost.plugin.api.model.project.ProjectPart
import org.pcsoft.app.aighost.plugin.api.model.project.ProjectPartInfo
import java.io.File
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream
import kotlin.reflect.KClass

/**
 * A project part beyond the three standard ones, so a test can hand a readable part of another
 * origin to the archive and see how a failure of such a part is weighed.
 *
 * @property note The text the part carries.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@ProjectPartInfo(identifier = "notes")
data class TestNotes(
    override val version: Int = 1,
    var note: String = ""
) : ProjectPart

/**
 * Developer tests for [StorageIO], the archive a project document is made of.
 */
class StorageIOTest {

    @TempDir
    lateinit var directory: File

    private val file: File get() = File(directory, "project.aig")

    /**
     * Use case: the user opens the stored document with an archive tool, so every part of the
     * project sits in an entry named after the identifier the part declares and carrying the JSON
     * extension.
     */
    @Test
    fun storesEveryPartUnderItsIdentifier() {
        StorageIO.saveToZip(file, TestData.project())

        assertEquals(
            listOf("book.json", "design.json", "meta.json"),
            entryNamesOf(file).sorted()
        )
    }

    /**
     * Use case: the user saves a project and opens it again, so the project comes back from the
     * archive exactly as it was written - the second and third part included - and nothing is
     * reported as lost.
     */
    @Test
    fun roundTripsTheWholeProject() {
        StorageIO.saveToZip(file, TestData.project())

        val result = loadEither().getOrNull()

        assertNotNull(result)
        assertEquals(TestData.project(), result!!.project)
        assertTrue(result.lostParts.isEmpty())
    }

    /**
     * Use case: an archive holds something that is not a project part at all, so that entry is passed
     * over instead of being mistaken for a part and written back as one.
     */
    @Test
    fun passesOverAnEntryWithoutTheExtension() {
        writeArchive(
            file,
            *standardEntries(),
            "notes.txt" to "a plain note"
        )

        val project = load()

        assertNotNull(project)
        assertEquals("My Novel", project!!.meta.name)
        assertTrue(project.unknownParts.isEmpty())
    }

    /**
     * Use case: a project carries a part written by a newer version or by a plugin that is not
     * installed here, so that entry is kept as it was stored instead of being thrown away.
     */
    @Test
    fun keepsAnEntryWithoutAModelClass() {
        writeArchive(
            file,
            *standardEntries(additionalParts = listOf("plugin-notes")),
            "plugin-notes.json" to """{"note":"written elsewhere"}"""
        )

        val project = load()

        assertNotNull(project)
        assertTrue(project!!.extensionParts.isEmpty())
        assertEquals(
            mapOf("plugin-notes" to """{"note":"written elsewhere"}"""),
            project.unknownParts
        )
    }

    /**
     * Use case: a project holding a part this application cannot read is saved again, so that part
     * goes back into the document exactly as it came out of it.
     */
    @Test
    fun writesAnUnreadPartBackUnchanged() {
        val stored = """{"note":"written elsewhere"}"""

        StorageIO.saveToZip(file, TestData.project().apply { unknownParts = mapOf("plugin-notes" to stored) })

        assertEquals(
            listOf("book.json", "design.json", "meta.json", "plugin-notes.json"),
            entryNamesOf(file).sorted()
        )
        assertEquals(stored, readEntry(file, "plugin-notes.json"))
    }

    /**
     * Use case: a project carrying a part beyond the standard ones is saved, so the meta data of the
     * document names that part and the next read can tell whether it is still there.
     */
    @Test
    fun namesEveryAdditionalPartInTheMetaData() {
        val project = TestData.project().apply { unknownParts = mapOf("plugin-notes" to "{}") }

        StorageIO.saveToZip(file, project)

        assertEquals(listOf("plugin-notes"), project.meta.additionalParts)
        assertEquals(listOf("plugin-notes"), load()!!.meta.additionalParts)
    }

    /**
     * Use case: the user opens a file that is not a project archive at all, so it is reported as a
     * corrupt project instead of a document of pure defaults being opened as the user's project.
     */
    @Test
    fun reportsAForeignFileAsCorrupt() {
        file.writeText("{ this is not an archive")

        assertEquals(Project.STANDARD_IDENTIFIERS, corruptionOf(loadEither()).missing)
    }

    /**
     * Use case: the archive lost one of the three standard parts on its way, so it is reported as a
     * corrupt project instead of being opened with the defaults of that part.
     */
    @Test
    fun reportsAMissingStandardPartAsCorrupt() {
        writeArchive(file, "meta.json" to """{"name":"My Novel"}""", "design.json" to "{}")

        assertEquals(setOf(Project.PART_BOOK), corruptionOf(loadEither()).missing)
    }

    /**
     * Use case: the entry of a standard part is there but its content is broken, so the part is as
     * gone as a missing entry and the document is reported as corrupt.
     */
    @Test
    fun reportsAnUnreadableStandardPartAsCorrupt() {
        writeArchive(
            file,
            "meta.json" to """{"name":"My Novel"}""",
            "design.json" to "{}",
            "book.json" to "{ this is not the book"
        )

        assertEquals(setOf(Project.PART_BOOK), corruptionOf(loadEither()).missing)
    }

    /**
     * Use case: the archive lost a part the meta data names, so the document still opens with
     * everything else and the lost part is reported instead of the whole project being thrown away.
     */
    @Test
    fun reportsAMissingAdditionalPartAsLost() {
        writeArchive(file, *standardEntries(additionalParts = listOf("plugin-notes")))

        val result = loadEither().getOrNull()

        assertNotNull(result)
        assertEquals(setOf("plugin-notes"), result!!.lostParts)
        assertEquals("My Novel", result.project.meta.name)
    }

    /**
     * Use case: a part beyond the standard ones got lost, so the meta data of the opened project
     * stops naming it and the document is complete again on the next save.
     */
    @Test
    fun dropsALostPartFromTheMetaData() {
        writeArchive(
            file,
            *standardEntries(additionalParts = listOf("plugin-notes", "other-notes")),
            "other-notes.json" to """{"note":"still there"}"""
        )

        val result = loadEither().getOrNull()

        assertNotNull(result)
        assertEquals(listOf("other-notes"), result!!.project.meta.additionalParts)
    }

    /**
     * Use case: the entry of a part beyond the standard ones is there but its content is broken, so
     * only that part is reported as lost while the rest of the document opens.
     */
    @Test
    fun reportsAnUnreadableAdditionalPartAsLost() {
        writeArchive(
            file,
            *standardEntries(additionalParts = listOf("notes")),
            "notes.json" to "{ this is not a note"
        )

        val result = loadEither(TestNotes::class).getOrNull()

        assertNotNull(result)
        assertEquals(setOf("notes"), result!!.lostParts)
        assertTrue(result.project.extensionParts.isEmpty())
        assertTrue(result.project.meta.additionalParts.isEmpty())
    }

    /**
     * Use case: a part beyond the standard ones is readable, so it opens beside the standard parts
     * and nothing is reported as lost.
     */
    @Test
    fun readsAnAdditionalPartBesideTheStandardOnes() {
        writeArchive(
            file,
            *standardEntries(additionalParts = listOf("notes")),
            "notes.json" to """{"note":"written here"}"""
        )

        val result = loadEither(TestNotes::class).getOrNull()

        assertNotNull(result)
        assertTrue(result!!.lostParts.isEmpty())
        assertEquals(TestNotes(note = "written here"), result.project.extensionParts["notes"])
    }

    /**
     * Use case: the user opens the stored document with an archive tool, so every entry holds
     * indented JSON that can be read and edited by hand.
     */
    @Test
    fun writesIndentedJsonPerEntry() {
        StorageIO.saveToZip(file, TestData.project())

        val content = readEntry(file, "meta.json")

        assertTrue(content.contains("\n"), "expected indented JSON but was: $content")
        assertTrue(content.contains(""""name" : "My Novel""""))
    }

    /** Reads the archive at [file] as a project document, knowing the given additional parts. */
    private fun loadEither(vararg additional: KClass<out ProjectPart>): Either<StorageIO.Error, StorageIO.LoadResult> =
        StorageIO.loadFromZip(file, Meta::class, Design::class, Book::class, *additional)

    /** The project the archive at [file] holds, `null` when it holds none. */
    private fun load(): Project? = loadEither().getOrNull()?.project

    /** The corruption the read of the archive reported, failing the test when it read a project. */
    private fun corruptionOf(result: Either<StorageIO.Error, StorageIO.LoadResult>): StorageIO.Error.Corrupt =
        assertInstanceOf(StorageIO.Error.Corrupt::class.java, result.leftOrNull())

    /** The three entries every project document holds, the meta data naming [additionalParts]. */
    private fun standardEntries(additionalParts: List<String> = emptyList()): Array<Pair<String, String>> {
        val names = additionalParts.joinToString(",") { "\"$it\"" }

        return arrayOf(
            "meta.json" to """{"name":"My Novel","additionalParts":[$names]}""",
            "design.json" to "{}",
            "book.json" to "{}"
        )
    }

    /** The names of all entries the archive at [file] holds, in the order they are stored. */
    private fun entryNamesOf(file: File): List<String> =
        ZipInputStream(file.inputStream()).use { stream ->
            generateSequence { stream.nextEntry }.map { it.name }.toList()
        }

    /** The text of the entry named [name] of the archive at [file]. */
    private fun readEntry(file: File, name: String): String =
        ZipInputStream(file.inputStream()).use { stream ->
            generateSequence { stream.nextEntry }
                .first { it.name == name }
                .let { stream.readBytes().decodeToString() }
        }

    /** Writes an archive holding exactly the given entries, so a hand made document can be read. */
    private fun writeArchive(file: File, vararg entries: Pair<String, String>) {
        ZipOutputStream(file.outputStream()).use { stream ->
            for ((name, content) in entries) {
                stream.putNextEntry(ZipEntry(name))
                stream.write(content.toByteArray())
                stream.closeEntry()
            }
        }
    }
}

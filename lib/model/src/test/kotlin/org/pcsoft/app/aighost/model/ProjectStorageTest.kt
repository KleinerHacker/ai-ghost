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

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNotSame
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import org.pcsoft.app.aighost.model.project.Project
import org.pcsoft.app.aighost.model.project.book.Book
import org.pcsoft.app.aighost.model.project.design.Design
import org.pcsoft.app.aighost.model.project.meta.Meta
import java.io.File
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

/**
 * Developer tests for [ProjectStorage] and [ProjectStorage.Error].
 */
class ProjectStorageTest {

    @TempDir
    lateinit var directory: File

    private val file: File get() = File(directory, "project.aig")

    /**
     * Use case: the user saves a project and opens it again later, so every part comes back from disk
     * exactly as it was written.
     */
    @Test
    fun roundTripsProject() {
        assertTrue(ProjectStorage.save(TestData.project(), file).isRight())

        assertEquals(TestData.project(), ProjectStorage.load(file).getOrNull())
    }

    /**
     * Use case: the project carries a part this application cannot read, so that part goes into the
     * file on the next save and comes out of it again unchanged.
     */
    @Test
    fun roundTripsAPartItCannotRead() {
        val project = Project().apply { unknownParts = mapOf(OUTLINE to STORED_OUTLINE) }

        assertTrue(ProjectStorage.save(project, file).isRight())

        val loaded = ProjectStorage.load(file).getOrNull()
        assertEquals(mapOf(OUTLINE to STORED_OUTLINE), loaded?.unknownParts)
        assertEquals(Meta(additionalParts = listOf(OUTLINE)), loaded?.meta)
    }

    /**
     * Use case: the storage keeps nothing, so every load builds a project of its own and two windows
     * reading the same document never write into the same object.
     */
    @Test
    fun eachLoadHandsOutItsOwnProject() {
        ProjectStorage.save(Project(), file)

        val first = ProjectStorage.load(file).getOrNull()
        val second = ProjectStorage.load(file).getOrNull()

        assertNotNull(first)
        assertNotNull(second)
        assertNotSame(first, second)
        assertEquals(first, second)
    }

    /**
     * Use case: the user edits the manuscript, so the change is kept in memory and is only on disk
     * after an explicit save.
     */
    @Test
    fun changeIsKeptUntilSaved() {
        val project = Project()
        ProjectStorage.save(project, file)

        project.meta = Meta(name = "Changed")

        assertEquals("New Project", storedMeta().name)
        assertTrue(ProjectStorage.save(project, file).isRight())
        assertEquals("Changed", storedMeta().name)
    }

    /**
     * Use case: a recently opened entry points at a project that was deleted meanwhile, so opening it
     * reports the missing file and hands out no project.
     */
    @Test
    fun reportsNotFoundForMissingFile() {
        val result = ProjectStorage.load(file)

        assertEquals(ProjectStorage.Error.NotFound(file), result.leftOrNull())
    }

    /**
     * Use case: the chosen path is a directory, so opening it reports the wrong path rather than
     * pretending the project is missing.
     */
    @Test
    fun reportsNotAFileForDirectoryOnLoad() {
        val asDirectory = File(directory, "project-folder").apply { mkdirs() }

        val result = ProjectStorage.load(asDirectory)

        assertEquals(ProjectStorage.Error.NotAFile(asDirectory), result.leftOrNull())
    }

    /**
     * Use case: the target path is a directory, so saving reports it as not a file and leaves the
     * directory untouched instead of replacing it with the project document.
     */
    @Test
    fun reportsNotAFileWhenSavingOntoDirectory() {
        val asDirectory = File(directory, "project-folder").apply { mkdirs() }

        val result = ProjectStorage.save(Project(), asDirectory)

        assertEquals(ProjectStorage.Error.NotAFile(asDirectory), result.leftOrNull())
        assertTrue(asDirectory.isDirectory)
    }

    /**
     * Use case: the user opens a file that is not a project document at all, so opening it reports a
     * corrupt project and hands out nothing the application could work with.
     */
    @Test
    fun reportsForeignFileAsCorrupt() {
        file.writeText("{ this is not an archive")

        val error = ProjectStorage.load(file).leftOrNull()

        val corrupt = assertInstanceOf(ProjectStorage.Error.Corrupt::class.java, error)
        assertEquals(file, corrupt.file)
        assertEquals(Project.STANDARD_IDENTIFIERS, corrupt.missing)
    }

    /**
     * Use case: the archive holds an entry that is not the document the part expects, so the standard
     * part is as gone as a missing entry and the file is reported as corrupt rather than handing out
     * a half filled project.
     */
    @Test
    fun reportsWrongDocumentAsCorrupt() {
        writeArchive("design.json" to """{"startWithEmptyPage":"yes"}""")

        val error = ProjectStorage.load(file).leftOrNull()

        val corrupt = assertInstanceOf(ProjectStorage.Error.Corrupt::class.java, error)
        assertEquals(Project.STANDARD_IDENTIFIERS, corrupt.missing)
    }

    /**
     * Use case: the project document lost one of the parts it is made of, so it is reported as
     * corrupt instead of being handed out with the defaults of the lost part in its place.
     */
    @Test
    fun reportsIncompleteDocumentAsCorrupt() {
        writeArchive("meta.json" to """{"name":"My Novel"}""", "design.json" to "{}")

        val error = ProjectStorage.load(file).leftOrNull()

        val corrupt = assertInstanceOf(ProjectStorage.Error.Corrupt::class.java, error)
        assertEquals(setOf(Project.PART_BOOK), corrupt.missing)
    }

    /**
     * Use case: the project document lost a part its meta data names beyond the standard ones, so the
     * loss is reported with the project that could be rescued instead of the whole document being
     * thrown away - and the project is not handed out as a complete one.
     */
    @Test
    fun reportsDocumentWithLostAdditionalPartAsIncomplete() {
        writeArchive(
            "meta.json" to """{"name":"My Novel","additionalParts":["$OUTLINE"]}""",
            "design.json" to "{}",
            "book.json" to "{}"
        )

        val error = ProjectStorage.load(file).leftOrNull()

        val incomplete = assertInstanceOf(ProjectStorage.Error.Incomplete::class.java, error)
        assertEquals(file, incomplete.file)
        assertEquals(setOf(OUTLINE), incomplete.lostParts)
        assertEquals("My Novel", incomplete.recovered.meta.name)
        assertTrue(incomplete.recovered.meta.additionalParts.isEmpty())
    }

    /**
     * Use case: the user accepts the loss of a part beyond the standard ones, so the rescued project
     * is written back without that part and the loss becomes final.
     */
    @Test
    fun savesTheRescuedProjectWithoutTheLostPart() {
        writeArchive(
            "meta.json" to """{"name":"My Novel","additionalParts":["$OUTLINE"]}""",
            "design.json" to "{}",
            "book.json" to "{}"
        )
        val incomplete = assertInstanceOf(
            ProjectStorage.Error.Incomplete::class.java,
            ProjectStorage.load(file).leftOrNull()
        )

        assertTrue(ProjectStorage.save(incomplete.recovered, incomplete.file).isRight())

        assertEquals("My Novel", storedMeta().name)
        assertTrue(storedMeta().additionalParts.isEmpty())
    }

    /**
     * Use case: the project is saved into a directory the user just named, so the missing folders are
     * created instead of the write failing.
     */
    @Test
    fun createsMissingParentDirectory() {
        val nested = File(directory, "books/nested/project.aig")

        assertTrue(ProjectStorage.save(Project(), nested).isRight())
        assertTrue(nested.isFile)
    }

    /**
     * Use case: the user saves the project, so every part is stored in an entry of its own and the
     * document can be opened again part by part.
     */
    @Test
    fun writesEveryPartAsAnEntry() {
        ProjectStorage.save(TestData.project(), file)

        val result = StorageIo.loadFromZip(file, Meta::class, Design::class, Book::class).getOrNull()
        assertEquals(
            setOf(Project.PART_META, Project.PART_DESIGN, Project.PART_BOOK),
            result?.project?.parts?.keys
        )
    }

    /**
     * Use case: the project is saved again, so the previous document is replaced instead of being
     * appended to, and no temporary file is left behind.
     */
    @Test
    fun overwritesPreviousFileWithoutLeavingTemporaries() {
        ProjectStorage.save(Project().apply { meta = Meta(name = "First") }, file)
        ProjectStorage.save(Project().apply { meta = Meta(name = "Second") }, file)

        assertEquals("Second", ProjectStorage.load(file).getOrNull()?.meta?.name)
        assertEquals(listOf("project.aig"), directory.list()?.sorted())
    }

    /**
     * Use case: an error is shown to the user, so every failure names the path it happened on and the
     * caller does not have to remember which file it asked for.
     */
    @Test
    fun everyErrorCarriesItsFile() {
        val asDirectory = File(directory, "project-folder").apply { mkdirs() }
        val broken = File(directory, "broken.aig").apply { writeText("{ this is not an archive") }

        assertEquals(file, ProjectStorage.load(file).leftOrNull()?.file)
        assertEquals(asDirectory, ProjectStorage.load(asDirectory).leftOrNull()?.file)
        assertEquals(broken, ProjectStorage.load(broken).leftOrNull()?.file)
    }

    /** The meta part of the document stored in [file], read back the way the storage reads it. */
    private fun storedMeta(): Meta =
        StorageIo.loadFromZip(file, Meta::class, Design::class, Book::class).getOrNull()!!.project.meta

    /** Writes a hand made archive to [file], so a document of an older version can be opened. */
    private fun writeArchive(vararg entries: Pair<String, String>) {
        ZipOutputStream(file.outputStream()).use { stream ->
            for ((name, content) in entries) {
                stream.putNextEntry(ZipEntry(name))
                stream.write(content.toByteArray())
                stream.closeEntry()
            }
        }
    }

    private companion object {
        const val OUTLINE = "outline"

        /** The stored text of a part of an origin this application does not know. */
        const val STORED_OUTLINE = """{"version":1,"headline":"Three acts"}"""
    }
}

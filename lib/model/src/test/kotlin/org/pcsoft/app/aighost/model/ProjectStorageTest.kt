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

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
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

    /** Starts every test with a fresh project, because the storage is shared by the whole process. */
    @BeforeEach
    fun reset() {
        ProjectStorage.new()
    }

    /** Leaves a fresh project behind, because the storage is shared by the whole process. */
    @AfterEach
    fun cleanUp() {
        ProjectStorage.new()
    }

    /**
     * Use case: the application is started, so an empty project is open right away and the user can
     * begin writing without opening anything first.
     */
    @Test
    fun startsWithFreshProject() {
        assertEquals(Project(), ProjectStorage.current)
        assertNull(ProjectStorage.currentFile)
    }

    /**
     * Use case: the user saves a project and opens it again later, so every part comes back from disk
     * exactly as it was written.
     */
    @Test
    fun roundTripsProject() {
        seedCurrentProject()

        assertTrue(ProjectStorage.save(file).isRight())
        ProjectStorage.new()

        assertTrue(ProjectStorage.load(file).isRight())
        assertEquals(TestData.project(), ProjectStorage.current)
    }

    /**
     * Use case: the open document carries a part this application cannot read, so that part goes back
     * into the file on the next save and comes out of it again unchanged.
     */
    @Test
    fun roundTripsAPartItCannotRead() {
        ProjectStorage.current.unknownParts = mapOf(OUTLINE to STORED_OUTLINE)

        assertTrue(ProjectStorage.save(file).isRight())
        ProjectStorage.new()

        assertTrue(ProjectStorage.load(file).isRight())
        assertEquals(mapOf(OUTLINE to STORED_OUTLINE), ProjectStorage.current.unknownParts)
        assertEquals(Meta(), ProjectStorage.current.meta)
    }

    /**
     * Use case: only one project is open at a time, so opening another one replaces the open project
     * and its file instead of keeping both around.
     */
    @Test
    fun loadReplacesOpenProject() {
        val other = File(directory, "other.aig")
        ProjectStorage.current.meta = Meta(name = "First")
        ProjectStorage.save(file)
        ProjectStorage.current.meta = Meta(name = "Second")
        ProjectStorage.save(other)

        ProjectStorage.load(file)

        assertEquals("First", ProjectStorage.current.meta.name)
        assertEquals(file, ProjectStorage.currentFile)
    }

    /**
     * Use case: the user closes a project and starts a new one, so the open project falls back to the
     * defaults and the next save asks for a path again.
     */
    @Test
    fun newClosesProjectAndForgetsFile() {
        ProjectStorage.current.meta = Meta(name = "Stored")
        ProjectStorage.save(file)

        ProjectStorage.new()

        assertEquals(Project(), ProjectStorage.current)
        assertNull(ProjectStorage.currentFile)
    }

    /**
     * Use case: the user edits the manuscript, so the change is kept in memory and is only on disk
     * after an explicit save.
     */
    @Test
    fun changeIsKeptUntilSaved() {
        ProjectStorage.save(file)

        ProjectStorage.current.meta = Meta(name = "Changed")

        assertEquals("Changed", ProjectStorage.current.meta.name)
        assertEquals("New Project", storedMeta().name)
    }

    /**
     * Use case: a view reads the open project, so every way of changing it - an edit, starting a new
     * project and opening one - leaves the value in the open project.
     */
    @Test
    fun everyChangeEndsUpInTheOpenProject() {
        ProjectStorage.current.meta = Meta(name = "Edited")
        ProjectStorage.save(file)

        ProjectStorage.new()
        assertEquals("New Project", ProjectStorage.current.meta.name)

        ProjectStorage.load(file)
        assertEquals("Edited", ProjectStorage.current.meta.name)
    }

    /**
     * Use case: the user saves a project that was never stored, so the storage reports that it needs
     * a path instead of guessing one.
     */
    @Test
    fun reportsNoFileWhenSavingUnnamedProject() {
        val result = ProjectStorage.save()

        assertEquals(ProjectStorage.Error.NoFile, result.leftOrNull())
        assertNull(ProjectStorage.Error.NoFile.file)
    }

    /**
     * Use case: the user saves an opened project again, so it goes back to the file it came from
     * without asking for the path a second time.
     */
    @Test
    fun savesToKnownFileWithoutPath() {
        ProjectStorage.save(file)
        ProjectStorage.current.meta = Meta(name = "Edited")

        assertTrue(ProjectStorage.save().isRight())
        assertEquals("Edited", storedMeta().name)
    }

    /**
     * Use case: a recently opened entry points at a project that was deleted meanwhile, so opening it
     * reports the missing file and leaves the open project untouched.
     */
    @Test
    fun reportsNotFoundForMissingFile() {
        val result = ProjectStorage.load(file)

        assertEquals(ProjectStorage.Error.NotFound(file), result.leftOrNull())
        assertEquals(Project(), ProjectStorage.current)
        assertNull(ProjectStorage.currentFile)
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

        val result = ProjectStorage.save(asDirectory)

        assertEquals(ProjectStorage.Error.NotAFile(asDirectory), result.leftOrNull())
        assertTrue(asDirectory.isDirectory)
    }

    /**
     * Use case: the project file was damaged by another program, so opening it reports it as
     * malformed and the project the user works on stays open.
     */
    @Test
    fun reportsMalformedFileAsError() {
        ProjectStorage.current.meta = Meta(name = "Untouched")
        file.writeText("{ this is not an archive")

        val error = ProjectStorage.load(file).leftOrNull()

        val malformed = assertInstanceOf(ProjectStorage.Error.Malformed::class.java, error)
        assertEquals(file, malformed.file)
        assertEquals("Untouched", ProjectStorage.current.meta.name)
        assertNull(ProjectStorage.currentFile)
    }

    /**
     * Use case: the archive holds an entry that is not the document the part expects, so opening it
     * reports the file as malformed rather than opening a half filled project.
     */
    @Test
    fun reportsWrongDocumentAsError() {
        writeArchive("design.json" to """{"startWithEmptyPage":"yes"}""")

        val error = ProjectStorage.load(file).leftOrNull()

        assertInstanceOf(ProjectStorage.Error.Malformed::class.java, error)
    }

    /**
     * Use case: a project written by an older version does not carry every part yet, so it is opened
     * with the defaults filled in instead of being rejected.
     */
    @Test
    fun readsPartialDocumentWithDefaults() {
        writeArchive("meta.json" to """{"name":"My Novel"}""")

        assertTrue(ProjectStorage.load(file).isRight())

        assertEquals("My Novel", ProjectStorage.current.meta.name)
        assertEquals(Design(), ProjectStorage.current.design)
        assertEquals(Book(), ProjectStorage.current.book)
    }

    /**
     * Use case: the project is saved into a directory the user just named, so the missing folders are
     * created instead of the write failing.
     */
    @Test
    fun createsMissingParentDirectory() {
        val nested = File(directory, "books/nested/project.aig")

        assertTrue(ProjectStorage.save(nested).isRight())
        assertTrue(nested.isFile)
        assertEquals(nested, ProjectStorage.currentFile)
    }

    /**
     * Use case: the user saves the project, so every part is stored in an entry of its own and the
     * document can be opened again part by part.
     */
    @Test
    fun writesEveryPartAsAnEntry() {
        seedCurrentProject()

        ProjectStorage.save(file)

        val content = StorageIO.loadFromZip(file, Meta::class, Design::class, Book::class)
        assertEquals(
            setOf(Project.PART_META, Project.PART_DESIGN, Project.PART_BOOK),
            content.parts.keys
        )
    }

    /**
     * Use case: the project is saved again, so the previous document is replaced instead of being
     * appended to, and no temporary file is left behind.
     */
    @Test
    fun overwritesPreviousFileWithoutLeavingTemporaries() {
        ProjectStorage.current.meta = Meta(name = "First")
        ProjectStorage.save(file)
        ProjectStorage.current.meta = Meta(name = "Second")
        ProjectStorage.save(file)

        assertTrue(ProjectStorage.load(file).isRight())
        assertEquals("Second", ProjectStorage.current.meta.name)
        assertEquals(listOf("project.aig"), directory.list()?.sorted())
    }

    /**
     * Use case: an error is shown to the user, so every failure that happened on a path names it and
     * the caller does not have to remember which file it asked for.
     */
    @Test
    fun everyErrorCarriesItsFile() {
        val asDirectory = File(directory, "project-folder").apply { mkdirs() }
        val broken = File(directory, "broken.aig").apply { writeText("{ this is not an archive") }

        assertEquals(file, ProjectStorage.load(file).leftOrNull()?.file)
        assertEquals(asDirectory, ProjectStorage.load(asDirectory).leftOrNull()?.file)
        assertEquals(broken, ProjectStorage.load(broken).leftOrNull()?.file)
    }

    /** Fills the open project with the values of the complete test project, part by part. */
    private fun seedCurrentProject() {
        val project = TestData.project()

        ProjectStorage.current.meta = project.meta
        ProjectStorage.current.design = project.design
        ProjectStorage.current.book = project.book
    }

    /** The meta part of the document stored in [file], read back the way the storage reads it. */
    private fun storedMeta(): Meta =
        StorageIO.loadFromZip(file, Meta::class).parts[Project.PART_META] as Meta

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

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
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import org.pcsoft.app.aighost.model.project.Project
import org.pcsoft.app.aighost.model.project.book.Book
import org.pcsoft.app.aighost.model.project.design.Design
import org.pcsoft.app.aighost.model.project.meta.Meta
import java.io.File
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

/**
 * Developer tests for [StorageIO], the archive a project document is made of.
 */
class StorageIOTest {

    @TempDir
    lateinit var directory: File

    private val file: File get() = File(directory, "project.aig")

    /**
     * Use case: the user saves a project, so every part lands in an entry of its own that is named
     * after the identifier the part declares instead of after its class.
     */
    @Test
    fun storesEveryPartUnderItsIdentifier() {
        StorageIO.saveToZip(file, TestData.meta(), TestData.design(), TestData.book())

        assertEquals(
            listOf(Project.PART_BOOK, Project.PART_DESIGN, Project.PART_META).sorted(),
            entryNamesOf(file).sorted()
        )
    }

    /**
     * Use case: the user saves a project and opens it again, so every part comes back from the
     * archive exactly as it was written - the second and third entry included.
     */
    @Test
    fun roundTripsEveryPart() {
        StorageIO.saveToZip(file, TestData.meta(), TestData.design(), TestData.book())

        val parts = StorageIO.loadFromZip(file, Meta::class, Design::class, Book::class)

        assertEquals(TestData.meta(), parts[Project.PART_META])
        assertEquals(TestData.design(), parts[Project.PART_DESIGN])
        assertEquals(TestData.book(), parts[Project.PART_BOOK])
    }

    /**
     * Use case: a project carries a part of a plugin that is not installed here, so that entry is
     * skipped and the parts this application knows are still opened.
     */
    @Test
    fun skipsAnEntryWithoutAModelClass() {
        writeArchive(
            file,
            Project.PART_META to """{"name":"My Novel"}""",
            "plugin-notes" to """{"note":"written elsewhere"}"""
        )

        val parts = StorageIO.loadFromZip(file, Meta::class, Design::class, Book::class)

        assertEquals(setOf(Project.PART_META), parts.keys)
        assertEquals("My Novel", (parts[Project.PART_META] as Meta).name)
    }

    /**
     * Use case: the user opens a file that is not a project archive at all, so no part is read
     * instead of a half filled project being reported as complete.
     */
    @Test
    fun readsNoPartFromAForeignFile() {
        file.writeText("{ this is not an archive")

        val parts = StorageIO.loadFromZip(file, Meta::class, Design::class, Book::class)

        assertTrue(parts.isEmpty())
    }

    /**
     * Use case: the user opens the stored document with an archive tool, so every entry holds
     * indented JSON that can be read and edited by hand.
     */
    @Test
    fun writesIndentedJsonPerEntry() {
        StorageIO.saveToZip(file, TestData.meta())

        val content = readEntry(file, Project.PART_META)

        assertTrue(content.contains("\n"), "expected indented JSON but was: $content")
        assertTrue(content.contains(""""name" : "My Novel""""))
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

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

package org.pcsoft.app.aighost.fx.model

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import org.pcsoft.app.aighost.fx.model.common.StyleDataProperty
import org.pcsoft.app.aighost.fx.model.project.book.BookPartProperty
import org.pcsoft.app.aighost.fx.model.project.book.BookProperty
import org.pcsoft.app.aighost.fx.model.project.design.DesignProperty
import org.pcsoft.app.aighost.fx.model.project.meta.MetaProperty
import org.pcsoft.app.aighost.model.common.Alignment
import org.pcsoft.app.aighost.model.project.Project
import java.io.File
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

/**
 * Developer tests for [FXProjectStorage].
 *
 * The storage hands the open project to the user interface as a property tree and puts another
 * project in place whenever one is loaded from a file or a fresh one is started. The editor, the
 * project tree and the design dialog hang on the properties of that tree, from the project itself
 * down to the size of a single font, so such an exchange has to reach every single one of them - a
 * property that stays quiet would leave the previous project on screen.
 *
 * Every test therefore puts the whole tree under observation, establishes a state in which every
 * field differs from the state the operation produces, and then asserts that no property stayed
 * quiet.
 */
class FXProjectStorageTest {

    @TempDir
    lateinit var directory: File

    private lateinit var metaProperty: MetaProperty
    private lateinit var designProperty: DesignProperty
    private lateinit var bookProperty: BookProperty
    private lateinit var recorder: ChangeRecorder

    /**
     * Starts from a fresh project and puts every property of its tree under observation, down to
     * every single field of every nested object.
     */
    @BeforeEach
    fun setUp() {
        FXProjectStorage.new()

        metaProperty = FXProjectStorage.current.metaProperty as MetaProperty
        designProperty = FXProjectStorage.current.designProperty as DesignProperty
        bookProperty = FXProjectStorage.current.bookProperty as BookProperty

        recorder = ChangeRecorder()
        recorder.watch("project", FXProjectStorage.current)

        recorder.watch("project.meta", metaProperty)
        recorder.watch("project.meta.name", metaProperty.nameProperty)
        recorder.watch("project.meta.author", metaProperty.authorProperty)
        recorder.watch("project.meta.copyright", metaProperty.copyrightProperty)

        recorder.watch("project.design", designProperty)
        recorder.watch("project.design.author", designProperty.authorDesignProperty)
        watchStyle("project.design.author.style", designProperty.authorDesignProperty.styleProperty)
        recorder.watch("project.design.copyright", designProperty.copyrightDesignProperty)
        watchStyle("project.design.copyright.style", designProperty.copyrightDesignProperty.styleProperty)
        recorder.watch("project.design.copyright.show", designProperty.copyrightDesignProperty.showProperty)
        recorder.watch("project.design.title", designProperty.titleDesignProperty)
        watchStyle("project.design.title.style", designProperty.titleDesignProperty.styleProperty)
        recorder.watch("project.design.chapter", designProperty.chapterDesignProperty)
        watchStyle("project.design.chapter.titleStyle", designProperty.chapterDesignProperty.titleStyleProperty)
        watchStyle(
            "project.design.chapter.titleAppendixStyle",
            designProperty.chapterDesignProperty.titleAppendixStyleProperty
        )
        recorder.watch("project.design.text", designProperty.textDesignProperty)
        watchStyle("project.design.text.style", designProperty.textDesignProperty.styleProperty)
        recorder.watch("project.design.startWithEmptyPage", designProperty.startWithEmptyPageProperty)
        recorder.watch("project.design.endWithEmptyPage", designProperty.endWithEmptyPageProperty)

        recorder.watch("project.book", bookProperty)
        recorder.watch("project.book.title", bookProperty.titleProperty)
        recorder.watch("project.book.titleAppendix", bookProperty.titleAppendixProperty)
        recorder.watch("project.book.chapters", bookProperty.chaptersProperty)
        watchBookPart("project.book.prolog", bookProperty.prologProperty)
        watchBookPart("project.book.epilog", bookProperty.epilogProperty)
        recorder.watch("project.book.blurb", bookProperty.blurbProperty)
        recorder.watch("project.book.blurb.paragraph", bookProperty.blurbProperty.paragraphProperty)
    }

    /**
     * Loading a project from a file has to reach every property of the tree.
     *
     * A fresh project is open, so every field carries its default. A document differing from that
     * default in every single field is loaded, and afterwards every watched property - the project,
     * its three parts, every nested object and every field of all of them - must have reported a
     * change and must hand out the loaded value.
     */
    @Test
    fun `loading a project fires a change on every property of the tree`() {
        val file = writeProjectFile()
        recorder.reset()

        val result = FXProjectStorage.load(file)

        assertTrue(result.isRight()) { "Loading the written project failed: $result" }
        recorder.assertAllFired("Loading a project")

        assertEquals("Loaded Project", FXProjectStorage.current.nameProperty.get())
        assertEquals("Jane Doe", FXProjectStorage.current.authorProperty.get())
        assertEquals("Georgia", designProperty.authorDesignProperty.styleProperty.fontProperty.nameProperty.get())
        assertEquals(21, designProperty.authorDesignProperty.styleProperty.fontProperty.sizeProperty.get())
        assertEquals(
            Alignment.CENTER,
            designProperty.authorDesignProperty.styleProperty.alignmentProperty.get()
        )
        assertEquals(true, designProperty.copyrightDesignProperty.showProperty.get())
        assertEquals("The Loaded Book", bookProperty.titleProperty.get())
        assertEquals("Before It Begins", bookProperty.prologProperty.titleProperty.get())
        assertEquals(1, bookProperty.chaptersProperty.size)
    }

    /**
     * Starting a fresh project has to reach every property of the tree.
     *
     * A loaded project is open, so every field differs from its default. After a fresh project was
     * started every watched property must have reported a change and must hand out the default
     * again - including the nested objects that are gone in a fresh project, such as the prolog.
     */
    @Test
    fun `creating a new project fires a change on every property of the tree`() {
        val file = writeProjectFile()
        assertTrue(FXProjectStorage.load(file).isRight()) { "The test could not establish a loaded project" }
        recorder.reset()

        FXProjectStorage.new()

        recorder.assertAllFired("Creating a new project")

        assertEquals("New Project", FXProjectStorage.current.nameProperty.get())
        assertEquals("", FXProjectStorage.current.authorProperty.get())
        assertEquals("Arial", designProperty.authorDesignProperty.styleProperty.fontProperty.nameProperty.get())
        assertEquals(Alignment.LEFT, designProperty.authorDesignProperty.styleProperty.alignmentProperty.get())
        assertEquals(false, designProperty.copyrightDesignProperty.showProperty.get())
        assertEquals("My Book", bookProperty.titleProperty.get())
        assertEquals(null, bookProperty.prologProperty.get())
        assertEquals(0, bookProperty.chaptersProperty.size)
    }

    /**
     * Starting a fresh project onto a fresh project has to keep the whole tree quiet.
     *
     * The values do not change, so no bound control has anything to redraw. A property reporting a
     * change here would repaint the whole user interface for nothing.
     */
    @Test
    fun `creating a new project onto the same values keeps every property quiet`() {
        recorder.reset()

        FXProjectStorage.new()

        recorder.assertNoneFired("Creating a new project onto the same values")
    }

    /**
     * A failing load must not touch the property tree.
     *
     * The named file does not exist, so the storage answers with an error and leaves the open project
     * alone. No property may report a change for that.
     */
    @Test
    fun `a load that finds no file keeps every property quiet`() {
        recorder.reset()

        val result = FXProjectStorage.load(File(directory, "missing-project.aig"))

        assertTrue(result.isLeft()) { "Loading a missing file was expected to fail, but was $result" }
        recorder.assertNoneFired("A load that finds no file")
        assertEquals("New Project", FXProjectStorage.current.nameProperty.get())
    }

    /** Puts a style and every field nested in it under observation. */
    private fun watchStyle(prefix: String, property: StyleDataProperty) {
        recorder.watch(prefix, property)
        recorder.watch("$prefix.alignment", property.alignmentProperty)
        recorder.watch("$prefix.font", property.fontProperty)
        recorder.watch("$prefix.font.name", property.fontProperty.nameProperty)
        recorder.watch("$prefix.font.size", property.fontProperty.sizeProperty)
        recorder.watch("$prefix.font.bold", property.fontProperty.boldProperty)
        recorder.watch("$prefix.font.italic", property.fontProperty.italicProperty)
    }

    /** Puts a part of the book - prolog or epilog - and every field nested in it under observation. */
    private fun watchBookPart(prefix: String, property: BookPartProperty<*>) {
        recorder.watch(prefix, property)
        recorder.watch("$prefix.title", property.titleProperty)
        recorder.watch("$prefix.titleAppendix", property.titleAppendixProperty)
        recorder.watch("$prefix.paragraph", property.paragraphProperty)
    }

    /**
     * Writes a project document carrying a value different from the default in every single field,
     * so a property that does not report a change on loading it can only be a property that was not
     * refreshed.
     *
     * The document is an archive holding one entry per project part, which is how the storage writes
     * a project to disk.
     */
    private fun writeProjectFile(): File {
        val file = File(directory, "loaded-project.aig")

        val meta = """
            {
              "name": "Loaded Project",
              "author": "Jane Doe",
              "copyright": "(c) 2026 Jane Doe"
            }
        """.trimIndent()

        val design = """
            {
              "authorDesign": { "style": { "font": { "name": "Georgia", "size": 21, "bold": true, "italic": true }, "alignment": "CENTER" } },
              "copyrightDesign": { "style": { "font": { "name": "Palatino", "size": 22, "bold": true, "italic": true }, "alignment": "CENTER" }, "show": true },
              "titleDesign": { "style": { "font": { "name": "Baskerville", "size": 23, "bold": true, "italic": true }, "alignment": "CENTER" } },
              "chapterDesign": {
                "titleStyle": { "font": { "name": "Garamond", "size": 25, "bold": true, "italic": true }, "alignment": "CENTER" },
                "titleAppendixStyle": { "font": { "name": "Bodoni", "size": 26, "bold": true, "italic": true }, "alignment": "CENTER" }
              },
              "textDesign": { "style": { "font": { "name": "Minion", "size": 27, "bold": true, "italic": true }, "alignment": "CENTER" } },
              "startWithEmptyPage": false,
              "endWithEmptyPage": false
            }
        """.trimIndent()

        val book = """
            {
              "title": "The Loaded Book",
              "titleAppendix": ["A Story Of Loading"],
              "prolog": {
                "title": "Before It Begins",
                "titleAppendix": ["Prologue"],
                "paragraph": ["The first paragraph of the prolog."]
              },
              "chapters": [
                {
                  "name": "chapter-one",
                  "title": "The First Chapter",
                  "titleAppendix": ["Chapter One"],
                  "paragraph": ["The first paragraph of the chapter."]
                }
              ],
              "epilog": {
                "title": "After It Ended",
                "titleAppendix": ["Epilogue"],
                "paragraph": ["The first paragraph of the epilog."]
              },
              "blurb": {
                "paragraph": ["A short blurb about the book."]
              }
            }
        """.trimIndent()

        ZipOutputStream(file.outputStream()).use { stream ->
            for ((name, content) in listOf(
                Project.PART_META to meta,
                Project.PART_DESIGN to design,
                Project.PART_BOOK to book
            )) {
                // Every part sits in an entry named after its identifier and carrying the extension
                // of the document format; an entry without it is not read as a part.
                stream.putNextEntry(ZipEntry("$name.json"))
                stream.write(content.toByteArray())
                stream.closeEntry()
            }
        }

        return file
    }
}

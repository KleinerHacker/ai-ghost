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
import org.pcsoft.app.aighost.fx.model.project.BookPartProperty
import org.pcsoft.app.aighost.fx.model.project.BookProperty
import org.pcsoft.app.aighost.fx.model.project.SettingsProperty
import org.pcsoft.app.aighost.model.common.Alignment
import java.io.File

/**
 * Developer tests for [FXProjectStorage].
 *
 * The storage hands the open project to the user interface as a property tree and puts another
 * project in place whenever one is loaded from a file or a fresh one is started. The editor, the
 * project tree and the settings dialog hang on the properties of that tree, from the project itself
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

    private lateinit var settingsProperty: SettingsProperty
    private lateinit var bookProperty: BookProperty
    private lateinit var recorder: ChangeRecorder

    /**
     * Starts from a fresh project and puts every property of its tree under observation, down to
     * every single field of every nested object.
     */
    @BeforeEach
    fun setUp() {
        FXProjectStorage.new()

        settingsProperty = FXProjectStorage.current.settingsProperty as SettingsProperty
        bookProperty = FXProjectStorage.current.bookProperty as BookProperty

        recorder = ChangeRecorder()
        recorder.watch("project", FXProjectStorage.current)
        recorder.watch("project.name", FXProjectStorage.current.nameProperty)
        recorder.watch("project.author", FXProjectStorage.current.authorProperty)
        recorder.watch("project.copyright", FXProjectStorage.current.copyrightProperty)

        recorder.watch("project.settings", settingsProperty)
        watchStyle("project.settings.authorFont", settingsProperty.authorFontProperty)
        watchStyle("project.settings.copyrightFont", settingsProperty.copyrightFontProperty)
        watchStyle("project.settings.titleFont", settingsProperty.titleFontProperty)
        watchStyle("project.settings.titleAppendixFont", settingsProperty.titleAppendixFontProperty)
        watchStyle("project.settings.chapterFont", settingsProperty.chapterFontProperty)
        watchStyle("project.settings.chapterAppendixFont", settingsProperty.chapterAppendixFontProperty)
        watchStyle("project.settings.textFont", settingsProperty.textFontProperty)
        recorder.watch("project.settings.copyrightPage", settingsProperty.copyrightPageProperty)
        recorder.watch("project.settings.startWithEmptyPage", settingsProperty.startWithEmptyPageProperty)
        recorder.watch("project.settings.endWithEmptyPage", settingsProperty.endWithEmptyPageProperty)

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
     * the settings, the book, every nested object and every field of all of them - must have
     * reported a change and must hand out the loaded value.
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
        assertEquals("Georgia", settingsProperty.authorFontProperty.fontProperty.nameProperty.get())
        assertEquals(21, settingsProperty.authorFontProperty.fontProperty.sizeProperty.get())
        assertEquals(Alignment.CENTER, settingsProperty.authorFontProperty.alignmentProperty.get())
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
        assertEquals("Arial", settingsProperty.authorFontProperty.fontProperty.nameProperty.get())
        assertEquals(Alignment.LEFT, settingsProperty.authorFontProperty.alignmentProperty.get())
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
     */
    private fun writeProjectFile(): File {
        val file = File(directory, "loaded-project.aig")
        file.writeText(
            """
            {
              "name": "Loaded Project",
              "author": "Jane Doe",
              "copyright": "(c) 2026 Jane Doe",
              "settings": {
                "authorFont": { "font": { "name": "Georgia", "size": 21, "bold": true, "italic": true }, "alignment": "CENTER" },
                "copyrightFont": { "font": { "name": "Palatino", "size": 22, "bold": true, "italic": true }, "alignment": "CENTER" },
                "titleFont": { "font": { "name": "Baskerville", "size": 23, "bold": true, "italic": true }, "alignment": "CENTER" },
                "titleAppendixFont": { "font": { "name": "Caslon", "size": 24, "bold": true, "italic": true }, "alignment": "CENTER" },
                "chapterFont": { "font": { "name": "Garamond", "size": 25, "bold": true, "italic": true }, "alignment": "CENTER" },
                "chapterAppendixFont": { "font": { "name": "Bodoni", "size": 26, "bold": true, "italic": true }, "alignment": "CENTER" },
                "textFont": { "font": { "name": "Minion", "size": 27, "bold": true, "italic": true }, "alignment": "CENTER" },
                "copyrightPage": true,
                "startWithEmptyPage": false,
                "endWithEmptyPage": false
              },
              "book": {
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
            }
            """.trimIndent()
        )
        return file
    }
}

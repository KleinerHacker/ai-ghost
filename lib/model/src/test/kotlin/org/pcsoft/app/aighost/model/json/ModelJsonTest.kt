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

package org.pcsoft.app.aighost.model.json

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.databind.exc.InvalidFormatException
import com.fasterxml.jackson.databind.exc.MismatchedInputException
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.pcsoft.app.aighost.model.common.Alignment
import org.pcsoft.app.aighost.model.TestData
import org.pcsoft.app.aighost.model.pref.Preferences
import org.pcsoft.app.aighost.model.pref.ThemeMode
import org.pcsoft.app.aighost.model.project.Blurb
import org.pcsoft.app.aighost.model.project.Book
import org.pcsoft.app.aighost.model.project.Chapter
import org.pcsoft.app.aighost.model.project.Epilog
import org.pcsoft.app.aighost.model.project.Project
import org.pcsoft.app.aighost.model.project.Prolog

/**
 * Developer tests for the JSON persistence of the whole model.
 *
 * Where the tests of the single classes check one type, these tests write and parse complete
 * documents the way the application stores them on disk, including pretty printing and hand written
 * files.
 */
class ModelJsonTest {

    private val mapper: ObjectMapper = ObjectMapper()
        .registerKotlinModule()
        .enable(SerializationFeature.INDENT_OUTPUT)

    /**
     * Use case: a project file that a user edited by hand is opened, so a full JSON document with
     * arbitrary formatting is parsed into the complete object graph, including prolog, epilog and
     * blurb.
     */
    @Test
    fun parsesHandWrittenProjectDocument() {
        val json = """
            {
              "name" : "My Novel",
              "author" : "Jane Doe",
              "copyright" : "(c) 2026 Jane Doe",
              "settings" : {
                "authorFont" : { "font" : { "name" : "Sans", "size" : 16, "bold" : false, "italic" : false }, "alignment" : "CENTER" },
                "copyrightFont" : { "font" : { "name" : "Serif", "size" : 8, "bold" : false, "italic" : false } },
                "titleFont" : { "font" : { "name" : "Sans", "size" : 28, "bold" : true, "italic" : false }, "alignment" : "CENTER" },
                "titleAppendixFont" : { "font" : { "name" : "Sans", "size" : 18, "bold" : false, "italic" : true }, "alignment" : "CENTER" },
                "chapterFont" : { "font" : { "name" : "Sans", "size" : 20, "bold" : true, "italic" : false } },
                "chapterAppendixFont" : { "font" : { "name" : "Sans", "size" : 14, "bold" : false, "italic" : true } },
                "textFont" : { "font" : { "name" : "Serif", "size" : 11, "bold" : false, "italic" : false }, "alignment" : "BLOCK" },
                "copyrightPage" : true,
                "startWithEmptyPage" : false,
                "endWithEmptyPage" : true
              },
              "book" : {
                "title" : "My Novel",
                "prolog" : { "title" : "Before It All", "paragraph" : [ "Long before." ] },
                "chapters" : [
                  { "name" : "first", "title" : "Prologue", "paragraph" : [ "Once upon a time." ] },
                  { "name" : "second", "title" : "Chapter 1", "paragraph" : [ "The beginning.", "And on it went." ] }
                ],
                "epilog" : { "title" : "After It All", "paragraph" : [ "And that was that." ] },
                "blurb" : { "paragraph" : [ "A gripping tale." ] }
              }
            }
        """.trimIndent()

        val project: Project = mapper.readValue(json)

        assertEquals("My Novel", project.name)
        assertEquals("Jane Doe", project.author)
        assertEquals(Alignment.BLOCK, project.settings.textFont.alignment)
        assertEquals(true, project.settings.copyrightPage)
        assertEquals(listOf("first", "second"), project.book.chapters.map(Chapter::name))
        assertEquals(listOf("The beginning.", "And on it went."), project.book.chapters[1].paragraph)
        assertEquals(Prolog("Before It All", paragraph = listOf("Long before.")), project.book.prolog)
        assertEquals(Epilog("After It All", paragraph = listOf("And that was that.")), project.book.epilog)
        assertEquals(Blurb(listOf("A gripping tale.")), project.book.blurb)
    }

    /**
     * Use case: a project is opened whose book was never given a prolog, an epilog or a blurb, so
     * the missing properties are read back as absent parts instead of failing the parse.
     */
    @Test
    fun parsesProjectDocumentWithoutOptionalBookParts() {
        val json = """
            {
              "name" : "My Novel",
              "author" : "Jane Doe",
              "copyright" : "(c) 2026 Jane Doe",
              "settings" : {
                "authorFont" : { "font" : { "name" : "Sans", "size" : 16, "bold" : false, "italic" : false } },
                "copyrightFont" : { "font" : { "name" : "Serif", "size" : 8, "bold" : false, "italic" : false } },
                "titleFont" : { "font" : { "name" : "Sans", "size" : 28, "bold" : true, "italic" : false } },
                "titleAppendixFont" : { "font" : { "name" : "Sans", "size" : 18, "bold" : false, "italic" : true } },
                "chapterFont" : { "font" : { "name" : "Sans", "size" : 20, "bold" : true, "italic" : false } },
                "chapterAppendixFont" : { "font" : { "name" : "Sans", "size" : 14, "bold" : false, "italic" : true } },
                "textFont" : { "font" : { "name" : "Serif", "size" : 11, "bold" : false, "italic" : false } },
                "copyrightPage" : false,
                "startWithEmptyPage" : false,
                "endWithEmptyPage" : false
              },
              "book" : {
                "title" : "My Novel",
                "chapters" : [ { "name" : "first", "title" : "Prologue" } ]
              }
            }
        """.trimIndent()

        val project: Project = mapper.readValue(json)

        assertNull(project.book.prolog)
        assertNull(project.book.epilog)
        assertNull(project.book.blurb)
        assertEquals(listOf("first"), project.book.chapters.map(Chapter::name))
    }

    /**
     * Use case: a project is saved to disk, so the written document is readable JSON that parses back
     * into exactly the project that was written.
     */
    @Test
    fun writesAndParsesProjectDocument() {
        val project = TestData.project()

        val json = mapper.writeValueAsString(project)
        val restored: Project = mapper.readValue(json)

        assertEquals(project, restored)
    }

    /**
     * Use case: chapter text contains quotes, backslashes and line breaks, so the characters are
     * escaped on write and restored unchanged on parse instead of breaking the document.
     */
    @Test
    fun escapesSpecialCharactersInChapterText() {
        val project = TestData.project().copy(
            book = Book(
                "Special\"Characters",
                listOf("A \\ backslash"),
                chapters = listOf(
                    Chapter(
                        "chapter\\1",
                        "Chapter\\1",
                        paragraph = listOf("He said: \"Hello\"\nand left.\tEnd")
                    )
                )
            )
        )

        val restored: Project = mapper.readValue(mapper.writeValueAsString(project))

        assertEquals(project, restored)
    }

    /**
     * Use case: a project is written whose texts contain non ASCII characters, so accented letters
     * and dashes survive the write and parse cycle unchanged.
     */
    @Test
    fun keepsUnicodeText() {
        val project = TestData.project().copy(
            author = "Renée Müller",
            book = Book(
                "Café Notes",
                chapters = listOf(
                    Chapter("naïve", "Naïve Beginnings", paragraph = listOf("A café, a résumé – ok."))
                ),
                blurb = Blurb(listOf("Crème de la crème – a novel."))
            )
        )

        val restored: Project = mapper.readValue(mapper.writeValueAsString(project))

        assertEquals(project, restored)
    }

    /**
     * Use case: the preferences and a project are stored as separate documents, so both parse
     * independently of each other with their own root type.
     */
    @Test
    fun writesAndParsesPreferencesDocument() {
        val preferences = Preferences(themeMode = ThemeMode.DARK)

        val json = mapper.writeValueAsString(preferences)
        val restored: Preferences = mapper.readValue(json)

        assertEquals(preferences, restored)
    }

    /**
     * Use case: a preferences file holds a theme name the application does not know, so the parse
     * fails loudly instead of silently continuing with an undefined appearance.
     */
    @Test
    fun rejectsUnknownThemeMode() {
        assertThrows<InvalidFormatException> {
            mapper.readValue<Preferences>("""{"themeMode":"NEON"}""")
        }
    }

    /**
     * Use case: a project file is truncated or damaged, so opening it fails with a parse error
     * instead of producing a half filled project.
     */
    @Test
    fun rejectsBrokenDocument() {
        assertThrows<JsonProcessingException> {
            mapper.readValue<Project>("""{"name":"My Novel","book":{"title":""")
        }
    }

    /**
     * Use case: a project file misses the mandatory name, so opening it fails instead of creating a
     * project without an identity.
     */
    @Test
    fun rejectsProjectWithoutName() {
        val json = mapper.writeValueAsString(TestData.project())
            .replaceFirst(""""name" : "My Novel",""", "")

        assertThrows<MismatchedInputException> { mapper.readValue<Project>(json) }
    }
}

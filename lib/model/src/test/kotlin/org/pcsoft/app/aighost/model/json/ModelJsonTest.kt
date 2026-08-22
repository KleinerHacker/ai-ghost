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
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.pcsoft.app.aighost.model.common.Alignment
import org.pcsoft.app.aighost.model.TestData
import org.pcsoft.app.aighost.model.pref.Preferences
import org.pcsoft.app.aighost.model.pref.ThemeMode
import org.pcsoft.app.aighost.model.project.Book
import org.pcsoft.app.aighost.model.project.Chapter
import org.pcsoft.app.aighost.model.project.Project

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
     * arbitrary formatting is parsed into the complete object graph.
     */
    @Test
    fun parsesHandWrittenProjectDocument() {
        val json = """
            {
              "name" : "My Novel",
              "author" : "Jane Doe",
              "copyright" : "(c) 2026 Jane Doe",
              "settings" : {
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
                "chapters" : [
                  { "title" : "Prologue", "paragraph" : [ "Once upon a time." ] },
                  { "title" : "Chapter 1", "paragraph" : [ "The beginning.", "And on it went." ] }
                ]
              }
            }
        """.trimIndent()

        val project: Project = mapper.readValue(json)

        assertEquals("My Novel", project.name)
        assertEquals("Jane Doe", project.author)
        assertEquals(Alignment.BLOCK, project.settings.textFont.alignment)
        assertEquals(true, project.settings.copyrightPage)
        assertEquals(2, project.book.chapters.size)
        assertEquals(listOf("The beginning.", "And on it went."), project.book.chapters[1].paragraph)
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
        val project = TestData.project().let { project ->
            project.copy(
                book = Book(
                    "Special\"Characters",
                    listOf("A \\ backslash"),
                    listOf(Chapter("Chapter\\1", paragraph = listOf("He said: \"Hello\"\nand left.\tEnd")))
                )
            )
        }

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
                chapters = listOf(Chapter("Naïve Beginnings", paragraph = listOf("A café, a résumé – ok.")))
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

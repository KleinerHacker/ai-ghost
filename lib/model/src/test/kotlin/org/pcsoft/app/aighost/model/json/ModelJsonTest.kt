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
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.pcsoft.app.aighost.model.TestData
import org.pcsoft.app.aighost.model.common.Alignment
import org.pcsoft.app.aighost.model.pref.Preferences
import org.pcsoft.app.aighost.model.pref.ThemeMode
import org.pcsoft.app.aighost.model.project.book.Blurb
import org.pcsoft.app.aighost.model.project.book.Book
import org.pcsoft.app.aighost.model.project.book.Chapter
import org.pcsoft.app.aighost.model.project.book.Epilog
import org.pcsoft.app.aighost.model.project.book.Prolog
import org.pcsoft.app.aighost.model.project.common.AIPrompt
import org.pcsoft.app.aighost.model.project.design.Design
import org.pcsoft.app.aighost.model.project.meta.Meta

/**
 * Developer tests for the JSON persistence of the whole model.
 *
 * Where the tests of the single classes check one type, these tests write and parse complete
 * documents the way the application stores them in the entries of a project archive, including
 * pretty printing and hand written files.
 */
class ModelJsonTest {

    private val mapper: ObjectMapper = ObjectMapper()
        .registerKotlinModule()
        .enable(SerializationFeature.INDENT_OUTPUT)

    /**
     * Use case: a manuscript that a user edited by hand is opened, so a full JSON document with
     * arbitrary formatting is parsed into the complete object graph, including prolog, epilog and
     * blurb.
     */
    @Test
    fun parsesHandWrittenBookDocument() {
        val json = """
            {
              "title" : "My Novel",
              "prompts" : { "contentPrompt" : "Tell a story in two parts.", "stylePrompt" : "Warm and calm." },
              "prolog" : { "title" : "Before It All", "paragraph" : [ "Long before." ] },
              "chapters" : [
                { "name" : "first", "title" : "Prologue", "paragraph" : [ "Once upon a time." ] },
                { "name" : "second", "title" : "Chapter 1", "paragraph" : [ "The beginning.", "And on it went." ] }
              ],
              "epilog" : { "title" : "After It All", "paragraph" : [ "And that was that." ] },
              "blurb" : { "paragraph" : [ "A gripping tale." ] }
            }
        """.trimIndent()

        val book: Book = mapper.readValue(json)

        assertEquals("My Novel", book.title)
        assertEquals(AIPrompt("Tell a story in two parts.", "Warm and calm."), book.prompts)
        assertEquals(listOf("first", "second"), book.chapters.map(Chapter::name))
        assertEquals(listOf("The beginning.", "And on it went."), book.chapters[1].paragraph)
        assertEquals(Prolog("Before It All", paragraph = listOf("Long before.")), book.prolog)
        assertEquals(Epilog("After It All", paragraph = listOf("And that was that.")), book.epilog)
        assertEquals(Blurb(paragraph = listOf("A gripping tale.")), book.blurb)
    }

    /**
     * Use case: a design that a user edited by hand is opened, so every style and every page flag is
     * parsed into the nested object graph instead of being dropped.
     */
    @Test
    fun parsesHandWrittenDesignDocument() {
        val json = """
            {
              "authorDesign" : { "style" : { "font" : { "name" : "Sans", "size" : 16 }, "alignment" : "CENTER" } },
              "copyrightDesign" : { "style" : { "font" : { "name" : "Serif", "size" : 8 } }, "show" : true },
              "titleDesign" : { "style" : { "font" : { "name" : "Sans", "size" : 28, "bold" : true } } },
              "chapterDesign" : {
                "titleStyle" : { "font" : { "name" : "Sans", "size" : 20, "bold" : true } },
                "titleAppendixStyle" : { "font" : { "name" : "Sans", "size" : 14, "italic" : true } }
              },
              "textDesign" : { "style" : { "font" : { "name" : "Serif", "size" : 11 }, "alignment" : "BLOCK" } },
              "startWithEmptyPage" : false,
              "endWithEmptyPage" : true
            }
        """.trimIndent()

        val design: Design = mapper.readValue(json)

        assertEquals(16, design.authorDesign.style.font.size)
        assertEquals(Alignment.CENTER, design.authorDesign.style.alignment)
        assertEquals(true, design.copyrightDesign.show)
        assertEquals(true, design.titleDesign.style.font.bold)
        assertEquals(true, design.chapterDesign.titleAppendixStyle.font.italic)
        assertEquals(Alignment.BLOCK, design.textDesign.style.alignment)
        assertEquals(false, design.startWithEmptyPage)
        assertEquals(true, design.endWithEmptyPage)
    }

    /**
     * Use case: a project is opened whose book was never given a prolog, an epilog or a blurb, so
     * the missing properties are read back as absent parts instead of failing the parse.
     */
    @Test
    fun parsesBookDocumentWithoutOptionalParts() {
        val json = """
            {
              "title" : "My Novel",
              "chapters" : [ { "name" : "first", "title" : "Prologue" } ]
            }
        """.trimIndent()

        val book: Book = mapper.readValue(json)

        assertNull(book.prolog)
        assertNull(book.epilog)
        assertNull(book.blurb)
        assertEquals(listOf("first"), book.chapters.map(Chapter::name))
    }

    /**
     * Use case: a project is saved to disk, so every part is written as readable JSON that parses
     * back into exactly the part that was written.
     */
    @Test
    fun writesAndParsesEveryProjectPart() {
        assertEquals(TestData.meta(), mapper.readValue<Meta>(mapper.writeValueAsString(TestData.meta())))
        assertEquals(TestData.design(), mapper.readValue<Design>(mapper.writeValueAsString(TestData.design())))
        assertEquals(TestData.book(), mapper.readValue<Book>(mapper.writeValueAsString(TestData.book())))
    }

    /**
     * Use case: chapter text contains quotes, backslashes and line breaks, so the characters are
     * escaped on write and restored unchanged on parse instead of breaking the document.
     */
    @Test
    fun escapesSpecialCharactersInChapterText() {
        val book = Book(
            title = "Special\"Characters",
            titleAppendix = listOf("A \\ backslash"),
            chapters = listOf(
                Chapter(
                    "chapter\\1",
                    "Chapter\\1",
                    paragraph = listOf("He said: \"Hello\"\nand left.\tEnd")
                )
            )
        )

        val restored: Book = mapper.readValue(mapper.writeValueAsString(book))

        assertEquals(book, restored)
    }

    /**
     * Use case: a project is written whose texts contain non ASCII characters, so accented letters
     * and dashes survive the write and parse cycle unchanged.
     */
    @Test
    fun keepsUnicodeText() {
        val meta = Meta(name = "Café Notes", author = "Renée Müller")
        val book = Book(
            title = "Café Notes",
            chapters = listOf(
                Chapter("naïve", "Naïve Beginnings", paragraph = listOf("A café, a résumé – ok."))
            ),
            blurb = Blurb(paragraph = listOf("Crème de la crème – a novel."))
        )

        assertEquals(meta, mapper.readValue<Meta>(mapper.writeValueAsString(meta)))
        assertEquals(book, mapper.readValue<Book>(mapper.writeValueAsString(book)))
    }

    /**
     * Use case: the preferences and a project are stored as separate documents, so both parse
     * independently of each other with their own root type.
     */
    @Test
    fun writesAndParsesPreferencesDocument() {
        val preferences = Preferences().apply { appearance.themeMode = ThemeMode.DARK }

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
            mapper.readValue<Preferences>("""{"appearance":{"themeMode":"NEON"}}""")
        }
    }

    /**
     * Use case: a project entry is truncated or damaged, so opening it fails with a parse error
     * instead of producing a half filled part.
     */
    @Test
    fun rejectsBrokenDocument() {
        assertThrows<JsonProcessingException> {
            mapper.readValue<Book>("""{"title":""")
        }
    }

    /**
     * Use case: a project file carries no name, so it is opened under the name a new project starts
     * with instead of being rejected, and everything else is read as written.
     */
    @Test
    fun readsMetaWithoutNameAsDefault() {
        val json = mapper.writeValueAsString(TestData.meta())
            .replaceFirst(""""name" : "My Novel",""", "")

        val meta: Meta = mapper.readValue(json)

        assertEquals("New Project", meta.name)
        assertEquals("Jane Doe", meta.author)
    }
}

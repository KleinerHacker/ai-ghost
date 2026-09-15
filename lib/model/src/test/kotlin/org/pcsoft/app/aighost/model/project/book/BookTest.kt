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

package org.pcsoft.app.aighost.model.project.book

import com.fasterxml.jackson.core.JacksonException
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.pcsoft.app.aighost.model.TestData
import org.pcsoft.app.aighost.model.project.common.AIPrompt
import org.pcsoft.framework.simplay.engine.model.Document
import org.pcsoft.framework.simplay.engine.model.PageNumbering

/**
 * Developer tests for [org.pcsoft.app.aighost.model.project.book.Book].
 */
class BookTest {

    private val mapper: ObjectMapper = ObjectMapper().registerKotlinModule()

    /**
     * Use case: the user creates a book before writing anything, so it starts without chapters
     * instead of requiring one up front.
     */
    @Test
    fun defaultsToEmptyChapters() {
        assertEquals(emptyList<Chapter>(), Book().chapters)
    }

    /**
     * Use case: a book is created before the user described what it is about, so it starts with
     * empty prompts instead of demanding a description up front.
     */
    @Test
    fun defaultsToEmptyPrompts() {
        assertEquals(AIPrompt(), Book().prompts)
    }

    /**
     * Use case: prolog, epilog and blurb are switched on through the menu, so a fresh book already
     * carries all three of them, empty and not yet part of the book.
     */
    @Test
    fun defaultsToEmptyPrologEpilogAndBlurbThatAreNotIncluded() {
        val book = Book()

        assertEquals(Copyright(), book.copyright)
        assertTrue(book.copyright.included)
        assertEquals(Prolog(), book.prolog)
        assertEquals(Epilog(), book.epilog)
        assertEquals(Blurb(), book.blurb)
        assertFalse(book.prolog.included)
        assertFalse(book.epilog.included)
        assertFalse(book.blurb.included)
    }

    /**
     * Use case: a book is written to disk, so prompts, chapters and the three switchable parts appear
     * in the JSON under the stable property names the file format promises, and the manuscript's
     * document appears as the single `document` string the format promises since IP-37.
     */
    @Test
    fun serialisesPromptsAndChapters() {
        val chapter = Chapter("prologue")
        val book = Book(
            prompts = AIPrompt("Tell a story in two parts.", "Warm and calm."),
            copyright = Copyright(included = true),
            chapters = listOf(chapter)
        )

        val json = mapper.writeValueAsString(book)

        assertEquals(
            """{"version":2,""" +
                """"prompts":{"contentPrompt":"Tell a story in two parts.","stylePrompt":"Warm and calm."},""" +
                """"copyright":{"included":true},""" +
                """"prolog":{"prompts":{"contentPrompt":"","stylePrompt":""},"included":false},""" +
                """"chapters":[{"name":"prologue","id":"${chapter.id}",""" +
                """"prompts":{"contentPrompt":"","stylePrompt":""}}],""" +
                """"epilog":{"prompts":{"contentPrompt":"","stylePrompt":""},"included":false},""" +
                """"blurb":{"prompt":"","paragraph":[],"included":false},""" +
                """"document":${mapper.writeValueAsString(DocumentCodec.encode(Document()))}}""",
            json
        )
    }

    /**
     * Use case: a project is created before the manuscript's document exists yet, so the book starts
     * with an empty document instead of requiring one up front - the same way it starts without
     * chapters.
     */
    @Test
    fun defaultsToEmptyDocument() {
        assertEquals(Document(), Book().document)
    }

    /**
     * Use case: the writing surface replaces the manuscript's document after an edit, so the change is
     * readable back through [Book.document] and is what Jackson actually persists in
     * [Book.documentPayload].
     */
    @Test
    fun writingDocumentIsReadableAgainAndPersistedAsPayload() {
        val document = Document(numbering = PageNumbering.OFF.copy(startNumber = 3))
        val book = Book()

        book.document = document

        assertEquals(document, book.document)
        assertEquals(DocumentCodec.encode(document), book.documentPayload)
    }

    /**
     * Use case: two books are compared - for instance while testing a round trip - so a difference in
     * the manuscript's document is not silently ignored just because [Book.document] is a computed
     * property outside the generated `equals`.
     */
    @Test
    fun documentDifferenceIsVisibleThroughDocumentPayload() {
        val withDocument = Book().apply { document = Document(numbering = PageNumbering.OFF.copy(startNumber = 3)) }

        assertNotEquals(Book(), withDocument)
    }

    /**
     * Use case: a project archive was damaged and `book.json` now carries a `document` field that is
     * not valid `Document` JSON, so reading the book fails loudly instead of handing out a book nobody
     * can trust.
     */
    @Test
    fun failsToReadACorruptDocumentPayload() {
        val json = """{"document":"not a document"}"""

        assertThrows(JacksonException::class.java) { mapper.readValue<Book>(json) }
    }

    /**
     * Use case: a stored book is opened again, so all chapters come back with their content and in
     * the order the user arranged them.
     */
    @Test
    fun roundTripsChaptersInOrder() {
        val book = TestData.book()

        val restored: Book = mapper.readValue(mapper.writeValueAsString(book))

        assertEquals(book, restored)
        assertEquals(listOf("first", "second"), restored.chapters.map(Chapter::name))
    }

    /**
     * Use case: a book with prolog, epilog and blurb is stored and opened again, so all three parts
     * come back with their content and with their switch instead of being dropped.
     */
    @Test
    fun roundTripsPrologEpilogAndBlurb() {
        val book = TestData.book()

        val restored: Book = mapper.readValue(mapper.writeValueAsString(book))

        assertEquals(TestData.prolog(), restored.prolog)
        assertEquals(TestData.epilog(), restored.epilog)
        assertEquals(TestData.blurb(), restored.blurb)
    }

    /**
     * Use case: the user described the manuscript for the assistant, so the prompts of the book are
     * stored with it and come back unchanged when the project is opened again.
     */
    @Test
    fun roundTripsPrompts() {
        val book = TestData.book()

        val restored: Book = mapper.readValue(mapper.writeValueAsString(book))

        assertEquals(TestData.bookPrompts(), restored.prompts)
    }

    /**
     * Use case: a book file holds only the prompts, so it is read back as a book without chapters and
     * with the three switchable parts in their default shape instead of failing.
     */
    @Test
    fun readsDocumentWithPromptsOnly() {
        val book: Book = mapper.readValue("""{"prompts":{"contentPrompt":"Tell a story."}}""")

        assertEquals(Book(prompts = AIPrompt(contentPrompt = "Tell a story.")), book)
    }

    /**
     * Use case: a book written by a newer version carries additional properties, so reading it
     * ignores what is unknown instead of failing.
     */
    @Test
    fun ignoresUnknownProperties() {
        val book: Book = mapper.readValue("""{"isbn":"123"}""")

        assertEquals(Book(), book)
    }

    /**
     * Use case: an old project written before IP-36 still carries `title`, `titleAppendix` and
     * `paragraph` in its JSON, so opening it drops those fields instead of failing to parse - the
     * hard requirement IP-36 leaves for IP-37's migration to build on.
     */
    @Test
    fun ignoresFieldsRemovedByThePreviousModelVersion() {
        val json = """
            {
              "title" : "My Novel",
              "titleAppendix" : [ "A Story in Two Parts" ],
              "prompts" : { "contentPrompt" : "Tell a story." },
              "chapters" : [ { "name" : "first", "title" : "Prologue", "paragraph" : [ "Once upon a time." ] } ]
            }
        """.trimIndent()

        val book: Book = mapper.readValue(json)

        assertEquals("first", book.chapters.single().name)
        assertEquals(AIPrompt(contentPrompt = "Tell a story."), book.prompts)
    }
}

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

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.pcsoft.app.aighost.model.TestData
import org.pcsoft.app.aighost.model.project.common.AIPrompt

/**
 * Developer tests for [org.pcsoft.app.aighost.model.project.book.Book].
 */
class BookTest {

    private val mapper: ObjectMapper = ObjectMapper().registerKotlinModule()

    /**
     * Use case: the user creates a book before writing anything, so it starts without appendix lines
     * and without chapters instead of requiring one up front.
     */
    @Test
    fun defaultsToEmptyAppendixAndChapters() {
        val book = Book(title = "My Novel")

        assertEquals(emptyList<String>(), book.titleAppendix)
        assertEquals(emptyList<Chapter>(), book.chapters)
    }

    /**
     * Use case: a book is created before the user described what it is about, so it starts with
     * empty prompts instead of demanding a description up front.
     */
    @Test
    fun defaultsToEmptyPrompts() {
        assertEquals(AIPrompt(), Book(title = "My Novel").prompts)
    }

    /**
     * Use case: a project is created before the user named the manuscript, so the book carries a
     * placeholder title the user can overwrite instead of demanding one up front.
     */
    @Test
    fun defaultsToPlaceholderTitle() {
        assertEquals("My Book", Book().title)
    }

    /**
     * Use case: prolog, epilog and blurb are created on demand through the menu, so a fresh book
     * carries none of them instead of empty placeholders the user never asked for.
     */
    @Test
    fun defaultsToNoPrologEpilogAndBlurb() {
        val book = Book(title = "My Novel")

        assertNull(book.prolog)
        assertNull(book.epilog)
        assertNull(book.blurb)
    }

    /**
     * Use case: a book is written to disk, so title, appendix lines, prompts and chapters appear in
     * the JSON under the stable property names the file format promises.
     */
    @Test
    fun serialisesTitleAppendixPromptsAndChapters() {
        val book = Book(
            title = "My Novel",
            titleAppendix = listOf("A Story"),
            prompts = AIPrompt("Tell a story in two parts.", "Warm and calm."),
            chapters = listOf(Chapter("prologue", "Prologue"))
        )

        val json = mapper.writeValueAsString(book)

        assertEquals(
            """{"version":1,"title":"My Novel","titleAppendix":["A Story"],""" +
                """"prompts":{"contentPrompt":"Tell a story in two parts.","stylePrompt":"Warm and calm."},""" +
                """"prolog":null,"chapters":[{"name":"prologue","title":"Prologue","titleAppendix":[],""" +
                """"prompts":{"contentPrompt":"","stylePrompt":""},"paragraph":[]}],""" +
                """"epilog":null,"blurb":null}""",
            json
        )
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
     * come back with their content instead of being dropped.
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
     * Use case: a book file holds only the title, so it is read back as a book without chapters
     * instead of failing.
     */
    @Test
    fun readsDocumentWithTitleOnly() {
        val book: Book = mapper.readValue("""{"title":"My Novel"}""")

        assertEquals(Book(title = "My Novel"), book)
    }

    /**
     * Use case: a book written by a newer version carries additional properties, so reading it
     * ignores what is unknown instead of failing.
     */
    @Test
    fun ignoresUnknownProperties() {
        val book: Book = mapper.readValue("""{"title":"My Novel","isbn":"123"}""")

        assertEquals(Book(title = "My Novel"), book)
    }
}

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

package org.pcsoft.app.aighost.model.project

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import com.fasterxml.jackson.databind.exc.MismatchedInputException

/**
 * Developer tests for [Chapter].
 */
class ChapterTest {

    private val mapper: ObjectMapper = ObjectMapper().registerKotlinModule()

    /**
     * Use case: the user creates a chapter and only names and titles it, so the chapter starts
     * without appendix lines and without text instead of forcing content up front.
     */
    @Test
    fun defaultsToEmptyAppendixAndText() {
        val chapter = Chapter("prologue", "Prologue")

        assertEquals(emptyList<String>(), chapter.titleAppendix)
        assertEquals(emptyList<String>(), chapter.paragraph)
    }

    /**
     * Use case: the project tree lists chapters that are not written yet, so a chapter offers its
     * name beside the heading it is printed with.
     */
    @Test
    fun keepsNameAndTitleApart() {
        val chapter = Chapter("draft-01", "Prologue")

        assertEquals("draft-01", chapter.name)
        assertEquals("Prologue", chapter.title)
    }

    /**
     * Use case: a chapter is used wherever written text is rendered, so it can be handed over as a
     * [BookPart] like the prolog and the epilog.
     */
    @Test
    fun isABookPart() {
        val part: BookPart = Chapter("draft-01", "Prologue", listOf("A beginning"), listOf("Text."))

        assertEquals("Prologue", part.title)
        assertEquals(listOf("A beginning"), part.titleAppendix)
        assertEquals(listOf("Text."), part.paragraph)
    }

    /**
     * Use case: a chapter is written to disk, so name, heading, appendix lines and paragraphs appear
     * in the JSON under the stable property names the file format promises.
     */
    @Test
    fun serialisesNameTitleAppendixAndParagraphs() {
        val chapter = Chapter("prologue", "Prologue", listOf("A beginning"), listOf("Once upon a time."))

        val json = mapper.writeValueAsString(chapter)

        assertEquals(
            """{"name":"prologue","title":"Prologue","titleAppendix":["A beginning"],""" +
                """"paragraph":["Once upon a time."]}""",
            json
        )
    }

    /**
     * Use case: a stored chapter is read back, so heading and all paragraphs survive the round trip
     * unchanged and keep their order.
     */
    @Test
    fun roundTripsParagraphsInOrder() {
        val chapter = Chapter(
            "chapter-01",
            "Chapter 1",
            listOf("The first step", "and the second"),
            listOf("First paragraph.", "Second paragraph.", "Third paragraph.")
        )

        val restored: Chapter = mapper.readValue(mapper.writeValueAsString(chapter))

        assertEquals(chapter, restored)
        assertEquals(
            listOf("First paragraph.", "Second paragraph.", "Third paragraph."),
            restored.paragraph
        )
    }

    /**
     * Use case: a chapter file holds only name and title, so it is read back as an outlined chapter
     * instead of failing.
     */
    @Test
    fun readsDocumentWithNameAndTitleOnly() {
        val chapter: Chapter = mapper.readValue("""{"name":"prologue","title":"Prologue"}""")

        assertEquals(Chapter("prologue", "Prologue"), chapter)
    }

    /**
     * Use case: a chapter file misses the mandatory name, so opening it fails instead of producing a
     * chapter the project tree cannot label.
     */
    @Test
    fun rejectsDocumentWithoutName() {
        assertThrows<MismatchedInputException> { mapper.readValue<Chapter>("""{"title":"Prologue"}""") }
    }

    /**
     * Use case: a chapter written by a newer version carries additional properties, so reading it
     * ignores what is unknown instead of failing.
     */
    @Test
    fun ignoresUnknownProperties() {
        val chapter: Chapter =
            mapper.readValue("""{"name":"prologue","title":"Prologue","summary":"short"}""")

        assertEquals(Chapter("prologue", "Prologue"), chapter)
    }
}

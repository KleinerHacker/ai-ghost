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

/**
 * Developer tests for [Chapter].
 */
class ChapterTest {

    private val mapper: ObjectMapper = ObjectMapper().registerKotlinModule()

    /**
     * Use case: the user creates a chapter and only names it, so the chapter starts without appendix
     * lines and without text instead of forcing content up front.
     */
    @Test
    fun defaultsToEmptyAppendixAndText() {
        val chapter = Chapter("Prologue")

        assertEquals(emptyList<String>(), chapter.titleAppendix)
        assertEquals(emptyList<String>(), chapter.paragraph)
    }

    /**
     * Use case: a chapter is written to disk, so heading, appendix lines and paragraphs appear in the
     * JSON under the stable property names the file format promises.
     */
    @Test
    fun serialisesTitleAppendixAndParagraphs() {
        val chapter = Chapter("Prologue", listOf("A beginning"), listOf("Once upon a time."))

        val json = mapper.writeValueAsString(chapter)

        assertEquals(
            """{"title":"Prologue","titleAppendix":["A beginning"],"paragraph":["Once upon a time."]}""",
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
     * Use case: a chapter file holds only the title, so it is read back as an outlined chapter
     * instead of failing.
     */
    @Test
    fun readsDocumentWithTitleOnly() {
        val chapter: Chapter = mapper.readValue("""{"title":"Prologue"}""")

        assertEquals(Chapter("Prologue"), chapter)
    }

    /**
     * Use case: a chapter written by a newer version carries additional properties, so reading it
     * ignores what is unknown instead of failing.
     */
    @Test
    fun ignoresUnknownProperties() {
        val chapter: Chapter = mapper.readValue("""{"title":"Prologue","summary":"short"}""")

        assertEquals(Chapter("Prologue"), chapter)
    }
}

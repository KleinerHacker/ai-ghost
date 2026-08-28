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
import org.junit.jupiter.api.Test

/**
 * Developer tests for [org.pcsoft.app.aighost.model.project.book.Prolog].
 */
class PrologTest {

    private val mapper: ObjectMapper = ObjectMapper().registerKotlinModule()

    /**
     * Use case: the user creates the prolog and only titles it, so it starts without appendix lines
     * and without text instead of forcing content up front.
     */
    @Test
    fun defaultsToEmptyAppendixAndText() {
        val prolog = Prolog("Before It All")

        assertEquals(emptyList<String>(), prolog.titleAppendix)
        assertEquals(emptyList<String>(), prolog.paragraph)
    }

    /**
     * Use case: the prolog is rendered like any other written part, so it can be handed over as a
     * [org.pcsoft.app.aighost.model.project.book.BookPart] together with chapters and the epilog.
     */
    @Test
    fun isABookPart() {
        val part: BookPart = Prolog("Before It All", listOf("A word up front"), listOf("Text."))

        assertEquals("Before It All", part.title)
        assertEquals(listOf("A word up front"), part.titleAppendix)
        assertEquals(listOf("Text."), part.paragraph)
    }

    /**
     * Use case: the prolog is written to disk, so heading, appendix lines and paragraphs appear in
     * the JSON under the stable property names the file format promises.
     */
    @Test
    fun serialisesTitleAppendixAndParagraphs() {
        val prolog = Prolog("Before It All", listOf("A word up front"), listOf("Long before."))

        val json = mapper.writeValueAsString(prolog)

        assertEquals(
            """{"title":"Before It All","titleAppendix":["A word up front"],""" +
                """"paragraph":["Long before."]}""",
            json
        )
    }

    /**
     * Use case: a stored prolog is read back, so heading and all paragraphs survive the round trip
     * unchanged and keep their order.
     */
    @Test
    fun roundTripsParagraphsInOrder() {
        val prolog = Prolog(
            "Before It All",
            listOf("A word up front", "and another"),
            listOf("First paragraph.", "Second paragraph.")
        )

        val restored: Prolog = mapper.readValue(mapper.writeValueAsString(prolog))

        assertEquals(prolog, restored)
        assertEquals(listOf("First paragraph.", "Second paragraph."), restored.paragraph)
    }

    /**
     * Use case: a project file holds a prolog with its title only, so it is read back as an outlined
     * prolog instead of failing.
     */
    @Test
    fun readsDocumentWithTitleOnly() {
        val prolog: Prolog = mapper.readValue("""{"title":"Before It All"}""")

        assertEquals(Prolog("Before It All"), prolog)
    }

    /**
     * Use case: a prolog written by a newer version carries additional properties, so reading it
     * ignores what is unknown instead of failing.
     */
    @Test
    fun ignoresUnknownProperties() {
        val prolog: Prolog = mapper.readValue("""{"title":"Before It All","summary":"short"}""")

        assertEquals(Prolog("Before It All"), prolog)
    }
}

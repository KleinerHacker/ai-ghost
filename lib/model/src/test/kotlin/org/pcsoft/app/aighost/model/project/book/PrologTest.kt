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
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.pcsoft.app.aighost.model.project.common.AIPrompt

/**
 * Developer tests for [org.pcsoft.app.aighost.model.project.book.Prolog].
 */
class PrologTest {

    private val mapper: ObjectMapper = ObjectMapper().registerKotlinModule()

    /**
     * Use case: the user creates the prolog and only titles it, so it starts without appendix lines,
     * without prompts and without text instead of forcing content up front.
     */
    @Test
    fun defaultsToEmptyAppendixPromptsAndText() {
        val prolog = Prolog("Before It All")

        assertEquals(emptyList<String>(), prolog.titleAppendix)
        assertEquals(AIPrompt(), prolog.prompts)
        assertEquals(emptyList<String>(), prolog.paragraph)
    }

    /**
     * Use case: a book is created before the user decided about its prolog, so the prolog is already
     * there, without a heading and without belonging to the book yet.
     */
    @Test
    fun defaultsToEmptyTitleAndNotIncluded() {
        val prolog = Prolog()

        assertEquals("", prolog.title)
        assertFalse(prolog.included)
    }

    /**
     * Use case: the prolog is rendered like any other written part, so it can be handed over as a
     * [org.pcsoft.app.aighost.model.project.book.BookPart] together with chapters and the epilog.
     */
    @Test
    fun isABookPart() {
        val part: BookPart = Prolog(
            "Before It All",
            listOf("A word up front"),
            AIPrompt("Tell what happened before.", "Calm and slow."),
            listOf("Text.")
        )

        assertEquals("Before It All", part.title)
        assertEquals(listOf("A word up front"), part.titleAppendix)
        assertEquals(AIPrompt("Tell what happened before.", "Calm and slow."), part.prompts)
        assertEquals(listOf("Text."), part.paragraph)
    }

    /**
     * Use case: the user takes the prolog out of the book and puts it back in later on, so everything
     * written into it is still there instead of having been thrown away with the switch.
     */
    @Test
    fun keepsTextWhenSwitchedOffAndOnAgain() {
        val prolog = Prolog(
            "Before It All",
            listOf("A word up front"),
            AIPrompt("Tell what happened before.", "Calm and slow."),
            listOf("Long before.", "And even earlier."),
            included = true
        )

        prolog.included = false
        prolog.included = true

        assertEquals("Before It All", prolog.title)
        assertEquals(listOf("A word up front"), prolog.titleAppendix)
        assertEquals(AIPrompt("Tell what happened before.", "Calm and slow."), prolog.prompts)
        assertEquals(listOf("Long before.", "And even earlier."), prolog.paragraph)
    }

    /**
     * Use case: the prolog is written to disk, so heading, appendix lines, prompts, paragraphs and the
     * switch appear in the JSON under the stable property names the file format promises.
     */
    @Test
    fun serialisesTitleAppendixPromptsParagraphsAndSwitch() {
        val prolog = Prolog(
            "Before It All",
            listOf("A word up front"),
            AIPrompt("Tell what happened before.", "Calm and slow."),
            listOf("Long before.")
        )

        val json = mapper.writeValueAsString(prolog)

        assertEquals(
            """{"title":"Before It All","titleAppendix":["A word up front"],""" +
                """"prompts":{"contentPrompt":"Tell what happened before.","stylePrompt":"Calm and slow."},""" +
                """"paragraph":["Long before."],"included":false}""",
            json
        )
    }

    /**
     * Use case: a stored prolog is read back, so heading, prompts, all paragraphs and the switch
     * survive the round trip unchanged and keep their order.
     */
    @Test
    fun roundTripsParagraphsInOrder() {
        val prolog = Prolog(
            "Before It All",
            listOf("A word up front", "and another"),
            AIPrompt("Tell what happened before.", "Calm and slow."),
            listOf("First paragraph.", "Second paragraph."),
            included = true
        )

        val restored: Prolog = mapper.readValue(mapper.writeValueAsString(prolog))

        assertEquals(prolog, restored)
        assertEquals(AIPrompt("Tell what happened before.", "Calm and slow."), restored.prompts)
        assertEquals(listOf("First paragraph.", "Second paragraph."), restored.paragraph)
        assertTrue(restored.included)
    }

    /**
     * Use case: a project file holds a prolog with its title only, so it is read back as an outlined
     * prolog that does not belong to the book yet instead of failing.
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

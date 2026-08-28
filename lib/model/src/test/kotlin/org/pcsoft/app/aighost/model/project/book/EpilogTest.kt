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
import org.pcsoft.app.aighost.model.project.common.AIPrompt

/**
 * Developer tests for [org.pcsoft.app.aighost.model.project.book.Epilog].
 */
class EpilogTest {

    private val mapper: ObjectMapper = ObjectMapper().registerKotlinModule()

    /**
     * Use case: the user creates the epilog and only titles it, so it starts without appendix lines,
     * without prompts and without text instead of forcing content up front.
     */
    @Test
    fun defaultsToEmptyAppendixPromptsAndText() {
        val epilog = Epilog("After It All")

        assertEquals(emptyList<String>(), epilog.titleAppendix)
        assertEquals(AIPrompt(), epilog.prompts)
        assertEquals(emptyList<String>(), epilog.paragraph)
    }

    /**
     * Use case: the epilog is rendered like any other written part, so it can be handed over as a
     * [org.pcsoft.app.aighost.model.project.book.BookPart] together with chapters and the prolog.
     */
    @Test
    fun isABookPart() {
        val part: BookPart = Epilog(
            "After It All",
            listOf("A last word"),
            AIPrompt("Tell how it ended.", "Calm and slow."),
            listOf("Text.")
        )

        assertEquals("After It All", part.title)
        assertEquals(listOf("A last word"), part.titleAppendix)
        assertEquals(AIPrompt("Tell how it ended.", "Calm and slow."), part.prompts)
        assertEquals(listOf("Text."), part.paragraph)
    }

    /**
     * Use case: the epilog is written to disk, so heading, appendix lines, prompts and paragraphs
     * appear in the JSON under the stable property names the file format promises.
     */
    @Test
    fun serialisesTitleAppendixPromptsAndParagraphs() {
        val epilog = Epilog(
            "After It All",
            listOf("A last word"),
            AIPrompt("Tell how it ended.", "Calm and slow."),
            listOf("And that was that.")
        )

        val json = mapper.writeValueAsString(epilog)

        assertEquals(
            """{"title":"After It All","titleAppendix":["A last word"],""" +
                """"prompts":{"contentPrompt":"Tell how it ended.","stylePrompt":"Calm and slow."},""" +
                """"paragraph":["And that was that."]}""",
            json
        )
    }

    /**
     * Use case: a stored epilog is read back, so heading, prompts and all paragraphs survive the
     * round trip unchanged and keep their order.
     */
    @Test
    fun roundTripsParagraphsInOrder() {
        val epilog = Epilog(
            "After It All",
            listOf("A last word", "and another"),
            AIPrompt("Tell how it ended.", "Calm and slow."),
            listOf("First paragraph.", "Second paragraph.")
        )

        val restored: Epilog = mapper.readValue(mapper.writeValueAsString(epilog))

        assertEquals(epilog, restored)
        assertEquals(AIPrompt("Tell how it ended.", "Calm and slow."), restored.prompts)
        assertEquals(listOf("First paragraph.", "Second paragraph."), restored.paragraph)
    }

    /**
     * Use case: a project file holds an epilog with its title only, so it is read back as an
     * outlined epilog instead of failing.
     */
    @Test
    fun readsDocumentWithTitleOnly() {
        val epilog: Epilog = mapper.readValue("""{"title":"After It All"}""")

        assertEquals(Epilog("After It All"), epilog)
    }

    /**
     * Use case: an epilog written by a newer version carries additional properties, so reading it
     * ignores what is unknown instead of failing.
     */
    @Test
    fun ignoresUnknownProperties() {
        val epilog: Epilog = mapper.readValue("""{"title":"After It All","summary":"short"}""")

        assertEquals(Epilog("After It All"), epilog)
    }
}

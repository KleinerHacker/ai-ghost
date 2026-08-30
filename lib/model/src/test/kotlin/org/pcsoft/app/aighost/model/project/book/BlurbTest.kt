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
 * Developer tests for [org.pcsoft.app.aighost.model.project.book.Blurb].
 */
class BlurbTest {

    private val mapper: ObjectMapper = ObjectMapper().registerKotlinModule()

    /**
     * Use case: the user creates the blurb before writing it, so it starts without a prompt and
     * without text instead of forcing a paragraph up front.
     */
    @Test
    fun defaultsToEmptyPromptAndText() {
        val blurb = Blurb()

        assertEquals("", blurb.prompt)
        assertEquals(emptyList<String>(), blurb.paragraph)
    }

    /**
     * Use case: the blurb is written to disk, so its prompt and its paragraphs appear in the JSON
     * under the stable property names the file format promises.
     */
    @Test
    fun serialisesPromptAndParagraphs() {
        val blurb = Blurb("Advertise a tale of two chapters.", listOf("A gripping tale."))

        val json = mapper.writeValueAsString(blurb)

        assertEquals(
            """{"prompt":"Advertise a tale of two chapters.","paragraph":["A gripping tale."]}""",
            json
        )
    }

    /**
     * Use case: a stored blurb is read back, so all paragraphs survive the round trip unchanged and
     * keep their order.
     */
    @Test
    fun roundTripsParagraphsInOrder() {
        val blurb = Blurb(
            paragraph = listOf("First paragraph.", "Second paragraph.", "Third paragraph.")
        )

        val restored: Blurb = mapper.readValue(mapper.writeValueAsString(blurb))

        assertEquals(blurb, restored)
        assertEquals(
            listOf("First paragraph.", "Second paragraph.", "Third paragraph."),
            restored.paragraph
        )
    }

    /**
     * Use case: the user lets the blurb be written by the assistant, so the prompt behind it is
     * stored with the blurb and comes back unchanged when the project is opened again.
     */
    @Test
    fun roundTripsPrompt() {
        val blurb = Blurb("Advertise a tale of two chapters.", listOf("A gripping tale."))

        val restored: Blurb = mapper.readValue(mapper.writeValueAsString(blurb))

        assertEquals("Advertise a tale of two chapters.", restored.prompt)
    }

    /**
     * Use case: a project file holds an empty blurb object, so it is read back as a blurb without
     * prompt and without text instead of failing.
     */
    @Test
    fun readsEmptyDocument() {
        val blurb: Blurb = mapper.readValue("{}")

        assertEquals(Blurb(), blurb)
    }

    /**
     * Use case: a blurb of an older project carries no prompt yet, so it is read back with an empty
     * prompt instead of failing.
     */
    @Test
    fun readsDocumentWithoutPrompt() {
        val blurb: Blurb = mapper.readValue("""{"paragraph":["A tale."]}""")

        assertEquals(Blurb(paragraph = listOf("A tale.")), blurb)
    }

    /**
     * Use case: a blurb written by a newer version carries additional properties, so reading it
     * ignores what is unknown instead of failing.
     */
    @Test
    fun ignoresUnknownProperties() {
        val blurb: Blurb = mapper.readValue("""{"paragraph":["A tale."],"language":"en"}""")

        assertEquals(Blurb(paragraph = listOf("A tale.")), blurb)
    }
}

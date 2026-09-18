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
     * Use case: a book is created before the user decided about its blurb, so the blurb is already
     * there without belonging to the book yet.
     */
    @Test
    fun defaultsToNotIncluded() {
        assertFalse(Blurb().included)
    }

    /**
     * Use case: the user takes the blurb off the cover and puts it back later on, so everything
     * written into it is still there instead of having been thrown away with the switch.
     */
    @Test
    fun keepsTextWhenSwitchedOffAndOnAgain() {
        val blurb = Blurb(
            prompt = "Advertise a tale of two chapters.",
            paragraph = listOf("A gripping tale.", "You will not put it down."),
            included = true
        )

        blurb.included = false
        blurb.included = true

        assertEquals("Advertise a tale of two chapters.", blurb.prompt)
        assertEquals(listOf("A gripping tale.", "You will not put it down."), blurb.paragraph)
    }

    /**
     * Use case: the blurb is written to disk, so its prompt, its paragraphs and the switch appear in
     * the JSON under the stable property names the file format promises.
     */
    @Test
    fun serialisesPromptParagraphsAndSwitch() {
        val blurb = Blurb("Advertise a tale of two chapters.", listOf("A gripping tale."))

        val json = mapper.writeValueAsString(blurb)

        assertEquals(
            """{"prompt":"Advertise a tale of two chapters.","paragraph":["A gripping tale."],""" +
                """"included":false}""",
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
     * Use case: the user lets the blurb be written by the assistant and puts it on the cover, so the
     * prompt behind it and the switch are stored with the blurb and come back unchanged when the
     * project is opened again.
     */
    @Test
    fun roundTripsPromptAndSwitch() {
        val blurb = Blurb("Advertise a tale of two chapters.", listOf("A gripping tale."), included = true)

        val restored: Blurb = mapper.readValue(mapper.writeValueAsString(blurb))

        assertEquals("Advertise a tale of two chapters.", restored.prompt)
        assertTrue(restored.included)
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

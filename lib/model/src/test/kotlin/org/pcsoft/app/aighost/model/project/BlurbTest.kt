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
 * Developer tests for [Blurb].
 */
class BlurbTest {

    private val mapper: ObjectMapper = ObjectMapper().registerKotlinModule()

    /**
     * Use case: the user creates the blurb before writing it, so it starts without text instead of
     * forcing a paragraph up front.
     */
    @Test
    fun defaultsToEmptyText() {
        val blurb = Blurb()

        assertEquals(emptyList<String>(), blurb.paragraph)
    }

    /**
     * Use case: the blurb is written to disk, so its paragraphs appear in the JSON under the stable
     * property name the file format promises.
     */
    @Test
    fun serialisesParagraphs() {
        val blurb = Blurb(listOf("A gripping tale."))

        val json = mapper.writeValueAsString(blurb)

        assertEquals("""{"paragraph":["A gripping tale."]}""", json)
    }

    /**
     * Use case: a stored blurb is read back, so all paragraphs survive the round trip unchanged and
     * keep their order.
     */
    @Test
    fun roundTripsParagraphsInOrder() {
        val blurb = Blurb(listOf("First paragraph.", "Second paragraph.", "Third paragraph."))

        val restored: Blurb = mapper.readValue(mapper.writeValueAsString(blurb))

        assertEquals(blurb, restored)
        assertEquals(
            listOf("First paragraph.", "Second paragraph.", "Third paragraph."),
            restored.paragraph
        )
    }

    /**
     * Use case: a project file holds an empty blurb object, so it is read back as a blurb without
     * text instead of failing.
     */
    @Test
    fun readsEmptyDocument() {
        val blurb: Blurb = mapper.readValue("{}")

        assertEquals(Blurb(), blurb)
    }

    /**
     * Use case: a blurb written by a newer version carries additional properties, so reading it
     * ignores what is unknown instead of failing.
     */
    @Test
    fun ignoresUnknownProperties() {
        val blurb: Blurb = mapper.readValue("""{"paragraph":["A tale."],"language":"en"}""")

        assertEquals(Blurb(listOf("A tale.")), blurb)
    }
}

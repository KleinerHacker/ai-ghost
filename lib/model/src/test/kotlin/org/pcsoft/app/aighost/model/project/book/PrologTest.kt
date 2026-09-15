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
     * Use case: the user creates the prolog before writing anything, so it starts without prompts
     * instead of forcing content up front.
     */
    @Test
    fun defaultsToEmptyPrompts() {
        assertEquals(AIPrompt(), Prolog().prompts)
    }

    /**
     * Use case: a book is created before the user decided about its prolog, so the prolog is already
     * there, without belonging to the book yet.
     */
    @Test
    fun defaultsToNotIncluded() {
        assertFalse(Prolog().included)
    }

    /**
     * Use case: the prolog is handled like any other written part, so it can be handed over as a
     * [org.pcsoft.app.aighost.model.project.book.BookPart] together with chapters and the epilog.
     */
    @Test
    fun isABookPart() {
        val part: BookPart = Prolog(AIPrompt("Tell what happened before.", "Calm and slow."))

        assertEquals(AIPrompt("Tell what happened before.", "Calm and slow."), part.prompts)
    }

    /**
     * Use case: the user takes the prolog out of the book and puts it back in later on, so its
     * prompts are still there instead of having been thrown away with the switch.
     */
    @Test
    fun keepsPromptsWhenSwitchedOffAndOnAgain() {
        val prolog = Prolog(
            AIPrompt("Tell what happened before.", "Calm and slow."),
            included = true
        )

        prolog.included = false
        prolog.included = true

        assertEquals(AIPrompt("Tell what happened before.", "Calm and slow."), prolog.prompts)
    }

    /**
     * Use case: the prolog is written to disk, so its prompts and the switch appear in the JSON under
     * the stable property names the file format promises.
     */
    @Test
    fun serialisesPromptsAndSwitch() {
        val prolog = Prolog(AIPrompt("Tell what happened before.", "Calm and slow."))

        val json = mapper.writeValueAsString(prolog)

        assertEquals(
            """{"prompts":{"contentPrompt":"Tell what happened before.","stylePrompt":"Calm and slow."},""" +
                """"included":false}""",
            json
        )
    }

    /**
     * Use case: a stored prolog is read back, so its prompts and the switch survive the round trip
     * unchanged.
     */
    @Test
    fun roundTripsPromptsAndSwitch() {
        val prolog = Prolog(AIPrompt("Tell what happened before.", "Calm and slow."), included = true)

        val restored: Prolog = mapper.readValue(mapper.writeValueAsString(prolog))

        assertEquals(prolog, restored)
        assertEquals(AIPrompt("Tell what happened before.", "Calm and slow."), restored.prompts)
        assertTrue(restored.included)
    }

    /**
     * Use case: a project file holds a prolog with no properties at all, so it is read back as an
     * outlined prolog that does not belong to the book yet instead of failing.
     */
    @Test
    fun readsEmptyDocumentWithDefaults() {
        val prolog: Prolog = mapper.readValue("""{}""")

        assertEquals(Prolog(), prolog)
    }

    /**
     * Use case: a prolog written by a newer version carries additional properties, so reading it
     * ignores what is unknown instead of failing.
     */
    @Test
    fun ignoresUnknownProperties() {
        val prolog: Prolog = mapper.readValue("""{"summary":"short"}""")

        assertEquals(Prolog(), prolog)
    }

    /**
     * Use case: an old project written before IP-36 still carries `title`, `titleAppendix` and
     * `paragraph` in its JSON, so opening it drops those fields instead of failing to parse - the
     * hard requirement IP-36 leaves for IP-37's migration to build on.
     */
    @Test
    fun ignoresFieldsRemovedByThePreviousModelVersion() {
        val prolog: Prolog = mapper.readValue(
            """{"title":"Before It All","titleAppendix":["A word up front"],"paragraph":["Long before."]}"""
        )

        assertEquals(Prolog(), prolog)
    }
}

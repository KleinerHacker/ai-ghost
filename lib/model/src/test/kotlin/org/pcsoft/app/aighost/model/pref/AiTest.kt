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

package org.pcsoft.app.aighost.model.pref

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

/**
 * Developer tests for [Ai].
 */
class AiTest {

    private val mapper: ObjectMapper = ObjectMapper().registerKotlinModule()

    /**
     * Use case: a user who never touched the settings gets the shipped limits, so a generation is
     * bounded from the first start on without any setup.
     */
    @Test
    fun defaultsToShippedLimits() {
        val ai = Ai()

        assertEquals(5000, ai.maxStoryCharacters)
        assertEquals(1000, ai.maxStyleCharacters)
    }

    /**
     * Use case: the user raises the limits in the settings dialog, so both are in effect right away
     * without a copy of the object having to be handed around.
     */
    @Test
    fun changesEveryLimitInPlace() {
        val ai = Ai()

        ai.maxStoryCharacters = 12000
        ai.maxStyleCharacters = 2500

        assertEquals(12000, ai.maxStoryCharacters)
        assertEquals(2500, ai.maxStyleCharacters)
    }

    /**
     * Use case: a caller wants the limits of another object without changing what it was handed, so
     * the copy carries the new value while the original keeps its own.
     */
    @Test
    fun copiesWithASingleChangedLimit() {
        val ai = Ai(maxStoryCharacters = 3000, maxStyleCharacters = 800)

        val copy = ai.copy(maxStoryCharacters = 9000)

        assertEquals(9000, copy.maxStoryCharacters)
        assertEquals(800, copy.maxStyleCharacters)
        assertEquals(3000, ai.maxStoryCharacters)
    }

    /**
     * Use case: two users configured the same limits, so the objects count as equal no matter which
     * one holds the settings.
     */
    @Test
    fun comparesByItsSettings() {
        val one = Ai(maxStoryCharacters = 3000, maxStyleCharacters = 800)
        val other = Ai(maxStoryCharacters = 3000, maxStyleCharacters = 800)

        assertEquals(one, other)
        assertEquals(one.hashCode(), other.hashCode())
    }

    /**
     * Use case: the limits are written to disk as part of the preferences, so both appear in the JSON
     * under the stable property names the file format promises.
     */
    @Test
    fun serialisesEveryLimitByName() {
        val json = mapper.writeValueAsString(Ai(maxStoryCharacters = 3000, maxStyleCharacters = 800))

        assertEquals("""{"maxStoryCharacters":3000,"maxStyleCharacters":800}""", json)
    }

    /**
     * Use case: a stored preferences file is read at start up, so the configured limits are restored
     * exactly as they were written.
     */
    @Test
    fun roundTripsEveryLimit() {
        val ai = Ai(maxStoryCharacters = 7500, maxStyleCharacters = 1250)

        val restored: Ai = mapper.readValue(mapper.writeValueAsString(ai))

        assertEquals(ai, restored)
    }

    /**
     * Use case: a limit beyond the range of a 32 bit number is configured, so it survives writing and
     * reading instead of being cut off.
     */
    @Test
    fun roundTripsALimitBeyondTheIntegerRange() {
        val ai = Ai(maxStoryCharacters = 5_000_000_000L, maxStyleCharacters = 3_000_000_000L)

        val restored: Ai = mapper.readValue(mapper.writeValueAsString(ai))

        assertEquals(ai, restored)
    }

    /**
     * Use case: a preferences file written by an older version does not know the limits yet, so
     * reading it falls back to the defaults instead of failing.
     */
    @Test
    fun readsEmptyDocumentWithDefaults() {
        val ai: Ai = mapper.readValue("{}")

        assertEquals(Ai(), ai)
    }

    /**
     * Use case: a preferences file written by a newer version carries additional properties, so
     * reading it ignores what is unknown instead of failing.
     */
    @Test
    fun ignoresUnknownProperties() {
        val ai: Ai = mapper.readValue("""{"maxStoryCharacters":2000,"maxTokens":42}""")

        assertEquals(Ai(maxStoryCharacters = 2000), ai)
    }

    /**
     * Use case: the limits are part of the preferences document, so they are written and read back
     * together with the other settings.
     */
    @Test
    fun roundTripsInsidePreferences() {
        val preferences = Preferences(ai = Ai(maxStoryCharacters = 4000, maxStyleCharacters = 900))

        val restored: Preferences = mapper.readValue(mapper.writeValueAsString(preferences))

        assertEquals(preferences, restored)
    }
}

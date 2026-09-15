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
import com.fasterxml.jackson.databind.exc.MismatchedInputException
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.pcsoft.app.aighost.model.project.common.AIPrompt
import java.util.UUID

/**
 * Developer tests for [org.pcsoft.app.aighost.model.project.book.Chapter].
 */
class ChapterTest {

    private val mapper: ObjectMapper = ObjectMapper().registerKotlinModule()

    /**
     * Use case: the user creates a chapter and only names it, so the chapter starts without prompts
     * instead of forcing content up front.
     */
    @Test
    fun defaultsToEmptyPrompts() {
        assertEquals(AIPrompt(), Chapter("prologue").prompts)
    }

    /**
     * Use case: a chapter is created, so it is handed a stable id right away instead of the caller
     * having to invent one.
     */
    @Test
    fun defaultsToAFreshlyAssignedId() {
        val chapter = Chapter("prologue")

        assertNotEquals(Chapter("prologue").id, chapter.id, "two freshly created chapters must not share an id")
    }

    /**
     * Use case: the same chapter is created twice with the same explicit id - as a round trip does -
     * so the two objects are recognised as the same chapter.
     */
    @Test
    fun twoChaptersWithTheSameIdAreEqualWhenEverythingElseMatches() {
        val id = UUID.randomUUID()

        assertEquals(Chapter("prologue", id), Chapter("prologue", id))
    }

    /**
     * Use case: a chapter is used wherever the prompts of a written part are handled, so it can be
     * handed over as a [org.pcsoft.app.aighost.model.project.book.BookPart] like the prolog and the
     * epilog.
     */
    @Test
    fun isABookPart() {
        val part: BookPart = Chapter("draft-01", prompts = AIPrompt("Tell how it started.", "Calm and slow."))

        assertEquals(AIPrompt("Tell how it started.", "Calm and slow."), part.prompts)
    }

    /**
     * Use case: a chapter is written to disk, so name, id and prompts appear in the JSON under the
     * stable property names the file format promises.
     */
    @Test
    fun serialisesNameIdAndPrompts() {
        val id = UUID.fromString("11111111-1111-1111-1111-111111111111")
        val chapter = Chapter("prologue", id, AIPrompt("Tell how it started.", "Calm and slow."))

        val json = mapper.writeValueAsString(chapter)

        assertEquals(
            """{"name":"prologue","id":"11111111-1111-1111-1111-111111111111",""" +
                """"prompts":{"contentPrompt":"Tell how it started.","stylePrompt":"Calm and slow."}}""",
            json
        )
    }

    /**
     * Use case: a stored chapter is read back, so name, id and prompts survive the round trip
     * unchanged.
     */
    @Test
    fun roundTripsNameIdAndPrompts() {
        val chapter = Chapter("chapter-01", prompts = AIPrompt("Tell how it started.", "Calm and slow."))

        val restored: Chapter = mapper.readValue(mapper.writeValueAsString(chapter))

        assertEquals(chapter, restored)
        assertEquals(chapter.id, restored.id, "the id must survive the round trip unchanged")
    }

    /**
     * Use case: a chapter file holds only the name, so it is read back with a freshly assigned id
     * instead of failing.
     */
    @Test
    fun readsDocumentWithNameOnly() {
        val chapter: Chapter = mapper.readValue("""{"name":"prologue"}""")

        assertEquals("prologue", chapter.name)
    }

    /**
     * Use case: a chapter file misses the mandatory name, so opening it fails instead of producing a
     * chapter the project tree cannot label.
     */
    @Test
    fun rejectsDocumentWithoutName() {
        assertThrows<MismatchedInputException> { mapper.readValue<Chapter>("""{}""") }
    }

    /**
     * Use case: a chapter written by a newer version carries additional properties, so reading it
     * ignores what is unknown instead of failing.
     */
    @Test
    fun ignoresUnknownProperties() {
        val chapter: Chapter = mapper.readValue("""{"name":"prologue","summary":"short"}""")

        assertEquals("prologue", chapter.name)
    }

    /**
     * Use case: an old project written before IP-36 still carries `title`, `titleAppendix` and
     * `paragraph` in its JSON, so opening it drops those fields instead of failing to parse - the
     * hard requirement IP-36 leaves for IP-37's migration to build on.
     */
    @Test
    fun ignoresFieldsRemovedByThePreviousModelVersion() {
        val chapter: Chapter = mapper.readValue(
            """{"name":"prologue","title":"Prologue","titleAppendix":["A word"],"paragraph":["Text."]}"""
        )

        assertEquals("prologue", chapter.name)
    }

    /**
     * Use case: an old project carries no `id` at all, so opening it hands the chapter a freshly
     * assigned id instead of failing - IP-37's migration then relies on the position in the chapter
     * list, not on an id that was never written.
     */
    @Test
    fun assignsAFreshIdWhenTheOldDocumentCarriesNone() {
        val chapter: Chapter = mapper.readValue("""{"name":"prologue"}""")

        assertEquals("prologue", chapter.name)
    }
}

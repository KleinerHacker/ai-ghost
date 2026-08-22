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
import com.fasterxml.jackson.databind.exc.MismatchedInputException
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.pcsoft.app.aighost.model.common.Alignment
import org.pcsoft.app.aighost.model.TestData

/**
 * Developer tests for [Project] and [Project.Settings].
 */
class ProjectTest {

    private val mapper: ObjectMapper = ObjectMapper().registerKotlinModule()

    /**
     * Use case: a project is saved and opened again, so the publishing data, the settings and the
     * whole manuscript come back exactly as they were written.
     */
    @Test
    fun roundTripsCompleteProject() {
        val project = TestData.project()

        val restored: Project = mapper.readValue(mapper.writeValueAsString(project))

        assertEquals(project, restored)
    }

    /**
     * Use case: the project is written to disk, so the publishing data appears in the JSON under the
     * stable property names the file format promises.
     */
    @Test
    fun serialisesPublishingData() {
        val json = mapper.writeValueAsString(TestData.project())

        assertEquals(true, json.contains(""""name":"My Novel""""))
        assertEquals(true, json.contains(""""author":"Jane Doe""""))
        assertEquals(true, json.contains(""""copyright":"(c) 2026 Jane Doe""""))
    }

    /**
     * Use case: every text of the manuscript is styled separately, so each of the six styles keeps
     * its own font and alignment across the round trip instead of collapsing into one.
     */
    @Test
    fun roundTripsEverySettingsStyleSeparately() {
        val restored: Project = mapper.readValue(mapper.writeValueAsString(TestData.project()))
        val settings = restored.settings

        assertEquals(8, settings.copyrightFont.font.size)
        assertEquals(Alignment.CENTER, settings.titleFont.alignment)
        assertEquals(18, settings.titleAppendixFont.font.size)
        assertEquals("Sans", settings.chapterFont.font.name)
        assertEquals(14, settings.chapterAppendixFont.font.size)
        assertEquals(Alignment.BLOCK, settings.textFont.alignment)
    }

    /**
     * Use case: the page options are stored as flags, so each one comes back with the value the user
     * selected instead of a shared default.
     */
    @Test
    fun roundTripsPageFlags() {
        val restored: Project = mapper.readValue(mapper.writeValueAsString(TestData.project()))

        assertEquals(true, restored.settings.copyrightPage)
        assertEquals(true, restored.settings.startWithEmptyPage)
        assertEquals(false, restored.settings.endWithEmptyPage)
    }

    /**
     * Use case: a project written by a newer version carries additional properties, so reading it
     * ignores what is unknown instead of failing.
     */
    @Test
    fun ignoresUnknownProperties() {
        val json = mapper.writeValueAsString(TestData.project())
            .replaceFirst("{", """{"language":"en",""")

        val project: Project = mapper.readValue(json)

        assertEquals(TestData.project(), project)
    }

    /**
     * Use case: a project file misses the mandatory manuscript, so opening it fails instead of
     * creating a project without a book.
     */
    @Test
    fun rejectsProjectWithoutBook() {
        assertThrows<MismatchedInputException> {
            mapper.readValue<Project>("""{"name":"My Novel","author":"Jane Doe","copyright":""}""")
        }
    }
}

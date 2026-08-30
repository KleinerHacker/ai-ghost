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

package org.pcsoft.app.aighost.model.project.design

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.pcsoft.app.aighost.model.TestData

/**
 * Developer tests for [PageFormat].
 */
class PageFormatTest {

    private val mapper: ObjectMapper = ObjectMapper().registerKotlinModule()

    /**
     * Use case: a fresh project is written on the sheet the application ships with, so the page format
     * answers with the A5 geometry and the margins of the default layout before the user touched them.
     */
    @Test
    fun defaultsToAnA5Page() {
        val format = PageFormat()

        assertEquals(A5_WIDTH, format.width)
        assertEquals(A5_HEIGHT, format.height)
        assertEquals(20.0, format.innerMargin)
        assertEquals(15.0, format.outerMargin)
        assertEquals(15.0, format.topMargin)
        assertEquals(20.0, format.bottomMargin)
    }

    /**
     * Use case: the user set every margin of the page separately, so each measure comes back with the
     * value it was given instead of collapsing into a shared one.
     */
    @Test
    fun roundTripsEveryMeasureSeparately() {
        val restored: PageFormat = mapper.readValue(mapper.writeValueAsString(TestData.pageFormat()))

        assertEquals(400.0, restored.width)
        assertEquals(600.0, restored.height)
        assertEquals(25.0, restored.innerMargin)
        assertEquals(18.0, restored.outerMargin)
        assertEquals(12.0, restored.topMargin)
        assertEquals(22.0, restored.bottomMargin)
        assertEquals(TestData.pageFormat(), restored)
    }

    /**
     * Use case: a stored page format carries one measure only, so the remaining ones are filled with
     * their defaults instead of the format being rejected.
     */
    @Test
    fun readsPartialDocumentWithDefaults() {
        val format: PageFormat = mapper.readValue("""{"innerMargin":30.0}""")

        assertEquals(30.0, format.innerMargin)
        assertEquals(A5_WIDTH, format.width)
        assertEquals(20.0, format.bottomMargin)
    }

    /**
     * Use case: a page format written by a newer version carries additional properties, so reading it
     * ignores what is unknown instead of failing.
     */
    @Test
    fun ignoresUnknownProperties() {
        val format: PageFormat = mapper.readValue("""{"gutter":10.0}""")

        assertEquals(PageFormat(), format)
    }
}

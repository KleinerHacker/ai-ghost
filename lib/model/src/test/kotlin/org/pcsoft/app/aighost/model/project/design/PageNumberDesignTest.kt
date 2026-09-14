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
 * Developer tests for [PageNumberDesign].
 */
class PageNumberDesignTest {

    private val mapper: ObjectMapper = ObjectMapper().registerKotlinModule()

    /**
     * Use case: a fresh project is rendered before the user turned page numbering on, so no number is
     * printed and the counter would start at the first sheet.
     */
    @Test
    fun defaultsToNoPageNumber() {
        val design = PageNumberDesign()

        assertEquals(PageNumberPosition.OFF, design.position)
        assertEquals(1, design.startNumber)
        assertEquals(PageNumberCountingMode.CONTINUOUS, design.countingMode)
    }

    /**
     * Use case: the user picks a position, a start value and a counting mode for the page numbers, so
     * every one of them survives the round trip instead of falling back to the shipped default.
     */
    @Test
    fun roundTripsEveryField() {
        val restored: PageNumberDesign = mapper.readValue(mapper.writeValueAsString(TestData.pageNumberDesign()))

        assertEquals(PageNumberPosition.BOTTOM_OUTER, restored.position)
        assertEquals(3, restored.startNumber)
        assertEquals(PageNumberCountingMode.SKIP_EXCLUDED, restored.countingMode)
        assertEquals(TestData.pageNumberDesign(), restored)
    }

    /**
     * Use case: a stored page numbering carries one field only, so the remaining ones are filled with
     * their defaults instead of the part being rejected.
     */
    @Test
    fun readsPartialDocumentWithDefaults() {
        val design: PageNumberDesign = mapper.readValue("""{"startNumber":5}""")

        assertEquals(5, design.startNumber)
        assertEquals(PageNumberPosition.OFF, design.position)
        assertEquals(PageNumberCountingMode.CONTINUOUS, design.countingMode)
    }

    /**
     * Use case: a page numbering written by a newer version carries additional properties, so reading
     * it ignores what is unknown instead of failing.
     */
    @Test
    fun ignoresUnknownProperties() {
        val design: PageNumberDesign = mapper.readValue("""{"textStyle":"bold"}""")

        assertEquals(PageNumberDesign(), design)
    }
}

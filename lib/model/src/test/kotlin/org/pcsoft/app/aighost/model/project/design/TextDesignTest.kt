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
import org.pcsoft.app.aighost.model.common.Alignment
import org.pcsoft.app.aighost.model.common.StyleData

/**
 * Developer tests for [TextDesign].
 */
class TextDesignTest {

    private val mapper: ObjectMapper = ObjectMapper().registerKotlinModule()

    /**
     * Use case: a fresh project is rendered before the user touched the design, so the body text is
     * drawn with the style the application ships with instead of nothing at all.
     */
    @Test
    fun defaultsToThePlainStyle() {
        assertEquals(StyleData(), TextDesign().style)
    }

    /**
     * Use case: the user styled the body text and stores the project, so the font and the alignment
     * come back exactly as they were chosen.
     */
    @Test
    fun roundTripsTheStyle() {
        val design = TestData.textDesign()

        val restored: TextDesign = mapper.readValue(mapper.writeValueAsString(design))

        assertEquals(design, restored)
        assertEquals(11, restored.style.font.size)
        assertEquals(Alignment.BLOCK, restored.style.alignment)
    }

    /**
     * Use case: a document holds an empty design object, so it is read back with the default style
     * instead of failing.
     */
    @Test
    fun readsEmptyDocument() {
        val design: TextDesign = mapper.readValue("{}")

        assertEquals(TextDesign(), design)
    }

    /**
     * Use case: a design written by a newer version carries additional properties, so reading it
     * ignores what is unknown instead of failing.
     */
    @Test
    fun ignoresUnknownProperties() {
        val design: TextDesign = mapper.readValue("""{"outline":true}""")

        assertEquals(TextDesign(), design)
    }
}

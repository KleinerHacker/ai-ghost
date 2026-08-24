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
import org.pcsoft.app.aighost.model.common.StyleData

/**
 * Developer tests for [CopyrightDesign].
 */
class CopyrightDesignTest {

    private val mapper: ObjectMapper = ObjectMapper().registerKotlinModule()

    /**
     * Use case: a fresh project is rendered before the user touched the design, so the copyright page
     * is left out and its style is the one the application ships with.
     */
    @Test
    fun defaultsToAHiddenCopyrightPage() {
        val design = CopyrightDesign()

        assertEquals(StyleData(), design.style)
        assertEquals(false, design.show)
    }

    /**
     * Use case: the user turns the copyright page on and styles it, so both the flag and the style
     * come back exactly as they were chosen.
     */
    @Test
    fun roundTripsStyleAndFlag() {
        val design = TestData.copyrightDesign()

        val restored: CopyrightDesign = mapper.readValue(mapper.writeValueAsString(design))

        assertEquals(design, restored)
        assertEquals(8, restored.style.font.size)
        assertEquals(true, restored.show)
    }

    /**
     * Use case: a document holds the flag only, so the style is filled with its default instead of
     * the design being rejected.
     */
    @Test
    fun readsPartialDocumentWithDefaults() {
        val design: CopyrightDesign = mapper.readValue("""{"show":true}""")

        assertEquals(CopyrightDesign(show = true), design)
    }

    /**
     * Use case: a design written by a newer version carries additional properties, so reading it
     * ignores what is unknown instead of failing.
     */
    @Test
    fun ignoresUnknownProperties() {
        val design: CopyrightDesign = mapper.readValue("""{"outline":true}""")

        assertEquals(CopyrightDesign(), design)
    }
}

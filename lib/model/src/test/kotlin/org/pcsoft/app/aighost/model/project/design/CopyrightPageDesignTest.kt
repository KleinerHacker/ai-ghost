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
 * Developer tests for [CopyrightPageDesign].
 */
class CopyrightPageDesignTest {

    private val mapper: ObjectMapper = ObjectMapper().registerKotlinModule()

    /**
     * Use case: a fresh project is rendered before the user touched the design, so the copyright page
     * is drawn with the style the application ships with and the author line is shown.
     */
    @Test
    fun defaultsToPlainStylesAndShownAuthor() {
        val design = CopyrightPageDesign()

        assertEquals(StyleData(), design.copyrightStyle)
        assertEquals(StyleData(), design.copyrightAppendixStyle)
        assertEquals(StyleData(), design.authorStyle)
        assertEquals(true, design.showAuthor)
    }

    /**
     * Use case: the user styles the notice, its further lines and the author name differently, so all
     * three keep their own font across the round trip instead of collapsing into one.
     */
    @Test
    fun roundTripsEveryStyleSeparately() {
        val design = TestData.copyrightPageDesign()

        val restored: CopyrightPageDesign = mapper.readValue(mapper.writeValueAsString(design))

        assertEquals(design, restored)
        assertEquals(8, restored.copyrightStyle.font.size)
        assertEquals(7, restored.copyrightAppendixStyle.font.size)
        assertEquals(9, restored.authorStyle.font.size)
        assertEquals(1.0, restored.copyrightStyle.textLineSpacing)
    }

    /**
     * Use case: the user hides the author name on the copyright page, so the switch survives the
     * round trip instead of falling back to the default.
     */
    @Test
    fun roundTripsTheAuthorSwitch() {
        val design = TestData.copyrightPageDesign().copy(showAuthor = false)

        val restored: CopyrightPageDesign = mapper.readValue(mapper.writeValueAsString(design))

        assertEquals(false, restored.showAuthor)
    }

    /**
     * Use case: a document holds the notice style only, so the remaining styles are filled with their
     * defaults instead of the design being rejected.
     */
    @Test
    fun readsPartialDocumentWithDefaults() {
        val design: CopyrightPageDesign = mapper.readValue("""{"copyrightStyle":{"alignment":"CENTER"}}""")

        assertEquals(StyleData(), design.copyrightAppendixStyle)
        assertEquals(StyleData(), design.authorStyle)
        assertEquals(true, design.showAuthor)
    }

    /**
     * Use case: a design written by a newer version carries additional properties, so reading it
     * ignores what is unknown instead of failing.
     */
    @Test
    fun ignoresUnknownProperties() {
        val design: CopyrightPageDesign = mapper.readValue("""{"border":true}""")

        assertEquals(CopyrightPageDesign(), design)
    }
}

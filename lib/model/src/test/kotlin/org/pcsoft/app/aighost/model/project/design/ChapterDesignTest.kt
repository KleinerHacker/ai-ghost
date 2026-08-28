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
 * Developer tests for [ChapterDesign].
 */
class ChapterDesignTest {

    private val mapper: ObjectMapper = ObjectMapper().registerKotlinModule()

    /**
     * Use case: a fresh project is rendered before the user touched the design, so a chapter heading
     * and its appendix are drawn with the style the application ships with.
     */
    @Test
    fun defaultsToPlainStyles() {
        val design = ChapterDesign()

        assertEquals(StyleData(), design.titleStyle)
        assertEquals(StyleData(), design.titleAppendixStyle)
    }

    /**
     * Use case: the user styles a chapter heading and its appendix differently, so both keep their
     * own font across the round trip instead of collapsing into one.
     */
    @Test
    fun roundTripsBothStylesSeparately() {
        val design = TestData.chapterDesign()

        val restored: ChapterDesign = mapper.readValue(mapper.writeValueAsString(design))

        assertEquals(design, restored)
        assertEquals(20, restored.titleStyle.font.size)
        assertEquals(14, restored.titleAppendixStyle.font.size)
    }

    /**
     * Use case: a document holds the heading style only, so the appendix style is filled with its
     * default instead of the design being rejected.
     */
    @Test
    fun readsPartialDocumentWithDefaults() {
        val design: ChapterDesign = mapper.readValue("""{"titleStyle":{"alignment":"CENTER"}}""")

        assertEquals(StyleData(), design.titleAppendixStyle)
    }

    /**
     * Use case: a design written by a newer version carries additional properties, so reading it
     * ignores what is unknown instead of failing.
     */
    @Test
    fun ignoresUnknownProperties() {
        val design: ChapterDesign = mapper.readValue("""{"numbering":"roman"}""")

        assertEquals(ChapterDesign(), design)
    }
}

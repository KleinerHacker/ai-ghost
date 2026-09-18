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
 * Developer tests for [ChapterPageDesign].
 */
class ChapterPageDesignTest {

    private val mapper: ObjectMapper = ObjectMapper().registerKotlinModule()

    /**
     * Use case: a fresh project is rendered before the user touched the design, so a chapter heading,
     * its appendix and its body text are drawn with the style the application ships with and the
     * heading shares the page with the text.
     */
    @Test
    fun defaultsToPlainStylesAndSharedPage() {
        val design = ChapterPageDesign()

        assertEquals(StyleData(), design.titleStyle)
        assertEquals(StyleData(), design.titleAppendixStyle)
        assertEquals(StyleData(), design.textStyle)
        assertEquals(false, design.titleOnSeparatePage)
    }

    /**
     * Use case: the user styles the heading, its appendix and the body text differently, so all three
     * keep their own font across the round trip instead of collapsing into one.
     */
    @Test
    fun roundTripsEveryStyleSeparately() {
        val design = TestData.chapterPageDesign()

        val restored: ChapterPageDesign = mapper.readValue(mapper.writeValueAsString(design))

        assertEquals(design, restored)
        assertEquals(20, restored.titleStyle.font.size)
        assertEquals(14, restored.titleAppendixStyle.font.size)
        assertEquals(Alignment.BLOCK, restored.textStyle.alignment)
        assertEquals(1.4, restored.textStyle.textLineSpacing)
    }

    /**
     * Use case: the user puts the chapter heading on a page of its own, so the switch survives the
     * round trip instead of falling back to the default.
     */
    @Test
    fun roundTripsTheSeparatePageSwitch() {
        val design = TestData.chapterPageDesign()

        val restored: ChapterPageDesign = mapper.readValue(mapper.writeValueAsString(design))

        assertEquals(true, restored.titleOnSeparatePage)
    }

    /**
     * Use case: a document holds the heading style only, so the remaining styles are filled with
     * their defaults instead of the design being rejected.
     */
    @Test
    fun readsPartialDocumentWithDefaults() {
        val design: ChapterPageDesign = mapper.readValue("""{"titleStyle":{"alignment":"CENTER"}}""")

        assertEquals(StyleData(), design.titleAppendixStyle)
        assertEquals(StyleData(), design.textStyle)
        assertEquals(false, design.titleOnSeparatePage)
    }

    /**
     * Use case: a design written by a newer version carries additional properties, so reading it
     * ignores what is unknown instead of failing.
     */
    @Test
    fun ignoresUnknownProperties() {
        val design: ChapterPageDesign = mapper.readValue("""{"numbering":"roman"}""")

        assertEquals(ChapterPageDesign(), design)
    }
}

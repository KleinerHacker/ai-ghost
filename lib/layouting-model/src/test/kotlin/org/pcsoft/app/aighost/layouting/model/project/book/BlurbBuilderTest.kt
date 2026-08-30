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

package org.pcsoft.app.aighost.layouting.model.project.book

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.pcsoft.app.aighost.layouting.TextAlignment
import org.pcsoft.app.aighost.layouting.model.common.BlockSpacing
import org.pcsoft.app.aighost.model.common.Alignment
import org.pcsoft.app.aighost.model.common.FontData
import org.pcsoft.app.aighost.model.common.StyleData
import org.pcsoft.app.aighost.model.project.book.Blurb
import org.pcsoft.app.aighost.model.project.design.Design
import org.pcsoft.app.aighost.model.project.design.TextDesign

/**
 * Developer tests for the blocks of the blurb, [BlurbBuilder].
 */
class BlurbBuilderTest {

    private val design = Design(
        textDesign = TextDesign(
            style = StyleData(font = FontData("Baskerville", 11), alignment = Alignment.BLOCK)
        ),
        textLineSpacing = 1.6
    )

    /**
     * Use case: the blurb is built into its paragraphs, in their order and with no heading in front of
     * them, since the blurb carries none.
     */
    @Test
    fun theBlurbIsItsParagraphsAndNothingElse() {
        val blurb = Blurb(paragraph = listOf("A harbour town keeps its secrets.", "Until one summer."))

        val blocks = BlurbBuilder.build(blurb, design)

        assertEquals(
            listOf("A harbour town keeps its secrets.", "Until one summer."),
            blocks.map { it.text }
        )
    }

    /**
     * Use case: the blurb is set the way the body of the book is, so it comes out in the text design
     * and with the line spacing of the text.
     */
    @Test
    fun theBlurbIsSetInTheTextDesign() {
        val blocks = BlurbBuilder.build(Blurb(paragraph = listOf("Until one summer.")), design)

        val style = blocks.single().style
        assertEquals("Baskerville", style.family)
        assertEquals(11.0, style.size)
        assertEquals(TextAlignment.JUSTIFY, style.alignment)
        assertEquals(1.6, style.lineSpacing)
        assertEquals(BlockSpacing.AFTER_PARAGRAPH, style.spaceAfter)
    }

    /**
     * Use case: an empty paragraph inside the blurb is kept, exactly as it is in a chapter - it is
     * part of what the user wrote.
     */
    @Test
    fun anEmptyParagraphIsKept() {
        val blurb = Blurb(paragraph = listOf("First.", "", "Second."))

        assertEquals(listOf("First.", "", "Second."), BlurbBuilder.build(blurb, design).map { it.text })
    }

    /**
     * Use case: the blurb belongs to every book but was not written yet, so it gives no block at all.
     */
    @Test
    fun anEmptyBlurbGivesNoBlock() {
        assertTrue(BlurbBuilder.build(Blurb(), design).isEmpty())
    }
}

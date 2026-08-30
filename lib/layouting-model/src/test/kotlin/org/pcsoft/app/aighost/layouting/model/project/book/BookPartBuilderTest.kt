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
import org.pcsoft.app.aighost.model.project.book.Chapter
import org.pcsoft.app.aighost.model.project.book.Epilog
import org.pcsoft.app.aighost.model.project.book.Prolog
import org.pcsoft.app.aighost.model.project.design.ChapterDesign
import org.pcsoft.app.aighost.model.project.design.Design
import org.pcsoft.app.aighost.model.project.design.TextDesign

/**
 * Developer tests for the blocks of a written part, [BookPartBuilder].
 */
class BookPartBuilderTest {

    private val design = Design(
        chapterDesign = ChapterDesign(
            titleStyle = StyleData(font = FontData("Garamond", 20, bold = true), alignment = Alignment.CENTER),
            titleAppendixStyle = StyleData(font = FontData("Garamond", 14, italic = true), alignment = Alignment.CENTER)
        ),
        textDesign = TextDesign(
            style = StyleData(font = FontData("Baskerville", 11), alignment = Alignment.BLOCK)
        ),
        chapterLineSpacing = 1.3,
        textLineSpacing = 1.6
    )

    private fun chapter() = Chapter(
        name = "First",
        title = "The Arrival",
        titleAppendix = listOf("In which the ship comes in"),
        paragraph = listOf("The harbour was quiet.", "Nobody was waiting.")
    )

    /**
     * Use case: a chapter is built into its heading, its further heading line and its paragraphs, in
     * exactly that order.
     */
    @Test
    fun aPartIsHeadingAppendixAndParagraphs() {
        val blocks = BookPartBuilder.build(chapter(), design)

        assertEquals(
            listOf(
                "The Arrival",
                "In which the ship comes in",
                "The harbour was quiet.",
                "Nobody was waiting."
            ),
            blocks.map { it.text }
        )
    }

    /**
     * Use case: the heading is styled by the chapter design and the body by the text design, so a
     * heading and a paragraph never end up looking alike by accident.
     */
    @Test
    fun theHeadingAndTheBodyComeFromDifferentDesigns() {
        val blocks = BookPartBuilder.build(chapter(), design)

        assertEquals("Garamond", blocks[0].style.family)
        assertEquals(20.0, blocks[0].style.size)
        assertTrue(blocks[0].style.bold)
        assertEquals(TextAlignment.CENTER, blocks[0].style.alignment)
        assertEquals(1.3, blocks[0].style.lineSpacing)

        assertEquals("Garamond", blocks[1].style.family)
        assertEquals(14.0, blocks[1].style.size)
        assertTrue(blocks[1].style.italic)

        assertTrue(blocks.drop(2).all { it.style.family == "Baskerville" })
        assertTrue(blocks.drop(2).all { it.style.size == 11.0 })
        assertTrue(blocks.drop(2).all { it.style.alignment == TextAlignment.JUSTIFY })
        assertTrue(blocks.drop(2).all { it.style.lineSpacing == 1.6 })
    }

    /**
     * Use case: the heading stands away from what is above and below it, and every paragraph keeps a
     * gap to the next one.
     */
    @Test
    fun theHeadingAndTheParagraphsCarryTheirGaps() {
        val blocks = BookPartBuilder.build(chapter(), design)

        assertEquals(BlockSpacing.BEFORE_PART_TITLE, blocks[0].style.spaceBefore)
        assertEquals(0.0, blocks[0].style.spaceAfter)
        assertEquals(BlockSpacing.AFTER_PART_TITLE, blocks[1].style.spaceAfter)
        assertTrue(blocks.drop(2).all { it.style.spaceAfter == BlockSpacing.AFTER_PARAGRAPH })
    }

    /**
     * Use case: a part without a further heading line is built as well; the gap below the heading then
     * stands on the heading itself.
     */
    @Test
    fun withoutAFurtherHeadingLineTheHeadingCarriesTheGapBelow() {
        val blocks = BookPartBuilder.build(chapter().copy(titleAppendix = emptyList()), design)

        assertEquals(BlockSpacing.BEFORE_PART_TITLE, blocks[0].style.spaceBefore)
        assertEquals(BlockSpacing.AFTER_PART_TITLE, blocks[0].style.spaceAfter)
    }

    /**
     * Use case: a part whose heading was not written yet still keeps its distance from what stands
     * above it, so the further heading line takes that gap over.
     */
    @Test
    fun withoutAHeadingTheFurtherLineTakesTheGapAbove() {
        val blocks = BookPartBuilder.build(chapter().copy(title = ""), design)

        assertEquals("In which the ship comes in", blocks[0].text)
        assertEquals(BlockSpacing.BEFORE_PART_TITLE, blocks[0].style.spaceBefore)
    }

    /**
     * Use case: the user pressed return twice and left a paragraph empty; unlike an empty heading it
     * is kept, because it is part of what was written.
     */
    @Test
    fun anEmptyParagraphIsKeptWhileAnEmptyHeadingIsNot() {
        val part = chapter().copy(title = "  ", paragraph = listOf("First.", "", "Second."))

        val blocks = BookPartBuilder.build(part, design)

        assertEquals(listOf("In which the ship comes in", "First.", "", "Second."), blocks.map { it.text })
    }

    /**
     * Use case: a prolog and an epilog carry the same shape as a chapter, so all three are built the
     * very same way and never need a builder of their own.
     */
    @Test
    fun aPrologAndAnEpilogAreBuiltLikeAChapter() {
        val prolog = Prolog(title = "Before", paragraph = listOf("It began earlier."))
        val epilog = Epilog(title = "After", paragraph = listOf("It ended later."))

        val fromProlog = BookPartBuilder.build(prolog, design)
        val fromEpilog = BookPartBuilder.build(epilog, design)

        assertEquals(listOf("Before", "It began earlier."), fromProlog.map { it.text })
        assertEquals(listOf("After", "It ended later."), fromEpilog.map { it.text })
        assertEquals(fromProlog.map { it.style }, fromEpilog.map { it.style })
    }

    /**
     * Use case: a part that was only outlined carries neither heading nor text and gives no block at
     * all.
     */
    @Test
    fun anEmptyPartGivesNoBlock() {
        assertTrue(BookPartBuilder.build(Prolog(), design).isEmpty())
    }
}

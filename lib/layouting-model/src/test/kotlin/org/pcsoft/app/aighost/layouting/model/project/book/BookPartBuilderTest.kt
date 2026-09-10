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
import org.pcsoft.app.aighost.model.common.Alignment
import org.pcsoft.app.aighost.model.common.FontData
import org.pcsoft.app.aighost.model.common.StyleData
import org.pcsoft.app.aighost.model.project.book.Chapter
import org.pcsoft.app.aighost.model.project.book.Epilog
import org.pcsoft.app.aighost.model.project.book.Prolog
import org.pcsoft.app.aighost.model.project.design.ChapterPageDesign
import org.pcsoft.framework.simplay.engine.model.FontStyle
import org.pcsoft.framework.simplay.engine.model.FontWeight
import org.pcsoft.framework.simplay.engine.model.TextAlignment

/**
 * Developer tests for the blocks of a written part, [BookPartBuilder].
 */
class BookPartBuilderTest {

    private val pageDesign = ChapterPageDesign(
        titleStyle = StyleData(
            font = FontData("Garamond", 20, bold = true),
            textLineSpacing = 1.3,
            alignment = Alignment.CENTER
        ),
        titleAppendixStyle = StyleData(
            font = FontData("Garamond", 14, italic = true),
            textLineSpacing = 1.3,
            alignment = Alignment.CENTER
        ),
        textStyle = StyleData(
            font = FontData("Baskerville", 11),
            textLineSpacing = 1.6,
            alignment = Alignment.BLOCK
        )
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
        val blocks = BookPartBuilder.build(chapter(), pageDesign)

        assertEquals(
            listOf(
                "The Arrival",
                "In which the ship comes in",
                "The harbour was quiet.",
                "Nobody was waiting."
            ),
            blocks.map { it.toString() }
        )
    }

    /**
     * Use case: the heading is styled by the heading style and the body by the text style, so a
     * heading and a paragraph never end up looking alike by accident.
     */
    @Test
    fun theHeadingAndTheBodyComeFromDifferentStyles() {
        val blocks = BookPartBuilder.build(chapter(), pageDesign)

        assertEquals("Garamond", blocks[0].style.font.family)
        assertEquals(20.0, blocks[0].style.font.size)
        assertEquals(FontWeight.BOLD, blocks[0].style.font.weight)
        assertEquals(TextAlignment.CENTER, blocks[0].style.alignment)
        assertEquals(1.3, blocks[0].style.lineSpacing.factor)

        assertEquals("Garamond", blocks[1].style.font.family)
        assertEquals(14.0, blocks[1].style.font.size)
        assertEquals(FontStyle.ITALIC, blocks[1].style.font.style)

        assertTrue(blocks.drop(2).all { it.style.font.family == "Baskerville" })
        assertTrue(blocks.drop(2).all { it.style.font.size == 11.0 })
        assertTrue(blocks.drop(2).all { it.style.alignment == TextAlignment.JUSTIFY })
        assertTrue(blocks.drop(2).all { it.style.lineSpacing.factor == 1.6 })
    }

    /**
     * Use case: a part without a further heading line is built as well - it is then just its heading
     * and its paragraphs.
     */
    @Test
    fun withoutAFurtherHeadingLineThePartIsHeadingAndParagraphs() {
        val blocks = BookPartBuilder.build(chapter().copy(titleAppendix = emptyList()), pageDesign)

        assertEquals(
            listOf("The Arrival", "The harbour was quiet.", "Nobody was waiting."),
            blocks.map { it.toString() }
        )
    }

    /**
     * Use case: a part whose heading was not written yet is built without it, so its further heading
     * line comes first.
     */
    @Test
    fun withoutAHeadingTheFurtherLineComesFirst() {
        val blocks = BookPartBuilder.build(chapter().copy(title = ""), pageDesign)

        assertEquals("In which the ship comes in", blocks[0].toString())
    }

    /**
     * Use case: the user pressed return twice and left a paragraph empty; unlike an empty heading it
     * is kept, because it is part of what was written.
     */
    @Test
    fun anEmptyParagraphIsKeptWhileAnEmptyHeadingIsNot() {
        val part = chapter().copy(title = "  ", paragraph = listOf("First.", "", "Second."))

        val blocks = BookPartBuilder.build(part, pageDesign)

        assertEquals(
            listOf("In which the ship comes in", "First.", "", "Second."),
            blocks.map { it.toString() }
        )
    }

    /**
     * Use case: a prolog and an epilog carry the same shape as a chapter, so all three are built the
     * very same way from the page design they are handed.
     */
    @Test
    fun aPrologAndAnEpilogAreBuiltLikeAChapter() {
        val prolog = Prolog(title = "Before", paragraph = listOf("It began earlier."))
        val epilog = Epilog(title = "After", paragraph = listOf("It ended later."))

        val fromProlog = BookPartBuilder.build(prolog, pageDesign)
        val fromEpilog = BookPartBuilder.build(epilog, pageDesign)

        assertEquals(listOf("Before", "It began earlier."), fromProlog.map { it.toString() })
        assertEquals(listOf("After", "It ended later."), fromEpilog.map { it.toString() })
        assertEquals(fromProlog.map { it.style }, fromEpilog.map { it.style })
    }

    /**
     * Use case: a part that was only outlined carries neither heading nor text and gives no block at
     * all.
     */
    @Test
    fun anEmptyPartGivesNoBlock() {
        assertTrue(BookPartBuilder.build(Prolog(), pageDesign).isEmpty())
    }
}

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

package org.pcsoft.app.aighost.layouting

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/**
 * Developer tests for the greedy line breaking, [GreedyLineBreaker].
 *
 * Everything is measured with [FixedTextMetrics], so every expected number is arithmetic: at a size
 * of ten points a character and a space are five points wide, a line is ten points high and its
 * ascent is eight.
 */
class GreedyLineBreakerTest {

    private val breaker: LineBreaker = GreedyLineBreaker(FixedTextMetrics())

    private fun style(
        alignment: TextAlignment = TextAlignment.LEFT,
        lineSpacing: Double = 1.0,
        spaceBefore: Double = 0.0,
        spaceAfter: Double = 0.0
    ) = TextStyle(
        family = "Test Family",
        size = 10.0,
        alignment = alignment,
        lineSpacing = lineSpacing,
        spaceBefore = spaceBefore,
        spaceAfter = spaceAfter
    )

    /**
     * Use case: a text that fits the column is set as a single line, keeping its words and taking the
     * width of the words plus the gaps between them.
     */
    @Test
    fun aTextThatFitsStaysOnOneLine() {
        val result = breaker.breakText(listOf(TextBlock("one two three", style())), 100.0)

        assertEquals(1, result.lines.size)
        val line = result.lines.single()
        assertEquals("one two three", line.text)
        assertEquals(65.0, line.width)
        assertEquals(0.0, line.x)
        assertEquals(100.0, result.columnWidth)
    }

    /**
     * Use case: a text wider than the column is broken at the last word that still fits, and the rest
     * opens the next line.
     */
    @Test
    fun aTextIsBrokenAtTheLastWordThatFits() {
        val result = breaker.breakText(listOf(TextBlock("one two three", style())), 40.0)

        assertEquals(listOf("one two", "three"), result.lines.map { it.text })
        assertEquals(listOf(35.0, 25.0), result.lines.map { it.width })
    }

    /**
     * Use case: the lines of a block are stacked; the second line stands one line height below the
     * first and every baseline sits the ascent below the top of its line.
     */
    @Test
    fun linesAreStackedByTheirLineHeight() {
        val result = breaker.breakText(listOf(TextBlock("one two three", style())), 40.0)

        assertEquals(listOf(0.0, 10.0), result.lines.map { it.y })
        assertEquals(listOf(8.0, 18.0), result.lines.map { it.baseline })
        assertEquals(20.0, result.height)
    }

    /**
     * Use case: the design spreads the lines of an element apart, so the line spacing factor widens
     * the step from one line to the next and with it the height of the whole result.
     */
    @Test
    fun theLineSpacingWidensTheStep() {
        val result = breaker.breakText(listOf(TextBlock("one two three", style(lineSpacing = 2.0))), 40.0)

        assertEquals(listOf(0.0, 20.0), result.lines.map { it.y })
        assertEquals(40.0, result.height)
    }

    /**
     * Use case: a block asks for empty space above and below itself, which pushes its first line down
     * and is part of the height the caller has to make room for.
     */
    @Test
    fun theGapsAroundABlockArePartOfItsHeight() {
        val block = TextBlock("one two three", style(spaceBefore = 7.0, spaceAfter = 3.0))

        val result = breaker.breakText(listOf(block), 40.0)

        assertEquals(listOf(7.0, 17.0), result.lines.map { it.y })
        assertEquals(30.0, result.height)
    }

    /**
     * Use case: a left aligned block starts every line at the left edge and leaves the right edge
     * ragged.
     */
    @Test
    fun leftAlignmentKeepsEveryLineAtTheLeftEdge() {
        val result = breaker.breakText(listOf(TextBlock("one two three", style(TextAlignment.LEFT))), 40.0)

        assertEquals(listOf(0.0, 0.0), result.lines.map { it.x })
    }

    /**
     * Use case: a centred block places every line so that the space left over is split evenly between
     * its two sides.
     */
    @Test
    fun centreAlignmentSplitsTheRemainingWidth() {
        val result = breaker.breakText(listOf(TextBlock("one two three", style(TextAlignment.CENTER))), 40.0)

        assertEquals(listOf(2.5, 7.5), result.lines.map { it.x })
    }

    /**
     * Use case: a right aligned block pushes every line against the right edge, so the whole space
     * left over stands in front of it.
     */
    @Test
    fun rightAlignmentPushesEveryLineToTheRightEdge() {
        val result = breaker.breakText(listOf(TextBlock("one two three", style(TextAlignment.RIGHT))), 40.0)

        assertEquals(listOf(5.0, 15.0), result.lines.map { it.x })
    }

    /**
     * Use case: a justified block spreads the width left over onto the gaps of the line, so both edges
     * are flush - while the last line keeps the plain space and stays at the left.
     */
    @Test
    fun justificationSpreadsTheRemainingWidthOntoTheGaps() {
        val result = breaker.breakText(listOf(TextBlock("one two three", style(TextAlignment.JUSTIFY))), 40.0)

        val stretched = result.lines.first()
        assertEquals(10.0, stretched.wordSpacing, "the single gap takes the whole remaining width")
        assertEquals(40.0, stretched.width)
        assertEquals(0.0, stretched.x)

        val last = result.lines.last()
        assertEquals(5.0, last.wordSpacing, "the last line keeps the plain space of the font")
        assertEquals(25.0, last.width)
        assertEquals(0.0, last.x)
    }

    /**
     * Use case: a justified line that carries a single word has no gap to spread the remaining width
     * onto, so it is left as it is instead of being torn apart.
     */
    @Test
    fun aJustifiedLineWithoutAGapIsNotStretched() {
        val text = "supercalifragilistic and one two"
        val result = breaker.breakText(listOf(TextBlock(text, style(TextAlignment.JUSTIFY))), 100.0)

        val first = result.lines.first()
        assertEquals("supercalifragilistic", first.text)
        assertEquals(5.0, first.wordSpacing)
        assertEquals(100.0, first.width)
    }

    /**
     * Use case: a word wider than the whole column is not cut into pieces; it is set alone on its line
     * and is allowed to stick out.
     */
    @Test
    fun anOverlongWordIsSetAloneAndSticksOut() {
        val result = breaker.breakText(listOf(TextBlock("hi supercalifragilistic", style())), 40.0)

        assertEquals(listOf("hi", "supercalifragilistic"), result.lines.map { it.text })
        val overlong = result.lines.last()
        assertEquals(100.0, overlong.width)
        assertTrue(overlong.width > result.columnWidth, "the word must be allowed to overflow")
    }

    /**
     * Use case: the user left a paragraph empty; it keeps its line and its vertical space instead of
     * silently disappearing from the page.
     */
    @Test
    fun anEmptyBlockKeepsItsLine() {
        val result = breaker.breakText(listOf(TextBlock("", style())), 40.0)

        val line = result.lines.single()
        assertEquals("", line.text)
        assertEquals(0.0, line.width)
        assertEquals(0, line.charStart)
        assertEquals(0, line.charEnd)
        assertEquals(10.0, result.height)
    }

    /**
     * Use case: a block that is nothing but whitespace carries no word at all and is treated like an
     * empty one - one line, no text.
     */
    @Test
    fun aBlockOfWhitespaceIsTreatedLikeAnEmptyOne() {
        val result = breaker.breakText(listOf(TextBlock("   ", style())), 40.0)

        assertEquals(1, result.lines.size)
        assertEquals("", result.lines.single().text)
    }

    /**
     * Use case: a click on a set line has to be turned back into a position in the manuscript, so every
     * line names its block and the range of characters it was set from.
     */
    @Test
    fun everyLineMapsBackOntoItsBlockAndCharacterRange() {
        val blocks = listOf(
            TextBlock("one two three", style()),
            TextBlock("four", style())
        )

        val result = breaker.breakText(blocks, 40.0)

        assertEquals(listOf(0, 0, 1), result.lines.map { it.blockIndex })
        assertEquals(listOf(0, 8, 0), result.lines.map { it.charStart })
        assertEquals(listOf(7, 13, 4), result.lines.map { it.charEnd })
        result.lines.forEach { line ->
            val source = blocks[line.blockIndex].text
            assertEquals(line.text, source.substring(line.charStart, line.charEnd))
        }
    }

    /**
     * Use case: a hyphenated word may be broken behind its hyphen; the two halves then carry no gap
     * between them, neither in the width nor in the text of the line.
     */
    @Test
    fun aHyphenIsABreakOpportunityWithoutAGap() {
        val block = TextBlock("auto-mobile", style())

        val together = breaker.breakText(listOf(block), 100.0).lines.single()
        assertEquals("auto-mobile", together.text)
        assertEquals(55.0, together.width, "no gap is counted inside a hyphenated word")

        val broken = breaker.breakText(listOf(block), 30.0)
        assertEquals(listOf("auto-", "mobile"), broken.lines.map { it.text })
        assertEquals(listOf(0, 5), broken.lines.map { it.charStart })
        assertEquals(listOf(5, 11), broken.lines.map { it.charEnd })
    }

    /**
     * Use case: several blocks are set one after another, each in its own style, and the second block
     * starts below the last line of the first.
     */
    @Test
    fun blocksAreSetOneAfterAnother() {
        val blocks = listOf(
            TextBlock("title", style(alignment = TextAlignment.CENTER, spaceAfter = 5.0)),
            TextBlock("body text", style())
        )

        val result = breaker.breakText(blocks, 100.0)

        assertEquals(listOf(0.0, 15.0), result.lines.map { it.y })
        assertEquals(TextAlignment.CENTER, result.lines.first().style.alignment)
        assertEquals(TextAlignment.LEFT, result.lines.last().style.alignment)
        assertEquals(25.0, result.height)
    }

    /**
     * Use case: nothing was written yet, so breaking gives an empty result instead of failing.
     */
    @Test
    fun anEmptySequenceGivesAnEmptyResult() {
        val result = breaker.breakText(emptyList(), 40.0)

        assertTrue(result.lines.isEmpty())
        assertEquals(0.0, result.height)
    }

    /**
     * Use case: the same input is laid out twice while the user types, and both runs give exactly the
     * same numbers - otherwise the page would flicker without anything having changed.
     */
    @Test
    fun twoRunsGiveTheSameNumbers() {
        val blocks = listOf(
            TextBlock("one two three", style(TextAlignment.JUSTIFY)),
            TextBlock("", style()),
            TextBlock("a longer paragraph that has to be broken more than once", style(TextAlignment.JUSTIFY))
        )

        val first = breaker.breakText(blocks, 60.0)
        val second = breaker.breakText(blocks, 60.0)

        assertEquals(first, second)
    }

    /**
     * Use case: a column of no width is not a column; the caller is told instead of getting a result
     * with a line per word.
     */
    @Test
    fun aColumnWithoutWidthIsRefused() {
        val blocks = listOf(TextBlock("one", style()))

        assertThrows(IllegalArgumentException::class.java) { breaker.breakText(blocks, 0.0) }
        assertThrows(IllegalArgumentException::class.java) { breaker.breakText(blocks, -10.0) }
    }
}

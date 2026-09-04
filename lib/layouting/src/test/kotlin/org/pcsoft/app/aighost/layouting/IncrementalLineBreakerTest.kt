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
 * Developer tests for [IncrementalLineBreaker].
 *
 * The delegate is a real [GreedyLineBreaker] on [FixedTextMetrics], wrapped in a [CountingLineBreaker]
 * that records which blocks were actually broken. A second, plain [GreedyLineBreaker] on the same
 * metrics is the reference every incremental result is compared against, so "same as breaking
 * everything in one go" is checked as bit equality, not as an approximation.
 */
class IncrementalLineBreakerTest {

    private val metrics = FixedTextMetrics()
    private val counting = CountingLineBreaker(GreedyLineBreaker(metrics))
    private val incremental = IncrementalLineBreaker(counting)
    private val reference: LineBreaker = GreedyLineBreaker(metrics)

    private fun style(
        size: Double = 10.0,
        alignment: TextAlignment = TextAlignment.LEFT,
        spaceAfter: Double = 0.0
    ) = TextStyle(family = "Test Family", size = size, alignment = alignment, spaceAfter = spaceAfter)

    private fun document() = listOf(
        TextBlock("the first paragraph of the part", style(spaceAfter = 4.0)),
        TextBlock("", style()),
        TextBlock("a second paragraph that is long enough to be broken more than once", style()),
        TextBlock("the third and last paragraph", style())
    )

    /**
     * Use case: the very first call has nothing cached, so every block is handed to the delegate and
     * the result is the same as breaking the whole document in one go.
     */
    @Test
    fun theFirstCallBreaksEveryBlockAndMatchesAFullBreak() {
        val blocks = document()

        val result = incremental.breakText(blocks, 120.0)

        assertEquals(reference.breakText(blocks, 120.0), result)
        assertEquals(blocks.map { it.text }, counting.brokenTexts, "every block is broken once")
        assertEquals(0L, incremental.cacheHits)
        assertEquals(blocks.size.toLong(), incremental.cacheMisses)
    }

    /**
     * Use case: the user typed in one paragraph; the next layout re-breaks only that paragraph and
     * reads every other block from the cache, while the stacked result still equals a full break.
     */
    @Test
    fun onlyTheChangedBlockIsBrokenAgain() {
        val first = document()
        incremental.breakText(first, 120.0)
        counting.reset()
        val hitsBefore = incremental.cacheHits
        val missesBefore = incremental.cacheMisses

        val edited = first.toMutableList()
        edited[2] = TextBlock("a second paragraph that is now a little longer than it was before", first[2].style)

        val result = incremental.breakText(edited, 120.0)

        assertEquals(reference.breakText(edited, 120.0), result)
        assertEquals(listOf(edited[2].text), counting.brokenTexts, "only the edited paragraph is measured")
        assertEquals(3L, incremental.cacheHits - hitsBefore, "the three unchanged blocks are read from the cache")
        assertEquals(1L, incremental.cacheMisses - missesBefore, "only the edited block is broken again")
    }

    /**
     * Use case: only the style of a block changed - a heading was made larger, say - which is a
     * different key, so that block is broken again and nothing else is.
     */
    @Test
    fun aChangedStyleInvalidatesOnlyThatBlock() {
        val first = document()
        incremental.breakText(first, 120.0)
        counting.reset()

        val restyled = first.toMutableList()
        restyled[0] = TextBlock(first[0].text, first[0].style.copy(size = 18.0))

        val result = incremental.breakText(restyled, 120.0)

        assertEquals(reference.breakText(restyled, 120.0), result)
        assertEquals(listOf(restyled[0].text), counting.brokenTexts)
    }

    /**
     * Use case: the column got narrower - the sheet was resized - so every previous result is useless;
     * the whole document is broken again and the cache does not keep the stale entries around.
     */
    @Test
    fun aChangedColumnWidthDropsTheWholeCache() {
        val blocks = document()
        incremental.breakText(blocks, 120.0)
        counting.reset()

        val result = incremental.breakText(blocks, 80.0)

        assertEquals(reference.breakText(blocks, 80.0), result)
        assertEquals(blocks.map { it.text }, counting.brokenTexts, "every block is re-broken for the new width")
        assertEquals(blocks.size, incremental.cacheSize, "no entry for the old width is left behind")
    }

    /**
     * Use case: the design changed every style at once, which the key cannot see; the caller clears
     * the cache, and the next call measures the whole document again.
     */
    @Test
    fun clearForcesAFullRebreak() {
        val blocks = document()
        incremental.breakText(blocks, 120.0)
        counting.reset()

        incremental.clear()
        assertEquals(0, incremental.cacheSize)

        incremental.breakText(blocks, 120.0)

        assertEquals(blocks.map { it.text }, counting.brokenTexts)
    }

    /**
     * Use case: an empty paragraph between two written ones keeps its own line and its vertical space
     * after being read from the cache, exactly as a full break would place it.
     */
    @Test
    fun anEmptyBlockKeepsItsSpaceThroughTheCache() {
        val blocks = document()
        incremental.breakText(blocks, 120.0)

        val result = incremental.breakText(blocks, 120.0)

        assertEquals(reference.breakText(blocks, 120.0), result)
        val emptyLine = result.lines.single { it.blockIndex == 1 }
        assertEquals("", emptyLine.text)
    }

    /**
     * Use case: a caller pre-breaks the blocks it is about to show off a typing pause; the following
     * real break is then answered entirely from the cache without touching the delegate.
     */
    @Test
    fun prewarmMakesTheNextBreakAPureCacheRead() {
        val blocks = document()

        incremental.prewarm(blocks, 120.0)
        assertEquals(blocks.size, incremental.cacheSize)
        counting.reset()

        val result = incremental.breakText(blocks, 120.0)

        assertEquals(reference.breakText(blocks, 120.0), result)
        assertTrue(counting.brokenTexts.isEmpty(), "prewarm already measured every block")
        assertEquals(blocks.size.toLong(), incremental.cacheHits)
    }

    /**
     * Use case: the same unchanged document is laid out twice while the user pauses; both runs give
     * exactly the same numbers, so the page does not flicker.
     */
    @Test
    fun twoRunsOverUnchangedInputGiveTheSameNumbers() {
        val blocks = document()

        val a = incremental.breakText(blocks, 120.0)
        val b = incremental.breakText(blocks, 120.0)

        assertEquals(a, b)
    }

    /**
     * Use case: nothing was written yet; breaking an empty list gives an empty result instead of
     * failing.
     */
    @Test
    fun anEmptySequenceGivesAnEmptyResult() {
        val result = incremental.breakText(emptyList(), 120.0)

        assertTrue(result.lines.isEmpty())
        assertEquals(0.0, result.height)
        assertEquals(120.0, result.columnWidth)
    }

    /**
     * Use case: a column of no width is not a column; the caller is told, on the real break and on the
     * prewarm alike.
     */
    @Test
    fun aColumnWithoutWidthIsRefused() {
        val blocks = document()

        assertThrows(IllegalArgumentException::class.java) { incremental.breakText(blocks, 0.0) }
        assertThrows(IllegalArgumentException::class.java) { incremental.prewarm(blocks, -5.0) }
    }

    /** A [LineBreaker] that forwards to a delegate and records the text of every block it breaks. */
    private class CountingLineBreaker(private val delegate: LineBreaker) : LineBreaker {

        private val broken = mutableListOf<String>()

        /** The text of every block handed to this breaker since the last [reset], in call order. */
        val brokenTexts: List<String>
            get() = broken.toList()

        override fun breakText(blocks: List<TextBlock>, columnWidth: Double): LaidOutText {
            blocks.forEach { broken += it.text }
            return delegate.breakText(blocks, columnWidth)
        }

        /** Forgets every recorded block, so the next assertion starts from a clean count. */
        fun reset() = broken.clear()
    }
}

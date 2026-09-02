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
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/**
 * Developer tests for [NonePageBreakPolicy].
 *
 * Every line is built by [line] with a `y` ten points below the previous one, matching what
 * [GreedyLineBreaker] produces for single spaced lines measured by [FixedTextMetrics].
 */
class NonePageBreakPolicyTest {

    private val policy: PageBreakPolicy = NonePageBreakPolicy

    private val style = TextStyle(family = "Test Family", size = 10.0)

    private fun line(y: Double) = LaidOutLine(
        x = 0.0,
        y = y,
        baseline = y + 8.0,
        width = 10.0,
        text = "line",
        style = style,
        blockIndex = 0,
        charStart = 0,
        charEnd = 4,
        wordSpacing = 0.0
    )

    private fun lines(count: Int) = (0 until count).map { line(it * 10.0) }

    /**
     * Use case: nothing was laid out yet, so breaking gives an empty result instead of a single empty
     * page.
     */
    @Test
    fun anEmptySequenceGivesNoPage() {
        assertTrue(policy.breakPages(emptyList(), 100.0).isEmpty())
    }

    /**
     * Use case: every line fits below the content height of one page, so they all stay on it.
     */
    @Test
    fun linesThatFitStayOnOnePage() {
        val pages = policy.breakPages(lines(3), contentHeight = 100.0)

        assertEquals(1, pages.size)
        assertEquals(3, pages.single().size)
    }

    /**
     * Use case: a fourth line no longer fits under the content height, so it opens a new page instead
     * of overflowing the current one.
     */
    @Test
    fun aLineThatNoLongerFitsOpensANewPage() {
        // Lines sit at y = 0, 10, 20, 30; a content height of 20 lets the third line (y = 20) in,
        // since 20 - 0 = 20 is still within bounds, but pushes the fourth (y = 30) to the next page.
        val pages = policy.breakPages(lines(4), contentHeight = 20.0)

        assertEquals(listOf(3, 1), pages.map { it.size })
    }

    /**
     * Use case: a page break resets the reference the next page's content height is measured from, so
     * every page can hold the same number of lines instead of shrinking further with every break.
     */
    @Test
    fun eachPageMeasuresFromItsOwnFirstLine() {
        val pages = policy.breakPages(lines(7), contentHeight = 20.0)

        assertEquals(listOf(3, 3, 1), pages.map { it.size })
    }
}

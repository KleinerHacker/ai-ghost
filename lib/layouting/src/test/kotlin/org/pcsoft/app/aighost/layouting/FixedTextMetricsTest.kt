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
import org.junit.jupiter.api.Test

/**
 * Developer tests for the deterministic measuring, [FixedTextMetrics].
 *
 * The implementation exists so a layout can be read as plain arithmetic, so what is proven here is
 * exactly that: every number follows from the font size and the length of the text.
 */
class FixedTextMetricsTest {

    private val metrics = FixedTextMetrics()

    private fun style(size: Double = 10.0) = TextStyle(family = "Test Family", size = size)

    /**
     * Use case: a caller needs the width of a word and gets its length times the character advance
     * times the font size, so the expected value can be written down without running anything.
     */
    @Test
    fun wordWidthIsTheLengthTimesTheAdvance() {
        assertEquals(15.0, metrics.wordWidth(style(), "one"))
        assertEquals(25.0, metrics.wordWidth(style(), "three"))
        assertEquals(0.0, metrics.wordWidth(style(), ""))
    }

    /**
     * Use case: the same word is measured in a bigger size and grows exactly with that size, which is
     * what makes a layout comparable across sizes.
     */
    @Test
    fun widthFollowsTheFontSize() {
        assertEquals(15.0, metrics.wordWidth(style(size = 10.0), "one"))
        assertEquals(30.0, metrics.wordWidth(style(size = 20.0), "one"))
    }

    /**
     * Use case: the gap between two words is as wide as a single character, so a line of n words and
     * n-1 gaps can be counted out by hand.
     */
    @Test
    fun spaceIsAsWideAsOneCharacter() {
        assertEquals(5.0, metrics.spaceWidth(style()))
        assertEquals(metrics.wordWidth(style(), "x"), metrics.spaceWidth(style()))
    }

    /**
     * Use case: stacking lines needs ascent, descent and leading; they follow the font size and add up
     * to the line height the breaker advances by.
     */
    @Test
    fun lineMetricsFollowTheFontSize() {
        val measured = metrics.lineMetrics(style(size = 10.0))

        assertEquals(8.0, measured.ascent)
        assertEquals(2.0, measured.descent)
        assertEquals(0.0, measured.leading)
        assertEquals(10.0, measured.lineHeight)
    }

    /**
     * Use case: a caller wants other proportions - a wider face, a deeper descender - and gets them by
     * handing in its own factors instead of writing a second implementation.
     */
    @Test
    fun theFactorsCanBeChosen() {
        val wide = FixedTextMetrics(advance = 1.0, ascentFactor = 1.0, descentFactor = 0.5, leadingFactor = 0.25)

        assertEquals(30.0, wide.wordWidth(style(), "one"))
        assertEquals(10.0, wide.spaceWidth(style()))
        assertEquals(LineMetrics(10.0, 5.0, 2.5), wide.lineMetrics(style()))
    }

    /**
     * Use case: only the size of a style is read - the family and the cut must not change a number,
     * otherwise a test could not predict one.
     */
    @Test
    fun familyAndCutAreIgnored() {
        val plain = style()
        val fancy = plain.copy(family = "Another Family", bold = true, italic = true)

        assertEquals(metrics.wordWidth(plain, "word"), metrics.wordWidth(fancy, "word"))
        assertEquals(metrics.spaceWidth(plain), metrics.spaceWidth(fancy))
        assertEquals(metrics.lineMetrics(plain), metrics.lineMetrics(fancy))
    }
}

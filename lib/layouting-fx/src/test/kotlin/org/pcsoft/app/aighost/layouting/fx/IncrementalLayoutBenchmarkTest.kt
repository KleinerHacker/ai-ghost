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

package org.pcsoft.app.aighost.layouting.fx

import javafx.stage.Stage
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.pcsoft.app.aighost.layouting.GreedyLineBreaker
import org.pcsoft.app.aighost.layouting.IncrementalLineBreaker
import org.pcsoft.app.aighost.layouting.LayoutEngine
import org.pcsoft.app.aighost.layouting.NonePageBreakPolicy
import org.pcsoft.app.aighost.layouting.PageGeometry
import org.pcsoft.app.aighost.layouting.TextAlignment
import org.pcsoft.app.aighost.layouting.TextBlock
import org.pcsoft.app.aighost.layouting.TextStyle
import org.pcsoft.app.aighost.layouting.fx.font.FontCatalog
import org.pcsoft.app.aighost.layouting.fx.font.JavaFxTextMetrics
import org.testfx.framework.junit5.ApplicationTest
import org.testfx.util.WaitForAsyncUtils
import kotlin.system.measureNanoTime

/**
 * Benchmark developer test for the incremental line breaking of IP-05, run against the real JavaFX
 * measuring [JavaFxTextMetrics].
 *
 * A mock manuscript of book length - [PARAGRAPH_COUNT] paragraphs of realistic prose plus a heading -
 * is laid out once from cold, then again after a single paragraph was edited. The test proves the two
 * properties IP-05 rests on:
 *
 * * **Correctness.** The incrementally stacked result is the same as breaking the whole part in one
 *   go with a plain [GreedyLineBreaker] - measured with the same font, so the comparison is exact
 *   apart from the last bit of a coordinate.
 * * **Cost.** A keystroke re-breaks exactly one block: [IncrementalLineBreaker.cacheMisses] grows by
 *   one, every other block is a cache hit, and the wall time of the edit relayout is a fraction of
 *   the cold layout.
 *
 * Measuring needs a `Text` node, so the test runs headless on the JavaFX application thread. The
 * measured durations are printed for the record; only the structural facts are asserted, so the test
 * does not turn flaky on a slow runner.
 */
class IncrementalLayoutBenchmarkTest : ApplicationTest() {

    override fun start(stage: Stage) = Unit

    private fun <T> fx(block: () -> T): T =
        WaitForAsyncUtils.asyncFx<T> { block() }.get()

    /**
     * Use case: while the user types in a book-length part, only the edited paragraph is measured
     * again and the layout it produces is identical to a full re-break of the whole part.
     */
    @Test
    fun aKeystrokeReBreaksOneBlockAndMatchesAFullBreak() {
        fx { JavaFxTextMetrics.clearCache() }

        val family = fx { FontCatalog.families }.first()
        val bodyStyle = TextStyle(family = family, size = 11.0, alignment = TextAlignment.JUSTIFY, lineSpacing = 1.3)
        val headingStyle = TextStyle(family = family, size = 20.0, bold = true, spaceAfter = 12.0)
        val geometry = PageGeometry(
            width = 419.53, height = 595.28,
            innerMargin = 56.7, outerMargin = 42.5, topMargin = 42.5, bottomMargin = 56.7,
            mirroredMargins = false
        )
        val columnWidth = geometry.width - geometry.innerMargin - geometry.outerMargin

        val original = buildList {
            add(TextBlock("Chapter One", headingStyle))
            repeat(PARAGRAPH_COUNT) { index -> add(TextBlock(paragraph(index), bodyStyle)) }
        }

        val incremental = IncrementalLineBreaker(GreedyLineBreaker(JavaFxTextMetrics))
        val reference = GreedyLineBreaker(JavaFxTextMetrics)

        // Cold layout: nothing is cached, every block is measured.
        lateinit var coldLayout: org.pcsoft.app.aighost.layouting.DocumentLayout
        val coldNanos = fx {
            measureNanoTime {
                val text = incremental.breakText(original, columnWidth)
                coldLayout = LayoutEngine.layout(text, geometry, startPageNumber = null, policy = NonePageBreakPolicy)
            }
        }
        assertEquals(original.size.toLong(), incremental.cacheMisses, "cold layout breaks every block once")
        assertEquals(0L, incremental.cacheHits)

        // One paragraph in the middle gets a keystroke.
        val editedIndex = PARAGRAPH_COUNT / 2 + 1
        val edited = original.toMutableList()
        edited[editedIndex] = TextBlock(original[editedIndex].text + " indeed.", bodyStyle)

        val missesBefore = incremental.cacheMisses
        lateinit var editLayout: org.pcsoft.app.aighost.layouting.DocumentLayout
        val editNanos = fx {
            measureNanoTime {
                val text = incremental.breakText(edited, columnWidth)
                editLayout = LayoutEngine.layout(text, geometry, startPageNumber = null, policy = NonePageBreakPolicy)
            }
        }

        assertEquals(1L, incremental.cacheMisses - missesBefore, "a keystroke re-breaks exactly one block")
        assertEquals((original.size - 1).toLong(), incremental.cacheHits, "every other block is read from the cache")

        val fullEditLayout = fx {
            val text = reference.breakText(edited, columnWidth)
            LayoutEngine.layout(text, geometry, startPageNumber = null, policy = NonePageBreakPolicy)
        }
        assertEquals(fullEditLayout.pages.size, editLayout.pages.size, "same page count as a full re-break")
        assertEquals(
            fullEditLayout.pages.flatMap { it.lines }.map { it.text },
            editLayout.pages.flatMap { it.lines }.map { it.text },
            "same lines, in the same order, as a full re-break"
        )

        assertTrue(coldLayout.pages.size > 1, "the mock manuscript is long enough to span many pages")
        assertTrue(
            editNanos * 3 < coldNanos,
            "the edit relayout ($editNanos ns) must be well below the cold layout ($coldNanos ns)"
        )

        println(
            "IP-05 benchmark: ${original.size} blocks, ${coldLayout.pages.size} pages | " +
                "cold ${coldNanos / 1_000_000.0} ms | edit ${editNanos / 1_000_000.0} ms"
        )
    }

    private companion object {

        /** Paragraphs in the mock manuscript, a realistic length for a single book chapter. */
        const val PARAGRAPH_COUNT: Int = 200

        private val WORDS: List<String> = (
            "the quiet harbour kept its boats close while a thin rain moved over the grey water and " +
                "the town behind it slept without a single lamp burning in any window that morning"
            ).split(" ")

        /**
         * Builds a deterministic paragraph whose length and wording vary a little with [seed]. The
         * paragraph opens with an ordinal word, so no two paragraphs of the manuscript share their
         * text and every one is a distinct block for the cache.
         */
        fun paragraph(seed: Int): String {
            val wordCount = 55 + (seed * 7) % 40
            val builder = StringBuilder("Paragraph $seed:")
            for (i in 0 until wordCount) {
                builder.append(' ')
                builder.append(WORDS[(seed + i * 3) % WORDS.size])
            }
            builder.append('.')
            return builder.toString()
        }
    }
}

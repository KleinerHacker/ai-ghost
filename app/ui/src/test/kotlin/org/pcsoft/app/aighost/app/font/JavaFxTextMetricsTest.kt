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

package org.pcsoft.app.aighost.app.font

import javafx.geometry.VPos
import javafx.scene.text.Text
import javafx.stage.Stage
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.pcsoft.app.aighost.model.common.FontData
import org.testfx.framework.junit5.ApplicationTest
import org.testfx.util.WaitForAsyncUtils

/**
 * Developer tests for the JavaFX backed text measuring, [JavaFxTextMetrics].
 *
 * Measuring needs a `Text` node and therefore a started toolkit, so the tests run headless on the
 * JavaFX application thread.
 */
class JavaFxTextMetricsTest : ApplicationTest() {

    override fun start(stage: Stage) = Unit

    /** Runs [block] on the JavaFX application thread and hands back its result. */
    private fun <T> fx(block: () -> T): T =
        WaitForAsyncUtils.asyncFx<T> { block() }.get()

    /** The measurer is a singleton shared by all callers, so every test starts from an empty cache. */
    @BeforeEach
    fun clearCache() {
        fx { JavaFxTextMetrics.clearCache() }
    }

    private fun installedFont(size: Int = 12, bold: Boolean = false, italic: Boolean = false) =
        FontData(name = fx { FontCatalog.families }.first(), size = size, bold = bold, italic = italic)

    /**
     * Use case: the layout engine asks for the width of a word and gets the width JavaFX itself
     * reports for that word in that font - unrounded, so line breaking stays exact.
     */
    @Test
    fun wordWidthIsTheUnroundedWidthOfTheJavaFxText() {
        val font = installedFont()

        val measured = fx { JavaFxTextMetrics.wordWidth(font, "manuscript") }

        val expected = fx {
            val reference = Text("manuscript")
            reference.textOrigin = VPos.BASELINE
            reference.font = FontResolver.font(font)
            reference.wrappingWidth = 0.0
            reference.prefWidth(-1.0)
        }
        assertEquals(expected, measured)
        assertTrue(measured > 0.0)
    }

    /**
     * Use case: a longer word is wider than a shorter one and a bigger size widens the same word, so
     * the numbers the engine breaks lines with really follow the text and the design.
     */
    @Test
    fun widthFollowsTheTextAndTheFontSize() {
        val small = installedFont(size = 12)
        val big = installedFont(size = 24)

        val short = fx { JavaFxTextMetrics.wordWidth(small, "ink") }
        val long = fx { JavaFxTextMetrics.wordWidth(small, "inkwell") }
        val enlarged = fx { JavaFxTextMetrics.wordWidth(big, "ink") }

        assertTrue(long > short, "a longer word must be wider")
        assertTrue(enlarged > short, "a bigger size must be wider")
    }

    /**
     * Use case: the engine puts the words of a line together itself and needs the width of the single
     * space between them, which is positive and narrower than a word.
     */
    @Test
    fun spaceWidthIsMeasuredOnItsOwn() {
        val font = installedFont()

        val space = fx { JavaFxTextMetrics.spaceWidth(font) }
        val word = fx { JavaFxTextMetrics.wordWidth(font, "chapter") }

        assertTrue(space > 0.0, "the space must have a width")
        assertTrue(space < word, "a space must be narrower than a word")
    }

    /**
     * Use case: the engine stacks lines on the paper and needs ascent, descent and leading of the
     * font, whose sum is the distance from one baseline to the next.
     */
    @Test
    fun lineMetricsCarryAscentDescentAndLeading() {
        val font = installedFont(size = 20)

        val metrics = fx { JavaFxTextMetrics.lineMetrics(font) }

        assertTrue(metrics.ascent > 0.0, "ascent must be positive")
        assertTrue(metrics.descent > 0.0, "descent must be positive")
        assertTrue(metrics.leading >= 0.0, "leading must not be negative")
        assertEquals(metrics.ascent + metrics.descent + metrics.leading, metrics.lineHeight)
        assertTrue(metrics.lineHeight >= metrics.ascent + metrics.descent)
    }

    /**
     * Use case: the same word in the same style is measured again while the user types; the answer
     * comes from the cache instead of the font system and is the very same number.
     */
    @Test
    fun repeatedMeasurementIsAnsweredFromTheCache() {
        val font = installedFont()

        val first = fx { JavaFxTextMetrics.wordWidth(font, "paragraph") }
        assertEquals(1, fx { JavaFxTextMetrics.cacheSize })
        assertEquals(0L, fx { JavaFxTextMetrics.cacheHits })

        val second = fx { JavaFxTextMetrics.wordWidth(font, "paragraph") }

        assertEquals(first, second)
        assertEquals(1, fx { JavaFxTextMetrics.cacheSize })
        assertEquals(1L, fx { JavaFxTextMetrics.cacheHits })
    }

    /**
     * Use case: the line metrics of a font are asked for once per line; the second call is a cache
     * hit as well and does not touch the helper node again.
     */
    @Test
    fun lineMetricsAreCachedPerFont() {
        val font = installedFont()

        val first = fx { JavaFxTextMetrics.lineMetrics(font) }
        val second = fx { JavaFxTextMetrics.lineMetrics(font) }

        assertEquals(first, second)
        assertEquals(1, fx { JavaFxTextMetrics.cacheSize })
        assertEquals(1L, fx { JavaFxTextMetrics.cacheHits })
    }

    /**
     * Use case: the same word set in another style is another measurement - family, size, weight and
     * slant all take part in the identity of a cached width.
     */
    @Test
    fun styleIsPartOfTheCacheKey() {
        val plain = installedFont()
        val bold = installedFont(bold = true)
        val italic = installedFont(italic = true)
        val bigger = installedFont(size = 18)

        listOf(plain, bold, italic, bigger).forEach { font ->
            fx { JavaFxTextMetrics.wordWidth(font, "title") }
        }

        assertEquals(4, fx { JavaFxTextMetrics.cacheSize })
        assertEquals(0L, fx { JavaFxTextMetrics.cacheHits })
    }

    /**
     * Use case: measuring runs on every keystroke, so the hidden helper node is built once and reused
     * instead of being created per measurement.
     */
    @Test
    fun theHiddenHelperNodeIsReused() {
        val font = installedFont()

        val node = fx { JavaFxTextMetrics.wordWidth(font, "first"); JavaFxTextMetrics.helper }
        val again = fx { JavaFxTextMetrics.wordWidth(font, "second"); JavaFxTextMetrics.helper }

        assertSame(node, again)
        assertTrue(fx { !JavaFxTextMetrics.helper.isVisible }, "the helper node must stay hidden")
    }

    /**
     * Use case: a font that is not installed is measured through its substitute, so the numbers match
     * what is really drawn on the paper.
     */
    @Test
    fun missingFamilyIsMeasuredWithItsSubstitute() {
        val missing = FontData(name = "No Such Family 4711", size = 15)
        val substitute = fx {
            val resolution = FontResolver.resolve(missing) as FontResolution.NotInstalled
            FontData(name = resolution.substituteFamily, size = 15)
        }

        val missingWidth = fx { JavaFxTextMetrics.wordWidth(missing, "substitute") }
        val substituteWidth = fx { JavaFxTextMetrics.wordWidth(substitute, "substitute") }

        assertEquals(substituteWidth, missingWidth)
    }

    /**
     * Use case: the font catalogue was built again, so every measurement taken against the previous
     * set of families is dropped.
     */
    @Test
    fun clearingTheCacheDropsEveryMeasurement() {
        val font = installedFont()
        fx { JavaFxTextMetrics.wordWidth(font, "page") }
        fx { JavaFxTextMetrics.lineMetrics(font) }
        assertEquals(2, fx { JavaFxTextMetrics.cacheSize })

        fx { JavaFxTextMetrics.clearCache() }

        assertEquals(0, fx { JavaFxTextMetrics.cacheSize })
        assertEquals(0L, fx { JavaFxTextMetrics.cacheHits })
    }
}

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

package org.pcsoft.app.aighost.layouting.fx.font

import javafx.geometry.VPos
import javafx.scene.text.Text
import javafx.stage.Stage
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.pcsoft.app.aighost.layouting.TextStyle
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

    private fun installedFace(size: Int = 12, bold: Boolean = false, italic: Boolean = false) =
        FontDescription(family = fx { FontCatalog.families }.first(), size = size, bold = bold, italic = italic)

    /**
     * Use case: the layout engine asks for the width of a word and gets the width JavaFX itself
     * reports for that word in that font - unrounded, so line breaking stays exact.
     */
    @Test
    fun wordWidthIsTheUnroundedWidthOfTheJavaFxText() {
        val face = installedFace()

        val measured = fx { JavaFxTextMetrics.wordWidth(face, "manuscript") }

        val expected = fx {
            val reference = Text("manuscript")
            reference.textOrigin = VPos.BASELINE
            reference.font = FontResolver.font(face)
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
        val small = installedFace(size = 12)
        val big = installedFace(size = 24)

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
        val face = installedFace()

        val space = fx { JavaFxTextMetrics.spaceWidth(face) }
        val word = fx { JavaFxTextMetrics.wordWidth(face, "chapter") }

        assertTrue(space > 0.0, "the space must have a width")
        assertTrue(space < word, "a space must be narrower than a word")
    }

    /**
     * Use case: the engine stacks lines on the paper and needs ascent, descent and leading of the
     * font, whose sum is the distance from one baseline to the next.
     */
    @Test
    fun lineMetricsCarryAscentDescentAndLeading() {
        val face = installedFace(size = 20)

        val metrics = fx { JavaFxTextMetrics.lineMetrics(face) }

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
        val face = installedFace()

        val first = fx { JavaFxTextMetrics.wordWidth(face, "paragraph") }
        assertEquals(1, fx { JavaFxTextMetrics.cacheSize })
        assertEquals(0L, fx { JavaFxTextMetrics.cacheHits })

        val second = fx { JavaFxTextMetrics.wordWidth(face, "paragraph") }

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
        val face = installedFace()

        val first = fx { JavaFxTextMetrics.lineMetrics(face) }
        val second = fx { JavaFxTextMetrics.lineMetrics(face) }

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
        val plain = installedFace()
        val bold = installedFace(bold = true)
        val italic = installedFace(italic = true)
        val bigger = installedFace(size = 18)

        listOf(plain, bold, italic, bigger).forEach { face ->
            fx { JavaFxTextMetrics.wordWidth(face, "title") }
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
        val face = installedFace()

        val node = fx { JavaFxTextMetrics.wordWidth(face, "first"); JavaFxTextMetrics.helper }
        val again = fx { JavaFxTextMetrics.wordWidth(face, "second"); JavaFxTextMetrics.helper }

        assertSame(node, again)
        assertTrue(fx { !JavaFxTextMetrics.helper.isVisible }, "the helper node must stay hidden")
    }

    /**
     * Use case: a font that is not installed is measured through its substitute, so the numbers match
     * what is really drawn on the paper.
     */
    @Test
    fun missingFamilyIsMeasuredWithItsSubstitute() {
        val missing = FontDescription(family = "No Such Family 4711", size = 15)
        val substitute = fx {
            val resolution = FontResolver.resolve(missing) as FontResolution.NotInstalled
            FontDescription(family = resolution.substituteFamily, size = 15)
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
        val face = installedFace()
        fx { JavaFxTextMetrics.wordWidth(face, "page") }
        fx { JavaFxTextMetrics.lineMetrics(face) }
        assertEquals(2, fx { JavaFxTextMetrics.cacheSize })

        fx { JavaFxTextMetrics.clearCache() }

        assertEquals(0, fx { JavaFxTextMetrics.cacheSize })
        assertEquals(0L, fx { JavaFxTextMetrics.cacheHits })
    }

    /**
     * Use case: the layout core asks through its own interface, in its own style type, and gets
     * exactly the numbers the description of the same face gives - the translation loses nothing.
     */
    @Test
    fun measuringThroughTheLayoutInterfaceGivesTheSameNumbersAsTheDescription() {
        val face = installedFace(size = 14, bold = true, italic = true)
        val style = layoutStyle(face)

        val byFace = fx {
            Triple(
                JavaFxTextMetrics.wordWidth(face, "manuscript"),
                JavaFxTextMetrics.spaceWidth(face),
                JavaFxTextMetrics.lineMetrics(face)
            )
        }
        val byStyle = fx {
            Triple(
                JavaFxTextMetrics.wordWidth(style, "manuscript"),
                JavaFxTextMetrics.spaceWidth(style),
                JavaFxTextMetrics.lineMetrics(style)
            )
        }

        assertEquals(byFace, byStyle)
    }

    /**
     * Use case: the same face reaches the measurer once as a description and once as a layout style;
     * both answers come out of the very same cache entry instead of being measured twice.
     */
    @Test
    fun aLayoutStyleAndTheDescriptionShareTheirCacheEntry() {
        val face = installedFace()

        fx { JavaFxTextMetrics.wordWidth(face, "chapter") }
        assertEquals(1, fx { JavaFxTextMetrics.cacheSize })
        assertEquals(0L, fx { JavaFxTextMetrics.cacheHits })

        fx { JavaFxTextMetrics.wordWidth(layoutStyle(face), "chapter") }

        assertEquals(1, fx { JavaFxTextMetrics.cacheSize })
        assertEquals(1L, fx { JavaFxTextMetrics.cacheHits })
    }

    /**
     * Use case: the layout core hands the measurer a style whose numbers are read only for the face -
     * the line spacing of the style must not end up in the returned line metrics.
     */
    @Test
    fun theLineSpacingOfAStyleDoesNotReachTheMeasuredLineMetrics() {
        val face = installedFace()
        val tight = layoutStyle(face).copy(lineSpacing = 1.0)
        val wide = layoutStyle(face).copy(lineSpacing = 2.5, spaceBefore = 20.0, spaceAfter = 20.0)

        val first = fx { JavaFxTextMetrics.lineMetrics(tight) }
        val second = fx { JavaFxTextMetrics.lineMetrics(wide) }

        assertEquals(first, second)
    }

    /** The layout style of the given face, with everything vertical left at its default. */
    private fun layoutStyle(face: FontDescription) =
        TextStyle(
            family = face.family,
            size = face.size.toDouble(),
            bold = face.bold,
            italic = face.italic
        )
}

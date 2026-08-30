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
import javafx.scene.text.Font
import javafx.scene.text.Text
import org.pcsoft.app.aighost.layouting.LineMetrics
import org.pcsoft.app.aighost.layouting.TextMetrics
import org.pcsoft.app.aighost.layouting.TextStyle
import org.pcsoft.app.aighost.model.common.FontData

/**
 * Measures text with JavaFX, so the layout engine works with the very numbers the drawing side later
 * produces.
 *
 * This is the production implementation of [TextMetrics]: the layout core asks in its own
 * [TextStyle], which is translated into the [FontData] the font resolution works on. The two
 * overloads taking a [FontData] are the same measurement and stay available to everything inside the
 * application that already holds a stored font.
 *
 * Everything is measured through a single hidden [Text] node: the font is set, the text is set,
 * `wrappingWidth` is put to zero and `prefWidth(-1)` is read. No font file is opened and
 * `Font.loadFont` is never called.
 *
 * Two properties are deliberate and must not be "fixed":
 * * The helper node is **created once and reused**. Building a [Text] per measurement is far too
 *   expensive for a layout that runs on every keystroke.
 * * A width is returned **unrounded**. Rounding up to whole pixels is right for the preferred width
 *   of a control, but it makes line breaking coarse and dependent on the font size; whole pixels are
 *   made at painting time only.
 *
 * The engine breaks lines itself, so what is measured are words, a single space and the line
 * metrics - never a whole paragraph. That keeps [cacheSize] small and lets the same word be reused
 * across paragraphs.
 *
 * **Threading:** a [Text] node belongs to the JavaFX application thread, so every method here must be
 * called on that thread. This object is *not* thread safe and holds mutable state - the helper node
 * and the caches - that is shared between all callers.
 */
object JavaFxTextMetrics : TextMetrics {

    /** Carries an ascender, a descender and no accents, so ascent and descent are both covered. */
    private const val REFERENCE_TEXT: String = "Hxpg"

    /** The single space every word gap is measured with. */
    private const val SPACE: String = " "

    /**
     * The one and only helper node every measurement runs through.
     *
     * Visible to the tests of this module so the reuse of the node can be proven; nothing outside the
     * module ever touches it.
     */
    internal val helper: Text by lazy {
        Text().apply {
            textOrigin = VPos.BASELINE
            wrappingWidth = 0.0
            lineSpacing = 0.0
            isVisible = false
        }
    }

    private val widths: MutableMap<WidthKey, Double> = HashMap()
    private val metrics: MutableMap<FontKey, LineMetrics> = HashMap()

    /** Number of measurements currently held, widths and line metrics together. */
    val cacheSize: Int
        get() = widths.size + metrics.size

    /** Number of measurements answered from the cache instead of the helper node. */
    var cacheHits: Long = 0L
        private set

    /**
     * Width of a single word, without any surrounding space.
     *
     * @param style Style of the layout core the word is set in.
     * @param word Word to measure; it must not contain a line break.
     * @return Width in points, unrounded.
     */
    override fun wordWidth(style: TextStyle, word: String): Double =
        cachedWidth(FontKey.of(style), word)

    /**
     * Width of the space between two words of the given style.
     *
     * @param style Style of the layout core the words are set in.
     * @return Width in points, unrounded.
     */
    override fun spaceWidth(style: TextStyle): Double =
        cachedWidth(FontKey.of(style), SPACE)

    /**
     * Ascent, descent and leading of a line set in the given style.
     *
     * The line spacing of the style is not applied here: what is returned are the metrics of the bare
     * face, the way the layout core expects them.
     *
     * @param style Style of the layout core the line is set in.
     */
    override fun lineMetrics(style: TextStyle): LineMetrics =
        cachedMetrics(FontKey.of(style))

    /**
     * Width of a single word, without any surrounding space.
     *
     * @param font Font of the design the word is set in.
     * @param word Word to measure; it must not contain a line break.
     * @return Width in points, unrounded.
     */
    fun wordWidth(font: FontData, word: String): Double =
        cachedWidth(FontKey.of(font), word)

    /**
     * Width of the space between two words in the given font.
     *
     * @param font Font of the design the words are set in.
     * @return Width in points, unrounded.
     */
    fun spaceWidth(font: FontData): Double =
        cachedWidth(FontKey.of(font), SPACE)

    /**
     * Ascent, descent and leading of a line set in the given font.
     *
     * @param font Font of the design the line is set in.
     */
    fun lineMetrics(font: FontData): LineMetrics =
        cachedMetrics(FontKey.of(font))

    /** Drops every measurement taken so far, for instance after the font catalogue was rebuilt. */
    fun clearCache() {
        widths.clear()
        metrics.clear()
        cacheHits = 0L
    }

    private fun cachedMetrics(key: FontKey): LineMetrics {
        metrics[key]?.let { hit ->
            cacheHits++
            return hit
        }

        val resolved = FontResolver.font(key.toFontData())
        val single = boundsOf(resolved, REFERENCE_TEXT)
        val double = boundsOf(resolved, REFERENCE_TEXT + "\n" + REFERENCE_TEXT)

        // With the text origin on the baseline the bounds of a single line run from -ascent to
        // +descent, and the height a second line adds is the distance between two baselines.
        val ascent = -single.minY
        val descent = single.maxY
        val lineAdvance = double.height - single.height
        val leading = (lineAdvance - (ascent + descent)).coerceAtLeast(0.0)

        val measured = LineMetrics(ascent, descent, leading)
        metrics[key] = measured
        return measured
    }

    private fun cachedWidth(font: FontKey, text: String): Double {
        val key = WidthKey(font, text)
        widths[key]?.let { hit ->
            cacheHits++
            return hit
        }

        val measured = measureWidth(FontResolver.font(font.toFontData()), text)
        widths[key] = measured
        return measured
    }

    private fun measureWidth(font: Font, text: String): Double {
        prepare(font, text)
        return helper.prefWidth(-1.0)
    }

    private fun boundsOf(font: Font, text: String): Extent {
        prepare(font, text)
        val bounds = helper.layoutBounds
        return Extent(bounds.minY, bounds.maxY, bounds.height)
    }

    private fun prepare(font: Font, text: String) {
        helper.font = font
        helper.text = text
        helper.wrappingWidth = 0.0
    }

    /** Vertical extent of a laid out piece of text, measured against the baseline. */
    private data class Extent(val minY: Double, val maxY: Double, val height: Double)

    /** Identity of a font as far as measuring is concerned. */
    private data class FontKey(
        val family: String,
        val size: Int,
        val bold: Boolean,
        val italic: Boolean
    ) {

        /** The stored font this key stands for, the form the font resolution works on. */
        fun toFontData(): FontData =
            FontData(family, size, bold, italic)

        companion object {

            fun of(data: FontData): FontKey =
                FontKey(data.name, data.size, data.bold, data.italic)

            // A stored font carries whole points only, so the size of a layout style is cut down to
            // one; a style built from a design never carries a fraction in the first place.
            fun of(style: TextStyle): FontKey =
                FontKey(style.family, style.size.toInt(), style.bold, style.italic)
        }
    }

    /** Identity of one measured piece of text. */
    private data class WidthKey(val font: FontKey, val text: String)
}

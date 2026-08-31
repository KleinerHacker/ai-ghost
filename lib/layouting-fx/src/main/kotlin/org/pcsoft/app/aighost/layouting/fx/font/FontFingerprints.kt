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
import javafx.scene.text.Font
import javafx.scene.text.Text
import java.security.MessageDigest
import java.util.Locale
import kotlin.math.round

/**
 * Takes the [FontFingerprint] of an installed font family with JavaFX.
 *
 * **Reference text and size are fixed for all time.** A fingerprint is only ever compared against
 * another one, so changing either of them would turn every fingerprint taken so far into a false
 * mismatch. The reference text covers the Latin alphabet together with the letters commonly used
 * beside it - the accented and stroked forms of Western and Central Europe and the Cyrillic
 * alphabet - because a substituted family often differs in exactly those letters while the plain
 * ASCII part still matches.
 *
 * A character the family has no glyph for is measured all the same: the width of the notdef box is
 * part of what the family produces on this machine and therefore part of its identity.
 *
 * Everything is measured through a single hidden [Text] node of this object's own, held apart from
 * the one the layout measuring uses so a fingerprint never disturbs a running layout. Every family
 * is measured once and then remembered, because a fingerprint costs a few hundred measurements.
 *
 * **Threading:** a [Text] node belongs to the JavaFX application thread, so every method here must be
 * called on that thread. This object is *not* thread safe.
 */
object FontFingerprints {

    /** Size in points every fingerprint is taken at. Fixed for all time. */
    const val REFERENCE_SIZE: Double = 12.0

    /** Number of hexadecimal characters [FontFingerprint.widths] is cut down to. */
    private const val DIGEST_LENGTH: Int = 16

    /** Carries an ascender, a descender and no accents, so ascent and descent are both covered. */
    private const val METRICS_TEXT: String = "Hxpg"

    /** Number of decimal places a measured value is kept to, as places behind the point. */
    private const val PLACES: Int = 4

    /**
     * Characters every fingerprint is taken over, in this order. Fixed for all time.
     *
     * * printable ASCII, `U+0020` to `U+007E`
     * * Latin-1 letters, `U+00C0` to `U+00FF` without the two mathematical signs sitting in that
     *   block - French, Norwegian, Danish, German, Icelandic, Spanish, Portuguese
     * * Latin Extended-A, `U+0100` to `U+017F` - Polish, Czech, Hungarian, Turkish, Baltic
     * * Cyrillic, `U+0410` to `U+044F` plus the two forms of `Ё`
     */
    val REFERENCE_TEXT: String = buildString {
        (0x0020..0x007E).forEach { append(it.toChar()) }
        (0x00C0..0x00FF)
            .filter { it != 0x00D7 && it != 0x00F7 }
            .forEach { append(it.toChar()) }
        (0x0100..0x017F).forEach { append(it.toChar()) }
        (0x0410..0x044F).forEach { append(it.toChar()) }
        append('Ё')
        append('ё')
    }

    /**
     * The one and only helper node every measurement of this object runs through.
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

    private val fingerprints: MutableMap<String, FontFingerprint> = HashMap()

    /**
     * Fingerprint of an installed family.
     *
     * The family is measured as it is: neither the fallback chain nor any other substitution is
     * applied, because a fingerprint taken from a substitute would describe the wrong family.
     *
     * @param family Family name to measure, as the font catalogue of the platform spells it.
     * @return The fingerprint, or `null` when the family is not installed on this machine.
     */
    fun of(family: String): FontFingerprint? {
        fingerprints[family]?.let { return it }
        if (!isInstalled(family)) {
            return null
        }

        val font = Font.font(family, REFERENCE_SIZE)
        val (ascent, descent, leading) = metricsOf(font)
        val taken = FontFingerprint(digestOfWidths(font), ascent, descent, leading)
        fingerprints[family] = taken
        return taken
    }

    /** Drops every fingerprint taken so far, for instance after the font catalogue was rebuilt. */
    fun clearCache() {
        fingerprints.clear()
    }

    /**
     * Whether the platform knows the family under that name, compared without regard to case, the way
     * a font family is named.
     */
    private fun isInstalled(family: String): Boolean =
        Font.getFamilies().any { it.equals(family, ignoreCase = true) }

    /**
     * Digest over the width of every character of [REFERENCE_TEXT], measured one by one.
     *
     * Single characters rather than the whole string: a run of text carries the kerning between its
     * characters, which depends on the pairs that happen to stand next to each other, while a single
     * character is the plain advance the family defines.
     */
    private fun digestOfWidths(font: Font): String {
        val widths = REFERENCE_TEXT
            .map { character -> format(widthOf(font, character.toString())) }
            .joinToString(";")

        return MessageDigest.getInstance("SHA-256")
            .digest(widths.toByteArray(Charsets.UTF_8))
            .joinToString("") { byte -> "%02x".format(byte) }
            .take(DIGEST_LENGTH)
    }

    /** Ascent, descent and leading of the face, in the order [FontFingerprint] takes them. */
    private fun metricsOf(font: Font): Triple<Double, Double, Double> {
        prepare(font, METRICS_TEXT)
        val single = helper.layoutBounds
        val singleHeight = single.height
        // With the text origin on the baseline the bounds of a single line run from -ascent to
        // +descent, and the height a second line adds is the distance between two baselines.
        val ascent = -single.minY
        val descent = single.maxY

        prepare(font, METRICS_TEXT + "\n" + METRICS_TEXT)
        val lineAdvance = helper.layoutBounds.height - singleHeight
        val leading = (lineAdvance - (ascent + descent)).coerceAtLeast(0.0)

        return Triple(rounded(ascent), rounded(descent), rounded(leading))
    }

    private fun widthOf(font: Font, text: String): Double {
        prepare(font, text)
        return helper.prefWidth(-1.0)
    }

    private fun prepare(font: Font, text: String) {
        helper.font = font
        helper.text = text
        helper.wrappingWidth = 0.0
    }

    /** Cuts a measured value down to [PLACES] places, so a rounding difference is not a mismatch. */
    private fun rounded(value: Double): Double {
        val factor = Math.pow(10.0, PLACES.toDouble())
        return round(value * factor) / factor
    }

    /** The same value as text, with the decimal point of no language in particular. */
    private fun format(value: Double): String =
        String.format(Locale.ROOT, "%.${PLACES}f", value)
}

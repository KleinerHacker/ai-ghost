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

import javafx.scene.text.Font
import javafx.stage.Stage
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.testfx.framework.junit5.ApplicationTest
import org.testfx.util.WaitForAsyncUtils
import java.util.concurrent.Callable

/**
 * Developer tests of the font fingerprint: the reference it is taken over, the measurement itself and
 * the behaviour for a family that is not installed.
 */
class FontFingerprintsTest : ApplicationTest() {

    override fun start(stage: Stage) = Unit

    @BeforeEach
    fun clearFingerprints() {
        fx { FontFingerprints.clearCache() }
    }

    /**
     * The reference text is exactly the four blocks it is defined as: printable ASCII, the Latin-1
     * letters without the two mathematical signs sitting in that block, Latin Extended-A and the
     * Cyrillic alphabet with both forms of `Ё`. The text is compared as a whole, because a
     * fingerprint taken over a different text is not comparable to any fingerprint taken so far.
     */
    @Test
    fun `reference text carries the four fixed blocks`() {
        val expected = buildString {
            (0x0020..0x007E).forEach { append(it.toChar()) }
            (0x00C0..0x00FF)
                .filter { it != 0x00D7 && it != 0x00F7 }
                .forEach { append(it.toChar()) }
            (0x0100..0x017F).forEach { append(it.toChar()) }
            (0x0410..0x044F).forEach { append(it.toChar()) }
            append('Ё')
            append('ё')
        }

        assertEquals(expected, FontFingerprints.REFERENCE_TEXT, "The reference text must not change")
        assertFalse(FontFingerprints.REFERENCE_TEXT.contains('×'), "The times sign is left out")
        assertFalse(FontFingerprints.REFERENCE_TEXT.contains('÷'), "The division sign is left out")
    }

    /**
     * The letters the reference text exists for are really in it: the accented and stroked forms of
     * Western and Central Europe and the Cyrillic alphabet, which is where a substituted family
     * usually differs while the plain ASCII part still matches.
     */
    @Test
    fun `reference text carries the letters used beside the latin alphabet`() {
        val letters = "ÀÉÎÖÜßÆØÅÐÞŁŃŚŻČŘŠŽĂŐŰĮĶАБВГЯабвгяЁё"

        letters.forEach { letter ->
            assertTrue(
                FontFingerprints.REFERENCE_TEXT.contains(letter),
                "The reference text must carry $letter"
            )
        }
    }

    /** The size every fingerprint is taken at is fixed at twelve points and must never move. */
    @Test
    fun `reference size is twelve points`() {
        assertEquals(12.0, FontFingerprints.REFERENCE_SIZE, "The reference size must not change")
    }

    /**
     * A family the platform does not know has no fingerprint at all: nothing is measured with a
     * substitute, because that would describe the wrong family.
     */
    @Test
    fun `a family that is not installed has no fingerprint`() {
        val taken = fx { FontFingerprints.of("No Such Family At All") }

        assertNull(taken, "An unknown family must not be measured")
    }

    /**
     * An installed family is measured: the digest of the widths is the shortened hexadecimal form,
     * and the line metrics of a real face carry a positive ascent and descent.
     */
    @Test
    fun `an installed family is measured`() {
        val taken = fx { FontFingerprints.of(Font.getDefault().family) }

        assertNotNull(taken, "The default family is installed and must be measured")
        assertEquals(16, taken!!.widths.length, "The digest is cut down to sixteen characters")
        assertTrue(taken.widths.all { it in '0'..'9' || it in 'a'..'f' }, "The digest is hexadecimal")
        assertTrue(taken.ascent > 0.0, "A real face rises above the baseline")
        assertTrue(taken.descent > 0.0, "A real face reaches below the baseline")
        assertTrue(taken.leading >= 0.0, "The leading is never negative")
    }

    /**
     * The measurement is deterministic: the same family measured again after the cache was dropped
     * yields exactly the same fingerprint. Without this a document would report a substitution
     * against the very machine it was written on.
     */
    @Test
    fun `the same family is measured the same way again`() {
        val family = Font.getDefault().family

        val first = fx { FontFingerprints.of(family) }
        val second = fx {
            FontFingerprints.clearCache()
            FontFingerprints.of(family)
        }

        assertEquals(first, second, "A family must measure the same on every take")
    }

    /**
     * A family is measured once only: the second call answers with the fingerprint held from the
     * first one, because a fingerprint costs a few hundred measurements.
     */
    @Test
    fun `a measured family is remembered`() {
        val family = Font.getDefault().family

        val first = fx { FontFingerprints.of(family) }
        val second = fx { FontFingerprints.of(family) }

        assertSame(first, second, "The second call must be answered from the cache")
    }

    /**
     * Every measurement runs through the one helper node of this object, which is created once and
     * reused - building a node per measurement would be far too expensive for a few hundred
     * characters per family.
     */
    @Test
    fun `the hidden helper node is reused`() {
        val family = Font.getDefault().family

        val before = fx { FontFingerprints.helper }
        fx { FontFingerprints.of(family) }
        val after = fx { FontFingerprints.helper }

        assertSame(before, after, "The helper node must be the same one on every measurement")
        assertFalse(after.isVisible, "The helper node must never be seen")
    }

    /** Runs [block] on the JavaFX application thread and hands its result back. */
    private fun <T> fx(block: () -> T): T = WaitForAsyncUtils.asyncFx(Callable { block() }).get()
}

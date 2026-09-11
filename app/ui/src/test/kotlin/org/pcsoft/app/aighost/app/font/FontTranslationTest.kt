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

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.pcsoft.app.aighost.model.common.FontData
import org.pcsoft.framework.simplay.engine.model.FontFingerprint
import org.pcsoft.framework.simplay.engine.model.FontStyle
import org.pcsoft.framework.simplay.engine.model.FontWeight

/**
 * Developer tests for [toEngineFont], the copy from a stored [FontData] to the simPlay
 * [org.pcsoft.framework.simplay.engine.model.Font] `simplay-fx`'s `FxFontProbe` resolves and measures
 * with.
 */
class FontTranslationTest {

    /**
     * Use case: a font stored with non default values in every field reaches simPlay with exactly
     * those values - family and size are carried over as they are, and both switches of the cut
     * become simPlay's weight and style enums.
     */
    @Test
    fun everyFieldIsCarriedOverAsItIs() {
        val data = FontData(name = "Garamond", size = 17, bold = true, italic = true)

        val font = data.toEngineFont()

        assertEquals("Garamond", font.family)
        assertEquals(17.0, font.size)
        assertEquals(FontWeight.BOLD, font.weight)
        assertEquals(FontStyle.ITALIC, font.style)
    }

    /**
     * Use case: a plain body font with the defaults of [FontData] translates to a simPlay font that
     * carries the very same defaults.
     */
    @Test
    fun defaultsTranslateToTheSameDefaults() {
        val font = FontData().toEngineFont()

        assertEquals("Arial", font.family)
        assertEquals(12.0, font.size)
        assertEquals(FontWeight.NORMAL, font.weight)
        assertEquals(FontStyle.NORMAL, font.style)
    }

    /**
     * Use case: a font that was never fingerprinted carries no encoded fingerprint, so the simPlay
     * font translated from it carries none either.
     */
    @Test
    fun anAbsentFingerprintTranslatesToNone() {
        val font = FontData(name = "Georgia", size = 13).toEngineFont()

        assertNull(font.fingerprint)
    }

    /**
     * Use case: a stamped font carries its fingerprint as a single line of text, so the translation
     * decodes it back into simPlay's own fingerprint type instead of leaving it a string.
     */
    @Test
    fun aStoredFingerprintIsDecoded() {
        val fingerprint = FontFingerprint(
            normalizedSize = 100.0,
            ascent = 10.0,
            descent = 3.0,
            advances = listOf(1.0, 2.0, 3.0)
        )
        val data = FontData(name = "Georgia", size = 13, fingerprint = fingerprint.encode())

        val font = data.toEngineFont()

        assertEquals(fingerprint, font.fingerprint)
    }
}

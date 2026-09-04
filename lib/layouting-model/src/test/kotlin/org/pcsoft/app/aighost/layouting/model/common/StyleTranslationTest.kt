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

package org.pcsoft.app.aighost.layouting.model.common

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.pcsoft.app.aighost.layouting.TextAlignment
import org.pcsoft.app.aighost.layouting.TextStyle
import org.pcsoft.app.aighost.model.common.Alignment
import org.pcsoft.app.aighost.model.common.FontData
import org.pcsoft.app.aighost.model.common.StyleData

/**
 * Developer tests for the translation of a stored style into a layout style, [StyleData.toTextStyle].
 */
class StyleTranslationTest {

    /**
     * Use case: a stored style is handed to the layout core, and every part of the font - family,
     * size, weight and slant - together with the alignment and the line spacing arrives unchanged.
     */
    @Test
    fun everyPartOfTheStoredStyleIsCarriedOver() {
        val stored = StyleData(
            font = FontData(name = "Garamond", size = 14, bold = true, italic = true),
            textLineSpacing = 1.5,
            alignment = Alignment.CENTER
        )

        val translated = stored.toTextStyle()

        assertEquals(
            TextStyle(
                family = "Garamond",
                size = 14.0,
                bold = true,
                italic = true,
                alignment = TextAlignment.CENTER,
                lineSpacing = 1.5
            ),
            translated
        )
    }

    /**
     * Use case: the line spacing belongs to the stored style, the gaps around the block do not, so
     * the spacing is read from the style and the gaps are taken from the arguments.
     */
    @Test
    fun theLineSpacingComesFromTheStyleAndTheGapsFromTheArguments() {
        val translated = StyleData(textLineSpacing = 1.35).toTextStyle(
            spaceBefore = 8.0,
            spaceAfter = 4.0
        )

        assertEquals(1.35, translated.lineSpacing)
        assertEquals(8.0, translated.spaceBefore)
        assertEquals(4.0, translated.spaceAfter)
    }

    /**
     * Use case: a block left without gaps asks for none, so a caller that says nothing gets zero
     * instead of a hidden default.
     */
    @Test
    fun theGapsDefaultToNothing() {
        val translated = StyleData().toTextStyle()

        assertEquals(0.0, translated.spaceBefore)
        assertEquals(0.0, translated.spaceAfter)
    }

    /**
     * Use case: every alignment the user can store has its counterpart in the layout core, and the
     * block alignment of the document is the justification of the core.
     */
    @ParameterizedTest
    @CsvSource("LEFT,LEFT", "CENTER,CENTER", "RIGHT,RIGHT", "BLOCK,JUSTIFY")
    fun everyStoredAlignmentHasItsCounterpart(stored: Alignment, expected: TextAlignment) {
        assertEquals(expected, stored.toTextAlignment())
        assertEquals(expected, StyleData(alignment = stored).toTextStyle().alignment)
    }
}

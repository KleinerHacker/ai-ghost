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
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.pcsoft.app.aighost.model.common.Alignment
import org.pcsoft.app.aighost.model.common.FontData
import org.pcsoft.app.aighost.model.common.StyleData
import org.pcsoft.framework.simplay.engine.model.Font
import org.pcsoft.framework.simplay.engine.model.FontStyle
import org.pcsoft.framework.simplay.engine.model.FontWeight
import org.pcsoft.framework.simplay.engine.model.LineSpacing
import org.pcsoft.framework.simplay.engine.model.TextAlignment
import org.pcsoft.framework.simplay.engine.model.TextStyle

/**
 * Developer tests for the translation of a stored style into a simPlay layout style,
 * [StyleData.toTextStyle].
 */
class StyleTranslationTest {

    /**
     * Use case: a stored style is handed to the layout engine, and every part of the font - family,
     * size, weight and slant - together with the alignment and the line spacing arrives unchanged in
     * the simPlay style.
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
                font = Font(
                    family = "Garamond",
                    size = 14.0,
                    weight = FontWeight.BOLD,
                    style = FontStyle.ITALIC
                ),
                lineSpacing = LineSpacing(factor = 1.5),
                alignment = TextAlignment.CENTER
            ),
            translated
        )
    }

    /**
     * Use case: the stored line spacing is a plain factor, and it becomes the multiplicative factor
     * of the simPlay [LineSpacing] with no extra leading added.
     */
    @Test
    fun theLineSpacingFactorIsCarriedOver() {
        val translated = StyleData(textLineSpacing = 1.35).toTextStyle()

        assertEquals(LineSpacing(factor = 1.35), translated.lineSpacing)
    }

    /**
     * Use case: the raw model carries no font fingerprint - that is stamped by the UI where the
     * toolkit is available (IP-34) - so the translation always leaves it unset.
     */
    @Test
    fun theRawModelStyleCarriesNoFontFingerprint() {
        assertNull(StyleData().toTextStyle().font.fingerprint)
    }

    /**
     * Use case: the stored bold and slant flags are booleans; each maps to the matching constant of
     * the simPlay weight and style enums.
     */
    @ParameterizedTest
    @CsvSource(
        "false,false,NORMAL,NORMAL",
        "true,false,BOLD,NORMAL",
        "false,true,NORMAL,ITALIC",
        "true,true,BOLD,ITALIC"
    )
    fun theBoldAndSlantFlagsMapToTheEnums(
        bold: Boolean,
        italic: Boolean,
        expectedWeight: FontWeight,
        expectedStyle: FontStyle
    ) {
        val font = StyleData(font = FontData(bold = bold, italic = italic)).toTextStyle().font

        assertEquals(expectedWeight, font.weight)
        assertEquals(expectedStyle, font.style)
    }

    /**
     * Use case: every alignment the user can store has its counterpart in the layout engine, and the
     * block alignment of the document is the justification of the engine.
     */
    @ParameterizedTest
    @CsvSource("LEFT,LEFT", "CENTER,CENTER", "RIGHT,RIGHT", "BLOCK,JUSTIFY")
    fun everyStoredAlignmentHasItsCounterpart(stored: Alignment, expected: TextAlignment) {
        assertEquals(expected, stored.toTextAlignment())
        assertEquals(expected, StyleData(alignment = stored).toTextStyle().alignment)
    }
}

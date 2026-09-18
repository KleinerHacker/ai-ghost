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

import org.pcsoft.framework.simplay.engine.model.Font
import org.pcsoft.framework.simplay.engine.model.FontStyle
import org.pcsoft.framework.simplay.engine.model.FontWeight
import javafx.scene.text.Font as FxFont
import javafx.scene.text.FontPosture
import javafx.scene.text.FontWeight as FxFontWeight

/**
 * The one thing `simplay-fx`'s `FxFontProbe` does not answer: the name of the family the platform
 * actually set in place of a substituted or missing one.
 *
 * `FxFontProbe.checkAvailability` only classifies the outcome, it never names the family that was
 * resolved instead. This asks the JavaFX text stack the same question directly and reads the family
 * off the result, so a report can name what is set in place of what was asked for.
 *
 * **Threading:** resolves a JavaFX font, so it must run on the JavaFX application thread.
 */
object FontSubstitution {

    /** The family the platform actually resolves [font] to. */
    fun resolvedFamilyOf(font: Font): String {
        val weight = if (font.weight == FontWeight.BOLD) FxFontWeight.BOLD else FxFontWeight.NORMAL
        val posture = if (font.style == FontStyle.ITALIC) FontPosture.ITALIC else FontPosture.REGULAR
        return FxFont.font(font.family, weight, posture, font.size).family
    }
}

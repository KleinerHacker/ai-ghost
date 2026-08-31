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

package org.pcsoft.app.aighost.app.ui.component

import kotlin.math.abs

/**
 * A ready-made page size the user picks in the design project settings instead of typing width and
 * height by hand.
 *
 * The measures are kept in millimetres, the unit the dialog shows; the model is filled in points.
 * [CUSTOM] carries no measures - it stands for a page the user sizes freely.
 *
 * @property bundleKey key of the entry label in the message bundle
 * @property widthMm page width in millimetres, `null` for [CUSTOM]
 * @property heightMm page height in millimetres, `null` for [CUSTOM]
 */
enum class PagePreset(val bundleKey: String, val widthMm: Double?, val heightMm: Double?) {

    /** DIN A5. */
    A5("dialog.projectSettings.design.preset.a5", 148.0, 210.0),

    /** DIN A4. */
    A4("dialog.projectSettings.design.preset.a4", 210.0, 297.0),

    /** 12.5 cm x 19 cm, a common trade paperback. */
    TRADE_125_190("dialog.projectSettings.design.preset.trade125", 125.0, 190.0),

    /** 13.5 cm x 21.5 cm, a common trade paperback. */
    TRADE_135_215("dialog.projectSettings.design.preset.trade135", 135.0, 215.0),

    /** 6 in x 9 in, the US trade paperback. */
    US_TRADE_6_9("dialog.projectSettings.design.preset.usTrade", 152.4, 228.6),

    /** A freely sized page. */
    CUSTOM("dialog.projectSettings.design.preset.custom", null, null);

    companion object {

        /** How far a measure may sit off a preset and still count as that preset, in millimetres. */
        private const val TOLERANCE_MM: Double = 0.5

        /**
         * The preset matching [widthMm] x [heightMm] within half a millimetre, or [CUSTOM] when none
         * does.
         *
         * @param widthMm page width in millimetres
         * @param heightMm page height in millimetres
         */
        fun match(widthMm: Double, heightMm: Double): PagePreset =
            entries.firstOrNull {
                it.widthMm != null && it.heightMm != null &&
                        abs(it.widthMm - widthMm) <= TOLERANCE_MM && abs(it.heightMm - heightMm) <= TOLERANCE_MM
            } ?: CUSTOM
    }
}

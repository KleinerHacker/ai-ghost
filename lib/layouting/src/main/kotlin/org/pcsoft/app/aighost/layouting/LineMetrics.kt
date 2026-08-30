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

package org.pcsoft.app.aighost.layouting

/**
 * Vertical metrics of a single line set in one font.
 *
 * The values are fractional and are never rounded: rounding is right when a control asks for its
 * preferred size, but it makes line breaking coarse and size dependent, so whole pixels are only
 * made at painting time.
 *
 * @property ascent Distance from the baseline up to the top of the tallest glyph.
 * @property descent Distance from the baseline down to the bottom of the deepest glyph.
 * @property leading Additional gap the font asks for between two lines.
 */
data class LineMetrics(
    val ascent: Double,
    val descent: Double,
    val leading: Double
) {

    /** Distance from one baseline to the next when the lines are set without extra spacing. */
    val lineHeight: Double
        get() = ascent + descent + leading
}

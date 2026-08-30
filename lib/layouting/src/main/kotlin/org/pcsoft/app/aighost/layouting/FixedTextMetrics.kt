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
 * A [TextMetrics] that answers with numbers anybody can predict.
 *
 * Every character is as wide as [advance] times the font size, so the width of a word is its length
 * times that value: with the default factors a word of five characters in a size of ten points is
 * exactly 25 points wide. Ascent, descent and leading follow the size in the same way.
 *
 * The implementation ships in the main source set on purpose. It is not a test double of the layout
 * core alone: everything that has to lay text out without a toolkit at hand - a check, a benchmark,
 * a tool - measures against it, and a test then reads a layout as plain arithmetic instead of as the
 * numbers of whatever font the machine happens to have installed.
 *
 * The family and the cut of a style are ignored; the size is the only thing that matters.
 *
 * @property advance Width of one character as a factor on the font size.
 * @property ascentFactor Ascent as a factor on the font size.
 * @property descentFactor Descent as a factor on the font size.
 * @property leadingFactor Leading as a factor on the font size.
 */
data class FixedTextMetrics(
    val advance: Double = 0.5,
    val ascentFactor: Double = 0.8,
    val descentFactor: Double = 0.2,
    val leadingFactor: Double = 0.0
) : TextMetrics {

    override fun wordWidth(style: TextStyle, word: String): Double =
        word.length * advance * style.size

    override fun spaceWidth(style: TextStyle): Double =
        advance * style.size

    override fun lineMetrics(style: TextStyle): LineMetrics =
        LineMetrics(
            ascent = ascentFactor * style.size,
            descent = descentFactor * style.size,
            leading = leadingFactor * style.size
        )
}

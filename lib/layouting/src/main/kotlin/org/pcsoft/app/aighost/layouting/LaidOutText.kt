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
 * A single line as it was set, in numbers only.
 *
 * The type carries no toolkit type at all - no font, no colour, no node - so the same result can be
 * drawn on a canvas, written into a document or simply asserted on in a test.
 *
 * [blockIndex] together with [charStart] and [charEnd] maps the line back onto its input: they name
 * the block of the broken sequence and the half open range of [TextBlock.text] this line was set
 * from. That is what turns a click on the page back into a position in the manuscript.
 *
 * A justified line is not drawn as one string: its words are separated by [wordSpacing] instead of
 * by the plain space of the font. [text] is the content of the line, carrying a space exactly where
 * the input had one, while the geometry of the gaps is [wordSpacing].
 *
 * @property x Left edge of the line inside the column.
 * @property y Top edge of the line, the ascent above its baseline.
 * @property baseline Baseline of the line, `y` plus the ascent of its style.
 * @property width Width the line actually occupies, gaps included.
 * @property text Text of the line, with a space where the input separated two words by one.
 * @property style Style the line is set in.
 * @property blockIndex Index of the block this line was set from.
 * @property charStart First character of the line inside the text of its block, inclusive.
 * @property charEnd Character behind the line inside the text of its block, exclusive.
 * @property wordSpacing Distance between two words of this line, in points.
 */
data class LaidOutLine(
    val x: Double,
    val y: Double,
    val baseline: Double,
    val width: Double,
    val text: String,
    val style: TextStyle,
    val blockIndex: Int,
    val charStart: Int,
    val charEnd: Int,
    val wordSpacing: Double
)

/**
 * The complete result of breaking a sequence of blocks against a column.
 *
 * @property lines Lines in the order they are set, from the first block to the last.
 * @property columnWidth Width the blocks were broken against.
 * @property height Vertical space the whole result takes, the last gap below included.
 */
data class LaidOutText(
    val lines: List<LaidOutLine>,
    val columnWidth: Double,
    val height: Double
)

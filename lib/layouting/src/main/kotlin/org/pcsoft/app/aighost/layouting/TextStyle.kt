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
 * Horizontal placement of a set line inside its column.
 *
 * The layout core carries its own alignment on purpose: it must stay usable without the document
 * model, so it never refers to the alignment stored in a design.
 */
enum class TextAlignment {

    /** Lines start at the left edge of the column, the right edge stays ragged. */
    LEFT,

    /** Lines are centred inside the column. */
    CENTER,

    /** Lines end at the right edge of the column, the left edge stays ragged. */
    RIGHT,

    /** Lines are stretched over the full column width, the last line of a block stays left. */
    JUSTIFY
}

/**
 * Everything the layout core needs to know about how a block of text is set.
 *
 * The type is deliberately flat and free of any document model: a family name, a size and the two
 * switches of the cut are enough to identify a face for measuring, the alignment and the line
 * spacing describe how the lines are placed, and the two gaps say how much empty space the block
 * asks for above and below itself.
 *
 * A [lineSpacing] is a factor on the line height the font asks for: `1.0` sets the lines as tightly
 * as the font wants, a larger value spreads them apart.
 *
 * @property family Family name of the face the text is set in.
 * @property size Size of the face in points.
 * @property bold Whether the text is set in a bold weight.
 * @property italic Whether the text is set slanted.
 * @property alignment Horizontal placement of the lines inside the column.
 * @property lineSpacing Factor on the line height of the face, `1.0` by default.
 * @property spaceBefore Empty space above the block in points, `0.0` by default.
 * @property spaceAfter Empty space below the block in points, `0.0` by default.
 */
data class TextStyle(
    val family: String,
    val size: Double,
    val bold: Boolean = false,
    val italic: Boolean = false,
    val alignment: TextAlignment = TextAlignment.LEFT,
    val lineSpacing: Double = 1.0,
    val spaceBefore: Double = 0.0,
    val spaceAfter: Double = 0.0
)

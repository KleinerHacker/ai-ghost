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

import org.pcsoft.app.aighost.layouting.TextAlignment
import org.pcsoft.app.aighost.layouting.TextStyle
import org.pcsoft.app.aighost.model.common.Alignment
import org.pcsoft.app.aighost.model.common.StyleData

/**
 * The one place where a stored style becomes a style the layout core understands.
 *
 * The two types look alike but belong to different worlds: [StyleData] is what the user edits and
 * what is written to disk, [TextStyle] is what a line is set with. The line spacing is part of the
 * stored style and is carried over; the gaps around a block are not stored anywhere, so they are
 * handed in separately. The translation is written as an extension of the stored type, the same way
 * the font translations are.
 */

/**
 * Translates a stored style into a layout style.
 *
 * @receiver Stored style of the element, line spacing included.
 * @param spaceBefore Empty space above the block in points.
 * @param spaceAfter Empty space below the block in points.
 */
fun StyleData.toTextStyle(
    spaceBefore: Double = 0.0,
    spaceAfter: Double = 0.0
): TextStyle =
    TextStyle(
        family = font.name,
        size = font.size.toDouble(),
        bold = font.bold,
        italic = font.italic,
        alignment = alignment.toTextAlignment(),
        lineSpacing = textLineSpacing,
        spaceBefore = spaceBefore,
        spaceAfter = spaceAfter
    )

/**
 * Translates the stored alignment into the alignment of the layout core.
 *
 * @receiver Alignment as it is stored in the design.
 */
fun Alignment.toTextAlignment(): TextAlignment =
    when (this) {
        Alignment.LEFT -> TextAlignment.LEFT
        Alignment.CENTER -> TextAlignment.CENTER
        Alignment.RIGHT -> TextAlignment.RIGHT
        Alignment.BLOCK -> TextAlignment.JUSTIFY
    }

/**
 * The gaps a built block asks for above and below itself.
 *
 * None of these numbers is stored in the document: the design says how a text looks, not how far a
 * heading stands from the paragraph below it. Until that becomes a setting of its own the values are
 * fixed here, in one place, so every builder spaces its blocks the same way.
 *
 * The values are factors on nothing and are plain points, because a gap between a heading and a
 * paragraph must not grow with the size of the heading alone.
 */
object BlockSpacing {

    /** Space below the main title of the title page. */
    const val AFTER_TITLE: Double = 12.0

    /** Space below the last additional title line. */
    const val AFTER_TITLE_APPENDIX: Double = 24.0

    /** Space above the author name on the title page. */
    const val BEFORE_AUTHOR: Double = 36.0

    /** Space above the heading of a written part. */
    const val BEFORE_PART_TITLE: Double = 24.0

    /** Space below the heading of a written part. */
    const val AFTER_PART_TITLE: Double = 12.0

    /** Space below a paragraph of body text. */
    const val AFTER_PARAGRAPH: Double = 6.0
}

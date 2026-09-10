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

import org.pcsoft.app.aighost.model.common.Alignment
import org.pcsoft.app.aighost.model.common.StyleData
import org.pcsoft.framework.simplay.engine.model.Font
import org.pcsoft.framework.simplay.engine.model.FontStyle
import org.pcsoft.framework.simplay.engine.model.FontWeight
import org.pcsoft.framework.simplay.engine.model.LineSpacing
import org.pcsoft.framework.simplay.engine.model.TextAlignment
import org.pcsoft.framework.simplay.engine.model.TextStyle

/**
 * The one place where a stored style becomes a style the simPlay layout engine understands.
 *
 * The two types look alike but belong to different worlds: [StyleData] is what the user edits and
 * what is written to disk, [TextStyle] is what a block is set with. The stored bold and slant flags
 * become the [FontWeight] and [FontStyle] enums of simPlay; the line spacing factor becomes a
 * [LineSpacing].
 *
 * simPlay's [TextStyle] carries no gaps around a block - the ai-ghost spacing between a heading and
 * the paragraph below it, and between two paragraphs, has no counterpart in the raw model. That
 * spacing is part of the page policy simPlay does not own yet.
 * TODO(simPlay page policy): re-introduce the inter-block gaps once simPlay expresses them. The
 *  fixed point values used before were: after book title 12, after last title line 24, before the
 *  author 36, before a part heading 24, after a part heading 12, after a paragraph 6 (all in points).
 */

/**
 * Translates a stored style into a simPlay layout style.
 *
 * @receiver Stored style of the element, line spacing included.
 */
fun StyleData.toTextStyle(): TextStyle =
    TextStyle(
        font = Font(
            family = font.name,
            size = font.size.toDouble(),
            weight = if (font.bold) FontWeight.BOLD else FontWeight.NORMAL,
            style = if (font.italic) FontStyle.ITALIC else FontStyle.NORMAL,
            // IP-34: app/ui stamps Font.fingerprint from its own font probe; the raw model builder
            // never sets it, so a substitution is only flagged where the toolkit is available.
            fingerprint = null,
        ),
        lineSpacing = LineSpacing(factor = textLineSpacing),
        alignment = alignment.toTextAlignment(),
    )

/**
 * Translates the stored alignment into the alignment of the simPlay layout model.
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

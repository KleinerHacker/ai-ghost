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

package org.pcsoft.app.aighost.layouting.model.project.book

import org.pcsoft.app.aighost.layouting.TextBlock
import org.pcsoft.app.aighost.layouting.model.common.BlockSpacing
import org.pcsoft.app.aighost.layouting.model.common.StyleTranslation
import org.pcsoft.app.aighost.model.project.book.BookPart
import org.pcsoft.app.aighost.model.project.design.BookPartPageDesign

/**
 * Builds the blocks of a written part - a prolog, a chapter or an epilog.
 *
 * All three carry the same shape, so all three are built here: the heading, the further heading lines
 * and the paragraphs. The heading is styled by the part's page design, the paragraphs by its text
 * style; the caller passes the page design that belongs to the part.
 *
 * An empty heading is left out, an empty paragraph is **not**: the user put it there and it keeps its
 * line on the page.
 */
object BookPartBuilder {

    /**
     * Builds one written part.
     *
     * @param part Part the heading and the paragraphs are taken from.
     * @param pageDesign Page design of the part - the styles of the heading, its further lines and the text.
     * @return The blocks in the order they are set.
     */
    fun build(part: BookPart, pageDesign: BookPartPageDesign): List<TextBlock> {
        val blocks = ArrayList<TextBlock>()

        val appendix = part.titleAppendix.filter { it.isNotBlank() }

        if (part.title.isNotBlank()) {
            blocks += TextBlock(
                text = part.title,
                style = StyleTranslation.toTextStyle(
                    style = pageDesign.titleStyle,
                    spaceBefore = BlockSpacing.BEFORE_PART_TITLE,
                    spaceAfter = if (appendix.isEmpty()) BlockSpacing.AFTER_PART_TITLE else 0.0
                )
            )
        }

        appendix.forEachIndexed { index, line ->
            blocks += TextBlock(
                text = line,
                style = StyleTranslation.toTextStyle(
                    style = pageDesign.titleAppendixStyle,
                    spaceBefore = if (index == 0 && part.title.isBlank()) BlockSpacing.BEFORE_PART_TITLE else 0.0,
                    spaceAfter = if (index == appendix.lastIndex) BlockSpacing.AFTER_PART_TITLE else 0.0
                )
            )
        }

        val textStyle = StyleTranslation.toTextStyle(
            style = pageDesign.textStyle,
            spaceAfter = BlockSpacing.AFTER_PARAGRAPH
        )
        part.paragraph.forEach { paragraph ->
            blocks += TextBlock(text = paragraph, style = textStyle)
        }

        return blocks
    }
}

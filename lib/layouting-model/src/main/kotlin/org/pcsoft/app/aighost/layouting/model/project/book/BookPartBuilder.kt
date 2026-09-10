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

import org.pcsoft.app.aighost.layouting.model.common.toTextStyle
import org.pcsoft.app.aighost.model.project.book.BookPart
import org.pcsoft.app.aighost.model.project.design.BookPartPageDesign
import org.pcsoft.framework.simplay.engine.model.TextBlock

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

        if (part.title.isNotBlank()) {
            blocks += TextBlock.of(part.title, pageDesign.titleStyle.toTextStyle())
        }

        val appendixStyle = pageDesign.titleAppendixStyle.toTextStyle()
        part.titleAppendix.filter { it.isNotBlank() }.forEach { line ->
            blocks += TextBlock.of(line, appendixStyle)
        }

        val textStyle = pageDesign.textStyle.toTextStyle()
        part.paragraph.forEach { paragraph ->
            blocks += TextBlock.of(paragraph, textStyle)
        }

        return blocks
    }
}

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

package org.pcsoft.app.aighost.layouting.model.project.meta

import org.pcsoft.app.aighost.layouting.TextBlock
import org.pcsoft.app.aighost.layouting.model.common.BlockSpacing
import org.pcsoft.app.aighost.layouting.model.common.StyleTranslation
import org.pcsoft.app.aighost.model.project.book.Copyright
import org.pcsoft.app.aighost.model.project.design.Design
import org.pcsoft.app.aighost.model.project.meta.Meta

/**
 * Builds the blocks of the copyright page.
 *
 * The notice is one text the user typed and it may carry line breaks of its own. A block never does,
 * so the text is cut at its breaks and every part becomes a block. The further copyright lines follow
 * below it, and the author name closes the page when the design asks for it.
 *
 * Whether the page is printed at all is a switch of the copyright itself: a page that does not belong
 * to the book gives no block.
 */
object CopyrightPageBuilder {

    /**
     * Builds the copyright page.
     *
     * @param copyright Copyright page of the book - the notice, its further lines and the switch.
     * @param meta Meta data the author name is taken from.
     * @param design Design the copyright page styles are taken from.
     * @return The blocks in the order they are set, empty when the page does not belong to the book.
     */
    fun build(copyright: Copyright, meta: Meta, design: Design): List<TextBlock> {
        if (!copyright.included) {
            return emptyList()
        }

        val copyrightPage = design.copyrightPage
        val blocks = ArrayList<TextBlock>()

        if (copyright.copyright.isNotBlank()) {
            val noticeStyle = StyleTranslation.toTextStyle(
                style = copyrightPage.copyrightStyle,
                spaceAfter = BlockSpacing.AFTER_PARAGRAPH
            )
            copyright.copyright.lines().forEach { line ->
                blocks += TextBlock(text = line, style = noticeStyle)
            }
        }

        val appendixStyle = StyleTranslation.toTextStyle(
            style = copyrightPage.copyrightAppendixStyle,
            spaceAfter = BlockSpacing.AFTER_PARAGRAPH
        )
        copyright.copyrightAppendix.filter { it.isNotBlank() }.forEach { line ->
            blocks += TextBlock(text = line, style = appendixStyle)
        }

        if (copyrightPage.showAuthor && meta.author.isNotBlank()) {
            blocks += TextBlock(
                text = meta.author,
                style = StyleTranslation.toTextStyle(
                    style = copyrightPage.authorStyle,
                    spaceBefore = BlockSpacing.BEFORE_AUTHOR
                )
            )
        }

        return blocks
    }
}

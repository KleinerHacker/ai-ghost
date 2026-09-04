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
import org.pcsoft.app.aighost.layouting.model.common.toTextStyle
import org.pcsoft.app.aighost.model.project.book.Blurb
import org.pcsoft.app.aighost.model.project.design.Design

/**
 * Builds the blocks of the blurb.
 *
 * The blurb carries no heading of its own, so it is its paragraphs and nothing else. They are set in
 * the blurb page design, the same way the body of a chapter is.
 */
object BlurbBuilder {

    /**
     * Builds the blurb.
     *
     * @param blurb Blurb the paragraphs are taken from.
     * @param design Design the blurb page style is taken from.
     * @return The blocks in the order they are set.
     */
    fun build(blurb: Blurb, design: Design): List<TextBlock> {
        val style = design.blurbPage.textStyle.toTextStyle(
            spaceAfter = BlockSpacing.AFTER_PARAGRAPH
        )

        return blurb.paragraph.map { paragraph -> TextBlock(text = paragraph, style = style) }
    }
}

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
import org.pcsoft.app.aighost.model.project.design.Design
import org.pcsoft.app.aighost.model.project.meta.Meta

/**
 * Builds the blocks of the copyright page.
 *
 * The copyright is one text the user typed and it may carry line breaks of its own. A block never
 * does, so the text is cut at its breaks and every part becomes a block: what the user typed on its
 * own line is set on its own line.
 */
object CopyrightPageBuilder {

    /**
     * Builds the copyright page.
     *
     * @param meta Meta data the copyright text is taken from.
     * @param design Design the style and the line spacing are taken from.
     * @return The blocks in the order they are set, empty when nothing was typed.
     */
    fun build(meta: Meta, design: Design): List<TextBlock> {
        if (meta.copyright.isBlank()) {
            return emptyList()
        }

        val style = StyleTranslation.toTextStyle(
            style = design.copyrightDesign.style,
            lineSpacing = design.copyrightLineSpacing,
            spaceAfter = BlockSpacing.AFTER_PARAGRAPH
        )

        return meta.copyright.lines().map { line -> TextBlock(text = line, style = style) }
    }
}

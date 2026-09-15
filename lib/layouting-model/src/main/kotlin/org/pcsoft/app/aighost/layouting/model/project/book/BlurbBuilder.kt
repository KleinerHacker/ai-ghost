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
import org.pcsoft.app.aighost.model.project.book.Blurb
import org.pcsoft.app.aighost.model.project.design.Design
import org.pcsoft.framework.simplay.engine.model.TextBlock

/** Stable id of the blurb's anchor, matching [org.pcsoft.app.aighost.layouting.model.project.BookDocumentBuilder]'s page id. */
internal const val BLURB_ANCHOR_ID = "blurb"

/**
 * Builds the blocks of the blurb.
 *
 * Unlike a prolog, a chapter or an epilog, the blurb's text still lives on [Blurb.paragraph] itself -
 * IP-36 never touched it - so this builder keeps building fresh from the model on every call instead
 * of reading an existing page back. [withAnchor] only marks the blurb's page with its `"blurb"` anchor
 * for the whole-book [org.pcsoft.app.aighost.layouting.model.project.BookDocumentBuilder.build]; the
 * single-part writing surface (`BookPartEditorController`) keeps editing plain paragraph text and
 * must never see the anchor token mixed into it, so it always calls this with the default `false`.
 */
object BlurbBuilder {

    /**
     * Builds the blurb.
     *
     * @param blurb Blurb the paragraphs are taken from.
     * @param design Design the blurb page style is taken from.
     * @param withAnchor Whether the `${blurb}` anchor token is embedded at the start of the first
     * block - `true` only for the whole-book document, `false` (the default) for the writing surface.
     * @return The blocks in the order they are set; with [withAnchor] set, never empty even when
     * [blurb] carries no paragraph yet.
     */
    fun build(blurb: Blurb, design: Design, withAnchor: Boolean = false): List<TextBlock> {
        val style = design.blurbPage.textStyle.toTextStyle()

        if (blurb.paragraph.isEmpty()) {
            return if (withAnchor) listOf(TextBlock.of("\${$BLURB_ANCHOR_ID}", style)) else emptyList()
        }

        return blurb.paragraph.mapIndexed { index, paragraph ->
            val text = if (withAnchor && index == 0) "\${$BLURB_ANCHOR_ID}$paragraph" else paragraph
            TextBlock.of(text, style)
        }
    }
}

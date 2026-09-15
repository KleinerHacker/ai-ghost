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
import org.pcsoft.app.aighost.model.project.book.Book
import org.pcsoft.app.aighost.model.project.design.Design
import org.pcsoft.app.aighost.model.project.meta.Meta
import org.pcsoft.framework.simplay.engine.model.Document
import org.pcsoft.framework.simplay.engine.model.TextBlock

/** Stable id of the title page's anchor, matching [org.pcsoft.app.aighost.layouting.model.project.BookDocumentBuilder]'s page id. */
internal const val TITLE_ANCHOR_ID = "title"

/**
 * Builds the blocks of the title page.
 *
 * IP-36 removed the main title and its further lines from [Book]; that text lives only in the book's
 * simPlay [Document] since IP-38, addressed through the `"title"` anchor and read back via
 * [BookPartBuilder]. The author name still lives in [Meta] and is built fresh on every call, appended
 * after the anchor-addressed title blocks.
 */
object TitlePageBuilder {

    /**
     * Builds the title page.
     *
     * @param document The book's document, searched for the `"title"` anchor's page.
     * @param meta Meta data the author name is taken from.
     * @param design Design the title page styles are taken from.
     * @return The title's anchor-addressed blocks, followed by the author block when the design shows
     * it and one was typed.
     */
    fun build(document: Document, meta: Meta, design: Design): List<TextBlock> {
        val titlePage = design.titlePage
        val blocks = ArrayList<TextBlock>()

        blocks += BookPartBuilder.build(document, TITLE_ANCHOR_ID, titlePage.titleStyle.toTextStyle())

        if (titlePage.showAuthor && meta.author.isNotBlank()) {
            blocks += TextBlock.of(meta.author, titlePage.authorStyle.toTextStyle())
        }

        return blocks
    }
}

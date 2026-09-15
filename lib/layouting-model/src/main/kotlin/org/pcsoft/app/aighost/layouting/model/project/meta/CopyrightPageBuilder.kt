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

import org.pcsoft.app.aighost.layouting.model.common.toTextStyle
import org.pcsoft.app.aighost.layouting.model.project.book.BookPartBuilder
import org.pcsoft.app.aighost.model.project.book.Copyright
import org.pcsoft.app.aighost.model.project.design.Design
import org.pcsoft.app.aighost.model.project.meta.Meta
import org.pcsoft.framework.simplay.engine.model.Document
import org.pcsoft.framework.simplay.engine.model.TextBlock

/** Stable id of the copyright page's anchor, matching [org.pcsoft.app.aighost.layouting.model.project.BookDocumentBuilder]'s page id. */
internal const val COPYRIGHT_ANCHOR_ID = "copyright"

/**
 * Builds the blocks of the copyright page.
 *
 * IP-36 removed the copyright notice and its further lines from [Copyright]; that text lives only in
 * the book's simPlay [Document] since IP-38, addressed through the `"copyright"` anchor and read back
 * via [BookPartBuilder]. The author name still lives in [Meta] and is built fresh on every call,
 * appended after the anchor-addressed notice blocks - but only while the page still belongs to the
 * book.
 */
object CopyrightPageBuilder {

    /**
     * Builds the copyright page.
     *
     * @param document The book's document, searched for the `"copyright"` anchor's page.
     * @param copyright Copyright page of the book - only its switch matters here, its text lives in
     * [document].
     * @param meta Meta data the author name is taken from.
     * @param design Design the copyright page styles are taken from.
     * @return Empty when the page is not included; otherwise the notice's anchor-addressed blocks,
     * followed by the author block when the design shows it and one was typed.
     */
    fun build(document: Document, copyright: Copyright, meta: Meta, design: Design): List<TextBlock> {
        if (!copyright.included) {
            return emptyList()
        }

        val copyrightPage = design.copyrightPage
        val blocks = ArrayList<TextBlock>()

        blocks += BookPartBuilder.build(document, COPYRIGHT_ANCHOR_ID, copyrightPage.copyrightStyle.toTextStyle())

        if (copyrightPage.showAuthor && meta.author.isNotBlank()) {
            blocks += TextBlock.of(meta.author, copyrightPage.authorStyle.toTextStyle())
        }

        return blocks
    }
}

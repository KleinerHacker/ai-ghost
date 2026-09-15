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
import org.pcsoft.framework.simplay.engine.model.TextBlock

/**
 * Builds the blocks of the title page.
 *
 * IP-36 removed the main title and its further lines from [Book]: that text now lives only in the
 * simPlay `Document` the book carries (IP-37/IP-38), addressed through the title's anchor. Until
 * IP-38 rebuilds this builder around that anchor, only the author name - which still lives in
 * [Meta] - is built here.
 */
object TitlePageBuilder {

    /**
     * Builds the title page.
     *
     * @param book Book the title page belongs to - no longer a source of text, kept for the future
     * anchor lookup of IP-38.
     * @param meta Meta data the author name is taken from.
     * @param design Design the title page styles are taken from.
     * @return The author block, when the design shows it and one was typed; otherwise empty until
     * IP-38 rebuilds this method around the title's anchor in the book's `Document`.
     */
    fun build(book: Book, meta: Meta, design: Design): List<TextBlock> {
        // TODO(IP-38): read the title and its further lines from the book's Document through the
        //  "title" anchor id instead.
        val blocks = ArrayList<TextBlock>()
        val titlePage = design.titlePage

        if (titlePage.showAuthor && meta.author.isNotBlank()) {
            blocks += TextBlock.of(meta.author, titlePage.authorStyle.toTextStyle())
        }

        return blocks
    }
}

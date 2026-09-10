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
 * The page is the title of the book, the further title lines below it and the author name from the
 * meta data. Everything that was left empty is left out: a book without additional title lines gets
 * no empty blocks that would only push the author down the page.
 */
object TitlePageBuilder {

    /**
     * Builds the title page.
     *
     * @param book Book the title and its further lines are taken from.
     * @param meta Meta data the author name is taken from.
     * @param design Design the title page styles are taken from.
     * @return The blocks in the order they are set.
     */
    fun build(book: Book, meta: Meta, design: Design): List<TextBlock> {
        val blocks = ArrayList<TextBlock>()
        val titlePage = design.titlePage

        if (book.title.isNotBlank()) {
            blocks += TextBlock.of(book.title, titlePage.titleStyle.toTextStyle())
        }

        val appendixStyle = titlePage.titleAppendixStyle.toTextStyle()
        book.titleAppendix.filter { it.isNotBlank() }.forEach { line ->
            blocks += TextBlock.of(line, appendixStyle)
        }

        if (titlePage.showAuthor && meta.author.isNotBlank()) {
            blocks += TextBlock.of(meta.author, titlePage.authorStyle.toTextStyle())
        }

        return blocks
    }
}

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

package org.pcsoft.app.aighost.layouting.model.project

import org.pcsoft.app.aighost.layouting.model.common.toTextStyle
import org.pcsoft.app.aighost.model.project.book.Book
import org.pcsoft.app.aighost.model.project.design.Design
import org.pcsoft.framework.simplay.engine.model.Document
import org.pcsoft.framework.simplay.engine.model.FlowPage
import org.pcsoft.framework.simplay.engine.model.Page
import org.pcsoft.framework.simplay.engine.model.SinglePage
import org.pcsoft.framework.simplay.engine.model.TextStyle

/**
 * Restyles [Book.document] after a design change, without rebuilding a single page or moving the
 * caret.
 *
 * A design change only ever replaces the [TextStyle] every block of a page is set in; the text and
 * every `TextAnchor` inside it stay exactly as they are. [BookDocumentBuilder] rebuilds a page from
 * scratch and would lose that guarantee - typing position, undo history, everything - for the sake of
 * a font change, which is why this is a separate, narrower operation: it walks every page of
 * [Book.document] already there, decides which design style belongs to it by its stable page id
 * (`"prolog"`, `"epilog"`, `"blurb"` or a chapter's [org.pcsoft.app.aighost.model.project.book.Chapter.id]),
 * and swaps the style of every block on a matching page via [org.pcsoft.framework.simplay.engine.model.TextBlock.withStyle].
 * The title and copyright page are left untouched: their only anchor-addressed content is a still
 * unwritten seed block, and their author block is rebuilt fresh from [Design] on every read anyway.
 */
object DocumentStyleRefresher {

    /**
     * Restyles every prolog, chapter, epilog and blurb page of [book]'s document to [design].
     *
     * @param book The manuscript whose [Book.document] is restyled.
     * @param design The design the new style of every block is taken from.
     * @return A copy of [book]'s document with every matching page's blocks restyled; a page this
     * function does not recognize a role for is returned unchanged.
     */
    fun refresh(book: Book, design: Design): Document {
        val chapterIds = book.chapters.mapTo(HashSet()) { it.id.toString() }

        val pages = book.document.pages.map { page ->
            val style = styleFor(page.id, chapterIds, design) ?: return@map page
            page.withBlocksRestyled(style)
        }

        return book.document.copy(pages = pages)
    }

    private fun styleFor(pageId: String, chapterIds: Set<String>, design: Design): TextStyle? = when {
        pageId == "prolog" -> design.prologPage.textStyle.toTextStyle()
        pageId == "epilog" -> design.epilogPage.textStyle.toTextStyle()
        pageId == "blurb" -> design.blurbPage.textStyle.toTextStyle()
        pageId in chapterIds -> design.chapterPage.textStyle.toTextStyle()
        else -> null
    }

    private fun Page.withBlocksRestyled(style: TextStyle): Page {
        val restyled = blocks.map { it.withStyle(style) }
        return when (this) {
            is FlowPage -> copy(blocks = restyled)
            is SinglePage -> copy(blocks = restyled)
        }
    }
}

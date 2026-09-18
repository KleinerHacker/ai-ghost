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

import org.pcsoft.app.aighost.layouting.model.common.toPageLayout
import org.pcsoft.app.aighost.layouting.model.common.toPageNumbering
import org.pcsoft.app.aighost.layouting.model.common.toTextStyle
import org.pcsoft.app.aighost.layouting.model.project.book.BlurbBuilder
import org.pcsoft.app.aighost.layouting.model.project.book.BookPartBuilder
import org.pcsoft.app.aighost.layouting.model.project.book.TitlePageBuilder
import org.pcsoft.app.aighost.layouting.model.project.meta.CopyrightPageBuilder
import org.pcsoft.app.aighost.model.project.book.Book
import org.pcsoft.app.aighost.model.project.design.Design
import org.pcsoft.app.aighost.model.project.meta.Meta
import org.pcsoft.framework.simplay.engine.model.Document
import org.pcsoft.framework.simplay.engine.model.FlowPage
import org.pcsoft.framework.simplay.engine.model.Page
import org.pcsoft.framework.simplay.engine.model.SinglePage

/** Stable id of the title page, never printed with a page number. */
private const val TITLE_PAGE_ID = "title"

/** Stable id of the copyright page, never printed with a page number. */
private const val COPYRIGHT_PAGE_ID = "copyright"

/**
 * Turns a whole book into a simPlay [Document].
 *
 * The document is a derived view, never a stored state: it is rebuilt from [Book], [Design] and
 * [Meta] whenever the manuscript or the design changes. Every part of the book gets a page of its
 * own, in reading order: the title page, the copyright page, the prolog, the chapters, the epilog
 * and the blurb. The title and copyright pages are confined to their sheet ([SinglePage]); every
 * written part flows onto as many sheets as it needs ([FlowPage]).
 *
 * Every page is built with a fixed, stable id instead of simPlay's random default, so the title and
 * copyright page can be named in [Document.numbering]'s `excludedPageIds` without depending on an id
 * generated somewhere else. Since IP-38 that id doubles as the page's `TextAnchor` id for every
 * written part: [book]'s own [Book.document] is threaded through every part builder, so an existing
 * page's text survives a rebuild and only a part with no page yet is seeded with an empty,
 * anchor-only block - a chapter's page id is its stable [org.pcsoft.app.aighost.model.project.book.Chapter.id],
 * not its position, so renaming or reordering chapters never orphans a page.
 *
 * The page policy simPlay does not own yet is marked with TODO(simPlay page policy) throughout:
 * mirrored margins, inactive pages of a switched-off part, the hard edge of the blurb and the
 * leading and trailing blank sheets. That policy moves into simPlay and this builder adopts it from
 * there.
 */
object BookDocumentBuilder {

    /**
     * Builds the [Document] of [book] under [design], taking the author name from [meta].
     *
     * @param book The manuscript - title, copyright, prolog, chapters, epilog, blurb and its current
     * [Book.document], read for every part's existing anchor-addressed text.
     * @param design The typography and the page geometry every page shares.
     * @param meta The project meta data the author name is read from.
     * @return One [Document] whose pages follow the reading order of the book.
     */
    fun build(book: Book, design: Design, meta: Meta): Document {
        // TODO(simPlay page policy): one shared layout for every page - mirrored recto/verso margins
        //  and the leading/trailing blank sheets of Design.startWithEmptyPage / endWithEmptyPage are
        //  not expressed yet; simPlay owns that once it lands.
        val layout = design.pageFormat.toPageLayout()
        val document = book.document
        val pages = ArrayList<Page>()
        val excludedPageIds = mutableSetOf(TITLE_PAGE_ID)

        pages += SinglePage(layout, TitlePageBuilder.build(document, meta, design), id = TITLE_PAGE_ID)

        if (book.copyright.included) {
            pages += SinglePage(
                layout,
                CopyrightPageBuilder.build(document, book.copyright, meta, design),
                id = COPYRIGHT_PAGE_ID
            )
            excludedPageIds += COPYRIGHT_PAGE_ID
        }

        // TODO(simPlay page policy): a part with included == false still gets its page here; marking
        //  that page inactive with PageMode.DISABLED is IP-16's job, the future book preview - it is
        //  not attempted in the single-part BookPartEditor.
        pages += FlowPage(
            layout,
            BookPartBuilder.build(document, "prolog", design.prologPage.textStyle.toTextStyle()),
            id = "prolog"
        )

        book.chapters.forEach { chapter ->
            val anchorId = chapter.id.toString()
            pages += FlowPage(
                layout,
                BookPartBuilder.build(document, anchorId, design.chapterPage.textStyle.toTextStyle()),
                id = anchorId
            )
        }

        pages += FlowPage(
            layout,
            BookPartBuilder.build(document, "epilog", design.epilogPage.textStyle.toTextStyle()),
            id = "epilog"
        )

        // TODO(simPlay page policy): the blurb is a plain flow page for now; its hard page edge is
        //  part of the policy that moves into simPlay.
        pages += FlowPage(layout, BlurbBuilder.build(book.blurb, design, withAnchor = true), id = "blurb")

        return Document(pages, numbering = design.pageNumbering.toPageNumbering(excludedPageIds))
    }
}

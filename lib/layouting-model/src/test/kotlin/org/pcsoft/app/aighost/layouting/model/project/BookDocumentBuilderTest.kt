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

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.pcsoft.app.aighost.layouting.model.common.toPageLayout
import org.pcsoft.app.aighost.layouting.model.project.book.BookPartBuilder
import org.pcsoft.app.aighost.model.common.FontData
import org.pcsoft.app.aighost.model.common.StyleData
import org.pcsoft.app.aighost.model.project.book.Blurb
import org.pcsoft.app.aighost.model.project.book.Book
import org.pcsoft.app.aighost.model.project.book.Chapter
import org.pcsoft.app.aighost.model.project.book.Copyright
import org.pcsoft.app.aighost.model.project.book.Epilog
import org.pcsoft.app.aighost.model.project.book.Prolog
import org.pcsoft.app.aighost.model.project.design.Design
import org.pcsoft.app.aighost.model.project.design.PageNumberCountingMode
import org.pcsoft.app.aighost.model.project.design.PageNumberDesign
import org.pcsoft.app.aighost.model.project.design.PageNumberPosition
import org.pcsoft.app.aighost.model.project.meta.Meta
import org.pcsoft.framework.simplay.engine.PageCountingMode
import org.pcsoft.framework.simplay.engine.model.FlowPage
import org.pcsoft.framework.simplay.engine.model.SinglePage
import org.pcsoft.framework.simplay.engine.model.PageNumberPosition as SimplayPageNumberPosition

/**
 * Developer tests for the whole-book document builder, [BookDocumentBuilder].
 */
class BookDocumentBuilderTest {

    private val meta = Meta(author = "Jane Doe")

    private fun book() = Book(
        title = "The Silent Harbour",
        prolog = Prolog(title = "Before", paragraph = listOf("It began earlier."), included = true),
        chapters = listOf(
            Chapter(name = "One", title = "The Arrival", paragraph = listOf("The harbour was quiet.")),
            Chapter(name = "Two", title = "The Departure", paragraph = listOf("Then it was over."))
        ),
        epilog = Epilog(title = "After", paragraph = listOf("It ended later."), included = true),
        blurb = Blurb(paragraph = listOf("A harbour town keeps its secrets."))
    )

    /**
     * Use case: a book with a prolog, two chapters, an epilog and a blurb becomes a document whose
     * pages follow the reading order: title, copyright, prolog, the two chapters, epilog, blurb.
     */
    @Test
    fun theDocumentFollowsTheReadingOrderOfTheBook() {
        val document = BookDocumentBuilder.build(book(), Design(), meta)

        assertEquals(7, document.pages.size)
        assertInstanceOf(SinglePage::class.java, document.pages[0])
        assertInstanceOf(SinglePage::class.java, document.pages[1])
        assertTrue(document.pages.drop(2).all { it is FlowPage })
    }

    /**
     * Use case: the title page and the copyright page are confined to their sheet, every written
     * part of the manuscript flows onto as many sheets as it needs.
     */
    @Test
    fun theFrontMatterIsConfinedAndTheWrittenPartsFlow() {
        val document = BookDocumentBuilder.build(book(), Design(), meta)

        assertInstanceOf(SinglePage::class.java, document.pages[0])
        assertInstanceOf(SinglePage::class.java, document.pages[1])
        assertInstanceOf(FlowPage::class.java, document.pages[2]) // prolog
        assertInstanceOf(FlowPage::class.java, document.pages[3]) // chapter one
        assertInstanceOf(FlowPage::class.java, document.pages[4]) // chapter two
        assertInstanceOf(FlowPage::class.java, document.pages[5]) // epilog
        assertInstanceOf(FlowPage::class.java, document.pages[6]) // blurb
    }

    /**
     * Use case: a prolog and an epilog that were switched off still get their page - simPlay has no
     * inactive page, so dropping them from the numbering is left for the page policy.
     */
    @Test
    fun aSwitchedOffPrologAndEpilogStillGetTheirPage() {
        val withOff = book().let {
            it.copy(
                prolog = it.prolog.copy(included = false),
                epilog = it.epilog.copy(included = false)
            )
        }

        val document = BookDocumentBuilder.build(withOff, Design(), meta)

        assertEquals(7, document.pages.size)
        assertEquals(
            BookPartBuilder.build(withOff.prolog, Design().prologPage),
            (document.pages[2] as FlowPage).blocks
        )
    }

    /**
     * Use case: the copyright page carries its own include switch; when it is off the book has no
     * copyright sheet and the prolog follows the title page directly.
     */
    @Test
    fun aCopyrightPageThatIsNotIncludedIsLeftOut() {
        val withoutCopyright = book().copy(copyright = Copyright(included = false))

        val document = BookDocumentBuilder.build(withoutCopyright, Design(), meta)

        assertEquals(6, document.pages.size)
        assertInstanceOf(SinglePage::class.java, document.pages[0]) // title
        assertInstanceOf(FlowPage::class.java, document.pages[1]) // prolog, no copyright before it
    }

    /**
     * Use case: every page shares the one layout the page format translates into, until simPlay owns
     * the recto/verso policy.
     */
    @Test
    fun everyPageSharesTheLayoutFromThePageFormat() {
        val design = Design()
        val expected = design.pageFormat.toPageLayout()

        val document = BookDocumentBuilder.build(book(), design, meta)

        assertTrue(document.pages.all { it.layout == expected })
    }

    /**
     * Use case: the document is a derived view of the design, so changing a page design changes the
     * style of the blocks that design produces.
     */
    @Test
    fun aDesignChangeChangesTheBlockStyles() {
        val design = Design()
        val changed = design.copy(
            chapterPage = design.chapterPage.copy(
                textStyle = StyleData(font = FontData(name = "Courier", size = 13))
            )
        )

        val chapterPage = BookDocumentBuilder.build(book(), changed, meta).pages[3] as FlowPage

        assertTrue(chapterPage.blocks.any { it.style.font.family == "Courier" && it.style.font.size == 13.0 })
    }

    /**
     * Use case: the blocks of a page are exactly what that part's block builder produces, so the
     * document builder only arranges pages and never rewrites their content.
     */
    @Test
    fun theBlocksOfEachPageComeFromItsPartBuilder() {
        val theBook = book()
        val design = Design()

        val document = BookDocumentBuilder.build(theBook, design, meta)

        assertEquals(
            BookPartBuilder.build(theBook.chapters[0], design.chapterPage),
            (document.pages[3] as FlowPage).blocks
        )
        assertEquals(
            BookPartBuilder.build(theBook.epilog, design.epilogPage),
            (document.pages[5] as FlowPage).blocks
        )
    }

    /**
     * Use case: every page is built with a fixed id instead of simPlay's random default, so the title
     * and copyright page can be named in the numbering's excluded ids across a rebuild of the document.
     */
    @Test
    fun everyPageCarriesAStableId() {
        val document = BookDocumentBuilder.build(book(), Design(), meta)

        assertEquals(
            listOf("title", "copyright", "prolog", "chapter-0", "chapter-1", "epilog", "blurb"),
            document.pages.map { it.id }
        )
    }

    /**
     * Use case: a fresh project has page numbering switched off, so the built document carries no
     * number position either, matching the design's default.
     */
    @Test
    fun defaultsToNoPageNumber() {
        val document = BookDocumentBuilder.build(book(), Design(), meta)

        assertEquals(SimplayPageNumberPosition.OFF, document.numbering.position)
    }

    /**
     * Use case: the user turns page numbering on with a start value other than one and asks unnumbered
     * sheets to skip the counter, so every one of these settings reaches the built document.
     */
    @Test
    fun translatesThePageNumberDesignIntoTheDocument() {
        val design = Design(
            pageNumbering = PageNumberDesign(
                position = PageNumberPosition.BOTTOM_OUTER,
                startNumber = 3,
                countingMode = PageNumberCountingMode.SKIP_EXCLUDED
            )
        )

        val document = BookDocumentBuilder.build(book(), design, meta)

        assertEquals(SimplayPageNumberPosition.BOTTOM_OUTER, document.numbering.position)
        assertEquals(3, document.numbering.startNumber)
        assertEquals(PageCountingMode.SKIP_EXCLUDED, document.numbering.counting)
    }

    /**
     * Use case: the title page and the copyright page never carry a number, so both stable ids are
     * named in the document's excluded ids.
     */
    @Test
    fun excludesTheTitleAndCopyrightPageFromNumbering() {
        val document = BookDocumentBuilder.build(book(), Design(), meta)

        assertEquals(setOf("title", "copyright"), document.numbering.excludedPageIds)
    }

    /**
     * Use case: the copyright page is switched off, so only the title page is left to exclude from
     * numbering - there is no copyright id on the document to name.
     */
    @Test
    fun excludesOnlyTheTitlePageWhenCopyrightIsOff() {
        val withoutCopyright = book().copy(copyright = Copyright(included = false))

        val document = BookDocumentBuilder.build(withoutCopyright, Design(), meta)

        assertEquals(setOf("title"), document.numbering.excludedPageIds)
    }
}

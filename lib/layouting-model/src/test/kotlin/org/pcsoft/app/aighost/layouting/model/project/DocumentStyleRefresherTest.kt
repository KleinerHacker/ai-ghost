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
import org.junit.jupiter.api.Test
import org.pcsoft.app.aighost.layouting.model.common.toTextStyle
import org.pcsoft.app.aighost.model.common.FontData
import org.pcsoft.app.aighost.model.common.StyleData
import org.pcsoft.app.aighost.model.project.book.Book
import org.pcsoft.app.aighost.model.project.book.Chapter
import org.pcsoft.app.aighost.model.project.design.ChapterPageDesign
import org.pcsoft.app.aighost.model.project.design.Design
import org.pcsoft.app.aighost.model.project.design.EpilogPageDesign
import org.pcsoft.framework.simplay.engine.geometry.Margins
import org.pcsoft.framework.simplay.engine.geometry.Size
import org.pcsoft.framework.simplay.engine.model.Document
import org.pcsoft.framework.simplay.engine.model.FlowPage
import org.pcsoft.framework.simplay.engine.model.PageLayout
import org.pcsoft.framework.simplay.engine.model.SinglePage
import org.pcsoft.framework.simplay.engine.model.TextBlock

/**
 * Developer tests for [DocumentStyleRefresher].
 */
class DocumentStyleRefresherTest {

    private val layout = PageLayout(Size(420.0, 595.0), Margins(40.0, 40.0, 50.0, 50.0))
    private val oldStyle = StyleData(font = FontData("Serif", 10)).toTextStyle()

    /**
     * Use case: the user changes the chapter design, so an existing chapter's blocks pick up the new
     * style while their text and their anchor stay exactly as written.
     */
    @Test
    fun restylesAChapterPageWithoutTouchingItsText() {
        val chapter = Chapter(name = "One")
        val block = TextBlock.of("\${${chapter.id}}Once upon a time.", oldStyle)
        val book = Book(chapters = listOf(chapter)).apply {
            document = Document(pages = listOf(FlowPage(layout, listOf(block), id = chapter.id.toString())))
        }
        val newDesign = Design(chapterPage = ChapterPageDesign(textStyle = StyleData(font = FontData("Baskerville", 12))))

        val refreshed = DocumentStyleRefresher.refresh(book, newDesign)

        val restyledBlock = refreshed.pages.single().blocks.single()
        assertEquals("\${${chapter.id}}Once upon a time.", restyledBlock.toString())
        assertEquals("Baskerville", restyledBlock.style.font.family)
        assertEquals(12.0, restyledBlock.style.font.size)
    }

    /**
     * Use case: the epilog design changes, so the epilog page's own blocks are restyled the same way a
     * chapter's are.
     */
    @Test
    fun restylesTheEpilogPage() {
        val block = TextBlock.of("\${epilog}The end.", oldStyle)
        val book = Book().apply {
            document = Document(pages = listOf(FlowPage(layout, listOf(block), id = "epilog")))
        }
        val newDesign = Design(epilogPage = EpilogPageDesign(textStyle = StyleData(font = FontData("Baskerville", 12))))

        val refreshed = DocumentStyleRefresher.refresh(book, newDesign)

        assertEquals("Baskerville", refreshed.pages.single().blocks.single().style.font.family)
    }

    /**
     * Use case: the title page carries only its unwritten seed and the author line built fresh from
     * the design on every read, so a design change leaves its page in the document untouched.
     */
    @Test
    fun leavesTheTitlePageUntouched() {
        val block = TextBlock.of("\${title}", oldStyle)
        val page = SinglePage(layout, listOf(block), id = "title")
        val book = Book().apply { document = Document(pages = listOf(page)) }

        val refreshed = DocumentStyleRefresher.refresh(book, Design())

        // Book.document decodes a fresh Document from JSON on every read, so the refreshed page is
        // never the same object as `page` even when nothing about it changes - only its content is.
        assertEquals(page, refreshed.pages.single())
    }
}

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

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.pcsoft.app.aighost.layouting.TextAlignment
import org.pcsoft.app.aighost.layouting.model.common.BlockSpacing
import org.pcsoft.app.aighost.model.common.Alignment
import org.pcsoft.app.aighost.model.common.FontData
import org.pcsoft.app.aighost.model.common.StyleData
import org.pcsoft.app.aighost.model.project.book.Book
import org.pcsoft.app.aighost.model.project.design.Design
import org.pcsoft.app.aighost.model.project.design.TitlePageDesign
import org.pcsoft.app.aighost.model.project.meta.Meta

/**
 * Developer tests for the blocks of the title page, [TitlePageBuilder].
 */
class TitlePageBuilderTest {

    private val design = Design(
        titlePage = TitlePageDesign(
            titleStyle = StyleData(
                font = FontData("Garamond", 28, bold = true),
                textLineSpacing = 1.4,
                alignment = Alignment.CENTER
            ),
            titleAppendixStyle = StyleData(
                font = FontData("Garamond", 28, bold = true),
                textLineSpacing = 1.4,
                alignment = Alignment.CENTER
            ),
            showAuthor = true,
            authorStyle = StyleData(
                font = FontData("Garamond", 16, italic = true),
                textLineSpacing = 1.1,
                alignment = Alignment.CENTER
            )
        )
    )

    /**
     * Use case: a complete title page is built - the title, its further lines and the author name -
     * and every block carries the text and the style the design gives for it.
     */
    @Test
    fun theTitlePageIsTitleAppendixAndAuthor() {
        val book = Book(title = "The Silent Harbour", titleAppendix = listOf("A Novel", "Volume One"))
        val meta = Meta(author = "Jane Doe")

        val blocks = TitlePageBuilder.build(book, meta, design)

        assertEquals(
            listOf("The Silent Harbour", "A Novel", "Volume One", "Jane Doe"),
            blocks.map { it.text }
        )
        assertEquals(listOf("Garamond", "Garamond", "Garamond", "Garamond"), blocks.map { it.style.family })
        assertEquals(listOf(28.0, 28.0, 28.0, 16.0), blocks.map { it.style.size })
        assertEquals(listOf(true, true, true, false), blocks.map { it.style.bold })
        assertEquals(listOf(false, false, false, true), blocks.map { it.style.italic })
        assertTrue(blocks.all { it.style.alignment == TextAlignment.CENTER })
    }

    /**
     * Use case: the title and the author are set with the line spacing their own style carries, not
     * with a single one for the whole page.
     */
    @Test
    fun everyBlockCarriesTheLineSpacingOfItsOwnStyle() {
        val blocks = TitlePageBuilder.build(
            Book(title = "The Silent Harbour", titleAppendix = listOf("A Novel")),
            Meta(author = "Jane Doe"),
            design
        )

        assertEquals(listOf(1.4, 1.4, 1.1), blocks.map { it.style.lineSpacing })
    }

    /**
     * Use case: the parts of the title page are held apart on the paper, so the title, the last
     * further line and the author name carry the gaps of the layout.
     */
    @Test
    fun thePartsOfThePageAreHeldApart() {
        val blocks = TitlePageBuilder.build(
            Book(title = "The Silent Harbour", titleAppendix = listOf("A Novel", "Volume One")),
            Meta(author = "Jane Doe"),
            design
        )

        assertEquals(BlockSpacing.AFTER_TITLE, blocks[0].style.spaceAfter)
        assertEquals(0.0, blocks[1].style.spaceAfter)
        assertEquals(BlockSpacing.AFTER_TITLE_APPENDIX, blocks[2].style.spaceAfter)
        assertEquals(BlockSpacing.BEFORE_AUTHOR, blocks[3].style.spaceBefore)
    }

    /**
     * Use case: a book without further title lines is built as well; the gap that would have stood
     * below the last of them stands below the title instead, so the author keeps its distance.
     */
    @Test
    fun withoutFurtherTitleLinesTheTitleCarriesTheWholeGap() {
        val blocks = TitlePageBuilder.build(Book(title = "The Silent Harbour"), Meta(author = "Jane Doe"), design)

        assertEquals(listOf("The Silent Harbour", "Jane Doe"), blocks.map { it.text })
        assertEquals(BlockSpacing.AFTER_TITLE_APPENDIX, blocks[0].style.spaceAfter)
    }

    /**
     * Use case: the design hides the author name on the title page, so the block is left out even
     * though an author was typed.
     */
    @Test
    fun theAuthorIsLeftOutWhenTheDesignHidesIt() {
        val hidden = design.copy(titlePage = design.titlePage.copy(showAuthor = false))

        val blocks = TitlePageBuilder.build(Book(title = "The Silent Harbour"), Meta(author = "Jane Doe"), hidden)

        assertEquals(listOf("The Silent Harbour"), blocks.map { it.text })
    }

    /**
     * Use case: what the user left empty must not take a line of its own, so a blank title, a blank
     * further line and a missing author are left out instead of being set as empty blocks.
     */
    @Test
    fun whatWasLeftEmptyIsLeftOut() {
        val book = Book(title = "   ", titleAppendix = listOf("A Novel", "  "))

        val blocks = TitlePageBuilder.build(book, Meta(author = ""), design)

        assertEquals(listOf("A Novel"), blocks.map { it.text })
    }

    /**
     * Use case: nothing was typed at all, so the title page carries no block instead of a page of
     * empty lines.
     */
    @Test
    fun anEmptyBookAndAnEmptyMetaGiveNoBlock() {
        val blocks = TitlePageBuilder.build(Book(title = ""), Meta(author = ""), design)

        assertTrue(blocks.isEmpty())
    }
}

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
import org.pcsoft.app.aighost.model.common.Alignment
import org.pcsoft.app.aighost.model.common.FontData
import org.pcsoft.app.aighost.model.common.StyleData
import org.pcsoft.app.aighost.model.project.book.Book
import org.pcsoft.app.aighost.model.project.design.Design
import org.pcsoft.app.aighost.model.project.design.TitlePageDesign
import org.pcsoft.app.aighost.model.project.meta.Meta
import org.pcsoft.framework.simplay.engine.model.TextAlignment

/**
 * Developer tests for the blocks of the title page, [TitlePageBuilder].
 *
 * IP-36 removed the main title and its further lines from [Book] - that text now lives only in the
 * book's simPlay `Document` (IP-37/IP-38) - so only the author name, still carried by [Meta], is built
 * here until IP-38 rebuilds this builder around the title's anchor in that document.
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
     * Use case: an author name was typed and the design shows it, so the title page gives exactly the
     * author block, styled with the author style of the design.
     */
    @Test
    fun theTitlePageIsTheAuthorBlockAlone() {
        val blocks = TitlePageBuilder.build(Book(), Meta(author = "Jane Doe"), design)

        assertEquals(listOf("Jane Doe"), blocks.map { it.toString() })
        assertEquals("Garamond", blocks[0].style.font.family)
        assertEquals(16.0, blocks[0].style.font.size)
        assertEquals(TextAlignment.CENTER, blocks[0].style.alignment)
        assertEquals(1.1, blocks[0].style.lineSpacing.factor)
    }

    /**
     * Use case: the design hides the author name on the title page, so no block is built even though
     * an author was typed.
     */
    @Test
    fun theAuthorIsLeftOutWhenTheDesignHidesIt() {
        val hidden = design.copy(titlePage = design.titlePage.copy(showAuthor = false))

        val blocks = TitlePageBuilder.build(Book(), Meta(author = "Jane Doe"), hidden)

        assertTrue(blocks.isEmpty())
    }

    /**
     * Use case: no author was typed, so the title page gives no block at all.
     */
    @Test
    fun anEmptyAuthorGivesNoBlock() {
        val blocks = TitlePageBuilder.build(Book(), Meta(author = ""), design)

        assertTrue(blocks.isEmpty())
    }
}

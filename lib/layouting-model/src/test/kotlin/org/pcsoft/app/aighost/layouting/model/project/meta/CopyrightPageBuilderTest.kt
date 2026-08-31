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

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.pcsoft.app.aighost.layouting.TextAlignment
import org.pcsoft.app.aighost.layouting.model.common.BlockSpacing
import org.pcsoft.app.aighost.model.common.Alignment
import org.pcsoft.app.aighost.model.common.FontData
import org.pcsoft.app.aighost.model.common.StyleData
import org.pcsoft.app.aighost.model.project.book.Copyright
import org.pcsoft.app.aighost.model.project.design.CopyrightPageDesign
import org.pcsoft.app.aighost.model.project.design.Design
import org.pcsoft.app.aighost.model.project.meta.Meta

/**
 * Developer tests for the blocks of the copyright page, [CopyrightPageBuilder].
 */
class CopyrightPageBuilderTest {

    private val design = Design(
        copyrightPage = CopyrightPageDesign(
            copyrightStyle = StyleData(
                font = FontData("Baskerville", 9),
                textLineSpacing = 1.15,
                alignment = Alignment.RIGHT
            ),
            copyrightAppendixStyle = StyleData(
                font = FontData("Baskerville", 8),
                textLineSpacing = 1.1,
                alignment = Alignment.RIGHT
            ),
            showAuthor = false,
            authorStyle = StyleData(
                font = FontData("Baskerville", 10, italic = true),
                textLineSpacing = 1.2,
                alignment = Alignment.RIGHT
            )
        )
    )

    /**
     * Use case: the notice the user typed is built into a block and carries the style and the line
     * spacing the design gives for the copyright page.
     */
    @Test
    fun theNoticeIsSetInTheCopyrightDesign() {
        val blocks = CopyrightPageBuilder.build(
            Copyright(copyright = "Copyright 2026 Jane Doe"),
            Meta(),
            design
        )

        val block = blocks.single()
        assertEquals("Copyright 2026 Jane Doe", block.text)
        assertEquals("Baskerville", block.style.family)
        assertEquals(9.0, block.style.size)
        assertEquals(TextAlignment.RIGHT, block.style.alignment)
        assertEquals(1.15, block.style.lineSpacing)
        assertEquals(BlockSpacing.AFTER_PARAGRAPH, block.style.spaceAfter)
    }

    /**
     * Use case: the user typed the notice over several lines; a block carries no line break, so every
     * one of those lines becomes a block of its own and is set on its own line.
     */
    @Test
    fun everyTypedLineBecomesABlockOfItsOwn() {
        val copyright = Copyright(copyright = "Copyright 2026 Jane Doe\nAll rights reserved\n\nFirst edition")

        val blocks = CopyrightPageBuilder.build(copyright, Meta(), design)

        assertEquals(
            listOf("Copyright 2026 Jane Doe", "All rights reserved", "", "First edition"),
            blocks.map { it.text }
        )
        assertTrue(blocks.all { it.style == blocks.first().style })
    }

    /**
     * Use case: the further copyright lines follow the notice, each set in the appendix style instead
     * of the notice style.
     */
    @Test
    fun theFurtherLinesAreSetInTheAppendixStyle() {
        val copyright = Copyright(
            copyright = "Copyright 2026 Jane Doe",
            copyrightAppendix = listOf("All rights reserved", "  ", "First edition")
        )

        val blocks = CopyrightPageBuilder.build(copyright, Meta(), design)

        assertEquals(
            listOf("Copyright 2026 Jane Doe", "All rights reserved", "First edition"),
            blocks.map { it.text }
        )
        assertEquals(8.0, blocks[1].style.size)
        assertEquals(1.1, blocks[1].style.lineSpacing)
    }

    /**
     * Use case: the design asks for the author name on the copyright page, so it closes the page in
     * the author style and stands away from the lines above it.
     */
    @Test
    fun theAuthorClosesThePageWhenTheDesignAsksForIt() {
        val withAuthor = design.copy(copyrightPage = design.copyrightPage.copy(showAuthor = true))

        val blocks = CopyrightPageBuilder.build(
            Copyright(copyright = "Copyright 2026 Jane Doe"),
            Meta(author = "Jane Doe"),
            withAuthor
        )

        assertEquals(listOf("Copyright 2026 Jane Doe", "Jane Doe"), blocks.map { it.text })
        assertEquals(10.0, blocks[1].style.size)
        assertTrue(blocks[1].style.italic)
        assertEquals(BlockSpacing.BEFORE_AUTHOR, blocks[1].style.spaceBefore)
    }

    /**
     * Use case: the design asks for the author name but none was typed, so the page still ends with
     * the notice instead of an empty author line.
     */
    @Test
    fun theAuthorLineIsLeftOutWhenNoAuthorWasTyped() {
        val withAuthor = design.copy(copyrightPage = design.copyrightPage.copy(showAuthor = true))

        val blocks = CopyrightPageBuilder.build(
            Copyright(copyright = "Copyright 2026 Jane Doe"),
            Meta(author = ""),
            withAuthor
        )

        assertEquals(listOf("Copyright 2026 Jane Doe"), blocks.map { it.text })
    }

    /**
     * Use case: nothing was typed, so the page carries no block and the caller can leave it out of
     * the book altogether.
     */
    @Test
    fun withoutANoticeNoBlockIsBuilt() {
        assertTrue(CopyrightPageBuilder.build(Copyright(copyright = ""), Meta(), design).isEmpty())
        assertTrue(CopyrightPageBuilder.build(Copyright(copyright = "   "), Meta(), design).isEmpty())
    }

    /**
     * Use case: the user took the copyright page out of the book, so it gives no block no matter what
     * text it still carries.
     */
    @Test
    fun aPageThatIsNotIncludedGivesNoBlock() {
        val copyright = Copyright(
            copyright = "Copyright 2026 Jane Doe",
            copyrightAppendix = listOf("All rights reserved"),
            included = false
        )

        assertTrue(CopyrightPageBuilder.build(copyright, Meta(author = "Jane Doe"), design).isEmpty())
    }
}

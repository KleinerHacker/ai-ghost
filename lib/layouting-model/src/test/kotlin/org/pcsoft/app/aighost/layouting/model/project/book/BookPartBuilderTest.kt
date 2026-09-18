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
import org.junit.jupiter.api.Test
import org.pcsoft.app.aighost.layouting.model.common.toTextStyle
import org.pcsoft.app.aighost.model.common.Alignment
import org.pcsoft.app.aighost.model.common.FontData
import org.pcsoft.app.aighost.model.common.StyleData
import org.pcsoft.app.aighost.model.project.design.ChapterPageDesign
import org.pcsoft.framework.simplay.engine.geometry.Margins
import org.pcsoft.framework.simplay.engine.geometry.Size
import org.pcsoft.framework.simplay.engine.model.Document
import org.pcsoft.framework.simplay.engine.model.FlowPage
import org.pcsoft.framework.simplay.engine.model.PageLayout
import org.pcsoft.framework.simplay.engine.model.TextBlock

/**
 * Developer tests for the blocks of a written part, [BookPartBuilder].
 *
 * A written part's text lives only in the book's simPlay `Document`, addressed through its anchor id
 * (IP-38): [BookPartBuilder] reads it back from there, or seeds a fresh, anchor-only block when the
 * document carries no page for it yet.
 */
class BookPartBuilderTest {

    private val layout = PageLayout(Size(420.0, 595.0), Margins(40.0, 40.0, 50.0, 50.0))

    private val style = ChapterPageDesign(
        titleStyle = StyleData(font = FontData("Garamond", 20, bold = true), textLineSpacing = 1.3, alignment = Alignment.CENTER),
        titleAppendixStyle = StyleData(font = FontData("Garamond", 14, italic = true), textLineSpacing = 1.3, alignment = Alignment.CENTER),
        textStyle = StyleData(font = FontData("Baskerville", 11), textLineSpacing = 1.6, alignment = Alignment.BLOCK)
    ).textStyle.toTextStyle()

    /**
     * Use case: a chapter's anchor has no page in the document yet - a freshly created project - so
     * the builder seeds a single block that carries nothing but the anchor.
     */
    @Test
    fun seedsAnAnchorOnlyBlockWhenThereIsNoPageYet() {
        val blocks = BookPartBuilder.build(Document(), "chapter-1", style)

        assertEquals(listOf("\${chapter-1}"), blocks.map { it.toString() })
    }

    /**
     * Use case: the seeded block is built in the given style, so it matches the design the moment
     * the user starts typing into it.
     */
    @Test
    fun seedsTheBlockInTheGivenStyle() {
        val blocks = BookPartBuilder.build(Document(), "prolog", style)

        assertEquals(style, blocks.single().style)
    }

    /**
     * Use case: the user already wrote into a chapter, so its page in the document is read back
     * unchanged instead of being overwritten with a fresh, empty seed.
     */
    @Test
    fun readsTheExistingBlocksOfTheAnchorsPage() {
        val existing = listOf(
            TextBlock.of("\${prolog}Once upon a time.", style),
            TextBlock.of("The story continued.", style)
        )
        val document = Document(pages = listOf(FlowPage(layout, existing, id = "prolog")))

        assertEquals(existing, BookPartBuilder.build(document, "prolog", style))
    }

    /**
     * Use case: the document carries pages for other anchors, so only the page whose id matches the
     * requested anchor is read.
     */
    @Test
    fun onlyReadsThePageMatchingTheAnchorId() {
        val chapterOne = listOf(TextBlock.of("\${chapter-1}First chapter.", style))
        val chapterTwo = listOf(TextBlock.of("\${chapter-2}Second chapter.", style))
        val document = Document(
            pages = listOf(
                FlowPage(layout, chapterOne, id = "chapter-1"),
                FlowPage(layout, chapterTwo, id = "chapter-2")
            )
        )

        assertEquals(chapterTwo, BookPartBuilder.build(document, "chapter-2", style))
    }
}

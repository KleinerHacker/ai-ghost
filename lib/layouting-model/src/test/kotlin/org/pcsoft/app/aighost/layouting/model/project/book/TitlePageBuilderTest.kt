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
import org.pcsoft.app.aighost.model.common.Alignment
import org.pcsoft.app.aighost.model.common.FontData
import org.pcsoft.app.aighost.model.common.StyleData
import org.pcsoft.app.aighost.model.project.book.Book
import org.pcsoft.app.aighost.model.project.design.Design
import org.pcsoft.app.aighost.model.project.design.TitlePageDesign
import org.pcsoft.app.aighost.model.project.meta.Meta
import org.pcsoft.framework.simplay.engine.model.Document
import org.pcsoft.framework.simplay.engine.model.TextAlignment

/**
 * Developer tests for the blocks of the title page, [TitlePageBuilder].
 *
 * IP-36 removed the main title and its further lines from [Book]; since IP-38 that text lives only in
 * the book's simPlay [Document], read back through the `"title"` anchor via [BookPartBuilder]. The
 * author name still lives in [Meta] and is appended after the anchor-addressed title blocks.
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
     * Use case: a freshly created project has no title text yet, so the title page gives only the
     * seeded `"title"` anchor block, followed by the author block the design asks for.
     */
    @Test
    fun theTitlePageIsTheAnchorFollowedByTheAuthorBlock() {
        val blocks = TitlePageBuilder.build(Document(), Meta(author = "Jane Doe"), design)

        assertEquals(listOf("\${title}", "Jane Doe"), blocks.map { it.toString() })
        assertEquals("Garamond", blocks[1].style.font.family)
        assertEquals(16.0, blocks[1].style.font.size)
        assertEquals(TextAlignment.CENTER, blocks[1].style.alignment)
        assertEquals(1.1, blocks[1].style.lineSpacing.factor)
    }

    /**
     * Use case: the anchor block is seeded in the title's own style, not the author's, so it already
     * matches the design the moment the user starts typing a title.
     */
    @Test
    fun theAnchorBlockIsSeededInTheTitleStyle() {
        val blocks = TitlePageBuilder.build(Document(), Meta(author = ""), design)

        assertEquals("Garamond", blocks[0].style.font.family)
        assertEquals(28.0, blocks[0].style.font.size)
    }

    /**
     * Use case: the design hides the author name on the title page, so only the anchor block remains,
     * even though an author was typed.
     */
    @Test
    fun theAuthorIsLeftOutWhenTheDesignHidesIt() {
        val hidden = design.copy(titlePage = design.titlePage.copy(showAuthor = false))

        val blocks = TitlePageBuilder.build(Document(), Meta(author = "Jane Doe"), hidden)

        assertEquals(listOf("\${title}"), blocks.map { it.toString() })
    }

    /**
     * Use case: no author was typed, so only the anchor block remains.
     */
    @Test
    fun anEmptyAuthorGivesOnlyTheAnchorBlock() {
        val blocks = TitlePageBuilder.build(Document(), Meta(author = ""), design)

        assertEquals(listOf("\${title}"), blocks.map { it.toString() })
    }
}

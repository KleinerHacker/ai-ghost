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
import org.pcsoft.app.aighost.model.common.Alignment
import org.pcsoft.app.aighost.model.common.FontData
import org.pcsoft.app.aighost.model.common.StyleData
import org.pcsoft.app.aighost.model.project.book.Copyright
import org.pcsoft.app.aighost.model.project.design.CopyrightPageDesign
import org.pcsoft.app.aighost.model.project.design.Design
import org.pcsoft.app.aighost.model.project.meta.Meta
import org.pcsoft.framework.simplay.engine.model.FontStyle

/**
 * Developer tests for the blocks of the copyright page, [CopyrightPageBuilder].
 *
 * IP-36 removed the copyright notice and its further lines from [Copyright] - that text now lives only
 * in the book's simPlay `Document` (IP-37/IP-38) - so only the author name, still carried by [Meta], is
 * built here until IP-38 rebuilds this builder around the copyright page's anchor in that document.
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
            showAuthor = true,
            authorStyle = StyleData(
                font = FontData("Baskerville", 10, italic = true),
                textLineSpacing = 1.2,
                alignment = Alignment.RIGHT
            )
        )
    )

    /**
     * Use case: the design asks for the author name on the copyright page and the page is included, so
     * the author block is built in the author style.
     */
    @Test
    fun theAuthorIsBuiltWhenTheDesignAsksForIt() {
        val blocks = CopyrightPageBuilder.build(Copyright(included = true), Meta(author = "Jane Doe"), design)

        assertEquals(listOf("Jane Doe"), blocks.map { it.toString() })
        assertEquals(10.0, blocks[0].style.font.size)
        assertEquals(FontStyle.ITALIC, blocks[0].style.font.style)
    }

    /**
     * Use case: the design asks for the author name but none was typed, so no block is built.
     */
    @Test
    fun theAuthorLineIsLeftOutWhenNoAuthorWasTyped() {
        val blocks = CopyrightPageBuilder.build(Copyright(included = true), Meta(author = ""), design)

        assertTrue(blocks.isEmpty())
    }

    /**
     * Use case: the user took the copyright page out of the book, so it gives no block no matter what
     * the design or the meta data carry.
     */
    @Test
    fun aPageThatIsNotIncludedGivesNoBlock() {
        assertTrue(CopyrightPageBuilder.build(Copyright(included = false), Meta(author = "Jane Doe"), design).isEmpty())
    }
}

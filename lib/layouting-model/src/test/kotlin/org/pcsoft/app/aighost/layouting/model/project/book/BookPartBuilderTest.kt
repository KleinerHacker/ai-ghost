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

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.pcsoft.app.aighost.model.common.Alignment
import org.pcsoft.app.aighost.model.common.FontData
import org.pcsoft.app.aighost.model.common.StyleData
import org.pcsoft.app.aighost.model.project.book.Chapter
import org.pcsoft.app.aighost.model.project.book.Epilog
import org.pcsoft.app.aighost.model.project.book.Prolog
import org.pcsoft.app.aighost.model.project.design.ChapterPageDesign

/**
 * Developer tests for the blocks of a written part, [BookPartBuilder].
 *
 * IP-36 removed the heading and the paragraphs from every book part - that text now lives only in the
 * book's simPlay `Document` (IP-37/IP-38) - so this builder gives no block at all until IP-38 rebuilds
 * it around the part's anchor in that document.
 */
class BookPartBuilderTest {

    private val pageDesign = ChapterPageDesign(
        titleStyle = StyleData(
            font = FontData("Garamond", 20, bold = true),
            textLineSpacing = 1.3,
            alignment = Alignment.CENTER
        ),
        titleAppendixStyle = StyleData(
            font = FontData("Garamond", 14, italic = true),
            textLineSpacing = 1.3,
            alignment = Alignment.CENTER
        ),
        textStyle = StyleData(
            font = FontData("Baskerville", 11),
            textLineSpacing = 1.6,
            alignment = Alignment.BLOCK
        )
    )

    /**
     * Use case: a chapter is built, so it gives no block at all - its text no longer lives on the
     * model - until IP-38 reads it from the anchor of the book's `Document`.
     */
    @Test
    fun aChapterGivesNoBlockUntilItsAnchorIsResolved() {
        val chapter = Chapter(name = "First")

        assertTrue(BookPartBuilder.build(chapter, pageDesign).isEmpty())
    }

    /**
     * Use case: a prolog and an epilog carry the same shape as a chapter, so both give no block
     * either, for the same reason.
     */
    @Test
    fun aPrologAndAnEpilogGiveNoBlockEither() {
        assertTrue(BookPartBuilder.build(Prolog(), pageDesign).isEmpty())
        assertTrue(BookPartBuilder.build(Epilog(), pageDesign).isEmpty())
    }
}

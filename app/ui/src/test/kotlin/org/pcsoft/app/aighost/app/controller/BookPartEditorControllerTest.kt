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

package org.pcsoft.app.aighost.app.controller

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.pcsoft.app.aighost.app.ui.component.ProjectListItem
import org.pcsoft.app.aighost.fx.model.project.ProjectProperty
import org.pcsoft.app.aighost.layouting.model.common.toPageLayout
import org.pcsoft.app.aighost.layouting.model.common.toTextStyle
import org.pcsoft.app.aighost.model.common.Alignment
import org.pcsoft.app.aighost.model.common.FontData
import org.pcsoft.app.aighost.model.common.StyleData
import org.pcsoft.app.aighost.model.project.Project
import org.pcsoft.app.aighost.model.project.book.Blurb
import org.pcsoft.app.aighost.model.project.book.Book
import org.pcsoft.app.aighost.model.project.book.Chapter
import org.pcsoft.app.aighost.model.project.book.Copyright
import org.pcsoft.app.aighost.model.project.book.Epilog
import org.pcsoft.app.aighost.model.project.book.Prolog
import org.pcsoft.app.aighost.model.project.design.BlurbPageDesign
import org.pcsoft.app.aighost.model.project.design.ChapterPageDesign
import org.pcsoft.app.aighost.model.project.design.CopyrightPageDesign
import org.pcsoft.app.aighost.model.project.design.Design
import org.pcsoft.app.aighost.model.project.design.EpilogPageDesign
import org.pcsoft.app.aighost.model.project.design.PrologPageDesign
import org.pcsoft.app.aighost.model.project.design.TitlePageDesign
import org.pcsoft.app.aighost.model.project.meta.Meta
import org.pcsoft.framework.simplay.engine.model.Document
import org.pcsoft.framework.simplay.engine.model.FlowPage
import org.pcsoft.framework.simplay.engine.model.TextBlock

/**
 * Developer tests for [BookPartEditorController].
 *
 * Since IP-39 the sheet always shows the whole book as one [Document]; a tree selection resolves onto
 * a `TextAnchor` id instead of a bound model property, and a prolog, a chapter, an epilog, the title
 * page and the copyright page are all read from and written back into the book's simPlay `Document`
 * through that anchor. Only the blurb still carries its own paragraphs on the model directly.
 */
class BookPartEditorControllerTest {

    private lateinit var project: ProjectProperty
    private lateinit var chapter: Chapter

    private fun style(size: Int = 12): StyleData =
        StyleData(font = FontData("Serif", size, bold = false, italic = false), alignment = Alignment.LEFT)

    private fun project(book: Book, meta: Meta = Meta(name = "My Novel", author = "Jane Doe")) = ProjectProperty(
        Project(
            meta = meta,
            design = Design(
                titlePage = TitlePageDesign(style(), style(), showAuthor = true, authorStyle = style()),
                copyrightPage = CopyrightPageDesign(style(), style(), showAuthor = false, authorStyle = style()),
                prologPage = PrologPageDesign(style(), style(), style()),
                blurbPage = BlurbPageDesign(style()),
                chapterPage = ChapterPageDesign(style(), style(), style()),
                epilogPage = EpilogPageDesign(style(), style(), style()),
                startWithEmptyPage = false,
                endWithEmptyPage = false
            ),
            book = book
        )
    )

    @BeforeEach
    fun setUp() {
        chapter = Chapter("first")
        project = project(
            Book(
                prolog = Prolog(included = true),
                chapters = listOf(chapter),
                epilog = Epilog(),
                blurb = Blurb(paragraph = listOf("A gripping tale."))
            )
        )
    }

    private val design: Design get() = project.value.design

    /**
     * Use case: nothing is picked, so the controller reports the empty mode with an empty anchor id.
     */
    @Test
    fun resolvesNothingToTheEmptyMode() {
        val resolution = BookPartEditorController.resolve(null)

        assertEquals(PartMode.NONE, resolution.mode)
        assertEquals("", resolution.anchorId)
    }

    /**
     * Use case: the prolog is picked, so it resolves to a writable book part keyed by the `"prolog"`
     * anchor.
     */
    @Test
    fun resolvesThePrologToItsAnchor() {
        val resolution = BookPartEditorController.resolve(ProjectListItem.PrologItem(project.value.book.prolog))

        assertEquals(PartMode.BOOK_PART, resolution.mode)
        assertEquals("prolog", resolution.anchorId)
    }

    /**
     * Use case: a chapter is picked, so it resolves to its own stable id - not its name, which the
     * user may still change - so a rename never breaks the anchor.
     */
    @Test
    fun resolvesAChapterToItsOwnId() {
        val resolution = BookPartEditorController.resolve(ProjectListItem.ChapterItem(chapter))

        assertEquals(PartMode.BOOK_PART, resolution.mode)
        assertEquals(chapter.id.toString(), resolution.anchorId)
    }

    /**
     * Use case: the title page and the copyright page are picked, so both resolve to their own anchor,
     * writable since IP-39 instead of read only.
     */
    @Test
    fun resolvesTheFrontMatterToItsAnchors() {
        val title = BookPartEditorController.resolve(ProjectListItem.TitlePageItem)
        val copyright = BookPartEditorController.resolve(ProjectListItem.CopyrightPageItem)

        assertEquals(PartMode.TITLE_PAGE, title.mode)
        assertEquals("title", title.anchorId)
        assertEquals(PartMode.COPYRIGHT_PAGE, copyright.mode)
        assertEquals("copyright", copyright.anchorId)
    }

    /**
     * Use case: the blurb is picked, so it resolves to the headingless writable mode, keyed by the
     * `"blurb"` anchor.
     */
    @Test
    fun resolvesTheBlurbToItsAnchor() {
        val resolution = BookPartEditorController.resolve(ProjectListItem.BlurbItem(project.value.book.blurb))

        assertEquals(PartMode.BLURB, resolution.mode)
        assertEquals("blurb", resolution.anchorId)
    }

    /**
     * Use case: a structural branch such as the chapters node is picked, so it resolves to the empty
     * mode.
     */
    @Test
    fun resolvesAStructuralNodeToTheEmptyMode() {
        assertEquals(PartMode.NONE, BookPartEditorController.resolve(ProjectListItem.Chapters).mode)
    }

    /**
     * Use case: a prolog with no page in the document yet is built into the whole document, so it
     * gives a single seeded block carrying only its anchor, and one matching target.
     */
    @Test
    fun bookPartWithNoPageYetGivesTheSeededAnchorBlock() {
        val plan = BookPartEditorController.buildWholeDocument(project.value, design, project.value.meta)

        val prologPage = plan.document.pages.single { it.id == "prolog" }
        assertEquals(listOf("\${prolog}"), prologPage.blocks.map { it.toString() })
        assertEquals(listOf(PartTarget.AnchorBlock("prolog", 0)), plan.targets.getValue("prolog"))
    }

    /**
     * Use case: a chapter whose page already carries text is built into the whole document, so that
     * text is read back unchanged, addressed by the chapter's own id.
     */
    @Test
    fun bookPartWithExistingTextReadsItBackByItsAnchor() {
        val anchorId = chapter.id.toString()
        project.bookProperty.document = Document(
            pages = listOf(
                FlowPage(
                    design.pageFormat.toPageLayout(),
                    listOf(TextBlock.of("\${$anchorId}Once upon a time.", design.chapterPage.textStyle.toTextStyle())),
                    id = anchorId
                )
            )
        )

        val plan = BookPartEditorController.buildWholeDocument(project.value, design, project.value.meta)

        val chapterPage = plan.document.pages.single { it.id == anchorId }
        assertEquals(listOf("\${$anchorId}Once upon a time."), chapterPage.blocks.map { it.toString() })
        assertEquals(listOf(PartTarget.AnchorBlock(anchorId, 0)), plan.targets.getValue(anchorId))
    }

    /**
     * Use case: the title page carries an author block appended from the meta data, so the whole
     * document's target list excludes it - only the anchor-addressed blocks before it are writable.
     */
    @Test
    fun titlePageExcludesItsAuthorBlockFromTheTargets() {
        val plan = BookPartEditorController.buildWholeDocument(project.value, design, project.value.meta)

        val titlePage = plan.document.pages.single { it.id == "title" }
        assertEquals(2, titlePage.blocks.size)
        assertEquals(listOf(PartTarget.AnchorBlock("title", 0)), plan.targets.getValue("title"))
    }

    /**
     * Use case: the copyright page is switched off, so the whole document has no page and no target
     * list for it at all.
     */
    @Test
    fun copyrightPageIsAbsentWhenNotIncluded() {
        val withoutCopyright = project(
            Book(
                copyright = Copyright(included = false),
                prolog = Prolog(),
                chapters = emptyList(),
                epilog = Epilog(),
                blurb = Blurb()
            )
        )

        val plan = BookPartEditorController.buildWholeDocument(withoutCopyright.value, design, withoutCopyright.value.meta)

        assertTrue(plan.document.pages.none { it.id == "copyright" })
        assertNull(plan.targets["copyright"])
    }

    /**
     * Use case: an empty blurb is still seeded with its anchor token, so the whole document has one
     * block and one matching paragraph target for it.
     */
    @Test
    fun emptyBlurbIsSeededWithOneParagraphTarget() {
        val emptyBlurbProject = project(
            Book(prolog = Prolog(), chapters = emptyList(), epilog = Epilog(), blurb = Blurb())
        )

        val plan = BookPartEditorController.buildWholeDocument(emptyBlurbProject.value, design, emptyBlurbProject.value.meta)

        val blurbPage = plan.document.pages.single { it.id == "blurb" }
        assertEquals(1, blurbPage.blocks.size)
        assertEquals(listOf(PartTarget.Paragraph(0)), plan.targets.getValue("blurb"))
    }

    /**
     * Use case: the blurb's first paragraph is edited, so the anchor token
     * [org.pcsoft.app.aighost.layouting.model.project.book.BlurbBuilder] mixes into its live block
     * text is stripped back off before the clean paragraph is stored.
     */
    @Test
    fun writingTheFirstBlurbParagraphStripsItsAnchorToken() {
        BookPartEditorController.writeModel(project, design, PartTarget.Paragraph(0), "\${blurb}A sharper piece of cover text.")

        assertEquals(listOf("A sharper piece of cover text."), project.value.book.blurb.paragraph)
        assertEquals("A sharper piece of cover text.", BookPartEditorController.readModel(project, PartTarget.Paragraph(0)))
    }

    /**
     * Use case: a further blurb paragraph carries no anchor token, so it is stored exactly as typed.
     */
    @Test
    fun writingAFurtherBlurbParagraphKeepsItUnchanged() {
        BookPartEditorController.writeModel(project, design, PartTarget.Paragraph(1), "A second paragraph.")

        assertEquals(listOf("A gripping tale.", "A second paragraph."), project.value.book.blurb.paragraph)
    }

    /**
     * Use case: a prolog is edited for the very first time, so its page does not exist in the book's
     * document yet - writing still creates it, with the typed text, instead of silently doing nothing.
     */
    @Test
    fun writingAnAnchorBlockCreatesItsPageWhenNoneExistsYet() {
        BookPartEditorController.writeModel(project, design, PartTarget.AnchorBlock("prolog", 0), "\${prolog}It was a dark night.")

        assertEquals(
            "\${prolog}It was a dark night.",
            BookPartEditorController.readModel(project, PartTarget.AnchorBlock("prolog", 0))
        )
        assertEquals(
            "\${prolog}It was a dark night.",
            project.value.book.document.pages.single { it.id == "prolog" }.blocks.single().toString()
        )
    }

    /**
     * Use case: a prolog whose page already exists is edited again, so the existing block is replaced
     * in place instead of a second page being created.
     */
    @Test
    fun writingAnAnchorBlockReplacesAnExistingBlock() {
        BookPartEditorController.writeModel(project, design, PartTarget.AnchorBlock("prolog", 0), "\${prolog}First draft.")

        BookPartEditorController.writeModel(project, design, PartTarget.AnchorBlock("prolog", 0), "\${prolog}Second draft.")

        assertEquals(1, project.value.book.document.pages.count { it.id == "prolog" })
        assertEquals(
            "\${prolog}Second draft.",
            project.value.book.document.pages.single { it.id == "prolog" }.blocks.single().toString()
        )
    }

    /**
     * Use case: an anchor block target names a page or a block position the document does not have,
     * so it reads back as the empty string instead of failing.
     */
    @Test
    fun readsAnUnknownAnchorBlockAsEmpty() {
        assertEquals("", BookPartEditorController.readModel(project, PartTarget.AnchorBlock("prolog", 0)))
        assertEquals("", BookPartEditorController.readModel(project, PartTarget.AnchorBlock("unknown", 0)))
    }

    /**
     * Use case: a paragraph target past the end of the blurb's paragraphs reads back as the empty
     * string instead of failing.
     */
    @Test
    fun readsAnUnsetParagraphTargetAsEmpty() {
        assertEquals("", BookPartEditorController.readModel(project, PartTarget.Paragraph(5)))
    }

    /**
     * Use case: a target whose page index falls outside the existing page's blocks is ignored instead
     * of throwing, since [org.pcsoft.framework.simplay.fx.PaperSheetView] never reports such a target.
     */
    @Test
    fun writingAnOutOfRangeAnchorBlockIndexIsIgnored() {
        BookPartEditorController.writeModel(project, design, PartTarget.AnchorBlock("prolog", 0), "\${prolog}First draft.")

        BookPartEditorController.writeModel(project, design, PartTarget.AnchorBlock("prolog", 5), "Ignored.")

        assertEquals(
            "\${prolog}First draft.",
            project.value.book.document.pages.single { it.id == "prolog" }.blocks.single().toString()
        )
        assertFalse(project.value.book.document.pages.single { it.id == "prolog" }.blocks.map { it.toString() }.contains("Ignored."))
    }

    /**
     * Use case: splitting in the middle of a paragraph yields two paragraphs whose concatenation
     * reconstructs the original text exactly - the character-range round trip IP-32 relies on.
     */
    @Test
    fun splitsAParagraphAtTheGivenOffset() {
        val result = BookPartEditorController.splitParagraph(listOf("Once upon a time."), 0, 10)

        assertEquals(listOf("Once upon ", "a time."), result)
        assertEquals("Once upon a time.", result[0] + result[1])
    }

    /**
     * Use case: splitting at offset zero or at the paragraph's own length yields an empty first or
     * second half instead of merging back with a neighbour.
     */
    @Test
    fun splitsAtTheParagraphBoundaryWithoutMerging() {
        assertEquals(listOf("", "Text"), BookPartEditorController.splitParagraph(listOf("Text"), 0, 0))
        assertEquals(listOf("Text", ""), BookPartEditorController.splitParagraph(listOf("Text"), 0, 4))
    }

    /**
     * Use case: merging a paragraph with its previous or its next neighbour concatenates the two texts
     * and removes the other paragraph from the list.
     */
    @Test
    fun mergesAParagraphWithEitherNeighbour() {
        val paragraphs = listOf("First.", "Second.", "Third.")

        assertEquals(
            listOf("First.Second.", "Third."),
            BookPartEditorController.mergeParagraph(paragraphs, 1, withPrevious = true)
        )
        assertEquals(
            listOf("First.", "Second.Third."),
            BookPartEditorController.mergeParagraph(paragraphs, 1, withPrevious = false)
        )
    }

    /**
     * Use case: merging at the very first or the very last paragraph has no such neighbour, so nothing
     * happens.
     */
    @Test
    fun mergingAtTheOuterEdgesIsANoOp() {
        val paragraphs = listOf("First.", "Second.")

        assertNull(BookPartEditorController.mergeParagraph(paragraphs, 0, withPrevious = true))
        assertNull(BookPartEditorController.mergeParagraph(paragraphs, 1, withPrevious = false))
    }

    /**
     * Use case: removing a paragraph drops it from the list, but the only remaining paragraph of a
     * part is never removed, so there is always somewhere left to type.
     */
    @Test
    fun removesAParagraphButKeepsTheLastOne() {
        assertEquals(
            listOf("First.", "Third."),
            BookPartEditorController.removeParagraph(listOf("First.", "Second.", "Third."), 1)
        )
        assertNull(BookPartEditorController.removeParagraph(listOf("Only one."), 0))
    }

    /**
     * Use case: moving a paragraph up or down swaps it with its neighbour; moving it past the start or
     * the end of the list is a no-op.
     */
    @Test
    fun movesAParagraphUpAndDown() {
        val paragraphs = listOf("First.", "Second.", "Third.")

        assertEquals(listOf("Second.", "First.", "Third."), BookPartEditorController.moveParagraph(paragraphs, 1, up = true))
        assertEquals(listOf("First.", "Third.", "Second."), BookPartEditorController.moveParagraph(paragraphs, 1, up = false))
        assertNull(BookPartEditorController.moveParagraph(paragraphs, 0, up = true))
        assertNull(BookPartEditorController.moveParagraph(paragraphs, 2, up = false))
    }
}

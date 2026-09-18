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
import org.pcsoft.app.aighost.app.Messages
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
     * Use case: one label is built per page of the whole document, in reading order, naming the title
     * page, the copyright page, the prolog, every chapter (numbered from one, with its own name) and
     * the epilog and the blurb - matching the page ids [BookPartEditorController.buildWholeDocument]
     * gives the very same book.
     */
    @Test
    fun buildsOnePageLabelPerPartInReadingOrder() {
        val labels = BookPartEditorController.buildPageLabels(project.value.book)

        assertEquals(
            listOf("title", "copyright", "prolog", chapter.id.toString(), "epilog", "blurb"),
            labels.map { it.pageId }
        )
        assertEquals(chapterPageLabel(1, "first"), labels.single { it.pageId == chapter.id.toString() }.text)
    }

    /**
     * Use case: the copyright page is switched off, so no label is built for it either, mirroring the
     * whole document, which has no page for it.
     */
    @Test
    fun pageLabelsExcludeTheCopyrightPageWhenNotIncluded() {
        val withoutCopyright = Book(
            copyright = Copyright(included = false),
            prolog = Prolog(),
            chapters = emptyList(),
            epilog = Epilog(),
            blurb = Blurb()
        )

        val labels = BookPartEditorController.buildPageLabels(withoutCopyright)

        assertTrue(labels.none { it.pageId == "copyright" })
    }

    /**
     * Use case: several chapters are labelled with their one-based reading position, not their index
     * in the underlying list, so the first chapter reads "Chapter 1", not "Chapter 0".
     */
    @Test
    fun pageLabelsNumberChaptersFromOne() {
        val second = Chapter("second")
        val withTwoChapters = Book(
            prolog = Prolog(),
            chapters = listOf(chapter, second),
            epilog = Epilog(),
            blurb = Blurb()
        )

        val labels = BookPartEditorController.buildPageLabels(withTwoChapters)

        assertEquals(chapterPageLabel(1, "first"), labels.single { it.pageId == chapter.id.toString() }.text)
        assertEquals(chapterPageLabel(2, "second"), labels.single { it.pageId == second.id.toString() }.text)
    }

    // Builds the expected text of a chapter's page label straight from the bundle, so this test does
    // not depend on the JVM's default locale resolving to the English base bundle.
    private fun chapterPageLabel(number: Int, name: String): String =
        java.text.MessageFormat.format(Messages["component.bookPartEditor.pageLabel.chapter"], number, name)

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

    private val style get() = design.chapterPage.textStyle.toTextStyle()

    /**
     * Use case: splitting in the middle of a block yields two blocks whose text concatenates back to
     * the original - the character-range round trip IP-32 relies on.
     */
    @Test
    fun splitsATextBlockAtTheGivenOffset() {
        val blocks = listOf(TextBlock.of("Once upon a time.", style))

        val result = BookPartEditorController.splitTextBlock(blocks, 0, 10, style)!!

        assertEquals(listOf("Once upon ", "a time."), result.map { it.toString() })
    }

    /**
     * Use case: a block carrying an anchor token at its very start keeps that token on the first half
     * of the split, so block `0` of a page never loses its `TextAnchor`.
     */
    @Test
    fun splittingAnAnchoredBlockKeepsTheAnchorOnTheFirstHalf() {
        val blocks = listOf(TextBlock.of("\${prolog}It was a dark night.", style))

        val result = BookPartEditorController.splitTextBlock(blocks, 0, 8, style)!!

        assertEquals("\${prolog}It was a", result[0].toString())
        assertEquals(" dark night.", result[1].toString())
    }

    /**
     * Use case: splitting at offset zero or at the block's own length yields an empty first or second
     * half instead of merging back with a neighbour.
     */
    @Test
    fun splitsAtTheBlockBoundaryWithoutMerging() {
        val blocks = listOf(TextBlock.of("Text", style))

        assertEquals(listOf("", "Text"), BookPartEditorController.splitTextBlock(blocks, 0, 0, style)!!.map { it.toString() })
        assertEquals(listOf("Text", ""), BookPartEditorController.splitTextBlock(blocks, 0, 4, style)!!.map { it.toString() })
    }

    /**
     * Use case: an out-of-range block index rejects the split instead of throwing.
     */
    @Test
    fun splittingAnOutOfRangeBlockIndexIsRejected() {
        assertNull(BookPartEditorController.splitTextBlock(listOf(TextBlock.of("Text", style)), 5, 0, style))
    }

    /**
     * Use case: merging a block with its previous or its next neighbour concatenates the two texts and
     * removes the other block from the list.
     */
    @Test
    fun mergesATextBlockWithEitherNeighbour() {
        val blocks = listOf(TextBlock.of("First.", style), TextBlock.of("Second.", style), TextBlock.of("Third.", style))

        assertEquals(
            listOf("First.Second.", "Third."),
            BookPartEditorController.mergeTextBlock(blocks, 1, withPrevious = true)!!.map { it.toString() }
        )
        assertEquals(
            listOf("First.", "Second.Third."),
            BookPartEditorController.mergeTextBlock(blocks, 1, withPrevious = false)!!.map { it.toString() }
        )
    }

    /**
     * Use case: merging block `0` with its next neighbour keeps the anchor token at the front of the
     * merged block, still at position `0`.
     */
    @Test
    fun mergingTheAnchoredBlockWithItsNextNeighbourKeepsTheAnchor() {
        val blocks = listOf(TextBlock.of("\${prolog}First.", style), TextBlock.of("Second.", style))

        val result = BookPartEditorController.mergeTextBlock(blocks, 0, withPrevious = false)!!

        assertEquals(listOf("\${prolog}First.Second."), result.map { it.toString() })
    }

    /**
     * Use case: merging at the very first or the very last block has no such neighbour, so nothing
     * happens.
     */
    @Test
    fun mergingAtTheOuterEdgesIsANoOp() {
        val blocks = listOf(TextBlock.of("First.", style), TextBlock.of("Second.", style))

        assertNull(BookPartEditorController.mergeTextBlock(blocks, 0, withPrevious = true))
        assertNull(BookPartEditorController.mergeTextBlock(blocks, 1, withPrevious = false))
    }

    /**
     * Use case: removing a block drops it from the list, but never the block at position `0`, which
     * carries the page's anchor, and never the last block left.
     */
    @Test
    fun removesATextBlockButKeepsBlockZeroAndTheLastOne() {
        val blocks = listOf(TextBlock.of("\${prolog}", style), TextBlock.of("Second.", style), TextBlock.of("Third.", style))

        assertEquals(
            listOf("\${prolog}", "Third."),
            BookPartEditorController.removeTextBlock(blocks, 1)!!.map { it.toString() }
        )
        assertNull(BookPartEditorController.removeTextBlock(blocks, 0))
        assertNull(BookPartEditorController.removeTextBlock(listOf(TextBlock.of("Only one.", style)), 0))
    }

    /**
     * Use case: moving a block up or down swaps it with its neighbour; moving it past the start or the
     * end of the list, or moving the anchored block `0` at all, is a no-op.
     */
    @Test
    fun movesATextBlockUpAndDownButNeverBlockZero() {
        val blocks = listOf(TextBlock.of("\${prolog}", style), TextBlock.of("Second.", style), TextBlock.of("Third.", style))

        assertEquals(
            listOf("\${prolog}", "Third.", "Second."),
            BookPartEditorController.moveTextBlock(blocks, 1, up = false)!!.map { it.toString() }
        )
        assertNull(BookPartEditorController.moveTextBlock(blocks, 1, up = true))
        assertNull(BookPartEditorController.moveTextBlock(blocks, 2, up = false))
        assertNull(BookPartEditorController.moveTextBlock(blocks, 0, up = false))
    }

    /**
     * Use case: a structural operation is applied to the named anchor's page as one transaction,
     * producing a new [Document] with only that page replaced.
     */
    @Test
    fun appliesAParagraphOperationToTheNamedAnchorsPage() {
        val document = Document(
            pages = listOf(
                FlowPage(design.pageFormat.toPageLayout(), listOf(TextBlock.of("\${prolog}Text.", style)), id = "prolog")
            )
        )

        val result = BookPartEditorController.applyParagraphOperation(document, "prolog") { blocks ->
            BookPartEditorController.splitTextBlock(blocks, 0, 9, style)
        }!!

        assertEquals(2, result.pages.single { it.id == "prolog" }.blocks.size)
    }

    /**
     * Use case: an unknown anchor id, or an operation that rejects the change (a boundary hit), leaves
     * the whole transaction as `null` instead of a partially applied document.
     */
    @Test
    fun rejectsTheWholeTransactionOnAnUnknownAnchorOrARejectedOperation() {
        val document = Document(
            pages = listOf(FlowPage(design.pageFormat.toPageLayout(), listOf(TextBlock.of("\${prolog}", style)), id = "prolog"))
        )

        assertNull(BookPartEditorController.applyParagraphOperation(document, "unknown") { it })
        assertNull(BookPartEditorController.applyParagraphOperation(document, "prolog") { blocks ->
            BookPartEditorController.removeTextBlock(blocks, 0)
        })
    }

    /**
     * Use case: a page's block still starts with its anchor token after an edit, so a native
     * `Backspace`/`Delete` merge may be accepted; a block `0` overwritten without it is rejected.
     */
    @Test
    fun detectsWhetherAPageStillKeepsItsAnchor() {
        val kept = FlowPage(design.pageFormat.toPageLayout(), listOf(TextBlock.of("\${prolog}Text.", style)), id = "prolog")
        val lost = FlowPage(design.pageFormat.toPageLayout(), listOf(TextBlock.of("Text.", style)), id = "prolog")
        val empty = FlowPage(design.pageFormat.toPageLayout(), emptyList(), id = "prolog")

        assertTrue(BookPartEditorController.pageKeepsAnchor(kept))
        assertFalse(BookPartEditorController.pageKeepsAnchor(lost))
        assertFalse(BookPartEditorController.pageKeepsAnchor(empty))
    }

    /**
     * Use case: the document-wide block ordinal `CaretModel.moveIntoBlock` expects sums the block
     * counts of every page before the target one, plus the page-local index.
     */
    @Test
    fun resolvesTheDocumentWideBlockIndexAcrossPages() {
        val document = Document(
            pages = listOf(
                FlowPage(design.pageFormat.toPageLayout(), listOf(TextBlock.of("A", style), TextBlock.of("B", style)), id = "prolog"),
                FlowPage(design.pageFormat.toPageLayout(), listOf(TextBlock.of("C", style)), id = "epilog"),
            )
        )

        assertEquals(0, BookPartEditorController.documentBlockIndex(document, "prolog", 0))
        assertEquals(1, BookPartEditorController.documentBlockIndex(document, "prolog", 1))
        assertEquals(2, BookPartEditorController.documentBlockIndex(document, "epilog", 0))
        assertNull(BookPartEditorController.documentBlockIndex(document, "unknown", 0))
    }

    /**
     * Use case: the character offset of a block relative to the document's linear position excludes
     * the zero-width anchor token of an earlier block, the same way `CaretModel.position` counts.
     */
    @Test
    fun resolvesTheBlockLocalCharOffsetExcludingAnchors() {
        val second = TextBlock.of("Second.", style)
        val document = Document(
            pages = listOf(
                FlowPage(
                    design.pageFormat.toPageLayout(),
                    listOf(TextBlock.of("\${prolog}First.", style), second),
                    id = "prolog"
                )
            )
        )

        // "First." counts as 6 characters (the anchor counts as none), so position 9 is 3 characters
        // into "Second.".
        assertEquals(3, BookPartEditorController.blockLocalCharOffset(document, second, 9))
    }
}

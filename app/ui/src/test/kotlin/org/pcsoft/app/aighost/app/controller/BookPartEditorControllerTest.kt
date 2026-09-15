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
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.pcsoft.app.aighost.app.ui.component.ProjectListItem
import org.pcsoft.app.aighost.fx.model.project.ProjectProperty
import org.pcsoft.app.aighost.model.common.Alignment
import org.pcsoft.app.aighost.model.common.FontData
import org.pcsoft.app.aighost.model.common.StyleData
import org.pcsoft.app.aighost.model.project.Project
import org.pcsoft.app.aighost.model.project.book.Blurb
import org.pcsoft.app.aighost.model.project.book.Book
import org.pcsoft.app.aighost.model.project.book.Chapter
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

/**
 * Developer tests for [BookPartEditorController].
 *
 * IP-36 removed the heading and the flowing text from every book part - that text now lives only in
 * the book's simPlay `Document` (IP-37/IP-38) - so [PartMode.BOOK_PART] resolves to no block and no
 * target here until IP-38 rebuilds the writing surface around the part's anchor; only the blurb, which
 * still carries its own paragraphs, and the read-only front matter are covered beyond routing.
 */
class BookPartEditorControllerTest {

    private lateinit var project: ProjectProperty

    private fun style(size: Int = 12): StyleData =
        StyleData(font = FontData("Serif", size, bold = false, italic = false), alignment = Alignment.LEFT)

    @BeforeEach
    fun setUp() {
        project = ProjectProperty(
            Project(
                meta = Meta(name = "My Novel", author = "Jane Doe"),
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
                book = Book(
                    prolog = Prolog(included = true),
                    chapters = listOf(Chapter("first")),
                    epilog = Epilog(),
                    blurb = Blurb(paragraph = listOf("A gripping tale."))
                )
            )
        )
    }

    private val design: Design get() = project.value.design

    /**
     * Use case: nothing is picked, so the controller reports the empty mode with no bound part.
     */
    @Test
    fun resolvesNothingToTheEmptyMode() {
        val resolution = BookPartEditorController.resolve(project, null)

        assertEquals(PartMode.NONE, resolution.mode)
        assertNull(resolution.boundPart)
        assertEquals("", resolution.partId)
    }

    /**
     * Use case: no project is open, so any picked node still resolves to the empty mode.
     */
    @Test
    fun resolvesToNothingWithoutAProject() {
        val resolution = BookPartEditorController.resolve(null, ProjectListItem.PrologItem(null))

        assertEquals(PartMode.NONE, resolution.mode)
    }

    /**
     * Use case: the prolog is picked, so it resolves to a writable book part bound to the prolog
     * property and keyed as "prolog".
     */
    @Test
    fun resolvesThePrologToAWritableBookPart() {
        val resolution = BookPartEditorController.resolve(project, ProjectListItem.PrologItem(project.value.book.prolog))

        assertEquals(PartMode.BOOK_PART, resolution.mode)
        assertEquals("prolog", resolution.partId)
        assertSame(project.bookProperty.prologProperty, resolution.boundPart)
    }

    /**
     * Use case: a chapter is picked, so it resolves to a writable book part whose id carries the
     * chapter name, so consecutive typing in different chapters never merges into one undo step.
     */
    @Test
    fun resolvesAChapterWithItsNameInTheId() {
        val resolution = BookPartEditorController.resolve(
            project,
            ProjectListItem.ChapterItem(project.value.book.chapters.single())
        )

        assertEquals(PartMode.BOOK_PART, resolution.mode)
        assertEquals("chapter:first", resolution.partId)
        assertNotNull(resolution.boundPart)
    }

    /**
     * Use case: the title page and the copyright page are picked, so both resolve to their read-only
     * mode with no bound part.
     */
    @Test
    fun resolvesTheFrontMatterToItsReadOnlyModes() {
        assertEquals(PartMode.TITLE_PAGE, BookPartEditorController.resolve(project, ProjectListItem.TitlePageItem).mode)
        assertEquals(
            PartMode.COPYRIGHT_PAGE,
            BookPartEditorController.resolve(project, ProjectListItem.CopyrightPageItem).mode
        )
        assertNull(BookPartEditorController.resolve(project, ProjectListItem.TitlePageItem).boundPart)
    }

    /**
     * Use case: the blurb is picked, so it resolves to the headingless writable mode; its paragraphs
     * are reached through the project, not through a bound part.
     */
    @Test
    fun resolvesTheBlurbToTheHeadinglessMode() {
        val resolution = BookPartEditorController.resolve(project, ProjectListItem.BlurbItem(project.value.book.blurb))

        assertEquals(PartMode.BLURB, resolution.mode)
        assertNull(resolution.boundPart)
    }

    /**
     * Use case: a structural branch such as the chapters node is picked, so it resolves to the empty
     * mode.
     */
    @Test
    fun resolvesAStructuralNodeToTheEmptyMode() {
        assertEquals(PartMode.NONE, BookPartEditorController.resolve(project, ProjectListItem.Chapters).mode)
    }

    /**
     * Use case: a book part (prolog, chapter, epilog) is picked, so it gives neither block nor target
     * - the text lives only in the book's `Document`, which this controller does not read yet.
     */
    @Test
    fun bookPartResolutionGivesNoBlockOrTarget() {
        val resolution = BookPartEditorController.resolve(project, ProjectListItem.PrologItem(project.value.book.prolog))

        val plan = BookPartEditorController.buildBlocks(project.value, design, resolution)

        assertTrue(plan.blocks.isEmpty())
        assertTrue(plan.targets.isEmpty())
    }

    /**
     * Use case: an empty blurb has nothing to show, so it is seeded with a single empty paragraph
     * block and a matching target, giving the user somewhere to type.
     */
    @Test
    fun seedsAnEmptyBlurbWithOneParagraph() {
        val emptyBlurbProject = ProjectProperty(project.value.copy(book = project.value.book.copy(blurb = Blurb())))
        val resolution = BookPartEditorController.resolve(
            emptyBlurbProject,
            ProjectListItem.BlurbItem(emptyBlurbProject.value.book.blurb)
        )

        val plan = BookPartEditorController.buildBlocks(emptyBlurbProject.value, design, resolution)

        assertEquals(1, plan.blocks.size)
        assertEquals("", plan.blocks.single().toString())
        assertEquals(listOf(PartTarget.Paragraph(0)), plan.targets)
    }

    /**
     * Use case: the title page is picked, so its blocks are built for display but carry no targets,
     * because a read-only sheet writes nothing back.
     */
    @Test
    fun buildsBlocksWithoutTargetsForTheReadOnlyTitlePage() {
        val resolution = BookPartEditorController.resolve(project, ProjectListItem.TitlePageItem)

        val plan = BookPartEditorController.buildBlocks(project.value, design, resolution)

        assertTrue(plan.targets.isEmpty())
    }

    /**
     * Use case: nothing is picked, so there are neither blocks nor targets to lay out.
     */
    @Test
    fun buildsNothingForTheEmptyMode() {
        val plan = BookPartEditorController.buildBlocks(
            project.value,
            design,
            BookPartEditorController.PartResolution(PartMode.NONE, null, "")
        )

        assertTrue(plan.blocks.isEmpty())
        assertTrue(plan.targets.isEmpty())
    }

    /**
     * Use case: the blurb is edited, so the text is written into the blurb paragraphs of the book,
     * which are reached through the project and not through a bound part.
     */
    @Test
    fun writesBlurbParagraphsThroughTheProject() {
        val resolution = BookPartEditorController.resolve(project, ProjectListItem.BlurbItem(project.value.book.blurb))

        BookPartEditorController.writeModel(project, resolution, PartTarget.Paragraph(0), "A sharper piece of cover text.")

        assertEquals(listOf("A sharper piece of cover text."), project.value.book.blurb.paragraph)
        assertEquals(
            "A sharper piece of cover text.",
            BookPartEditorController.readModel(project, resolution, PartTarget.Paragraph(0))
        )
    }

    /**
     * Use case: a target that does not resolve to a set field - a book part, since IP-36 - reads back
     * as the empty string instead of failing.
     */
    @Test
    fun readsAnUnsetTargetAsEmpty() {
        val resolution = BookPartEditorController.resolve(project, ProjectListItem.PrologItem(project.value.book.prolog))

        assertEquals("", BookPartEditorController.readModel(project, resolution, PartTarget.Paragraph(0)))
    }

    /**
     * Use case: the resolved part's paragraph list is looked up for the blurb, and not at all for a
     * book part, a read-only mode or the empty mode.
     */
    @Test
    fun resolvesTheParagraphListPerMode() {
        val bookPartResolution =
            BookPartEditorController.resolve(project, ProjectListItem.PrologItem(project.value.book.prolog))
        val blurbResolution = BookPartEditorController.resolve(project, ProjectListItem.BlurbItem(project.value.book.blurb))
        val titleResolution = BookPartEditorController.resolve(project, ProjectListItem.TitlePageItem)

        assertSame(
            project.bookProperty.blurbProperty.paragraphProperty,
            BookPartEditorController.paragraphListProperty(project, blurbResolution)
        )
        assertNull(BookPartEditorController.paragraphListProperty(project, bookPartResolution))
        assertNull(BookPartEditorController.paragraphListProperty(project, titleResolution))
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

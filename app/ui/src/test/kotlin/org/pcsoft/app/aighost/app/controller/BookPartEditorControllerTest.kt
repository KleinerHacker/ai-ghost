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
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.pcsoft.app.aighost.app.ui.component.ProjectListItem
import org.pcsoft.app.aighost.fx.model.project.ProjectProperty
import org.pcsoft.app.aighost.layouting.FixedTextMetrics
import org.pcsoft.app.aighost.layouting.GreedyLineBreaker
import org.pcsoft.app.aighost.layouting.LineBreaker
import org.pcsoft.app.aighost.layouting.model.common.toPageGeometry
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
 * The controller holds no state, so every test builds a project property, calls one function and
 * asserts its result. The line breaking runs through [FixedTextMetrics], so no JavaFX toolkit is
 * needed and every measurement is plain arithmetic.
 */
class BookPartEditorControllerTest {

    private lateinit var project: ProjectProperty

    private val breaker: LineBreaker = GreedyLineBreaker(FixedTextMetrics())

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
                    title = "My Novel",
                    prolog = Prolog(title = "Before It All", paragraph = listOf("The first paragraph.")),
                    chapters = listOf(Chapter("first", "The First Part", paragraph = listOf("Once upon a time."))),
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
     * Use case: the prolog carries a heading and a paragraph, so its blocks map to a title target
     * followed by one paragraph target.
     */
    @Test
    fun buildsBlocksAndTargetsForAWrittenBookPart() {
        val resolution = BookPartEditorController.resolve(project, ProjectListItem.PrologItem(project.value.book.prolog))

        val plan = BookPartEditorController.buildBlocks(project.value, design, resolution)

        assertTrue(plan.blocks.isNotEmpty())
        assertEquals(PartTarget.Title, plan.targets.first())
        assertTrue(plan.targets.contains(PartTarget.Paragraph(0)))
    }

    /**
     * Use case: an empty writable part has nothing to show, so it is seeded with a single empty
     * paragraph block and a matching target, giving the user somewhere to type.
     */
    @Test
    fun seedsAnEmptyWritablePartWithOneParagraph() {
        // The epilog is built empty in setUp: no heading, no appendix, no paragraphs.
        val resolution = BookPartEditorController.resolve(project, ProjectListItem.EpilogItem(project.value.book.epilog))

        val plan = BookPartEditorController.buildBlocks(project.value, design, resolution)

        assertEquals(1, plan.blocks.size)
        assertEquals("", plan.blocks.single().text)
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

        assertTrue(plan.blocks.isNotEmpty())
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
     * Use case: the blocks of a part are broken and paginated, so the result carries at least one
     * page and every laid out line belongs to one of the blocks.
     */
    @Test
    fun laysOutBlocksOntoPages() {
        val resolution = BookPartEditorController.resolve(project, ProjectListItem.PrologItem(project.value.book.prolog))
        val plan = BookPartEditorController.buildBlocks(project.value, design, resolution)
        val geometry = design.pageFormat.toPageGeometry()
        val columnWidth = BookPartEditorController.columnWidth(design, reported = 0.0)

        val layout = BookPartEditorController.layout(plan.blocks, geometry, columnWidth, breaker)

        assertTrue(layout.pages.isNotEmpty())
        assertTrue(layout.pages.flatMap { it.lines }.isNotEmpty())
    }

    /**
     * Use case: the flow view has not reported a width yet, so the column width falls back to the
     * plain content width of the page; once it reports one, that value is used.
     */
    @Test
    fun picksTheColumnWidthFromTheReportedValueOrThePage() {
        val fallback = BookPartEditorController.columnWidth(design, reported = 0.0)
        val expected = design.pageFormat.width - design.pageFormat.innerMargin - design.pageFormat.outerMargin

        assertEquals(expected, fallback)
        assertEquals(321.0, BookPartEditorController.columnWidth(design, reported = 321.0))
    }

    /**
     * Use case: a heading change on the sheet is written back, so the new title stands in the prolog
     * of the manuscript and reads back the same way.
     */
    @Test
    fun writesAndReadsTheHeadingOfABookPart() {
        val resolution = BookPartEditorController.resolve(project, ProjectListItem.PrologItem(project.value.book.prolog))

        BookPartEditorController.writeModel(project, resolution, PartTarget.Title, "A New Prolog Title")

        assertEquals("A New Prolog Title", project.value.book.prolog.title)
        assertEquals("A New Prolog Title", BookPartEditorController.readModel(project, resolution, PartTarget.Title))
    }

    /**
     * Use case: an existing paragraph is edited and a further one is added past the end of the list,
     * so both land in the paragraph list of the part.
     */
    @Test
    fun writesAnExistingParagraphAndAppendsANewOne() {
        val resolution = BookPartEditorController.resolve(project, ProjectListItem.PrologItem(project.value.book.prolog))

        BookPartEditorController.writeModel(project, resolution, PartTarget.Paragraph(0), "The rewritten first paragraph.")
        BookPartEditorController.writeModel(project, resolution, PartTarget.Paragraph(1), "A brand new second paragraph.")

        assertEquals(
            listOf("The rewritten first paragraph.", "A brand new second paragraph."),
            project.value.book.prolog.paragraph
        )
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
     * Use case: a target that does not resolve to a set field - a heading on the headingless blurb -
     * reads back as the empty string instead of failing.
     */
    @Test
    fun readsAnUnsetTargetAsEmpty() {
        val resolution = BookPartEditorController.resolve(project, ProjectListItem.BlurbItem(project.value.book.blurb))

        assertEquals("", BookPartEditorController.readModel(project, resolution, PartTarget.Title))
        assertFalse(BookPartEditorController.readModel(project, resolution, PartTarget.Paragraph(0)).isEmpty())
    }
}

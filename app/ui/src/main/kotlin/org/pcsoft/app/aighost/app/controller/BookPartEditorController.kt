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

import org.pcsoft.app.aighost.app.ui.component.ProjectListItem
import org.pcsoft.app.aighost.fx.model.project.ProjectProperty
import org.pcsoft.app.aighost.fx.model.project.book.BookPartProperty
import org.pcsoft.app.aighost.fx.model.project.book.ChapterProperty
import org.pcsoft.app.aighost.layouting.DocumentLayout
import org.pcsoft.app.aighost.layouting.LayoutEngine
import org.pcsoft.app.aighost.layouting.LineBreaker
import org.pcsoft.app.aighost.layouting.NonePageBreakPolicy
import org.pcsoft.app.aighost.layouting.PageGeometry
import org.pcsoft.app.aighost.layouting.TextBlock
import org.pcsoft.app.aighost.layouting.model.common.toTextStyle
import org.pcsoft.app.aighost.layouting.model.project.book.BlurbBuilder
import org.pcsoft.app.aighost.layouting.model.project.book.BookPartBuilder
import org.pcsoft.app.aighost.layouting.model.project.book.TitlePageBuilder
import org.pcsoft.app.aighost.layouting.model.project.meta.CopyrightPageBuilder
import org.pcsoft.app.aighost.model.project.Project
import org.pcsoft.app.aighost.model.project.design.Design

/**
 * The domain logic of the book part writing surface, kept out of its view model.
 *
 * The view model owns everything that has a lifetime - the flow view it drives, the incremental line
 * breaker with its cache, one string property per editable block, the caret, the undo history. This
 * controller owns none of that: every function here takes what it needs as an argument and returns a
 * plain result, the same way [IoController] reads and writes documents without holding an open one.
 * That is what makes the routing of a tree node, the assembly of the sheet and the mapping of a block
 * back onto a manuscript field testable on their own, without a JavaFX thread.
 *
 * The functions fall into three groups: [resolve] turns the picked project tree node into a
 * [PartResolution]; [columnWidth], [buildBlocks] and [layout] turn a resolution plus the design into
 * a laid out [DocumentLayout]; [readModel] and [writeModel] move a single block's text between the
 * sheet and the model.
 */
object BookPartEditorController {

    /**
     * Routes the picked project tree node onto the mode of the sheet and the part it edits.
     *
     * @param project the open project, `null` when none is open
     * @param item the picked node, `null` when nothing is picked
     * @return the mode, the bound part (for a prolog, chapter or epilog) and the id used as the undo
     * merge key; the bound part is `null` for every read-only or headingless mode
     */
    fun resolve(project: ProjectProperty?, item: ProjectListItem?): PartResolution {
        val book = project?.bookProperty?.value
        if (project == null || book == null) {
            return PartResolution(PartMode.NONE, null, "")
        }

        return when (item) {
            is ProjectListItem.TitlePageItem -> PartResolution(PartMode.TITLE_PAGE, null, "")
            is ProjectListItem.CopyrightPageItem -> PartResolution(PartMode.COPYRIGHT_PAGE, null, "")
            is ProjectListItem.PrologItem ->
                PartResolution(PartMode.BOOK_PART, project.bookProperty.prologProperty, "prolog")

            is ProjectListItem.EpilogItem ->
                PartResolution(PartMode.BOOK_PART, project.bookProperty.epilogProperty, "epilog")

            is ProjectListItem.ChapterItem ->
                PartResolution(PartMode.BOOK_PART, ChapterProperty.of(item.chapter), "chapter:" + item.chapter.name)

            is ProjectListItem.BlurbItem -> PartResolution(PartMode.BLURB, null, "")
            else -> PartResolution(PartMode.NONE, null, "")
        }
    }

    /**
     * Picks the column width a part is broken against.
     *
     * The flow view reports its own exact width only once it holds text controls; until then this
     * falls back to the plain content width of the page.
     *
     * @param design the design carrying the page format
     * @param reported the width reported by the flow view, `0.0` or less while it has none
     * @return the width to break against, never less than one point
     */
    fun columnWidth(design: Design, reported: Double): Double =
        if (reported > 0.0) {
            reported
        } else {
            (design.pageFormat.width - design.pageFormat.innerMargin - design.pageFormat.outerMargin)
                .coerceAtLeast(1.0)
        }

    /**
     * Builds the text blocks of the resolved part and the target each of them writes back to.
     *
     * A writable part that has no content yet is given a single empty paragraph block, so the flow
     * view has a control to report a column width from and the user has somewhere to type.
     *
     * @param project the open project
     * @param design the design of the project
     * @param resolution the resolved part, from [resolve]
     * @return the blocks in the order they are set and one target per block; both empty for
     * [PartMode.NONE] and for a book part with no bound model
     */
    fun buildBlocks(project: Project, design: Design, resolution: PartResolution): BlockPlan {
        val book = project.book
        val meta = project.meta

        return when (resolution.mode) {
            PartMode.TITLE_PAGE ->
                ensureWritableBlock(TitlePageBuilder.build(book, meta, design), design, resolution, emptyList())

            PartMode.COPYRIGHT_PAGE ->
                ensureWritableBlock(
                    CopyrightPageBuilder.build(book.copyright, meta, design),
                    design,
                    resolution,
                    emptyList()
                )

            PartMode.BLURB -> {
                val blocks = BlurbBuilder.build(book.blurb, design)
                val targets = book.blurb.paragraph.indices.map { PartTarget.Paragraph(it) }
                ensureWritableBlock(blocks, design, resolution, targets)
            }

            PartMode.BOOK_PART -> {
                val part = resolution.boundPart?.value ?: return BlockPlan(emptyList(), emptyList())
                val pageDesign = when (resolution.partId.substringBefore(':')) {
                    "prolog" -> design.prologPage
                    "epilog" -> design.epilogPage
                    else -> design.chapterPage
                }
                val blocks = BookPartBuilder.build(part, pageDesign)
                val targets = ArrayList<PartTarget>()
                if (part.title.isNotBlank()) {
                    targets += PartTarget.Title
                }
                part.titleAppendix.forEachIndexed { index, line ->
                    if (line.isNotBlank()) {
                        targets += PartTarget.AppendixLine(index)
                    }
                }
                part.paragraph.indices.forEach { targets += PartTarget.Paragraph(it) }
                ensureWritableBlock(blocks, design, resolution, targets)
            }

            PartMode.NONE -> BlockPlan(emptyList(), emptyList())
        }
    }

    /**
     * Breaks [blocks] against [columnWidth] with [breaker] and paginates the result onto [geometry].
     *
     * The pages carry no number: the editor shows one part on its own, the running number of the book
     * is a concern of the preview.
     *
     * @param blocks the blocks to lay out, never empty
     * @param geometry the page geometry the pages are placed onto
     * @param columnWidth the width to break against, from [columnWidth]
     * @param breaker the line breaker held by the view model, so its cache survives across keystrokes
     * @return the paginated layout
     */
    fun layout(
        blocks: List<TextBlock>,
        geometry: PageGeometry,
        columnWidth: Double,
        breaker: LineBreaker
    ): DocumentLayout {
        val text = breaker.breakText(blocks, columnWidth)
        return LayoutEngine.layout(
            text = text,
            geometry = geometry,
            startPageNumber = null,
            policy = NonePageBreakPolicy
        )
    }

    /**
     * Reads the current text of one block from the model.
     *
     * @param project the open project, needed for the blurb whose paragraphs are not on [PartResolution.boundPart]
     * @param resolution the resolved part
     * @param target the block whose text is read
     * @return the text, or the empty string when the target does not resolve to a set field
     */
    fun readModel(project: ProjectProperty, resolution: PartResolution, target: PartTarget): String {
        val boundPart = resolution.boundPart
        return when (target) {
            is PartTarget.Title -> boundPart?.titleProperty?.get().orEmpty()
            is PartTarget.AppendixLine -> boundPart?.titleAppendixProperty?.getOrNull(target.modelIndex).orEmpty()
            is PartTarget.Paragraph -> when (resolution.mode) {
                PartMode.BLURB -> blurbParagraphs(project).getOrNull(target.index).orEmpty()
                else -> boundPart?.paragraphProperty?.getOrNull(target.index).orEmpty()
            }
        }
    }

    /**
     * Writes the text of one block back into the model.
     *
     * A paragraph target one past the end of the list appends, so a freshly seeded empty paragraph
     * becomes a real one on the first keystroke.
     *
     * @param project the open project
     * @param resolution the resolved part
     * @param target the block whose text changed
     * @param value the new text
     */
    fun writeModel(project: ProjectProperty, resolution: PartResolution, target: PartTarget, value: String) {
        val boundPart = resolution.boundPart
        when (target) {
            is PartTarget.Title -> boundPart?.titleProperty?.set(value)

            is PartTarget.AppendixLine -> {
                val list = boundPart?.titleAppendixProperty ?: return
                if (target.modelIndex in list.indices) {
                    list[target.modelIndex] = value
                }
            }

            is PartTarget.Paragraph -> {
                val list =
                    if (resolution.mode == PartMode.BLURB) blurbParagraphs(project) else boundPart?.paragraphProperty
                list ?: return
                if (target.index in list.indices) {
                    list[target.index] = value
                } else if (target.index == list.size) {
                    list.add(value)
                }
            }
        }
    }

    private fun blurbParagraphs(project: ProjectProperty) =
        project.bookProperty.blurbProperty.paragraphProperty

    // A writable part needs at least one text control; an empty prolog gets one empty paragraph block.
    private fun ensureWritableBlock(
        blocks: List<TextBlock>,
        design: Design,
        resolution: PartResolution,
        targets: List<PartTarget>
    ): BlockPlan {
        if (blocks.isNotEmpty()) return BlockPlan(blocks, targets)
        if (resolution.mode != PartMode.BOOK_PART && resolution.mode != PartMode.BLURB) {
            return BlockPlan(blocks, targets)
        }

        val styleData = when (resolution.mode) {
            PartMode.BLURB -> design.blurbPage.textStyle
            else -> when (resolution.partId.substringBefore(':')) {
                "prolog" -> design.prologPage.textStyle
                "epilog" -> design.epilogPage.textStyle
                else -> design.chapterPage.textStyle
            }
        }
        return BlockPlan(
            listOf(TextBlock(text = "", style = styleData.toTextStyle())),
            listOf(PartTarget.Paragraph(0))
        )
    }

    /**
     * The resolved part behind a picked project tree node.
     *
     * @property mode which kind of part the sheet shows
     * @property boundPart the editable property of a prolog, chapter or epilog; `null` for every
     * other mode, including the blurb, whose paragraphs are reached through the project instead
     * @property partId a stable id of the part, used as the undo merge key and to pick the page design
     */
    data class PartResolution(
        val mode: PartMode,
        val boundPart: BookPartProperty<*>?,
        val partId: String
    )

    /**
     * The blocks of a part and the target each of them writes back to, in block order.
     *
     * @property blocks the text blocks to lay out
     * @property targets one entry per block, naming the model field it edits
     */
    data class BlockPlan(
        val blocks: List<TextBlock>,
        val targets: List<PartTarget>
    )
}

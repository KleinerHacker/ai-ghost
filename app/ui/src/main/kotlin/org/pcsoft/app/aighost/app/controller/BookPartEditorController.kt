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

import javafx.beans.property.ListProperty
import org.pcsoft.app.aighost.app.ui.component.ProjectListItem
import org.pcsoft.app.aighost.fx.model.project.ProjectProperty
import org.pcsoft.app.aighost.fx.model.project.book.BookPartProperty
import org.pcsoft.app.aighost.fx.model.project.book.ChapterProperty
import org.pcsoft.app.aighost.layouting.model.common.toPageLayout
import org.pcsoft.app.aighost.layouting.model.common.toTextStyle
import org.pcsoft.app.aighost.layouting.model.project.book.BlurbBuilder
import org.pcsoft.app.aighost.layouting.model.project.book.BookPartBuilder
import org.pcsoft.app.aighost.layouting.model.project.book.TitlePageBuilder
import org.pcsoft.app.aighost.layouting.model.project.meta.CopyrightPageBuilder
import org.pcsoft.app.aighost.model.project.Project
import org.pcsoft.app.aighost.model.project.design.Design
import org.pcsoft.framework.simplay.engine.model.FlowPage
import org.pcsoft.framework.simplay.engine.model.Page
import org.pcsoft.framework.simplay.engine.model.SinglePage
import org.pcsoft.framework.simplay.engine.model.TextBlock

/**
 * The domain logic of the book part writing surface, kept out of its view model.
 *
 * The view model owns everything that has a lifetime - the sheet it drives, one string property per
 * editable block, the caret, the undo history. This controller owns none of that: every function here
 * takes what it needs as an argument and returns a plain result, the same way [IoController] reads and
 * writes documents without holding an open one. That is what makes the routing of a tree node, the
 * assembly of the sheet's blocks and the mapping of a block back onto a manuscript field testable on
 * their own, without a JavaFX toolkit.
 *
 * Since IP-38 a prolog, a chapter and an epilog are read from and written back into the book's simPlay
 * `Document` through the anchor id of their page - `"prolog"`, `"epilog"` or a chapter's
 * `id.toString()` - via [org.pcsoft.app.aighost.layouting.model.project.book.BookPartBuilder]. Only the
 * blurb, which still carries its own paragraphs, and the read-only title and copyright pages keep a
 * different path.
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
                PartResolution(
                    PartMode.BOOK_PART,
                    ChapterProperty.of(item.chapter),
                    "chapter:" + item.chapter.id
                )

            is ProjectListItem.BlurbItem -> PartResolution(PartMode.BLURB, null, "")
            else -> PartResolution(PartMode.NONE, null, "")
        }
    }

    /**
     * Builds the text blocks of the resolved part and the target each of them writes back to.
     *
     * A writable part that has no content yet is given a single empty paragraph block, so the sheet
     * has somewhere to place a caret and the user has somewhere to type; [PartMode.BOOK_PART] never
     * needs that seed of its own, since [BookPartBuilder] already seeds an anchor-only block for a
     * part with no page yet.
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
                BlockPlan(TitlePageBuilder.build(book.document, meta, design), emptyList())

            PartMode.COPYRIGHT_PAGE ->
                BlockPlan(CopyrightPageBuilder.build(book.document, book.copyright, meta, design), emptyList())

            PartMode.BLURB -> {
                val blocks = BlurbBuilder.build(book.blurb, design)
                val targets = book.blurb.paragraph.indices.map { PartTarget.Paragraph(it) }
                ensureWritableBlock(blocks, design, resolution, targets)
            }

            PartMode.BOOK_PART -> {
                if (resolution.boundPart?.value == null) return BlockPlan(emptyList(), emptyList())
                val anchorId = anchorIdOf(resolution)
                val pageDesign = pageDesignOf(resolution, design)
                val blocks = BookPartBuilder.build(book.document, anchorId, pageDesign.textStyle.toTextStyle())
                val targets = blocks.indices.map { PartTarget.AnchorBlock(anchorId, it) }
                BlockPlan(blocks, targets)
            }

            PartMode.NONE -> BlockPlan(emptyList(), emptyList())
        }
    }

    /**
     * Reads the current text of one block from the model.
     *
     * @param project the open project, needed for the blurb whose paragraphs are not on
     * [PartResolution.boundPart], and for an anchor block, whose text lives on [ProjectProperty.bookProperty]
     * @param resolution the resolved part
     * @param target the block whose text is read
     * @return the text, or the empty string when the target does not resolve to a set field
     */
    fun readModel(project: ProjectProperty, resolution: PartResolution, target: PartTarget): String =
        when (target) {
            is PartTarget.Paragraph -> paragraphListProperty(project, resolution)?.getOrNull(target.index).orEmpty()
            is PartTarget.AnchorBlock -> pageOf(project, target.anchorId)
                ?.blocks?.getOrNull(target.blockIndex)?.toString().orEmpty()
        }

    /**
     * Writes the text of one block back into the model.
     *
     * A paragraph target one past the end of the list appends, so a freshly seeded empty paragraph
     * becomes a real one on the first keystroke. An anchor block target's page may not exist in the
     * document yet either - [buildBlocks] only ever seeds a block for display, it never persists it -
     * so the very first edit of a prolog, a chapter or an epilog creates that page here, the same way
     * a fresh paragraph target creates its entry.
     *
     * @param project the open project
     * @param design the design of the project, needed to lay out a page an anchor block target
     * creates because none existed yet
     * @param resolution the resolved part
     * @param target the block whose text changed
     * @param value the new text
     */
    fun writeModel(project: ProjectProperty, design: Design, resolution: PartResolution, target: PartTarget, value: String) {
        when (target) {
            is PartTarget.Paragraph -> {
                val list = paragraphListProperty(project, resolution) ?: return
                if (target.index in list.indices) {
                    list[target.index] = value
                } else if (target.index == list.size) {
                    list.add(value)
                }
            }

            is PartTarget.AnchorBlock -> {
                val document = project.bookProperty.document ?: return
                val pageIndex = document.pages.indexOfFirst { it.id == target.anchorId }

                if (pageIndex < 0) {
                    if (target.blockIndex != 0) return
                    val style = pageDesignOf(resolution, design).textStyle.toTextStyle()
                    val newPage = FlowPage(design.pageFormat.toPageLayout(), listOf(TextBlock.of(value, style)), id = target.anchorId)
                    project.bookProperty.document = document.copy(pages = document.pages + newPage)
                    return
                }

                val page = document.pages[pageIndex]
                if (target.blockIndex !in page.blocks.indices) return

                val newBlock = TextBlock.of(value, page.blocks[target.blockIndex].style)
                val newBlocks = page.blocks.toMutableList().apply { this[target.blockIndex] = newBlock }
                val newPages = document.pages.toMutableList().apply { this[pageIndex] = page.withBlocks(newBlocks) }
                project.bookProperty.document = document.copy(pages = newPages)
            }
        }
    }

    /**
     * Resolves the paragraph list a structural operation - split, merge, move or removal - acts on.
     *
     * @param project the open project, needed for the blurb whose paragraphs are not on [PartResolution.boundPart]
     * @param resolution the resolved part
     * @return the paragraph list, or `null` for a mode with no plain paragraph list of its own -
     * [PartMode.BOOK_PART] included, whose blocks are addressed through [PartTarget.AnchorBlock] instead
     */
    fun paragraphListProperty(project: ProjectProperty, resolution: PartResolution): ListProperty<String>? =
        when (resolution.mode) {
            PartMode.BLURB -> blurbParagraphs(project)
            else -> null
        }

    private fun blurbParagraphs(project: ProjectProperty) =
        project.bookProperty.blurbProperty.paragraphProperty

    /** The anchor id of the resolved part - `"prolog"`, `"epilog"` or a chapter's `id.toString()`. */
    private fun anchorIdOf(resolution: PartResolution): String =
        when (val prefix = resolution.partId.substringBefore(':')) {
            "prolog", "epilog" -> prefix
            else -> resolution.partId.substringAfter(':')
        }

    /** The page design the resolved [PartMode.BOOK_PART] part is styled with. */
    private fun pageDesignOf(resolution: PartResolution, design: Design) =
        when (resolution.partId.substringBefore(':')) {
            "prolog" -> design.prologPage
            "epilog" -> design.epilogPage
            else -> design.chapterPage
        }

    /** The page of [ProjectProperty.bookProperty]'s document whose id is [anchorId], if any. */
    private fun pageOf(project: ProjectProperty, anchorId: String): Page? =
        project.bookProperty.document?.pages?.firstOrNull { it.id == anchorId }

    /** Returns a copy of this page with [blocks] in place of its own, keeping its layout and id. */
    private fun Page.withBlocks(blocks: List<TextBlock>): Page = when (this) {
        is FlowPage -> copy(blocks = blocks)
        is SinglePage -> copy(blocks = blocks)
    }

    /**
     * Splits the paragraph at [index] into two, at [charOffset].
     *
     * Not called by this plan; kept for IP-32, which wires it to a key handler of its own.
     *
     * @param paragraphs the paragraph list to split in
     * @param index the paragraph to split
     * @param charOffset the character offset the split falls at; `0` and the paragraph's own length
     * are valid and yield an empty first or second half
     * @return the paragraph list with the split applied
     */
    fun splitParagraph(paragraphs: List<String>, index: Int, charOffset: Int): List<String> {
        val text = paragraphs[index]
        val offset = charOffset.coerceIn(0, text.length)
        val result = paragraphs.toMutableList()
        result[index] = text.substring(0, offset)
        result.add(index + 1, text.substring(offset))
        return result
    }

    /**
     * Merges the paragraph at [index] with a neighbour.
     *
     * Not called by this plan; kept for IP-32, which wires it to a key handler of its own.
     *
     * @param paragraphs the paragraph list to merge in
     * @param index the paragraph the merge was requested from
     * @param withPrevious `true` to merge with the paragraph before it, `false` for the one after it
     * @return the paragraph list with the merge applied, or `null` when [index] has no such neighbour
     */
    fun mergeParagraph(paragraphs: List<String>, index: Int, withPrevious: Boolean): List<String>? {
        val otherIndex = if (withPrevious) index - 1 else index + 1
        if (otherIndex !in paragraphs.indices) return null

        val firstIndex = if (withPrevious) otherIndex else index
        val secondIndex = if (withPrevious) index else otherIndex
        val result = paragraphs.toMutableList()
        result[firstIndex] = paragraphs[firstIndex] + paragraphs[secondIndex]
        result.removeAt(secondIndex)
        return result
    }

    /**
     * Removes the paragraph at [index], keeping at least one paragraph.
     *
     * Not called by this plan; kept for IP-32, which wires it to a key handler of its own.
     *
     * @param paragraphs the paragraph list to remove from
     * @param index the paragraph to remove
     * @return the paragraph list without that paragraph, or `null` when it is the only one left
     */
    fun removeParagraph(paragraphs: List<String>, index: Int): List<String>? {
        if (paragraphs.size <= 1) return null
        val result = paragraphs.toMutableList()
        result.removeAt(index)
        return result
    }

    /**
     * Moves the paragraph at [index] one position towards the start or the end of the part.
     *
     * Not called by this plan; kept for IP-32, which wires it to a key handler of its own.
     *
     * @param paragraphs the paragraph list to move in
     * @param index the paragraph to move
     * @param up `true` to move it towards the start, `false` towards the end
     * @return the paragraph list with the move applied, or `null` when [index] already sits at that end
     */
    fun moveParagraph(paragraphs: List<String>, index: Int, up: Boolean): List<String>? {
        val otherIndex = if (up) index - 1 else index + 1
        if (otherIndex !in paragraphs.indices) return null

        val result = paragraphs.toMutableList()
        val moved = result.removeAt(index)
        result.add(otherIndex, moved)
        return result
    }

    // A writable part needs at least one block; an empty blurb gets one empty paragraph block.
    private fun ensureWritableBlock(
        blocks: List<TextBlock>,
        design: Design,
        resolution: PartResolution,
        targets: List<PartTarget>
    ): BlockPlan {
        if (blocks.isNotEmpty()) return BlockPlan(blocks, targets)
        if (resolution.mode != PartMode.BLURB) {
            return BlockPlan(blocks, targets)
        }

        return BlockPlan(
            listOf(TextBlock.of("", design.blurbPage.textStyle.toTextStyle())),
            listOf(PartTarget.Paragraph(0))
        )
    }

    /**
     * The resolved part behind a picked project tree node.
     *
     * @property mode which kind of part the sheet shows
     * @property boundPart the editable property of a prolog, chapter or epilog; `null` for every
     * other mode, including the blurb, whose paragraphs are reached through the project instead
     * @property partId a stable id of the part, used as the undo merge key and to pick the page design -
     * `"prolog"`, `"epilog"` or `"chapter:" + the chapter's id` for [PartMode.BOOK_PART]
     */
    data class PartResolution(
        val mode: PartMode,
        val boundPart: BookPartProperty<*>?,
        val partId: String
    )

    /**
     * The blocks of a part and the target each of them writes back to, in block order.
     *
     * @property blocks the text blocks `PaperSheetView` lays out and paginates on its own
     * @property targets one entry per block, naming the model field it edits
     */
    data class BlockPlan(
        val blocks: List<TextBlock>,
        val targets: List<PartTarget>
    )
}

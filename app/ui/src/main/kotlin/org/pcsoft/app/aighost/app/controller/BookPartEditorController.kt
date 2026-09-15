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
import org.pcsoft.app.aighost.layouting.model.common.toTextStyle
import org.pcsoft.app.aighost.layouting.model.project.book.BlurbBuilder
import org.pcsoft.app.aighost.layouting.model.project.book.BookPartBuilder
import org.pcsoft.app.aighost.layouting.model.project.book.TitlePageBuilder
import org.pcsoft.app.aighost.layouting.model.project.meta.CopyrightPageBuilder
import org.pcsoft.app.aighost.model.project.Project
import org.pcsoft.app.aighost.model.project.design.Design
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
 * IP-36 removed the heading and the flowing text from [org.pcsoft.app.aighost.model.project.book.BookPart]:
 * that text now lives only in the book's simPlay `Document`, addressed through the part's anchor
 * (IP-37/IP-38). Until IP-38 rebuilds the writing surface around that anchor, [PartMode.BOOK_PART]
 * (prolog, chapter, epilog) therefore resolves to no block and no target at all; only the blurb - which
 * still carries its own paragraphs - and the read-only front matter keep working here.
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
     * Builds the text blocks of the resolved part and the target each of them writes back to.
     *
     * A writable part that has no content yet is given a single empty paragraph block, so the sheet
     * has somewhere to place a caret and the user has somewhere to type. [PartMode.BOOK_PART] carries
     * no such field anymore (see the class KDoc), so it never gets seeded.
     *
     * @param project the open project
     * @param design the design of the project
     * @param resolution the resolved part, from [resolve]
     * @return the blocks in the order they are set and one target per block; both empty for
     * [PartMode.NONE], for [PartMode.BOOK_PART] and for a book part with no bound model
     */
    fun buildBlocks(project: Project, design: Design, resolution: PartResolution): BlockPlan {
        val book = project.book
        val meta = project.meta

        return when (resolution.mode) {
            PartMode.TITLE_PAGE ->
                BlockPlan(TitlePageBuilder.build(book, meta, design), emptyList())

            PartMode.COPYRIGHT_PAGE ->
                BlockPlan(CopyrightPageBuilder.build(book.copyright, meta, design), emptyList())

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
                // TODO(IP-38): build the blocks and targets from the part's anchor in the book's
                //  Document instead of the empty list BookPartBuilder gives until then.
                BlockPlan(BookPartBuilder.build(part, pageDesign), emptyList())
            }

            PartMode.NONE -> BlockPlan(emptyList(), emptyList())
        }
    }

    /**
     * Reads the current text of one block from the model.
     *
     * @param project the open project, needed for the blurb whose paragraphs are not on [PartResolution.boundPart]
     * @param resolution the resolved part
     * @param target the block whose text is read
     * @return the text, or the empty string when the target does not resolve to a set field
     */
    fun readModel(project: ProjectProperty, resolution: PartResolution, target: PartTarget): String =
        when (target) {
            is PartTarget.Paragraph -> paragraphListProperty(project, resolution)?.getOrNull(target.index).orEmpty()
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
        when (target) {
            is PartTarget.Paragraph -> {
                val list = paragraphListProperty(project, resolution) ?: return
                if (target.index in list.indices) {
                    list[target.index] = value
                } else if (target.index == list.size) {
                    list.add(value)
                }
            }
        }
    }

    /**
     * Resolves the paragraph list a structural operation - split, merge, move or removal - acts on.
     *
     * @param project the open project, needed for the blurb whose paragraphs are not on [PartResolution.boundPart]
     * @param resolution the resolved part
     * @return the paragraph list, or `null` for a mode with no paragraphs of its own - which, since
     * IP-36, includes [PartMode.BOOK_PART] until IP-38 rebuilds it around the book's anchors
     */
    fun paragraphListProperty(project: ProjectProperty, resolution: PartResolution): ListProperty<String>? =
        when (resolution.mode) {
            PartMode.BLURB -> blurbParagraphs(project)
            else -> null
        }

    private fun blurbParagraphs(project: ProjectProperty) =
        project.bookProperty.blurbProperty.paragraphProperty

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
     * @property blocks the text blocks `PaperSheetView` lays out and paginates on its own
     * @property targets one entry per block, naming the model field it edits
     */
    data class BlockPlan(
        val blocks: List<TextBlock>,
        val targets: List<PartTarget>
    )
}

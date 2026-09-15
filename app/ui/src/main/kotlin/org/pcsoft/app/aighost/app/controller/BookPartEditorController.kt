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
import org.pcsoft.app.aighost.layouting.model.common.toPageLayout
import org.pcsoft.app.aighost.layouting.model.common.toTextStyle
import org.pcsoft.app.aighost.layouting.model.project.BookDocumentBuilder
import org.pcsoft.app.aighost.model.project.Project
import org.pcsoft.app.aighost.model.project.design.Design
import org.pcsoft.app.aighost.model.project.meta.Meta
import org.pcsoft.framework.simplay.engine.model.Document
import org.pcsoft.framework.simplay.engine.model.FlowPage
import org.pcsoft.framework.simplay.engine.model.Page
import org.pcsoft.framework.simplay.engine.model.SinglePage
import org.pcsoft.framework.simplay.engine.model.TextBlock

/**
 * The domain logic of the book's writing surface, kept out of its view model.
 *
 * Since IP-39 the sheet always shows the whole book as one [Document], built by
 * [BookDocumentBuilder]; a tree selection only navigates to the picked part's `TextAnchor`, it never
 * swaps the document. This controller resolves a picked [ProjectListItem] onto that anchor id
 * ([resolve]), builds the whole-book document together with the write-back target of every block
 * ([buildWholeDocument]), and maps a block back onto the manuscript field it stands for
 * ([readModel]/[writeModel]) - the same way [org.pcsoft.app.aighost.app.controller.IoController] reads
 * and writes documents without holding an open one, so all of this is testable without a JavaFX
 * toolkit.
 *
 * A prolog, a chapter, an epilog, the title page and the copyright page are all read from and written
 * back into the book's simPlay `Document` through the anchor id of their page - `"prolog"`,
 * `"epilog"`, `"title"`, `"copyright"` or a chapter's `id.toString()` - via
 * [org.pcsoft.app.aighost.layouting.model.project.book.BookPartBuilder]. The title and the copyright
 * page may carry one further, non-anchored block with the author's name, rebuilt fresh from [Meta] on
 * every call; that block is never part of a block's write-back target. Only the blurb, which still
 * carries its own paragraphs on the model directly, keeps a different path:
 * [org.pcsoft.app.aighost.layouting.model.project.book.BlurbBuilder] embeds the blurb's anchor token
 * inside the first paragraph's own text instead of a block of its own, so [writeModel] strips it back
 * off before the clean paragraph is stored.
 */
object BookPartEditorController {

    /**
     * The anchor token [org.pcsoft.app.aighost.layouting.model.project.book.BlurbBuilder] embeds at
     * the start of the blurb's first block; stripped back off before the paragraph is stored.
     */
    private const val BLURB_ANCHOR_TOKEN = "\${blurb}"

    /**
     * Resolves the picked project tree node onto the `TextAnchor` id `PaperSheetView` navigates to.
     *
     * @param item the picked node, `null` when nothing is picked
     * @return the mode of the resolved part and its anchor id; the anchor id is empty for
     * [PartMode.NONE]
     */
    fun resolve(item: ProjectListItem?): PartResolution = when (item) {
        is ProjectListItem.TitlePageItem -> PartResolution(PartMode.TITLE_PAGE, "title")
        is ProjectListItem.CopyrightPageItem -> PartResolution(PartMode.COPYRIGHT_PAGE, "copyright")
        is ProjectListItem.PrologItem -> PartResolution(PartMode.BOOK_PART, "prolog")
        is ProjectListItem.EpilogItem -> PartResolution(PartMode.BOOK_PART, "epilog")
        is ProjectListItem.ChapterItem -> PartResolution(PartMode.BOOK_PART, item.chapter.id.toString())
        is ProjectListItem.BlurbItem -> PartResolution(PartMode.BLURB, "blurb")
        else -> PartResolution(PartMode.NONE, "")
    }

    /**
     * Builds the whole-book document together with the write-back target of every one of its blocks.
     *
     * @param project the open project
     * @param design the design of the project
     * @param meta the project meta data, needed for the author blocks of the title and the copyright
     * page
     * @return the document [org.pcsoft.framework.simplay.fx.PaperSheetView] is given, and one list of
     * [PartTarget] per page, keyed by the page's id (its anchor id)
     */
    fun buildWholeDocument(project: Project, design: Design, meta: Meta): WholeDocumentPlan {
        val document = BookDocumentBuilder.build(project.book, design, meta)
        val targets = document.pages.associate { page -> page.id to targetsOf(page, design, meta) }
        return WholeDocumentPlan(document, targets)
    }

    /**
     * Reads the current text of one block from the model.
     *
     * @param project the open project, needed for the blurb whose paragraphs are not addressed by an
     * anchor, and for an anchor block, whose text lives on [ProjectProperty.bookProperty]
     * @param target the block whose text is read
     * @return the text, or the empty string when the target does not resolve to a set field
     */
    fun readModel(project: ProjectProperty, target: PartTarget): String =
        when (target) {
            is PartTarget.Paragraph -> project.bookProperty.blurbProperty.paragraphProperty
                .getOrNull(target.index).orEmpty()

            is PartTarget.AnchorBlock -> pageOf(project, target.anchorId)
                ?.blocks?.getOrNull(target.blockIndex)?.toString().orEmpty()
        }

    /**
     * Writes the text of one block back into the model.
     *
     * A paragraph target one past the end of the list appends. An anchor block target's page may not
     * exist in the document yet - the very first edit of a prolog, a chapter, an epilog, the title or
     * the copyright page creates that page here.
     *
     * @param project the open project
     * @param design the design of the project, needed to lay out a page an anchor block target
     * creates because none existed yet
     * @param target the block whose text changed
     * @param value the new text
     */
    fun writeModel(project: ProjectProperty, design: Design, target: PartTarget, value: String) {
        when (target) {
            is PartTarget.Paragraph -> {
                val list = project.bookProperty.blurbProperty.paragraphProperty
                val cleaned = if (target.index == 0) value.removePrefix(BLURB_ANCHOR_TOKEN) else value
                if (target.index in list.indices) {
                    list[target.index] = cleaned
                } else if (target.index == list.size) {
                    list.add(cleaned)
                }
            }

            is PartTarget.AnchorBlock -> {
                val document = project.bookProperty.document ?: return
                val pageIndex = document.pages.indexOfFirst { it.id == target.anchorId }

                if (pageIndex < 0) {
                    if (target.blockIndex != 0) return
                    val style = styleOfAnchor(target.anchorId, design)
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

    /** The targets of every block of [page], in block order; empty for a page with no target at all. */
    private fun targetsOf(page: Page, design: Design, meta: Meta): List<PartTarget> = when (page.id) {
        "title" -> anchorTargets(page, "title", design.titlePage.showAuthor && meta.author.isNotBlank())
        "copyright" -> anchorTargets(page, "copyright", design.copyrightPage.showAuthor && meta.author.isNotBlank())
        "blurb" -> page.blocks.indices.map { PartTarget.Paragraph(it) }
        else -> anchorTargets(page, page.id, hasTrailingAuthorBlock = false)
    }

    // The title and the copyright page may carry one further, non-anchored author block, rebuilt fresh
    // from Meta on every call; it is never a write-back target of its own.
    private fun anchorTargets(page: Page, anchorId: String, hasTrailingAuthorBlock: Boolean): List<PartTarget> {
        val count = if (hasTrailingAuthorBlock) (page.blocks.size - 1).coerceAtLeast(0) else page.blocks.size
        return (0 until count).map { PartTarget.AnchorBlock(anchorId, it) }
    }

    /** The style a freshly seeded block of the anchor [anchorId] is built with. */
    private fun styleOfAnchor(anchorId: String, design: Design) = when (anchorId) {
        "title" -> design.titlePage.titleStyle.toTextStyle()
        "copyright" -> design.copyrightPage.copyrightStyle.toTextStyle()
        "prolog" -> design.prologPage.textStyle.toTextStyle()
        "epilog" -> design.epilogPage.textStyle.toTextStyle()
        else -> design.chapterPage.textStyle.toTextStyle()
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

    /**
     * The resolved part behind a picked project tree node.
     *
     * @property mode which kind of part is picked
     * @property anchorId the `TextAnchor` id `PaperSheetView` navigates to; empty for [PartMode.NONE]
     */
    data class PartResolution(
        val mode: PartMode,
        val anchorId: String
    )

    /**
     * The whole-book document and the write-back target of every one of its blocks.
     *
     * @property document the document [org.pcsoft.framework.simplay.fx.PaperSheetView] is given
     * @property targets one list of [PartTarget] per page, keyed by the page's id
     */
    data class WholeDocumentPlan(
        val document: Document,
        val targets: Map<String, List<PartTarget>>
    )
}

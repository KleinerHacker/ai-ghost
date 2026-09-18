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
import org.pcsoft.framework.simplay.engine.model.TextStyle
import org.pcsoft.framework.simplay.engine.model.charCount

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

    /** Matches the `${anchorId}` token `BookPartBuilder` embeds at the very start of a page's block 0. */
    private val ANCHOR_PREFIX = Regex("^\\$\\{[^}]*}")

    /**
     * Splits the block at [index] of [blocks] into two, at the anchor-excluding character [charOffset].
     *
     * An anchor token at the very start of [blocks]\[0\] is kept on the first half, so block `0` never
     * loses its `TextAnchor`.
     *
     * @param blocks the block list of one page to split in
     * @param index the block to split
     * @param charOffset the character offset the split falls at, counted like `CaretModel.position`
     * (a `TextAnchor` counts as zero characters); `0` and the block's own length are valid and yield an
     * empty first or second half
     * @param style the style both halves are built with
     * @return the block list with the split applied, or `null` when [index] is out of range
     */
    fun splitTextBlock(blocks: List<TextBlock>, index: Int, charOffset: Int, style: TextStyle): List<TextBlock>? {
        if (index !in blocks.indices) return null

        val raw = blocks[index].toString()
        val anchorPrefix = ANCHOR_PREFIX.find(raw)?.value.orEmpty()
        val text = raw.removePrefix(anchorPrefix)
        val offset = charOffset.coerceIn(0, text.length)

        val result = blocks.toMutableList()
        result[index] = TextBlock.of(anchorPrefix + text.substring(0, offset), style)
        result.add(index + 1, TextBlock.of(text.substring(offset), style))
        return result
    }

    /**
     * Removes the block at [index] of [blocks], keeping at least one block and never the one at
     * position `0`, which carries the page's `TextAnchor`.
     *
     * @param blocks the block list of one page to remove from
     * @param index the block to remove
     * @return the block list without that block, or `null` when [index] is `0`, out of range, or the
     * only block left
     */
    fun removeTextBlock(blocks: List<TextBlock>, index: Int): List<TextBlock>? {
        if (index !in blocks.indices || index == 0 || blocks.size <= 1) return null
        val result = blocks.toMutableList()
        result.removeAt(index)
        return result
    }

    /**
     * Moves the block at [index] of [blocks] one position towards the start or the end of the page.
     *
     * The block at position `0`, which carries the page's `TextAnchor`, may never move and nothing may
     * move into position `0` in its place.
     *
     * @param blocks the block list of one page to move in
     * @param index the block to move
     * @param up `true` to move it towards the start, `false` towards the end
     * @return the block list with the move applied, or `null` when the move is not possible
     */
    fun moveTextBlock(blocks: List<TextBlock>, index: Int, up: Boolean): List<TextBlock>? {
        if (index !in blocks.indices || index == 0) return null
        val otherIndex = if (up) index - 1 else index + 1
        if (otherIndex !in blocks.indices || otherIndex == 0) return null

        val result = blocks.toMutableList()
        val moved = result.removeAt(index)
        result.add(otherIndex, moved)
        return result
    }

    /**
     * Applies a structural block operation to the page of [document] whose id is [anchorId], as one
     * transaction.
     *
     * [document] MUST be read fresh right before this call, never from a reference cached earlier (a
     * key press, say) - an AI rewrite (IP-18) may have replaced it in between.
     *
     * @param document the document to read the page from, read at call time
     * @param anchorId the anchor id of the page to operate on
     * @param op the block operation to apply to that page's block list; `null` means the operation was
     * rejected (a boundary was hit) and the whole transaction is abandoned
     * @return the document with the page replaced, or `null` when the page does not exist or [op]
     * rejected the operation
     */
    fun applyParagraphOperation(
        document: Document,
        anchorId: String,
        op: (List<TextBlock>) -> List<TextBlock>?,
    ): Document? {
        val pageIndex = document.pages.indexOfFirst { it.id == anchorId }
        if (pageIndex < 0) return null

        val page = document.pages[pageIndex]
        val newBlocks = op(page.blocks) ?: return null

        val newPages = document.pages.toMutableList().apply { this[pageIndex] = page.withBlocks(newBlocks) }
        return document.copy(pages = newPages)
    }

    /**
     * Merges the block at [index] of [blocks] with a neighbour, for the context menu's explicit
     * "merge" commands - a keyboard `Backspace`/`Delete` across a block boundary already merges blocks
     * through `PaperSheetView` itself and never calls this.
     *
     * The merged text is rebuilt through [TextBlock.of] from the concatenation of both blocks'
     * [TextBlock.toString], so an anchor token at the very start of [blocks]\[0\] survives whichever
     * side of the merge it sits on.
     *
     * @param blocks the block list of one page to merge in
     * @param index the block the merge was requested from
     * @param withPrevious `true` to merge with the block before it, `false` for the one after it
     * @return the block list with the merge applied, or `null` when [index] has no such neighbour
     */
    fun mergeTextBlock(blocks: List<TextBlock>, index: Int, withPrevious: Boolean): List<TextBlock>? {
        if (index !in blocks.indices) return null
        val otherIndex = if (withPrevious) index - 1 else index + 1
        if (otherIndex !in blocks.indices) return null

        val firstIndex = minOf(index, otherIndex)
        val secondIndex = maxOf(index, otherIndex)
        val merged = TextBlock.of(blocks[firstIndex].toString() + blocks[secondIndex].toString(), blocks[firstIndex].style)

        val result = blocks.toMutableList()
        result[firstIndex] = merged
        result.removeAt(secondIndex)
        return result
    }

    /** Whether [page] still starts with its `${id}`-anchor token, checked after a native structural edit. */
    fun pageKeepsAnchor(page: Page): Boolean =
        page.blocks.isNotEmpty() && ANCHOR_PREFIX.containsMatchIn(page.blocks.first().toString())

    /**
     * The document-wide, zero-based block ordinal `CaretModel.moveIntoBlock` expects for the block at
     * [pageLocalIndex] of the page whose id is [anchorId] - `CaretModel` addresses blocks across the
     * whole document, not per page.
     *
     * @param document the document to resolve the ordinal against, read at call time
     * @param anchorId the anchor id of the target page
     * @param pageLocalIndex the block's index within that page's own block list
     * @return the document-wide block ordinal, or `null` when [anchorId] does not resolve to a page
     */
    fun documentBlockIndex(document: Document, anchorId: String, pageLocalIndex: Int): Int? {
        var count = 0
        for (page in document.pages) {
            if (page.id == anchorId) return count + pageLocalIndex
            count += page.blocks.size
        }
        return null
    }

    /**
     * The character offset of [block] within [document], counted the same way `CaretModel.position`
     * counts (a `TextAnchor` is zero characters), relative to the start of [block] itself rather than
     * the whole document.
     *
     * @param document the document [block] belongs to
     * @param block the block to resolve [position] against, matched by reference
     * @param position the document-wide linear caret position
     * @return the character offset within [block], clamped to zero
     */
    fun blockLocalCharOffset(document: Document, block: TextBlock, position: Int): Int {
        var counted = 0
        for (page in document.pages) {
            for (candidate in page.blocks) {
                if (candidate === block) return (position - counted).coerceAtLeast(0)
                counted += candidate.charCount()
            }
        }
        return 0
    }

    /**
     * The targets of every block of [page], in block order - the public entry point [targetsOf] uses
     * internally, exposed for a caller that must rebuild a single page's targets after a native
     * structural edit (a `Backspace`/`Delete` merge) instead of the whole document.
     */
    fun targetsOfPage(page: Page, design: Design, meta: Meta): List<PartTarget> = targetsOf(page, design, meta)

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

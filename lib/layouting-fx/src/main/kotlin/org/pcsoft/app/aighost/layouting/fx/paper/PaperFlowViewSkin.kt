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

package org.pcsoft.app.aighost.layouting.fx.paper

import javafx.animation.PauseTransition
import javafx.geometry.Insets
import javafx.scene.control.ContextMenu
import javafx.scene.control.MenuItem
import javafx.scene.control.ScrollPane
import javafx.scene.control.Skin
import javafx.scene.control.SkinBase
import javafx.scene.control.TextArea
import javafx.scene.input.Clipboard
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyEvent
import javafx.scene.layout.Pane
import javafx.scene.layout.Region
import javafx.scene.layout.StackPane
import javafx.scene.layout.VBox
import javafx.scene.shape.Line
import javafx.scene.text.Font
import javafx.scene.text.Text
import javafx.util.Duration
import org.pcsoft.app.aighost.layouting.DocumentLayout
import org.pcsoft.app.aighost.layouting.Page
import org.pcsoft.app.aighost.layouting.PageGeometry

/**
 * The [Skin] of [PaperFlowView].
 *
 * The skin owns one [TextArea] per block of the [DocumentLayout] currently shown, stacked inside a
 * [ScrollPane]. Where a page break falls between two blocks, a real gap is left between their text
 * controls, the same [PAGE_GAP] used by [PaperPageViewSkin]. Where a break falls inside a block, a
 * dashed overlay carrying the page number is drawn across the control instead, positioned by the
 * fraction of the block's text that came before the break - [TextArea] keeps its own text layout
 * private, so the mark cannot be placed at the exact pixel of the broken line without reaching into
 * toolkit internals, which this codebase never does.
 *
 * No global registration is made anywhere in this class: every listener here observes either a
 * property of [PaperFlowView] itself, a node this skin owns, or one text control this skin owns -
 * nothing needs the on-screen guard of `showingBinding()`.
 *
 * **Threading:** like every JavaFX skin, every member of this class must be used on the JavaFX
 * application thread only.
 *
 * @constructor Builds the skin for [control]; called by [PaperFlowView.createDefaultSkin] only.
 */
class PaperFlowViewSkin(control: PaperFlowView) : SkinBase<PaperFlowView>(control) {

    private companion object {
        /** Empty space left between two blocks whose page break is a real gap between sheets. */
        const val PAGE_GAP: Double = 24.0

        /** Empty space left between two blocks that share the same sheet. */
        const val BLOCK_SPACING: Double = 4.0

        /** How long the mark and column width recomputation waits for further resizes before it runs. */
        const val RELAYOUT_DEBOUNCE_MS: Double = 100.0
    }

    /** One page break falling inside a block, at the character offset the break starts at. */
    private data class PageBreakMark(val charOffset: Int, val pageNumber: Int?)

    /** One block reconstructed from the laid out lines that belong to it. */
    private data class BlockSegment(
        val blockIndex: Int,
        val text: String,
        val breaks: List<PageBreakMark>,
        val gapBefore: Boolean,
        /** Character offset the block's first wrapped line ends at, exclusive. */
        val firstLineEnd: Int,
        /** Character offset the block's last wrapped line starts at. */
        val lastLineStart: Int
    )

    /** The nodes making up one block: its text control and the overlay its break marks are drawn on. */
    private class BlockPane(val blockIndex: Int) {
        val textArea = TextArea()
        val overlay = Pane()
        val stack = StackPane(textArea, overlay)
        var breaks: List<PageBreakMark> = emptyList()

        // Real wrapped-line boundaries from the layout engine, the same source of truth the break
        // marks use - not the control's own text layout, which this codebase never reaches into. Up
        // and Down only cross into the neighbouring block once the caret sits on the outermost of
        // these wrapped lines, the same way a native multi-line field only exits at its own edge.
        var firstLineEnd: Int = 0
        var lastLineStart: Int = 0
    }

    private val scrollPane = ScrollPane()
    private val blockContainer = VBox()
    private val blockPanes: MutableMap<Int, BlockPane> = LinkedHashMap()
    private val relayoutDebounce = PauseTransition(Duration.millis(RELAYOUT_DEBOUNCE_MS))

    // The text every block carried right after the previous rebuild, keyed by block index. A text
    // control's own textProperty already reports the new value by the time its invalidation listener
    // runs - only its caretPosition still lags behind, updated by the control's default behaviour only
    // once that notification returns - so the *old* text a rebuild needs to detect an edit against can
    // never be read off the control being rebuilt itself; it has to be remembered from the rebuild
    // before.
    private var lastRenderedText: Map<Int, String> = emptyMap()

    init {
        blockContainer.styleClass.add("paper-flow-view-container")
        blockContainer.spacing = 0.0
        blockContainer.isFillWidth = true
        scrollPane.styleClass.add("paper-flow-view-scroll-pane")
        scrollPane.content = blockContainer
        scrollPane.isFitToWidth = true
        children.add(scrollPane)

        relayoutDebounce.setOnFinished { relayout() }

        control.documentLayoutProperty().addListener { _, _, _ -> rebuildStructure() }
        control.pageGeometryProperty().addListener { _, _, _ -> rebuildStructure() }
        scrollPane.widthProperty().addListener { _, _, _ -> relayoutDebounce.playFromStart() }

        rebuildStructure()
    }

    /** Content width the layout engine measured against: page width minus inner and outer margin. */
    private fun contentWidth(geometry: PageGeometry): Double =
        (geometry.width - geometry.innerMargin - geometry.outerMargin).coerceAtLeast(0.0)

    /** The focused block's identity right before a rebuild: its index and its (still stale) caret. */
    private data class FocusMemo(val blockIndex: Int, val caretPosition: Int)

    /**
     * Rebuilds every block's text control from scratch, keeping the caret and the focus of the block
     * that carried it before the rebuild - a fresh [DocumentLayout] must not interrupt typing.
     *
     * A rebuild reacting to the very keystroke that changed the focused block's own text runs from
     * inside that block's own `textProperty` invalidation: the property already reports the new text
     * at that point, but [TextArea.caretPositionProperty] is only advanced by the control's default
     * behaviour once that notification returns - so the caret memorised here is the *pre-edit* offset.
     * Comparing [lastRenderedText] - the text the block carried after the previous rebuild - against
     * the new text handed in this time and shifting the memorised caret by the length difference
     * corrects for that; a plain restyling, where the text at that block index never changes, leaves
     * the caret exactly where it was.
     */
    private fun rebuildStructure() {
        val focused = blockPanes.values.firstOrNull { it.textArea.isFocused }
        val focusMemo = focused?.let { FocusMemo(it.blockIndex, it.textArea.caretPosition) }
        val previousText = lastRenderedText

        blockPanes.clear()
        blockContainer.children.clear()

        val geometry = skinnable.pageGeometry
        val layout = skinnable.documentLayout
        if (geometry == null || layout == null || layout.pages.isEmpty()) {
            skinnable.setColumnWidth(0.0)
            lastRenderedText = emptyMap()
            return
        }

        val segments = segmentsOf(layout)
        for (segment in segments) {
            if (segment.gapBefore) {
                blockContainer.children.add(gapRegion(PAGE_GAP))
            } else if (blockContainer.children.isNotEmpty()) {
                blockContainer.children.add(gapRegion(BLOCK_SPACING))
            }
            blockContainer.children.add(buildBlockPane(segment).stack)
        }
        lastRenderedText = segments.associate { it.blockIndex to it.text }

        relayout()

        val caretTarget = skinnable.consumePendingCaretRequest() ?: focusMemo?.let { memo ->
            val oldText = previousText[memo.blockIndex]
            val newText = lastRenderedText[memo.blockIndex]
            val caretPosition = if (oldText != null && newText != null && newText != oldText) {
                memo.caretPosition + (newText.length - oldText.length)
            } else {
                memo.caretPosition
            }
            memo.blockIndex to caretPosition
        }
        caretTarget?.let { (blockIndex, caretPosition) ->
            blockPanes[blockIndex]?.let { pane ->
                pane.textArea.requestFocus()
                pane.textArea.positionCaret(caretPosition.coerceIn(0, pane.textArea.text.length))
            }
        }
    }

    private fun gapRegion(height: Double): Region {
        val region = Region()
        region.minHeight = height
        region.prefHeight = height
        region.maxHeight = height
        region.styleClass.add("paper-flow-view-gap")
        return region
    }

    /**
     * Groups the lines of [layout] by the block they were set from, in reading order, reconstructing
     * each block's text and the offsets any page break inside it falls at.
     */
    private fun segmentsOf(layout: DocumentLayout): List<BlockSegment> {
        data class PlacedLine(val page: Page, val text: String, val blockIndex: Int)

        val placedLines = layout.pages.flatMap { page -> page.lines.map { PlacedLine(page, it.text, it.blockIndex) } }
        val segments = mutableListOf<BlockSegment>()

        var index = 0
        var previousGroupLastPage: Page? = null
        while (index < placedLines.size) {
            val blockIndex = placedLines[index].blockIndex
            val builder = StringBuilder()
            val breaks = mutableListOf<PageBreakMark>()
            var previousPage: Page? = null
            var firstPage: Page? = null
            var firstLineEnd = 0
            var lastLineStart = 0

            while (index < placedLines.size && placedLines[index].blockIndex == blockIndex) {
                val line = placedLines[index]
                if (firstPage == null) firstPage = line.page
                if (previousPage != null && previousPage.position != line.page.position) {
                    breaks.add(PageBreakMark(builder.length, line.page.pageNumber))
                }
                if (builder.isNotEmpty()) builder.append(' ')
                builder.append(line.text)
                lastLineStart = builder.length - line.text.length
                if (previousPage == null) firstLineEnd = builder.length
                previousPage = line.page
                index++
            }

            val gapBefore = previousGroupLastPage != null && firstPage != null &&
                previousGroupLastPage.position != firstPage.position
            segments.add(BlockSegment(blockIndex, builder.toString(), breaks, gapBefore, firstLineEnd, lastLineStart))
            previousGroupLastPage = previousPage
        }

        return segments
    }

    private fun buildBlockPane(segment: BlockSegment): BlockPane {
        val pane = BlockPane(segment.blockIndex)
        pane.breaks = segment.breaks
        pane.firstLineEnd = segment.firstLineEnd
        pane.lastLineStart = segment.lastLineStart
        pane.overlay.isMouseTransparent = true
        pane.overlay.styleClass.add("paper-flow-view-break-overlay")

        val textArea = pane.textArea
        textArea.styleClass.add("paper-flow-view-block")
        textArea.isWrapText = true
        textArea.text = segment.text

        wireEvents(pane)
        blockPanes[segment.blockIndex] = pane
        return pane
    }

    /** Wires one block's text control to report every change back through [PaperFlowListener]. */
    private fun wireEvents(pane: BlockPane) {
        val textArea = pane.textArea
        val blockIndex = pane.blockIndex

        textArea.textProperty().addListener { _, _, newValue ->
            skinnable.paperFlowListeners.forEach { it.onTextChanged(blockIndex, newValue) }
        }
        textArea.caretPositionProperty().addListener { _, _, newValue ->
            skinnable.paperFlowListeners.forEach { it.onCaretMoved(blockIndex, newValue.toInt()) }
        }
        textArea.focusedProperty().addListener { _, _, focused ->
            skinnable.paperFlowListeners.forEach { it.onFocusChanged(blockIndex, focused) }
        }

        // Break marks are positioned by the control's real pixel size, only known once it was laid
        // out at least once - redrawn whenever that size settles, not only from the shared debounce.
        textArea.heightProperty().addListener { _, _, _ -> drawBreakMarks(pane) }
        textArea.widthProperty().addListener { _, _, _ -> drawBreakMarks(pane) }

        // No character formatting is ever offered: pasted content is reduced to plain text.
        textArea.addEventHandler(KeyEvent.KEY_PRESSED) { event -> handleKeyPressed(pane, event) }

        val pasteMenuItem = MenuItem("Paste as plain text")
        pasteMenuItem.setOnAction { pastePlainText(textArea) }
        val moveUpMenuItem = MenuItem("Move paragraph up").apply {
            setOnAction {
                skinnable.paperFlowListeners.forEach { it.onMoveRequested(blockIndex, up = true) }
            }
        }
        val moveDownMenuItem = MenuItem("Move paragraph down").apply {
            setOnAction {
                skinnable.paperFlowListeners.forEach { it.onMoveRequested(blockIndex, up = false) }
            }
        }
        textArea.contextMenu = ContextMenu(pasteMenuItem, moveUpMenuItem, moveDownMenuItem, MenuItem("Remove block").apply {
            setOnAction {
                skinnable.paperFlowListeners.forEach { it.onRemoveRequested(blockIndex) }
            }
        })
    }

    /**
     * A block carries no rich text: whatever sits on the clipboard is inserted as plain text only,
     * any formatting the source application attached to it is dropped.
     */
    private fun pastePlainText(textArea: TextArea) {
        val plain = Clipboard.getSystemClipboard().string ?: return
        textArea.insertText(textArea.caretPosition, plain)
    }

    /**
     * Turns Enter, Backspace-at-start and Delete-at-end into a split or merge intent instead of
     * letting the text control apply them as an ordinary edit - a block is a paragraph, not a
     * multi-line field.
     */
    private fun handleKeyPressed(pane: BlockPane, event: KeyEvent) {
        val textArea = pane.textArea
        when (event.code) {
            KeyCode.ENTER -> {
                skinnable.paperFlowListeners.forEach {
                    it.onSplitRequested(pane.blockIndex, textArea.caretPosition)
                }
                event.consume()
            }

            KeyCode.BACK_SPACE -> if (textArea.caretPosition == 0 && textArea.selectedText.isEmpty()) {
                skinnable.paperFlowListeners.forEach { it.onMergeRequested(pane.blockIndex, withPrevious = true) }
                event.consume()
            }

            KeyCode.DELETE -> if (textArea.caretPosition == textArea.text.length && textArea.selectedText.isEmpty()) {
                skinnable.paperFlowListeners.forEach { it.onMergeRequested(pane.blockIndex, withPrevious = false) }
                event.consume()
            }

            KeyCode.UP -> when {
                event.isControlDown && event.isShiftDown -> {
                    skinnable.paperFlowListeners.forEach { it.onMoveRequested(pane.blockIndex, up = true) }
                    event.consume()
                }

                !event.isControlDown && !event.isShiftDown && !event.isAltDown &&
                    textArea.caretPosition <= pane.firstLineEnd && textArea.selectedText.isEmpty() -> {
                    if (focusAdjacentBlock(pane.blockIndex, delta = -1, caretAtEnd = true)) event.consume()
                }
            }

            KeyCode.DOWN -> when {
                event.isControlDown && event.isShiftDown -> {
                    skinnable.paperFlowListeners.forEach { it.onMoveRequested(pane.blockIndex, up = false) }
                    event.consume()
                }

                !event.isControlDown && !event.isShiftDown && !event.isAltDown &&
                    textArea.caretPosition >= pane.lastLineStart && textArea.selectedText.isEmpty() -> {
                    if (focusAdjacentBlock(pane.blockIndex, delta = 1, caretAtEnd = false)) event.consume()
                }
            }

            else -> {}
        }
    }

    /**
     * Moves the focus from [blockIndex] to the block right before or after it, placing the caret at
     * the end or the start of its text - the plain Up/Down counterpart to Ctrl+Shift+Up/Down
     * ([PaperFlowListener.onMoveRequested]), staying entirely inside this skin since no block, no text
     * and no order changes, only where the caret sits.
     *
     * @param blockIndex the block the arrow key was pressed in
     * @param delta `-1` for the block before it, `1` for the block after it
     * @param caretAtEnd `true` to place the caret at the end of the target block's text, `false` for
     * its start
     * @return `true` if such a neighbouring block exists and took the focus, `false` at the first or
     * the last block, where the key press is left for the control's own default handling
     */
    private fun focusAdjacentBlock(blockIndex: Int, delta: Int, caretAtEnd: Boolean): Boolean {
        val pane = blockPanes[blockIndex + delta] ?: return false
        pane.textArea.requestFocus()
        pane.textArea.positionCaret(if (caretAtEnd) pane.textArea.text.length else 0)
        return true
    }

    /**
     * Recomputes the reported column width and repositions every break mark; debounced behind
     * [relayoutDebounce] when triggered by a resize, called directly after a structural rebuild.
     */
    private fun relayout() {
        val geometry = skinnable.pageGeometry ?: return
        val first = blockPanes.values.firstOrNull()
        val insets: Insets = first?.textArea?.insets ?: Insets.EMPTY
        skinnable.setColumnWidth((contentWidth(geometry) - insets.left - insets.right).coerceAtLeast(0.0))

        for (pane in blockPanes.values) {
            drawBreakMarks(pane)
        }
    }

    /** Draws a dashed line with the target page number for every break inside [pane]'s block. */
    private fun drawBreakMarks(pane: BlockPane) {
        pane.overlay.children.clear()
        if (pane.breaks.isEmpty()) return

        val textLength = pane.textArea.text.length.coerceAtLeast(1)
        val height = pane.textArea.height.takeIf { it > 0.0 } ?: pane.textArea.prefHeight(-1.0)
        val width = pane.textArea.width.takeIf { it > 0.0 } ?: pane.textArea.prefWidth(-1.0)
        if (height <= 0.0 || width <= 0.0) return

        for (mark in pane.breaks) {
            val fraction = mark.charOffset.toDouble() / textLength.toDouble()
            val y = (height * fraction).coerceIn(0.0, height)

            val line = Line(0.0, y, width, y)
            line.styleClass.add("paper-flow-view-break-mark")
            pane.overlay.children.add(line)

            mark.pageNumber?.let { number ->
                val label = Text(number.toString())
                label.styleClass.add("paper-flow-view-break-mark-label")
                label.font = Font.font(9.0)
                label.x = width - 20.0
                label.y = y - 2.0
                pane.overlay.children.add(label)
            }
        }
    }

    /** @see PaperFlowView.scrollToBlock */
    fun scrollToBlock(blockIndex: Int) {
        val pane = blockPanes[blockIndex] ?: return
        val totalHeight = blockContainer.height.takeIf { it > 0.0 } ?: return
        val viewportHeight = scrollPane.viewportBounds.height
        val offsetY = pane.stack.boundsInParent.minY
        val maxScroll = (totalHeight - viewportHeight).coerceAtLeast(0.0)
        scrollPane.vvalue = if (maxScroll <= 0.0) 0.0 else (offsetY / maxScroll).coerceIn(0.0, 1.0)
    }

    /** Number of block text controls currently built, exposed for tests. */
    internal val blockCount: Int
        get() = blockPanes.size
}

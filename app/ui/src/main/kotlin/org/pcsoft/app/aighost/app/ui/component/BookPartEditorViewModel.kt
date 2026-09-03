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

package org.pcsoft.app.aighost.app.ui.component

import de.saxsys.mvvmfx.ViewModel
import javafx.beans.InvalidationListener
import javafx.beans.Observable
import javafx.beans.binding.Bindings
import javafx.beans.binding.BooleanBinding
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.beans.property.StringProperty
import javafx.beans.value.ChangeListener
import javafx.beans.value.ObservableValue
import org.pcsoft.app.aighost.app.Messages
import org.pcsoft.app.aighost.app.controller.IoController
import org.pcsoft.app.aighost.app.undo.UndoStack
import org.pcsoft.app.aighost.fx.model.project.ProjectProperty
import org.pcsoft.app.aighost.fx.model.project.book.BookPartProperty
import org.pcsoft.app.aighost.fx.model.project.book.ChapterProperty
import org.pcsoft.app.aighost.layouting.GreedyLineBreaker
import org.pcsoft.app.aighost.layouting.LayoutEngine
import org.pcsoft.app.aighost.layouting.NonePageBreakPolicy
import org.pcsoft.app.aighost.layouting.TextBlock
import org.pcsoft.app.aighost.layouting.fx.font.JavaFxTextMetrics
import org.pcsoft.app.aighost.layouting.fx.paper.PaperFlowListener
import org.pcsoft.app.aighost.layouting.fx.paper.PaperFlowView
import org.pcsoft.app.aighost.layouting.model.common.PageGeometryTranslation
import org.pcsoft.app.aighost.layouting.model.project.book.BlurbBuilder
import org.pcsoft.app.aighost.layouting.model.project.book.BookPartBuilder
import org.pcsoft.app.aighost.layouting.model.project.book.TitlePageBuilder
import org.pcsoft.app.aighost.layouting.model.project.meta.CopyrightPageBuilder

/**
 * View model of [BookPartEditor].
 *
 * The project is handed over through [bindProject], the picked tree node through [bindSelection] and
 * the undo history of the open project through [bindUndoStack]; all three are taken as the model
 * itself and never wrapped in a property of their own. The [PaperFlowView] the view holds is handed
 * in once through [attach].
 *
 * Prolog, chapter and epilog are edited through one flow over one [BookPartProperty]; the title page
 * and the copyright page are shown read only, the blurb has no heading. Every keystroke reported by
 * [PaperFlowView] is written into the property model of the part and folded into a single undo entry
 * per block for the length of a typing pause; the layout on the sheet is recomputed from the model
 * after every change and after every design change, so the caret survives a restyling.
 *
 * The component follows only models handed to it and registers nothing in a global registry, so the
 * `showingBinding` pattern of `fx-component-lifecycle` does not apply here, the same as for
 * [PaperFlowView] itself.
 */
class BookPartEditorViewModel : ViewModel {

    /** Which kind of part the sheet currently shows, driving the empty state and the read-only flag. */
    val mode: SimpleObjectProperty<PartMode> = SimpleObjectProperty(this, "mode", PartMode.NONE)

    /** Whether a part is shown at all, so the sheet is visible instead of the empty state. */
    val contentAvailable: BooleanBinding =
        Bindings.createBooleanBinding({ mode.value != PartMode.NONE }, mode)

    /** Whether the shown part may be written, `false` for the title page and the copyright page. */
    val editable: BooleanBinding = Bindings.createBooleanBinding(
        { mode.value == PartMode.BOOK_PART || mode.value == PartMode.BLURB },
        mode
    )

    /** Paragraph the caret last sat in, or `null` while it sits in a heading or nowhere. */
    internal val caretParagraphIndex: Int?
        get() = (caretTarget as? PartTarget.Paragraph)?.index

    /** Character offset of the caret inside the block it last sat in. */
    internal val caretCharOffset: Int
        get() = caretOffset

    // The flow view the view holds, handed in once after the FXML is loaded.
    private lateinit var paperFlowView: PaperFlowView

    private var project: ProjectProperty? = null
    private var undoStack: UndoStack? = null
    private var lastSelection: ProjectListItem? = null

    // The part currently edited, built through the book's own property or through ChapterProperty.of.
    private var boundPart: BookPartProperty<*>? = null
    private var partId: String = ""

    // What each block of the current layout writes back to, in block order.
    private var targets: List<PartTarget> = emptyList()

    // One string property per block target, so a text change can be recorded as an undo step and an
    // undo can play it back through the same path a keystroke takes.
    private val targetProperties: MutableMap<PartTarget, StringProperty> = HashMap()

    // True while a freshly computed layout is handed to the flow view, so the text-change events the
    // rebuild fires for every block are not mistaken for edits.
    private var applyingLayout = false

    // True while a target property is written from a reported edit, so its own listener does not
    // write the model twice.
    private var writingTarget = false

    private var focusedBlock: Int? = null

    // The caret is kept as a block target plus a character offset, never as a coordinate, so it
    // survives a restyling that moves every line.
    private var caretTarget: PartTarget? = null
    private var caretOffset: Int = 0

    private val designListener = InvalidationListener { recompute() }
    private var boundDesign: Observable? = null

    private val columnWidthListener = ChangeListener<Number> { _, _, width ->
        if (width.toDouble() > 0.0) recompute()
    }

    private val selectionListener =
        ChangeListener<ProjectListItem?> { _, _, newValue -> onSelectionChanged(newValue) }
    private var boundSelection: ObservableValue<ProjectListItem?>? = null

    private val flowListener = object : PaperFlowListener {
        override fun onTextChanged(blockIndex: Int, text: String) = handleTextChanged(blockIndex, text)

        override fun onCaretMoved(blockIndex: Int, caretPosition: Int) {
            caretTarget = targets.getOrNull(blockIndex)
            caretOffset = caretPosition
        }

        override fun onFocusChanged(blockIndex: Int, focused: Boolean) {
            if (focused) {
                val previous = focusedBlock
                if (previous != null && previous != blockIndex) {
                    undoStack?.endMerging()
                }
                focusedBlock = blockIndex
            } else if (focusedBlock == blockIndex) {
                // The whole sheet lost focus - a merge in progress ends here as well.
                undoStack?.endMerging()
                focusedBlock = null
            }
        }
    }

    /**
     * Hands the flow view of the component over, once, and starts listening to it.
     *
     * @param paperFlowView the flow view held by [BookPartEditorView]
     */
    internal fun attach(paperFlowView: PaperFlowView) {
        this.paperFlowView = paperFlowView
        paperFlowView.addPaperFlowListener(flowListener)
        paperFlowView.columnWidthProperty().addListener(columnWidthListener)
    }

    /**
     * Binds the sheet to the given project and releases the one bound before.
     *
     * @param project the open project, `null` to follow none
     */
    internal fun bindProject(project: ProjectProperty?) {
        boundDesign?.removeListener(designListener)
        this.project = project
        boundDesign = project?.designProperty?.also { it.addListener(designListener) }

        onSelectionChanged(lastSelection)
    }

    /**
     * Lets the sheet follow the picked node of the project tree.
     *
     * @param selection the selection reported by [ProjectList], read once and followed afterwards
     */
    internal fun bindSelection(selection: ObservableValue<ProjectListItem?>) {
        boundSelection?.removeListener(selectionListener)
        boundSelection = selection
        selection.addListener(selectionListener)

        onSelectionChanged(selection.value)
    }

    /**
     * Takes the undo history of the open project over and configures the typing pause from the
     * preferences of the user.
     *
     * @param undoStack the one undo history of the surrounding window
     */
    internal fun bindUndoStack(undoStack: UndoStack) {
        this.undoStack = undoStack
        undoStack.mergeTimeoutMillis =
            IoController.preferences.editorProperty.paragraphMergePauseMillis
    }

    /** Releases every binding of the component, used while it leaves the screen for good. */
    internal fun release() {
        boundDesign?.removeListener(designListener)
        boundDesign = null
        boundSelection?.removeListener(selectionListener)
        boundSelection = null
        if (::paperFlowView.isInitialized) {
            paperFlowView.removePaperFlowListener(flowListener)
            paperFlowView.columnWidthProperty().removeListener(columnWidthListener)
        }
        boundPart = null
        targetProperties.clear()
        targets = emptyList()
    }

    private fun onSelectionChanged(item: ProjectListItem?) {
        lastSelection = item
        undoStack?.endMerging()
        focusedBlock = null
        targetProperties.clear()
        boundPart = null
        partId = ""

        val p = project
        val book = p?.bookProperty?.value
        mode.value = when {
            p == null || book == null -> PartMode.NONE
            item is ProjectListItem.TitlePageItem -> PartMode.TITLE_PAGE
            item is ProjectListItem.CopyrightPageItem -> PartMode.COPYRIGHT_PAGE
            item is ProjectListItem.PrologItem -> {
                boundPart = p.bookProperty.prologProperty
                partId = "prolog"
                PartMode.BOOK_PART
            }

            item is ProjectListItem.EpilogItem -> {
                boundPart = p.bookProperty.epilogProperty
                partId = "epilog"
                PartMode.BOOK_PART
            }

            item is ProjectListItem.ChapterItem -> {
                boundPart = ChapterProperty.of(item.chapter)
                partId = "chapter:" + item.chapter.name
                PartMode.BOOK_PART
            }

            item is ProjectListItem.BlurbItem -> PartMode.BLURB
            else -> PartMode.NONE
        }

        recompute()
    }

    private fun handleTextChanged(blockIndex: Int, text: String) {
        if (applyingLayout) return

        if (mode.value != PartMode.BOOK_PART && mode.value != PartMode.BLURB) {
            // A read-only part must not change; rebuild it from the untouched model.
            recompute()
            return
        }

        val target = targets.getOrNull(blockIndex) ?: return
        val property = propertyFor(target)
        val old = property.value ?: ""
        if (text == old) return

        writingTarget = true
        try {
            property.value = text
        } finally {
            writingTarget = false
        }
        writeModel(target, text)
        undoStack?.record(Messages["component.bookPartEditor.undo.edit"], property, old, text, mergeKey = partId to target)
        recompute()
    }

    private fun propertyFor(target: PartTarget): StringProperty =
        targetProperties.getOrPut(target) {
            SimpleStringProperty(readModel(target)).apply {
                addListener { _, _, newValue ->
                    if (writingTarget) return@addListener
                    // Reached only through an undo or redo, which plays the value back the same way.
                    writeModel(target, newValue ?: "")
                    recompute()
                }
            }
        }

    private fun readModel(target: PartTarget): String = when (target) {
        is PartTarget.Title -> boundPart?.titleProperty?.get().orEmpty()
        is PartTarget.AppendixLine -> boundPart?.titleAppendixProperty?.getOrNull(target.modelIndex).orEmpty()
        is PartTarget.Paragraph -> when (mode.value) {
            PartMode.BLURB -> blurbParagraphs()?.getOrNull(target.index).orEmpty()
            else -> boundPart?.paragraphProperty?.getOrNull(target.index).orEmpty()
        }
    }

    private fun writeModel(target: PartTarget, value: String) {
        when (target) {
            is PartTarget.Title -> boundPart?.titleProperty?.set(value)

            is PartTarget.AppendixLine -> {
                val list = boundPart?.titleAppendixProperty ?: return
                if (target.modelIndex in list.indices) {
                    list[target.modelIndex] = value
                }
            }

            is PartTarget.Paragraph -> {
                val list = if (mode.value == PartMode.BLURB) blurbParagraphs() else boundPart?.paragraphProperty
                list ?: return
                if (target.index in list.indices) {
                    list[target.index] = value
                } else if (target.index == list.size) {
                    list.add(value)
                }
            }
        }
    }

    private fun blurbParagraphs() = project?.bookProperty?.blurbProperty?.paragraphProperty

    private fun recompute() {
        if (!::paperFlowView.isInitialized) return

        val p = project?.value
        val design = p?.design
        if (p == null || design == null || mode.value == PartMode.NONE) {
            targets = emptyList()
            applyingLayout = true
            try {
                paperFlowView.documentLayout = null
            } finally {
                applyingLayout = false
            }
            return
        }

        val geometry = PageGeometryTranslation.toPageGeometry(design.pageFormat)
        paperFlowView.pageGeometry = geometry

        val (blocks, blockTargets) = buildBlocks(p, design)
        targets = blockTargets

        if (blocks.isEmpty()) {
            applyingLayout = true
            try {
                paperFlowView.documentLayout = null
            } finally {
                applyingLayout = false
            }
            return
        }

        // The flow view derives the exact column width from the insets of its own text controls,
        // which only exist once a layout was handed in. The first layout therefore uses the plain
        // content width of the page; the column-width listener recomputes with the exact value as
        // soon as the controls report it.
        val reported = paperFlowView.columnWidth
        val columnWidth = if (reported > 0.0) {
            reported
        } else {
            (design.pageFormat.width - design.pageFormat.innerMargin - design.pageFormat.outerMargin)
                .coerceAtLeast(1.0)
        }

        val text = GreedyLineBreaker(JavaFxTextMetrics).breakText(blocks, columnWidth)
        val layout = LayoutEngine.layout(
            text = text,
            geometry = geometry,
            startPageNumber = null,
            policy = NonePageBreakPolicy
        )

        applyingLayout = true
        try {
            paperFlowView.documentLayout = layout
        } finally {
            applyingLayout = false
        }
    }

    private fun buildBlocks(
        project: org.pcsoft.app.aighost.model.project.Project,
        design: org.pcsoft.app.aighost.model.project.design.Design
    ): Pair<List<TextBlock>, List<PartTarget>> {
        val book = project.book
        val meta = project.meta

        return when (mode.value) {
            PartMode.TITLE_PAGE ->
                ensureWritableBlock(TitlePageBuilder.build(book, meta, design), design, emptyList())

            PartMode.COPYRIGHT_PAGE ->
                ensureWritableBlock(
                    CopyrightPageBuilder.build(book.copyright, meta, design),
                    design,
                    emptyList()
                )

            PartMode.BLURB -> {
                val blocks = BlurbBuilder.build(book.blurb, design)
                val targets = book.blurb.paragraph.indices.map { PartTarget.Paragraph(it) }
                ensureWritableBlock(blocks, design, targets)
            }

            PartMode.BOOK_PART -> {
                val part = boundPart?.value ?: return emptyList<TextBlock>() to emptyList()
                val pageDesign = when (partId.substringBefore(':')) {
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
                ensureWritableBlock(blocks, design, targets)
            }

            PartMode.NONE -> emptyList<TextBlock>() to emptyList()
        }
    }

    // A writable part needs at least one text control so the flow view can report a column width and
    // the user has somewhere to type; an empty prolog gets one empty paragraph block for that.
    private fun ensureWritableBlock(
        blocks: List<TextBlock>,
        design: org.pcsoft.app.aighost.model.project.design.Design,
        targets: List<PartTarget>
    ): Pair<List<TextBlock>, List<PartTarget>> {
        if (blocks.isNotEmpty()) return blocks to targets
        if (mode.value != PartMode.BOOK_PART && mode.value != PartMode.BLURB) return blocks to targets

        val style = org.pcsoft.app.aighost.layouting.model.common.StyleTranslation.toTextStyle(
            when (mode.value) {
                PartMode.BLURB -> design.blurbPage.textStyle
                else -> when (partId.substringBefore(':')) {
                    "prolog" -> design.prologPage.textStyle
                    "epilog" -> design.epilogPage.textStyle
                    else -> design.chapterPage.textStyle
                }
            }
        )
        return listOf(TextBlock(text = "", style = style)) to listOf(PartTarget.Paragraph(0))
    }

    /** Which kind of part the sheet shows. */
    enum class PartMode {
        /** Nothing writable is picked, so the sheet shows its empty state. */
        NONE,

        /** The title page is shown read only. */
        TITLE_PAGE,

        /** The copyright page is shown read only. */
        COPYRIGHT_PAGE,

        /** A prolog, a chapter or an epilog is edited through one flow. */
        BOOK_PART,

        /** The blurb is edited, a flow without a heading. */
        BLURB
    }

    /** What a single block of the current layout writes its text back to. */
    internal sealed interface PartTarget {

        /** The heading of the part. */
        data object Title : PartTarget

        /**
         * A further heading line of the part.
         *
         * @property modelIndex Index into the part's `titleAppendix` list, blank lines included.
         */
        data class AppendixLine(val modelIndex: Int) : PartTarget

        /**
         * A paragraph of the part.
         *
         * @property index Index into the part's `paragraph` list.
         */
        data class Paragraph(val index: Int) : PartTarget
    }
}

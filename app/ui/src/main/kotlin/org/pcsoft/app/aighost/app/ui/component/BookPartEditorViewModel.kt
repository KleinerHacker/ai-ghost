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
import org.pcsoft.app.aighost.app.controller.BookPartEditorController
import org.pcsoft.app.aighost.app.controller.IoController
import org.pcsoft.app.aighost.app.controller.PartMode
import org.pcsoft.app.aighost.app.controller.PartTarget
import org.pcsoft.app.aighost.app.undo.UndoStack
import org.pcsoft.app.aighost.fx.model.project.ProjectProperty
import org.pcsoft.app.aighost.layouting.DocumentLayout
import org.pcsoft.app.aighost.layouting.GreedyLineBreaker
import org.pcsoft.app.aighost.layouting.IncrementalLineBreaker
import org.pcsoft.app.aighost.layouting.fx.font.JavaFxTextMetrics
import org.pcsoft.app.aighost.layouting.fx.paper.PaperFlowListener
import org.pcsoft.app.aighost.layouting.fx.paper.PaperFlowView
import org.pcsoft.app.aighost.layouting.model.common.toPageGeometry

/**
 * View model of [BookPartEditor].
 *
 * The project is handed over through [bindProject], the picked tree node through [bindSelection] and
 * the undo history of the open project through [bindUndoStack]; all three are taken as the model
 * itself and never wrapped in a property of their own. The [PaperFlowView] the view holds is handed
 * in once through [attach].
 *
 * The domain logic - routing a tree node onto a part, assembling the sheet from the model and the
 * design, mapping a block back onto a manuscript field - lives in [BookPartEditorController]. This
 * view model keeps only what has a lifetime: the flow view it drives, the [IncrementalLineBreaker]
 * with its per-keystroke cache, one [StringProperty] per editable block for the undo history, the
 * caret and the current [BookPartEditorController.PartResolution].
 *
 * Every keystroke reported by [PaperFlowView] is written into the model and folded into a single undo
 * entry per block for the length of a typing pause; the layout on the sheet is recomputed after every
 * change and after every design change, so the caret survives a restyling. Only the block that
 * changed is measured again, the rest is read from the incremental breaker.
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

    // The part currently edited, resolved from the picked tree node by the controller.
    private var resolution: BookPartEditorController.PartResolution =
        BookPartEditorController.PartResolution(PartMode.NONE, null, "")

    // What each block of the current layout writes back to, in block order.
    private var targets: List<PartTarget> = emptyList()

    // One string property per block target, so a text change can be recorded as an undo step and an
    // undo can play it back through the same path a keystroke takes.
    private val targetProperties: MutableMap<PartTarget, StringProperty> = HashMap()

    // One line breaker for the whole lifetime of the editor: a block that did not change keeps its
    // broken lines instead of being measured again on the next keystroke. Cleared on a design change,
    // where the key - text, style, column width - would not show that every block was restyled.
    private val lineBreaker = IncrementalLineBreaker(GreedyLineBreaker(JavaFxTextMetrics))

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

    private val designListener = InvalidationListener {
        lineBreaker.clear()
        recompute()
    }
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
        lineBreaker.clear()

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
        targetProperties.clear()
        targets = emptyList()
        lineBreaker.clear()
    }

    private fun onSelectionChanged(item: ProjectListItem?) {
        lastSelection = item
        undoStack?.endMerging()
        focusedBlock = null
        targetProperties.clear()

        resolution = BookPartEditorController.resolve(project, item)
        mode.value = resolution.mode

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
        val projectProperty = project ?: return
        val property = propertyFor(target)
        val old = property.value ?: ""
        if (text == old) return

        writingTarget = true
        try {
            property.value = text
        } finally {
            writingTarget = false
        }
        BookPartEditorController.writeModel(projectProperty, resolution, target, text)
        undoStack?.record(
            Messages["component.bookPartEditor.undo.edit"],
            property,
            old,
            text,
            mergeKey = resolution.partId to target
        )
        recompute()
    }

    private fun propertyFor(target: PartTarget): StringProperty =
        targetProperties.getOrPut(target) {
            val initial = project?.let { BookPartEditorController.readModel(it, resolution, target) } ?: ""
            SimpleStringProperty(initial).apply {
                addListener { _, _, newValue ->
                    if (writingTarget) return@addListener
                    // Reached only through an undo or redo, which plays the value back the same way.
                    project?.let { BookPartEditorController.writeModel(it, resolution, target, newValue ?: "") }
                    recompute()
                }
            }
        }

    private fun recompute() {
        if (!::paperFlowView.isInitialized) return

        val project = this.project?.value
        val design = project?.design
        if (project == null || design == null || mode.value == PartMode.NONE) {
            targets = emptyList()
            pushLayout(null)
            return
        }

        val geometry = design.pageFormat.toPageGeometry()
        paperFlowView.pageGeometry = geometry

        val plan = BookPartEditorController.buildBlocks(project, design, resolution)
        targets = plan.targets

        if (plan.blocks.isEmpty()) {
            pushLayout(null)
            return
        }

        val columnWidth = BookPartEditorController.columnWidth(design, paperFlowView.columnWidth)
        pushLayout(BookPartEditorController.layout(plan.blocks, geometry, columnWidth, lineBreaker))
    }

    // Hands a layout to the flow view without the text-change events of the rebuild being taken for
    // edits.
    private fun pushLayout(layout: DocumentLayout?) {
        applyingLayout = true
        try {
            paperFlowView.documentLayout = layout
        } finally {
            applyingLayout = false
        }
    }
}

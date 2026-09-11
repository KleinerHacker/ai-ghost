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
import org.pcsoft.app.aighost.layouting.model.common.toPageLayout
import org.pcsoft.framework.simplay.engine.model.Document
import org.pcsoft.framework.simplay.engine.model.FlowPage
import org.pcsoft.framework.simplay.engine.model.Page
import org.pcsoft.framework.simplay.engine.model.SinglePage
import org.pcsoft.framework.simplay.fx.PaperSheetMode
import org.pcsoft.framework.simplay.fx.PaperSheetView

/**
 * View model of [BookPartEditor].
 *
 * The project is handed over through [bindProject], the picked tree node through [bindSelection] and
 * the undo history of the open project through [bindUndoStack]; all three are taken as the model
 * itself and never wrapped in a property of their own. The [PaperSheetView] the view holds is handed
 * in once through [attach].
 *
 * The domain logic - routing a tree node onto a part, assembling the blocks from the model and the
 * design, and mapping a block back onto a manuscript field - lives in [BookPartEditorController]. This
 * view model keeps only what has a lifetime: the sheet it drives, one [StringProperty] per editable
 * block for the undo history, and the current [BookPartEditorController.PartResolution]. Laying the
 * blocks out onto pages, breaking lines and pagination are [PaperSheetView]'s own concern.
 *
 * Every edit [PaperSheetView] makes replaces its whole `document`; this view model reads the changed
 * blocks back against [targets] and writes the ones that differ into the model, folded into a single
 * undo entry per block for the length of a typing pause. A design change rebuilds the `document` from
 * the model so every block picks up its new style; the caret's linear position is read before and
 * restored after, since restyling never changes the text itself.
 *
 * Splitting, merging, removing and reordering a paragraph is not attempted here: `PaperSheetView`'s
 * own editing turns a line break into a space and never creates a new block, so that stays IP-32's
 * job, wired as key handlers of its own. A delete reaching across a paragraph boundary can still merge
 * two of `PaperSheetView`'s blocks on its own; this view model detects that (the block count no longer
 * matches [targets]) and rejects it by rebuilding from the untouched model, rather than guessing which
 * paragraph the merged text belongs to.
 *
 * The component follows only models handed to it and registers nothing in a global registry, so the
 * `showingBinding` pattern of `fx-component-lifecycle` does not apply here, the same as for
 * [PaperSheetView] itself.
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

    // The sheet the view holds, handed in once after the FXML is loaded.
    private lateinit var paperSheetView: PaperSheetView

    private var project: ProjectProperty? = null
    private var undoStack: UndoStack? = null
    private var lastSelection: ProjectListItem? = null

    // The part currently edited, resolved from the picked tree node by the controller.
    private var resolution: BookPartEditorController.PartResolution =
        BookPartEditorController.PartResolution(PartMode.NONE, null, "")

    // What each block of the current document writes back to, in block order.
    private var targets: List<PartTarget> = emptyList()

    // One string property per block target, so a text change can be recorded as an undo step and an
    // undo can play it back through the same path a keystroke takes.
    private val targetProperties: MutableMap<PartTarget, StringProperty> = HashMap()

    // True while a freshly built document is handed to the sheet, so the document-change event that
    // assignment itself fires is not mistaken for an edit.
    private var applyingDocument = false

    // True while a target property is written from a reported edit, so its own listener does not
    // write the model twice.
    private var writingTarget = false

    private val designListener = InvalidationListener { recompute() }
    private var boundDesign: Observable? = null

    private val selectionListener =
        ChangeListener<ProjectListItem?> { _, _, newValue -> onSelectionChanged(newValue) }
    private var boundSelection: ObservableValue<ProjectListItem?>? = null

    private val documentListener =
        ChangeListener<Document?> { _, _, newValue -> handleDocumentChanged(newValue) }

    /**
     * Hands the sheet of the component over, once, and starts listening to it.
     *
     * @param paperSheetView the sheet held by [BookPartEditorView]
     */
    internal fun attach(paperSheetView: PaperSheetView) {
        this.paperSheetView = paperSheetView
        paperSheetView.documentProperty.addListener(documentListener)
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
        if (::paperSheetView.isInitialized) {
            paperSheetView.documentProperty.removeListener(documentListener)
        }
        targetProperties.clear()
        targets = emptyList()
    }

    private fun onSelectionChanged(item: ProjectListItem?) {
        lastSelection = item
        undoStack?.endMerging()
        targetProperties.clear()

        resolution = BookPartEditorController.resolve(project, item)
        mode.value = resolution.mode

        recompute()
    }

    // Reads back what PaperSheetView's own editing changed and writes the differing blocks into the
    // model; a block count that no longer matches targets means a delete merged two blocks across
    // their boundary, a structural change this plan does not attempt - it is rejected by recompute().
    private fun handleDocumentChanged(document: Document?) {
        if (applyingDocument) return
        if (mode.value != PartMode.BOOK_PART && mode.value != PartMode.BLURB) return
        val projectProperty = project ?: return

        val blocks = document?.pages?.firstOrNull()?.blocks.orEmpty()
        if (blocks.size != targets.size) {
            recompute()
            return
        }

        writingTarget = true
        try {
            targets.forEachIndexed { index, target ->
                val text = blocks[index].toString()
                val property = propertyFor(target)
                val old = property.value ?: ""
                if (text == old) return@forEachIndexed

                property.value = text
                BookPartEditorController.writeModel(projectProperty, resolution, target, text)
                undoStack?.record(
                    Messages["component.bookPartEditor.undo.edit"],
                    property,
                    old,
                    text,
                    mergeKey = resolution.partId to target
                )
            }
        } finally {
            writingTarget = false
        }
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
        if (!::paperSheetView.isInitialized) return

        val project = this.project?.value
        val design = project?.design
        if (project == null || design == null || mode.value == PartMode.NONE) {
            targets = emptyList()
            pushDocument(null)
            return
        }

        val plan = BookPartEditorController.buildBlocks(project, design, resolution)
        targets = plan.targets

        if (plan.blocks.isEmpty()) {
            pushDocument(null)
            return
        }

        paperSheetView.mode = if (resolution.mode == PartMode.BOOK_PART || resolution.mode == PartMode.BLURB) {
            PaperSheetMode.EDITABLE
        } else {
            PaperSheetMode.READONLY
        }

        val layout = design.pageFormat.toPageLayout()
        val page: Page = when (resolution.mode) {
            PartMode.TITLE_PAGE, PartMode.COPYRIGHT_PAGE -> SinglePage(layout, plan.blocks)
            else -> FlowPage(layout, plan.blocks)
        }
        pushDocument(Document(listOf(page)))
    }

    // Hands a freshly built document to the sheet without the document-change event of the assignment
    // itself being taken for an edit, and restores the caret's linear position afterwards - a rebuild
    // never changes the text, only the style, so the same linear offset still names the same character.
    private fun pushDocument(document: Document?) {
        val caretBefore = paperSheetView.caretModel.position
        applyingDocument = true
        try {
            paperSheetView.document = document
        } finally {
            applyingDocument = false
        }
        if (document != null && paperSheetView.mode == PaperSheetMode.EDITABLE) {
            paperSheetView.caretModel.moveTo(caretBefore)
        }
    }
}

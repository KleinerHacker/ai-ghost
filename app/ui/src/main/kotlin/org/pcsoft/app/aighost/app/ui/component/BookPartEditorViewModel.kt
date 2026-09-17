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
import javafx.application.Platform
import javafx.beans.InvalidationListener
import javafx.beans.Observable
import javafx.beans.binding.Bindings
import javafx.beans.binding.BooleanBinding
import javafx.beans.property.SimpleBooleanProperty
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
import org.pcsoft.app.aighost.fx.model.project.book.BookProperty
import org.pcsoft.app.aighost.layouting.model.project.DocumentStyleRefresher
import org.pcsoft.app.aighost.model.pref.WritingMode
import org.pcsoft.framework.simplay.engine.model.Document
import org.pcsoft.framework.simplay.fx.PaperSheetMode
import org.pcsoft.framework.simplay.fx.PaperSheetView
import org.pcsoft.framework.simplay.uicommon.PageMode

/**
 * View model of [BookPartEditor].
 *
 * The project is handed over through [bindProject], the picked tree node through [bindSelection] and
 * the undo history of the open project through [bindUndoStack]; all three are taken as the model
 * itself and never wrapped in a property of their own. The [PaperSheetView] the view holds is handed
 * in once through [attach].
 *
 * Since IP-39 there is exactly one `Document` for the whole book, built by
 * [BookPartEditorController.buildWholeDocument] and pushed to the sheet whenever the project is bound
 * or the design changes; a tree selection never swaps it, it only moves the sheet's caret to the
 * picked part's `TextAnchor` ([navigateTo]). The domain logic - resolving a tree node onto an anchor
 * id, assembling the whole document from the model and mapping a block back onto a manuscript field -
 * lives in [BookPartEditorController]. This view model keeps only what has a lifetime: the sheet it
 * drives, one [StringProperty] per block for the undo history, and the write-back target of every
 * block of the current document.
 *
 * Every edit [PaperSheetView] makes changes its `document` in place; this view model reads the pages
 * back against [targets] and writes the blocks that differ into the model, folded into a single undo
 * entry per block for the length of a typing pause. A design change restyles the book's stored
 * `Document` in place through [DocumentStyleRefresher] - text and every anchor stay untouched - and
 * only then rebuilds the sheet's whole-book `document` from it, so every block picks up its new style;
 * the caret's linear position is read before and restored after, since restyling never changes the
 * text itself. Typing itself never triggers a rebuild, so `simplay-engine.measure` only runs once per
 * project open or design change, not per keystroke.
 *
 * Splitting, merging, removing and reordering a paragraph is not attempted here: `PaperSheetView`'s
 * own editing turns a line break into a space and never creates a new block, so that stays IP-32's
 * job, wired as key handlers of its own. A delete reaching across a paragraph boundary can still merge
 * two of `PaperSheetView`'s blocks on its own; this view model detects that (a page's block count no
 * longer matches its targets) and rejects it by rebuilding the whole document from the untouched
 * model, rather than guessing which paragraph the merged text belongs to.
 *
 * A switched-off prolog, epilog or blurb (IP-23) still gets a page from [BookPartEditorController], so
 * its anchor keeps working - [applyPageModes] is what actually keeps it visibly inactive, through
 * [PaperSheetView.pageModes], a purely transient, view-side switch simPlay never persists into
 * `Document`. That map is cleared whenever [PaperSheetView.document] is reloaded from outside, which
 * [pushWholeDocument] does on every project bind and every design change, so [applyPageModes] runs
 * again right after; a toggle of the switch itself, reported through a listener on
 * [BookProperty.prologProperty], [BookProperty.epilogProperty] and [BookProperty.blurbProperty], reapplies
 * it on its own without rebuilding the document.
 *
 * The component follows only models handed to it and registers nothing in a global registry, so the
 * `showingBinding` pattern of `fx-component-lifecycle` does not apply here, the same as for
 * [PaperSheetView] itself.
 */
class BookPartEditorViewModel : ViewModel {

    /** Which kind of part is currently picked in the project tree. */
    val mode: SimpleObjectProperty<PartMode> = SimpleObjectProperty(this, "mode", PartMode.NONE)

    /** Whether a project is open, so the sheet is visible instead of the empty state. */
    val contentAvailable: SimpleBooleanProperty = SimpleBooleanProperty(this, "contentAvailable", false)

    /** Whether the picked part may be written; `false` only while nothing is picked yet. */
    val editable: BooleanBinding = Bindings.createBooleanBinding({ mode.value != PartMode.NONE }, mode)

    /** Whether the sheet is switched to writing or to preview, restored from and saved to the preferences. */
    val writingMode: SimpleObjectProperty<WritingMode> = SimpleObjectProperty(this, "writingMode", WritingMode.WRITING)

    /** Whether the whole book is measured for the first time after opening a project right now. */
    val loading: SimpleBooleanProperty = SimpleBooleanProperty(this, "loading", false)

    // The sheet the view holds, handed in once after the FXML is loaded.
    private lateinit var paperSheetView: PaperSheetView

    private var project: ProjectProperty? = null
    private var undoStack: UndoStack? = null
    private var lastSelection: ProjectListItem? = null

    // The write-back target of every block of the current document, keyed by page (anchor) id.
    private var targets: Map<String, List<PartTarget>> = emptyMap()

    // One string property per block target, so a text change can be recorded as an undo step and an
    // undo can play it back through the same path a keystroke takes.
    private val targetProperties: MutableMap<PartTarget, StringProperty> = HashMap()

    // True while a freshly built document is handed to the sheet, so the document-change event that
    // assignment itself fires is not mistaken for an edit.
    private var applyingDocument = false

    // True while a target property is written from a reported edit, so its own listener does not
    // write the model twice.
    private var writingTarget = false

    private val designListener = InvalidationListener { pushWholeDocument() }
    private var boundDesign: Observable? = null

    // The book of the bound project, kept only to remove includedListener again when the project is
    // rebound - BookProperty itself never changes for the life of a ProjectProperty.
    private var boundBook: BookProperty? = null

    // A field property of BookPartProperty reports every change of the part it wraps as an
    // invalidation of itself, no matter whether the switch was flipped from the project tree or from
    // somewhere else, so this alone keeps the sheet's page modes in step with the model.
    private val includedListener = InvalidationListener { applyPageModes() }

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
        writingMode.value = IoController.preferences.editorProperty.writingMode
        paperSheetView.mode = writingMode.value.toPaperSheetMode()
        paperSheetView.documentProperty.addListener(documentListener)
    }

    /**
     * Switches the sheet between writing and preview, applies it to [PaperSheetView.mode] and saves it
     * to the preferences of the user.
     *
     * @param mode the mode to switch to
     */
    internal fun setWritingMode(mode: WritingMode) {
        writingMode.value = mode
        IoController.preferences.editorProperty.writingMode = mode
        if (::paperSheetView.isInitialized) {
            paperSheetView.mode = mode.toPaperSheetMode()
        }
    }

    private fun WritingMode.toPaperSheetMode(): PaperSheetMode = when (this) {
        WritingMode.WRITING -> PaperSheetMode.EDITABLE
        WritingMode.PREVIEW -> PaperSheetMode.SELECTABLE
    }

    /**
     * Binds the sheet to the given project and releases the one bound before.
     *
     * @param project the open project, `null` to follow none
     */
    internal fun bindProject(project: ProjectProperty?) {
        boundDesign?.removeListener(designListener)
        boundBook?.prologProperty?.removeListener(includedListener)
        boundBook?.epilogProperty?.removeListener(includedListener)
        boundBook?.blurbProperty?.removeListener(includedListener)

        this.project = project
        boundDesign = project?.designProperty?.also { it.addListener(designListener) }
        boundBook = project?.bookProperty?.also {
            it.prologProperty.addListener(includedListener)
            it.epilogProperty.addListener(includedListener)
            it.blurbProperty.addListener(includedListener)
        }

        if (project == null || !::paperSheetView.isInitialized) {
            pushWholeDocument()
            if (project != null) {
                if (lastSelection != null) navigateTo(lastSelection) else restoreLastAnchor()
            }
            return
        }

        // The very first measure of a freshly opened project can be expensive for a long book.
        // simplay-engine.measure is a synchronous call on this same FX thread, so showing the indicator
        // before it runs needs a pulse of its own - otherwise nothing would ever be painted before the
        // call blocks the thread.
        loading.value = true
        Platform.runLater {
            pushWholeDocument()
            if (lastSelection != null) {
                navigateTo(lastSelection)
            } else {
                restoreLastAnchor()
            }
            loading.value = false
        }
    }

    // Moves the caret to the anchor last navigated to before the project was closed, without needing
    // to reconstruct the project tree node that once stood for it.
    private fun restoreLastAnchor() {
        if (!::paperSheetView.isInitialized || project == null) return
        val anchorId = IoController.preferences.editorProperty.lastAnchorId ?: return
        paperSheetView.caretModel.moveToAnchor(anchorId)
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
        boundBook?.prologProperty?.removeListener(includedListener)
        boundBook?.epilogProperty?.removeListener(includedListener)
        boundBook?.blurbProperty?.removeListener(includedListener)
        boundBook = null
        boundSelection?.removeListener(selectionListener)
        boundSelection = null
        if (::paperSheetView.isInitialized) {
            paperSheetView.documentProperty.removeListener(documentListener)
        }
        targetProperties.clear()
        targets = emptyMap()
    }

    private fun onSelectionChanged(item: ProjectListItem?) {
        undoStack?.endMerging()
        navigateTo(item)
    }

    // Moves the sheet's caret to the picked part's anchor instead of swapping the document.
    private fun navigateTo(item: ProjectListItem?) {
        lastSelection = item
        val resolution = BookPartEditorController.resolve(item)
        mode.value = if (project != null) resolution.mode else PartMode.NONE

        if (!::paperSheetView.isInitialized || project == null || resolution.anchorId.isEmpty()) return
        paperSheetView.caretModel.moveToAnchor(resolution.anchorId)
        IoController.preferences.editorProperty.lastAnchorId = resolution.anchorId
    }

    // Reads back what PaperSheetView's own editing changed and writes the differing blocks into the
    // model; a page whose block count no longer matches its targets means a delete merged two blocks
    // across their boundary, a structural change this plan does not attempt - it is rejected by
    // rebuilding the whole document from the untouched model.
    private fun handleDocumentChanged(document: Document?) {
        if (applyingDocument) return
        val projectProperty = project ?: return
        val proj = projectProperty.value ?: return
        val design = proj.design

        for (page in document?.pages.orEmpty()) {
            val pageTargets = targets[page.id] ?: continue
            if (page.blocks.size < pageTargets.size) {
                pushWholeDocument()
                return
            }

            writingTarget = true
            try {
                pageTargets.forEachIndexed { index, target ->
                    val text = page.blocks[index].toString()
                    val property = propertyFor(target)
                    val old = property.value ?: ""
                    if (text == old) return@forEachIndexed

                    property.value = text
                    BookPartEditorController.writeModel(projectProperty, design, target, text)
                    undoStack?.record(
                        Messages["component.bookPartEditor.undo.edit"],
                        property,
                        old,
                        text,
                        mergeKey = page.id to target
                    )
                }
            } finally {
                writingTarget = false
            }
        }
    }

    private fun propertyFor(target: PartTarget): StringProperty =
        targetProperties.getOrPut(target) {
            val initial = project?.let { BookPartEditorController.readModel(it, target) } ?: ""
            SimpleStringProperty(initial).apply {
                addListener { _, _, newValue ->
                    if (writingTarget) return@addListener
                    // Reached only through an undo or redo, which plays the value back the same way.
                    val projectProperty = project
                    val design = projectProperty?.value?.design
                    if (projectProperty != null && design != null) {
                        BookPartEditorController.writeModel(projectProperty, design, target, newValue ?: "")
                    }
                    pushWholeDocument()
                }
            }
        }

    // Rebuilds the whole-book document from the model and hands it to the sheet, without the
    // document-change event of the assignment itself being taken for an edit, and restores the
    // caret's linear position afterwards - a rebuild never changes the text, only the style, so the
    // same linear offset still names the same character.
    private fun pushWholeDocument() {
        val projectProperty = this.project
        val project = projectProperty?.value
        val design = project?.design
        contentAvailable.value = projectProperty != null && project != null && design != null

        if (projectProperty == null || project == null || design == null) {
            targets = emptyMap()
            targetProperties.clear()
            if (::paperSheetView.isInitialized) {
                applyingDocument = true
                try {
                    paperSheetView.document = null
                } finally {
                    applyingDocument = false
                }
            }
            return
        }

        // A design change never rewrites text or moves an anchor, only the style of the blocks that
        // already sit on each page - restyling the stored document here, through the property so the
        // write reaches the real Book instance, means buildWholeDocument() below reads it back already
        // in its new style, the same way it would after a reload.
        projectProperty.bookProperty.document = DocumentStyleRefresher.refresh(project.book, design)

        val plan = BookPartEditorController.buildWholeDocument(project, design, project.meta)
        targets = plan.targets
        targetProperties.clear()

        if (!::paperSheetView.isInitialized) return

        val caretBefore = paperSheetView.caretModel.position
        applyingDocument = true
        try {
            paperSheetView.document = plan.document
        } finally {
            applyingDocument = false
        }
        paperSheetView.caretModel.moveTo(caretBefore)
        // document was just reloaded from outside, which clears PaperSheetView.pageModes - reapplied
        // here so a switched-off part stays visibly inactive without a second call from the caller.
        applyPageModes()
    }

    // Keeps a switched-off prolog, epilog or blurb visibly inactive on the sheet through
    // PaperSheetView.pageModes, without rebuilding the document: the page itself keeps existing (and
    // keeps its anchor), only its interaction mode changes.
    private fun applyPageModes() {
        if (!::paperSheetView.isInitialized) return
        val book = project?.value?.book ?: return

        paperSheetView.pageModes = buildMap {
            if (!book.prolog.included) put("prolog", PageMode.DISABLED)
            if (!book.epilog.included) put("epilog", PageMode.DISABLED)
            if (!book.blurb.included) put("blurb", PageMode.DISABLED)
        }
    }
}

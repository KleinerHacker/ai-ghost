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

import de.saxsys.mvvmfx.FxmlView
import de.saxsys.mvvmfx.InjectViewModel
import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.scene.control.ToggleButton
import org.pcsoft.app.aighost.app.undo.UndoStack
import org.pcsoft.app.aighost.fx.model.project.ProjectProperty
import org.pcsoft.app.aighost.model.pref.WritingMode
import java.net.URL
import java.util.*

/**
 * View of [Editor], holding the project tree, the writing surface and the inspector in a horizontal
 * split.
 *
 * The split itself is described in the FXML; the view only passes the project model on to the tree,
 * the writing surface and the inspector, so none of them reads the project from anywhere else. The
 * writing surface and the inspector also follow the node picked in the tree, which is why
 * [pnlProjectList]'s selection is bound onto the view model here and handed to them. The undo history
 * is passed to [pnlProjectList] as well as to [bookPartEditor] (IP-23), since a toggled prolog, epilog
 * or blurb switch in the tree is recorded into the same history as a text change.
 *
 * The model arrives after this view was built, which is why the view is told about it through the
 * view model instead of reading it in [initialize].
 */
class EditorView : FxmlView<EditorViewModel>, Initializable {

    @FXML
    private lateinit var inspector: Inspector

    @FXML
    private lateinit var pnlProjectList: ProjectList

    @FXML
    private lateinit var bookPartEditor: BookPartEditor

    @FXML
    private lateinit var btnWritingMode: ToggleButton

    @InjectViewModel
    private lateinit var viewModel: EditorViewModel

    override fun initialize(location: URL?, resources: ResourceBundle?) {
        viewModel.onProjectBound = ::bindProject
        viewModel.project?.also(::bindProject)

        viewModel.onUndoStackBound = ::bindUndoStack
        viewModel.undoStack?.also(::bindUndoStack)

        viewModel.selectedProjectTreeItem.bind(pnlProjectList.selectedItem)
        inspector.bindSelection(viewModel.selectedProjectTreeItem)
        bookPartEditor.bindSelection(viewModel.selectedProjectTreeItem)

        btnWritingMode.isSelected = bookPartEditor.writingModeProperty.get() == WritingMode.PREVIEW
        bookPartEditor.writingModeProperty.addListener { _, _, mode ->
            btnWritingMode.isSelected = mode == WritingMode.PREVIEW
        }
    }

    /**
     * Switches the writing surface between writing and preview, following the tool bar's toggle
     * button.
     *
     * Triggered by [btnWritingMode].
     */
    @FXML
    private fun actionToggleWritingMode() {
        bookPartEditor.setWritingMode(if (btnWritingMode.isSelected) WritingMode.PREVIEW else WritingMode.WRITING)
    }

    /**
     * Passes the property model of the project on to the parts of the editor.
     *
     * @param project the project model of the surrounding window
     */
    private fun bindProject(project: ProjectProperty) {
        pnlProjectList.bindProject(project)
        inspector.bindProject(project)
        bookPartEditor.bindProject(project)
    }

    /**
     * Passes the undo history of the open project on to the tree and the writing surface.
     *
     * @param undoStack the one undo history of the surrounding window
     */
    private fun bindUndoStack(undoStack: UndoStack) {
        pnlProjectList.bindUndoStack(undoStack)
        bookPartEditor.bindUndoStack(undoStack)
    }
}

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
import org.pcsoft.app.aighost.fx.model.project.ProjectProperty
import java.net.URL
import java.util.*

/**
 * View of [Editor], holding the project tree and the editing area in a horizontal split.
 *
 * The split itself is described in the FXML; the view only passes the project model on to the tree
 * and to the manuscript editor, so neither of them reads the project from anywhere else.
 *
 * The model arrives after this view was built, which is why the view is told about it through the
 * view model instead of reading it in [initialize].
 */
class EditorView : FxmlView<EditorViewModel>, Initializable {

    @FXML
    private lateinit var bookEditor: BookEditor

    @FXML
    private lateinit var pnlProjectList: ProjectList

    @InjectViewModel
    private lateinit var viewModel: EditorViewModel

    override fun initialize(location: URL?, resources: ResourceBundle?) {
        viewModel.onProjectBound = ::bindProject
        viewModel.project?.also(::bindProject)

        viewModel.selectedProjectTreeItem.bind(pnlProjectList.selectedItem)

        bookEditor.visibleProperty().bind(viewModel.showBookEditor)
    }

    /**
     * Passes the property model of the project on to the parts of the editor.
     *
     * The manuscript editor works on the book of that project, which is a property model of its own
     * and stays the same instance while the project inside it is exchanged.
     *
     * @param project the project model of the surrounding window
     */
    private fun bindProject(project: ProjectProperty) {
        pnlProjectList.bindProject(project)
        bookEditor.bindBook(project.bookProperty)
    }
}

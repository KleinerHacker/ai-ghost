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
 * View of [Editor], holding the project tree, the editing area and the inspector in a horizontal
 * split.
 *
 * The split itself is described in the FXML; the view only passes the project model on to the tree
 * and to the inspector, so neither of them reads the project from anywhere else. The inspector also
 * follows the node picked in the tree, which is why [pnlProjectList]'s selection is bound onto the
 * view model here and handed to it.
 *
 * The model arrives after this view was built, which is why the view is told about it through the
 * view model instead of reading it in [initialize].
 */
class EditorView : FxmlView<EditorViewModel>, Initializable {

    @FXML
    private lateinit var inspector: Inspector

    @FXML
    private lateinit var pnlProjectList: ProjectList

    @InjectViewModel
    private lateinit var viewModel: EditorViewModel

    override fun initialize(location: URL?, resources: ResourceBundle?) {
        viewModel.onProjectBound = ::bindProject
        viewModel.project?.also(::bindProject)

        viewModel.selectedProjectTreeItem.bind(pnlProjectList.selectedItem)
        inspector.bindSelection(viewModel.selectedProjectTreeItem)
    }

    /**
     * Passes the property model of the project on to the parts of the editor.
     *
     * @param project the project model of the surrounding window
     */
    private fun bindProject(project: ProjectProperty) {
        pnlProjectList.bindProject(project)
        inspector.bindProject(project)
    }
}

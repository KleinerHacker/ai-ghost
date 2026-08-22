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
import java.net.URL
import java.util.ResourceBundle

/**
 * View of [Editor], holding the project tree and the editing area in a horizontal split.
 *
 * The split itself is described in the FXML; the view only passes the project on to the tree, so the
 * tree never reads the project from anywhere but its own property.
 */
class EditorView : FxmlView<EditorViewModel>, Initializable {

    @FXML
    private lateinit var pnlProjectList: ProjectList

    @InjectViewModel
    private lateinit var viewModel: EditorViewModel

    override fun initialize(location: URL?, resources: ResourceBundle?) {
        pnlProjectList.project.bind(viewModel.project)
    }
}

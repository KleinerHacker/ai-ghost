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

import de.saxsys.mvvmfx.FluentViewLoader
import javafx.scene.layout.BorderPane
import org.pcsoft.app.aighost.fx.model.project.ProjectProperty

/**
 * Puts the project tree and the editing area side by side, separated by a splitter the user moves.
 *
 * The component owns no data of its own: [bindProject] hands it the property model of the project,
 * which it passes on to the [ProjectList] on the left and to the [BookEditor] on the right.
 */
class Editor : BorderPane() {

    private val viewModel: EditorViewModel

    init {
        FluentViewLoader.fxmlView(EditorView::class.java).let {
            it.root(this)
            it.load().let {
                viewModel = it.viewModel
            }
        }
    }

    /**
     * Hands the property model of the project to the editor and to everything below it.
     *
     * @param project the project model of the surrounding window
     */
    fun bindProject(project: ProjectProperty) = viewModel.bind(project)
}

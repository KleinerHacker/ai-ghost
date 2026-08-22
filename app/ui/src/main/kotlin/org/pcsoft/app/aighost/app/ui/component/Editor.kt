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
import javafx.beans.property.ObjectProperty
import javafx.scene.layout.BorderPane
import org.pcsoft.app.aighost.model.project.Project

/**
 * Puts the project tree and the editing area side by side, separated by a splitter the user moves.
 *
 * The component owns no data of its own. [project] is bound by whoever shows the component and is
 * handed on to the [ProjectList] on the left; the area on the right shows a placeholder until the
 * editing parts exist.
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

    /** The project being edited, absent while no project is open. */
    val project: ObjectProperty<Project?> by viewModel::project
}

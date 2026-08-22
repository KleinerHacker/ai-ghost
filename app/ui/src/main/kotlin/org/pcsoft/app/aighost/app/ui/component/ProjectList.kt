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
import javafx.beans.property.ReadOnlyObjectProperty
import javafx.scene.layout.BorderPane
import org.pcsoft.app.aighost.model.project.Project

/**
 * Shows the open project as a tree: prolog, chapters, epilog and blurb below the project itself.
 *
 * The component owns no data of its own. [project] is bound by whoever shows the component, and
 * [selectedItem] reports every change of the selection back, so the surrounding window decides what
 * to open.
 */
class ProjectList : BorderPane() {

    private val viewModel: ProjectListViewModel

    init {
        FluentViewLoader.fxmlView(ProjectListView::class.java).let {
            it.root(this)
            it.load().let {
                viewModel = it.viewModel
            }
        }
    }

    /** The project shown by the tree, absent while no project is open. */
    val project: ObjectProperty<Project?> by viewModel::project

    /** The node the user picked in the tree, absent while nothing is selected. */
    val selectedItem: ReadOnlyObjectProperty<ProjectListItem?> by viewModel::selectedItem
}

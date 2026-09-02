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
import javafx.beans.value.ObservableValue
import javafx.scene.layout.BorderPane
import org.pcsoft.app.aighost.fx.model.project.ProjectProperty

/**
 * Editing surface next to the manuscript, holding what does not belong on the page itself: the fields
 * of the book and, for whatever part of it is picked in [ProjectList], the fields of that part.
 *
 * Fixed, always the same named collapsible sections lay the fields out - "Book" and "Chapter" - each
 * holding its own collapse state. A section that does not match what is bound to it shows an empty
 * state instead of stale or misleading fields.
 *
 * The component owns no data of its own: [bindProject] hands it the property model of the open
 * project, and [bindSelection] tells it which node of the project tree is currently picked; both are
 * the only way in, so everything the user writes lands in the bound objects right away.
 */
class Inspector : BorderPane() {

    private val viewModel: InspectorViewModel

    init {
        FluentViewLoader.fxmlView(InspectorView::class.java).let {
            it.root(this)
            it.load().let { tuple ->
                viewModel = tuple.viewModel
            }
        }
    }

    /**
     * Hands the property model of the open project to the "Book" section.
     *
     * @param project the open project, `null` to edit none
     */
    fun bindProject(project: ProjectProperty?) = viewModel.bindProject(project)

    /**
     * Lets the "Chapter" section follow the node picked in the project tree.
     *
     * @param selection the selection reported by [ProjectList]
     */
    fun bindSelection(selection: ObservableValue<ProjectListItem?>) = viewModel.bindSelection(selection)
}

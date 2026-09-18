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
import javafx.beans.property.ReadOnlyObjectProperty
import javafx.scene.layout.BorderPane

/**
 * Navigation of the project settings dialog: a tree without a visible root, one node per
 * [ProjectSettingsSection].
 *
 * The component owns no data of its own and never touches the project. It only reports which section
 * the user picked through [selectedSection], so the dialog around it decides which editor to show.
 */
class ProjectSettingsTree : BorderPane() {

    private val view: ProjectSettingsTreeView
    private val viewModel: ProjectSettingsTreeViewModel

    init {
        FluentViewLoader.fxmlView(ProjectSettingsTreeView::class.java).let {
            it.root(this)
            it.load().let { loaded ->
                view = loaded.codeBehind
                viewModel = loaded.viewModel
            }
        }
    }

    /** The section the user picked in the tree, absent while nothing is selected. */
    val selectedSection: ReadOnlyObjectProperty<ProjectSettingsSection?> by viewModel::selectedSection

    /** Moves the selection to [section], as if the user had clicked its node. */
    fun select(section: ProjectSettingsSection) = view.select(section)
}

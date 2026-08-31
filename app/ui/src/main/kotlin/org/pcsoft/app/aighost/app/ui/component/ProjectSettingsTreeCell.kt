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

import javafx.scene.control.TreeCell
import java.util.ResourceBundle

/**
 * Renders one [ProjectSettingsSection] inside the navigation tree of [ProjectSettingsTree].
 *
 * Every node is labelled from the message bundle, so the tree speaks the same language as the FXML
 * around it. Built as a named class rather than an inline cell, so it mirrors [ProjectListCell].
 *
 * @property messages the bundle the view was loaded with, so a cell speaks the same language as the
 *   FXML around it
 */
internal class ProjectSettingsTreeCell(private val messages: ResourceBundle) : TreeCell<ProjectSettingsSection>() {

    override fun updateItem(item: ProjectSettingsSection?, empty: Boolean) {
        super.updateItem(item, empty)

        text = if (empty || item == null) null else messages.getString(item.bundleKey)
    }
}

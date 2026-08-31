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

package org.pcsoft.app.aighost.app.ui.dialog

import de.saxsys.mvvmfx.FxmlView
import de.saxsys.mvvmfx.InjectViewModel
import javafx.beans.binding.BooleanExpression
import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.scene.image.ImageView
import javafx.scene.layout.StackPane
import org.pcsoft.app.aighost.app.AiGhostIcons
import org.pcsoft.app.aighost.app.ui.component.DesignSettings
import org.pcsoft.app.aighost.app.ui.component.PlaceholderSettings
import org.pcsoft.app.aighost.app.ui.component.ProjectSettingsSection
import org.pcsoft.app.aighost.app.ui.component.ProjectSettingsTree
import java.net.URL
import java.util.ResourceBundle

/**
 * View of [ProjectSettingsDialog]: the navigation tree on the left, the editor of the picked section
 * on the right.
 *
 * Only "Design" carries a real editor today; every other section shows [PlaceholderSettings]. That
 * editor is bound once to the working copy of the dialog; picking a node only swaps which editor is
 * at the front.
 */
class ProjectSettingsDialogView : FxmlView<ProjectSettingsDialogViewModel>, Initializable {

    @FXML
    private lateinit var imgIcon: ImageView

    @FXML
    private lateinit var tree: ProjectSettingsTree

    @FXML
    private lateinit var content: StackPane

    @FXML
    private lateinit var design: DesignSettings

    @FXML
    private lateinit var placeholder: PlaceholderSettings

    @InjectViewModel
    private lateinit var viewModel: ProjectSettingsDialogViewModel

    /** Whether the input of the currently shown editor can be stored. */
    val valid: BooleanExpression get() = design.valid

    override fun initialize(location: URL?, resources: ResourceBundle?) {
        imgIcon.image = AiGhostIcons.projectSettings

        design.bindDesign(viewModel.workingDesign)

        tree.selectedSection.addListener { _, _, section -> showSection(section) }
        showSection(tree.selectedSection.value)
    }

    private fun showSection(section: ProjectSettingsSection?) {
        val current = section ?: ProjectSettingsSection.Design
        val showsEditor = current.implemented

        design.isVisible = showsEditor
        design.isManaged = showsEditor
        placeholder.isVisible = !showsEditor
        placeholder.isManaged = !showsEditor

        if (!showsEditor) placeholder.setSection(current)
    }
}

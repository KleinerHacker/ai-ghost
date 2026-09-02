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
import javafx.beans.binding.Bindings
import javafx.beans.binding.BooleanExpression
import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.scene.image.ImageView
import javafx.scene.layout.StackPane
import org.pcsoft.app.aighost.app.AiGhostIcons
import org.pcsoft.app.aighost.app.ui.component.BookPartPageDesignSettings
import org.pcsoft.app.aighost.app.ui.component.ChapterPageDesignSettings
import org.pcsoft.app.aighost.app.ui.component.CopyrightPageDesignSettings
import org.pcsoft.app.aighost.app.ui.component.DesignSettings
import org.pcsoft.app.aighost.app.ui.component.PlaceholderSettings
import org.pcsoft.app.aighost.app.ui.component.ProjectSettingsSection
import org.pcsoft.app.aighost.app.ui.component.ProjectSettingsTree
import org.pcsoft.app.aighost.app.ui.component.StyleDataEditor
import org.pcsoft.app.aighost.app.ui.component.TitlePageDesignSettings
import java.net.URL
import java.util.ResourceBundle

/**
 * View of [ProjectSettingsDialog]: the navigation tree on the left, the editor of the picked section
 * on the right.
 *
 * Every design section carries a real editor today, each bound once to the working copy of the
 * dialog when the view is built; picking a node only swaps which editor is at the front.
 * [PlaceholderSettings] stays as the fallback for a section without an editor of its own, currently
 * only "General".
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
    private lateinit var titleDesign: TitlePageDesignSettings

    @FXML
    private lateinit var copyrightDesign: CopyrightPageDesignSettings

    @FXML
    private lateinit var prologDesign: BookPartPageDesignSettings

    @FXML
    private lateinit var chapterDesign: ChapterPageDesignSettings

    @FXML
    private lateinit var epilogDesign: BookPartPageDesignSettings

    @FXML
    private lateinit var blurbDesign: StyleDataEditor

    @FXML
    private lateinit var placeholder: PlaceholderSettings

    @InjectViewModel
    private lateinit var viewModel: ProjectSettingsDialogViewModel

    /** Whether the input of the currently shown editor can be stored. */
    val valid: BooleanExpression
        get() = Bindings.and(design.valid, titleDesign.valid)
            .and(copyrightDesign.valid)
            .and(prologDesign.valid)
            .and(chapterDesign.valid)
            .and(epilogDesign.valid)
            .and(blurbDesign.valid)

    override fun initialize(location: URL?, resources: ResourceBundle?) {
        imgIcon.image = AiGhostIcons.projectSettings

        design.bindDesign(viewModel.workingDesign)
        titleDesign.bindDesign(viewModel.workingDesign.titlePageProperty)
        copyrightDesign.bindDesign(viewModel.workingDesign.copyrightPageProperty)
        prologDesign.bindStyles(
            viewModel.workingDesign.prologPageProperty.titleStyleProperty,
            viewModel.workingDesign.prologPageProperty.titleAppendixStyleProperty,
            viewModel.workingDesign.prologPageProperty.textStyleProperty
        )
        chapterDesign.bindDesign(viewModel.workingDesign.chapterPageProperty)
        epilogDesign.bindStyles(
            viewModel.workingDesign.epilogPageProperty.titleStyleProperty,
            viewModel.workingDesign.epilogPageProperty.titleAppendixStyleProperty,
            viewModel.workingDesign.epilogPageProperty.textStyleProperty
        )
        blurbDesign.bindStyle(viewModel.workingDesign.blurbPageProperty.textStyleProperty)

        tree.selectedSection.addListener { _, _, section -> showSection(section) }
        showSection(tree.selectedSection.value)
    }

    private fun showSection(section: ProjectSettingsSection?) {
        val current = section ?: ProjectSettingsSection.Design

        val shownNode = when (current) {
            ProjectSettingsSection.Design -> design
            ProjectSettingsSection.DesignTitle -> titleDesign
            ProjectSettingsSection.DesignCopyright -> copyrightDesign
            ProjectSettingsSection.DesignProlog -> prologDesign
            ProjectSettingsSection.DesignChapter -> chapterDesign
            ProjectSettingsSection.DesignEpilog -> epilogDesign
            ProjectSettingsSection.DesignBlurb -> blurbDesign
            ProjectSettingsSection.General -> null
        }

        for (node in listOf(design, titleDesign, copyrightDesign, prologDesign, chapterDesign, epilogDesign, blurbDesign, placeholder)) {
            val visible = node === shownNode
            node.isVisible = visible
            node.isManaged = visible
        }

        if (shownNode == null) {
            placeholder.isVisible = true
            placeholder.isManaged = true
            placeholder.setSection(current)
        }
    }
}

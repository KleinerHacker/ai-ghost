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
import javafx.scene.control.TreeItem
import javafx.scene.control.TreeView
import org.pcsoft.app.aighost.app.Messages
import java.net.URL
import java.util.ResourceBundle

/**
 * View of [ProjectSettingsTree], showing every [ProjectSettingsSection] as a fixed tree node.
 *
 * The shape of the tree is fixed and built here, the same way [ProjectListView] builds its own:
 * `General` sits at the top, `Design` below it carries the part sections. The root is hidden, so the
 * two top nodes read as the top level. The cells label themselves from the message bundle, so the
 * tree speaks the same language as the dialog around it.
 */
class ProjectSettingsTreeView : FxmlView<ProjectSettingsTreeViewModel>, Initializable {

    @FXML
    private lateinit var treSettings: TreeView<ProjectSettingsSection>

    @InjectViewModel
    private lateinit var viewModel: ProjectSettingsTreeViewModel

    private val generalItem = TreeItem<ProjectSettingsSection>(ProjectSettingsSection.General)
    private val designItem = TreeItem<ProjectSettingsSection>(ProjectSettingsSection.Design)
    private val designTitleItem = TreeItem<ProjectSettingsSection>(ProjectSettingsSection.DesignTitle)
    private val designCopyrightItem = TreeItem<ProjectSettingsSection>(ProjectSettingsSection.DesignCopyright)
    private val designEpilogItem = TreeItem<ProjectSettingsSection>(ProjectSettingsSection.DesignEpilog)
    private val designChapterItem = TreeItem<ProjectSettingsSection>(ProjectSettingsSection.DesignChapter)
    private val designPrologItem = TreeItem<ProjectSettingsSection>(ProjectSettingsSection.DesignProlog)
    private val designBlurbItem = TreeItem<ProjectSettingsSection>(ProjectSettingsSection.DesignBlurb)

    private val itemsBySection: Map<ProjectSettingsSection, TreeItem<ProjectSettingsSection>> = mapOf(
        ProjectSettingsSection.General to generalItem,
        ProjectSettingsSection.Design to designItem,
        ProjectSettingsSection.DesignTitle to designTitleItem,
        ProjectSettingsSection.DesignCopyright to designCopyrightItem,
        ProjectSettingsSection.DesignEpilog to designEpilogItem,
        ProjectSettingsSection.DesignChapter to designChapterItem,
        ProjectSettingsSection.DesignProlog to designPrologItem,
        ProjectSettingsSection.DesignBlurb to designBlurbItem
    )

    override fun initialize(location: URL?, resources: ResourceBundle?) {
        // The cells label the nodes themselves, so they need the same bundle the FXML around them
        // was resolved with.
        val messages = resources ?: Messages.bundle

        designItem.children.setAll(
            designTitleItem, designCopyrightItem,
            designEpilogItem, designChapterItem, designPrologItem, designBlurbItem
        )

        val root = TreeItem<ProjectSettingsSection>()
        root.children.setAll(generalItem, designItem)
        root.isExpanded = true
        designItem.isExpanded = true

        treSettings.root = root
        treSettings.isShowRoot = false
        treSettings.setCellFactory { ProjectSettingsTreeCell(messages) }

        treSettings.selectionModel.selectedItemProperty().addListener { _, _, selected ->
            viewModel.select(selected?.value)
        }
        treSettings.selectionModel.select(designItem)
    }

    /**
     * Moves the tree selection to the node of [section].
     *
     * @param section the section to select
     */
    internal fun select(section: ProjectSettingsSection) {
        itemsBySection[section]?.let { treSettings.selectionModel.select(it) }
    }
}

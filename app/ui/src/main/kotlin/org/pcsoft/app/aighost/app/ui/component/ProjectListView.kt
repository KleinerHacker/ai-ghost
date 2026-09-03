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
import javafx.beans.Observable
import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.scene.control.TreeItem
import javafx.scene.control.TreeView
import org.pcsoft.app.aighost.app.Messages
import java.net.URL
import java.util.ResourceBundle

/**
 * View of [ProjectList], showing the open project as a tree.
 *
 * The shape of the tree is fixed: below the project sit the title page, the copyright page, the
 * prolog, chapters, epilog and blurb, and the chapters branch carries one node per chapter of the
 * book. The fixed nodes exist even while their part has not been written yet, so the user sees where
 * to add it.
 */
class ProjectListView : FxmlView<ProjectListViewModel>, Initializable {
    @FXML
    private lateinit var treProject: TreeView<ProjectListItem>

    @InjectViewModel
    private lateinit var viewModel: ProjectListViewModel

    private val titlePageItem = TreeItem<ProjectListItem>(ProjectListItem.TitlePageItem)
    private val copyrightPageItem = TreeItem<ProjectListItem>(ProjectListItem.CopyrightPageItem)
    private val prologItem = TreeItem<ProjectListItem>(ProjectListItem.PrologItem(null))
    private val chaptersItem = TreeItem<ProjectListItem>(ProjectListItem.Chapters)
    private val epilogItem = TreeItem<ProjectListItem>(ProjectListItem.EpilogItem(null))
    private val blurbItem = TreeItem<ProjectListItem>(ProjectListItem.BlurbItem(null))

    override fun initialize(location: URL?, resources: ResourceBundle?) {
        // The cells label the fixed nodes themselves, so they need the same bundle the FXML around
        // them was resolved with.
        val messages = resources ?: Messages.bundle

        val rootItem = TreeItem<ProjectListItem>(ProjectListItem.Root)
        rootItem.children.setAll(titlePageItem, copyrightPageItem, prologItem, chaptersItem, epilogItem, blurbItem)
        rootItem.isExpanded = true
        chaptersItem.isExpanded = true

        treProject.root = rootItem
        treProject.isShowRoot = true
        treProject.setCellFactory { ProjectListCell(messages) }

        // The nodes stay in place while the project changes; only the model object they carry is
        // exchanged, so an expanded branch is not collapsed by a reload.
        viewModel.prolog.addListener { _: Observable -> prologItem.value = ProjectListItem.PrologItem(viewModel.prolog.value) }
        viewModel.epilog.addListener { _: Observable -> epilogItem.value = ProjectListItem.EpilogItem(viewModel.epilog.value) }
        viewModel.blurb.addListener { _: Observable -> blurbItem.value = ProjectListItem.BlurbItem(viewModel.blurb.value) }
        viewModel.chapters.addListener { _: Observable -> updateChapters() }
        updateChapters()

        treProject.selectionModel.selectedItemProperty().addListener { _, _, selected ->
            viewModel.select(selected?.value)
        }
    }

    private fun updateChapters() {
        // Built in a loop on purpose: on an observable list `map` resolves to the JavaFX binding
        // operator instead of the Kotlin collection function.
        val items = ArrayList<TreeItem<ProjectListItem>>(viewModel.chapters.size)
        for (chapter in viewModel.chapters) {
            items += TreeItem<ProjectListItem>(ProjectListItem.ChapterItem(chapter))
        }
        chaptersItem.children.setAll(items)
    }
}

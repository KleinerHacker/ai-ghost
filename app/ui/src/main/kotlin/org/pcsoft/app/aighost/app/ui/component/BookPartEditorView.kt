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
import javafx.scene.Node
import javafx.scene.control.ContextMenu
import javafx.scene.control.Label
import javafx.scene.control.MenuItem
import javafx.scene.control.ProgressIndicator
import org.pcsoft.app.aighost.app.Messages
import org.pcsoft.framework.simplay.fx.PaperSheetView
import java.net.URL
import java.util.ResourceBundle

/**
 * View of [BookPartEditor].
 *
 * The FXML holds the [PaperSheetView], an empty-state label and a progress indicator stacked on top
 * of each other; exactly one of them is shown, decided by whether the whole book is being measured for
 * the first time ([BookPartEditorViewModel.loading]) and, once it is not, by whether a project is open
 * ([BookPartEditorViewModel.contentAvailable]). The view hands the sheet to the view model once and
 * lets the view model do the rest.
 *
 * The sheet's context menu (IP-32) offers the same split, merge, remove and move commands as the
 * `Enter` and `Ctrl+Shift+Up`/`Ctrl+Shift+Down` keys of [BookPartEditorViewModel] - every item calls
 * straight into the view model, which resolves the block the caret currently sits in itself.
 */
class BookPartEditorView : FxmlView<BookPartEditorViewModel>, Initializable {

    @FXML
    private lateinit var sheet: PaperSheetView

    @FXML
    private lateinit var lblEmpty: Label

    @FXML
    private lateinit var prgLoading: ProgressIndicator

    @InjectViewModel
    private lateinit var viewModel: BookPartEditorViewModel

    override fun initialize(location: URL?, resources: ResourceBundle?) {
        viewModel.attach(sheet)
        sheet.contextMenu = buildContextMenu()

        refreshVisibility()
        viewModel.contentAvailable.addListener { _, _, _ -> refreshVisibility() }
        viewModel.loading.addListener { _, _, _ -> refreshVisibility() }
    }

    private fun buildContextMenu(): ContextMenu = ContextMenu(
        MenuItem(Messages["component.bookPartEditor.contextMenu.split"]).apply {
            setOnAction { viewModel.performSplit() }
        },
        MenuItem(Messages["component.bookPartEditor.contextMenu.mergePrevious"]).apply {
            setOnAction { viewModel.performMerge(withPrevious = true) }
        },
        MenuItem(Messages["component.bookPartEditor.contextMenu.mergeNext"]).apply {
            setOnAction { viewModel.performMerge(withPrevious = false) }
        },
        MenuItem(Messages["component.bookPartEditor.contextMenu.remove"]).apply {
            setOnAction { viewModel.performRemove() }
        },
        MenuItem(Messages["component.bookPartEditor.contextMenu.moveUp"]).apply {
            setOnAction { viewModel.performMove(up = true) }
        },
        MenuItem(Messages["component.bookPartEditor.contextMenu.moveDown"]).apply {
            setOnAction { viewModel.performMove(up = false) }
        },
    )

    private fun refreshVisibility() {
        val loading = viewModel.loading.get()
        val available = viewModel.contentAvailable.get()

        showExactly(prgLoading, loading)
        showExactly(sheet, !loading && available)
        showExactly(lblEmpty, !loading && !available)
    }

    private fun showExactly(node: Node, visible: Boolean) {
        node.isVisible = visible
        node.isManaged = visible
    }
}

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

package org.pcsoft.app.aighost.app.ui.window

import de.saxsys.mvvmfx.FxmlView
import de.saxsys.mvvmfx.InjectViewModel
import javafx.beans.Observable
import javafx.beans.binding.BooleanBinding
import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.scene.control.Menu
import javafx.scene.control.MenuItem
import javafx.scene.layout.BorderPane
import org.pcsoft.app.aighost.app.AiGhostIcons
import org.pcsoft.app.aighost.app.ui.component.Editor
import org.pcsoft.app.aighost.app.ui.showingBinding
import java.net.URL
import java.util.ResourceBundle

/**
 * View of the application main window, holding the menu bar, the tool bar and the tab pane.
 *
 * Texts come from the global resource bundle via the `%key` syntax of the FXML file; the menu icons
 * are attached by the FXML file itself through the `fx:factory` methods of [AiGhostIcons].
 */
class MainWindowView : FxmlView<MainWindowViewModel>, Initializable {
    @FXML
    private lateinit var pnlRoot: BorderPane
    @FXML
    private lateinit var mnuOpenRecent: Menu
    @FXML
    private lateinit var editor: Editor

    @InjectViewModel
    private lateinit var viewModel: MainWindowViewModel

    // Kept as a field: the binding is only held by this view, and a garbage collected binding would
    // stop notifying, so the menu would never be filled again.
    private lateinit var showing: BooleanBinding

    override fun initialize(location: URL?, resources: ResourceBundle?) {
        // The preferences report nothing, so the view model reads them again every time this
        // component comes on screen.
        showing = pnlRoot.showingBinding()
        showing.addListener { _, _, onScreen -> if (onScreen) viewModel.onShow() }

        viewModel.openRecent.addListener { _: Observable ->
            mnuOpenRecent.items.setAll(viewModel.openRecent.value.map {
                MenuItem(it.name)
            })
        }
        mnuOpenRecent.disableProperty().bind(viewModel.openRecentDisabled)

        editor.project.bind(viewModel.project)
    }

    @FXML
    private fun actionHelpOnline() {

    }

    @FXML
    private fun actionExit() {
        pnlRoot.scene.window.hide()
    }
}

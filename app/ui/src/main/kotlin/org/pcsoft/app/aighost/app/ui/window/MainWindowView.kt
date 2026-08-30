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
import javafx.event.EventHandler
import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.scene.control.Label
import javafx.scene.control.Menu
import javafx.scene.control.MenuItem
import javafx.scene.layout.BorderPane
import javafx.scene.layout.VBox
import javafx.stage.FileChooser
import org.pcsoft.app.aighost.app.AiGhostIcons
import org.pcsoft.app.aighost.app.ui.component.Editor
import org.pcsoft.app.aighost.app.ui.showingBinding
import java.io.File
import java.net.URL
import java.util.*

/**
 * View of the application main window, holding the menu bar, the tool bar and the tab pane.
 *
 * This view manages the main user interface components and coordinates user interactions with the
 * underlying [MainWindowViewModel]. It asks the user for a file where one is needed and leaves
 * everything else - reading, writing and the open project itself - to the view model.
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

    /**
     * Binding that tracks whether this view is currently visible on screen.
     *
     * This field must be kept as a strong reference because JavaFX bindings are weakly referenced.
     * If this binding were garbage collected, it would stop notifying listeners, which would prevent
     * the view model from refreshing preferences when the window becomes visible.
     */
    private lateinit var showing: BooleanBinding

    /**
     * Initializes the view after the FXML file has been loaded.
     *
     * Sets up the visibility tracking to refresh the view model when the window appears on screen,
     * binds the recent files menu to the view model's list of recent projects, and hands the project
     * of the window to the editor component.
     *
     * @param location The location used to resolve relative paths for the root object, or null if unknown.
     * @param resources The resources used to localize the root object, or null if not localized.
     */
    override fun initialize(location: URL?, resources: ResourceBundle?) {
        // The preferences report nothing, so the view model reads them again every time this
        // component comes on screen.
        showing = pnlRoot.showingBinding()
        showing.addListener { _, _, onScreen -> if (onScreen) viewModel.onShow() else viewModel.onHide() }

        viewModel.openRecent.addListener { _: Observable ->
            mnuOpenRecent.items.setAll(viewModel.openRecent.value.map(::recentFileItem))
        }
        mnuOpenRecent.disableProperty().bind(viewModel.openRecentDisabled)

        // The editor works on the property model of the window instead of on a project handed to it
        // through a property of its own, so a property never carries another property.
        editor.bindProject(viewModel.project)


    }

    /**
     * Builds the menu entry of a recently opened project: its file name above, the directory it sits
     * in below.
     *
     * Both lines are carried by the graphic of the item instead of its text, because JavaFX puts a
     * graphic beside the text and not above it. The appearance of the two lines belongs to the
     * stylesheet, so they only receive their style class here.
     *
     * @param file The project file the entry stands for.
     */
    private fun recentFileItem(file: File): MenuItem {
        val name = Label(file.name).apply { styleClass += "recent-file-name" }
        val directory = Label(file.parent ?: "").apply { styleClass += "recent-file-directory" }

        return MenuItem().apply {
            graphic = VBox(name, directory).apply { styleClass += "recent-file" }
            onAction = EventHandler { viewModel.openProject(file) }
        }
    }

    /**
     * Opens the online help in the user's default web browser.
     *
     * This action is triggered by the corresponding menu item in the Help menu.
     * Currently not implemented.
     */
    @FXML
    private fun actionHelpOnline() {

    }

    /**
     * Exits the application by hiding the main window.
     *
     * This action is triggered by the Exit menu item. The window is hidden rather than closed
     * to allow for proper cleanup and potential "are you sure" dialogs in the future.
     */
    @FXML
    private fun actionExit() {
        pnlRoot.scene.window.hide()
    }

    /**
     * Saves the current project to its existing file location.
     *
     * If the project has not been saved before (no file location exists), this delegates to
     * [actionSaveAs] to prompt the user for a file location. Otherwise, it saves directly to
     * the known location without user interaction.
     *
     * This action is typically triggered by the Save menu item or Ctrl+S keyboard shortcut.
     */
    fun actionSave() {
        if (!viewModel.alreadySaved.get()) {
            actionSaveAs()
            return
        }

        viewModel.saveProject()
    }

    /**
     * Prompts the user to select a file location and saves the current project there.
     *
     * Opens a file chooser dialog allowing the user to specify where to save the project.
     * The dialog is filtered to show only AI Ghost Project files (*.aih). If the user selects
     * a location, the project is saved there through the view model.
     *
     * This action is typically triggered by the Save As menu item or when saving a new project
     * for the first time.
     */
    fun actionSaveAs() {
        FileChooser().apply {
            title = "Save project"
            extensionFilters.addAll(
                FileChooser.ExtensionFilter("AI Ghost Project", "*.aih")
            )
        }.showSaveDialog(pnlRoot.scene.window)?.let {
            viewModel.saveProject(it)
        }
    }

    /**
     * Prompts the user to select a project file to open.
     *
     * Opens a file chooser dialog allowing the user to browse for an existing AI Ghost Project
     * file (*.aih). If the user selects a file, it is opened through the view model, replacing the
     * current project in the editor.
     *
     * This action is typically triggered by the Open menu item or Ctrl+O keyboard shortcut.
     */
    fun actionOpen() {
        FileChooser().apply {
            title = "Open project"
            extensionFilters.addAll(
                FileChooser.ExtensionFilter("AI Ghost Project", "*.aih")
            )
        }.showOpenDialog(pnlRoot.scene.window)?.let {
            viewModel.openProject(it)
        }
    }
}

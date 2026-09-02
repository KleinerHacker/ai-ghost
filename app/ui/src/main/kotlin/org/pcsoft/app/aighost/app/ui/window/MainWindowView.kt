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
import javafx.beans.binding.Bindings
import javafx.event.EventHandler
import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.scene.control.Label
import javafx.scene.control.Menu
import javafx.scene.control.MenuItem
import javafx.scene.control.SplitMenuButton
import javafx.scene.control.Tooltip
import javafx.scene.layout.BorderPane
import javafx.scene.layout.VBox
import javafx.stage.FileChooser
import org.pcsoft.app.aighost.app.AiGhostIcons
import org.pcsoft.app.aighost.app.undo.UndoEntry
import org.pcsoft.app.aighost.app.ui.AiGhostDialog
import org.pcsoft.app.aighost.app.ui.component.Editor
import org.pcsoft.app.aighost.app.ui.showingBinding
import java.io.File
import java.net.URL
import java.text.MessageFormat
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
    private lateinit var mnuUndo: MenuItem
    @FXML
    private lateinit var mnuRedo: MenuItem
    @FXML
    private lateinit var btnUndo: SplitMenuButton
    @FXML
    private lateinit var btnRedo: SplitMenuButton
    @FXML
    private lateinit var tltUndo: Tooltip
    @FXML
    private lateinit var tltRedo: Tooltip
    @FXML
    private lateinit var editor: Editor

    @InjectViewModel
    private lateinit var viewModel: MainWindowViewModel

    /** Resource bundle of this view, kept to format the dynamic undo/redo tooltip texts. */
    private lateinit var resources: ResourceBundle

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
        this.resources = requireNotNull(resources) { "MainWindowView requires a resource bundle" }

        // The preferences report nothing, so the view model reads them again every time this
        // component comes on screen.
        showing = pnlRoot.showingBinding()
        showing.addListener { _, _, onScreen -> if (onScreen) viewModel.onShow() else viewModel.onHide() }

        viewModel.openRecent.addListener { _: Observable ->
            mnuOpenRecent.items.setAll(viewModel.openRecent.value.map(::recentFileItem))
        }
        mnuOpenRecent.disableProperty().bind(viewModel.openRecentDisabled)

        mnuUndo.disableProperty().bind(Bindings.not(viewModel.undoStack.canUndoProperty))
        mnuRedo.disableProperty().bind(Bindings.not(viewModel.undoStack.canRedoProperty))
        btnUndo.disableProperty().bind(Bindings.not(viewModel.undoStack.canUndoProperty))
        btnRedo.disableProperty().bind(Bindings.not(viewModel.undoStack.canRedoProperty))

        viewModel.undoStack.undoEntries.addListener { _: Observable -> refreshUndoHistory() }
        viewModel.undoStack.redoEntries.addListener { _: Observable -> refreshRedoHistory() }
        refreshUndoHistory()
        refreshRedoHistory()

        // The editor works on the property model of the window instead of on a project handed to it
        // through a property of its own, so a property never carries another property.
        editor.bindProject(viewModel.project)


    }

    /**
     * Rebuilds the dropdown of [btnUndo] from the current undo history and updates its tooltip to
     * name the change that would be reverted next.
     *
     * Choosing an entry from the dropdown jumps back several steps at once through
     * [org.pcsoft.app.aighost.app.undo.UndoStack.undoUntil].
     */
    private fun refreshUndoHistory() {
        val entries = viewModel.undoStack.undoEntries
        btnUndo.items.setAll(entries.map(::undoHistoryItem))
        tltUndo.text = entries.firstOrNull()?.label
            ?.let { MessageFormat.format(resources.getString("window.main.toolbar.undo.tooltip"), it) }
            ?: resources.getString("window.main.menu.edit.undo")
    }

    /**
     * Rebuilds the dropdown of [btnRedo] from the current redo history and updates its tooltip to
     * name the change that would be applied next.
     *
     * Choosing an entry from the dropdown jumps forward several steps at once through
     * [org.pcsoft.app.aighost.app.undo.UndoStack.redoUntil].
     */
    private fun refreshRedoHistory() {
        val entries = viewModel.undoStack.redoEntries
        btnRedo.items.setAll(entries.map(::redoHistoryItem))
        tltRedo.text = entries.firstOrNull()?.label
            ?.let { MessageFormat.format(resources.getString("window.main.toolbar.redo.tooltip"), it) }
            ?: resources.getString("window.main.menu.edit.redo")
    }

    private fun undoHistoryItem(entry: UndoEntry): MenuItem =
        MenuItem(entry.label).apply { onAction = EventHandler { viewModel.undoStack.undoUntil(entry) } }

    private fun redoHistoryItem(entry: UndoEntry): MenuItem =
        MenuItem(entry.label).apply { onAction = EventHandler { viewModel.undoStack.redoUntil(entry) } }

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
     * Reverts the most recent entry of the undo history.
     *
     * This action is triggered by the Undo menu item and the matching tool bar button; its dropdown
     * jumps back several steps at once instead, through
     * [org.pcsoft.app.aighost.app.undo.UndoStack.undoUntil].
     */
    @FXML
    private fun actionUndo() {
        viewModel.undoStack.undo()
    }

    /**
     * Applies the most recently undone entry again.
     *
     * This action is triggered by the Redo menu item and the matching tool bar button; its dropdown
     * jumps forward several steps at once instead, through
     * [org.pcsoft.app.aighost.app.undo.UndoStack.redoUntil].
     */
    @FXML
    private fun actionRedo() {
        viewModel.undoStack.redo()
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
     * Opens the project settings dialog for the open project.
     *
     * The dialog edits a working copy and writes the changed page geometry back into the design of
     * the open project only when the user closes it with OK or presses APPLY.
     *
     * This action is triggered by the Project Settings menu item and the matching tool bar button.
     */
    @FXML
    private fun actionProjectSettings() {
        AiGhostDialog.showProjectSettings(viewModel.project.designProperty, pnlRoot.scene.window)
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

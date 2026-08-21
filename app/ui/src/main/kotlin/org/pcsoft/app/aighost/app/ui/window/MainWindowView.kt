package org.pcsoft.app.aighost.app.ui.window

import de.saxsys.mvvmfx.FxmlView
import de.saxsys.mvvmfx.InjectViewModel
import javafx.fxml.FXML
import javafx.scene.control.Menu
import javafx.scene.layout.BorderPane
import org.pcsoft.app.aighost.app.AiGhostIcons

/**
 * View of the application main window, holding the menu bar.
 *
 * Texts come from the global resource bundle via the `%key` syntax of the FXML file; the menu icons
 * are attached by the FXML file itself through the `fx:factory` methods of [AiGhostIcons].
 */
class MainWindowView : FxmlView<MainWindowViewModel> {
    @FXML
    private lateinit var pnlRoot: BorderPane
    @FXML
    private lateinit var mnuOpenRecent: Menu

    @InjectViewModel
    private lateinit var viewModel: MainWindowViewModel

    @FXML
    private fun actionHelpOnline() {

    }

    fun actionExit() {
        pnlRoot.scene.window.hide()
    }
}

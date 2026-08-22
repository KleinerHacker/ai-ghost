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

    @InjectViewModel
    private lateinit var viewModel: MainWindowViewModel

    // Kept as a field: the binding is only held by this view, and a garbage collected binding would
    // stop notifying, which would leave the preferences listener registered for good.
    private lateinit var showing: BooleanBinding

    override fun initialize(location: URL?, resources: ResourceBundle?) {
        // The view model follows the preferences exactly while this component is on screen, so it
        // never keeps a listener in the global storage while nobody looks at the window.
        showing = pnlRoot.showingBinding()
        showing.addListener { _, _, onScreen -> if (onScreen) viewModel.onShow() else viewModel.onHide() }

        viewModel.openRecent.addListener { _: Observable ->
            mnuOpenRecent.items.setAll(viewModel.openRecent.value.map {
                MenuItem(it.name)
            })
        }
    }

    @FXML
    private fun actionHelpOnline() {

    }

    @FXML
    private fun actionExit() {
        pnlRoot.scene.window.hide()
    }
}

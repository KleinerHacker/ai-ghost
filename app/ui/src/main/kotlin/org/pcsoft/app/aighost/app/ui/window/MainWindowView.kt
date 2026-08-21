package org.pcsoft.app.aighost.app.ui.window

import de.saxsys.mvvmfx.FxmlView
import de.saxsys.mvvmfx.InjectViewModel
import javafx.fxml.Initializable
import java.net.URL
import java.util.*

class MainWindowView : FxmlView<MainWindowViewModel>, Initializable {
    @InjectViewModel
    private lateinit var viewModel: MainWindowViewModel

    override fun initialize(location: URL?, resources: ResourceBundle?) {

    }
}
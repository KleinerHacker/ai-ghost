package org.pcsoft.app.aighost.app.ui.window

import de.saxsys.mvvmfx.FluentViewLoader
import javafx.scene.Scene
import javafx.stage.Stage
import org.pcsoft.app.aighost.app.AiGhostIcons
import org.pcsoft.app.aighost.app.AiGhostTheme
import org.pcsoft.app.aighost.app.Messages

/**
 * The application main window.
 */
class MainWindow : Stage() {
    init {
        title = Messages["window.main.title"]
        icons.setAll(AiGhostIcons.application)

        FluentViewLoader.fxmlView(MainWindowView::class.java).load().apply {
            scene = Scene(view).also(AiGhostTheme::apply)
        }
    }
}

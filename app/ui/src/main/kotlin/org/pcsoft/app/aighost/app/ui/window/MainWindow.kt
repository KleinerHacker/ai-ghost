package org.pcsoft.app.aighost.app.ui.window

import de.saxsys.mvvmfx.FluentViewLoader
import javafx.scene.Scene
import javafx.stage.Stage

class MainWindow : Stage() {
    init {
        title = "AI Ghost"

        FluentViewLoader.fxmlView(MainWindowView::class.java).load().apply {
            scene = Scene(view)
        }
    }
}
package org.pcsoft.app.aighost.app

import javafx.application.Application
import javafx.stage.Stage
import org.pcsoft.app.aighost.app.ui.window.MainWindow

class AiGhostApplication : Application() {
    override fun start(stage: Stage) {
        MainWindow().show()
    }
}

fun main() {
    Application.launch(AiGhostApplication::class.java)
}

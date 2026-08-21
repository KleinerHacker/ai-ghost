package org.pcsoft.app.aighost.app

import de.saxsys.mvvmfx.MvvmFX
import javafx.application.Application
import javafx.stage.Stage
import org.pcsoft.app.aighost.app.ui.window.MainWindow

/**
 * JavaFX application entry point of AI Ghost.
 */
class AiGhostApplication : Application() {
    override fun start(stage: Stage) {
        AiGhostTheme.install()
        MainWindow().show()
    }
}

/**
 * Starts the application after registering the global resource bundle used for I18N.
 */
fun main() {
    MvvmFX.setGlobalResourceBundle(Messages.bundle)
    Application.launch(AiGhostApplication::class.java)
}

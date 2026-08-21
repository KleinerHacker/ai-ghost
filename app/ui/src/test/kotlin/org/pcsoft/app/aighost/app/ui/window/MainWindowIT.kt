package org.pcsoft.app.aighost.app.ui.window

import de.saxsys.mvvmfx.MvvmFX
import javafx.scene.control.MenuBar
import javafx.stage.Stage
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.pcsoft.app.aighost.app.AiGhostIcons
import org.pcsoft.app.aighost.app.AiGhostTheme
import org.pcsoft.app.aighost.app.Messages
import org.testfx.framework.junit5.ApplicationTest
import java.util.Locale
import java.util.ResourceBundle

/**
 * Integration tests starting the complete main window.
 */
class MainWindowIT : ApplicationTest() {

    private lateinit var window: MainWindow

    override fun start(stage: Stage) {
        MvvmFX.setGlobalResourceBundle(ResourceBundle.getBundle(Messages.BUNDLE_NAME, Locale.GERMAN))

        window = MainWindow()
        window.show()
    }

    /**
     * Use case: starting the application shows a window titled after the product and carrying the
     * application icon in every available size.
     */
    @Test
    fun windowStartsWithTitleAndApplicationIcon() {
        assertEquals("AI Ghost", window.title)
        assertEquals(AiGhostIcons.APPLICATION_ICON_SIZES.size, window.icons.size)
        window.icons.forEach { assertFalse(it.isError) }
    }

    /**
     * Use case: the running window shows the menu bar with its German texts.
     */
    @Test
    fun windowShowsTheGermanMenuBar() {
        val menuBar = window.scene.root.lookup(".menu-bar") as MenuBar

        assertEquals(listOf("Datei", "Veröffentlichen", "Hilfe"), menuBar.menus.map { it.text })
        assertTrue(window.isShowing)
    }

    /**
     * Use case: the main window is decorated with the global application theme, so its controls are
     * rendered in the colours and shapes of the product design.
     */
    @Test
    fun windowUsesTheApplicationTheme() {
        assertEquals(listOf(AiGhostTheme.stylesheet), window.scene.stylesheets.toList())
    }
}

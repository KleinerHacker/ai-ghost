package org.pcsoft.app.aighost.app.ui.window

import de.saxsys.mvvmfx.FluentViewLoader
import de.saxsys.mvvmfx.MvvmFX
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.scene.control.Menu
import javafx.scene.control.MenuBar
import javafx.scene.control.MenuItem
import javafx.scene.control.SeparatorMenuItem
import javafx.stage.Stage
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.pcsoft.app.aighost.app.Messages
import org.testfx.framework.junit5.ApplicationTest
import java.util.Locale
import java.util.ResourceBundle

/**
 * Developer tests for the menu bar of [MainWindowView].
 */
class MainWindowViewTest : ApplicationTest() {

    private lateinit var menuBar: MenuBar

    override fun start(stage: Stage) {
        // No fallback, so the English base bundle is used no matter which locale the build runs under.
        MvvmFX.setGlobalResourceBundle(
            ResourceBundle.getBundle(
                Messages.BUNDLE_NAME,
                Locale.ROOT,
                ResourceBundle.Control.getNoFallbackControl(ResourceBundle.Control.FORMAT_PROPERTIES)
            )
        )

        val view: Parent = FluentViewLoader.fxmlView(MainWindowView::class.java).load().view
        menuBar = view.lookup(".menu-bar") as MenuBar
        stage.scene = Scene(view)
        stage.show()
    }

    private fun allItems(): List<MenuItem> {
        val result = mutableListOf<MenuItem>()

        fun collect(items: List<MenuItem>) {
            items.forEach { item ->
                if (item is SeparatorMenuItem) return@forEach
                result += item
                if (item is Menu) collect(item.items)
            }
        }

        collect(menuBar.menus)
        return result
    }

    /**
     * Use case: the menu bar offers the three top level menus File, Publish and Help.
     */
    @Test
    fun menuBarShowsEveryTopLevelMenu() {
        assertEquals(listOf("File", "Publish", "Help"), menuBar.menus.map { it.text })
    }

    /**
     * Use case: every menu text is resolved through the resource bundle, so no raw `%key`
     * placeholder reaches the user.
     */
    @Test
    fun everyMenuTextIsResolvedFromTheResourceBundle() {
        allItems().forEach { item ->
            assertFalse(
                item.text.isNullOrBlank() || item.text.startsWith("%"),
                "unresolved menu text: ${item.text}"
            )
        }
    }

    /**
     * Use case: the entries the user works with every day carry their usual keyboard shortcut.
     */
    @Test
    fun frequentlyUsedItemsCarryAnAccelerator() {
        val accelerators = allItems()
            .filter { it.accelerator != null }
            .associate { it.text to it.accelerator.name }

        assertEquals("Shortcut+O", accelerators["Open..."])
        assertEquals("Shortcut+S", accelerators["Save"])
        assertEquals("Shift+Shortcut+S", accelerators["Save As..."])
        assertEquals("F1", accelerators["Online Help..."])
        assertEquals("Alt+Shortcut+E", accelerators["Epilog..."])
        assertEquals("Alt+Shortcut+P", accelerators["Prolog..."])
        assertEquals("Alt+Shortcut+C", accelerators["Chapter..."])
        assertEquals("Alt+Shortcut+K", accelerators["Blurb..."])
    }

    /**
     * Use case: every menu entry the specification asks for shows its icon.
     */
    @Test
    fun everyIconCarryingItemShowsItsGraphic() {
        val withGraphic = allItems().filter { it.graphic != null }.map { it.text }

        listOf(
            "Epilog...", "Prolog...", "Chapter...", "Blurb...", "Open...", "Save", "Save As...",
            "Preferences...", "Project Settings...", "Export", "to PDF...", "Online Help..."
        ).forEach { text ->
            assertNotNull(
                withGraphic.firstOrNull { it == text },
                "menu entry $text has no icon"
            )
        }
    }
}

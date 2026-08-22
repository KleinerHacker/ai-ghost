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

import de.saxsys.mvvmfx.MvvmFX
import javafx.scene.control.Menu
import javafx.scene.control.MenuBar
import javafx.stage.Stage
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.pcsoft.app.aighost.app.AiGhostIcons
import org.pcsoft.app.aighost.app.AiGhostTheme
import org.pcsoft.app.aighost.app.Messages
import org.pcsoft.app.aighost.model.PreferencesStorage
import org.pcsoft.app.aighost.model.pref.RecentOpened
import org.testfx.framework.junit5.ApplicationTest
import org.testfx.util.WaitForAsyncUtils
import java.io.File
import java.util.Locale
import java.util.ResourceBundle

/**
 * Integration tests starting the complete main window.
 */
class MainWindowIT : ApplicationTest() {

    private lateinit var window: MainWindow

    // The preferences of the developer running the build must not be touched, so the changes are
    // written to a file of their own. The listeners of the storage are global, so the window is
    // notified all the same.
    private val preferencesFile: File =
        File.createTempFile("ai-ghost-preferences", ".json").apply { deleteOnExit() }

    override fun start(stage: Stage) {
        MvvmFX.setGlobalResourceBundle(ResourceBundle.getBundle(Messages.BUNDLE_NAME, Locale.GERMAN))

        window = MainWindow()
        window.show()
    }

    @AfterEach
    fun releaseWindow() {
        // Hiding deregisters the preferences listener again, so a test never leaves one behind.
        interact { window.hide() }
        preferencesFile.delete()
    }

    /** Texts of the entries the "open recent" menu currently offers. */
    private fun openRecentEntries(): List<String> {
        val menuBar = window.scene.root.lookup(".menu-bar") as MenuBar
        val fileMenu = menuBar.menus.first()
        val openRecent = fileMenu.items
            .filterIsInstance<Menu>()
            .first { it.text == "Zuletzt geöffnete Projekte" }

        return openRecent.items.map { it.text }
    }

    /** Stores [paths] as the recently opened files and waits until the UI processed the change. */
    private fun storeRecentlyOpened(vararg paths: String) {
        PreferencesStorage.update(preferencesFile) {
            it.copy(recentOpened = RecentOpened(entries = paths.toList()))
        }
        WaitForAsyncUtils.waitForFxEvents()
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

    /**
     * Use case: while the window is shown, a project opened elsewhere in the application appears in
     * the "open recent" menu without the window having to be reopened.
     */
    @Test
    fun openRecentFollowsThePreferencesWhileTheWindowIsShown() {
        storeRecentlyOpened("/books/first-novel.ghost")
        assertEquals(listOf("first-novel.ghost"), openRecentEntries())

        storeRecentlyOpened("/books/second-novel.ghost", "/books/first-novel.ghost")
        assertEquals(listOf("second-novel.ghost", "first-novel.ghost"), openRecentEntries())
    }

    /**
     * Use case: a window that is not shown any more stops following the preferences, so a hidden
     * window keeps no listener alive; reopening it picks the changes up again.
     */
    @Test
    fun openRecentIgnoresThePreferencesWhileTheWindowIsHidden() {
        storeRecentlyOpened("/books/first-novel.ghost")
        assertEquals(listOf("first-novel.ghost"), openRecentEntries())

        interact { window.hide() }
        storeRecentlyOpened("/books/second-novel.ghost")
        assertEquals(listOf("first-novel.ghost"), openRecentEntries())

        interact { window.show() }
        storeRecentlyOpened("/books/third-novel.ghost")
        assertEquals(listOf("third-novel.ghost"), openRecentEntries())
    }

    /**
     * Use case: the window follows the preferences per component, not per window - a content that is
     * hidden while the window stays open stops following as well, so no listener is kept alive for a
     * component nobody sees.
     */
    @Test
    fun openRecentIgnoresThePreferencesWhileTheContentIsHidden() {
        storeRecentlyOpened("/books/first-novel.ghost")
        assertEquals(listOf("first-novel.ghost"), openRecentEntries())

        interact { window.scene.root.isVisible = false }
        storeRecentlyOpened("/books/second-novel.ghost")
        assertEquals(listOf("first-novel.ghost"), openRecentEntries())

        interact { window.scene.root.isVisible = true }
        storeRecentlyOpened("/books/third-novel.ghost")
        assertEquals(listOf("third-novel.ghost"), openRecentEntries())
    }
}

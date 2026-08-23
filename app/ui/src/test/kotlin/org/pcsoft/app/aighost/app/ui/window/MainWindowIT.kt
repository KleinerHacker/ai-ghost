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
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.pcsoft.app.aighost.app.AiGhostIcons
import org.pcsoft.app.aighost.app.AiGhostTheme
import org.pcsoft.app.aighost.app.Messages
import org.pcsoft.app.aighost.model.PreferencesStorage
import org.pcsoft.app.aighost.model.pref.RecentOpened
import org.junit.jupiter.api.BeforeAll
import org.testfx.framework.junit5.ApplicationTest
import org.testfx.util.WaitForAsyncUtils
import java.util.Locale
import java.util.ResourceBundle

/**
 * Integration tests starting the complete main window.
 */
class MainWindowIT : ApplicationTest() {

    companion object {
        /**
         * The storage hands out no preferences before they were established, and the window reads
         * them while it is built, so a test starts from the defaults.
         */
        @JvmStatic
        @BeforeAll
        fun establishPreferences() {
            PreferencesStorage.reset()
        }
    }

    private lateinit var window: MainWindow

    // The storage is a singleton holding the preferences of the current user, so the recent files a
    // test sets are put back afterwards. Nothing is written to disk, because only an explicit save
    // touches the file of the developer running the build.
    private var storedRecentOpened: RecentOpened = RecentOpened()

    @BeforeEach
    fun keepRecentOpened() {
        storedRecentOpened = PreferencesStorage.current.recentOpened
    }

    override fun start(stage: Stage) {
        MvvmFX.setGlobalResourceBundle(ResourceBundle.getBundle(Messages.BUNDLE_NAME, Locale.GERMAN))

        window = MainWindow()
        window.show()
    }

    @AfterEach
    fun releaseWindow() {
        interact { window.hide() }

        PreferencesStorage.current.recentOpened = storedRecentOpened
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

    /** Stores [paths] as the recently opened files of the current user. */
    private fun storeRecentlyOpened(vararg paths: String) {
        PreferencesStorage.current.recentOpened = RecentOpened(entries = paths.toList())
    }

    /** Takes the window off screen and back on, so its content reads the preferences again. */
    private fun showAgain() {
        interact { window.hide() }
        interact { window.show() }
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
        assertEquals(AiGhostTheme.stylesheets, window.scene.stylesheets.toList())
    }

    /**
     * Use case: the user opened projects earlier, so the "open recent" menu offers them in the
     * stored order as soon as the window is on screen.
     */
    @Test
    fun openRecentShowsTheStoredFilesWhenTheWindowIsShown() {
        storeRecentlyOpened("/books/second-novel.ghost", "/books/first-novel.ghost")

        showAgain()

        assertEquals(listOf("second-novel.ghost", "first-novel.ghost"), openRecentEntries())
    }

    /**
     * Use case: a project was opened elsewhere in the application while the window was away, so
     * bringing the window back on screen offers the new entry.
     */
    @Test
    fun openRecentIsRefreshedWhenTheWindowIsShownAgain() {
        storeRecentlyOpened("/books/first-novel.ghost")
        showAgain()
        assertEquals(listOf("first-novel.ghost"), openRecentEntries())

        storeRecentlyOpened("/books/second-novel.ghost")
        assertEquals(listOf("first-novel.ghost"), openRecentEntries())

        showAgain()
        assertEquals(listOf("second-novel.ghost"), openRecentEntries())
    }

    /**
     * Use case: the menu is filled per component, not per window - a content that was hidden while
     * the window stayed open reads the preferences again as soon as it is visible.
     */
    @Test
    fun openRecentIsRefreshedWhenTheContentBecomesVisibleAgain() {
        storeRecentlyOpened("/books/first-novel.ghost")
        showAgain()
        assertEquals(listOf("first-novel.ghost"), openRecentEntries())

        interact { window.scene.root.isVisible = false }
        storeRecentlyOpened("/books/second-novel.ghost")
        assertEquals(listOf("first-novel.ghost"), openRecentEntries())

        interact { window.scene.root.isVisible = true }
        WaitForAsyncUtils.waitForFxEvents()
        assertEquals(listOf("second-novel.ghost"), openRecentEntries())
    }
}

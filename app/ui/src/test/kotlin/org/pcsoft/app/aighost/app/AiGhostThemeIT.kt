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

package org.pcsoft.app.aighost.app

import javafx.css.CssParser
import javafx.scene.Scene
import javafx.scene.control.Button
import javafx.scene.control.ContextMenu
import javafx.scene.control.Label
import javafx.scene.control.Menu
import javafx.scene.control.MenuBar
import javafx.scene.control.MenuButton
import javafx.scene.control.MenuItem
import javafx.scene.control.ScrollPane
import javafx.scene.control.Separator
import javafx.scene.control.SeparatorMenuItem
import javafx.scene.control.SplitMenuButton
import javafx.scene.control.TextArea
import javafx.scene.control.TextField
import javafx.scene.control.ToggleButton
import javafx.scene.control.ToolBar
import javafx.scene.control.Tooltip
import javafx.scene.layout.VBox
import javafx.stage.Stage
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.testfx.framework.junit5.ApplicationTest
import java.net.URI
import java.util.logging.Handler
import java.util.logging.Level
import java.util.logging.LogRecord
import java.util.logging.Logger

/**
 * Integration tests loading and applying the complete application theme.
 *
 * JavaFX never fails hard on a broken stylesheet - it logs the problem and keeps the default value.
 * These tests therefore listen to the log while every styled control is rendered, so a rule the
 * platform cannot apply breaks the build instead of silently degrading the appearance.
 */
class AiGhostThemeIT : ApplicationTest() {

    private val records = mutableListOf<LogRecord>()

    private lateinit var handler: Handler
    private lateinit var stage: Stage
    private lateinit var contextMenu: ContextMenu
    private lateinit var button: Button

    override fun start(stage: Stage) {
        this.stage = stage

        handler = object : Handler() {
            override fun publish(record: LogRecord) {
                synchronized(records) { records += record }
            }

            override fun flush() = Unit

            override fun close() = Unit
        }
        handler.level = Level.ALL
        Logger.getLogger("").apply {
            level = Level.ALL
            addHandler(handler)
        }

        button = Button("Button").apply { tooltip = Tooltip("Tooltip") }
        contextMenu = ContextMenu(MenuItem("Entry"), SeparatorMenuItem(), MenuItem("Other entry"))

        val menuBar = MenuBar(
            Menu("Menu", null, MenuItem("Entry"), SeparatorMenuItem(), Menu("Sub", null, MenuItem("Deep")))
        )
        val toolBar = ToolBar(
            button,
            ToggleButton("Toggle"),
            MenuButton("Menu button", null, MenuItem("Entry")),
            SplitMenuButton(MenuItem("Entry")),
            Separator()
        )
        val root = VBox(
            menuBar,
            toolBar,
            Label("Label"),
            TextField("Text"),
            TextArea("Text"),
            ScrollPane(Label("Content"))
        )

        stage.scene = Scene(root, 400.0, 300.0).also(AiGhostTheme::apply)
        stage.show()
    }

    override fun stop() {
        Logger.getLogger("").removeHandler(handler)
    }

    /**
     * Use case: the shipped stylesheet is syntactically valid, so the whole file is understood by
     * the JavaFX CSS parser instead of being dropped rule by rule.
     */
    @Test
    fun stylesheetParsesIntoRules() {
        val stylesheet = CssParser().parse(URI(AiGhostTheme.stylesheet).toURL())

        assertTrue(stylesheet.rules.isNotEmpty(), "Stylesheet contains no rules")
    }

    /**
     * Use case: applying the theme to every styled control - including the popup controls, which are
     * styled only once they are shown - produces no warning, so no declaration silently falls back
     * to the platform default.
     */
    @Test
    fun applyingTheThemeLogsNoCssProblem() {
        interact {
            contextMenu.show(stage)
            button.tooltip.show(stage)
            stage.scene.root.applyCss()
        }
        interact {
            button.tooltip.hide()
            contextMenu.hide()
        }

        val problems = synchronized(records) {
            records
                .filter { it.level.intValue() >= Level.WARNING.intValue() }
                .map { "${it.loggerName}: ${it.message}" }
                .filter { it.contains("ai-ghost.css") || it.contains("-fx-") }
        }

        assertTrue(problems.isEmpty(), "CSS problems reported: $problems")
    }
}

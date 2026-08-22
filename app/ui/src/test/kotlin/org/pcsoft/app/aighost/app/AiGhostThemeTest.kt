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

import javafx.application.Application
import javafx.scene.Scene
import javafx.scene.layout.StackPane
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.testfx.framework.junit5.ApplicationExtension

/**
 * Developer tests for [AiGhostTheme].
 */
@ExtendWith(ApplicationExtension::class)
class AiGhostThemeTest {

    /**
     * Use case: the global stylesheet is shipped as a resource and can be resolved to a URL, so the
     * application never starts with a missing appearance.
     */
    @Test
    fun stylesheetResourceExists() {
        assertNotNull(AiGhostTheme::class.java.getResource(AiGhostTheme.STYLESHEET_PATH))
        assertTrue(AiGhostTheme.stylesheet.endsWith("ai-ghost.css"))
    }

    /**
     * Use case: the stylesheet content defines the palette shared with the MkDocs theme and the
     * logo, so the design stays consistent across application and documentation.
     */
    @Test
    fun stylesheetDefinesSharedPalette() {
        val css = AiGhostTheme::class.java.getResourceAsStream(AiGhostTheme.STYLESHEET_PATH)!!
            .use { it.reader().readText() }

        listOf("#3f51b5", "#7986cb", "#1e2a4a", "#e5eaf6", "#8fd8f5").forEach { colour ->
            assertTrue(css.contains(colour), "Missing palette colour: $colour")
        }
    }

    /**
     * Use case: size based properties never reference a looked-up variable, because JavaFX resolves
     * those to plain numbers and logs a conversion warning instead of applying the value.
     */
    @Test
    fun sizeBasedPropertiesUseLiteralValues() {
        val css = AiGhostTheme::class.java.getResourceAsStream(AiGhostTheme.STYLESHEET_PATH)!!
            .use { it.reader().readText() }

        val offenders = Regex("-fx-[a-z-]*(radius|width|padding|spacing|size)\\s*:\\s*[^;]*")
            .findAll(css)
            .map { it.value }
            .filter { it.contains("-ghost-") }
            .toList()

        assertTrue(offenders.isEmpty(), "Looked-up variables used for sizes: $offenders")
    }

    /**
     * Use case: no state selector changes the geometry of a control, so a control never jumps when
     * it gains focus, hover or the pressed state.
     */
    @Test
    fun stateSelectorsDoNotChangeGeometry() {
        val css = AiGhostTheme::class.java.getResourceAsStream(AiGhostTheme.STYLESHEET_PATH)!!
            .use { it.reader().readText() }

        val offenders = Regex("([^{}]*):(focused|hover|armed|showing|selected)[^{}]*\\{([^}]*)}")
            .findAll(css)
            .filter { match ->
                match.groupValues[3].lines().any { line ->
                    Regex("-fx-(border-width|padding|border-insets|background-insets)\\s*:").containsMatchIn(line)
                }
            }
            .map { it.groupValues[1].trim() + ":" + it.groupValues[2] }
            .toList()

        assertTrue(offenders.isEmpty(), "State selectors changing geometry: $offenders")
    }

    /**
     * Use case: applying the theme decorates a scene with the stylesheet, and repeating the call
     * does not add it a second time.
     */
    @Test
    fun applyAddsStylesheetExactlyOnce() {
        val scene = Scene(StackPane())

        AiGhostTheme.apply(scene)
        AiGhostTheme.apply(scene)

        assertEquals(listOf(AiGhostTheme.stylesheet), scene.stylesheets.toList())
    }

    /**
     * Use case: installing the theme selects Modena as the platform base look the stylesheet builds
     * upon.
     */
    @Test
    fun installSelectsModenaAsBaseLook() {
        AiGhostTheme.install()

        assertEquals(Application.STYLESHEET_MODENA, Application.getUserAgentStylesheet())
    }
}

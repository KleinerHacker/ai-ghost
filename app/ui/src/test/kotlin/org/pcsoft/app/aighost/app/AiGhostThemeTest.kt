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
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.pcsoft.app.aighost.model.pref.ThemeMode
import org.testfx.framework.junit5.ApplicationExtension

/**
 * Developer tests for [AiGhostTheme] and [AiGhostColorScheme].
 */
@ExtendWith(ApplicationExtension::class)
class AiGhostThemeTest {

    private val originalColorScheme = AiGhostTheme.colorScheme

    /** Restores the colour scheme, so a test selecting one does not influence the following ones. */
    @AfterEach
    fun restoreColorScheme() {
        AiGhostTheme.colorScheme = originalColorScheme
    }

    private fun read(path: String): String =
        AiGhostTheme::class.java.getResourceAsStream(path)!!.use { it.reader().readText() }

    private fun allPaths(): List<String> =
        AiGhostColorScheme.entries.map { it.path } + AiGhostTheme.BASE_PATH + AiGhostTheme.COMPONENT_PATHS

    private fun variablesOf(css: String): Set<String> =
        Regex("(-ghost-[a-z-]+)\\s*:").findAll(css).map { it.groupValues[1] }.toSet()

    /**
     * Use case: every stylesheet of the theme - colour schemes, base and components - is shipped as
     * a resource and can be resolved to a URL, so the application never starts with a missing
     * appearance.
     */
    @Test
    fun allStylesheetResourcesExist() {
        allPaths().forEach { path ->
            assertNotNull(AiGhostTheme::class.java.getResource(path), "Missing stylesheet: $path")
        }
    }

    /**
     * Use case: the applied stylesheets are the colour scheme first, then the base and then every
     * component, so a component rule can override the base and both read the palette of the scheme.
     */
    @Test
    fun stylesheetsStartWithTheColorScheme() {
        AiGhostTheme.colorScheme = AiGhostColorScheme.DARK

        val stylesheets = AiGhostTheme.stylesheets

        assertEquals(2 + AiGhostTheme.COMPONENT_PATHS.size, stylesheets.size)
        assertTrue(stylesheets.first().endsWith("dark.css"), "First entry is not the colour scheme")
        assertTrue(stylesheets[1].endsWith("base.css"), "Second entry is not the base stylesheet")
    }

    /**
     * Use case: the light scheme defines the palette shared with the MkDocs theme and the logo, and
     * the dark scheme the one of the MkDocs "slate" scheme, so the design stays consistent across
     * application and documentation in both appearances.
     */
    @Test
    fun colorSchemesDefineTheSharedPalette() {
        val light = read(AiGhostColorScheme.LIGHT.path)
        val dark = read(AiGhostColorScheme.DARK.path)

        listOf("#3f51b5", "#7986cb", "#1e2a4a", "#e5eaf6", "#8fd8f5").forEach { colour ->
            assertTrue(light.contains(colour), "Missing light palette colour: $colour")
        }
        listOf("#7986cb", "#9fa8da", "#5c6bc0").forEach { colour ->
            assertTrue(dark.contains(colour), "Missing dark palette colour: $colour")
        }
    }

    /**
     * Use case: every colour scheme defines exactly the same variables, so exchanging the scheme
     * never leaves a rule of the theme without a value.
     */
    @Test
    fun everyColorSchemeDefinesTheSameVariables() {
        val reference = variablesOf(read(AiGhostColorScheme.LIGHT.path))

        assertTrue(reference.isNotEmpty(), "Light scheme defines no palette variable")
        AiGhostColorScheme.entries.forEach { scheme ->
            assertEquals(reference, variablesOf(read(scheme.path)), "Deviating variables in $scheme")
        }
    }

    /**
     * Use case: only a colour scheme carries colour literals, so the whole appearance really is
     * exchanged with the scheme and no component keeps a colour of its own.
     */
    @Test
    fun onlyColorSchemesCarryColourLiterals() {
        val literal = Regex("#[0-9a-fA-F]{3,8}|rgba?\\(")

        (listOf(AiGhostTheme.BASE_PATH) + AiGhostTheme.COMPONENT_PATHS).forEach { path ->
            val offenders = literal.findAll(read(path)).map { it.value }.toList()
            assertTrue(offenders.isEmpty(), "Colour literals in $path: $offenders")
        }
    }

    /**
     * Use case: size based properties never reference a looked-up variable, because JavaFX resolves
     * those to plain numbers and logs a conversion warning instead of applying the value.
     */
    @Test
    fun sizeBasedPropertiesUseLiteralValues() {
        allPaths().forEach { path ->
            val offenders = Regex("-fx-[a-z-]*(radius|width|padding|spacing|size)\\s*:\\s*[^;]*")
                .findAll(read(path))
                .map { it.value }
                .filter { it.contains("-ghost-") }
                .toList()

            assertTrue(offenders.isEmpty(), "Looked-up variables used for sizes in $path: $offenders")
        }
    }

    /**
     * Use case: no state selector changes the geometry of a control, so a control never jumps when
     * it gains focus, hover or the pressed state.
     */
    @Test
    fun stateSelectorsDoNotChangeGeometry() {
        allPaths().forEach { path ->
            val offenders = Regex("([^{}]*):(focused|hover|armed|showing|selected)[^{}]*\\{([^}]*)}")
                .findAll(read(path))
                .filter { match ->
                    match.groupValues[3].lines().any { line ->
                        Regex("-fx-(border-width|padding|border-insets|background-insets)\\s*:").containsMatchIn(line)
                    }
                }
                .map { it.groupValues[1].trim() + ":" + it.groupValues[2] }
                .toList()

            assertTrue(offenders.isEmpty(), "State selectors changing geometry in $path: $offenders")
        }
    }

    /**
     * Use case: applying the theme decorates a scene with every stylesheet, and repeating the call
     * does not add any of them a second time.
     */
    @Test
    fun applyAddsStylesheetsExactlyOnce() {
        val scene = Scene(StackPane())

        AiGhostTheme.apply(scene)
        AiGhostTheme.apply(scene)

        assertEquals(AiGhostTheme.stylesheets, scene.stylesheets.toList())
    }

    /**
     * Use case: the appearance selected in the preferences is mapped onto the matching colour
     * scheme, so a user asking for a dark application gets the dark palette.
     */
    @Test
    fun resolveMapsTheSelectedAppearance() {
        assertEquals(AiGhostColorScheme.LIGHT, AiGhostTheme.resolve(ThemeMode.LIGHT))
        assertEquals(AiGhostColorScheme.DARK, AiGhostTheme.resolve(ThemeMode.DARK))
    }

    /**
     * Use case: following the operating system always yields a usable scheme - the reported one, or
     * the light scheme when the platform reports nothing at all.
     */
    @Test
    fun resolveFollowsTheOperatingSystemForSystemMode() {
        assertTrue(AiGhostTheme.resolve(ThemeMode.SYSTEM) in AiGhostColorScheme.entries)
    }

    /**
     * Use case: installing the theme selects Modena as the platform base look the theme builds upon
     * and picks the colour scheme of the user, so the very first scene is already dressed correctly.
     */
    @Test
    fun installSelectsModenaAsBaseLookAndPicksTheColorScheme() {
        AiGhostTheme.install()

        assertEquals(Application.STYLESHEET_MODENA, Application.getUserAgentStylesheet())
        assertTrue(AiGhostTheme.colorScheme in AiGhostColorScheme.entries)
    }
}

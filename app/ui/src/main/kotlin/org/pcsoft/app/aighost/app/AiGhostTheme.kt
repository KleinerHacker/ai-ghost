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
import javafx.application.ColorScheme
import javafx.application.Platform
import javafx.scene.Scene
import org.pcsoft.app.aighost.model.PreferencesStorage
import org.pcsoft.app.aighost.model.pref.ThemeMode

/**
 * Central access point for the visual appearance of the application.
 *
 * The theme is split into three layers, which are applied to a scene in exactly this order:
 *
 * 1. the colour scheme ([AiGhostColorScheme]), the only stylesheet carrying colour literals,
 * 2. [BASE_PATH], the type face and the window surface,
 * 3. [COMPONENT_PATHS], one stylesheet per styled control.
 *
 * Every resource path lives here exactly once, the same way [AiGhostIcons] owns the icon file names,
 * so no other place in the code base names a stylesheet.
 *
 * [install] prepares the platform default and selects the colour scheme, every scene of the
 * application is then decorated through [apply].
 */
object AiGhostTheme {

    /** Resource path of the base stylesheet, which is independent of the colour scheme. */
    const val BASE_PATH: String = "/styles/base.css"

    /**
     * Resource paths of the component stylesheets, one per styled control.
     *
     * Styling another control means adding a stylesheet next to the existing ones and listing it
     * here; the order inside the list does not matter, because the selectors do not overlap.
     */
    val COMPONENT_PATHS: List<String> = listOf(
        "/styles/component/menu-bar.css",
        "/styles/component/context-menu.css",
        "/styles/component/tool-bar.css",
        "/styles/component/status-bar.css",
        "/styles/component/button.css",
        "/styles/component/tooltip.css",
        "/styles/component/text-input.css",
        "/styles/component/tree-view.css",
        "/styles/component/scroll-bar.css",
        "/styles/component/tab-pane.css",
        "/styles/component/split-pane.css",
        "/styles/component/editor.css",
        "/styles/component/prompt-area.css",
        "/styles/component/text-field.css",
        "/styles/component/dialog.css"
    )

    /**
     * Colour scheme the theme is dressed in.
     *
     * The value is taken from the preferences of the user by [install] and is read exactly once at
     * start up, so a scheme change in the preferences takes effect after a restart of the
     * application only. Scenes decorated before a change keep the scheme they were decorated with.
     */
    var colorScheme: AiGhostColorScheme = AiGhostColorScheme.LIGHT

    /** External forms of the stylesheets of the theme, in the order they have to be applied. */
    val stylesheets: List<String>
        get() = (listOf(colorScheme.path) + BASE_PATH + COMPONENT_PATHS).map(::externalForm)

    /**
     * Installs the base look the application theme builds upon.
     *
     * Modena is set explicitly so that the appearance does not change with the platform default; the
     * theme itself only overrides what it needs on top of it. The shipped type faces are registered
     * in the same step, so [AiGhostFonts.FAMILY] is available wherever a font family is expected,
     * and the colour scheme the user selected is resolved into [colorScheme].
     */
    fun install() {
        Application.setUserAgentStylesheet(Application.STYLESHEET_MODENA)
        AiGhostFonts.install()
        colorScheme = resolve(PreferencesStorage.current.themeMode)
    }

    /**
     * Resolves the appearance the user selected into a colour scheme of the theme.
     *
     * [ThemeMode.SYSTEM] follows the operating system, which JavaFX reports through
     * [Platform.getPreferences]. Should the platform not report a colour scheme at all - or should
     * the toolkit not be running yet - the light scheme is used, the same default the preferences
     * themselves carry.
     *
     * @param themeMode appearance the user selected
     * @return colour scheme to dress the theme in
     */
    fun resolve(themeMode: ThemeMode): AiGhostColorScheme = when (themeMode) {
        ThemeMode.LIGHT -> AiGhostColorScheme.LIGHT
        ThemeMode.DARK -> AiGhostColorScheme.DARK
        ThemeMode.SYSTEM -> systemColorScheme()
    }

    /**
     * Adds the stylesheets of the theme to the given scene.
     *
     * Calling this more than once for the same scene is safe - a stylesheet is added only if it is
     * not present yet.
     *
     * @param scene the scene to decorate
     */
    fun apply(scene: Scene) {
        stylesheets.forEach { stylesheet ->
            if (stylesheet !in scene.stylesheets) {
                scene.stylesheets += stylesheet
            }
        }
    }

    /**
     * Reads the colour scheme of the operating system, falling back to [AiGhostColorScheme.LIGHT].
     *
     * [Platform.getPreferences] needs a running toolkit and is therefore guarded: outside a JavaFX
     * application the call fails, which is a missing answer here, not an error.
     */
    private fun systemColorScheme(): AiGhostColorScheme =
        runCatching { Platform.getPreferences().colorScheme }
            .getOrNull()
            .let { if (it == ColorScheme.DARK) AiGhostColorScheme.DARK else AiGhostColorScheme.LIGHT }

    /**
     * Resolves a resource path into the external form JavaFX expects in `Scene.getStylesheets()`.
     *
     * @param path resource path of a stylesheet of the theme
     * @return external form of the resource
     * @throws IllegalArgumentException if the stylesheet is not shipped with the application
     */
    private fun externalForm(path: String): String {
        val url = requireNotNull(AiGhostTheme::class.java.getResource(path)) {
            "Stylesheet resource not found: $path"
        }
        return url.toExternalForm()
    }
}

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

/**
 * Central access point for the visual appearance of the application.
 *
 * The theme is a single global stylesheet whose palette and shapes follow the project logo and the
 * MkDocs theme. The resource path lives here exactly once, the same way [AiGhostIcons] owns the icon
 * file names, so no other place in the code base names the stylesheet.
 *
 * [install] prepares the platform default, every scene of the application is then decorated through
 * [apply].
 */
object AiGhostTheme {

    /** Resource path of the global application stylesheet. */
    const val STYLESHEET_PATH: String = "/styles/ai-ghost.css"

    /** External form of [STYLESHEET_PATH], the value JavaFX expects in `Scene.getStylesheets()`. */
    val stylesheet: String by lazy {
        val url = requireNotNull(AiGhostTheme::class.java.getResource(STYLESHEET_PATH)) {
            "Stylesheet resource not found: $STYLESHEET_PATH"
        }
        url.toExternalForm()
    }

    /**
     * Installs the base look the application theme builds upon.
     *
     * Modena is set explicitly so that the appearance does not change with the platform default; the
     * theme itself only overrides what it needs on top of it. The shipped type faces are registered
     * in the same step, so [AiGhostFonts.FAMILY] is available wherever a font family is expected.
     */
    fun install() {
        Application.setUserAgentStylesheet(Application.STYLESHEET_MODENA)
        AiGhostFonts.install()
    }

    /**
     * Adds the application stylesheet to the given scene.
     *
     * Calling this more than once for the same scene is safe - the stylesheet is added only if it is
     * not present yet.
     *
     * @param scene the scene to decorate
     */
    fun apply(scene: Scene) {
        if (stylesheet !in scene.stylesheets) {
            scene.stylesheets += stylesheet
        }
    }
}

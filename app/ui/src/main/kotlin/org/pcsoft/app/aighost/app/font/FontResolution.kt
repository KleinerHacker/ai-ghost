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

package org.pcsoft.app.aighost.app.font

import javafx.scene.text.Font

/**
 * Outcome of turning the font of a design into a font JavaFX can draw with.
 *
 * A missing family is a state of its own rather than a silent substitution, so the user can be told
 * that the manuscript is not shown in the font it was written in.
 *
 * @property font Font to draw and measure with, in every case.
 */
sealed interface FontResolution {

    val font: Font

    /**
     * The family the design asks for is installed and is what gets drawn.
     *
     * @property font Font of the requested family, weight and slant.
     */
    data class Installed(override val font: Font) : FontResolution

    /**
     * The family the design asks for is not installed; the fallback chain picked a substitute.
     *
     * @property requestedFamily Family the design asks for.
     * @property substituteFamily Family that is drawn instead.
     * @property font Font of the substitute family, carrying the requested weight, slant and size.
     */
    data class NotInstalled(
        val requestedFamily: String,
        val substituteFamily: String,
        override val font: Font
    ) : FontResolution
}

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

package org.pcsoft.app.aighost.layouting.fx.font

import javafx.scene.text.Font
import javafx.scene.text.FontPosture
import javafx.scene.text.FontWeight

/**
 * Turns a [FontDescription] into the [Font] JavaFX draws and measures with.
 *
 * The resolution is deterministic: the same [FontDescription] on the same machine always yields the
 * same font, and a family that is not installed always falls back along the same chain, so a
 * manuscript does not paginate differently from one run to the next.
 *
 * No font file is read - the families come from [FontCatalog], which asks JavaFX itself.
 */
object FontResolver {

    /**
     * Families a missing family falls back to, in this order.
     *
     * The chain covers the common sans faces of the supported platforms; when none of them is
     * installed the default font of JavaFX is used, which always exists.
     */
    val FALLBACK_FAMILIES: List<String> = listOf(
        "Arial",
        "Helvetica",
        "Segoe UI",
        "Liberation Sans",
        "DejaVu Sans",
        "SansSerif"
    )

    /**
     * Resolves [description] and reports whether the requested family was available.
     *
     * @param description Face to resolve, as the caller stores it.
     * @return [FontResolution.Installed] for an installed family, otherwise
     *   [FontResolution.NotInstalled] naming the substitute in use.
     */
    fun resolve(description: FontDescription): FontResolution {
        if (FontCatalog.contains(description.family)) {
            return FontResolution.Installed(create(description.family, description))
        }

        val substitute = FALLBACK_FAMILIES.firstOrNull { FontCatalog.contains(it) }
            ?: Font.getDefault().family
        return FontResolution.NotInstalled(description.family, substitute, create(substitute, description))
    }

    /**
     * Resolves [description] to the font to draw with, without reporting a substitution.
     *
     * @param description Face to resolve, as the caller stores it.
     */
    fun font(description: FontDescription): Font = resolve(description).font

    private fun create(family: String, description: FontDescription): Font =
        Font.font(
            family,
            if (description.bold) FontWeight.BOLD else FontWeight.NORMAL,
            if (description.italic) FontPosture.ITALIC else FontPosture.REGULAR,
            description.size.toDouble()
        )
}

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

/**
 * Catalogue of the font families this machine offers.
 *
 * The list is taken from [Font.getFamilies] and therefore contains exactly what JavaFX is able to
 * draw with - no font file is opened, parsed or shipped for it. Families JavaFX reports but cannot
 * actually render text with are dropped, so every entry of [families] is a family a design may be
 * built on.
 *
 * The catalogue is built once and kept in memory, because asking the font system is far too
 * expensive to repeat for every drop down and every layout run. [rebuild] is the only way to pick up
 * a font that was installed while the application was running; it also clears the measurement cache
 * of [JavaFxTextMetrics], whose numbers were taken with the previous set of families.
 */
object FontCatalog {

    /** Size the probe font is created with; the family of a font does not depend on its size. */
    private const val PROBE_SIZE: Double = 12.0

    @Volatile
    private var cached: List<String>? = null

    /**
     * Families that are installed and usable, sorted by name.
     *
     * The first call builds the catalogue, every later call answers from memory.
     */
    val families: List<String>
        get() = cached ?: build().also { cached = it }

    /**
     * Builds the catalogue again and returns the new list.
     *
     * The measurement cache of [JavaFxTextMetrics] is cleared as well, since a changed set of
     * families can change what a measurement resolves to.
     */
    fun rebuild(): List<String> {
        val families = build()
        cached = families
        JavaFxTextMetrics.clearCache()
        return families
    }

    /**
     * Answers whether [family] is installed on this machine, ignoring the case of the name.
     *
     * @param family Family name as a design stores it.
     */
    fun contains(family: String): Boolean =
        families.any { it.equals(family, ignoreCase = true) }

    private fun build(): List<String> =
        Font.getFamilies()
            .filter { isUsable(it) }
            .distinct()
            .sorted()

    /**
     * A family is usable when JavaFX hands back a font of that very family instead of quietly
     * substituting another one.
     */
    private fun isUsable(family: String): Boolean {
        if (family.isBlank()) {
            return false
        }

        val font = Font.font(family, PROBE_SIZE) ?: return false
        return font.family.equals(family, ignoreCase = true)
    }
}

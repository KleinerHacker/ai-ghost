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
import javafx.stage.Stage
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.testfx.framework.junit5.ApplicationTest
import org.testfx.util.WaitForAsyncUtils

/**
 * Developer tests for the catalogue of the installed font families, [FontCatalog].
 *
 * The catalogue asks the JavaFX font system, which needs a started toolkit, so the tests run
 * headless on the JavaFX application thread.
 */
class FontCatalogTest : ApplicationTest() {

    override fun start(stage: Stage) = Unit

    /** Runs [block] on the JavaFX application thread and hands back its result. */
    private fun <T> fx(block: () -> T): T =
        WaitForAsyncUtils.asyncFx<T> { block() }.get()

    /**
     * Use case: the application asks which fonts this machine offers and gets a non empty list whose
     * entries are sorted by name, so a font drop down can show them as they are.
     */
    @Test
    fun catalogueListsTheInstalledFamiliesSorted() {
        val families = fx { FontCatalog.families }

        assertTrue(families.isNotEmpty(), "no font family reported by JavaFX")
        assertEquals(families.sorted(), families)
        assertEquals(families.distinct(), families)
    }

    /**
     * Use case: every family of the catalogue can really be drawn with - a name JavaFX reports but
     * silently substitutes another face for would make a design lie about its font.
     */
    @Test
    fun catalogueDropsFamiliesWithoutUsableRendering() {
        val families = fx { FontCatalog.families }

        fx {
            families.forEach { family ->
                assertFalse(family.isBlank(), "blank family in the catalogue")
                assertEquals(
                    family.lowercase(),
                    Font.font(family, 12.0).family.lowercase(),
                    "family '$family' is not rendered with itself"
                )
            }
        }
    }

    /**
     * Use case: the catalogue is asked over and over while the user works; it is built once and every
     * later call answers from memory instead of the font system.
     */
    @Test
    fun catalogueIsBuiltOnceAndKeptInMemory() {
        val first = fx { FontCatalog.families }
        val second = fx { FontCatalog.families }

        assertSame(first, second)
    }

    /**
     * Use case: a font was installed while the application was running, so the catalogue is built
     * again on purpose - it hands back a fresh list of the same content and drops the measurements
     * that were taken against the previous set of families.
     */
    @Test
    fun rebuildBuildsTheCatalogueAgainAndClearsTheMeasurementCache() {
        val before = fx { FontCatalog.families }
        fx { JavaFxTextMetrics.wordWidth(description(before.first()), "Manuscript") }
        assertTrue(fx { JavaFxTextMetrics.cacheSize } > 0)

        val rebuilt = fx { FontCatalog.rebuild() }

        assertEquals(before, rebuilt)
        assertEquals(0, fx { JavaFxTextMetrics.cacheSize })
        assertSame(rebuilt, fx { FontCatalog.families })
    }

    /**
     * Use case: a design names a family; the catalogue answers whether it exists, no matter how the
     * name is capitalised, and says no for a family nobody installed.
     */
    @Test
    fun containsAnswersForInstalledAndMissingFamilies() {
        val installed = fx { FontCatalog.families }.first()

        assertTrue(fx { FontCatalog.contains(installed) })
        assertTrue(fx { FontCatalog.contains(installed.uppercase()) })
        assertTrue(fx { FontCatalog.contains(installed.lowercase()) })
        assertFalse(fx { FontCatalog.contains(MISSING_FAMILY) })
    }

    private fun description(family: String) =
        FontDescription(family = family, size = 12)

    private companion object {

        /** A family name no machine carries, used to force the missing state. */
        const val MISSING_FAMILY: String = "No Such Family 4711"
    }
}

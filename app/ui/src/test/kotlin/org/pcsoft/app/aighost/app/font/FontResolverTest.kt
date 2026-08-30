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
import javafx.scene.text.FontPosture
import javafx.scene.text.FontWeight
import javafx.stage.Stage
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.pcsoft.app.aighost.model.common.FontData
import org.testfx.framework.junit5.ApplicationTest
import org.testfx.util.WaitForAsyncUtils

/**
 * Developer tests for the resolution of a design font onto a JavaFX font, [FontResolver].
 */
class FontResolverTest : ApplicationTest() {

    override fun start(stage: Stage) = Unit

    /** Runs [block] on the JavaFX application thread and hands back its result. */
    private fun <T> fx(block: () -> T): T =
        WaitForAsyncUtils.asyncFx<T> { block() }.get()

    private fun installedFamily(): String = fx { FontCatalog.families }.first()

    /**
     * Use case: the family of the design is installed, so the text is drawn in exactly that family,
     * at exactly the size the design asks for.
     */
    @Test
    fun installedFamilyIsResolvedToItself() {
        val family = installedFamily()

        val resolution = fx { FontResolver.resolve(FontData(name = family, size = 14)) }

        assertTrue(resolution is FontResolution.Installed, "expected an installed resolution")
        assertEquals(family.lowercase(), resolution.font.family.lowercase())
        assertEquals(14.0, resolution.font.size)
    }

    /**
     * Use case: the design asks for bold and slanted text, so the resolved font carries the bold
     * weight and the italic posture instead of the regular face.
     */
    @Test
    fun weightAndSlantOfTheDesignAreApplied() {
        val family = installedFamily()
        val data = FontData(name = family, size = 14, bold = true, italic = true)

        val resolved = fx { FontResolver.font(data) }

        val expected = fx {
            Font.font(family, FontWeight.BOLD, FontPosture.ITALIC, 14.0)
        }
        assertEquals(expected, resolved)
    }

    /**
     * Use case: the manuscript was written on another machine with a family that is missing here -
     * the resolution says so, names the substitute and still hands back a usable font.
     */
    @Test
    fun missingFamilyIsReportedAsItsOwnState() {
        val data = FontData(name = MISSING_FAMILY, size = 18, bold = true)

        val resolution = fx { FontResolver.resolve(data) }

        assertTrue(resolution is FontResolution.NotInstalled, "expected a missing family")
        val missing = resolution as FontResolution.NotInstalled
        assertEquals(MISSING_FAMILY, missing.requestedFamily)
        assertNotEquals(MISSING_FAMILY, missing.substituteFamily)
        assertEquals(18.0, missing.font.size)
        assertEquals(missing.substituteFamily.lowercase(), missing.font.family.lowercase())
    }

    /**
     * Use case: the substitute is not picked at random - it is the first family of the documented
     * fallback chain that is installed here, so two runs on the same machine paginate the same way.
     */
    @Test
    fun substituteFollowsTheFixedFallbackChain() {
        val resolution = fx {
            FontResolver.resolve(FontData(name = MISSING_FAMILY)) as FontResolution.NotInstalled
        }

        val expected = fx {
            FontResolver.FALLBACK_FAMILIES.firstOrNull { FontCatalog.contains(it) }
                ?: Font.getDefault().family
        }
        assertEquals(expected, resolution.substituteFamily)
    }

    /**
     * Use case: the same design font is resolved over and over during a layout run and must always
     * give the same answer, for an installed as well as for a missing family.
     */
    @Test
    fun resolutionIsDeterministic() {
        val installed = FontData(name = installedFamily(), size = 11)
        val missing = FontData(name = MISSING_FAMILY, size = 11)

        assertEquals(fx { FontResolver.resolve(installed) }, fx { FontResolver.resolve(installed) })
        assertEquals(fx { FontResolver.resolve(missing) }, fx { FontResolver.resolve(missing) })
    }

    /**
     * Use case: a caller that only wants to draw asks for the font alone and gets the font of the
     * resolution, whether the family was installed or substituted.
     */
    @Test
    fun fontShortcutReturnsTheFontOfTheResolution() {
        val missing = FontData(name = MISSING_FAMILY, size = 20, italic = true)

        assertEquals(fx { FontResolver.resolve(missing) }.font, fx { FontResolver.font(missing) })
    }

    private companion object {

        /** A family name no machine carries, used to force the missing state. */
        const val MISSING_FAMILY: String = "No Such Family 4711"
    }
}

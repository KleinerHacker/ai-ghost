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

import javafx.stage.Stage
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.Test
import org.pcsoft.app.aighost.layouting.fx.font.FontFingerprints
import org.pcsoft.app.aighost.model.common.FontData
import org.pcsoft.app.aighost.model.common.FontMetricsData
import org.testfx.framework.junit5.ApplicationTest
import org.testfx.util.WaitForAsyncUtils

/**
 * Developer tests for the comparison of a stored font against the font of this machine,
 * [FontIdentity].
 */
class FontIdentityTest : ApplicationTest() {

    override fun start(stage: Stage) = Unit

    /** Runs [block] on the JavaFX application thread and hands back its result. */
    private fun <T> fx(block: () -> T): T =
        WaitForAsyncUtils.asyncFx<T> { block() }.get()

    private fun installedFamily(): String = fx { FontCatalog.families }.first()

    private fun fingerprintOf(family: String): FontMetricsData =
        fx { FontFingerprints.of(family) }!!.toMetricsData()

    /**
     * Use case: a project written before the fingerprint existed is opened. Nothing was ever
     * measured for its fonts, so nothing can be said about them and above all nothing is reported.
     */
    @Test
    fun aFontWithoutAFingerprintIsUnknown() {
        val identity = fx { FontIdentity.of(FontData(name = installedFamily())) }

        assertEquals(FontIdentity.Unknown, identity, "an unmeasured font must stay unknown")
    }

    /**
     * Use case: a project is opened on the machine it was written on. The family measures exactly as
     * it did back then, so the manuscript is set the way the author saw it.
     */
    @Test
    fun aFontMeasuringAsItDidMatches() {
        val family = installedFamily()
        val data = FontData(name = family, metrics = fingerprintOf(family))

        val identity = fx { FontIdentity.of(data) }

        assertEquals(FontIdentity.Matches, identity, "the same machine must report a match")
    }

    /**
     * Use case: the family is installed under the name of the project, but another version of the
     * face sits behind it. The name matches and the measurement does not, which is exactly what the
     * fingerprint exists for.
     */
    @Test
    fun aFontMeasuringDifferentlyDeviates() {
        val family = installedFamily()
        val stored = fingerprintOf(family).copy(widths = "0000000000000000")
        val data = FontData(name = family, metrics = stored)

        val identity = fx { FontIdentity.of(data) }

        val deviates = assertInstanceOf(FontIdentity.Deviates::class.java, identity)
        assertEquals(family, deviates.family, "the deviation names the family of the project")
        assertEquals(stored, deviates.stored, "the deviation carries what the project stored")
        assertEquals(
            fingerprintOf(family),
            deviates.measured,
            "the deviation carries what this machine measures"
        )
    }

    /**
     * Use case: the family the project was written in is not installed here at all, so the fallback
     * chain sets the manuscript in another one. The report has to name both.
     */
    @Test
    fun aFontThatIsNotInstalledIsSubstituted() {
        val data = FontData(
            name = "No Such Family At All",
            metrics = FontMetricsData("0123456789abcdef", 11.0, 3.0, 0.0)
        )

        val identity = fx { FontIdentity.of(data) }

        val substituted = assertInstanceOf(FontIdentity.Substituted::class.java, identity)
        assertEquals(
            "No Such Family At All",
            substituted.requestedFamily,
            "the report names the family the project asks for"
        )
        assertEquals(
            fx { FontResolver.font(data) }.family.lowercase(),
            substituted.substituteFamily.lowercase(),
            "the report names the family that is set instead"
        )
    }

    /**
     * Use case: a font that is not installed carries no fingerprint either - an older project on a
     * machine without that family. Nothing was ever measured, so nothing is reported.
     */
    @Test
    fun aFontThatIsNeitherInstalledNorMeasuredIsUnknown() {
        val identity = fx { FontIdentity.of(FontData(name = "No Such Family At All")) }

        assertEquals(FontIdentity.Unknown, identity, "an unmeasured font must stay unknown")
    }
}

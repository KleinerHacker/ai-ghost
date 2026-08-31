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
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.pcsoft.app.aighost.layouting.fx.font.FontCatalog
import org.pcsoft.app.aighost.model.common.FontMetricsData
import org.pcsoft.app.aighost.model.project.design.Design
import org.testfx.framework.junit5.ApplicationTest
import org.testfx.util.WaitForAsyncUtils

/**
 * Developer tests for the fingerprinting and the checking of the fonts of a design,
 * [FontIdentityCheck].
 */
class FontIdentityCheckTest : ApplicationTest() {

    override fun start(stage: Stage) = Unit

    /** Runs [block] on the JavaFX application thread and hands back its result. */
    private fun <T> fx(block: () -> T): T =
        WaitForAsyncUtils.asyncFx<T> { block() }.get()

    private fun installedFamily(): String = fx { FontCatalog.families }.first()

    /** A design whose six styles all name the family handed in. */
    private fun designOf(family: String): Design = Design().apply {
        stylesOf(this).forEach { it.font.name = family }
    }

    private fun stylesOf(design: Design) = listOf(
        design.titlePage.titleStyle,
        design.titlePage.authorStyle,
        design.copyrightPage.copyrightStyle,
        design.chapterPage.titleStyle,
        design.chapterPage.titleAppendixStyle,
        design.chapterPage.textStyle
    )

    /**
     * Use case: a project is saved for the first time since the fingerprint exists. Every font of
     * its design is measured, so the manuscript records what it was written in.
     */
    @Test
    fun stampingRecordsEveryFontOfTheDesign() {
        val design = designOf(installedFamily())

        fx { FontIdentityCheck.stamp(design) }

        stylesOf(design).forEach { style ->
            assertNotNull(style.font.metrics, "every font of the design must be measured")
        }
    }

    /**
     * Use case: a project written elsewhere is saved here. The fingerprints it brought along stay as
     * they are - overwriting them would turn the record into "what the last machine measured" and
     * the comparison would never report anything again.
     */
    @Test
    fun stampingLeavesAnExistingFingerprintAlone() {
        val design = designOf(installedFamily())
        val brought = FontMetricsData("0123456789abcdef", 11.0, 3.0, 0.0)
        design.chapterPage.textStyle.font.metrics = brought

        fx { FontIdentityCheck.stamp(design) }

        assertEquals(brought, design.chapterPage.textStyle.font.metrics, "a stored fingerprint stays")
    }

    /**
     * Use case: a design names a font this machine does not have. It is left without a fingerprint,
     * because the fingerprint of the substitute would describe the wrong family and would make every
     * other machine report a deviation.
     */
    @Test
    fun stampingLeavesAnUninstalledFamilyWithoutAFingerprint() {
        val design = designOf("No Such Family At All")

        fx { FontIdentityCheck.stamp(design) }

        stylesOf(design).forEach { style ->
            assertNull(style.font.metrics, "an uninstalled family must not be measured")
        }
    }

    /**
     * Use case: a project is opened on the machine it was saved on. Every font measures as it did,
     * so the user is told nothing at all.
     */
    @Test
    fun aDesignStampedHereReportsNothing() {
        val design = designOf(installedFamily())
        fx { FontIdentityCheck.stamp(design) }

        val findings = fx { FontIdentityCheck.check(design) }

        assertTrue(findings.isEmpty(), "the machine that stamped must not report anything")
    }

    /**
     * Use case: a project older than the fingerprint is opened. Nothing was ever measured, so
     * nothing is reported - the alternative would be a false alarm on every machine.
     */
    @Test
    fun aDesignWithoutFingerprintsReportsNothing() {
        val design = designOf(installedFamily())

        val findings = fx { FontIdentityCheck.check(design) }

        assertTrue(findings.isEmpty(), "an unmeasured design must not report anything")
    }

    /**
     * Use case: a project is opened on another machine. The body text names a family that is missing
     * here and the title carries a fingerprint that does not match, so exactly those two elements
     * are reported and each one carries the key naming it.
     */
    @Test
    fun everyElementThatIsNotSetAsWrittenIsReported() {
        val family = installedFamily()
        val design = designOf(family)
        fx { FontIdentityCheck.stamp(design) }
        design.titlePage.titleStyle.font.metrics =
            design.titlePage.titleStyle.font.metrics!!.copy(widths = "0000000000000000")
        design.chapterPage.textStyle.font.name = "No Such Family At All"

        val findings = fx { FontIdentityCheck.check(design) }

        assertEquals(2, findings.size, "exactly the two broken elements are reported")
        assertEquals(
            FontIdentityCheck.ELEMENT_TITLE,
            findings[0].elementKey,
            "the title is reported first, the way the elements appear in a book"
        )
        assertInstanceOf(FontIdentity.Deviates::class.java, findings[0].identity)
        assertEquals(
            FontIdentityCheck.ELEMENT_TEXT,
            findings[1].elementKey,
            "the body text is reported last"
        )
        assertInstanceOf(FontIdentity.Substituted::class.java, findings[1].identity)
    }
}

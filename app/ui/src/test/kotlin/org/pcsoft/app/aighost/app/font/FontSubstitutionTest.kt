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

import javafx.scene.text.FontPosture
import javafx.scene.text.FontWeight
import javafx.stage.Stage
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.pcsoft.app.aighost.model.common.FontData
import org.testfx.framework.junit5.ApplicationTest
import org.testfx.util.WaitForAsyncUtils
import javafx.scene.text.Font as FxFont

/**
 * Developer tests for [FontSubstitution], the wrapper naming the family the platform resolves a
 * request to.
 */
class FontSubstitutionTest : ApplicationTest() {

    override fun start(stage: Stage) = Unit

    /** Runs [block] on the JavaFX application thread and hands back its result. */
    private fun <T> fx(block: () -> T): T =
        WaitForAsyncUtils.asyncFx<T> { block() }.get()

    /**
     * Use case: a family this machine does not have is requested. The name reported is the very
     * family the plain JavaFX text stack resolves the same request to, independently asked here as
     * the oracle for the assertion.
     */
    @Test
    fun namesTheFamilyTheTextStackResolvesTo() {
        val font = FontData(name = "No Such Family At All", bold = true, italic = true).toEngineFont()

        val resolved = fx { FontSubstitution.resolvedFamilyOf(font) }

        val expected = fx {
            FxFont.font("No Such Family At All", FontWeight.BOLD, FontPosture.ITALIC, font.size).family
        }
        assertEquals(expected, resolved)
    }
}

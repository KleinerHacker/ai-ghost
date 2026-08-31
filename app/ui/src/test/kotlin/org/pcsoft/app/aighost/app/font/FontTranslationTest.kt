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

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.pcsoft.app.aighost.model.common.FontData
import org.pcsoft.app.aighost.model.common.FontMetricsData

/**
 * Developer tests for [toFontDescription], the copy from a stored [FontData] to the
 * [org.pcsoft.app.aighost.layouting.fx.font.FontDescription] the renderer library resolves with.
 */
class FontTranslationTest {

    /**
     * Use case: a font stored with non default values in every field reaches the renderer library
     * with exactly those values - family, size and both switches of the cut are carried over as they
     * are.
     */
    @Test
    fun everyFieldIsCarriedOverAsItIs() {
        val data = FontData(name = "Garamond", size = 17, bold = true, italic = true)

        val description = data.toFontDescription()

        assertEquals("Garamond", description.family)
        assertEquals(17, description.size)
        assertEquals(true, description.bold)
        assertEquals(true, description.italic)
    }

    /**
     * Use case: a plain body font with the defaults of [FontData] translates to a description that
     * carries the very same defaults.
     */
    @Test
    fun defaultsTranslateToTheSameDefaults() {
        val description = FontData().toFontDescription()

        assertEquals("Arial", description.family)
        assertEquals(12, description.size)
        assertEquals(false, description.bold)
        assertEquals(false, description.italic)
    }

    /**
     * Use case: the measurement fingerprint stored beside the name is an identity, not an input of
     * font resolution, so it is left behind and does not influence the resulting description.
     */
    @Test
    fun theMeasurementFingerprintIsNotPartOfTheDescription() {
        val plain = FontData(name = "Georgia", size = 13)
        val stamped = FontData(
            name = "Georgia",
            size = 13,
            metrics = FontMetricsData("digest", 10.0, 3.0, 1.0)
        )

        assertEquals(plain.toFontDescription(), stamped.toFontDescription())
    }
}

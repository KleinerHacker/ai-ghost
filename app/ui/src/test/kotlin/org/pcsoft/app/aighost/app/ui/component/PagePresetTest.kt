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

package org.pcsoft.app.aighost.app.ui.component

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

/**
 * Developer tests for [PagePreset].
 */
class PagePresetTest {

    /**
     * Use case: a page is exactly the size of a preset, so that preset is reported.
     */
    @Test
    fun matchesAnExactPreset() {
        assertEquals(PagePreset.A5, PagePreset.match(148.0, 210.0))
        assertEquals(PagePreset.A4, PagePreset.match(210.0, 297.0))
        assertEquals(PagePreset.US_TRADE_6_9, PagePreset.match(152.4, 228.6))
    }

    /**
     * Use case: a page sits within half a millimetre of a preset, so rounding on the way through
     * points and back still lands on that preset.
     */
    @Test
    fun matchesWithinHalfAMillimetre() {
        assertEquals(PagePreset.A5, PagePreset.match(148.3, 209.7))
    }

    /**
     * Use case: a page is sized freely and matches no preset, so it counts as custom.
     */
    @Test
    fun reportsCustomWhenNothingMatches() {
        assertEquals(PagePreset.CUSTOM, PagePreset.match(300.0, 400.0))
    }

    /**
     * Use case: the custom entry carries no measures, so it can never be produced by a size match.
     */
    @Test
    fun customCarriesNoMeasures() {
        assertEquals(null, PagePreset.CUSTOM.widthMm)
        assertEquals(null, PagePreset.CUSTOM.heightMm)
    }
}

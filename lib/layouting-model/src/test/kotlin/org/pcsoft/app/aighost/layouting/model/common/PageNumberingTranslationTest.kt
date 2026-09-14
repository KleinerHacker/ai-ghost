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

package org.pcsoft.app.aighost.layouting.model.common

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.pcsoft.app.aighost.model.project.design.PageNumberCountingMode
import org.pcsoft.app.aighost.model.project.design.PageNumberDesign
import org.pcsoft.app.aighost.model.project.design.PageNumberPosition
import org.pcsoft.framework.simplay.engine.PageCountingMode
import org.pcsoft.framework.simplay.engine.model.PageNumberPosition as SimplayPageNumberPosition

/**
 * Developer tests for the translation of a stored page numbering into a simPlay [org.pcsoft.framework.simplay.engine.model.PageNumbering],
 * [PageNumberDesign.toPageNumbering].
 */
class PageNumberingTranslationTest {

    /**
     * Use case: a stored page numbering is handed to the layout engine, so its start value and the
     * excluded ids arrive unchanged in the simPlay numbering.
     */
    @Test
    fun theStartNumberAndTheExcludedIdsAreCarriedOver() {
        val stored = PageNumberDesign(
            position = PageNumberPosition.BOTTOM_OUTER,
            startNumber = 5,
            countingMode = PageNumberCountingMode.CONTINUOUS
        )

        val translated = stored.toPageNumbering(setOf("title", "copyright"))

        assertEquals(5, translated.startNumber)
        assertEquals(setOf("title", "copyright"), translated.excludedPageIds)
    }

    /**
     * Use case: every position a page number may be printed at is translated to its simPlay
     * counterpart, constant for constant.
     */
    @ParameterizedTest
    @CsvSource(
        "OFF, OFF",
        "TOP_LEFT, TOP_LEFT",
        "TOP_CENTER, TOP_CENTER",
        "TOP_RIGHT, TOP_RIGHT",
        "TOP_INNER, TOP_INNER",
        "TOP_OUTER, TOP_OUTER",
        "BOTTOM_LEFT, BOTTOM_LEFT",
        "BOTTOM_CENTER, BOTTOM_CENTER",
        "BOTTOM_RIGHT, BOTTOM_RIGHT",
        "BOTTOM_INNER, BOTTOM_INNER",
        "BOTTOM_OUTER, BOTTOM_OUTER",
    )
    fun everyPositionTranslatesToItsSimplayCounterpart(stored: PageNumberPosition, expected: SimplayPageNumberPosition) {
        assertEquals(expected, stored.toSimplayPosition())
    }

    /**
     * Use case: both counting modes are translated to their simPlay counterpart, constant for
     * constant.
     */
    @ParameterizedTest
    @CsvSource(
        "CONTINUOUS, CONTINUOUS",
        "SKIP_EXCLUDED, SKIP_EXCLUDED",
    )
    fun everyCountingModeTranslatesToItsSimplayCounterpart(stored: PageNumberCountingMode, expected: PageCountingMode) {
        assertEquals(expected, stored.toPageCountingMode())
        assertEquals(expected, PageNumberDesign(countingMode = stored).toPageNumbering(emptySet()).counting)
    }
}

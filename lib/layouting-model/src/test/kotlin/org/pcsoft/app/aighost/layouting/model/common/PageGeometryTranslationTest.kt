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
import org.pcsoft.app.aighost.layouting.PageGeometry
import org.pcsoft.app.aighost.model.project.design.PageFormat

/**
 * Developer tests for the translation of a stored page format into a layout page geometry,
 * [PageFormat.toPageGeometry].
 */
class PageGeometryTranslationTest {

    /**
     * Use case: a stored page format is handed to the layout core, and every measure - the sheet
     * size, all four margins and the mirrored margins switch - arrives unchanged.
     */
    @Test
    fun everyMeasureOfTheStoredFormatIsCarriedOver() {
        val stored = PageFormat(
            width = 400.0,
            height = 600.0,
            innerMargin = 25.0,
            outerMargin = 18.0,
            topMargin = 12.0,
            bottomMargin = 22.0,
            mirroredMargins = true
        )

        val translated = stored.toPageGeometry()

        assertEquals(
            PageGeometry(
                width = 400.0,
                height = 600.0,
                innerMargin = 25.0,
                outerMargin = 18.0,
                topMargin = 12.0,
                bottomMargin = 22.0,
                mirroredMargins = true
            ),
            translated
        )
    }
}

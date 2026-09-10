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
import org.pcsoft.app.aighost.model.project.design.PageFormat
import org.pcsoft.framework.simplay.engine.geometry.Margins
import org.pcsoft.framework.simplay.engine.geometry.Size
import org.pcsoft.framework.simplay.engine.model.PageLayout
import org.junit.jupiter.api.Test

/**
 * Developer tests for the translation of a stored page format into a simPlay page layout,
 * [PageFormat.toPageLayout].
 */
class PageLayoutTranslationTest {

    /**
     * Use case: a stored page format is handed to the engine; the sheet size arrives as a [Size] and
     * the four margins as a [Margins], with the inner margin on the left and the outer margin on the
     * right.
     */
    @Test
    fun theSheetSizeAndTheFourMarginsAreCarriedOver() {
        val stored = PageFormat(
            width = 400.0,
            height = 600.0,
            innerMargin = 25.0,
            outerMargin = 18.0,
            topMargin = 12.0,
            bottomMargin = 22.0
        )

        val translated = stored.toPageLayout()

        assertEquals(
            PageLayout(
                size = Size(width = 400.0, height = 600.0),
                margins = Margins(left = 25.0, top = 12.0, right = 18.0, bottom = 22.0)
            ),
            translated
        )
    }

    /**
     * Use case: simPlay has no recto/verso notion, so the mirrored-margins switch of the stored
     * format has no effect - a page format with it on translates exactly like one with it off.
     */
    @Test
    fun theMirroredMarginsSwitchHasNoEffectYet() {
        val base = PageFormat(innerMargin = 25.0, outerMargin = 18.0)

        assertEquals(
            base.copy(mirroredMargins = false).toPageLayout(),
            base.copy(mirroredMargins = true).toPageLayout()
        )
    }
}

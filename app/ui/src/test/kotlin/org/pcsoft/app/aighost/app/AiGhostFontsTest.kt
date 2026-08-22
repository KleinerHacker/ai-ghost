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

package org.pcsoft.app.aighost.app

import javafx.scene.text.Font
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.testfx.framework.junit5.ApplicationExtension

/**
 * Developer tests for [AiGhostFonts].
 */
@ExtendWith(ApplicationExtension::class)
class AiGhostFontsTest {

    /**
     * Use case: the shipped type face is packaged as a resource and can be resolved, so the
     * application never starts without its own lettering.
     */
    @Test
    fun fontResourceExists() {
        assertNotNull(AiGhostFonts::class.java.getResource(AiGhostFonts.REGULAR_PATH))
        assertTrue(AiGhostFonts.REGULAR_PATH.endsWith("GhostWriter-Regular.ttf"))
    }

    /**
     * Use case: installing the fonts makes the shipped family known to JavaFX, so every place that
     * asks for [AiGhostFonts.FAMILY] receives the real type face instead of a substitute.
     */
    @Test
    fun installRegistersTheShippedFamily() {
        AiGhostFonts.install()

        assertTrue(
            AiGhostFonts.FAMILY in Font.getFamilies(),
            "Missing font family: ${AiGhostFonts.FAMILY}",
        )
    }

    /**
     * Use case: installing the fonts twice - for instance because the theme is installed again in a
     * test - leaves the family registered exactly once.
     */
    @Test
    fun installTwiceRegistersTheFamilyOnce() {
        AiGhostFonts.install()
        AiGhostFonts.install()

        assertEquals(1, Font.getFamilies().count { it == AiGhostFonts.FAMILY })
    }

    /**
     * Use case: installing the theme also brings the lettering, so a caller never has to remember to
     * register the fonts separately.
     */
    @Test
    fun themeInstallBringsTheFonts() {
        AiGhostTheme.install()

        assertTrue(AiGhostFonts.FAMILY in Font.getFamilies())
    }
}

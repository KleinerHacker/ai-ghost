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

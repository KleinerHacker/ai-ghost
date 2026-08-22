package org.pcsoft.app.aighost.app

import javafx.scene.text.Font
import javafx.scene.text.Text
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.testfx.framework.junit5.ApplicationTest
import org.testfx.util.WaitForAsyncUtils

/**
 * Integration tests covering the shipped Ghost Writer type face inside a running JavaFX toolkit.
 */
class AiGhostFontsIT : ApplicationTest() {

    private companion object {

        /** Size the measurements are taken at. */
        const val SIZE: Double = 40.0

        /** Every letter the user interface has to render in English and in German. */
        const val LETTERS: String =
            "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyzÄÖÜäöüß"

        /** Every digit. */
        const val DIGITS: String = "0123456789"

        /** Every punctuation and special character the font has to carry. */
        const val SIGNS: String =
            ".,;:!?'\"„“‚‘()[]{}-–—/\\|+=<>~^*#%&@_$€§°"
    }

    private lateinit var font: Font

    /**
     * Registers the shipped fonts and resolves the family under test before every test.
     */
    @BeforeEach
    fun installFont() {
        onFxThread { AiGhostFonts.install() }
        font = onFxThread { Font.font(AiGhostFonts.FAMILY, SIZE) }
    }

    /**
     * Use case: the family the application asks for is the shipped one, so the user interface is not
     * silently rendered with a platform font of the same size.
     */
    @Test
    fun requestingTheFamilyYieldsTheShippedFont() {
        assertEquals(AiGhostFonts.FAMILY, font.family)
        assertEquals(SIZE, font.size)
    }

    /**
     * Use case: every character of both shipped languages, every digit and every supported sign has
     * an outline, so no text ever falls back to a replacement box.
     */
    @Test
    fun everySupportedCharacterIsDrawn() {
        val missing = onFxThread {
            (LETTERS + DIGITS + SIGNS).filter { character -> widthOf(character.toString()) <= 0.0 }
        }

        assertTrue(missing.isEmpty(), "Characters without an outline: $missing")
    }

    /**
     * Use case: a blank is narrower than any letter but still advances the caret, so words stay
     * separated without a visible gap of the wrong size.
     */
    @Test
    fun spaceAdvancesWithoutInk() {
        val space = onFxThread { widthOf(" ") }
        val letter = onFxThread { widthOf("M") }

        assertTrue(space > 0.0, "The space does not advance")
        assertTrue(space < letter, "The space is not narrower than a letter")
    }

    /**
     * Use case: all digits share one advance width, so figures in a column line up underneath each
     * other. A substituted font would break this property immediately.
     */
    @Test
    fun digitsShareOneAdvanceWidth() {
        val widths = onFxThread { DIGITS.map { widthOf(it.toString()) } }

        widths.forEach { width ->
            assertEquals(widths.first(), width, 0.01, "Digit widths differ: $widths")
        }
    }

    /**
     * Use case: a letter with a diaeresis advances exactly as far as the same letter without it, so
     * German and English text share one rhythm and a translated label keeps its width.
     */
    @Test
    fun diaeresisKeepsTheAdvanceWidth() {
        val pairs = listOf("A" to "Ä", "O" to "Ö", "U" to "Ü", "a" to "ä", "o" to "ö", "u" to "ü")

        val differing = onFxThread {
            pairs.filter { (plain, marked) -> widthOf(plain) != widthOf(marked) }
        }

        assertTrue(differing.isEmpty(), "Diaeresis changes the advance width: $differing")
    }

    private fun widthOf(value: String): Double =
        Text(value).also { it.font = font }.layoutBounds.width

    private fun <T> onFxThread(action: () -> T): T =
        WaitForAsyncUtils.asyncFx<T> { action() }.get()
}

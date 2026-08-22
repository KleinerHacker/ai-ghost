package org.pcsoft.app.aighost.model.common

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.exc.MismatchedInputException
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource

/**
 * Developer tests for [StyleData], [FontData] and [Alignment].
 */
class StyleDataTest {

    private val mapper: ObjectMapper = ObjectMapper().registerKotlinModule()

    /**
     * Use case: a style is created from a font alone, so the text is aligned to the left until the
     * user picks something else.
     */
    @Test
    fun defaultsToLeftAlignment() {
        assertEquals(Alignment.LEFT, StyleData(FontData("Serif", 12, false, false)).alignment)
    }

    /**
     * Use case: a style is written to disk, so font and alignment appear in the JSON under the stable
     * property names the file format promises.
     */
    @Test
    fun serialisesFontAndAlignment() {
        val style = StyleData(FontData("Serif", 12, true, false), Alignment.BLOCK)

        val json = mapper.writeValueAsString(style)

        assertEquals(
            """{"font":{"name":"Serif","size":12,"bold":true,"italic":false},"alignment":"BLOCK"}""",
            json
        )
    }

    /**
     * Use case: a stored style is read back, so every selectable alignment is restored exactly as it
     * was written.
     */
    @ParameterizedTest
    @EnumSource(Alignment::class)
    fun roundTripsEveryAlignment(alignment: Alignment) {
        val style = StyleData(FontData("Sans", 14, false, true), alignment)

        val restored: StyleData = mapper.readValue(mapper.writeValueAsString(style))

        assertEquals(style, restored)
    }

    /**
     * Use case: bold and italic are stored separately, so a font that is only italic does not come
     * back as bold as well.
     */
    @Test
    fun roundTripsFontFlagsSeparately() {
        val font = FontData("Sans", 9, bold = false, italic = true)

        val restored: FontData = mapper.readValue(mapper.writeValueAsString(font))

        assertEquals(false, restored.bold)
        assertEquals(true, restored.italic)
        assertEquals(9, restored.size)
        assertEquals("Sans", restored.name)
    }

    /**
     * Use case: a style written by a newer version carries additional properties, so reading it
     * ignores what is unknown instead of failing.
     */
    @Test
    fun ignoresUnknownProperties() {
        val json = """{"font":{"name":"Serif","size":12,"bold":false,"italic":false,"underline":true}}"""

        val style: StyleData = mapper.readValue(json)

        assertEquals(StyleData(FontData("Serif", 12, false, false)), style)
    }

    /**
     * Use case: a style file misses the mandatory font, so reading it fails instead of producing a
     * style that cannot be rendered.
     */
    @Test
    fun rejectsStyleWithoutFont() {
        assertThrows<MismatchedInputException> {
            mapper.readValue<StyleData>("""{"alignment":"LEFT"}""")
        }
    }
}

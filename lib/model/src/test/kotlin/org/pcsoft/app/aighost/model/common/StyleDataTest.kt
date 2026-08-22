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

package org.pcsoft.app.aighost.model.common

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
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
     * Use case: a style file names the alignment only, so it is read with the default font instead of
     * being rejected, and the text stays renderable.
     */
    @Test
    fun readsStyleWithoutFontAsDefault() {
        val style: StyleData = mapper.readValue("""{"alignment":"LEFT"}""")

        assertEquals(FontData(), style.font)
        assertEquals(Alignment.LEFT, style.alignment)
    }

    /**
     * Use case: a style is created without any argument, so the text is drawn in the plain default
     * face the application ships with rather than in an undefined one.
     */
    @Test
    fun defaultsToPlainFont() {
        val font = FontData()

        assertEquals("Arial", font.name)
        assertEquals(12, font.size)
        assertEquals(false, font.bold)
        assertEquals(false, font.italic)
        assertEquals(FontData(), StyleData().font)
    }
}

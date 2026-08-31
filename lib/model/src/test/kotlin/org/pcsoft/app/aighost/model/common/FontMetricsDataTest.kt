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
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

/**
 * Developer tests for [FontMetricsData].
 */
class FontMetricsDataTest {

    private val mapper: ObjectMapper = ObjectMapper().registerKotlinModule()

    /**
     * Use case: a fingerprint object is created without any argument, so it stands for a family that
     * was never measured instead of claiming measures nobody took.
     */
    @Test
    fun defaultsToUnmeasuredValues() {
        val metrics = FontMetricsData()

        assertEquals("", metrics.widths)
        assertEquals(0.0, metrics.ascent)
        assertEquals(0.0, metrics.descent)
        assertEquals(0.0, metrics.leading)
    }

    /**
     * Use case: a font is created without a fingerprint, so the field says that the family has never
     * been measured rather than carrying an empty measurement.
     */
    @Test
    fun leavesFontMetricsAbsentByDefault() {
        assertNull(FontData().metrics)
    }

    /**
     * Use case: a fingerprint is written to disk and read back on another machine, so every measure
     * and the digest of the widths arrive exactly as they were taken.
     */
    @Test
    fun roundTripsEveryField() {
        val metrics = FontMetricsData(
            widths = "a1b2c3d4e5f60718",
            ascent = 9.75,
            descent = 2.5,
            leading = 1.25
        )

        val restored: FontMetricsData = mapper.readValue(mapper.writeValueAsString(metrics))

        assertEquals(metrics, restored)
        assertEquals("a1b2c3d4e5f60718", restored.widths)
        assertEquals(9.75, restored.ascent)
        assertEquals(2.5, restored.descent)
        assertEquals(1.25, restored.leading)
    }

    /**
     * Use case: a font carrying a fingerprint is written to disk, so the nested object travels along
     * with the family it belongs to and comes back attached to it.
     */
    @Test
    fun roundTripsMetricsNestedInFont() {
        val font = FontData(
            name = "Serif",
            size = 11,
            bold = true,
            italic = false,
            metrics = FontMetricsData(widths = "0f0f0f0f", ascent = 8.0, descent = 2.0, leading = 1.0)
        )

        val restored: FontData = mapper.readValue(mapper.writeValueAsString(font))

        assertEquals(font, restored)
        assertEquals("0f0f0f0f", restored.metrics?.widths)
    }

    /**
     * Use case: a fingerprint written by a newer version carries additional measures, so reading it
     * ignores what is unknown instead of failing on a project file that is otherwise fine.
     */
    @Test
    fun ignoresUnknownProperties() {
        val json = """{"widths":"abcdef","ascent":7.5,"descent":2.0,"leading":0.5,"xHeight":4.0}"""

        val metrics: FontMetricsData = mapper.readValue(json)

        assertEquals(FontMetricsData("abcdef", 7.5, 2.0, 0.5), metrics)
    }

    /**
     * Use case: an older fingerprint names the digest alone, so the measures missing from the file are
     * read as untaken instead of rejecting the whole project.
     */
    @Test
    fun readsPartialMetricsWithDefaults() {
        val metrics: FontMetricsData = mapper.readValue("""{"widths":"abcdef"}""")

        assertEquals("abcdef", metrics.widths)
        assertEquals(0.0, metrics.ascent)
        assertEquals(0.0, metrics.descent)
        assertEquals(0.0, metrics.leading)
    }

    /**
     * Use case: a font file written before fingerprints existed is opened, so the family is read
     * without one and the missing fingerprint says "never taken".
     */
    @Test
    fun readsFontWithoutMetricsAsAbsent() {
        val font: FontData = mapper.readValue("""{"name":"Serif","size":12}""")

        assertNull(font.metrics)
    }
}

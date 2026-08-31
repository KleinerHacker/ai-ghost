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

package org.pcsoft.app.aighost.model.project.book

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/**
 * Developer tests for [Copyright].
 */
class CopyrightTest {

    private val mapper: ObjectMapper = ObjectMapper().registerKotlinModule()

    /**
     * Use case: a book is created before the user wrote its front matter, so the copyright page is
     * prefilled with a notice, carries no further lines and belongs to the book.
     */
    @Test
    fun defaultsToAPrefilledIncludedPage() {
        val copyright = Copyright()

        assertTrue(copyright.copyright.startsWith("Copyright "))
        assertEquals(emptyList<String>(), copyright.copyrightAppendix)
        assertEquals(true, copyright.included)
    }

    /**
     * Use case: the user takes the copyright page out of the book and puts it back later on, so the
     * text and its further lines are still there instead of having been thrown away with the switch.
     */
    @Test
    fun keepsTextWhenSwitchedOffAndOnAgain() {
        val copyright = Copyright(
            copyright = "(c) 2026 Jane Doe",
            copyrightAppendix = listOf("All rights reserved."),
            included = true
        )

        copyright.included = false
        copyright.included = true

        assertEquals("(c) 2026 Jane Doe", copyright.copyright)
        assertEquals(listOf("All rights reserved."), copyright.copyrightAppendix)
    }

    /**
     * Use case: the copyright page is written to disk, so its notice, its further lines and the
     * switch appear in the JSON under the stable property names the file format promises.
     */
    @Test
    fun serialisesNoticeAppendixAndSwitch() {
        val json = mapper.writeValueAsString(
            Copyright("(c) 2026 Jane Doe", listOf("All rights reserved."), included = false)
        )

        assertTrue(json.contains(""""copyright":"(c) 2026 Jane Doe""""))
        assertTrue(json.contains(""""copyrightAppendix":["All rights reserved."]"""))
        assertTrue(json.contains(""""included":false"""))
    }

    /**
     * Use case: a stored project is opened again, so its copyright page comes back exactly as it was
     * written.
     */
    @Test
    fun roundTripsEveryProperty() {
        val copyright = Copyright("(c) 2026 Jane Doe", listOf("All rights reserved.", "Printed in the EU."))

        val restored: Copyright = mapper.readValue(mapper.writeValueAsString(copyright))

        assertEquals(copyright, restored)
    }

    /**
     * Use case: a document holds the notice only, so the remaining properties are filled with their
     * defaults instead of the part being rejected.
     */
    @Test
    fun readsPartialDocumentWithDefaults() {
        val copyright: Copyright = mapper.readValue("""{"copyright":"(c) 2026 Jane Doe"}""")

        assertEquals("(c) 2026 Jane Doe", copyright.copyright)
        assertEquals(emptyList<String>(), copyright.copyrightAppendix)
        assertEquals(true, copyright.included)
    }

    /**
     * Use case: a copyright page written by a newer version carries additional properties, so reading
     * it ignores what is unknown instead of failing.
     */
    @Test
    fun ignoresUnknownProperties() {
        val copyright: Copyright = mapper.readValue("""{"copyright":"(c) 2026","isbn":"123"}""")

        assertEquals("(c) 2026", copyright.copyright)
    }
}

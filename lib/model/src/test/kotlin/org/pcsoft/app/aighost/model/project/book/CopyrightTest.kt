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
     * Use case: a book is created before the user wrote its front matter, so the copyright page
     * already belongs to the book.
     */
    @Test
    fun defaultsToIncluded() {
        assertEquals(true, Copyright().included)
    }

    /**
     * Use case: the copyright page is written to disk, so its switch appears in the JSON under the
     * stable property name the file format promises.
     */
    @Test
    fun serialisesSwitch() {
        val json = mapper.writeValueAsString(Copyright(included = false))

        assertTrue(json.contains(""""included":false"""))
    }

    /**
     * Use case: a stored project is opened again, so its copyright page comes back exactly as it was
     * written.
     */
    @Test
    fun roundTripsEveryProperty() {
        val copyright = Copyright(included = false)

        val restored: Copyright = mapper.readValue(mapper.writeValueAsString(copyright))

        assertEquals(copyright, restored)
    }

    /**
     * Use case: a document holds no properties at all, so the switch is filled with its default
     * instead of the part being rejected.
     */
    @Test
    fun readsEmptyDocumentWithDefaults() {
        val copyright: Copyright = mapper.readValue("""{}""")

        assertEquals(true, copyright.included)
    }

    /**
     * Use case: a copyright page written by a newer version carries additional properties, so reading
     * it ignores what is unknown instead of failing.
     */
    @Test
    fun ignoresUnknownProperties() {
        val copyright: Copyright = mapper.readValue("""{"included":false,"isbn":"123"}""")

        assertEquals(false, copyright.included)
    }

    /**
     * Use case: an old project written before IP-36 still carries `copyright` and
     * `copyrightAppendix` in its JSON, so opening it drops those fields instead of failing to parse -
     * the hard requirement IP-36 leaves for IP-37's migration to build on.
     */
    @Test
    fun ignoresFieldsRemovedByThePreviousModelVersion() {
        val copyright: Copyright = mapper.readValue(
            """{"copyright":"(c) 2026","copyrightAppendix":["All rights reserved."],"included":true}"""
        )

        assertEquals(true, copyright.included)
    }
}

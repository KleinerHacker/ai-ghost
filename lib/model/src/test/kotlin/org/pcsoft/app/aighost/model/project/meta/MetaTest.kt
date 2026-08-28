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

package org.pcsoft.app.aighost.model.project.meta

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.pcsoft.app.aighost.model.TestData
import org.pcsoft.app.aighost.model.project.Project
import org.pcsoft.app.aighost.plugin.api.model.project.ProjectPartInfo

/**
 * Developer tests for [Meta].
 */
class MetaTest {

    private val mapper: ObjectMapper = ObjectMapper().registerKotlinModule()

    /**
     * Use case: the user creates a project through the menu, so it starts as a named but otherwise
     * empty project instead of asking for every text up front.
     */
    @Test
    fun defaultsToANamedButEmptyProject() {
        val meta = Meta()

        assertEquals("New Project", meta.name)
        assertEquals("", meta.author)
        assertEquals("", meta.copyright)
        assertEquals(emptyList<String>(), meta.additionalParts)
    }

    /**
     * Use case: the document says which parts belong to it, so the list of the parts beyond the
     * standard ones is stored under a stable property name and comes back from the file.
     */
    @Test
    fun roundTripsTheAdditionalParts() {
        val meta = TestData.meta().apply { additionalParts = listOf("outline") }

        val json = mapper.writeValueAsString(meta)

        assertEquals(true, json.contains(""""additionalParts":["outline"]"""))
        assertEquals(meta, mapper.readValue<Meta>(json))
    }

    /**
     * Use case: the part is stored in an entry of its own, so it names the identifier the project
     * uses for its meta data instead of relying on the class name.
     */
    @Test
    fun declaresTheMetaPartIdentifier() {
        val info = Meta::class.java.getAnnotation(ProjectPartInfo::class.java)

        assertEquals(Project.PART_META, info.identifier)
    }

    /**
     * Use case: the meta data is written to disk, so the publishing texts appear in the document
     * under the stable property names the file format promises.
     */
    @Test
    fun serialisesPublishingData() {
        val json = mapper.writeValueAsString(TestData.meta())

        assertEquals(true, json.contains(""""name":"My Novel""""))
        assertEquals(true, json.contains(""""author":"Jane Doe""""))
        assertEquals(true, json.contains(""""copyright":"(c) 2026 Jane Doe""""))
        assertEquals(true, json.contains(""""version":1"""))
    }

    /**
     * Use case: a stored project is opened again, so its publishing data comes back exactly as it was
     * written.
     */
    @Test
    fun roundTripsPublishingData() {
        val restored: Meta = mapper.readValue(mapper.writeValueAsString(TestData.meta()))

        assertEquals(TestData.meta(), restored)
    }

    /**
     * Use case: a document holds the name only, so the remaining texts are filled with their defaults
     * instead of the part being rejected.
     */
    @Test
    fun readsPartialDocumentWithDefaults() {
        val meta: Meta = mapper.readValue("""{"name":"My Novel"}""")

        assertEquals("My Novel", meta.name)
        assertEquals("", meta.author)
        assertEquals(1, meta.version)
    }

    /**
     * Use case: meta data written by a newer version carries additional properties, so reading it
     * ignores what is unknown instead of failing.
     */
    @Test
    fun ignoresUnknownProperties() {
        val meta: Meta = mapper.readValue("""{"name":"My Novel","language":"en"}""")

        assertEquals("My Novel", meta.name)
    }
}

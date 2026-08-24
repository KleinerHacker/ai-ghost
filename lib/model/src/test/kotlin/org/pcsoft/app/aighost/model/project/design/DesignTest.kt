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

package org.pcsoft.app.aighost.model.project.design

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.pcsoft.app.aighost.model.TestData
import org.pcsoft.app.aighost.model.common.Alignment
import org.pcsoft.app.aighost.model.common.StyleData
import org.pcsoft.app.aighost.model.project.Project
import org.pcsoft.app.aighost.plugin.api.model.project.ProjectPartInfo

/**
 * Developer tests for [Design].
 */
class DesignTest {

    private val mapper: ObjectMapper = ObjectMapper().registerKotlinModule()

    /**
     * Use case: a fresh project is rendered before the user touched the design, so every part carries
     * the default style and the page flags start on the layout the application ships with.
     */
    @Test
    fun defaultsToPlainDesign() {
        val design = Design()

        assertEquals(AuthorDesign(), design.authorDesign)
        assertEquals(CopyrightDesign(), design.copyrightDesign)
        assertEquals(TitleDesign(), design.titleDesign)
        assertEquals(ChapterDesign(), design.chapterDesign)
        assertEquals(TextDesign(), design.textDesign)
        assertEquals(true, design.startWithEmptyPage)
        assertEquals(true, design.endWithEmptyPage)
        assertEquals(StyleData(), design.textDesign.style)
    }

    /**
     * Use case: the part is stored in an entry of its own, so it names the identifier the project
     * uses for its design instead of relying on the class name.
     */
    @Test
    fun declaresTheDesignPartIdentifier() {
        val info = Design::class.java.getAnnotation(ProjectPartInfo::class.java)

        assertEquals(Project.PART_DESIGN, info.identifier)
    }

    /**
     * Use case: the user styled every text of the manuscript separately, so each design part keeps its
     * own font and alignment across the round trip instead of collapsing into one.
     */
    @Test
    fun roundTripsEveryDesignPartSeparately() {
        val restored: Design = mapper.readValue(mapper.writeValueAsString(TestData.design()))

        assertEquals(16, restored.authorDesign.style.font.size)
        assertEquals(Alignment.CENTER, restored.authorDesign.style.alignment)
        assertEquals(8, restored.copyrightDesign.style.font.size)
        assertEquals(Alignment.CENTER, restored.titleDesign.style.alignment)
        assertEquals("Sans", restored.chapterDesign.titleStyle.font.name)
        assertEquals(14, restored.chapterDesign.titleAppendixStyle.font.size)
        assertEquals(Alignment.BLOCK, restored.textDesign.style.alignment)
        assertEquals(TestData.design(), restored)
    }

    /**
     * Use case: the page options are stored as flags, so each one comes back with the value the user
     * selected instead of a shared default.
     */
    @Test
    fun roundTripsPageFlags() {
        val restored: Design = mapper.readValue(mapper.writeValueAsString(TestData.design()))

        assertEquals(true, restored.copyrightDesign.show)
        assertEquals(true, restored.startWithEmptyPage)
        assertEquals(false, restored.endWithEmptyPage)
    }

    /**
     * Use case: a document holds one design part only, so the remaining ones are filled with their
     * defaults instead of the part being rejected.
     */
    @Test
    fun readsPartialDocumentWithDefaults() {
        val design: Design = mapper.readValue("""{"startWithEmptyPage":false}""")

        assertEquals(false, design.startWithEmptyPage)
        assertEquals(TextDesign(), design.textDesign)
        assertEquals(1, design.version)
    }

    /**
     * Use case: a design written by a newer version carries additional properties, so reading it
     * ignores what is unknown instead of failing.
     */
    @Test
    fun ignoresUnknownProperties() {
        val design: Design = mapper.readValue("""{"pageMargin":20}""")

        assertEquals(Design(), design)
    }
}

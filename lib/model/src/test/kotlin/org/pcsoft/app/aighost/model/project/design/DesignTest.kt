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
import org.pcsoft.app.aighost.model.project.Project
import org.pcsoft.app.aighost.plugin.api.model.project.ProjectPartInfo

/**
 * Developer tests for [Design].
 */
class DesignTest {

    private val mapper: ObjectMapper = ObjectMapper().registerKotlinModule()

    /**
     * Use case: a fresh project is rendered before the user touched the design, so every page carries
     * the default styles and the page flags start on the layout the application ships with.
     */
    @Test
    fun defaultsToPlainDesign() {
        val design = Design()

        assertEquals(TitlePageDesign(), design.titlePage)
        assertEquals(CopyrightPageDesign(), design.copyrightPage)
        assertEquals(PrologPageDesign(), design.prologPage)
        assertEquals(BlurbPageDesign(), design.blurbPage)
        assertEquals(ChapterPageDesign(), design.chapterPage)
        assertEquals(EpilogPageDesign(), design.epilogPage)
        assertEquals(true, design.startWithEmptyPage)
        assertEquals(true, design.endWithEmptyPage)
    }

    /**
     * Use case: a fresh project is laid out before anybody chose a paper size, so the design carries
     * the default page geometry.
     */
    @Test
    fun defaultsToTheShippedPageLayout() {
        assertEquals(PageFormat(), Design().pageFormat)
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
     * Use case: the user styled every page of the manuscript separately, so each page design keeps
     * its own fonts, alignments and line spacings across the round trip instead of collapsing.
     */
    @Test
    fun roundTripsEveryPageDesignSeparately() {
        val restored: Design = mapper.readValue(mapper.writeValueAsString(TestData.design()))

        assertEquals(28, restored.titlePage.titleStyle.font.size)
        assertEquals(Alignment.CENTER, restored.titlePage.authorStyle.alignment)
        assertEquals(8, restored.copyrightPage.copyrightStyle.font.size)
        assertEquals(1.0, restored.copyrightPage.copyrightStyle.textLineSpacing)
        assertEquals("Sans", restored.chapterPage.titleStyle.font.name)
        assertEquals(14, restored.chapterPage.titleAppendixStyle.font.size)
        assertEquals(Alignment.BLOCK, restored.chapterPage.textStyle.alignment)
        assertEquals(1.45, restored.blurbPage.textStyle.textLineSpacing)
        assertEquals(TestData.design(), restored)
    }

    /**
     * Use case: the user chose a paper size of their own, so the page geometry survives the round
     * trip instead of falling back to the shipped default.
     */
    @Test
    fun roundTripsPageFormat() {
        val restored: Design = mapper.readValue(mapper.writeValueAsString(TestData.design()))

        assertEquals(TestData.pageFormat(), restored.pageFormat)
    }

    /**
     * Use case: the page options are stored as flags, so each one comes back with the value the user
     * selected instead of a shared default.
     */
    @Test
    fun roundTripsPageFlags() {
        val restored: Design = mapper.readValue(mapper.writeValueAsString(TestData.design()))

        assertEquals(true, restored.chapterPage.titleOnSeparatePage)
        assertEquals(true, restored.startWithEmptyPage)
        assertEquals(false, restored.endWithEmptyPage)
    }

    /**
     * Use case: a document holds one page flag only, so the remaining page designs are filled with
     * their defaults instead of the part being rejected.
     */
    @Test
    fun readsPartialDocumentWithDefaults() {
        val design: Design = mapper.readValue("""{"startWithEmptyPage":false}""")

        assertEquals(false, design.startWithEmptyPage)
        assertEquals(ChapterPageDesign(), design.chapterPage)
        assertEquals(PageFormat(), design.pageFormat)
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

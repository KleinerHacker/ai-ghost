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

package org.pcsoft.app.aighost.model.project

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.pcsoft.app.aighost.model.TestData
import org.pcsoft.app.aighost.model.project.book.Book
import org.pcsoft.app.aighost.model.project.design.Design
import org.pcsoft.app.aighost.model.project.meta.Meta
import org.pcsoft.app.aighost.plugin.api.model.project.ProjectPart

/**
 * Developer tests for [Project], for the three standard parts it carries as fields and for the parts
 * that are put beside them.
 */
class ProjectTest {

    /**
     * Use case: the user creates a project through the menu, so it starts with the three parts the
     * application ships with instead of being empty and unusable.
     */
    @Test
    fun defaultsToTheThreeStandardParts() {
        val project = Project()

        assertEquals(Meta(), project.meta)
        assertEquals(Design(), project.design)
        assertEquals(Book(), project.book)
        assertEquals(emptyMap<String, ProjectPart>(), project.extensionParts)
        assertEquals(emptyMap<String, String>(), project.unknownParts)
    }

    /**
     * Use case: the storage writes the open project, so it asks for every part of it - the three
     * standard ones under their identifier and everything a plugin put beside them.
     */
    @Test
    fun showsEveryPartUnderItsIdentifier() {
        val custom = customPart()
        val project = TestData.project().also { it.putPart("custom", custom) }

        assertEquals(
            listOf(Project.PART_META, Project.PART_DESIGN, Project.PART_BOOK, "custom"),
            project.parts.keys.toList()
        )
        assertSame(project.meta, project.parts[Project.PART_META])
        assertSame(project.design, project.parts[Project.PART_DESIGN])
        assertSame(project.book, project.parts[Project.PART_BOOK])
        assertSame(custom, project.parts["custom"])
    }

    /**
     * Use case: a document carries a part this application cannot read, so it is kept as the text it
     * was stored as and stays out of the parts a view could try to work with.
     */
    @Test
    fun keepsAPartItCannotReadOutOfTheParts() {
        val project = Project(unknownParts = mapOf("plugin-notes" to STORED_NOTES))

        assertEquals(
            listOf(Project.PART_META, Project.PART_DESIGN, Project.PART_BOOK),
            project.parts.keys.toList()
        )
        assertNull(project.part("plugin-notes"))
        assertEquals(mapOf("plugin-notes" to STORED_NOTES), project.unknownParts)
    }

    /**
     * Use case: the part behind a text the application could not read becomes readable - a plugin was
     * installed - so writing it takes the text away and the archive keeps one entry, not two.
     */
    @Test
    fun puttingAPartDropsTheTextStoredUnderTheSameIdentifier() {
        val custom = customPart()
        val project = Project(unknownParts = mapOf("plugin-notes" to STORED_NOTES))

        project.putPart("plugin-notes", custom)

        assertSame(custom, project.part("plugin-notes"))
        assertEquals(emptyMap<String, String>(), project.unknownParts)
    }

    /**
     * Use case: a view shows the project name and the manuscript, so each standard part is reached
     * through its own field instead of the view having to look an identifier up and cast the result.
     */
    @Test
    fun readsEveryStandardPartThroughItsField() {
        val project = TestData.project()

        assertSame(project.meta, project.part(Project.PART_META))
        assertSame(project.design, project.part(Project.PART_DESIGN))
        assertSame(project.book, project.part(Project.PART_BOOK))
        assertEquals("My Novel", project.meta.name)
    }

    /**
     * Use case: the user opens the design dialog and confirms it, so the new design replaces the one
     * the project carried while the other parts stay exactly as they were.
     */
    @Test
    fun writingAPartReplacesOnlyThatPart() {
        val project = TestData.project()
        val book = project.book
        val design = Design(startWithEmptyPage = false, endWithEmptyPage = false)

        project.design = design

        assertEquals(design, project.design)
        assertSame(book, project.book)
        assertEquals("My Novel", project.meta.name)
    }

    /**
     * Use case: a plugin puts a part of its own into the project, so that part travels with the
     * document instead of being dropped when a standard part is written.
     */
    @Test
    fun keepsPartsOfOtherOrigin() {
        val custom = customPart()
        val project = Project(extensionParts = mapOf("custom" to custom))

        project.meta = Meta(name = "Renamed")

        assertSame(custom, project.part("custom"))
        assertEquals("Renamed", project.meta.name)
    }

    /**
     * Use case: a plugin stores its part under the identifier it declares, so the part is put into the
     * project beside the standard ones and is found under exactly that identifier again.
     */
    @Test
    fun putsAPartOfOtherOriginBesideTheStandardOnes() {
        val custom = customPart()
        val project = Project()

        project.putPart("custom", custom)

        assertSame(custom, project.part("custom"))
        assertEquals(mapOf("custom" to custom), project.extensionParts)
    }

    /**
     * Use case: the property model of a standard part writes it back into the project, so the part
     * reaches the field it belongs to instead of ending up beside it.
     */
    @Test
    fun putsAStandardPartIntoItsField() {
        val project = Project()
        val meta = Meta(name = "My Novel")

        project.putPart(Project.PART_META, meta)

        assertSame(meta, project.meta)
        assertEquals(emptyMap<String, ProjectPart>(), project.extensionParts)
    }

    /**
     * Use case: a plugin writes a part of the wrong type under a standard identifier, so the project
     * reports it instead of silently losing the standard part.
     */
    @Test
    fun refusesAStandardIdentifierForAPartOfAnotherType() {
        val project = Project()

        assertThrows<IllegalArgumentException> { project.putPart(Project.PART_META, customPart()) }

        assertEquals(Meta(), project.meta)
    }

    /**
     * Use case: a plugin is switched off and takes its part out of the project, so the part is gone
     * while everything else stays.
     */
    @Test
    fun removesAPartOfOtherOrigin() {
        val project = Project(extensionParts = mapOf("custom" to customPart()))

        assertTrue(project.removePart("custom"))

        assertNull(project.part("custom"))
        assertEquals(Meta(), project.meta)
    }

    /**
     * Use case: a part is taken out twice - by two plugins for instance - so the second attempt
     * reports that there was nothing left to remove instead of failing.
     */
    @Test
    fun reportsRemovingAPartThatIsNotThere() {
        assertFalse(Project().removePart("custom"))
    }

    /**
     * Use case: something tries to take a standard part out of the project, so the project refuses it
     * - a project without its meta data, design or manuscript cannot be shown.
     */
    @Test
    fun refusesToRemoveAStandardPart() {
        val project = Project()

        assertThrows<IllegalArgumentException> { project.removePart(Project.PART_META) }

        assertEquals(Meta(), project.meta)
    }

    /**
     * Use case: a project file is read, so its entries become the standard parts of the project and
     * everything beyond them is kept beside them.
     */
    @Test
    fun buildsAProjectFromTheStoredParts() {
        val meta = Meta(name = "My Novel")
        val custom = customPart()

        val project = Project.fromParts(
            mapOf(Project.PART_META to meta, "custom" to custom),
            mapOf("plugin-notes" to STORED_NOTES)
        )

        assertSame(meta, project.meta)
        assertEquals(Design(), project.design)
        assertEquals(Book(), project.book)
        assertEquals(mapOf("custom" to custom), project.extensionParts)
        assertEquals(mapOf("plugin-notes" to STORED_NOTES), project.unknownParts)
    }

    /**
     * Use case: a document lost a part because an older version wrote it, so the project falls back to
     * the defaults of that part instead of failing when a view reads it.
     */
    @Test
    fun fallsBackToDefaultsForAMissingPart() {
        val project = Project.fromParts(emptyMap())

        assertEquals(Meta(), project.meta)
        assertEquals(Design(), project.design)
        assertEquals(Book(), project.book)
    }

    /**
     * Use case: two projects are compared - a saved one against the one in memory - so they count as
     * equal exactly when they carry the same parts.
     */
    @Test
    fun comparesByItsParts() {
        assertEquals(TestData.project(), TestData.project())
        assertTrue(Project() != TestData.project())
        assertTrue(Project() != Project(extensionParts = mapOf("custom" to customPart())))
        assertTrue(Project() != Project(unknownParts = mapOf("plugin-notes" to STORED_NOTES)))
    }

    /**
     * A part of an origin the application does not know, standing in for what a plugin brings along.
     */
    private fun customPart(): ProjectPart = object : ProjectPart {
        override val version: Int = 1
    }

    private companion object {
        /** The stored text of a part this application cannot read. */
        const val STORED_NOTES = """{"version":1,"note":"written elsewhere"}"""
    }
}

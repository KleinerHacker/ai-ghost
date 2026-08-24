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
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.pcsoft.app.aighost.model.TestData
import org.pcsoft.app.aighost.model.project.book.Book
import org.pcsoft.app.aighost.model.project.design.Design
import org.pcsoft.app.aighost.model.project.meta.Meta
import org.pcsoft.app.aighost.plugin.api.model.project.ProjectPart

/**
 * Developer tests for [Project] and the typed access to its parts.
 */
class ProjectTest {

    /**
     * Use case: the user creates a project through the menu, so it starts with the three parts the
     * application ships with instead of being empty and unusable.
     */
    @Test
    fun defaultsToTheThreeBuiltInParts() {
        val project = Project()

        assertEquals(
            setOf(Project.PART_META, Project.PART_DESIGN, Project.PART_BOOK),
            project.parts.keys
        )
        assertEquals(Meta(), project.meta)
        assertEquals(Design(), project.design)
        assertEquals(Book(), project.book)
    }

    /**
     * Use case: a view shows the project name and the manuscript, so each part is reached by its type
     * instead of the view having to look the identifier up and cast the result itself.
     */
    @Test
    fun readsEveryPartByItsType() {
        val project = TestData.project()

        assertSame(project.parts[Project.PART_META], project.meta)
        assertSame(project.parts[Project.PART_DESIGN], project.design)
        assertSame(project.parts[Project.PART_BOOK], project.book)
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
     * Use case: a plugin stores a part of its own in the project, so that part travels with the
     * document instead of being dropped when a built in part is written.
     */
    @Test
    fun keepsPartsOfOtherOrigin() {
        val custom = object : ProjectPart {
            override val version: Int = 1
        }
        val project = Project(Project().parts + ("custom" to custom))

        project.meta = Meta(name = "Renamed")

        assertSame(custom, project.parts["custom"])
        assertEquals("Renamed", project.meta.name)
    }

    /**
     * Use case: a document lost a part because an older version wrote it, so the project answers with
     * the defaults of that part instead of failing when a view reads it.
     */
    @Test
    fun fallsBackToDefaultsForAMissingPart() {
        val project = Project(emptyMap())

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
    }
}

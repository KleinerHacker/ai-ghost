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

import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.pcsoft.app.aighost.model.project.book.Book
import org.pcsoft.app.aighost.model.project.design.Design
import org.pcsoft.app.aighost.model.project.meta.Meta
import org.pcsoft.app.aighost.plugin.api.model.project.ProjectPartRegistry

/**
 * Developer tests for [StandardProjectParts], the announcement of the parts the application ships
 * with.
 */
class StandardProjectPartsTest {

    @BeforeEach
    fun setUp() {
        StandardProjectParts.register()
    }

    /**
     * Use case: a stored document is read, so the storage finds the class behind every standard entry
     * of it instead of skipping the whole project.
     */
    @Test
    fun announcesTheThreeStandardParts() {
        assertSame(Meta::class, ProjectPartRegistry.partClassOf(Project.PART_META))
        assertSame(Design::class, ProjectPartRegistry.partClassOf(Project.PART_DESIGN))
        assertSame(Book::class, ProjectPartRegistry.partClassOf(Project.PART_BOOK))
    }

    /**
     * Use case: the announcement is made from more than one place - the storage and a test for
     * instance - so making it again changes nothing instead of reporting a conflict.
     */
    @Test
    fun announcesThePartsAgainWithoutComplaining() {
        StandardProjectParts.register()

        assertSame(Meta::class, ProjectPartRegistry.partClassOf(Project.PART_META))
    }
}

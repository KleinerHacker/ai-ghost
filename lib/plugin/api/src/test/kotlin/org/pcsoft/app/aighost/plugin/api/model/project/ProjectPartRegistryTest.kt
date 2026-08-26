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

package org.pcsoft.app.aighost.plugin.api.model.project

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

/**
 * Developer tests for [ProjectPartRegistry], the place a project part is looked up by the identifier
 * it is stored under.
 */
class ProjectPartRegistryTest {

    /**
     * The registry lives as long as the process does, so every test takes back what it registered -
     * otherwise the next one would find an identifier that is already taken.
     */
    @AfterEach
    fun tearDown() {
        ProjectPartRegistry.unregister(NOTES)
        ProjectPartRegistry.unregister(OUTLINE)
    }

    /**
     * Use case: a plugin announces the part it brings along, so the storage finds the class behind the
     * identifier of that part when a document is read.
     */
    @Test
    fun findsARegisteredPartByItsIdentifier() {
        ProjectPartRegistry.register(NotesPart::class)

        assertSame(NotesPart::class, ProjectPartRegistry.partClassOf(NOTES))
    }

    /**
     * Use case: a document carries an entry of a plugin that is not installed here, so the registry
     * answers with nothing and the storage skips that entry instead of failing.
     */
    @Test
    fun answersWithNothingForAnUnknownIdentifier() {
        assertNull(ProjectPartRegistry.partClassOf(NOTES))
    }

    /**
     * Use case: the same plugin is loaded twice, so registering its part again changes nothing instead
     * of reporting a conflict with itself.
     */
    @Test
    fun registersTheSamePartTwiceWithoutComplaining() {
        ProjectPartRegistry.register(NotesPart::class)
        ProjectPartRegistry.register(NotesPart::class)

        assertSame(NotesPart::class, ProjectPartRegistry.partClassOf(NOTES))
    }

    /**
     * Use case: two plugins claim the same identifier, so the second one is refused - a document would
     * otherwise be read into the wrong class.
     */
    @Test
    fun refusesAnIdentifierThatIsAlreadyTaken() {
        ProjectPartRegistry.register(NotesPart::class)

        assertThrows<IllegalStateException> { ProjectPartRegistry.register(OtherNotesPart::class) }

        assertSame(NotesPart::class, ProjectPartRegistry.partClassOf(NOTES))
    }

    /**
     * Use case: a plugin author forgets the annotation on the part, so registering it is refused right
     * away instead of the part turning up under a name nobody expects.
     */
    @Test
    fun refusesAPartWithoutItsMetaData() {
        assertThrows<IllegalArgumentException> { ProjectPartRegistry.register(UnnamedPart::class) }
    }

    /**
     * Use case: a plugin is switched off, so its part is taken out of the registry and a document is
     * no longer read into a class that is not there any more.
     */
    @Test
    fun releasesAnIdentifierAgain() {
        ProjectPartRegistry.register(NotesPart::class)

        assertTrue(ProjectPartRegistry.unregister(NOTES))

        assertNull(ProjectPartRegistry.partClassOf(NOTES))
        assertFalse(ProjectPartRegistry.unregister(NOTES))
    }

    /**
     * Use case: the storage writes the parts of a project, so it asks for the identifier of every part
     * class - whether that class was registered or not.
     */
    @Test
    fun namesAPartByTheIdentifierItDeclares() {
        assertEquals(NOTES, ProjectPartRegistry.identifierOf(NotesPart::class))
        assertEquals("UnnamedPart", ProjectPartRegistry.identifierOf(UnnamedPart::class))
    }

    /**
     * Use case: a view lists what the application can read, so it asks the registry for everything
     * registered at that moment.
     */
    @Test
    fun listsEverythingItHolds() {
        ProjectPartRegistry.register(NotesPart::class)
        ProjectPartRegistry.register(OutlinePart::class)

        val registered = ProjectPartRegistry.registered

        assertSame(NotesPart::class, registered[NOTES])
        assertSame(OutlinePart::class, registered[OUTLINE])
    }

    /**
     * Use case: a caller keeps the listing it was given while a plugin is loaded, so that listing is a
     * snapshot and does not change under its hands.
     */
    @Test
    fun handsOutTheListingAsASnapshot() {
        val registered = ProjectPartRegistry.registered

        ProjectPartRegistry.register(NotesPart::class)

        assertNull(registered[NOTES])
    }

    private companion object {
        const val NOTES = "notes"
        const val OUTLINE = "outline"
    }

    /** A part of a plugin, standing in for what such a plugin brings along. */
    @ProjectPartInfo(identifier = NOTES)
    private class NotesPart : ProjectPart {
        override val version: Int = 1
    }

    /** Another part claiming the identifier of [NotesPart]. */
    @ProjectPartInfo(identifier = NOTES)
    private class OtherNotesPart : ProjectPart {
        override val version: Int = 1
    }

    /** A part of a plugin under an identifier of its own. */
    @ProjectPartInfo(identifier = OUTLINE)
    private class OutlinePart : ProjectPart {
        override val version: Int = 1
    }

    /** A part whose author forgot to declare its identifier. */
    private class UnnamedPart : ProjectPart {
        override val version: Int = 1
    }
}

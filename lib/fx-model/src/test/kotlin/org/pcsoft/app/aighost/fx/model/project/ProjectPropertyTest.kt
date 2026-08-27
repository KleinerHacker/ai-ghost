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

package org.pcsoft.app.aighost.fx.model.project

import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.pcsoft.app.aighost.fx.model.ChangeRecorder
import org.pcsoft.app.aighost.fx.model.project.book.BookProperty
import org.pcsoft.app.aighost.fx.model.project.design.DesignProperty
import org.pcsoft.app.aighost.fx.model.project.meta.MetaProperty
import org.pcsoft.app.aighost.fx.model.property.common.OverrideStringProperty
import org.pcsoft.app.aighost.model.common.Alignment
import org.pcsoft.app.aighost.model.common.FontData
import org.pcsoft.app.aighost.model.common.StyleData
import org.pcsoft.app.aighost.model.project.Project
import org.pcsoft.app.aighost.model.project.book.Book
import org.pcsoft.app.aighost.model.project.book.Chapter
import org.pcsoft.app.aighost.model.project.book.Prolog
import org.pcsoft.app.aighost.model.project.design.Design
import org.pcsoft.app.aighost.model.project.design.TextDesign
import org.pcsoft.app.aighost.model.project.meta.Meta
import org.pcsoft.app.aighost.plugin.api.model.project.ProjectPart

/**
 * Developer tests for [ProjectProperty].
 *
 * The property holds the open project and offers every part of it - and every field of the objects
 * nested in those parts - as a property of its own. The tests watch the whole tree at once: the
 * project itself, its three standard parts, a part a plugin attached and a field deep inside each of
 * them, so a change that fails to travel through one of the levels is named by the assertion.
 */
class ProjectPropertyTest {

    private lateinit var project: Project
    private lateinit var property: ProjectProperty
    private lateinit var metaProperty: MetaProperty
    private lateinit var designProperty: DesignProperty
    private lateinit var bookProperty: BookProperty
    private lateinit var notesProperty: NotesProperty
    private lateinit var recorder: ChangeRecorder

    @BeforeEach
    fun setUp() {
        project = newProject()
        property = ProjectProperty(project)

        metaProperty = property.metaProperty as MetaProperty
        designProperty = property.designProperty as DesignProperty
        bookProperty = property.bookProperty as BookProperty
        notesProperty = property.attachPart(NOTES, NotesPart::class, ::NotesProperty)

        recorder = ChangeRecorder()
        recorder.watch("project", property)
        recorder.watch("project.meta", metaProperty)
        recorder.watch("project.meta.name", metaProperty.nameProperty)
        recorder.watch("project.meta.author", metaProperty.authorProperty)
        recorder.watch("project.design", designProperty)
        recorder.watch("project.design.startWithEmptyPage", designProperty.startWithEmptyPageProperty)
        recorder.watch(
            "project.design.text.style.font.size",
            designProperty.textDesignProperty.styleProperty.fontProperty.sizeProperty
        )
        recorder.watch("project.book", bookProperty)
        recorder.watch("project.book.title", bookProperty.titleProperty)
        recorder.watch("project.book.chapters", bookProperty.chaptersProperty)
        recorder.watch("project.book.prolog", bookProperty.prologProperty)
        recorder.watch("project.book.prolog.title", bookProperty.prologProperty.titleProperty)
        recorder.watch("project.notes", notesProperty)
        recorder.watch("project.notes.note", notesProperty.noteProperty)
    }

    /** The project every test starts from, built fresh so no test sees the objects of another. */
    private fun newProject(): Project = Project(
        meta = Meta(name = "My Novel", author = "Jane Doe", copyright = "(c) 2026 Jane Doe"),
        design = Design(
            textDesign = TextDesign(StyleData(FontData("Text Serif", 11), Alignment.BLOCK)),
            startWithEmptyPage = true,
            endWithEmptyPage = false
        ),
        book = Book(
            title = "My Novel",
            prolog = Prolog("Before It All", paragraph = listOf("Long before.")),
            chapters = listOf(Chapter("first", "The First Part"))
        ),
        extensionParts = mapOf(NOTES to NotesPart(note = "Written by a plugin"))
    )

    /** Another project, different from [newProject] in every part and in every watched field. */
    private fun otherProject(): Project = Project(
        meta = Meta(name = "Other Novel", author = "John Doe"),
        design = Design(
            textDesign = TextDesign(StyleData(FontData("Other Serif", 12), Alignment.LEFT)),
            startWithEmptyPage = false
        ),
        book = Book(
            title = "Other Novel",
            prolog = Prolog("Another Start"),
            chapters = listOf(Chapter("only", "The Only Part"))
        ),
        extensionParts = mapOf(NOTES to NotesPart(note = "Written elsewhere"))
    )

    /**
     * Use case: a view is built for the open project, so every part property already answers with the
     * part the project carries instead of staying empty until the project is exchanged once.
     */
    @Test
    fun readsEveryPartOfTheProjectRightAway() {
        assertSame(project.meta, property.metaProperty.get())
        assertSame(project.design, property.designProperty.get())
        assertSame(project.book, property.bookProperty.get())
        assertEquals("My Novel", property.nameProperty.get())
        assertEquals("Jane Doe", property.authorProperty.get())
    }

    /**
     * Use case: a plugin builds its view for the open project, so the property it attached already
     * answers with the part of that project and with every field of it.
     */
    @Test
    fun readsTheAttachedPartOfTheProjectRightAway() {
        assertSame(project.part(NOTES), notesProperty.get())
        assertEquals("Written by a plugin", notesProperty.noteProperty.get())
    }

    /**
     * Use case: the user renames the project in a text field, so the name reaches the meta part of
     * the project and every property between that field and the root reports the change.
     */
    @Test
    fun writingTheNameReachesTheProject() {
        property.nameProperty.set("Renamed")

        assertEquals("Renamed", project.meta.name)
        assertEquals(1, recorder.countOf("project.meta.name"))
        assertEquals(1, recorder.countOf("project.meta"))
        assertEquals(1, recorder.countOf("project"))
    }

    /**
     * Use case: the user edits the heading of the prolog, so the value travels through book and
     * prolog into the project and every level of that path reports the change.
     */
    @Test
    fun writingDeepInsideTheBookReachesTheProject() {
        bookProperty.prologProperty.titleProperty.set("A New Start")

        assertEquals("A New Start", project.book.prolog?.title)
        assertEquals(1, recorder.countOf("project.book.prolog.title"))
        assertEquals(1, recorder.countOf("project.book.prolog"))
        assertEquals(1, recorder.countOf("project.book"))
        assertEquals(1, recorder.countOf("project"))
    }

    /**
     * Use case: the user changes the size of the body text, so the value travels through design and
     * text design into the project and every level of that path reports the change.
     */
    @Test
    fun writingDeepInsideTheDesignReachesTheProject() {
        designProperty.textDesignProperty.styleProperty.fontProperty.sizeProperty.set(13)

        assertEquals(13, project.design.textDesign.style.font.size)
        assertEquals(1, recorder.countOf("project.design.text.style.font.size"))
        assertEquals(1, recorder.countOf("project.design"))
        assertEquals(1, recorder.countOf("project"))
    }

    /**
     * Use case: the user edits a field of the view a plugin brought along, so the value travels
     * through the part of that plugin into the project and every level of that path reports it.
     */
    @Test
    fun writingDeepInsideTheAttachedPartReachesTheProject() {
        notesProperty.noteProperty.set("Rewritten")

        assertEquals("Rewritten", (project.part(NOTES) as NotesPart).note)
        assertEquals(1, recorder.countOf("project.notes.note"))
        assertEquals(1, recorder.countOf("project.notes"))
        assertEquals(1, recorder.countOf("project"))
    }

    /**
     * Use case: the view of a plugin hands a whole new part back, so it replaces the part of that
     * plugin in the project while every other part stays where it is.
     */
    @Test
    fun writingTheWholeAttachedPartReachesTheProject() {
        val book = project.book

        notesProperty.set(NotesPart(note = "Replaced"))

        assertEquals(NotesPart(note = "Replaced"), project.part(NOTES))
        assertSame(book, project.book)
        assertEquals(1, recorder.countOf("project"))
    }

    /**
     * Use case: a plugin takes its part out of the project through the property it attached, so the
     * part is gone from the document and the property answers with nothing.
     */
    @Test
    fun clearingTheAttachedPartRemovesItFromTheProject() {
        notesProperty.set(null)

        assertNull(project.part(NOTES))
        assertEquals(emptyMap<String, ProjectPart>(), project.extensionParts)
        assertNull(notesProperty.get())
    }

    /**
     * Use case: a text field of the user interface is bound to the project name, so what the user
     * types arrives in the project through the binding without any code writing it there.
     */
    @Test
    fun writingThroughABindingReachesTheProject() {
        val input = SimpleStringProperty("Bound Name")
        property.nameProperty.bind(input)

        assertEquals("Bound Name", project.meta.name)

        input.set("Another Name")

        assertEquals("Another Name", project.meta.name)

        property.nameProperty.unbind()
    }

    /**
     * Use case: a control of a plugin is bound to the part of that plugin, so what the user enters
     * arrives in the project through the binding without any code writing it there.
     */
    @Test
    fun writingThroughABindingOnTheAttachedPartReachesTheProject() {
        val input = SimpleObjectProperty<NotesPart?>(NotesPart(note = "Bound Note"))
        notesProperty.bind(input)

        assertEquals(NotesPart(note = "Bound Note"), project.part(NOTES))

        input.set(NotesPart(note = "Another Note"))

        assertEquals(NotesPart(note = "Another Note"), project.part(NOTES))

        notesProperty.unbind()
    }

    /**
     * Use case: the design dialog hands a whole new design back, so it replaces the design part of
     * the project while the other parts stay exactly where they are.
     */
    @Test
    fun writingAWholePartReplacesOnlyThatPart() {
        val book = project.book

        property.designProperty.set(Design(startWithEmptyPage = false, endWithEmptyPage = false))

        assertFalse(project.design.startWithEmptyPage)
        assertSame(book, project.book)
        assertEquals("My Novel", project.meta.name)
        assertEquals(1, recorder.countOf("project.design.startWithEmptyPage"))
        assertEquals(1, recorder.countOf("project"))
    }

    /**
     * Use case: a plugin stored a part of its own in the project, so writing a standard part keeps
     * that part instead of dropping it from the document.
     */
    @Test
    fun keepsPartsOfOtherOrigin() {
        val custom = object : ProjectPart {
            override val version: Int = 1
        }
        project.putPart("custom", custom)

        property.nameProperty.set("Renamed")

        assertSame(custom, project.part("custom"))
    }

    /**
     * Use case: a plugin asks for a property of a part the application ships with, so it is sent to
     * the property that already exists for it instead of a second one being built beside it.
     */
    @Test
    fun refusesToAttachAStandardPart() {
        assertThrows<IllegalArgumentException> {
            property.attachPart(Project.PART_META, Meta::class)
        }
    }

    /**
     * Use case: the same plugin is loaded twice, so attaching its part a second time is reported
     * instead of two properties writing into the same part of the project.
     */
    @Test
    fun refusesToAttachTheSamePartTwice() {
        assertThrows<IllegalStateException> {
            property.attachPart(NOTES, NotesPart::class, ::NotesProperty)
        }
    }

    /**
     * Use case: a plugin does not need a property per field of its part, so it attaches the part as a
     * whole and still writes it into the project.
     */
    @Test
    fun attachesAPartWithoutFieldProperties() {
        val plain = property.attachPart(OUTLINE, OutlinePart::class)

        plain.set(OutlinePart(headline = "Three acts"))

        assertEquals(OutlinePart(headline = "Three acts"), project.part(OUTLINE))
        assertEquals(OutlinePart(headline = "Three acts"), plain.get())
    }

    /**
     * Use case: another project file is opened, so every property of the tree takes over the value of
     * the new project and reports it, down to the fields nested in its parts and in the part a plugin
     * attached.
     */
    @Test
    fun openingAnotherProjectUpdatesTheWholeTree() {
        recorder.reset()

        property.set(otherProject())

        assertEquals("Other Novel", property.nameProperty.get())
        assertEquals("Another Start", bookProperty.prologProperty.titleProperty.get())
        assertEquals(12, designProperty.textDesignProperty.styleProperty.fontProperty.sizeProperty.get())
        assertEquals("Written elsewhere", notesProperty.noteProperty.get())
        recorder.assertAllFired("opening another project")
    }

    /**
     * Use case: a project is opened again without having been changed, so an exchange against an
     * equal project leaves every property quiet instead of redrawing the whole user interface.
     */
    @Test
    fun openingAnEqualProjectStaysQuiet() {
        recorder.reset()

        property.set(newProject())

        recorder.assertNoneFired("opening an equal project")
    }

    /**
     * Use case: a plugin is switched off, so the property it attached stops reporting what the open
     * project carries while the properties of the standard parts keep working.
     */
    @Test
    fun detachingAPartStopsReportingTheProject() {
        assertTrue(property.detachPart(NOTES))
        recorder.reset()

        property.set(otherProject())

        assertEquals(0, recorder.countOf("project.notes"))
        assertEquals(0, recorder.countOf("project.notes.note"))
        assertEquals(1, recorder.countOf("project.meta"))
        assertFalse(property.detachPart(NOTES))
        assertNull(property.attachedPart(NOTES))
    }

    /**
     * Use case: a document lost a part because an older version wrote it, so the part property
     * answers with the defaults of that part instead of failing when a view reads it.
     */
    @Test
    fun fallsBackToDefaultsForAMissingPart() {
        property.set(Project.fromParts(emptyMap()))

        assertEquals(Meta(), property.metaProperty.get())
        assertEquals(Design(), property.designProperty.get())
        assertEquals(Book(), property.bookProperty.get())
        assertNull(notesProperty.get())
    }

    private companion object {
        const val NOTES = "notes"
        const val OUTLINE = "outline"
    }

    /** A part of a plugin, standing in for what such a plugin puts into a project. */
    data class NotesPart(
        override val version: Int = 1,
        var note: String = ""
    ) : ProjectPart

    /** A part of a plugin that is watched as a whole, without a property per field. */
    data class OutlinePart(
        override val version: Int = 1,
        var headline: String = ""
    ) : ProjectPart

    /**
     * The property model a plugin builds for its own part: it derives from [ProjectPartProperty] and
     * offers the field of the part as a property of its own, exactly like the models of the parts the
     * application ships with.
     */
    class NotesProperty(
        setter: (NotesPart?) -> Unit,
        getter: () -> NotesPart?,
        fireEvent: () -> Unit
    ) : ProjectPartProperty<NotesPart>(setter, getter, fireEvent) {

        /** The note the part carries, as a property of its own. */
        val noteProperty: OverrideStringProperty = OverrideStringProperty(
            { newValue -> value?.also { it.note = newValue ?: "" } },
            { value?.note },
            { fireValueChangedEvent() }
        )

        override fun invalidated() {
            super.invalidated()
            noteProperty.refresh()
        }

        override fun refresh() {
            super.refresh()
            noteProperty.refresh()
        }
    }
}

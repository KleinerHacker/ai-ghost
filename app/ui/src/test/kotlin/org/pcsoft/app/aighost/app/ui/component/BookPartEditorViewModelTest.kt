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

package org.pcsoft.app.aighost.app.ui.component

import javafx.beans.property.SimpleObjectProperty
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.pcsoft.app.aighost.app.controller.IoController
import org.pcsoft.app.aighost.app.controller.PartMode
import org.pcsoft.app.aighost.app.undo.UndoStack
import org.pcsoft.app.aighost.fx.model.project.ProjectProperty
import org.pcsoft.app.aighost.model.project.Project
import org.pcsoft.app.aighost.model.project.book.Blurb
import org.pcsoft.app.aighost.model.project.book.Book
import org.pcsoft.app.aighost.model.project.book.Chapter
import org.pcsoft.app.aighost.model.project.book.Epilog
import org.pcsoft.app.aighost.model.project.book.Prolog

/**
 * Developer tests for [BookPartEditorViewModel] - the routing of a picked tree node onto the mode of
 * the sheet and the configuration taken from the preferences.
 *
 * These tests never hand a [org.pcsoft.app.aighost.layouting.fx.paper.PaperFlowView] to the view
 * model, so no layout is computed; the parts that need the real control are covered by
 * [BookPartEditorTest], and the pure routing and assembly logic by
 * [org.pcsoft.app.aighost.app.controller.BookPartEditorControllerTest].
 */
class BookPartEditorViewModelTest {

    private lateinit var viewModel: BookPartEditorViewModel
    private lateinit var project: ProjectProperty
    private lateinit var selection: SimpleObjectProperty<ProjectListItem?>

    private var originalPause: Long = 0

    @BeforeEach
    fun setUp() {
        originalPause = IoController.preferences.editorProperty.paragraphMergePauseMillis
        viewModel = BookPartEditorViewModel()
        project = ProjectProperty(
            Project(
                book = Book(
                    title = "My Novel",
                    prolog = Prolog(title = "Before It All"),
                    chapters = listOf(Chapter("first", "The First Part")),
                    epilog = Epilog(title = "After It All"),
                    blurb = Blurb(paragraph = listOf("A gripping tale."))
                )
            )
        )
        selection = SimpleObjectProperty(null)
    }

    @AfterEach
    fun tearDown() {
        IoController.preferences.editorProperty.paragraphMergePauseMillis = originalPause
    }

    /**
     * Use case: the editor comes up before anything is picked, so the sheet has nothing to show and
     * offers no writing.
     */
    @Test
    fun startsWithNothingToWrite() {
        assertEquals(PartMode.NONE, viewModel.mode.value)
        assertFalse(viewModel.contentAvailable.get())
        assertFalse(viewModel.editable.get())
    }

    /**
     * Use case: the user picks the prolog, so the sheet switches to a writable prose flow.
     */
    @Test
    fun showsAWritableFlowForTheProlog() {
        viewModel.bindProject(project)
        viewModel.bindSelection(selection)

        selection.value = ProjectListItem.PrologItem(project.value.book.prolog)

        assertEquals(PartMode.BOOK_PART, viewModel.mode.value)
        assertTrue(viewModel.contentAvailable.get())
        assertTrue(viewModel.editable.get())
    }

    /**
     * Use case: the user picks a chapter, so the sheet switches to a writable prose flow as well.
     */
    @Test
    fun showsAWritableFlowForAChapter() {
        viewModel.bindProject(project)
        viewModel.bindSelection(selection)

        selection.value = ProjectListItem.ChapterItem(project.value.book.chapters.single())

        assertEquals(PartMode.BOOK_PART, viewModel.mode.value)
        assertTrue(viewModel.editable.get())
    }

    /**
     * Use case: the user picks the title page, so the sheet shows it but does not let it be written.
     */
    @Test
    fun showsAReadOnlySheetForTheTitlePage() {
        viewModel.bindProject(project)
        viewModel.bindSelection(selection)

        selection.value = ProjectListItem.TitlePageItem

        assertEquals(PartMode.TITLE_PAGE, viewModel.mode.value)
        assertTrue(viewModel.contentAvailable.get())
        assertFalse(viewModel.editable.get())
    }

    /**
     * Use case: the user picks the copyright page, so the sheet shows it read only, the same way as
     * the title page.
     */
    @Test
    fun showsAReadOnlySheetForTheCopyrightPage() {
        viewModel.bindProject(project)
        viewModel.bindSelection(selection)

        selection.value = ProjectListItem.CopyrightPageItem

        assertEquals(PartMode.COPYRIGHT_PAGE, viewModel.mode.value)
        assertFalse(viewModel.editable.get())
    }

    /**
     * Use case: the user picks the blurb, so the sheet switches to a writable flow that carries no
     * heading.
     */
    @Test
    fun showsAHeadinglessFlowForTheBlurb() {
        viewModel.bindProject(project)
        viewModel.bindSelection(selection)

        selection.value = ProjectListItem.BlurbItem(project.value.book.blurb)

        assertEquals(PartMode.BLURB, viewModel.mode.value)
        assertTrue(viewModel.editable.get())
    }

    /**
     * Use case: the user picks a structural node such as the chapters branch, so the sheet shows
     * nothing to write.
     */
    @Test
    fun showsNothingForAStructuralNode() {
        viewModel.bindProject(project)
        viewModel.bindSelection(selection)

        selection.value = ProjectListItem.Chapters

        assertEquals(PartMode.NONE, viewModel.mode.value)
        assertFalse(viewModel.contentAvailable.get())
    }

    /**
     * Use case: no project is open, so picking a part still leaves the sheet with nothing to show.
     */
    @Test
    fun fallsBackToNothingWithoutAProject() {
        viewModel.bindProject(null)
        viewModel.bindSelection(selection)

        selection.value = ProjectListItem.PrologItem(null)

        assertEquals(PartMode.NONE, viewModel.mode.value)
    }

    /**
     * Use case: the selection changes after the editor was already bound, so the sheet follows the
     * newly picked node.
     */
    @Test
    fun followsALaterSelectionChange() {
        viewModel.bindProject(project)
        viewModel.bindSelection(selection)
        selection.value = ProjectListItem.PrologItem(project.value.book.prolog)

        selection.value = ProjectListItem.TitlePageItem

        assertEquals(PartMode.TITLE_PAGE, viewModel.mode.value)
    }

    /**
     * Use case: the undo history is handed over, so the writing surface configures its merge timeout
     * from the typing pause the user set in the preferences.
     */
    @Test
    fun takesTheTypingPauseFromThePreferences() {
        IoController.preferences.editorProperty.paragraphMergePauseMillis = 450
        val stack = UndoStack()

        viewModel.bindUndoStack(stack)

        assertEquals(450, stack.mergeTimeoutMillis)
    }
}

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

import de.saxsys.mvvmfx.MvvmFX
import javafx.beans.property.SimpleObjectProperty
import javafx.scene.Scene
import javafx.scene.control.TextArea
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyEvent
import javafx.stage.Stage
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.pcsoft.app.aighost.app.Messages
import org.pcsoft.app.aighost.app.undo.UndoStack
import org.pcsoft.app.aighost.fx.model.project.ProjectProperty
import org.pcsoft.app.aighost.model.common.Alignment
import org.pcsoft.app.aighost.model.common.FontData
import org.pcsoft.app.aighost.model.common.StyleData
import org.pcsoft.app.aighost.model.project.Project
import org.pcsoft.app.aighost.model.project.book.Book
import org.pcsoft.app.aighost.model.project.book.Chapter
import org.pcsoft.app.aighost.model.project.book.Epilog
import org.pcsoft.app.aighost.model.project.book.Prolog
import org.pcsoft.app.aighost.model.project.design.ChapterPageDesign
import org.pcsoft.app.aighost.model.project.design.CopyrightPageDesign
import org.pcsoft.app.aighost.model.project.design.Design
import org.pcsoft.app.aighost.model.project.design.EpilogPageDesign
import org.pcsoft.app.aighost.model.project.design.BlurbPageDesign
import org.pcsoft.app.aighost.model.project.design.PrologPageDesign
import org.pcsoft.app.aighost.model.project.design.TitlePageDesign
import org.pcsoft.app.aighost.model.project.meta.Meta
import org.testfx.framework.junit5.ApplicationTest
import org.testfx.util.WaitForAsyncUtils
import java.util.Locale
import java.util.ResourceBundle

/**
 * Developer tests for [BookPartEditor] - binding, typing on the sheet, undo and the read-only front
 * matter, all headless.
 */
class BookPartEditorTest : ApplicationTest() {

    private lateinit var editor: BookPartEditor
    private lateinit var projectModel: ProjectProperty
    private lateinit var selection: SimpleObjectProperty<ProjectListItem?>
    private lateinit var undoStack: UndoStack

    override fun start(stage: Stage) {
        MvvmFX.setGlobalResourceBundle(
            ResourceBundle.getBundle(
                Messages.BUNDLE_NAME,
                Locale.ROOT,
                ResourceBundle.Control.getNoFallbackControl(ResourceBundle.Control.FORMAT_PROPERTIES)
            )
        )

        projectModel = ProjectProperty(project())
        selection = SimpleObjectProperty(null)
        undoStack = UndoStack()

        editor = BookPartEditor()
        editor.bindProject(projectModel)
        editor.bindUndoStack(undoStack)
        editor.bindSelection(selection)

        stage.scene = Scene(editor, 700.0, 600.0)
        stage.show()
    }

    private fun style(size: Int = 12): StyleData =
        StyleData(font = FontData("Serif", size, bold = false, italic = false), alignment = Alignment.LEFT)

    private fun project(): Project = Project(
        meta = Meta(name = "My Novel", author = "Jane Doe"),
        design = Design(
            titlePage = TitlePageDesign(style(), style(), showAuthor = true, authorStyle = style()),
            copyrightPage = CopyrightPageDesign(style(), style(), showAuthor = false, authorStyle = style()),
            prologPage = PrologPageDesign(style(), style(), style()),
            blurbPage = BlurbPageDesign(style()),
            chapterPage = ChapterPageDesign(style(), style(), style()),
            epilogPage = EpilogPageDesign(style(), style(), style()),
            startWithEmptyPage = false,
            endWithEmptyPage = false
        ),
        book = Book(
            title = "My Novel",
            prolog = Prolog(paragraph = listOf("The first paragraph.")),
            chapters = listOf(Chapter("first", "The First Part")),
            epilog = Epilog()
        )
    )

    private val prolog: Prolog get() = projectModel.value.book.prolog

    private fun blocks(): List<TextArea> =
        editor.lookupAll(".paper-flow-view-block").filterIsInstance<TextArea>()

    private fun select(item: ProjectListItem?) {
        interact { selection.value = item }
        WaitForAsyncUtils.waitForFxEvents()
        interact { editor.scene.root.layout() }
        WaitForAsyncUtils.waitForFxEvents()
    }

    /**
     * Use case: nothing is picked, so the sheet shows its hint instead of a writing surface.
     */
    @Test
    fun showsAHintWhileNothingIsPicked() {
        assertTrue(blocks().isEmpty(), "no writing surface while nothing is picked")
    }

    /**
     * Use case: the user picks the prolog, so its paragraph turns up on the sheet as an editable text
     * control.
     */
    @Test
    fun opensThePrologTextOnTheSheet() {
        select(ProjectListItem.PrologItem(prolog))

        assertTrue(
            blocks().any { it.text == "The first paragraph." },
            "the prolog paragraph must be shown on the sheet"
        )
    }

    /**
     * Use case: the user types into a paragraph on the sheet, so the new text lands in the model of
     * the part.
     */
    @Test
    fun writesEveryKeystrokeIntoTheModel() {
        select(ProjectListItem.PrologItem(prolog))
        val area = blocks().first { it.text == "The first paragraph." }

        interact { area.text = "The first paragraph, extended." }
        WaitForAsyncUtils.waitForFxEvents()

        assertEquals(listOf("The first paragraph, extended."), prolog.paragraph)
    }

    /**
     * Use case: the user undoes a text change on the sheet, so the paragraph returns to what it was.
     */
    @Test
    fun undoesATextChange() {
        select(ProjectListItem.PrologItem(prolog))
        val area = blocks().first { it.text == "The first paragraph." }

        interact { area.text = "A rewritten paragraph." }
        WaitForAsyncUtils.waitForFxEvents()
        assertTrue(undoStack.canUndoProperty.get(), "a text change must be undoable")

        interact { undoStack.undo() }
        WaitForAsyncUtils.waitForFxEvents()

        assertEquals(listOf("The first paragraph."), prolog.paragraph)
    }

    /**
     * Use case: a design value changes while the prolog is open, so the sheet is laid out again and
     * the caret keeps its place inside the paragraph.
     */
    @Test
    fun keepsTheCaretAcrossADesignChange() {
        select(ProjectListItem.PrologItem(prolog))
        val area = blocks().first { it.text == "The first paragraph." }
        interact {
            area.requestFocus()
            area.positionCaret(4)
        }
        WaitForAsyncUtils.waitForFxEvents()

        interact {
            projectModel.value.design.prologPage = PrologPageDesign(style(), style(), style(size = 16))
            projectModel.designProperty.refresh()
        }
        WaitForAsyncUtils.waitForFxEvents()
        interact { editor.scene.root.layout() }
        WaitForAsyncUtils.waitForFxEvents()

        val current = blocks().first { it.text == "The first paragraph." }
        assertEquals(4, current.caretPosition, "the caret keeps its offset across the restyling")
    }

    /**
     * Use case: the user picks the title page, so it is shown but a typed change is discarded instead
     * of reaching the model.
     */
    @Test
    fun showsTheTitlePageReadOnly() {
        select(ProjectListItem.TitlePageItem)
        val area = blocks().firstOrNull { it.text == "My Novel" }
        assertTrue(area != null, "the title page is rendered on the sheet")

        interact { area!!.text = "A Different Title" }
        WaitForAsyncUtils.waitForFxEvents()

        assertEquals("My Novel", projectModel.value.book.title, "the title page must not be writable")
        assertFalse(undoStack.canUndoProperty.get(), "a read-only sheet records no undo entry")
    }

    /**
     * Use case: the user presses Enter in the middle of a paragraph, so it splits into two paragraphs
     * and the caret lands at the start of the new second one; undoing restores the single paragraph.
     */
    @Test
    fun splitsAParagraphAtTheCaretAndPlacesCaretAtTheNewParagraph() {
        select(ProjectListItem.PrologItem(prolog))
        val area = blocks().first { it.text == "The first paragraph." }
        interact {
            area.requestFocus()
            area.positionCaret(10)
        }
        WaitForAsyncUtils.waitForFxEvents()

        interact { area.fireEvent(keyPressed(KeyCode.ENTER)) }
        WaitForAsyncUtils.waitForFxEvents()

        assertEquals(listOf("The first ", "paragraph."), prolog.paragraph)
        assertTrue(undoStack.canUndoProperty.get(), "a split must be undoable")
        val newArea = blocks().first { it.text == "paragraph." }
        assertTrue(newArea.isFocused, "the caret must land in the new second paragraph")
        assertEquals(0, newArea.caretPosition, "the caret must sit at the start of the new paragraph")

        interact { undoStack.undo() }
        WaitForAsyncUtils.waitForFxEvents()
        assertEquals(listOf("The first paragraph."), prolog.paragraph)
    }

    /**
     * Use case: the user presses Backspace at the start of a paragraph, so it merges with the previous
     * one and the caret lands at the former paragraph boundary; undoing restores both paragraphs.
     */
    @Test
    fun mergesWithPreviousAndPlacesCaretAtTheFormerBoundary() {
        interact {
            projectModel.bookProperty.prologProperty.paragraphProperty
                .setAll(listOf("First paragraph.", "Second paragraph."))
        }
        select(ProjectListItem.PrologItem(prolog))
        val area = blocks().first { it.text == "Second paragraph." }
        interact {
            area.requestFocus()
            area.positionCaret(0)
        }
        WaitForAsyncUtils.waitForFxEvents()

        interact { area.fireEvent(keyPressed(KeyCode.BACK_SPACE)) }
        WaitForAsyncUtils.waitForFxEvents()

        assertEquals(listOf("First paragraph.Second paragraph."), prolog.paragraph)
        assertTrue(undoStack.canUndoProperty.get(), "a merge must be undoable")
        val mergedArea = blocks().first { it.text == "First paragraph.Second paragraph." }
        assertTrue(mergedArea.isFocused, "the caret must stay on the merged paragraph")
        assertEquals(16, mergedArea.caretPosition, "the caret must sit at the former paragraph boundary")

        interact { undoStack.undo() }
        WaitForAsyncUtils.waitForFxEvents()
        assertEquals(listOf("First paragraph.", "Second paragraph."), prolog.paragraph)
    }

    /**
     * Use case: the user moves a paragraph down with Ctrl+Shift+Down, so it swaps places with its
     * neighbour and keeps the caret at the same offset it carried; undoing restores the order.
     */
    @Test
    fun movesAParagraphDownAndKeepsItsCaretOffset() {
        interact {
            projectModel.bookProperty.prologProperty.paragraphProperty
                .setAll(listOf("First paragraph.", "Second paragraph."))
        }
        select(ProjectListItem.PrologItem(prolog))
        val area = blocks().first { it.text == "First paragraph." }
        interact {
            area.requestFocus()
            area.positionCaret(3)
        }
        WaitForAsyncUtils.waitForFxEvents()

        interact { area.fireEvent(keyPressed(KeyCode.DOWN, control = true, shift = true)) }
        WaitForAsyncUtils.waitForFxEvents()

        assertEquals(listOf("Second paragraph.", "First paragraph."), prolog.paragraph)
        assertTrue(undoStack.canUndoProperty.get(), "a move must be undoable")
        val movedArea = blocks().first { it.text == "First paragraph." }
        assertTrue(movedArea.isFocused, "the caret must stay on the moved paragraph")
        assertEquals(3, movedArea.caretPosition, "the caret must keep its offset after the move")

        interact { undoStack.undo() }
        WaitForAsyncUtils.waitForFxEvents()
        assertEquals(listOf("First paragraph.", "Second paragraph."), prolog.paragraph)
    }

    /**
     * Use case: Backspace at the start of the very first paragraph has no previous paragraph to merge
     * with, so nothing changes and no undo entry is recorded.
     */
    @Test
    fun mergingAtTheFirstParagraphIsANoOp() {
        select(ProjectListItem.PrologItem(prolog))
        val area = blocks().first { it.text == "The first paragraph." }
        interact {
            area.requestFocus()
            area.positionCaret(0)
        }
        WaitForAsyncUtils.waitForFxEvents()

        interact { area.fireEvent(keyPressed(KeyCode.BACK_SPACE)) }
        WaitForAsyncUtils.waitForFxEvents()

        assertEquals(listOf("The first paragraph."), prolog.paragraph)
        assertFalse(undoStack.canUndoProperty.get(), "merging at the first paragraph must record no undo entry")
    }

    /**
     * Use case: the user types several characters in a row at the end of a paragraph, so each one
     * lands after the one typed before it - proving the fix for a defect where the caret was reset to
     * its pre-edit offset on every rebuild, so a fast typist saw every new character appear in front
     * of the previous one instead of after it.
     */
    @Test
    fun caretAdvancesWhileTypingSeveralCharactersInARow() {
        select(ProjectListItem.PrologItem(prolog))
        var area = blocks().first { it.text == "The first paragraph." }
        interact {
            area.requestFocus()
            area.positionCaret(area.text.length)
        }
        WaitForAsyncUtils.waitForFxEvents()

        for (character in "XYZ") {
            area = blocks().first { it.isFocused }
            write(character.toString())
            WaitForAsyncUtils.waitForFxEvents()
        }

        area = blocks().first { it.isFocused }
        assertEquals("The first paragraph.XYZ", area.text, "each character must land after the one typed before it")
        assertEquals(23, area.caretPosition, "the caret must sit right after the last character typed")
    }

    private fun keyPressed(code: KeyCode, control: Boolean = false, shift: Boolean = false) = KeyEvent(
        KeyEvent.KEY_PRESSED, "", "", code,
        shift, control, false, false
    )
}

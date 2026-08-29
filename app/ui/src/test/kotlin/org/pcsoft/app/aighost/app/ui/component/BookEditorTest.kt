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
import javafx.application.Platform
import javafx.scene.Node
import javafx.scene.Scene
import javafx.scene.control.Button
import javafx.scene.control.ButtonType
import javafx.scene.control.DialogPane
import javafx.scene.control.Label
import javafx.scene.control.TextArea
import javafx.scene.control.TextField
import javafx.scene.image.ImageView
import javafx.scene.layout.VBox
import javafx.stage.Stage
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.pcsoft.app.aighost.app.Messages
import org.pcsoft.app.aighost.app.ui.component.base.AiPromptArea
import org.pcsoft.app.aighost.app.ui.component.base.AiTextField
import org.pcsoft.app.aighost.app.ui.component.base.AiTextFieldListItem
import org.pcsoft.app.aighost.fx.model.project.ProjectProperty
import org.pcsoft.app.aighost.fx.model.project.book.BookProperty
import org.pcsoft.app.aighost.model.project.Project
import org.pcsoft.app.aighost.model.project.book.Book
import org.pcsoft.app.aighost.model.project.common.AIPrompt
import org.testfx.framework.junit5.ApplicationTest
import org.testfx.util.WaitForAsyncUtils
import java.util.Locale
import java.util.ResourceBundle
import java.util.concurrent.TimeUnit

/**
 * Developer tests for [BookEditor].
 *
 * The editor is bound to a manuscript of its own in every test, and both directions are proven on
 * the controls the user really works with: what is typed into a text field or a text area has to
 * stand in the model object afterwards, and what the model object carries has to stand in those
 * controls - no matter whether it was written through the property model or on the object itself.
 */
class BookEditorTest : ApplicationTest() {

    private lateinit var editor: BookEditor

    private val titleField: AiTextField
        get() = editor.lookup("#txtTitle") as AiTextField

    private val appendixRows: List<Node>
        get() = (editor.lookup(".ai-list-entries") as VBox).children.toList()

    private val addButton: Button
        get() = editor.lookup(".ai-list-add") as Button

    /** The hint standing in for the title lines while there is none. */
    private val appendixHint: Label
        get() = editor.lookup(".ai-list-empty") as Label

    /** The area the story prompt is written in. */
    private val storyPromptArea: AiPromptArea
        get() = editor.lookup("#txaStoryPrompt") as AiPromptArea

    /** The area the style prompt is written in. */
    private val stylePromptArea: AiPromptArea
        get() = editor.lookup("#txaStylePrompt") as AiPromptArea

    /** The input line the title is typed into. */
    private val titleInput: TextField
        get() = titleField.lookup(".text-field") as TextField

    /** The writing surface the content prompt is typed into. */
    private val contentPromptInput: TextArea
        get() = storyPromptArea.lookup(".text-area") as TextArea

    /** The writing surface the style prompt is typed into. */
    private val stylePromptInput: TextArea
        get() = stylePromptArea.lookup(".text-area") as TextArea

    override fun start(stage: Stage) {
        // No fallback, so the English base bundle is used no matter which locale the build runs under.
        MvvmFX.setGlobalResourceBundle(
            ResourceBundle.getBundle(
                Messages.BUNDLE_NAME,
                Locale.ROOT,
                ResourceBundle.Control.getNoFallbackControl(ResourceBundle.Control.FORMAT_PROPERTIES)
            )
        )

        editor = BookEditor()
        stage.scene = Scene(editor, 600.0, 700.0)
        stage.show()
    }

    /**
     * Builds a manuscript property of its own, so a test decides what the editor is handed.
     *
     * @param book the manuscript the property carries
     * @return the property of the manuscript
     */
    private fun bookPropertyOf(book: Book): BookProperty = ProjectProperty(Project(book = book)).bookProperty

    /**
     * Hands a manuscript to the editor and waits until it is shown.
     *
     * @param book the manuscript to edit
     * @return the property the editor was handed
     */
    private fun show(book: Book): BookProperty {
        val property = bookPropertyOf(book)
        interact { editor.bindBook(property) }
        WaitForAsyncUtils.waitForFxEvents()

        return property
    }

    /**
     * Use case: a manuscript is handed to the editor, so the title, one row per title line and both
     * prompts show what it carries.
     */
    @Test
    fun boundBookIsShown() {
        val book = Book(
            title = "The Silent House",
            titleAppendix = listOf("A ghost story", "Book one"),
            prompts = AIPrompt(contentPrompt = "A house nobody lives in", stylePrompt = "Dark and quiet")
        )

        show(book)

        assertEquals("The Silent House", titleInput.text)
        assertEquals(2, appendixRows.size)
        assertEquals("A ghost story", inputOfRow(0).text)
        assertEquals("Book one", inputOfRow(1).text)
        assertEquals("A house nobody lives in", contentPromptInput.text)
        assertEquals("Dark and quiet", stylePromptInput.text)
    }

    /**
     * Use case: the user types a title with the keyboard, so every keystroke travels through the
     * component into the model object.
     */
    @Test
    fun typedTitleReachesTheModel() {
        // A book carries a title from the start, so the field is emptied before it is typed into.
        val book = Book(title = "")
        val property = show(book)

        clickOn(titleInput).write("The Silent House")
        WaitForAsyncUtils.waitForFxEvents()

        assertEquals("The Silent House", book.title)
        assertEquals("The Silent House", property.title)
    }

    /**
     * Use case: the user writes into every field of the editor, so the model object carries the title,
     * the title lines and both prompts afterwards - the editor keeps no copy of its own.
     */
    @Test
    fun everyFieldWritesThroughToTheModel() {
        val book = Book()
        show(book)

        clickOn(addButton)
        WaitForAsyncUtils.waitForFxEvents()

        interact {
            titleInput.text = "The Silent House"
            inputOfRow(0).text = "A ghost story"
            contentPromptInput.text = "A house nobody lives in"
            stylePromptInput.text = "Dark and quiet"
        }
        WaitForAsyncUtils.waitForFxEvents()

        assertEquals("The Silent House", book.title)
        assertEquals(listOf("A ghost story"), book.titleAppendix)
        assertEquals("A house nobody lives in", book.prompts.contentPrompt)
        assertEquals("Dark and quiet", book.prompts.stylePrompt)
    }

    /**
     * Use case: the manuscript is written through its property model - by another editor of the same
     * project for instance - so every control of the editor shows the new values.
     */
    @Test
    fun everyFieldFollowsTheProperty() {
        val property = show(Book())

        interact {
            property.title = "The Silent House"
            property.promptsProperty.contentPrompt = "A house nobody lives in"
            property.promptsProperty.stylePrompt = "Dark and quiet"
            property.titleAppendix = listOf("A ghost story", "Book one")
        }
        WaitForAsyncUtils.waitForFxEvents()

        assertEquals("The Silent House", titleInput.text)
        assertEquals("A house nobody lives in", contentPromptInput.text)
        assertEquals("Dark and quiet", stylePromptInput.text)
        assertEquals(2, appendixRows.size)
        assertEquals("A ghost story", inputOfRow(0).text)
        assertEquals("Book one", inputOfRow(1).text)
    }

    /**
     * Use case: the model object itself is written past its property model - a project that was read
     * from a file for instance - so the editor shows those values as soon as the property model was
     * told to take them over.
     */
    @Test
    fun everyFieldFollowsTheModelObject() {
        val book = Book()
        val property = show(book)

        interact {
            book.title = "The Silent House"
            book.titleAppendix = listOf("A ghost story")
            book.prompts = AIPrompt(contentPrompt = "A house nobody lives in", stylePrompt = "Dark and quiet")
            property.refresh()
        }
        WaitForAsyncUtils.waitForFxEvents()

        assertEquals("The Silent House", titleInput.text)
        assertEquals("A house nobody lives in", contentPromptInput.text)
        assertEquals("Dark and quiet", stylePromptInput.text)
        assertEquals(1, appendixRows.size)
        assertEquals("A ghost story", inputOfRow(0).text)
    }

    /**
     * Use case: the manuscript is exchanged while the editor stands - another project was opened - so
     * the controls show the new manuscript and writing into them reaches that one only.
     */
    @Test
    fun exchangedBookIsShownAndWrittenTo() {
        val first = Book(title = "The Silent House", titleAppendix = listOf("A ghost story"))
        val second = Book(title = "The Open Door", titleAppendix = listOf("Book one"))

        show(first)
        show(second)

        assertEquals("The Open Door", titleInput.text)
        assertEquals(1, appendixRows.size)
        assertEquals("Book one", inputOfRow(0).text)

        interact {
            titleInput.text = "The Locked Door"
            inputOfRow(0).text = "Book two"
        }
        WaitForAsyncUtils.waitForFxEvents()

        assertEquals("The Locked Door", second.title)
        assertEquals(listOf("Book two"), second.titleAppendix)
        assertEquals("The Silent House", first.title)
        assertEquals(listOf("A ghost story"), first.titleAppendix)
    }

    /**
     * Use case: the user presses the plus, so another row stands below the last one and the manuscript
     * carries the line it is filled with.
     */
    @Test
    fun addedRowReachesTheModel() {
        val book = Book()
        show(book)

        clickOn(addButton)
        WaitForAsyncUtils.waitForFxEvents()
        assertEquals(1, appendixRows.size)
        assertEquals(listOf(""), book.titleAppendix)

        interact { inputOfRow(0).text = "A ghost story" }
        WaitForAsyncUtils.waitForFxEvents()

        assertEquals(listOf("A ghost story"), book.titleAppendix)
    }

    /**
     * Use case: the user presses the bin of a row and agrees to lose the line, so the row is gone,
     * the manuscript no longer carries it and the rows left behind still write to their own line.
     */
    @Test
    fun removedRowIsGoneAfterAgreement() {
        val book = Book(titleAppendix = listOf("A ghost story", "Book one"))
        show(book)

        val dialog = requestRemovalAndAwaitQuestion(0)
        assertEquals("Attention: the title line is removed!", dialog.headerText)
        answer(dialog, ButtonType.YES)

        assertEquals(1, appendixRows.size)
        assertEquals(listOf("Book one"), book.titleAppendix)

        interact { inputOfRow(0).text = "Book two" }
        WaitForAsyncUtils.waitForFxEvents()

        assertEquals(listOf("Book two"), book.titleAppendix)
    }

    /**
     * Use case: the user presses the bin of a row and refuses to lose the line, so everything stays
     * as it was.
     */
    @Test
    fun removedRowIsKeptAfterRefusal() {
        val book = Book(titleAppendix = listOf("A ghost story", "Book one"))
        show(book)

        answer(requestRemovalAndAwaitQuestion(0), ButtonType.NO)

        assertEquals(2, appendixRows.size)
        assertEquals(listOf("A ghost story", "Book one"), book.titleAppendix)
    }

    /**
     * Use case: a title line is taken out of the manuscript past the editor, so the row of that line
     * disappears and the remaining rows show the lines that are left.
     */
    @Test
    fun rowsFollowLinesRemovedOnTheModel() {
        val property = show(Book(titleAppendix = listOf("A ghost story", "Book one")))

        interact { property.titleAppendix = listOf("Book one") }
        WaitForAsyncUtils.waitForFxEvents()

        assertEquals(1, appendixRows.size)
        assertEquals("Book one", inputOfRow(0).text)
    }

    /**
     * Use case: a single title line is rewritten on the manuscript, so the row of that line shows the
     * new text while the line next to it is left as it is.
     */
    @Test
    fun lineRewrittenOnTheModelIsShown() {
        val book = Book(titleAppendix = listOf("A ghost story", "Book one"))
        val property = show(book)

        interact { property.titleAppendixProperty[0] = "A quiet story" }
        WaitForAsyncUtils.waitForFxEvents()

        assertEquals(2, appendixRows.size)
        assertEquals("A quiet story", inputOfRow(0).text)
        assertEquals("Book one", inputOfRow(1).text)
        assertEquals(listOf("A quiet story", "Book one"), book.titleAppendix)
    }

    /**
     * Use case: the user rewrites a title line, so the row he writes in stays the one it was - his
     * cursor is not thrown out of the field - and the manuscript carries the new text.
     */
    @Test
    fun lineRewrittenByTheUserKeepsItsRow() {
        val book = Book(titleAppendix = listOf("A ghost story", "Book one"))
        show(book)
        val row = appendixRows[0]

        interact { inputOfRow(0).text = "A quiet story" }
        WaitForAsyncUtils.waitForFxEvents()

        assertEquals(listOf("A quiet story", "Book one"), book.titleAppendix)
        assertTrue(row === appendixRows[0], "the row that was written in was built anew")
    }

    /**
     * Use case: no project is open, so the editor stands empty without a row and without a text.
     */
    @Test
    fun withoutBookTheEditorIsEmpty() {
        assertEquals("", titleInput.text)
        assertTrue(appendixRows.isEmpty(), "a row was built without a manuscript")
    }

    /**
     * Use case: the project is closed while the editor stands, so its controls are emptied and the
     * manuscript left behind keeps what was written into it.
     */
    @Test
    fun closedProjectEmptiesTheEditor() {
        val book = Book(
            title = "The Silent House",
            titleAppendix = listOf("A ghost story"),
            prompts = AIPrompt(contentPrompt = "A house nobody lives in", stylePrompt = "Dark and quiet")
        )
        show(book)

        interact { editor.bindBook(null) }
        WaitForAsyncUtils.waitForFxEvents()

        assertEquals("", titleInput.text)
        assertEquals("", contentPromptInput.text)
        assertEquals("", stylePromptInput.text)
        assertTrue(appendixRows.isEmpty(), "the rows were kept")
        assertEquals("The Silent House", book.title)
        assertEquals(listOf("A ghost story"), book.titleAppendix)
        assertEquals("A house nobody lives in", book.prompts.contentPrompt)
    }

    /**
     * Use case: every field is named, so the user reads what is written where.
     */
    @Test
    fun fieldsAreLabelled() {
        val labels = editor.lookupAll(".book-editor-label").map { (it as Label).text }

        assertTrue(
            labels.containsAll(listOf("Title", "Title appendix", "Content prompt", "Style prompt")),
            labels.toString()
        )
    }

    /**
     * Use case: the manuscript carries no title line, so the list says so in place of the rows and
     * hides that hint again as soon as a line stands there.
     */
    @Test
    fun emptyAppendixExplainsItself() {
        val property = show(Book())

        assertTrue(appendixHint.isVisible, "the hint of the empty list is missing")
        assertEquals("No title appendix yet", appendixHint.text)

        interact { property.titleAppendix = listOf("A ghost story") }
        WaitForAsyncUtils.waitForFxEvents()

        assertFalse(appendixHint.isVisible, "the hint stands beside a title line")
    }

    /**
     * Use case: the plus explains itself through a tooltip and carries its icon, so the user
     * recognises what pressing it does.
     */
    @Test
    fun addButtonCarriesIconAndTooltip() {
        assertEquals("Add title line", addButton.tooltip.text)
        assertNotNull(addButton.graphic as ImageView)
    }

    /**
     * Use case: the bin of a row explains itself through a tooltip and carries its icon.
     */
    @Test
    fun removeButtonCarriesIconAndTooltip() {
        show(Book(titleAppendix = listOf("A ghost story")))

        val button = removeButtonOfRow(0)
        assertEquals("Remove title line", button.tooltip.text)
        assertNotNull(button.graphic as ImageView)
    }

    /**
     * Reads the field of the title line at the given position.
     *
     * @param index position of the row
     * @return the field of that row
     */
    private fun fieldOfRow(index: Int): AiTextFieldListItem = appendixRows[index] as AiTextFieldListItem

    /**
     * Reads the input line of the title line at the given position.
     *
     * @param index position of the row
     * @return the input line of that row
     */
    private fun inputOfRow(index: Int): TextField = fieldOfRow(index).lookup(".text-field") as TextField

    /**
     * Reads the button removing the title line at the given position.
     *
     * @param index position of the row
     * @return the button of that row
     */
    private fun removeButtonOfRow(index: Int): Button = fieldOfRow(index).lookup(".ai-delete") as Button

    /**
     * Presses the bin of a row and waits until the question about the title line stands.
     *
     * The question blocks the JavaFX thread while it is open, so the button is pressed without
     * waiting for the press to be worked off.
     *
     * @param index position of the row
     * @return the pane of the open question
     */
    private fun requestRemovalAndAwaitQuestion(index: Int): DialogPane {
        val button = removeButtonOfRow(index)
        Platform.runLater { button.fire() }
        WaitForAsyncUtils.waitFor(TIMEOUT_SECONDS, TimeUnit.SECONDS) { dialogIsOpen() }

        return dialogPane()
    }

    /**
     * Answers the open question and waits until it is closed again.
     *
     * @param dialog pane of the open question
     * @param answer button the question is answered with
     */
    private fun answer(dialog: DialogPane, answer: ButtonType) {
        val button = dialog.lookupButton(answer) as Button
        Platform.runLater { button.fire() }
        WaitForAsyncUtils.waitFor(TIMEOUT_SECONDS, TimeUnit.SECONDS) { !dialogIsOpen() }
        WaitForAsyncUtils.waitForFxEvents()
    }

    /**
     * Tells whether a question of the application stands at the moment.
     *
     * @return `true` while a dialog is open
     */
    private fun dialogIsOpen(): Boolean = listWindows().any { it.scene?.root is DialogPane }

    /**
     * Reads the pane of the open question.
     *
     * @return the pane of the dialog that is open
     */
    private fun dialogPane(): DialogPane =
        listWindows().first { it.scene?.root is DialogPane }.scene.root as DialogPane

    private companion object {
        /** Seconds a test waits for a question to open or to close. */
        const val TIMEOUT_SECONDS: Long = 10L
    }
}

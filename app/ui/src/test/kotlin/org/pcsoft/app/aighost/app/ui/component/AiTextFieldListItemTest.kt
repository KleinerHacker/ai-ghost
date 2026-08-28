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
import javafx.scene.Scene
import javafx.scene.control.Button
import javafx.scene.control.ButtonType
import javafx.scene.control.DialogPane
import javafx.scene.control.TextField
import javafx.scene.image.ImageView
import javafx.stage.Stage
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.pcsoft.app.aighost.app.Messages
import org.testfx.framework.junit5.ApplicationTest
import org.testfx.util.WaitForAsyncUtils
import java.util.Locale
import java.util.ResourceBundle
import java.util.concurrent.TimeUnit

/**
 * Developer tests for [AiTextFieldListItem].
 *
 * The item is the entry of a list, so it is proven that it carries the text of the entry in both
 * directions and that it only reports the two requests it publishes - it neither writes an AI text
 * itself nor takes itself out of a list.
 */
class AiTextFieldListItemTest : ApplicationTest() {

    private lateinit var item: AiTextFieldListItem

    /** The input line the text of the entry is typed into. */
    private val input: TextField
        get() = item.lookup(".text-field") as TextField

    /** The wand asking the AI for a text. */
    private val createButton: Button
        get() = item.lookup(".ai-create") as Button

    /** The cross asking for the entry to be removed. */
    private val deleteButton: Button
        get() = item.lookup(".ai-delete") as Button

    override fun start(stage: Stage) {
        // No fallback, so the English base bundle is used no matter which locale the build runs under.
        MvvmFX.setGlobalResourceBundle(
            ResourceBundle.getBundle(
                Messages.BUNDLE_NAME,
                Locale.ROOT,
                ResourceBundle.Control.getNoFallbackControl(ResourceBundle.Control.FORMAT_PROPERTIES)
            )
        )

        item = AiTextFieldListItem()
        stage.scene = Scene(item, 400.0, 80.0)
        stage.show()
    }

    /**
     * Use case: a text is handed to the entry from outside, so the input line of the entry shows it.
     */
    @Test
    fun textIsShownInTheInput() {
        interact { item.text.value = "A ghost story" }
        WaitForAsyncUtils.waitForFxEvents()

        assertEquals("A ghost story", input.text)
    }

    /**
     * Use case: the user types into the entry, so the text of the entry carries what was written and
     * whoever shows the item reads it there.
     */
    @Test
    fun typedTextReachesTheItem() {
        clickOn(input).write("A ghost story")
        WaitForAsyncUtils.waitForFxEvents()

        assertEquals("A ghost story", item.text.value)
    }

    /**
     * Use case: a hint is given for the empty entry, so the input line shows it while nothing is
     * written.
     */
    @Test
    fun promptTextIsShownWhileTheEntryIsEmpty() {
        interact { item.promptText.value = "Further title line" }
        WaitForAsyncUtils.waitForFxEvents()

        assertEquals("Further title line", input.promptText)
    }

    /**
     * Use case: nobody says what the entry holds, so the cross explains itself in general words.
     */
    @Test
    fun crossExplainsItselfInGeneralWords() {
        assertEquals("Remove entry", deleteButton.tooltip.text)
    }

    /**
     * Use case: whoever shows the item says what removing means here, so the cross explains itself
     * with those words instead.
     */
    @Test
    fun crossExplainsItselfWithTheGivenWords() {
        interact { item.deleteTooltip.value = "Remove title line" }
        WaitForAsyncUtils.waitForFxEvents()

        assertEquals("Remove title line", deleteButton.tooltip.text)
    }

    /**
     * Use case: the cross carries its icon, so the user recognises what pressing it does.
     */
    @Test
    fun crossCarriesItsIcon() {
        assertNotNull(deleteButton.graphic as ImageView)
    }

    /**
     * Use case: the user presses the cross, so the item reports the request with itself as its source
     * without taking anything away - the list it belongs to decides that.
     */
    @Test
    fun crossReportsTheRemovalWithoutCarryingItOut() {
        var source: Any? = null
        interact {
            item.setOnDeleteAction { event -> source = event.source }
            item.text.value = "A ghost story"
        }

        clickOn(deleteButton)
        WaitForAsyncUtils.waitForFxEvents()

        assertSame(item, source)
        assertEquals("A ghost story", item.text.value)
        assertFalse(dialogIsOpen(), "the cross asked a question of its own")
    }

    /**
     * Use case: the cross is pressed while a handler listens for the wand only, so the request of the
     * cross does not pass as a request for an AI text.
     */
    @Test
    fun crossDoesNotPassAsTheWand() {
        var created = false
        interact { item.setOnCreateAiText { created = true } }

        clickOn(deleteButton)
        WaitForAsyncUtils.waitForFxEvents()

        assertFalse(created, "the cross reported a request for an AI text")
    }

    /**
     * Use case: the entry is empty, so the wand hands the request on as the event of the item without
     * asking anything first.
     */
    @Test
    fun wandFiresTheCreateEventOnAnEmptyEntry() {
        var source: Any? = null
        interact { item.setOnCreateAiText { event -> source = event.source } }

        clickOn(createButton)
        WaitForAsyncUtils.waitForFxEvents()

        assertSame(item, source)
        assertFalse(dialogIsOpen(), "the empty entry asked before creating")
    }

    /**
     * Use case: the wand is pressed while a handler listens for the cross only, so the request of the
     * wand does not pass as a request to remove the entry.
     */
    @Test
    fun wandDoesNotPassAsTheCross() {
        var deleted = false
        interact { item.setOnDeleteAction { deleted = true } }

        clickOn(createButton)
        WaitForAsyncUtils.waitForFxEvents()

        assertFalse(deleted, "the wand reported a request to remove the entry")
    }

    /**
     * Use case: a text is already written in the entry, so the wand warns that it would be lost and
     * hands the request on once the user agreed.
     */
    @Test
    fun writtenTextIsOverwrittenAfterAgreement() {
        var fired = false
        interact {
            item.setOnCreateAiText { fired = true }
            item.text.value = "A ghost story"
        }

        val dialog = requestCreationAndAwaitQuestion()
        assertEquals("Attention: the written text is overwritten!", dialog.headerText)
        answer(dialog, ButtonType.YES)

        assertTrue(fired, "the request was not passed on")
    }

    /**
     * Use case: a text is already written and the user refuses to lose it, so nothing is asked of the
     * AI and the text of the entry stays untouched.
     */
    @Test
    fun writtenTextIsKeptAfterRefusal() {
        var fired = false
        interact {
            item.setOnCreateAiText { fired = true }
            item.text.value = "A ghost story"
        }

        answer(requestCreationAndAwaitQuestion(), ButtonType.NO)

        assertFalse(fired, "the request was passed on although it was refused")
        assertEquals("A ghost story", input.text)
    }

    /**
     * Use case: both handlers are removed again, so pressing the wand or the cross no longer reaches
     * anybody.
     */
    @Test
    fun removedHandlersAreNoLongerCalled() {
        var calls = 0
        interact {
            item.setOnCreateAiText { calls++ }
            item.setOnDeleteAction { calls++ }
            item.setOnCreateAiText(null)
            item.setOnDeleteAction(null)
        }

        clickOn(createButton)
        clickOn(deleteButton)
        WaitForAsyncUtils.waitForFxEvents()

        assertEquals(0, calls)
        assertNull(item.getOnCreateAiText())
        assertNull(item.getOnDeleteAction())
    }

    /**
     * Presses the wand and waits until the question about the written text stands.
     *
     * The question blocks the JavaFX thread while it is open, so the button is pressed without
     * waiting for the press to be worked off.
     *
     * @return the pane of the open question
     */
    private fun requestCreationAndAwaitQuestion(): DialogPane {
        val button = createButton
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

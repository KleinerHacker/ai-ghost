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
 * Developer tests for [AiTextField].
 */
class AiTextFieldTest : ApplicationTest() {

    private lateinit var textField: AiTextField

    private val input: TextField
        get() = textField.lookup(".text-field") as TextField

    private val createButton: Button
        get() = textField.lookup(".ai-create") as Button

    override fun start(stage: Stage) {
        // No fallback, so the English base bundle is used no matter which locale the build runs under.
        MvvmFX.setGlobalResourceBundle(
            ResourceBundle.getBundle(
                Messages.BUNDLE_NAME,
                Locale.ROOT,
                ResourceBundle.Control.getNoFallbackControl(ResourceBundle.Control.FORMAT_PROPERTIES)
            )
        )

        textField = AiTextField()
        stage.scene = Scene(textField, 400.0, 80.0)
        stage.show()
    }

    /**
     * Use case: the field is empty, so the wand hands the request on as the action event of the
     * component without asking anything first.
     */
    @Test
    fun wandFiresTheCreateEventOnAnEmptyField() {
        var source: Any? = null
        interact { textField.setOnCreateAiText { event -> source = event.source } }

        clickOn(createButton)
        WaitForAsyncUtils.waitForFxEvents()

        assertSame(textField, source)
        assertFalse(dialogIsOpen(), "the empty field asked before creating")
    }

    /**
     * Use case: a text is already written, so the wand warns that it would be lost and hands the
     * request on once the user agreed.
     */
    @Test
    fun writtenTextIsOverwrittenAfterAgreement() {
        var fired = false
        interact {
            textField.setOnCreateAiText { fired = true }
            textField.text.value = "The old title"
        }

        val dialog = requestCreationAndAwaitQuestion()
        assertEquals("Attention: the written text is overwritten!", dialog.headerText)
        answer(dialog, ButtonType.YES)

        assertTrue(fired, "the request was not passed on")
    }

    /**
     * Use case: a text is already written and the user refuses to lose it, so nothing is asked of
     * the AI and the text stays untouched.
     */
    @Test
    fun writtenTextIsKeptAfterRefusal() {
        var fired = false
        interact {
            textField.setOnCreateAiText { fired = true }
            textField.text.value = "The old title"
        }

        answer(requestCreationAndAwaitQuestion(), ButtonType.NO)

        assertFalse(fired, "the request was passed on although it was refused")
        assertEquals("The old title", input.text)
    }

    /**
     * Use case: the handler of the wand is removed again, so pressing it no longer reaches the
     * former listener.
     */
    @Test
    fun createHandlerCanBeRemoved() {
        var fired = false
        interact {
            textField.setOnCreateAiText { fired = true }
            textField.setOnCreateAiText(null)
        }

        clickOn(createButton)
        WaitForAsyncUtils.waitForFxEvents()

        assertFalse(fired)
        assertNull(textField.getOnCreateAiText())
    }

    /**
     * Use case: the wand explains itself through a tooltip and carries its icon, so the user
     * recognises what pressing it does.
     */
    @Test
    fun wandCarriesIconAndTooltip() {
        assertEquals("Create", createButton.tooltip.text)
        assertNotNull(createButton.graphic as ImageView)
    }

    /**
     * Use case: the surrounding view sets a text from the outside, so the input line shows it and
     * gives back what the user writes into it.
     */
    @Test
    fun textIsSharedWithTheInputLine() {
        interact { textField.text.value = "The old title" }
        WaitForAsyncUtils.waitForFxEvents()
        assertEquals("The old title", input.text)

        interact { input.text = "A new title" }
        WaitForAsyncUtils.waitForFxEvents()
        assertEquals("A new title", textField.text.value)
    }

    /**
     * Use case: the surrounding view labels the empty field, so the hint it sets reaches the input
     * line.
     */
    @Test
    fun promptTextReachesTheInputLine() {
        interact { textField.promptText.value = "Title of the chapter" }
        WaitForAsyncUtils.waitForFxEvents()

        assertEquals("Title of the chapter", input.promptText)
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

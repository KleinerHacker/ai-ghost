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

package org.pcsoft.app.aighost.app.ui.component.base

import de.saxsys.mvvmfx.MvvmFX
import javafx.scene.Scene
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.control.TextArea
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

/**
 * Developer tests for [AiPromptArea].
 */
class AiPromptAreaTest : ApplicationTest() {

    private lateinit var promptArea: AiPromptArea

    private val textArea: TextArea
        get() = promptArea.lookup(".text-area") as TextArea

    private val optimizeButton: Button
        get() = promptArea.lookup(".prompt-optimize") as Button

    private val counter: Label
        get() = promptArea.lookup(".prompt-counter") as Label

    private val tokens: Label
        get() = promptArea.lookup(".prompt-tokens") as Label

    override fun start(stage: Stage) {
        // No fallback, so the English base bundle is used no matter which locale the build runs under.
        MvvmFX.setGlobalResourceBundle(
            ResourceBundle.getBundle(
                Messages.BUNDLE_NAME,
                Locale.ROOT,
                ResourceBundle.Control.getNoFallbackControl(ResourceBundle.Control.FORMAT_PROPERTIES)
            )
        )

        promptArea = AiPromptArea()
        stage.scene = Scene(promptArea, 400.0, 300.0)
        stage.show()
    }

    /**
     * Use case: the user is given a limit from the outside, so the counter names the characters
     * written against the characters allowed as soon as the text changes.
     */
    @Test
    fun counterReportsWrittenAndAllowedCharacters() {
        interact {
            promptArea.maxCharacters.set(100L)
            promptArea.text.value = "Write a short story"
        }
        WaitForAsyncUtils.waitForFxEvents()

        assertEquals("19 / 100", counter.text)
    }

    /**
     * Use case: without a limit there is nothing to count against, so the counter gives its space
     * back instead of showing a meaningless number.
     */
    @Test
    fun counterIsHiddenWithoutLimit() {
        interact { promptArea.text.value = "Write a short story" }
        WaitForAsyncUtils.waitForFxEvents()

        assertFalse(counter.isVisible)
        assertFalse(counter.isManaged)
    }

    /**
     * Use case: the user types beyond the limit, so the writing surface takes the characters that
     * still fit and drops the rest.
     */
    @Test
    fun inputBeyondTheLimitIsCutOff() {
        interact { promptArea.maxCharacters.set(10L) }

        clickOn(textArea).write("0123456789ABCDEF")
        WaitForAsyncUtils.waitForFxEvents()

        assertEquals("0123456789", textArea.text)
        assertEquals("0123456789", promptArea.text.value)
    }

    /**
     * Use case: the user pastes a text that is longer than the limit allows, so the insertion is
     * taken over up to the limit instead of being refused as a whole.
     */
    @Test
    fun pastedTextIsCutOffAtTheLimit() {
        interact {
            promptArea.maxCharacters.set(5L)
            textArea.text = "0123456789"
        }
        WaitForAsyncUtils.waitForFxEvents()

        assertEquals("01234", textArea.text)
    }

    /**
     * Use case: a limit is set after the prompt was written, so what is already there is cut down
     * to the new limit and the counter never reports more than is allowed.
     */
    @Test
    fun limitSetAfterwardsShortensTheWrittenPrompt() {
        interact {
            promptArea.text.value = "0123456789"
            promptArea.maxCharacters.set(4L)
        }
        WaitForAsyncUtils.waitForFxEvents()

        assertEquals("0123", promptArea.text.value)
        assertEquals("4 / 4", counter.text)
    }

    /**
     * Use case: the prompt fills up, so the counter warns from nine tenths of the limit on and
     * reports the reached limit in its own colour.
     */
    @Test
    fun counterChangesItsStyleWithTheUsedCharacters() {
        interact {
            promptArea.maxCharacters.set(10L)
            promptArea.text.value = "12345678"
        }
        WaitForAsyncUtils.waitForFxEvents()
        assertFalse(counter.styleClass.contains("prompt-counter-warn"), "warned too early")

        interact { promptArea.text.value = "123456789" }
        WaitForAsyncUtils.waitForFxEvents()
        assertTrue(counter.styleClass.contains("prompt-counter-warn"), "no warning at 90 percent")
        assertFalse(counter.styleClass.contains("prompt-counter-limit"), "limit reported too early")

        interact { promptArea.text.value = "1234567890" }
        WaitForAsyncUtils.waitForFxEvents()
        assertTrue(counter.styleClass.contains("prompt-counter-limit"), "no limit at 100 percent")
        assertFalse(counter.styleClass.contains("prompt-counter-warn"), "warning kept beyond the limit")
    }

    /**
     * Use case: the cost of a prompt is what the user pays for, so the footer estimates its tokens
     * and follows every change of the text.
     */
    @Test
    fun tokenEstimationFollowsTheWrittenText() {
        interact { promptArea.text.value = "12345678" }
        WaitForAsyncUtils.waitForFxEvents()

        assertEquals(2L, promptArea.tokens.get())
        assertEquals("~2 tokens", tokens.text)

        interact { promptArea.text.value = "" }
        WaitForAsyncUtils.waitForFxEvents()

        assertEquals(0L, promptArea.tokens.get())
        assertEquals("~0 tokens", tokens.text)
    }

    /**
     * Use case: the user asks the AI for a better wording, so the wand hands the request on as the
     * action event of the component - once per press, not once more for the action of the button
     * that climbs up to the component on its own.
     */
    @Test
    fun wandFiresTheOptimizeEvent() {
        var source: Any? = null
        var fired = 0
        interact {
            promptArea.setOnOptimizePrompt { event ->
                source = event.source
                fired++
            }
        }

        clickOn(optimizeButton)
        WaitForAsyncUtils.waitForFxEvents()

        assertSame(promptArea, source)
        assertEquals(1, fired, "the request was handed on more than once")
    }

    /**
     * Use case: the handler of the wand is removed again, so pressing it no longer reaches the
     * former listener.
     */
    @Test
    fun optimizeHandlerCanBeRemoved() {
        var fired = false
        interact {
            promptArea.setOnOptimizePrompt { fired = true }
            promptArea.setOnOptimizePrompt(null)
        }

        clickOn(optimizeButton)
        WaitForAsyncUtils.waitForFxEvents()

        assertFalse(fired)
        assertNull(promptArea.getOnOptimizePrompt())
    }

    /**
     * Use case: the wand explains itself through a tooltip and carries its icon, so the user
     * recognises what pressing it does.
     */
    @Test
    fun wandCarriesIconAndTooltip() {
        assertEquals("Optimize prompt", optimizeButton.tooltip.text)
        assertNotNull(optimizeButton.graphic as ImageView)
    }

    /**
     * Use case: the surrounding view labels the empty area, so the hint it sets reaches the writing
     * surface.
     */
    @Test
    fun promptTextReachesTheWritingSurface() {
        interact { promptArea.promptText.value = "Describe the chapter" }
        WaitForAsyncUtils.waitForFxEvents()

        assertEquals("Describe the chapter", textArea.promptText)
    }
}

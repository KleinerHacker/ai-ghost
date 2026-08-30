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

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/**
 * Developer tests for [AiPromptAreaViewModel].
 */
class AiPromptAreaViewModelTest {

    private val viewModel = AiPromptAreaViewModel()

    /**
     * Use case: the component is shown before anything was typed, so it starts on an empty prompt
     * without a limit and reports no cost at all.
     */
    @Test
    fun freshViewModelStartsEmptyAndWithoutLimit() {
        assertEquals("", viewModel.text.value)
        assertEquals(0L, viewModel.length.get())
        assertEquals(0L, viewModel.tokens.get())
        assertFalse(viewModel.limited.get())
        assertEquals(AiPromptUsage.NORMAL, viewModel.usage.get())
    }

    /**
     * Use case: the user writes a prompt, so its length and its estimated token cost are reported
     * for the footer to show.
     */
    @Test
    fun lengthAndTokensFollowTheText() {
        viewModel.text.value = "Write a short story"

        assertEquals(19L, viewModel.length.get())
        assertEquals(5L, viewModel.tokens.get())
    }

    /**
     * Use case: a limit is handed in from the outside, so the counter is shown; a limit of zero or
     * less means the prompt may grow freely and nothing is counted.
     */
    @Test
    fun limitDecidesWhetherTheCounterIsShown() {
        viewModel.maxCharacters.set(50L)
        assertTrue(viewModel.limited.get())

        viewModel.maxCharacters.set(0L)
        assertFalse(viewModel.limited.get())

        viewModel.maxCharacters.set(-1L)
        assertFalse(viewModel.limited.get())
    }

    /**
     * Use case: the prompt fills up, so the usage steps from plain over the warning at nine tenths
     * of the limit to the reached limit itself.
     */
    @Test
    fun usageStepsWithTheWrittenCharacters() {
        viewModel.maxCharacters.set(10L)

        viewModel.text.value = "12345678"
        assertEquals(AiPromptUsage.NORMAL, viewModel.usage.get())

        viewModel.text.value = "123456789"
        assertEquals(AiPromptUsage.WARN, viewModel.usage.get())

        viewModel.text.value = "1234567890"
        assertEquals(AiPromptUsage.LIMIT, viewModel.usage.get())
    }

    /**
     * Use case: without a limit the prompt can never run against one, so a long text stays plain.
     */
    @Test
    fun usageStaysPlainWithoutLimit() {
        viewModel.text.value = "A prompt of considerable length without any limit at all"

        assertEquals(AiPromptUsage.NORMAL, viewModel.usage.get())
    }

    /**
     * Use case: the writing surface asks what of an insertion still fits, so a text longer than the
     * limit is cut down while a fitting one is handed back untouched.
     */
    @Test
    fun limitCutsTextDownToTheAllowedLength() {
        viewModel.maxCharacters.set(5L)

        assertEquals("01234", viewModel.limit("0123456789"))
        assertEquals("0123", viewModel.limit("0123"))

        viewModel.maxCharacters.set(0L)
        assertEquals("0123456789", viewModel.limit("0123456789"))
    }

    /**
     * Use case: a limit is set after the prompt was written, so the text already there is cut down
     * instead of staying longer than the limit it is measured against.
     */
    @Test
    fun limitAppliesToAlreadyWrittenText() {
        viewModel.text.value = "0123456789"
        viewModel.maxCharacters.set(4L)

        assertEquals("0123", viewModel.text.value)
        assertEquals(4L, viewModel.length.get())
        assertEquals(AiPromptUsage.LIMIT, viewModel.usage.get())
    }

    /**
     * Use case: the user presses the wand, so the request is passed on to the component, which
     * turns it into the event of the outside world.
     */
    @Test
    fun optimizeReachesTheRegisteredCallback() {
        var called = false
        viewModel.onOptimize = { called = true }

        viewModel.optimize()

        assertTrue(called)
    }

    /**
     * Use case: the component registers no callback at all, so a press of the wand is simply
     * swallowed instead of failing.
     */
    @Test
    fun optimizeWithoutCallbackDoesNothing() {
        viewModel.optimize()
    }

    /**
     * Use case: the text is cleared through a null value, which JavaFX allows on a string property,
     * so the footer falls back to an empty prompt.
     */
    @Test
    fun clearedTextIsTreatedAsEmpty() {
        viewModel.text.value = "Write a short story"
        viewModel.text.value = null

        assertEquals(0L, viewModel.length.get())
        assertEquals(0L, viewModel.tokens.get())
    }
}

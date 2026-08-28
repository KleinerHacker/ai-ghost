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

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

/**
 * Developer tests for [AiTextFieldListItemViewModel].
 */
class AiTextFieldListItemViewModelTest {

    private val viewModel = AiTextFieldListItemViewModel()

    /**
     * Use case: a fresh entry carries no text, no hint and no wording of its own, so the item shows
     * an empty line and a cross explaining itself in general words.
     */
    @Test
    fun freshEntryIsEmpty() {
        assertEquals("", viewModel.text.value)
        assertNull(viewModel.promptText.value)
        assertNull(viewModel.deleteTooltip.value)
    }

    /**
     * Use case: the wand was pressed, so the request is handed on to the item, which turns it into
     * the event the outside world listens to.
     */
    @Test
    fun creationIsHandedOn() {
        var calls = 0
        viewModel.onCreate = { calls++ }

        viewModel.create()

        assertEquals(1, calls)
    }

    /**
     * Use case: the cross was pressed, so the request is handed on to the item without the entry
     * being changed - what happens with it is decided outside.
     */
    @Test
    fun removalIsHandedOn() {
        var calls = 0
        viewModel.onDelete = { calls++ }
        viewModel.text.value = "A ghost story"

        viewModel.delete()

        assertEquals(1, calls)
        assertEquals("A ghost story", viewModel.text.value)
    }

    /**
     * Use case: nobody listens to the item, so pressing the wand or the cross is worked off without
     * anything happening.
     */
    @Test
    fun requestsWithoutAListenerAreWorkedOff() {
        viewModel.create()
        viewModel.delete()
    }
}

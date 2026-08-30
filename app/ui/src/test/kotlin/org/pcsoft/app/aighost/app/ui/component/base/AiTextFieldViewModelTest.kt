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
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/**
 * Developer tests for [AiTextFieldViewModel].
 */
class AiTextFieldViewModelTest {

    private val viewModel = AiTextFieldViewModel()

    /**
     * Use case: the field is empty, so asking the AI for a text throws nothing away and the request
     * is passed on without a question.
     */
    @Test
    fun emptyFieldAsksNothing() {
        var asked = false
        var created = false
        viewModel.confirmOverwrite = { asked = true; true }
        viewModel.onCreate = { created = true }

        viewModel.create()

        assertFalse(asked, "the empty field asked before creating")
        assertTrue(created, "the request was not passed on")
    }

    /**
     * Use case: a text is written and the user agrees to give it up, so the request reaches the
     * surrounding view.
     */
    @Test
    fun writtenTextIsOverwrittenAfterAgreement() {
        var created = false
        viewModel.text.value = "The old title"
        viewModel.confirmOverwrite = { true }
        viewModel.onCreate = { created = true }

        viewModel.create()

        assertTrue(created, "the request was not passed on")
    }

    /**
     * Use case: a text is written and the user refuses to give it up, so nothing is asked of the AI
     * and the text stays as it is.
     */
    @Test
    fun writtenTextIsKeptAfterRefusal() {
        var created = false
        viewModel.text.value = "The old title"
        viewModel.confirmOverwrite = { false }
        viewModel.onCreate = { created = true }

        viewModel.create()

        assertFalse(created, "the request was passed on although it was refused")
        assertEquals("The old title", viewModel.text.value)
    }

    /**
     * Use case: the component is used without anybody listening, so pressing the wand is answered
     * with nothing instead of an error.
     */
    @Test
    fun requestWithoutListenerIsHarmless() {
        viewModel.text.value = "The old title"
        viewModel.confirmOverwrite = { true }

        viewModel.create()

        assertNull(viewModel.onCreate)
    }
}

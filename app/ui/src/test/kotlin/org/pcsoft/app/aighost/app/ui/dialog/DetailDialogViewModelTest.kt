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

package org.pcsoft.app.aighost.app.ui.dialog

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotSame
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.pcsoft.app.aighost.app.Messages
import org.testfx.framework.junit5.ApplicationExtension

/**
 * Developer tests for [DetailDialogViewModel].
 */
@ExtendWith(ApplicationExtension::class)
class DetailDialogViewModelTest {

    private val viewModel = DetailDialogViewModel()

    /**
     * Use case: a dialog is opened for the first time, so its details pane is folded and the button
     * offers to unfold it.
     */
    @Test
    fun startsWithFoldedDetails() {
        assertFalse(viewModel.detailsVisible.value, "the details pane is unfolded from the start")
        assertEquals(Messages["dialog.details.show"], viewModel.detailsButtonText.value)
    }

    /**
     * Use case: the user asks for the report and folds it away again, so the pane follows both
     * clicks and the button always names what the next click does.
     */
    @Test
    fun togglesTheDetailsPaneBackAndForth() {
        viewModel.toggleDetails()

        assertTrue(viewModel.detailsVisible.value, "the details pane stayed folded")
        assertEquals(Messages["dialog.details.hide"], viewModel.detailsButtonText.value)

        viewModel.toggleDetails()

        assertFalse(viewModel.detailsVisible.value, "the details pane stayed unfolded")
        assertEquals(Messages["dialog.details.show"], viewModel.detailsButtonText.value)
    }

    /**
     * Use case: the severity of the dialog decides its icon, so switching the severity exchanges
     * the image the view shows.
     */
    @Test
    fun followsTheSeverityWithItsIcon() {
        viewModel.type.value = DialogType.ERROR
        val error = viewModel.icon.value

        viewModel.type.value = DialogType.WARNING
        val warning = viewModel.icon.value

        assertFalse(error.isError, "the error icon could not be loaded")
        assertFalse(warning.isError, "the warning icon could not be loaded")
        assertNotSame(error, warning, "both severities show the same icon")
    }

    /**
     * Use case: the texts of the dialog are handed in by whoever opens it, so the view model carries
     * them unchanged.
     */
    @Test
    fun carriesTheTextsItWasGiven() {
        viewModel.caption.value = "The project is incomplete"
        viewModel.message.value = "Parts of the project could not be read."
        viewModel.details.value = "- outline"

        assertEquals("The project is incomplete", viewModel.caption.value)
        assertEquals("Parts of the project could not be read.", viewModel.message.value)
        assertEquals("- outline", viewModel.details.value)
    }
}

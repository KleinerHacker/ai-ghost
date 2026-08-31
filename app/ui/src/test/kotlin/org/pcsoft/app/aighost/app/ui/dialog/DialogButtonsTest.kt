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

import javafx.scene.control.ButtonType
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.testfx.framework.junit5.ApplicationExtension

/**
 * Developer tests for [DialogButtons].
 */
@ExtendWith(ApplicationExtension::class)
class DialogButtonsTest {

    /**
     * Use case: a report is only acknowledged, so the dialog carries the standard button of JavaFX,
     * which is translated by the toolkit and answers ENTER.
     */
    @Test
    fun acknowledgesWithASingleButton() {
        val buttons = DialogButtons.OK.buttonTypes

        assertEquals(listOf(ButtonType.OK), buttons)
        assertTrue(buttons[0].buttonData.isDefaultButton, "the acknowledgement does not answer ENTER")
    }

    /**
     * Use case: a question is asked, so the dialog carries the standard yes and no of JavaFX, the
     * yes answering ENTER and the no answering ESCAPE and the window close button.
     */
    @Test
    fun asksWithYesAndNo() {
        val buttons = DialogButtons.YES_NO.buttonTypes

        assertEquals(listOf(ButtonType.YES, ButtonType.NO), buttons)
        assertTrue(buttons[0].buttonData.isDefaultButton, "yes does not answer ENTER")
        assertTrue(buttons[1].buttonData.isCancelButton, "no does not answer ESCAPE")
    }

    /**
     * Use case: a settings editor is shown, so the dialog carries the standard OK, CANCEL and APPLY
     * of JavaFX - OK stores and answers ENTER, CANCEL discards and answers ESCAPE, APPLY stores
     * without closing and does neither.
     */
    @Test
    fun editsWithOkCancelAndApply() {
        val buttons = DialogButtons.OK_CANCEL_APPLY.buttonTypes

        assertEquals(listOf(ButtonType.OK, ButtonType.CANCEL, ButtonType.APPLY), buttons)
        assertTrue(buttons[0].buttonData.isDefaultButton, "OK does not answer ENTER")
        assertTrue(buttons[1].buttonData.isCancelButton, "CANCEL does not answer ESCAPE")
        assertFalse(buttons[2].buttonData.isDefaultButton, "APPLY answers ENTER")
        assertFalse(buttons[2].buttonData.isCancelButton, "APPLY answers ESCAPE")
    }
}

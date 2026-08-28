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

import de.saxsys.mvvmfx.MvvmFX
import javafx.scene.control.Button
import javafx.scene.control.ButtonBar
import javafx.scene.control.ButtonType
import javafx.scene.control.Label
import javafx.scene.control.TextArea
import javafx.stage.Stage
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.pcsoft.app.aighost.app.Messages
import org.testfx.framework.junit5.ApplicationTest
import java.util.Locale
import java.util.ResourceBundle

/**
 * Developer tests for [DetailDialog].
 *
 * The dialog is built but never shown: showing it would wait for an answer of the user, while
 * everything that is checked here - its buttons, its texts and its details pane - is part of the
 * dialog pane already.
 */
class DetailDialogTest : ApplicationTest() {

    private companion object {
        const val CAPTION: String = "The project is incomplete"
        const val MESSAGE: String = "Parts of the project could not be read."
        const val DETAILS: String = "- outline"
    }

    override fun start(stage: Stage) {
        // No fallback, so the English base bundle is used no matter which locale the build runs under.
        MvvmFX.setGlobalResourceBundle(
            ResourceBundle.getBundle(
                Messages.BUNDLE_NAME,
                Locale.ROOT,
                ResourceBundle.Control.getNoFallbackControl(ResourceBundle.Control.FORMAT_PROPERTIES)
            )
        )
    }

    /**
     * Use case: a failure is reported with a report to unfold, so the dialog shows caption, message
     * and details, and carries a single button to acknowledge it.
     */
    @Test
    fun reportsAFailureWithASingleButton() {
        val dialog = build(DialogType.ERROR, DialogButtons.OK)

        assertEquals(listOf(ButtonType.OK), dialog.dialogPane.buttonTypes)
        assertEquals(CAPTION, caption(dialog).text)
        assertEquals(MESSAGE, message(dialog).text)
        assertEquals(DETAILS, details(dialog).text)
    }

    /**
     * Use case: the user is asked a question with a report to unfold, so the dialog offers a yes and
     * a no, the yes answering ENTER and the no answering ESCAPE.
     */
    @Test
    fun asksAQuestionWithYesAndNo() {
        val dialog = build(DialogType.WARNING, DialogButtons.YES_NO)

        assertEquals(listOf(ButtonType.YES, ButtonType.NO), dialog.dialogPane.buttonTypes)
        assertTrue(dialog.dialogPane.buttonTypes[0].buttonData.isDefaultButton, "yes is not the default answer")
        assertTrue(dialog.dialogPane.buttonTypes[1].buttonData.isCancelButton, "no does not answer ESCAPE")
        assertEquals(ButtonBar.ButtonData.YES, dialog.dialogPane.buttonTypes[0].buttonData)
    }

    /**
     * Use case: the dialog draws its header itself, so the header of the plain alert is dropped and
     * neither the caption nor the icon is shown twice.
     */
    @Test
    fun dropsTheHeaderOfThePlainAlert() {
        val dialog = build(DialogType.ERROR, DialogButtons.OK)

        assertNull(dialog.dialogPane.headerText, "the alert still carries its own header")
        assertNull(dialog.dialogPane.graphic, "the alert still carries its own icon")
        assertNotNull(dialog.dialogPane.content, "the dialog shows no content of its own")
    }

    /**
     * Use case: the report is out of the way until the user asks for it, so the details pane is
     * folded at first and leaves the layout while it is folded.
     */
    @Test
    fun keepsTheReportFoldedUntilItIsAskedFor() {
        val dialog = build(DialogType.WARNING, DialogButtons.YES_NO)
        val details = details(dialog)

        assertFalse(dialog.detailsVisible, "the dialog opens with an unfolded report")
        assertFalse(details.isVisible, "the report is shown while it is folded")
        assertFalse(details.isManaged, "the folded report still takes its space in the layout")
    }

    /**
     * Use case: the user clicks the details button, so the report is unfolded and the button offers
     * to fold it away again.
     */
    @Test
    fun unfoldsTheReportOnDemand() {
        val dialog = build(DialogType.ERROR, DialogButtons.OK)
        val toggle = toggle(dialog)

        assertEquals(Messages["dialog.details.show"], toggle.text)

        interact { toggle.fire() }

        assertTrue(dialog.detailsVisible, "the report stayed folded")
        assertTrue(details(dialog).isVisible, "the unfolded report is not shown")
        assertTrue(details(dialog).isManaged, "the unfolded report takes no space in the layout")
        assertEquals(Messages["dialog.details.hide"], toggle.text)

        interact { toggle.fire() }

        assertFalse(dialog.detailsVisible, "the report stayed unfolded")
        assertEquals(Messages["dialog.details.show"], toggle.text)
    }

    /**
     * Use case: the report is unfolded, so the dialog grows in height only - the pane asks for no
     * width of its own and is stretched to the width the header above it needs.
     */
    @Test
    fun growsInHeightOnly() {
        val dialog = build(DialogType.ERROR, DialogButtons.OK)
        val details = details(dialog)

        assertEquals(0.0, details.prefWidth, "the report asks for a width of its own")
        assertEquals(0.0, details.minWidth, "the report demands a minimum width")
        assertEquals(Double.POSITIVE_INFINITY, details.maxWidth, "the report is not stretched to the dialog")
        assertEquals(0.0, details.prefHeight, "the folded report is not empty")

        interact { toggle(dialog).fire() }

        assertEquals(0.0, details.prefWidth, "unfolding the report gave it a width of its own")
    }

    /**
     * Use case: the report of a dialog can only be read, so the user cannot change what the
     * application wrote into it.
     */
    @Test
    fun showsTheReportReadOnly() {
        val dialog = build(DialogType.ERROR, DialogButtons.OK)

        assertFalse(details(dialog).isEditable, "the report can be edited")
    }

    /** Builds a dialog on the JavaFX thread, the only thread a dialog may be created on. */
    private fun build(type: DialogType, buttons: DialogButtons): DetailDialog {
        lateinit var dialog: DetailDialog
        interact { dialog = DetailDialog(type, buttons, CAPTION, MESSAGE, DETAILS) }
        return dialog
    }

    private fun caption(dialog: DetailDialog): Label =
        dialog.dialogPane.lookup(".detail-dialog-caption") as Label

    private fun message(dialog: DetailDialog): Label =
        dialog.dialogPane.lookup(".detail-dialog-message") as Label

    private fun details(dialog: DetailDialog): TextArea =
        dialog.dialogPane.lookup(".detail-dialog-details") as TextArea

    private fun toggle(dialog: DetailDialog): Button =
        dialog.dialogPane.lookup(".detail-dialog-toggle") as Button
}

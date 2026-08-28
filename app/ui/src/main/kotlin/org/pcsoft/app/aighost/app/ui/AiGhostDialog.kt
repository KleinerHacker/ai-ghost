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

package org.pcsoft.app.aighost.app.ui

import javafx.scene.control.Alert
import javafx.scene.control.ButtonBar
import javafx.scene.image.ImageView
import javafx.stage.Stage
import javafx.stage.Window
import org.pcsoft.app.aighost.app.AiGhostIcons
import org.pcsoft.app.aighost.app.AiGhostTheme
import org.pcsoft.app.aighost.app.ui.dialog.DetailDialog
import org.pcsoft.app.aighost.app.ui.dialog.DialogButtons
import org.pcsoft.app.aighost.app.ui.dialog.DialogType

/**
 * Every dialog of the application, dressed in the theme and asked in one place.
 *
 * The object knows nothing about the situation a dialog is opened in: every text is handed in
 * already translated, so a caller decides what is said and this place decides how it looks. Two
 * flavours exist per severity - a plain alert, and a dialog carrying a report the user unfolds.
 */
object AiGhostDialog {

    /**
     * Reports a failure and waits until the user acknowledged it.
     *
     * @param title title of the dialog window
     * @param caption headline telling in one line what happened
     * @param message text describing what happened
     * @param owner window the dialog belongs to, none by default
     */
    @JvmStatic
    @JvmOverloads
    fun showError(title: String, caption: String, message: String, owner: Window? = null) {
        showSimple(DialogType.ERROR, DialogButtons.OK, title, caption, message, owner)
    }

    /**
     * Reports a failure together with a report the user can unfold, and waits until the user
     * acknowledged it.
     *
     * @param title title of the dialog window
     * @param caption headline telling in one line what happened
     * @param message text describing what happened
     * @param details report shown in the details pane
     * @param owner window the dialog belongs to, none by default
     */
    @JvmStatic
    @JvmOverloads
    fun showErrorDetails(
        title: String,
        caption: String,
        message: String,
        details: String,
        owner: Window? = null
    ) {
        showDetail(DialogType.ERROR, DialogButtons.OK, title, caption, message, details, owner)
    }

    /**
     * Warns the user and waits until the warning was acknowledged.
     *
     * @param title title of the dialog window
     * @param caption headline telling in one line what the user has to know
     * @param message text describing the situation
     * @param owner window the dialog belongs to, none by default
     */
    @JvmStatic
    @JvmOverloads
    fun showWarning(title: String, caption: String, message: String, owner: Window? = null) {
        showSimple(DialogType.WARNING, DialogButtons.OK, title, caption, message, owner)
    }

    /**
     * Warns the user with a report to unfold and waits until the warning was acknowledged.
     *
     * @param title title of the dialog window
     * @param caption headline telling in one line what the user has to know
     * @param message text describing the situation
     * @param details report shown in the details pane
     * @param owner window the dialog belongs to, none by default
     */
    @JvmStatic
    @JvmOverloads
    fun showWarningDetails(
        title: String,
        caption: String,
        message: String,
        details: String,
        owner: Window? = null
    ) {
        showDetail(DialogType.WARNING, DialogButtons.OK, title, caption, message, details, owner)
    }

    /**
     * Warns the user and asks whether the application may carry on.
     *
     * Everything but an explicit yes is a no: closing the dialog with ESCAPE or with the window
     * close button answers the question negatively.
     *
     * @param title title of the dialog window
     * @param caption headline telling in one line what the user has to decide about
     * @param message text describing the situation
     * @param owner window the dialog belongs to, none by default
     * @return `true` when the user answered with yes
     */
    @JvmStatic
    @JvmOverloads
    fun showWarningConfirm(title: String, caption: String, message: String, owner: Window? = null): Boolean =
        showSimple(DialogType.WARNING, DialogButtons.YES_NO, title, caption, message, owner)

    /**
     * Warns the user with a report to unfold and asks whether the application may carry on.
     *
     * @param title title of the dialog window
     * @param caption headline telling in one line what the user has to decide about
     * @param message text describing the situation
     * @param details report shown in the details pane
     * @param owner window the dialog belongs to, none by default
     * @return `true` when the user answered with yes
     */
    @JvmStatic
    @JvmOverloads
    fun showWarningConfirmDetails(
        title: String,
        caption: String,
        message: String,
        details: String,
        owner: Window? = null
    ): Boolean = showDetail(DialogType.WARNING, DialogButtons.YES_NO, title, caption, message, details, owner)

    /**
     * Shows a plain alert, which needs nothing but the parts JavaFX already draws itself.
     *
     * @return `true` when the user answered with yes
     */
    private fun showSimple(
        type: DialogType,
        buttons: DialogButtons,
        title: String,
        caption: String,
        message: String,
        owner: Window?
    ): Boolean = Alert(type.alertType).apply {
        this.title = title
        headerText = caption
        contentText = message
        graphic = ImageView(type.icon).apply {
            fitWidth = AiGhostIcons.DIALOG_ICON_SIZE
            fitHeight = AiGhostIcons.DIALOG_ICON_SIZE
            isPreserveRatio = true
            isSmooth = true
        }
        dialogPane.buttonTypes.setAll(buttons.buttonTypes)
        decorate(this, owner)
    }.let(::isConfirmed)

    /**
     * Shows a dialog carrying a report the user can unfold.
     *
     * @return `true` when the user answered with yes
     */
    private fun showDetail(
        type: DialogType,
        buttons: DialogButtons,
        title: String,
        caption: String,
        message: String,
        details: String,
        owner: Window?
    ): Boolean = DetailDialog(type, buttons, caption, message, details).apply {
        this.title = title
        decorate(this, owner)
    }.let(::isConfirmed)

    /**
     * Dresses a dialog in the application theme and binds it to the window it belongs to.
     *
     * @param alert the dialog to prepare
     * @param owner window the dialog belongs to, `null` when it belongs to none
     */
    private fun decorate(alert: Alert, owner: Window?) {
        owner?.let(alert::initOwner)
        alert.dialogPane.scene?.let(AiGhostTheme::apply)
        (alert.dialogPane.scene?.window as? Stage)?.icons?.setAll(AiGhostIcons.application)
    }

    /**
     * Asks the user and reads the answer.
     *
     * A dialog closed with ESCAPE or with the window close button carries no answer at all, which
     * counts as a no, the same way the negative button does.
     *
     * @param alert the prepared dialog
     * @return `true` when the user picked the affirmative button
     */
    private fun isConfirmed(alert: Alert): Boolean =
        alert.showAndWait()
            .map { it.buttonData == ButtonBar.ButtonData.YES }
            .orElse(false) == true
}

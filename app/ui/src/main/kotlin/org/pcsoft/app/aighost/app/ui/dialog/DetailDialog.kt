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

import de.saxsys.mvvmfx.FluentViewLoader
import javafx.scene.control.Alert

/**
 * Dialog reporting something that carries more than one line of explanation.
 *
 * The dialog shows icon, caption and message like a plain alert, and adds a report the user unfolds
 * with a button - a list of the parts a document lost, for instance. The content is built by
 * [DetailDialogView], the alert itself only carries it and owns the buttons.
 *
 * The dialog is not shown here; [org.pcsoft.app.aighost.app.ui.AiGhostDialog] dresses it in the
 * theme and asks the user.
 *
 * @param type severity of the report
 * @param buttons buttons the user can close the dialog with
 * @param caption headline of the dialog
 * @param message text of the dialog
 * @param details report shown in the details pane
 */
class DetailDialog(
    type: DialogType,
    buttons: DialogButtons,
    caption: String,
    message: String,
    details: String
) : Alert(type.alertType) {

    /** Style class of the dialog pane, so the stylesheet can address the detailed dialog alone. */
    companion object {
        const val STYLE_CLASS: String = "detail-dialog"
    }

    private val viewModel: DetailDialogViewModel

    init {
        FluentViewLoader.fxmlView(DetailDialogView::class.java).load().apply {
            // The dialog draws its own header, so the one of the alert is dropped completely -
            // otherwise the caption and the icon would be shown twice.
            dialogPane.headerText = null
            dialogPane.graphic = null
            dialogPane.content = view
            this@DetailDialog.viewModel = this.viewModel
        }

        dialogPane.styleClass += STYLE_CLASS
        dialogPane.buttonTypes.setAll(buttons.buttonTypes)

        viewModel.type.value = type
        viewModel.caption.value = caption
        viewModel.message.value = message
        viewModel.details.value = details
    }

    /** Whether the details pane is unfolded. */
    var detailsVisible: Boolean
        get() = viewModel.detailsVisible.value
        set(value) {
            viewModel.detailsVisible.value = value
        }
}

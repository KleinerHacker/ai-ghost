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
import javafx.event.ActionEvent
import javafx.scene.control.Alert
import javafx.scene.control.ButtonType
import javafx.scene.layout.Region
import javafx.stage.StageStyle
import org.pcsoft.app.aighost.fx.model.project.design.DesignProperty

/**
 * Dialog editing the settings of the open project: the page geometry today, the typography once
 * IP-13 fills in the design sections.
 *
 * The dialog carries its own content - a navigation tree and the editor of the picked section, built
 * by [ProjectSettingsDialogView] - so the header of the plain alert is dropped. It edits a working
 * copy; [applyChanges] writes it into the design of the open project, and OK does the same before
 * closing. CANCEL, ESCAPE and the window close button leave the project untouched.
 *
 * The dialog is not shown here; [org.pcsoft.app.aighost.app.ui.AiGhostDialog] dresses it in the
 * theme and shows it.
 *
 * @param target the design of the open project, the object OK and APPLY write into
 * @param buttons buttons the dialog carries, an editor set by default
 */
class ProjectSettingsDialog(
    target: DesignProperty,
    buttons: DialogButtons = DialogButtons.OK_CANCEL_APPLY
) : Alert(AlertType.NONE) {

    /** Style class of the dialog pane, so the stylesheet can address this dialog alone. */
    companion object {
        const val STYLE_CLASS: String = "project-settings-dialog"
    }

    private val viewModel: ProjectSettingsDialogViewModel
    private val contentView: ProjectSettingsDialogView

    init {
        FluentViewLoader.fxmlView(ProjectSettingsDialogView::class.java).load().apply {
            dialogPane.headerText = null
            dialogPane.graphic = null
            dialogPane.content = view
            this@ProjectSettingsDialog.viewModel = this.viewModel
            this@ProjectSettingsDialog.contentView = this.codeBehind
        }
        initStyle(StageStyle.UTILITY)
        isResizable = true

        dialogPane.minHeight = Region.USE_PREF_SIZE
        dialogPane.styleClass += STYLE_CLASS
        dialogPane.buttonTypes.setAll(buttons.buttonTypes)

        viewModel.bindTarget(target)

        // APPLY stores without closing the dialog, so its action is consumed after the working copy
        // was written back.
        dialogPane.lookupButton(ButtonType.APPLY)?.apply {
            addEventFilter(ActionEvent.ACTION) { event ->
                viewModel.apply()
                event.consume()
            }
            disableProperty().bind(contentView.valid.not())
        }
        // OK writes the working copy back as well, so it must not close while the input is invalid.
        dialogPane.lookupButton(ButtonType.OK)?.disableProperty()?.bind(contentView.valid.not())
    }

    /** Writes the working copy into the design of the open project. */
    fun applyChanges() = viewModel.apply()
}

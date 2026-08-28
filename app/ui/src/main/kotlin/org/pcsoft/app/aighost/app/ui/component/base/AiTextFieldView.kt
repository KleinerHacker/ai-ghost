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

import de.saxsys.mvvmfx.FxmlView
import de.saxsys.mvvmfx.InjectViewModel
import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.scene.control.Button
import javafx.scene.control.TextField
import org.pcsoft.app.aighost.app.Messages
import org.pcsoft.app.aighost.app.ui.AiGhostDialog
import java.net.URL
import java.util.ResourceBundle

/**
 * View of [AiTextField], holding the input line and the wand at its end.
 *
 * The view asks the question the view model wants answered before a written text is given up, and
 * shows it in the window the field is part of.
 */
class AiTextFieldView : FxmlView<AiTextFieldViewModel>, Initializable {

    @FXML
    private lateinit var txtValue: TextField

    @FXML
    private lateinit var btnCreate: Button

    @InjectViewModel
    private lateinit var viewModel: AiTextFieldViewModel

    override fun initialize(location: URL?, resources: ResourceBundle?) {
        // The question is asked outside of the FXML, so it needs the same bundle the FXML around it
        // was resolved with.
        val messages = resources ?: Messages.bundle

        txtValue.textProperty().bindBidirectional(viewModel.text)
        txtValue.promptTextProperty().bind(viewModel.promptText)

        viewModel.confirmOverwrite = {
            AiGhostDialog.showWarningConfirm(
                messages.getString(OVERWRITE_TITLE_KEY),
                messages.getString(OVERWRITE_HEADER_KEY),
                messages.getString(OVERWRITE_CONTENT_KEY),
                txtValue.scene?.window
            )
        }

        // The action of the button would climb up to the component itself, where it would pass as
        // the action of the component and reach the outside past the question asked here.
        btnCreate.setOnAction { event ->
            event.consume()
            viewModel.create()
        }
    }

    private companion object {
        /** Key of the title of the overwrite question inside the resource bundle. */
        const val OVERWRITE_TITLE_KEY: String = "aiTextField.overwrite.title"

        /** Key of the headline of the overwrite question inside the resource bundle. */
        const val OVERWRITE_HEADER_KEY: String = "aiTextField.overwrite.header"

        /** Key of the text of the overwrite question inside the resource bundle. */
        const val OVERWRITE_CONTENT_KEY: String = "aiTextField.overwrite.content"
    }
}

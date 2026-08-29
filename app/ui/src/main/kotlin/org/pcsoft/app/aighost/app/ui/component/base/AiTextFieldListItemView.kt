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
import javafx.beans.binding.Bindings
import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.scene.control.Button
import javafx.scene.control.Tooltip
import org.pcsoft.app.aighost.app.Messages
import java.net.URL
import java.util.ResourceBundle

/**
 * View of [AiTextFieldListItem], holding the field of the entry and the bin removing it.
 *
 * Both controls report to the outside through the view model only, so the events of the field and of
 * the bin are stopped here: they would climb up to the item itself, where they would pass as the
 * events the item publishes and reach the outside twice.
 */
class AiTextFieldListItemView : FxmlView<AiTextFieldListItemViewModel>, Initializable {

    @FXML
    private lateinit var txtValue: AiTextField

    @FXML
    private lateinit var btnDelete: Button

    @FXML
    private lateinit var tipDelete: Tooltip

    @InjectViewModel
    private lateinit var viewModel: AiTextFieldListItemViewModel

    override fun initialize(location: URL?, resources: ResourceBundle?) {
        // The wording of the bin is read outside of the FXML, so it needs the same bundle the FXML
        // around it was resolved with.
        val messages = resources ?: Messages.bundle
        val defaultTooltip = messages.getString(DELETE_TOOLTIP_KEY)

        txtValue.text.bindBidirectional(viewModel.text)
        txtValue.promptText.bind(viewModel.promptText)

        // Whoever shows the item names what removing means here; as long as nobody does, the bin
        // explains itself in general words.
        tipDelete.textProperty().bind(
            Bindings.createStringBinding(
                { viewModel.deleteTooltip.value ?: defaultTooltip },
                viewModel.deleteTooltip
            )
        )

        txtValue.setOnCreateAiText { event ->
            event.consume()
            viewModel.create()
        }
        btnDelete.setOnAction { event ->
            event.consume()
            viewModel.delete()
        }
    }

    private companion object {
        /** Key of the general wording of the bin inside the resource bundle. */
        const val DELETE_TOOLTIP_KEY: String = "component.aiTextFieldListItem.delete.tooltip"
    }
}

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

import de.saxsys.mvvmfx.FxmlView
import de.saxsys.mvvmfx.InjectViewModel
import javafx.beans.binding.Bindings
import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.control.TextArea
import javafx.scene.control.TextFormatter
import org.pcsoft.app.aighost.app.Messages
import java.net.URL
import java.text.MessageFormat
import java.util.ResourceBundle

/**
 * View of [AiPromptArea], holding the writing surface and the footer below it.
 *
 * The view keeps the texts of the footer in step with the view model and stops the writing surface
 * from taking more characters than the prompt is allowed to hold.
 */
class AiPromptAreaView : FxmlView<AiPromptAreaViewModel>, Initializable {

    @FXML
    private lateinit var txaPrompt: TextArea

    @FXML
    private lateinit var btnOptimize: Button

    @FXML
    private lateinit var lblTokens: Label

    @FXML
    private lateinit var lblCounter: Label

    @InjectViewModel
    private lateinit var viewModel: AiPromptAreaViewModel

    override fun initialize(location: URL?, resources: ResourceBundle?) {
        // The footer builds its texts itself, so it needs the same bundle the FXML around it was
        // resolved with.
        val messages = resources ?: Messages.bundle

        txaPrompt.textFormatter = TextFormatter<String> { change ->
            val allowed = viewModel.limit(change.controlNewText)
            when {
                allowed.length == change.controlNewText.length -> change
                // The whole insertion is beyond the limit, so nothing of it is taken over.
                change.text.length <= change.controlNewText.length - allowed.length -> null
                else -> change.apply {
                    text = change.text.substring(0, change.text.length - (change.controlNewText.length - allowed.length))
                }
            }
        }
        txaPrompt.textProperty().bindBidirectional(viewModel.text)
        txaPrompt.promptTextProperty().bind(viewModel.promptText)

        lblCounter.textProperty().bind(
            Bindings.createStringBinding(
                {
                    MessageFormat.format(
                        messages.getString(COUNTER_KEY),
                        viewModel.length.get(),
                        viewModel.maxCharacters.get()
                    )
                },
                viewModel.length,
                viewModel.maxCharacters
            )
        )
        // Without a limit there is nothing to count against, so the counter gives its space back.
        lblCounter.visibleProperty().bind(viewModel.limited)
        lblCounter.managedProperty().bind(viewModel.limited)

        lblTokens.textProperty().bind(
            Bindings.createStringBinding(
                { MessageFormat.format(messages.getString(TOKENS_KEY), viewModel.tokens.get()) },
                viewModel.tokens
            )
        )

        viewModel.usage.addListener { _, _, usage -> applyUsage(usage) }
        applyUsage(viewModel.usage.get())

        btnOptimize.setOnAction { viewModel.optimize() }
    }

    /**
     * Colours the character counter according to how full the prompt is.
     *
     * @param usage the step the prompt has reached
     */
    private fun applyUsage(usage: AiPromptUsage) {
        lblCounter.styleClass.removeAll(AiPromptUsage.entries.mapNotNull { it.styleClass })
        usage.styleClass?.let { lblCounter.styleClass += it }
    }

    private companion object {
        /** Key of the character counter inside the resource bundle. */
        const val COUNTER_KEY: String = "aiPromptArea.counter"

        /** Key of the estimated token cost inside the resource bundle. */
        const val TOKENS_KEY: String = "aiPromptArea.tokens"
    }
}

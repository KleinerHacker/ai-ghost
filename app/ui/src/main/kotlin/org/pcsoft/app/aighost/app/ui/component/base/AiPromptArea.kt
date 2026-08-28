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

import de.saxsys.mvvmfx.FluentViewLoader
import javafx.beans.property.LongProperty
import javafx.beans.property.ObjectProperty
import javafx.beans.property.ObjectPropertyBase
import javafx.beans.property.ReadOnlyLongProperty
import javafx.beans.property.StringProperty
import javafx.event.ActionEvent
import javafx.event.EventHandler
import javafx.scene.layout.BorderPane

/**
 * Input area for a prompt the user writes for the AI.
 *
 * Below the writing surface sits a footer: on its left the wand button, which hands the prompt over
 * to be improved, next to it the estimated token cost of the text, and on its right the number of
 * characters written against the number of characters allowed. The area takes no more characters
 * than [maxCharacters] permits, so the limit shown is the limit that holds, and the counter changes
 * its colour as soon as the prompt comes close to it.
 *
 * The component owns its text but no action: pressing the wand fires [onOptimizePrompt], and whoever
 * shows the component decides what optimising a prompt means.
 */
class AiPromptArea : BorderPane() {

    private val viewModel: AiPromptAreaViewModel

    init {
        FluentViewLoader.fxmlView(AiPromptAreaView::class.java).let {
            it.root(this)
            it.load().let { tuple ->
                viewModel = tuple.viewModel
            }
        }
        viewModel.onOptimize = { fireEvent(ActionEvent(this, this)) }
    }

    /** The prompt the user wrote, empty while nothing has been typed. */
    val text: StringProperty by viewModel::text

    /** Text shown while the prompt is empty, absent when no hint is wanted. */
    val promptText: StringProperty by viewModel::promptText

    /**
     * Number of characters the prompt may hold.
     *
     * A value of zero or less means no limit at all; the character counter is then hidden, because
     * there is nothing to count against.
     */
    val maxCharacters: LongProperty by viewModel::maxCharacters

    /** Estimated number of tokens the written prompt costs. */
    val tokens: ReadOnlyLongProperty get() = viewModel.tokens

    /**
     * Handler called when the user asks for the prompt to be optimised.
     *
     * Setting a handler registers it for [ActionEvent.ACTION] on the component itself, which is the
     * way a JavaFX control publishes its own action, so the handler can be attached from FXML
     * through `onOptimizePrompt` as well.
     */
    val onOptimizePrompt: ObjectProperty<EventHandler<ActionEvent>?> =
        object : ObjectPropertyBase<EventHandler<ActionEvent>?>(null) {
            override fun invalidated() {
                setEventHandler(ActionEvent.ACTION, get())
            }

            override fun getBean(): Any = this@AiPromptArea

            override fun getName(): String = "onOptimizePrompt"
        }

    /**
     * Reads the handler called when the user asks for the prompt to be optimised.
     *
     * @return the registered handler, `null` while none is set
     */
    fun getOnOptimizePrompt(): EventHandler<ActionEvent>? = onOptimizePrompt.get()

    /**
     * Sets the handler called when the user asks for the prompt to be optimised.
     *
     * @param handler the handler to call, `null` to remove the current one
     */
    fun setOnOptimizePrompt(handler: EventHandler<ActionEvent>?) {
        onOptimizePrompt.set(handler)
    }
}

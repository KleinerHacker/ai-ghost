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
import javafx.beans.property.ObjectProperty
import javafx.beans.property.ObjectPropertyBase
import javafx.beans.property.StringProperty
import javafx.event.ActionEvent
import javafx.event.EventHandler
import javafx.scene.layout.BorderPane

/**
 * Single line input whose text can be written by the AI instead of by the user.
 *
 * At its right end sits the wand, which asks for the text to be created. What is already written
 * would be lost by that, so the wand asks back before it hands the request on: only an answered yes
 * fires [onCreateAiText], while an empty field asks nothing at all.
 *
 * The component owns its text but no action: whoever shows the component decides what creating a
 * text means and writes the result back into [text].
 */
class AiTextField : BorderPane() {

    private val viewModel: AiTextFieldViewModel

    init {
        FluentViewLoader.fxmlView(AiTextFieldView::class.java).let {
            it.root(this)
            it.load().let { tuple ->
                viewModel = tuple.viewModel
            }
        }
        viewModel.onCreate = { fireEvent(ActionEvent(this, this)) }
    }

    /** The text the field holds, empty while nothing has been written. */
    val text: StringProperty by viewModel::text

    /** Text shown while the field is empty, absent when no hint is wanted. */
    val promptText: StringProperty by viewModel::promptText

    /**
     * Handler called when the user asks the AI for a text.
     *
     * Setting a handler registers it for [ActionEvent.ACTION] on the component itself, which is the
     * way a JavaFX control publishes its own action, so the handler can be attached from FXML
     * through `onCreateAiText` as well.
     */
    val onCreateAiText: ObjectProperty<EventHandler<ActionEvent>?> =
        object : ObjectPropertyBase<EventHandler<ActionEvent>?>(null) {
            override fun invalidated() {
                setEventHandler(ActionEvent.ACTION, get())
            }

            override fun getBean(): Any = this@AiTextField

            override fun getName(): String = "onCreateAiText"
        }

    /**
     * Reads the handler called when the user asks the AI for a text.
     *
     * @return the registered handler, `null` while none is set
     */
    fun getOnCreateAiText(): EventHandler<ActionEvent>? = onCreateAiText.get()

    /**
     * Sets the handler called when the user asks the AI for a text.
     *
     * @param handler the handler to call, `null` to remove the current one
     */
    fun setOnCreateAiText(handler: EventHandler<ActionEvent>?) {
        onCreateAiText.set(handler)
    }
}

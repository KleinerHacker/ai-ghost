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
import javafx.event.EventHandler
import javafx.scene.layout.BorderPane

/**
 * One entry of a list of texts: an [AiTextField] and, at its end, the bin taking the entry out
 * again.
 *
 * The item is one line of a list that grows and shrinks with what the user writes. It holds a text
 * of its own and publishes two requests, one for each button it carries: [onCreateAiText] when the
 * wand asks the AI for a text, [onDeleteAction] when the bin asks for the entry to be removed.
 *
 * Neither request is carried out here. Whoever shows the item owns the list, decides whether the
 * entry may really go and writes an AI text back into [text].
 */
class AiTextFieldListItem : BorderPane() {

    private val viewModel: AiTextFieldListItemViewModel

    init {
        FluentViewLoader.fxmlView(AiTextFieldListItemView::class.java).let {
            it.root(this)
            it.load().let { tuple ->
                viewModel = tuple.viewModel
            }
        }
        viewModel.onCreate = {
            fireEvent(AiTextFieldListItemEvent(this, this, AiTextFieldListItemEvent.CREATE_AI_TEXT))
        }
        viewModel.onDelete = {
            fireEvent(AiTextFieldListItemEvent(this, this, AiTextFieldListItemEvent.DELETE))
        }
    }

    /** The text of the entry, empty while nothing has been written. */
    val text: StringProperty by viewModel::text

    /** Text shown while the entry is empty, absent when no hint is wanted. */
    val promptText: StringProperty by viewModel::promptText

    /**
     * Words the bin explains itself with, absent while the general wording is wanted.
     *
     * The item does not know what it holds, so a list of title lines says here that a title line is
     * removed.
     */
    val deleteTooltip: StringProperty by viewModel::deleteTooltip

    /**
     * Handler called when the user asks the AI for a text.
     *
     * Setting a handler registers it for [AiTextFieldListItemEvent.CREATE_AI_TEXT] on the component
     * itself, which is the way a JavaFX control publishes its own action, so the handler can be
     * attached from FXML through `onCreateAiText` as well.
     */
    val onCreateAiText: ObjectProperty<EventHandler<AiTextFieldListItemEvent>?> =
        object : ObjectPropertyBase<EventHandler<AiTextFieldListItemEvent>?>(null) {
            override fun invalidated() {
                setEventHandler(AiTextFieldListItemEvent.CREATE_AI_TEXT, get())
            }

            override fun getBean(): Any = this@AiTextFieldListItem

            override fun getName(): String = "onCreateAiText"
        }

    /**
     * Handler called when the user asks for the entry to be removed.
     *
     * Setting a handler registers it for [AiTextFieldListItemEvent.DELETE] on the component itself,
     * so the handler can be attached from FXML through `onDeleteAction` as well. The item stays
     * where it is until the handler takes it out of the list.
     */
    val onDeleteAction: ObjectProperty<EventHandler<AiTextFieldListItemEvent>?> =
        object : ObjectPropertyBase<EventHandler<AiTextFieldListItemEvent>?>(null) {
            override fun invalidated() {
                setEventHandler(AiTextFieldListItemEvent.DELETE, get())
            }

            override fun getBean(): Any = this@AiTextFieldListItem

            override fun getName(): String = "onDeleteAction"
        }

    /**
     * Reads the handler called when the user asks the AI for a text.
     *
     * @return the registered handler, `null` while none is set
     */
    fun getOnCreateAiText(): EventHandler<AiTextFieldListItemEvent>? = onCreateAiText.get()

    /**
     * Sets the handler called when the user asks the AI for a text.
     *
     * @param handler the handler to call, `null` to remove the current one
     */
    fun setOnCreateAiText(handler: EventHandler<AiTextFieldListItemEvent>?) {
        onCreateAiText.set(handler)
    }

    /**
     * Reads the handler called when the user asks for the entry to be removed.
     *
     * @return the registered handler, `null` while none is set
     */
    fun getOnDeleteAction(): EventHandler<AiTextFieldListItemEvent>? = onDeleteAction.get()

    /**
     * Sets the handler called when the user asks for the entry to be removed.
     *
     * @param handler the handler to call, `null` to remove the current one
     */
    fun setOnDeleteAction(handler: EventHandler<AiTextFieldListItemEvent>?) {
        onDeleteAction.set(handler)
    }
}

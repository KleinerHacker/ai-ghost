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
import javafx.beans.property.ListProperty
import javafx.beans.property.ObjectProperty
import javafx.beans.property.ObjectPropertyBase
import javafx.beans.property.StringProperty
import javafx.event.EventHandler
import javafx.scene.layout.VBox

/**
 * A list of texts the user builds up: one [AiTextFieldListItem] per entry, and below them the plus
 * asking for another one.
 *
 * The list is shown as a list: its entries stand in a bordered area of their own, separated line by
 * line, with the plus as the last row of that area. While there is no entry at all, a hint takes
 * their place; the words of that hint come from outside, because the list does not know what it
 * holds.
 *
 * [entries] is the single interface for the data: the list handed in there is the one the outside
 * owns, and a text the user writes is put back into it, so no copy of the entries exists. Everything
 * that changes the list itself is only published as a request - [onAddEntry] when another entry is
 * wanted, [onDeleteEntry] when an entry is to be removed, [onCreateAiText] when the AI is asked for
 * the text of an entry. Whoever owns the entries decides what a new entry holds and whether an entry
 * may really go.
 */
class AiTextFieldList : VBox() {

    private val viewModel: AiTextFieldListViewModel

    init {
        FluentViewLoader.fxmlView(AiTextFieldListView::class.java).let {
            it.root(this)
            it.load().let { tuple ->
                viewModel = tuple.viewModel
            }
        }
        viewModel.onAdd = { index ->
            fireEvent(AiTextFieldListEvent(this, this, AiTextFieldListEvent.ADD_ENTRY, index))
        }
        viewModel.onDelete = { index ->
            fireEvent(AiTextFieldListEvent(this, this, AiTextFieldListEvent.DELETE_ENTRY, index))
        }
        viewModel.onCreate = { index ->
            fireEvent(AiTextFieldListEvent(this, this, AiTextFieldListEvent.CREATE_AI_TEXT, index))
        }
    }

    /**
     * The entries of the list, in the order they are shown in.
     *
     * Handing a list in here shows that list; it is not copied, so a text the user writes stands in
     * it afterwards and an entry added or removed elsewhere reaches the screen.
     */
    val entries: ListProperty<String> by viewModel::entries

    /** Text shown in an empty entry, absent when no hint is wanted. */
    val promptText: StringProperty by viewModel::promptText

    /**
     * Text shown in place of the entries while the list is empty, absent when nothing is to be
     * shown.
     *
     * The list does not know what it holds, so a list of title lines says here that no title line is
     * there yet.
     */
    val emptyText: StringProperty by viewModel::emptyText

    /**
     * Words the plus explains itself with, absent while the general wording is wanted.
     *
     * The list does not know what it holds, so a list of title lines says here that a title line is
     * added.
     */
    val addTooltip: StringProperty by viewModel::addTooltip

    /**
     * Words the bin of an entry explains itself with, absent while the general wording is wanted.
     *
     * The list does not know what it holds, so a list of title lines says here that a title line is
     * removed.
     */
    val deleteTooltip: StringProperty by viewModel::deleteTooltip

    /**
     * Handler called when the user asks for another entry.
     *
     * Setting a handler registers it for [AiTextFieldListEvent.ADD_ENTRY] on the component itself,
     * which is the way a JavaFX control publishes its own action, so the handler can be attached
     * from FXML through `onAddEntry` as well. The entry is not added here.
     */
    val onAddEntry: ObjectProperty<EventHandler<AiTextFieldListEvent>?> =
        object : ObjectPropertyBase<EventHandler<AiTextFieldListEvent>?>(null) {
            override fun invalidated() {
                setEventHandler(AiTextFieldListEvent.ADD_ENTRY, get())
            }

            override fun getBean(): Any = this@AiTextFieldList

            override fun getName(): String = "onAddEntry"
        }

    /**
     * Handler called when the user asks for an entry to be removed.
     *
     * Setting a handler registers it for [AiTextFieldListEvent.DELETE_ENTRY] on the component
     * itself, so the handler can be attached from FXML through `onDeleteEntry` as well. The entry
     * stays where it is until the handler takes it out of [entries].
     */
    val onDeleteEntry: ObjectProperty<EventHandler<AiTextFieldListEvent>?> =
        object : ObjectPropertyBase<EventHandler<AiTextFieldListEvent>?>(null) {
            override fun invalidated() {
                setEventHandler(AiTextFieldListEvent.DELETE_ENTRY, get())
            }

            override fun getBean(): Any = this@AiTextFieldList

            override fun getName(): String = "onDeleteEntry"
        }

    /**
     * Handler called when the user asks the AI for the text of an entry.
     *
     * Setting a handler registers it for [AiTextFieldListEvent.CREATE_AI_TEXT] on the component
     * itself, so the handler can be attached from FXML through `onCreateAiText` as well. The text is
     * not written here.
     */
    val onCreateAiText: ObjectProperty<EventHandler<AiTextFieldListEvent>?> =
        object : ObjectPropertyBase<EventHandler<AiTextFieldListEvent>?>(null) {
            override fun invalidated() {
                setEventHandler(AiTextFieldListEvent.CREATE_AI_TEXT, get())
            }

            override fun getBean(): Any = this@AiTextFieldList

            override fun getName(): String = "onCreateAiText"
        }

    /**
     * Reads the handler called when the user asks for another entry.
     *
     * @return the registered handler, `null` while none is set
     */
    fun getOnAddEntry(): EventHandler<AiTextFieldListEvent>? = onAddEntry.get()

    /**
     * Sets the handler called when the user asks for another entry.
     *
     * @param handler the handler to call, `null` to remove the current one
     */
    fun setOnAddEntry(handler: EventHandler<AiTextFieldListEvent>?) {
        onAddEntry.set(handler)
    }

    /**
     * Reads the handler called when the user asks for an entry to be removed.
     *
     * @return the registered handler, `null` while none is set
     */
    fun getOnDeleteEntry(): EventHandler<AiTextFieldListEvent>? = onDeleteEntry.get()

    /**
     * Sets the handler called when the user asks for an entry to be removed.
     *
     * @param handler the handler to call, `null` to remove the current one
     */
    fun setOnDeleteEntry(handler: EventHandler<AiTextFieldListEvent>?) {
        onDeleteEntry.set(handler)
    }

    /**
     * Reads the handler called when the user asks the AI for the text of an entry.
     *
     * @return the registered handler, `null` while none is set
     */
    fun getOnCreateAiText(): EventHandler<AiTextFieldListEvent>? = onCreateAiText.get()

    /**
     * Sets the handler called when the user asks the AI for the text of an entry.
     *
     * @param handler the handler to call, `null` to remove the current one
     */
    fun setOnCreateAiText(handler: EventHandler<AiTextFieldListEvent>?) {
        onCreateAiText.set(handler)
    }
}

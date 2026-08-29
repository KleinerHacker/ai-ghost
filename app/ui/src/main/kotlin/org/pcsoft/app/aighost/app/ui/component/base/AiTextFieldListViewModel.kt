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

import de.saxsys.mvvmfx.ViewModel
import javafx.beans.property.ListProperty
import javafx.beans.property.SimpleListProperty
import javafx.beans.property.SimpleStringProperty
import javafx.beans.property.StringProperty
import javafx.collections.FXCollections

/**
 * View model of [AiTextFieldList].
 *
 * It knows the entries of the list and the words the list explains itself with. The entries are the
 * list handed in from outside, not a copy of it: a text the user writes is put back into that very
 * list, so whoever owns it reads the change there.
 *
 * The three requests the list publishes - another entry, an entry to be removed and an AI text - are
 * handed on from here to [AiTextFieldList], which turns them into the events the outside world
 * listens to. None of them is carried out here, because whoever owns the entries decides what a new
 * entry holds and whether an entry may really go.
 */
class AiTextFieldListViewModel : ViewModel {

    /**
     * The entries of the list, in the order they are shown in.
     *
     * The list is handed in from outside and stays the one the outside owns, so it is written into
     * instead of being copied.
     */
    val entries: ListProperty<String> =
        SimpleListProperty(this, "entries", FXCollections.observableArrayList())

    /** Text shown in an empty entry, absent when no hint is wanted. */
    val promptText: StringProperty = SimpleStringProperty(this, "promptText", null)

    /**
     * Text shown in place of the entries while the list is empty, absent when nothing is to be
     * shown.
     *
     * The list does not know what it holds, so whoever shows it says here what an empty list means.
     */
    val emptyText: StringProperty = SimpleStringProperty(this, "emptyText", null)

    /** Words the plus explains itself with, absent while the general wording is wanted. */
    val addTooltip: StringProperty = SimpleStringProperty(this, "addTooltip", null)

    /** Words the bin of an entry explains itself with, absent while the general wording is wanted. */
    val deleteTooltip: StringProperty = SimpleStringProperty(this, "deleteTooltip", null)

    /**
     * Called when the user asks for another entry.
     *
     * Set by [AiTextFieldList], which turns the request into the event the outside world listens to.
     */
    internal var onAdd: ((Int) -> Unit)? = null

    /**
     * Called when the user asks for an entry to be removed.
     *
     * Set by [AiTextFieldList], which turns the request into the event the outside world listens to.
     */
    internal var onDelete: ((Int) -> Unit)? = null

    /**
     * Called when the user asks the AI for the text of an entry.
     *
     * Set by [AiTextFieldList], which turns the request into the event the outside world listens to.
     */
    internal var onCreate: ((Int) -> Unit)? = null

    /**
     * Writes the text of the entry at the given position into the entries.
     *
     * A position beyond the list and a text that is there already are dropped, so an entry reporting
     * what it was just given does not travel through the list a second time.
     *
     * Called by [AiTextFieldListView] only.
     *
     * @param index position of the entry
     * @param value text the entry carries
     */
    internal fun setEntry(index: Int, value: String) {
        if (index < 0 || index >= entries.size) return
        if (entries[index] == value) return

        entries[index] = value
    }

    /**
     * Passes the request for another entry on to [AiTextFieldList].
     *
     * The new entry is not added here: whoever owns the entries decides what it holds.
     *
     * Called by [AiTextFieldListView] only.
     */
    internal fun add() {
        onAdd?.invoke(entries.size)
    }

    /**
     * Passes the request to remove an entry on to [AiTextFieldList].
     *
     * Whether the entry may really go is decided outside: the list only reports that the bin of that
     * position was pressed, it does not take the entry out.
     *
     * Called by [AiTextFieldListView] only.
     *
     * @param index position of the entry
     */
    internal fun delete(index: Int) {
        onDelete?.invoke(index)
    }

    /**
     * Passes the request for an AI text on to [AiTextFieldList].
     *
     * Called by [AiTextFieldListView] only.
     *
     * @param index position of the entry
     */
    internal fun create(index: Int) {
        onCreate?.invoke(index)
    }
}

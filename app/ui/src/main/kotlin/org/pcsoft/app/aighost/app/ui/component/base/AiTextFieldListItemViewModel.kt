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
import javafx.beans.property.SimpleStringProperty
import javafx.beans.property.StringProperty

/**
 * View model of [AiTextFieldListItem].
 *
 * It knows the text of the entry, the hint shown while the entry is empty and the words the cross
 * explains itself with. Both requests the item publishes - creating a text and removing the entry -
 * are handed on from here to [AiTextFieldListItem], which turns them into the events the outside
 * world listens to.
 *
 * The question whether a written text may be given up is asked by [AiTextField] itself, so this
 * view model does not ask anything.
 */
class AiTextFieldListItemViewModel : ViewModel {

    /** The text of the entry, empty while nothing has been written. */
    val text: StringProperty = SimpleStringProperty(this, "text", "")

    /** Text shown while the entry is empty, absent when no hint is wanted. */
    val promptText: StringProperty = SimpleStringProperty(this, "promptText", null)

    /**
     * Words the cross explains itself with, absent while the general wording is wanted.
     *
     * A list of title lines removes a title line, a list of characters removes a character: the item
     * itself does not know what it holds, so whoever shows it says what removing means here.
     */
    val deleteTooltip: StringProperty = SimpleStringProperty(this, "deleteTooltip", null)

    /**
     * Called when the user asks the AI for a text.
     *
     * Set by [AiTextFieldListItem], which turns the request into the event the outside world listens
     * to.
     */
    internal var onCreate: (() -> Unit)? = null

    /**
     * Called when the user asks for the entry to be removed.
     *
     * Set by [AiTextFieldListItem], which turns the request into the event the outside world listens
     * to.
     */
    internal var onDelete: (() -> Unit)? = null

    /**
     * Passes the request for an AI text on to [AiTextFieldListItem].
     *
     * The written text was already given up at this point, because [AiTextField] asks about it
     * before it reports the request at all.
     *
     * Called by [AiTextFieldListItemView] only.
     */
    internal fun create() {
        onCreate?.invoke()
    }

    /**
     * Passes the request to remove the entry on to [AiTextFieldListItem].
     *
     * Whether the entry may really go is decided outside: the item only reports that the cross was
     * pressed, it does not take itself out of the list.
     *
     * Called by [AiTextFieldListItemView] only.
     */
    internal fun delete() {
        onDelete?.invoke()
    }
}

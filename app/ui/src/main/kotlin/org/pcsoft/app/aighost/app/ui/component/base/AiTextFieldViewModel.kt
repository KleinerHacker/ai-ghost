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
 * View model of [AiTextField].
 *
 * It knows the text the field holds and, from it, whether asking the AI would throw written text
 * away. Asking the question is left to [AiTextFieldView], which owns the window the question is
 * shown in; the decision what happens with the answer is made here.
 */
class AiTextFieldViewModel : ViewModel {

    /** The text the field holds, empty while nothing has been written. */
    val text: StringProperty = SimpleStringProperty(this, "text", "")

    /** Text shown while the field is empty, absent when no hint is wanted. */
    val promptText: StringProperty = SimpleStringProperty(this, "promptText", null)

    /**
     * Called when the user asks the AI for a text.
     *
     * Set by [AiTextField], which turns the request into the event the outside world listens to.
     */
    internal var onCreate: (() -> Unit)? = null

    /**
     * Asks the user whether the written text may be overwritten.
     *
     * Set by [AiTextFieldView], which shows the question as a dialog. Answering with no, or not
     * answering at all, keeps the text.
     */
    internal var confirmOverwrite: (() -> Boolean)? = null

    /**
     * Passes the request of the user on to [AiTextField], after the written text was given up.
     *
     * A text written into the field would be replaced by whatever the AI delivers, so the request
     * is only passed on once that was confirmed. An empty field has nothing to lose and asks
     * nothing.
     *
     * Called by [AiTextFieldView] only.
     */
    internal fun create() {
        if (!text.value.isNullOrEmpty() && confirmOverwrite?.invoke() == false) {
            return
        }

        onCreate?.invoke()
    }
}

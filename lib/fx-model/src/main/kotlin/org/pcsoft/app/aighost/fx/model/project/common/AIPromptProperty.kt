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

package org.pcsoft.app.aighost.fx.model.project.common

import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.beans.property.StringProperty
import org.pcsoft.app.aighost.fx.model.internal.BeanFields
import org.pcsoft.app.aighost.model.project.common.AIPrompt

/**
 * Property wrapping the prompts a part of the manuscript is generated from and offering every field
 * of it as a property of its own.
 *
 * The wrapped object may be absent as long as no part sits above this property, so every field
 * property answers with a neutral value and drops what is written to it until then.
 *
 * This property model is handed out with its own type, so a caller reaches both prompts directly; it
 * is built by the part carrying them alone and therefore carries an internal constructor.
 */
class AIPromptProperty internal constructor() : SimpleObjectProperty<AIPrompt?>() {

    private val fields = BeanFields<AIPrompt> { fireValueChangedEvent() }

    /** Description of what the generated text is about, as a property of its own. */
    val contentPromptProperty: StringProperty = SimpleStringProperty()

    /** Description of what the generated text is about. */
    var contentPrompt: String?
        get() = contentPromptProperty.get()
        set(value) {
            contentPromptProperty.set(value)
        }

    /** Description of the tone the generated text is written in, as a property of its own. */
    val stylePromptProperty: StringProperty = SimpleStringProperty()

    /** Description of the tone the generated text is written in. */
    var stylePrompt: String?
        get() = stylePromptProperty.get()
        set(value) {
            stylePromptProperty.set(value)
        }

    init {
        fields.string(contentPromptProperty, "contentPrompt")
        fields.string(stylePromptProperty, "stylePrompt")

        // The field properties belong to another object after every exchange, so they are tied to the
        // one this property carries now.
        addListener { _, _, newValue -> fields.rebind(newValue) }
        fields.rebind(get())
    }

    /**
     * Reads both prompts of the wrapped object again and hands what changed to the field properties,
     * for a caller that wrote on the object past this model.
     */
    fun refresh() = fields.refresh()

}

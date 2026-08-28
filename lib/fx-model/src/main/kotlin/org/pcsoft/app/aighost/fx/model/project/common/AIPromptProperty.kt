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

import org.pcsoft.app.aighost.fx.model.property.common.OverrideObjectProperty
import org.pcsoft.app.aighost.fx.model.property.common.OverrideStringProperty
import org.pcsoft.app.aighost.model.project.common.AIPrompt

/**
 * Property wrapping the prompts a part of the manuscript is generated from and offering every field
 * of it as a property of its own.
 *
 * The wrapped object may be absent as long as no part sits above this property, so every field
 * property answers with a neutral value and drops what is written to it until then.
 */
internal class AIPromptProperty(
    setter: (AIPrompt?) -> Unit,
    getter: () -> AIPrompt?,
    fireEvent: () -> Unit
) : OverrideObjectProperty<AIPrompt?>(setter, getter, fireEvent) {

    /** Description of what the generated text is about, as a property of its own. */
    val contentPromptProperty: OverrideStringProperty = OverrideStringProperty(
        { newValue -> value?.also { it.contentPrompt = newValue ?: "" } },
        { value?.contentPrompt },
        { fireValueChangedEvent() }
    )

    /** Description of what the generated text is about. */
    var contentPrompt: String?
        get() = contentPromptProperty.get()
        set(value) {
            contentPromptProperty.set(value)
        }

    /** Description of the tone the generated text is written in, as a property of its own. */
    val stylePromptProperty: OverrideStringProperty = OverrideStringProperty(
        { newValue -> value?.also { it.stylePrompt = newValue ?: "" } },
        { value?.stylePrompt },
        { fireValueChangedEvent() }
    )

    /** Description of the tone the generated text is written in. */
    var stylePrompt: String?
        get() = stylePromptProperty.get()
        set(value) {
            stylePromptProperty.set(value)
        }

    /**
     * Called whenever the wrapped object itself is exchanged, so the properties of its fields belong
     * to another object afterwards and have to take over its values.
     */
    override fun invalidated() {
        super.invalidated()
        refreshFields()
    }

    override fun refresh() {
        super.refresh()
        refreshFields()
    }

    /**
     * Lets every field property take over the value of the object the property carries now. A field
     * whose value did not change reports nothing, so an exchanged object is not announced as a change
     * of every one of its fields.
     */
    private fun refreshFields() {
        contentPromptProperty.refresh()
        stylePromptProperty.refresh()
    }

}

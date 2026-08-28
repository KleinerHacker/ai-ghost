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

package org.pcsoft.app.aighost.fx.model.project.book

import javafx.collections.FXCollections
import org.pcsoft.app.aighost.fx.model.property.common.OverrideListProperty
import org.pcsoft.app.aighost.fx.model.property.common.OverrideObjectProperty
import org.pcsoft.app.aighost.fx.model.property.common.OverrideStringProperty
import org.pcsoft.app.aighost.model.project.book.Blurb

/**
 * Property wrapping the blurb of a book and offering its prompt and its text as properties of their
 * own.
 *
 * A book carries a blurb only after the user created it, so the wrapped object is absent until then
 * and the field properties answer with an empty prompt and with no paragraphs at all.
 */
internal class BlurbProperty(
    setter: (Blurb?) -> Unit,
    getter: () -> Blurb?,
    fireEvent: () -> Unit
) : OverrideObjectProperty<Blurb?>(setter, getter, fireEvent) {

    /** Prompt the blurb is generated from, as a property of its own. */
    val promptProperty: OverrideStringProperty = OverrideStringProperty(
        { newValue -> value?.also { it.prompt = newValue ?: "" } },
        { value?.prompt },
        { fireValueChangedEvent() }
    )

    /** Prompt the blurb is generated from. */
    var prompt: String?
        get() = promptProperty.get()
        set(value) {
            promptProperty.set(value)
        }

    /** Paragraphs of the blurb in their order, as a property of their own. */
    val paragraphProperty: OverrideListProperty<String> = OverrideListProperty(
        { newValue -> value?.also { it.paragraph = newValue } },
        { value?.paragraph },
        { fireValueChangedEvent() }
    )

    /** Paragraphs of the blurb in their order. */
    var paragraph: List<String>
        get() = paragraphProperty.get()
        set(value) {
            paragraphProperty.set(FXCollections.observableArrayList(value))
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
     * Lets the field properties take over the prompt and the paragraphs of the object the property
     * carries now. A field whose value did not change reports nothing, so an exchanged object is not
     * announced as a change of every one of its fields.
     */
    private fun refreshFields() {
        promptProperty.refresh()
        paragraphProperty.refresh()
    }

}

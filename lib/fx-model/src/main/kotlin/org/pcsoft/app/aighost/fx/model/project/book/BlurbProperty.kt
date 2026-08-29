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

import javafx.beans.property.ListProperty
import javafx.beans.property.SimpleListProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.beans.property.StringProperty
import javafx.collections.FXCollections
import org.pcsoft.app.aighost.fx.model.internal.BeanFields
import org.pcsoft.app.aighost.model.project.book.Blurb

/**
 * Property wrapping the blurb of a book and offering its prompt and its text as properties of their
 * own.
 *
 * A book carries a blurb only after the user created it, so the wrapped object is absent until then
 * and the field properties answer with an empty prompt and with no paragraphs at all.
 *
 * This property model is handed out with its own type, so a caller reaches the prompt and the
 * paragraphs directly; it is built by the book alone and therefore carries an internal constructor.
 */
class BlurbProperty internal constructor() : SimpleObjectProperty<Blurb?>() {

    private val fields = BeanFields<Blurb> { fireValueChangedEvent() }

    /** Prompt the blurb is generated from, as a property of its own. */
    val promptProperty: StringProperty = SimpleStringProperty()

    /** Prompt the blurb is generated from. */
    var prompt: String?
        get() = promptProperty.get()
        set(value) {
            promptProperty.set(value)
        }

    /** Paragraphs of the blurb in their order, as a property of their own. */
    val paragraphProperty: ListProperty<String> =
        SimpleListProperty(FXCollections.observableArrayList())

    /** Paragraphs of the blurb in their order. */
    var paragraph: List<String>
        get() = paragraphProperty.get()
        set(value) {
            paragraphProperty.setAll(value)
        }

    init {
        fields.string(promptProperty, "prompt")
        fields.list(paragraphProperty, "paragraph")

        // The field properties belong to another object after every exchange, so they are tied to the
        // one this property carries now.
        addListener { _, _, newValue -> fields.rebind(newValue) }
        fields.rebind(get())
    }

    /**
     * Reads the prompt and the paragraphs of the wrapped blurb again and hands what changed to the
     * field properties, for a caller that wrote on the blurb past this model.
     */
    fun refresh() = fields.refresh()

}

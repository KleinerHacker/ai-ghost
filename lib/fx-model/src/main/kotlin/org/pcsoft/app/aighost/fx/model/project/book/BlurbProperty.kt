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

import javafx.beans.property.BooleanProperty
import javafx.beans.property.ListProperty
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleListProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.beans.property.StringProperty
import javafx.collections.FXCollections
import org.pcsoft.app.aighost.fx.model.internal.BeanFields
import org.pcsoft.app.aighost.model.project.book.Blurb

/**
 * Property wrapping the blurb of a book and offering its prompt, its text and its switch as
 * properties of their own.
 *
 * A book always carries its blurb; whether that blurb belongs to the book is told by the switch
 * alone. The wrapped object is absent only as long as no book sits behind the property standing for
 * it, and the field properties answer with an empty prompt and with no paragraphs at all until then.
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

    /** Whether the blurb belongs to the book, as a property of its own. */
    val includedProperty: BooleanProperty = SimpleBooleanProperty()

    /** Whether the blurb belongs to the book. */
    var included: Boolean
        get() = includedProperty.get()
        set(value) {
            includedProperty.set(value)
        }

    init {
        fields.string(promptProperty, "prompt")
        fields.list(paragraphProperty, "paragraph")
        fields.boolean(includedProperty, "included")

        // The field properties belong to another object after every exchange, so they are tied to the
        // one this property carries now.
        addListener { _, _, newValue -> fields.rebind(newValue) }
        fields.rebind(get())
    }

    /**
     * Reads the prompt, the paragraphs and the switch of the wrapped blurb again and hands what
     * changed to the field properties, for a caller that wrote on the blurb past this model.
     */
    fun refresh() = fields.refresh()

}

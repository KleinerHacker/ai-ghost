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
import org.pcsoft.app.aighost.model.project.book.Copyright

/**
 * Property wrapping the copyright page of a book and offering its notice, the further lines below it
 * and its switch as properties of their own.
 *
 * A book always carries its copyright page; whether that page is printed is told by the switch alone.
 * The wrapped object is absent only as long as no book sits behind the property standing for it, and
 * the field properties answer with an empty notice and with no further lines at all until then.
 *
 * This property model is handed out with its own type, so a caller reaches the notice and the further
 * lines directly; it is built by the book alone and therefore carries an internal constructor.
 */
class CopyrightProperty internal constructor() : SimpleObjectProperty<Copyright?>() {

    private val fields = BeanFields<Copyright> { fireValueChangedEvent() }

    /** The copyright notice, as a property of its own. */
    val copyrightProperty: StringProperty = SimpleStringProperty()

    /** The copyright notice. */
    var copyright: String?
        get() = copyrightProperty.get()
        set(value) {
            copyrightProperty.set(value)
        }

    /** Further lines printed below the copyright notice, as a property of their own. */
    val copyrightAppendixProperty: ListProperty<String> =
        SimpleListProperty(FXCollections.observableArrayList())

    /** Further lines printed below the copyright notice. */
    var copyrightAppendix: List<String>
        get() = copyrightAppendixProperty.get()
        set(value) {
            copyrightAppendixProperty.setAll(value)
        }

    /** Whether the copyright page is printed in the book, as a property of its own. */
    val includedProperty: BooleanProperty = SimpleBooleanProperty()

    /** Whether the copyright page is printed in the book. */
    var included: Boolean
        get() = includedProperty.get()
        set(value) {
            includedProperty.set(value)
        }

    init {
        fields.string(copyrightProperty, "copyright")
        fields.list(copyrightAppendixProperty, "copyrightAppendix")
        fields.boolean(includedProperty, "included")

        // The field properties belong to another object after every exchange, so they are tied to the
        // one this property carries now.
        addListener { _, _, newValue -> fields.rebind(newValue) }
        fields.rebind(get())
    }

    /**
     * Reads the notice, the further lines and the switch of the wrapped copyright page again and hands
     * what changed to the field properties, for a caller that wrote on the page past this model.
     */
    fun refresh() = fields.refresh()

}

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
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleObjectProperty
import org.pcsoft.app.aighost.fx.model.internal.BeanFields
import org.pcsoft.app.aighost.model.project.book.Copyright

/**
 * Property wrapping the copyright page of a book and offering its switch as a property of its own.
 *
 * A book always carries its copyright page; whether that page is printed is told by the switch alone.
 * The wrapped object is absent only as long as no book sits behind the property standing for it.
 *
 * This property model is handed out with its own type, so a caller reaches the switch directly; it is
 * built by the book alone and therefore carries an internal constructor.
 */
class CopyrightProperty internal constructor() : SimpleObjectProperty<Copyright?>() {

    private val fields = BeanFields<Copyright> { fireValueChangedEvent() }

    /** Whether the copyright page is printed in the book, as a property of its own. */
    val includedProperty: BooleanProperty = SimpleBooleanProperty()

    /** Whether the copyright page is printed in the book. */
    var included: Boolean
        get() = includedProperty.get()
        set(value) {
            includedProperty.set(value)
        }

    init {
        fields.boolean(includedProperty, "included")

        // The field properties belong to another object after every exchange, so they are tied to the
        // one this property carries now.
        addListener { _, _, newValue -> fields.rebind(newValue) }
        fields.rebind(get())
    }

    /**
     * Reads the switch of the wrapped copyright page again and hands what changed to the field
     * property, for a caller that wrote on the page past this model.
     */
    fun refresh() = fields.refresh()

}

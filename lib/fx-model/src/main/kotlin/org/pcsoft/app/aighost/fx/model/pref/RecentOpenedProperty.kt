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

package org.pcsoft.app.aighost.fx.model.pref

import javafx.beans.property.IntegerProperty
import javafx.beans.property.ListProperty
import javafx.beans.property.SimpleIntegerProperty
import javafx.beans.property.SimpleListProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.collections.FXCollections
import org.pcsoft.app.aighost.fx.model.internal.BeanFields
import org.pcsoft.app.aighost.model.pref.RecentOpened

/**
 * Property wrapping the files the user opened last and offering every field of it as a property of
 * its own.
 *
 * The preferences always carry such an object, so the field properties answer with the values of the
 * one this property is tied to right now.
 *
 * This property model is handed out with its own type, so a caller reaches every field of the entry
 * list directly; it is built by the preferences alone and therefore carries an internal constructor.
 */
class RecentOpenedProperty internal constructor() : SimpleObjectProperty<RecentOpened>() {

    private val fields = BeanFields<RecentOpened> { fireValueChangedEvent() }

    /** Number of entries that are kept at most, as a property of its own. */
    val maxProperty: IntegerProperty = SimpleIntegerProperty()

    /** Number of entries that are kept at most. */
    var max: Int
        get() = maxProperty.get()
        set(value) {
            maxProperty.set(value)
        }

    /** Paths of the files the user opened last, as a property of their own. */
    val entriesProperty: ListProperty<String> =
        SimpleListProperty(FXCollections.observableArrayList())

    /** Paths of the files the user opened last. */
    var entries: List<String>
        get() = entriesProperty.get()
        set(value) {
            entriesProperty.setAll(value)
        }

    init {
        fields.integer(maxProperty, "max")
        fields.list(entriesProperty, "entries")

        // The field properties belong to another object after every exchange, so they are tied to the
        // one this property carries now.
        addListener { _, _, newValue -> fields.rebind(newValue) }
        fields.rebind(get())
    }

    /**
     * Reads every field of the wrapped object again and hands what changed to the field properties,
     * for a caller that wrote on the object past this model.
     */
    fun refresh() = fields.refresh()

}

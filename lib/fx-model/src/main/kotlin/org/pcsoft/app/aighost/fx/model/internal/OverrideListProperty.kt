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

package org.pcsoft.app.aighost.fx.model.internal

import javafx.beans.property.SimpleListProperty
import javafx.collections.FXCollections
import javafx.collections.ObservableList

/**
 * Property wrapping a plain list field of a parent model object.
 *
 * Reads are answered from [getter], writes are handed to [setter] - no matter whether they replace
 * the whole list, change its content or come from a binding - and [fireEvent] lets the parent
 * property report the change of its nested field as its own change.
 */
internal class OverrideListProperty<T>(
    private val setter: (List<T>) -> Unit,
    private val getter: () -> List<T>,
    private val fireEvent: () -> Unit
) : SimpleListProperty<T>() {

    // Guards the model object and the listeners while the property aligns itself with the model
    // object, so such an alignment is not reported back as a change of its own.
    private var syncing = true

    // The entries the parent property was told about last. A bound list reports a content change
    // twice - once as an invalidation of the binding and once as a change of the list itself - so the
    // parent property is only notified when the entries really differ.
    private var reportedEntries: List<T> = emptyList()

    init {
        // The base class only observes the content of a list it knows, so it is given one right away.
        // The model object must not be touched here - it may not exist yet when the property is built.
        super.set(FXCollections.observableArrayList())
        validate()
        syncing = false
    }

    override fun get(): ObservableList<T> {
        val list = super.get()
        val currentValue = getter()
        if (list != currentValue) {
            // The model object was changed past the property, so the observed list follows it.
            syncing = true
            try {
                list.setAll(currentValue)
                reportedEntries = ArrayList(currentValue)
            } finally {
                syncing = false
            }
        }

        return list
    }

    override fun getValue(): ObservableList<T> = get()

    override fun set(newValue: ObservableList<T>) {
        check(!isBound) { "A bound value cannot be set." }

        // The base class skips the notification when the same list instance is set again, so the
        // model object is written here as well and not only from invalidated().
        setter(newValue)
        super.set(newValue)
        validate()
    }

    override fun setValue(v: ObservableList<T>) = set(v)

    /**
     * Called by the base class for every change - a replaced list, a content change and the values
     * produced by a binding alike - and thus the single point where such a change reaches the model
     * object.
     */
    override fun invalidated() {
        if (syncing) return

        val entries = ArrayList(super.get() ?: emptyList())
        val changed = entries != reportedEntries

        syncing = true
        try {
            setter(entries)
            reportedEntries = entries
        } finally {
            syncing = false
        }

        if (changed) {
            fireEvent()
        }
    }

    /**
     * Reads the value through the base class. It marks itself invalid on every change and only
     * becomes valid again - and only then observes the content of its list again - when it is read,
     * so a change is only reported once the property has been validated.
     */
    private fun validate() {
        super.get()
    }

}

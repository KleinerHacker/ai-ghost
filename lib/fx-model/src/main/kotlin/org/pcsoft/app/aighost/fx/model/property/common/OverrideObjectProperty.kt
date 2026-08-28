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

package org.pcsoft.app.aighost.fx.model.property.common

import javafx.beans.property.SimpleObjectProperty

/**
 * Property wrapping a plain object field of a parent model object.
 *
 * Reads are answered from [getter], writes are handed to [setter] - no matter whether they come from
 * application code or from a binding - and [fireEvent] lets the parent property report the change of
 * its nested field as its own change.
 *
 * This is the base every nested property model is built on, the ones of the application and the ones
 * a plugin brings along for its own project part.
 */
open class OverrideObjectProperty<T>(
    private val setter: (T) -> Unit,
    private val getter: () -> T,
    private val fireEvent: () -> Unit
) : SimpleObjectProperty<T>() {

    // Guards the model object and the parent property while the property takes over a value the model
    // object already carries, so such an alignment is not reported back as a change of its own.
    private var refreshing = false

    override fun get(): T = getter()

    override fun getValue(): T = getter()

    override fun set(newValue: T) {
        check(!isBound) { "A bound value cannot be set." }

        // The base class skips the notification when its own cached value does not change, so the
        // model object is written here as well and not only from invalidated().
        setter(newValue)
        super.set(newValue)
        validate()
    }

    override fun setValue(v: T) = set(v)

    /**
     * Called by the base class for every change, including the ones produced by a binding, and thus
     * the single point where a bound value reaches the model object.
     */
    override fun invalidated() {
        if (refreshing) return

        setter(super.get())
    }

    override fun fireValueChangedEvent() {
        super.fireValueChangedEvent()

        if (!refreshing) {
            fireEvent()
        }
    }

    /**
     * Takes over the value the wrapped field carries now and reports it to everyone listening to this
     * property.
     *
     * The parent property calls this after the model object behind it was exchanged: the field then
     * belongs to another object and its value is a different one, which nothing else would notice -
     * a control bound to this property would keep showing the value of the previous object. The
     * change is not reported back to the parent property, which is the one announcing it already.
     *
     * A bound property keeps the value of its binding and is left alone.
     */
    open fun refresh() {
        if (isBound) return

        refreshing = true
        try {
            super.set(getter())
        } finally {
            refreshing = false
        }
        validate()
    }

    /**
     * Reads the value through the base class. It marks itself invalid on every change and only
     * becomes valid again - and only then reports a further change - when it is read, which the
     * overridden [get] does not do because it answers from the model object.
     */
    private fun validate() {
        super.get()
    }

}

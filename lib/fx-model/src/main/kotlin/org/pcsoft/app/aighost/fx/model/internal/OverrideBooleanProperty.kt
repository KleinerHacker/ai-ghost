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

import javafx.beans.property.SimpleBooleanProperty

/**
 * Property wrapping a plain boolean field of a parent model object.
 *
 * Reads are answered from [getter], writes are handed to [setter] - no matter whether they come from
 * application code or from a binding - and [fireEvent] lets the parent property report the change of
 * its nested field as its own change.
 */
internal class OverrideBooleanProperty(
    private val setter: (Boolean) -> Unit,
    private val getter: () -> Boolean,
    private val fireEvent: () -> Unit
) : SimpleBooleanProperty() {

    override fun get(): Boolean = getter()

    override fun getValue(): Boolean = getter()

    override fun set(newValue: Boolean) {
        check(!isBound) { "A bound value cannot be set." }

        // The base class skips the notification when its own cached value does not change, so the
        // model object is written here as well and not only from invalidated().
        setter(newValue)
        super.set(newValue)
    }

    override fun setValue(v: Boolean?) = set(v ?: false)

    /**
     * Called by the base class for every change, including the ones produced by a binding, and thus
     * the single point where a bound value reaches the model object.
     */
    override fun invalidated() {
        setter(super.get())
    }

    override fun fireValueChangedEvent() {
        super.fireValueChangedEvent()
        fireEvent()
    }

}

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

import javafx.collections.FXCollections
import org.pcsoft.app.aighost.fx.model.property.common.OverrideIntegerProperty
import org.pcsoft.app.aighost.fx.model.property.common.OverrideListProperty
import org.pcsoft.app.aighost.fx.model.property.common.OverrideObjectProperty
import org.pcsoft.app.aighost.model.pref.RecentOpened

internal class RecentOpenedProperty(
    setter: (RecentOpened) -> Unit,
    getter: () -> RecentOpened,
    fireEvent: () -> Unit
) : OverrideObjectProperty<RecentOpened>(setter, getter, fireEvent) {

    val maxProperty: OverrideIntegerProperty = OverrideIntegerProperty(
        { value.max = it },
        { value.max },
        { fireValueChangedEvent() }
    )

    var max: Int
        get() = maxProperty.value
        set(value) {
            maxProperty.value = value
        }

    val entriesProperty: OverrideListProperty<String> = OverrideListProperty<String>(
        { value.entries = it },
        { value.entries },
        { fireValueChangedEvent() }
    )

    var entries: List<String>
        get() = entriesProperty.value
        set(value) {
            entriesProperty.value = FXCollections.observableArrayList(value)
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
        maxProperty.refresh()
        entriesProperty.refresh()
    }

}

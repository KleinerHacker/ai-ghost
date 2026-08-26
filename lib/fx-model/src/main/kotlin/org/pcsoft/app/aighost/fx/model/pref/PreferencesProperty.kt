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

import javafx.beans.property.ObjectProperty
import javafx.beans.property.SimpleObjectProperty
import org.pcsoft.app.aighost.fx.model.property.common.OverrideObjectProperty
import org.pcsoft.app.aighost.model.pref.Preferences
import org.pcsoft.app.aighost.model.pref.RecentOpened
import org.pcsoft.app.aighost.model.pref.ThemeMode

/**
 * Property holding the preferences of the user and offering every field of that object - and every
 * field of the objects nested in it - as a property of its own.
 *
 * A change travels through the whole object tree in both directions: a changed field is reported by
 * every property between that field and this one, and an exchanged object - a settings file that was
 * loaded again for instance - is passed down to the properties of its fields, so a control bound to
 * any level shows the current value.
 *
 * The preferences object stays the same instance while one of its fields changes, so such a change
 * reaches a listener registered here as an invalidation. A `ChangeListener` registered directly on
 * this property compares the old value with the new one and therefore only sees the exchange of the
 * whole preferences object; a binding built on this property is re-evaluated in both cases.
 */
class PreferencesProperty(preferences: Preferences) : SimpleObjectProperty<Preferences>(preferences) {

    private val overrideThemeMode = OverrideObjectProperty(
        { value.themeMode = it },
        { value.themeMode },
        { fireValueChangedEvent() }
    )

    /** Visual appearance of the application, as a property of its own. */
    val themeModeProperty: ObjectProperty<ThemeMode>
        get() = overrideThemeMode

    /** Visual appearance of the application. */
    var themeMode: ThemeMode
        get() = themeModeProperty.value
        set(value) {
            themeModeProperty.value = value
        }

    private val overrideRecentOpened = RecentOpenedProperty(
        setter = { value.recentOpened = it },
        getter = { value.recentOpened },
        fireEvent = { fireValueChangedEvent() }
    )

    /** Files the user opened last, as a property of its own. */
    val recentOpenedProperty: ObjectProperty<RecentOpened>
        get() = overrideRecentOpened

    /** Files the user opened last. */
    var recentOpened: RecentOpened
        get() = recentOpenedProperty.value
        set(value) {
            recentOpenedProperty.value = value
        }

    init {
        // The constructor of the base class stores the object without announcing it, so the field
        // properties have to take over its values here - otherwise they would only align on the
        // first exchange and report the initial values as a change of their own.
        invalidated()
    }

    /**
     * Called whenever the preferences object itself is exchanged - a freshly loaded settings file for
     * instance - so the properties of its fields belong to another object afterwards and have to take
     * over its values.
     */
    override fun invalidated() {
        if (get() == null) return

        overrideThemeMode.refresh()
        overrideRecentOpened.refresh()
    }

}

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

package org.pcsoft.app.aighost.model.pref

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonPropertyOrder
import java.util.concurrent.CopyOnWriteArrayList

/**
 * Application wide settings of a single user.
 *
 * The settings are changed on the object itself, one property at a time. Every change notifies the
 * registered [Listener]s, so a part of the application that has to follow a setting registers here
 * instead of asking again and again. A change is only reported when the value really differs from
 * the one that was set before.
 *
 * The preferences are persisted as JSON. Unknown properties are ignored so a file written by a newer
 * version of the application can still be read, and every property carries a default so an older
 * file stays readable as well. Listeners are not part of the document and survive a reload of the
 * values, because reading writes into this object instead of replacing it.
 *
 * @param recentOpened Files the user opened last, ten at most by default.
 * @param themeMode Visual appearance of the application, [ThemeMode.SYSTEM] by default.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonPropertyOrder("recentOpened", "themeMode")
class Preferences(
    recentOpened: RecentOpened = RecentOpened(max = 10),
    themeMode: ThemeMode = ThemeMode.SYSTEM
) {

    private val listeners = CopyOnWriteArrayList<Listener>()

    /** Files the user opened last, the most recent one first. */
    var recentOpened: RecentOpened = recentOpened
        set(value) {
            if (field == value) return

            field = value
            notifyChanged(Property.RECENT_OPENED)
        }

    /** Visual appearance of the application. */
    var themeMode: ThemeMode = themeMode
        set(value) {
            if (field == value) return

            field = value
            notifyChanged(Property.THEME_MODE)
        }

    /**
     * Registers [listener] to be notified about every changed property.
     *
     * A listener is called on the thread that changed the property, after the new value is in place.
     * Registering the same listener twice makes it be called twice.
     */
    fun addListener(listener: Listener) {
        listeners.add(listener)
    }

    /** Removes [listener] again; a listener that is not registered is ignored. */
    fun removeListener(listener: Listener) {
        listeners.remove(listener)
    }

    private fun notifyChanged(property: Property) {
        listeners.forEach { it.onChanged(this, property) }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Preferences) return false

        return recentOpened == other.recentOpened && themeMode == other.themeMode
    }

    override fun hashCode(): Int = 31 * recentOpened.hashCode() + themeMode.hashCode()

    override fun toString(): String = "Preferences(recentOpened=$recentOpened, themeMode=$themeMode)"

    /** Property of the [Preferences] a change is reported for. */
    enum class Property {

        /** [Preferences.recentOpened] was changed. */
        RECENT_OPENED,

        /** [Preferences.themeMode] was changed. */
        THEME_MODE
    }

    /** Notified whenever a property of the preferences was changed. */
    fun interface Listener {

        /**
         * Called after [property] was changed to its new value.
         *
         * The new value is read from [preferences]; the value that was replaced is gone, because the
         * preferences are changed in place.
         *
         * @param preferences The preferences the change happened on.
         * @param property The property that was changed.
         */
        fun onChanged(preferences: Preferences, property: Property)
    }
}

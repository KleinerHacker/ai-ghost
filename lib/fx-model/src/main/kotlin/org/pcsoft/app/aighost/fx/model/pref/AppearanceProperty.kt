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
import org.pcsoft.app.aighost.fx.model.internal.BeanFields
import org.pcsoft.app.aighost.model.pref.Appearance
import org.pcsoft.app.aighost.model.pref.ThemeMode

/**
 * Property wrapping the appearance settings of the application and offering every field of it as a
 * property of its own.
 *
 * The preferences always carry such an object, so the field properties answer with the values of the
 * one this property is tied to right now.
 *
 * The settings dialog edits the theme directly, so this property model is handed out with its own
 * type; it is built by the preferences alone and therefore carries an internal constructor.
 */
class AppearanceProperty internal constructor() : SimpleObjectProperty<Appearance>() {

    private val fields = BeanFields<Appearance> { fireValueChangedEvent() }

    /** Visual theme the application follows, as a property of its own. */
    val themeModeProperty: ObjectProperty<ThemeMode> = SimpleObjectProperty()

    /** Visual theme the application follows. */
    var themeMode: ThemeMode
        get() = themeModeProperty.get()
        set(value) {
            themeModeProperty.set(value)
        }

    init {
        fields.reference(themeModeProperty, "themeMode")

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

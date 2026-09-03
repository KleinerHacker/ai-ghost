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

import javafx.beans.property.SimpleObjectProperty
import org.pcsoft.app.aighost.fx.model.internal.BeanFields
import org.pcsoft.app.aighost.model.pref.Ai
import org.pcsoft.app.aighost.model.pref.Appearance
import org.pcsoft.app.aighost.model.pref.Editor
import org.pcsoft.app.aighost.model.pref.Preferences
import org.pcsoft.app.aighost.model.pref.RecentOpened

/**
 * Property holding the preferences of the user and offering every field of that object - and every
 * field of the objects nested in it - as a property of its own.
 *
 * A change travels through the whole object tree in both directions: a changed field is reported by
 * every property between that field and this one, and an exchanged object - a settings file that was
 * loaded again for instance - is passed down to the properties of its fields, so a control bound to
 * any level shows the current value.
 *
 * Every block nested in the preferences is handed out with its own type, so a user interface reaches
 * the fields of the recently opened files, of the appearance, of the AI settings and of the writing
 * surface through the property standing for that block.
 *
 * The preferences object stays the same instance while one of its fields changes, so such a change
 * reaches a listener registered here as an invalidation. A `ChangeListener` registered directly on
 * this property compares the old value with the new one and therefore only sees the exchange of the
 * whole preferences object; a binding built on this property is re-evaluated in both cases.
 */
class PreferencesProperty(preferences: Preferences) : SimpleObjectProperty<Preferences>(preferences) {

    private val fields = BeanFields<Preferences> { fireValueChangedEvent() }

    /** Files the user opened last, as a property of its own. */
    val recentOpenedProperty: RecentOpenedProperty = RecentOpenedProperty()

    /** Files the user opened last. */
    var recentOpened: RecentOpened
        get() = recentOpenedProperty.get()
        set(value) {
            recentOpenedProperty.set(value)
        }

    /** Visual appearance of the application, as a property of its own. */
    val appearanceProperty: AppearanceProperty = AppearanceProperty()

    /** Visual appearance of the application. */
    var appearance: Appearance
        get() = appearanceProperty.get()
        set(value) {
            appearanceProperty.set(value)
        }

    /** Settings of the AI functionality, as a property of its own. */
    val aiProperty: AiProperty = AiProperty()

    /** Settings of the AI functionality. */
    var ai: Ai
        get() = aiProperty.get()
        set(value) {
            aiProperty.set(value)
        }

    /** Settings of the writing surface, as a property of its own. */
    val editorProperty: EditorProperty = EditorProperty()

    /** Settings of the writing surface. */
    var editor: Editor
        get() = editorProperty.get()
        set(value) {
            editorProperty.set(value)
        }

    init {
        fields.model(recentOpenedProperty, "recentOpened", recentOpenedProperty::refresh)
        fields.model(appearanceProperty, "appearance", appearanceProperty::refresh)
        fields.model(aiProperty, "ai", aiProperty::refresh)
        fields.model(editorProperty, "editor", editorProperty::refresh)

        // The field properties belong to another object after every exchange, so they are tied to the
        // one this property carries now. The constructor of the base class stored the object without
        // announcing it, so they are tied to it right here as well.
        addListener { _, _, newValue -> fields.rebind(newValue) }
        fields.rebind(get())
    }

    /**
     * Reads every field of the preferences again - and every field of the objects nested in them - and
     * hands what changed to the field properties.
     *
     * This is what a caller uses after writing on the preferences object past this model: a plain
     * object reports nothing, so nobody would notice such a write otherwise.
     */
    fun refresh() = fields.refresh()

}

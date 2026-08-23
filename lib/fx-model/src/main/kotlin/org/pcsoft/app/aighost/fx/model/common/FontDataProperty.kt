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

package org.pcsoft.app.aighost.fx.model.common

import org.pcsoft.app.aighost.fx.model.internal.OverrideBooleanProperty
import org.pcsoft.app.aighost.fx.model.internal.OverrideIntegerProperty
import org.pcsoft.app.aighost.fx.model.internal.OverrideObjectProperty
import org.pcsoft.app.aighost.fx.model.internal.OverrideStringProperty
import org.pcsoft.app.aighost.model.common.FontData

/**
 * Property wrapping the font of a piece of text and offering every field of it as a property of its
 * own.
 *
 * The wrapped object may be absent - a style that is not there at all - so every field property
 * answers with a neutral value and drops what is written to it as long as no font sits behind this
 * property.
 */
internal class FontDataProperty(
    setter: (FontData?) -> Unit,
    getter: () -> FontData?,
    fireEvent: () -> Unit
) : OverrideObjectProperty<FontData?>(setter, getter, fireEvent) {

    /** Family name of the font, as a property of its own. */
    val nameProperty: OverrideStringProperty = OverrideStringProperty(
        { newValue -> value?.also { it.name = newValue ?: "" } },
        { value?.name },
        { fireValueChangedEvent() }
    )

    // A property carries a name of its own, so the accessors of the wrapped field are given another
    // name on the JVM side - otherwise they would silently replace the one of the base class.
    /** Family name of the font. */
    var name: String?
        @JvmName("getFontName") get() = nameProperty.get()
        @JvmName("setFontName") set(value) {
            nameProperty.set(value)
        }

    /** Font size in points, as a property of its own. */
    val sizeProperty: OverrideIntegerProperty = OverrideIntegerProperty(
        { newValue -> value?.also { it.size = newValue } },
        { value?.size ?: 0 },
        { fireValueChangedEvent() }
    )

    /** Font size in points. */
    var size: Int
        get() = sizeProperty.get()
        set(value) {
            sizeProperty.set(value)
        }

    /** Whether the text is drawn in a bold weight, as a property of its own. */
    val boldProperty: OverrideBooleanProperty = OverrideBooleanProperty(
        { newValue -> value?.also { it.bold = newValue } },
        { value?.bold ?: false },
        { fireValueChangedEvent() }
    )

    /** Whether the text is drawn in a bold weight. */
    var bold: Boolean
        get() = boldProperty.get()
        set(value) {
            boldProperty.set(value)
        }

    /** Whether the text is drawn slanted, as a property of its own. */
    val italicProperty: OverrideBooleanProperty = OverrideBooleanProperty(
        { newValue -> value?.also { it.italic = newValue } },
        { value?.italic ?: false },
        { fireValueChangedEvent() }
    )

    /** Whether the text is drawn slanted. */
    var italic: Boolean
        get() = italicProperty.get()
        set(value) {
            italicProperty.set(value)
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
        nameProperty.refresh()
        sizeProperty.refresh()
        boldProperty.refresh()
        italicProperty.refresh()
    }

}

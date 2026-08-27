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

import org.pcsoft.app.aighost.fx.model.property.common.OverrideObjectProperty
import org.pcsoft.app.aighost.model.common.Alignment
import org.pcsoft.app.aighost.model.common.FontData
import org.pcsoft.app.aighost.model.common.StyleData

/**
 * Property wrapping the appearance of a piece of text and offering every field of it - and every
 * field of the font nested in it - as a property of its own.
 *
 * The wrapped object may be absent, so every field property answers with a neutral value and drops
 * what is written to it as long as no style sits behind this property.
 */
internal class StyleDataProperty(
    setter: (StyleData?) -> Unit,
    getter: () -> StyleData?,
    fireEvent: () -> Unit
) : OverrideObjectProperty<StyleData?>(setter, getter, fireEvent) {

    /** Font the text is rendered with, as a property of its own. */
    val fontProperty: FontDataProperty = FontDataProperty(
        { newValue -> value?.also { it.font = newValue ?: FontData() } },
        { value?.font },
        { fireValueChangedEvent() }
    )

    /** Font the text is rendered with. */
    var font: FontData?
        get() = fontProperty.get()
        set(value) {
            fontProperty.set(value)
        }

    /** Horizontal placement of the text, as a property of its own. */
    val alignmentProperty: OverrideObjectProperty<Alignment?> = OverrideObjectProperty(
        { newValue -> value?.also { it.alignment = newValue ?: Alignment.LEFT } },
        { value?.alignment },
        { fireValueChangedEvent() }
    )

    /** Horizontal placement of the text. */
    var alignment: Alignment?
        get() = alignmentProperty.get()
        set(value) {
            alignmentProperty.set(value)
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
        fontProperty.refresh()
        alignmentProperty.refresh()
    }

}

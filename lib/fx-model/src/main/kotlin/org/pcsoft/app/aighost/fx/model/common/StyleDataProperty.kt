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

import javafx.beans.property.ObjectProperty
import javafx.beans.property.SimpleObjectProperty
import org.pcsoft.app.aighost.fx.model.internal.BeanFields
import org.pcsoft.app.aighost.model.common.Alignment
import org.pcsoft.app.aighost.model.common.FontData
import org.pcsoft.app.aighost.model.common.StyleData

/**
 * Property wrapping the appearance of a piece of text and offering every field of it - and every
 * field of the font nested in it - as a property of its own.
 *
 * The wrapped object may be absent, so every field property answers with a neutral value and drops
 * what is written to it as long as no style sits behind this property.
 *
 * This property model is handed out with its own type, so a caller reaches every field of the style
 * directly; it is built by the object carrying it alone and therefore carries an internal constructor.
 */
class StyleDataProperty internal constructor() : SimpleObjectProperty<StyleData?>() {

    private val fields = BeanFields<StyleData> { fireValueChangedEvent() }

    /** Font the text is rendered with, as a property of its own. */
    val fontProperty: FontDataProperty = FontDataProperty()

    /** Font the text is rendered with. */
    var font: FontData?
        get() = fontProperty.get()
        set(value) {
            fontProperty.set(value)
        }

    /** Horizontal placement of the text, as a property of its own. */
    val alignmentProperty: ObjectProperty<Alignment?> = SimpleObjectProperty()

    /** Horizontal placement of the text. */
    var alignment: Alignment?
        get() = alignmentProperty.get()
        set(value) {
            alignmentProperty.set(value)
        }

    init {
        fields.model(fontProperty, "font", fontProperty::refresh)
        fields.reference(alignmentProperty, "alignment")

        // The field properties belong to another object after every exchange, so they are tied to the
        // one this property carries now.
        addListener { _, _, newValue -> fields.rebind(newValue) }
        fields.rebind(get())
    }

    /**
     * Reads every field of the wrapped style again - and every field of the font nested in it - and
     * hands what changed to the field properties, for a caller that wrote on the style past this
     * model.
     */
    fun refresh() = fields.refresh()

}

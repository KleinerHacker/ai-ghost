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

import javafx.beans.property.BooleanProperty
import javafx.beans.property.IntegerProperty
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleIntegerProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.beans.property.StringProperty
import org.pcsoft.app.aighost.fx.model.internal.BeanFields
import org.pcsoft.app.aighost.model.common.FontData

/**
 * Property wrapping the font of a piece of text and offering every field of it as a property of its
 * own.
 *
 * The wrapped object may be absent - a style that is not there at all - so every field property
 * answers with a neutral value and drops what is written to it as long as no font sits behind this
 * property.
 *
 * This property model is handed out with its own type, so a caller reaches every field of the font
 * directly; it is built by the object carrying it alone and therefore carries an internal constructor.
 */
class FontDataProperty internal constructor() : SimpleObjectProperty<FontData?>() {

    private val fields = BeanFields<FontData> { fireValueChangedEvent() }

    /** Family name of the font, as a property of its own. */
    val nameProperty: StringProperty = SimpleStringProperty()

    // A property carries a name of its own, so the accessors of the wrapped field are given another
    // name on the JVM side - otherwise they would silently replace the one of the base class.
    /** Family name of the font. */
    var name: String?
        @JvmName("getFontName") get() = nameProperty.get()
        @JvmName("setFontName") set(value) {
            nameProperty.set(value)
        }

    /** Font size in points, as a property of its own. */
    val sizeProperty: IntegerProperty = SimpleIntegerProperty()

    /** Font size in points. */
    var size: Int
        get() = sizeProperty.get()
        set(value) {
            sizeProperty.set(value)
        }

    /** Whether the text is drawn in a bold weight, as a property of its own. */
    val boldProperty: BooleanProperty = SimpleBooleanProperty()

    /** Whether the text is drawn in a bold weight. */
    var bold: Boolean
        get() = boldProperty.get()
        set(value) {
            boldProperty.set(value)
        }

    /** Whether the text is drawn slanted, as a property of its own. */
    val italicProperty: BooleanProperty = SimpleBooleanProperty()

    /** Whether the text is drawn slanted. */
    var italic: Boolean
        get() = italicProperty.get()
        set(value) {
            italicProperty.set(value)
        }

    init {
        fields.string(nameProperty, "name")
        fields.integer(sizeProperty, "size")
        fields.boolean(boldProperty, "bold")
        fields.boolean(italicProperty, "italic")

        // The field properties belong to another object after every exchange, so they are tied to the
        // one this property carries now.
        addListener { _, _, newValue -> fields.rebind(newValue) }
        fields.rebind(get())
    }

    /**
     * Reads every field of the wrapped font again and hands what changed to the field properties, for
     * a caller that wrote on the font past this model.
     */
    fun refresh() = fields.refresh()

}

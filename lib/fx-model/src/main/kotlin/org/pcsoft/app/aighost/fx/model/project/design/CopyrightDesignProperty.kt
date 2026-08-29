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

package org.pcsoft.app.aighost.fx.model.project.design

import javafx.beans.property.BooleanProperty
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleObjectProperty
import org.pcsoft.app.aighost.fx.model.common.StyleDataProperty
import org.pcsoft.app.aighost.fx.model.internal.BeanFields
import org.pcsoft.app.aighost.model.common.StyleData
import org.pcsoft.app.aighost.model.project.design.CopyrightDesign

/**
 * Property wrapping the design settings of the copyright page and offering every field of it - and
 * every field of the style nested in it - as a property of its own.
 *
 * The wrapped object may be absent as long as no design sits above this property, so every field
 * property answers with a neutral value and drops what is written to it until then.
 */
internal class CopyrightDesignProperty : SimpleObjectProperty<CopyrightDesign?>() {

    private val fields = BeanFields<CopyrightDesign> { fireValueChangedEvent() }

    /** Appearance of the copyright page, as a property of its own. */
    val styleProperty: StyleDataProperty = StyleDataProperty()

    /** Appearance of the copyright page. */
    var style: StyleData?
        get() = styleProperty.get()
        set(value) {
            styleProperty.set(value)
        }

    /** Whether a separate copyright page is printed, as a property of its own. */
    val showProperty: BooleanProperty = SimpleBooleanProperty()

    /** Whether a separate copyright page is printed. */
    var show: Boolean
        get() = showProperty.get()
        set(value) {
            showProperty.set(value)
        }

    init {
        fields.model(styleProperty, "style", styleProperty::refresh)
        fields.boolean(showProperty, "show")

        // The field properties belong to another object after every exchange, so they are tied to the
        // one this property carries now.
        addListener { _, _, newValue -> fields.rebind(newValue) }
        fields.rebind(get())
    }

    /**
     * Reads every field of the wrapped object again - and every field of the style nested in it - and
     * hands what changed to the field properties, for a caller that wrote on the object past this
     * model.
     */
    fun refresh() = fields.refresh()

}

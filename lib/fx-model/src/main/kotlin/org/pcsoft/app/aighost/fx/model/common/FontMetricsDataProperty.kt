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

import javafx.beans.property.DoubleProperty
import javafx.beans.property.SimpleDoubleProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.beans.property.StringProperty
import org.pcsoft.app.aighost.fx.model.internal.BeanFields
import org.pcsoft.app.aighost.model.common.FontMetricsData

/**
 * Property wrapping the fingerprint of a font family and offering every field of it as a property of
 * its own.
 *
 * The wrapped object may be absent - a font whose family was never measured - so every field property
 * answers with a neutral value and drops what is written to it as long as no fingerprint sits behind
 * this property.
 *
 * This property model is handed out with its own type, so a caller reaches every field of the
 * fingerprint directly; it is built by the object carrying it alone and therefore carries an internal
 * constructor.
 */
class FontMetricsDataProperty internal constructor() : SimpleObjectProperty<FontMetricsData?>() {

    private val fields = BeanFields<FontMetricsData> { fireValueChangedEvent() }

    /** Digest over the widths of the reference set, as a property of its own. */
    val widthsProperty: StringProperty = SimpleStringProperty()

    /** Digest over the widths of the reference set. */
    var widths: String?
        get() = widthsProperty.get()
        set(value) {
            widthsProperty.set(value)
        }

    /** Distance above the base line in points, as a property of its own. */
    val ascentProperty: DoubleProperty = SimpleDoubleProperty()

    /** Distance above the base line in points. */
    var ascent: Double
        get() = ascentProperty.get()
        set(value) {
            ascentProperty.set(value)
        }

    /** Distance below the base line in points, as a property of its own. */
    val descentProperty: DoubleProperty = SimpleDoubleProperty()

    /** Distance below the base line in points. */
    var descent: Double
        get() = descentProperty.get()
        set(value) {
            descentProperty.set(value)
        }

    /** Gap between two lines in points, as a property of its own. */
    val leadingProperty: DoubleProperty = SimpleDoubleProperty()

    /** Gap between two lines in points. */
    var leading: Double
        get() = leadingProperty.get()
        set(value) {
            leadingProperty.set(value)
        }

    init {
        fields.string(widthsProperty, "widths")
        fields.double(ascentProperty, "ascent")
        fields.double(descentProperty, "descent")
        fields.double(leadingProperty, "leading")

        // The field properties belong to another object after every exchange, so they are tied to the
        // one this property carries now.
        addListener { _, _, newValue -> fields.rebind(newValue) }
        fields.rebind(get())
    }

    /**
     * Reads every field of the wrapped fingerprint again and hands what changed to the field
     * properties, for a caller that wrote on the fingerprint past this model.
     */
    fun refresh() = fields.refresh()

}

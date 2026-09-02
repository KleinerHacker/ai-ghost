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
import javafx.beans.property.DoubleProperty
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleDoubleProperty
import javafx.beans.property.SimpleObjectProperty
import org.pcsoft.app.aighost.fx.model.internal.BeanFields
import org.pcsoft.app.aighost.model.project.design.PageFormat

/**
 * Property wrapping the geometry of a page and offering every measure of it as a property of its own.
 *
 * The wrapped object may be absent, so every field property answers with a neutral value and drops
 * what is written to it as long as no page format sits behind this property.
 *
 * This property model is handed out with its own type, so a caller reaches every measure of the page
 * directly; it is built by the design carrying it alone and therefore carries an internal constructor.
 */
class PageFormatProperty internal constructor() : SimpleObjectProperty<PageFormat?>() {

    private val fields = BeanFields<PageFormat> { fireValueChangedEvent() }

    /** Width of the page in points, as a property of its own. */
    val widthProperty: DoubleProperty = SimpleDoubleProperty()

    /** Width of the page in points. */
    var width: Double
        get() = widthProperty.get()
        set(value) {
            widthProperty.set(value)
        }

    /** Height of the page in points, as a property of its own. */
    val heightProperty: DoubleProperty = SimpleDoubleProperty()

    /** Height of the page in points. */
    var height: Double
        get() = heightProperty.get()
        set(value) {
            heightProperty.set(value)
        }

    /** Empty space at the spine of the page in points, as a property of its own. */
    val innerMarginProperty: DoubleProperty = SimpleDoubleProperty()

    /** Empty space at the spine of the page in points. */
    var innerMargin: Double
        get() = innerMarginProperty.get()
        set(value) {
            innerMarginProperty.set(value)
        }

    /** Empty space at the open edge of the page in points, as a property of its own. */
    val outerMarginProperty: DoubleProperty = SimpleDoubleProperty()

    /** Empty space at the open edge of the page in points. */
    var outerMargin: Double
        get() = outerMarginProperty.get()
        set(value) {
            outerMarginProperty.set(value)
        }

    /** Empty space above the text of the page in points, as a property of its own. */
    val topMarginProperty: DoubleProperty = SimpleDoubleProperty()

    /** Empty space above the text of the page in points. */
    var topMargin: Double
        get() = topMarginProperty.get()
        set(value) {
            topMarginProperty.set(value)
        }

    /** Empty space below the text of the page in points, as a property of its own. */
    val bottomMarginProperty: DoubleProperty = SimpleDoubleProperty()

    /** Empty space below the text of the page in points. */
    var bottomMargin: Double
        get() = bottomMarginProperty.get()
        set(value) {
            bottomMarginProperty.set(value)
        }

    /** Whether the inner and outer margin swap sides between an odd and an even page, as a property of its own. */
    val mirroredMarginsProperty: BooleanProperty = SimpleBooleanProperty()

    /** Whether the inner and outer margin swap sides between an odd and an even page. */
    var mirroredMargins: Boolean
        get() = mirroredMarginsProperty.get()
        set(value) {
            mirroredMarginsProperty.set(value)
        }

    init {
        fields.double(widthProperty, "width")
        fields.double(heightProperty, "height")
        fields.double(innerMarginProperty, "innerMargin")
        fields.double(outerMarginProperty, "outerMargin")
        fields.double(topMarginProperty, "topMargin")
        fields.double(bottomMarginProperty, "bottomMargin")
        fields.boolean(mirroredMarginsProperty, "mirroredMargins")

        // The field properties belong to another object after every exchange, so they are tied to the
        // one this property carries now.
        addListener { _, _, newValue -> fields.rebind(newValue) }
        fields.rebind(get())
    }

    /**
     * Reads every measure of the wrapped page format again and hands what changed to the field
     * properties, for a caller that wrote on the page format past this model.
     */
    fun refresh() = fields.refresh()

}

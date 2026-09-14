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

import javafx.beans.property.IntegerProperty
import javafx.beans.property.ObjectProperty
import javafx.beans.property.SimpleIntegerProperty
import javafx.beans.property.SimpleObjectProperty
import org.pcsoft.app.aighost.fx.model.internal.BeanFields
import org.pcsoft.app.aighost.model.project.design.PageNumberCountingMode
import org.pcsoft.app.aighost.model.project.design.PageNumberDesign
import org.pcsoft.app.aighost.model.project.design.PageNumberPosition

/**
 * Property wrapping the page numbering settings of a project and offering every field of it as a
 * property of its own.
 *
 * The wrapped object may be absent, so every field property answers with a neutral value and drops
 * what is written to it as long as no page numbering sits behind this property.
 *
 * This property model is handed out with its own type, so a caller reaches every field of the page
 * numbering directly; it is built by the design carrying it alone and therefore carries an internal
 * constructor.
 */
class PageNumberDesignProperty internal constructor() : SimpleObjectProperty<PageNumberDesign?>() {

    private val fields = BeanFields<PageNumberDesign> { fireValueChangedEvent() }

    /** Where the number is printed, or [PageNumberPosition.OFF] to print none, as a property of its own. */
    val positionProperty: ObjectProperty<PageNumberPosition?> = SimpleObjectProperty()

    /** Where the number is printed, or [PageNumberPosition.OFF] to print none. */
    var position: PageNumberPosition?
        get() = positionProperty.get()
        set(value) {
            positionProperty.set(value)
        }

    /** Display value of the first counted sheet, as a property of its own. */
    val startNumberProperty: IntegerProperty = SimpleIntegerProperty()

    /** Display value of the first counted sheet. */
    var startNumber: Int
        get() = startNumberProperty.get()
        set(value) {
            startNumberProperty.set(value)
        }

    /** How the title page and the copyright page affect the counter, as a property of its own. */
    val countingModeProperty: ObjectProperty<PageNumberCountingMode?> = SimpleObjectProperty()

    /** How the title page and the copyright page affect the counter. */
    var countingMode: PageNumberCountingMode?
        get() = countingModeProperty.get()
        set(value) {
            countingModeProperty.set(value)
        }

    init {
        fields.reference(positionProperty, "position")
        fields.integer(startNumberProperty, "startNumber")
        fields.reference(countingModeProperty, "countingMode")

        // The field properties belong to another object after every exchange, so they are tied to the
        // one this property carries now.
        addListener { _, _, newValue -> fields.rebind(newValue) }
        fields.rebind(get())
    }

    /**
     * Reads every field of the wrapped page numbering again and hands what changed to the field
     * properties, for a caller that wrote on the page numbering past this model.
     */
    fun refresh() = fields.refresh()

}

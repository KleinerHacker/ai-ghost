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

import org.pcsoft.app.aighost.fx.model.common.StyleDataProperty
import org.pcsoft.app.aighost.fx.model.property.common.OverrideObjectProperty
import org.pcsoft.app.aighost.model.common.StyleData
import org.pcsoft.app.aighost.model.project.design.TextDesign

/**
 * Property wrapping the design settings of the body text of a project and offering the style it carries - and every
 * field of that style - as a property of its own.
 *
 * The wrapped object may be absent as long as no design sits above this property, so the style
 * property answers with a neutral value and drops what is written to it until then.
 */
internal class TextDesignProperty(
    setter: (TextDesign?) -> Unit,
    getter: () -> TextDesign?,
    fireEvent: () -> Unit
) : OverrideObjectProperty<TextDesign?>(setter, getter, fireEvent) {

    /** Appearance of the body text, as a property of its own. */
    val styleProperty: StyleDataProperty = StyleDataProperty(
        { newValue -> value?.also { it.style = newValue ?: StyleData() } },
        { value?.style },
        { fireValueChangedEvent() }
    )

    /** Appearance of the body text. */
    var style: StyleData?
        get() = styleProperty.get()
        set(value) {
            styleProperty.set(value)
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
        styleProperty.refresh()
    }

}

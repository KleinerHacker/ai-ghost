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
import org.pcsoft.app.aighost.fx.model.internal.OverrideBooleanProperty
import org.pcsoft.app.aighost.fx.model.internal.OverrideObjectProperty
import org.pcsoft.app.aighost.model.common.StyleData
import org.pcsoft.app.aighost.model.project.design.CopyrightDesign

/**
 * Property wrapping the design settings of the copyright page and offering every field of it - and
 * every field of the style nested in it - as a property of its own.
 *
 * The wrapped object may be absent as long as no design sits above this property, so every field
 * property answers with a neutral value and drops what is written to it until then.
 */
internal class CopyrightDesignProperty(
    setter: (CopyrightDesign?) -> Unit,
    getter: () -> CopyrightDesign?,
    fireEvent: () -> Unit
) : OverrideObjectProperty<CopyrightDesign?>(setter, getter, fireEvent) {

    /** Appearance of the copyright page, as a property of its own. */
    val styleProperty: StyleDataProperty = StyleDataProperty(
        { newValue -> value?.also { it.style = newValue ?: StyleData() } },
        { value?.style },
        { fireValueChangedEvent() }
    )

    /** Appearance of the copyright page. */
    var style: StyleData?
        get() = styleProperty.get()
        set(value) {
            styleProperty.set(value)
        }

    /** Whether a separate copyright page is printed, as a property of its own. */
    val showProperty: OverrideBooleanProperty = OverrideBooleanProperty(
        { newValue -> value?.also { it.show = newValue } },
        { value?.show ?: false },
        { fireValueChangedEvent() }
    )

    /** Whether a separate copyright page is printed. */
    var show: Boolean
        get() = showProperty.get()
        set(value) {
            showProperty.set(value)
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
        showProperty.refresh()
    }

}

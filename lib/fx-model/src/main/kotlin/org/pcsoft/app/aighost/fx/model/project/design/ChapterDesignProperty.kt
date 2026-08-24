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
import org.pcsoft.app.aighost.fx.model.internal.OverrideObjectProperty
import org.pcsoft.app.aighost.model.common.StyleData
import org.pcsoft.app.aighost.model.project.design.ChapterDesign

/**
 * Property wrapping the design settings of a chapter and offering every field of it - and every field
 * of the styles nested in it - as a property of its own.
 *
 * The wrapped object may be absent as long as no design sits above this property, so every field
 * property answers with a neutral value and drops what is written to it until then.
 */
internal class ChapterDesignProperty(
    setter: (ChapterDesign?) -> Unit,
    getter: () -> ChapterDesign?,
    fireEvent: () -> Unit
) : OverrideObjectProperty<ChapterDesign?>(setter, getter, fireEvent) {

    /** Appearance of a chapter heading, as a property of its own. */
    val titleStyleProperty: StyleDataProperty = StyleDataProperty(
        { newValue -> value?.also { it.titleStyle = newValue ?: StyleData() } },
        { value?.titleStyle },
        { fireValueChangedEvent() }
    )

    /** Appearance of a chapter heading. */
    var titleStyle: StyleData?
        get() = titleStyleProperty.get()
        set(value) {
            titleStyleProperty.set(value)
        }

    /** Appearance of the further chapter heading lines, as a property of its own. */
    val titleAppendixStyleProperty: StyleDataProperty = StyleDataProperty(
        { newValue -> value?.also { it.titleAppendixStyle = newValue ?: StyleData() } },
        { value?.titleAppendixStyle },
        { fireValueChangedEvent() }
    )

    /** Appearance of the further chapter heading lines. */
    var titleAppendixStyle: StyleData?
        get() = titleAppendixStyleProperty.get()
        set(value) {
            titleAppendixStyleProperty.set(value)
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
        titleStyleProperty.refresh()
        titleAppendixStyleProperty.refresh()
    }

}

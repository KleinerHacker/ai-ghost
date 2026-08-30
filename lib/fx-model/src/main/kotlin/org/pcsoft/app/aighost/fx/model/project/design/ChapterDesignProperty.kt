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

import javafx.beans.property.SimpleObjectProperty
import org.pcsoft.app.aighost.fx.model.common.StyleDataProperty
import org.pcsoft.app.aighost.fx.model.internal.BeanFields
import org.pcsoft.app.aighost.model.common.StyleData
import org.pcsoft.app.aighost.model.project.design.ChapterDesign

/**
 * Property wrapping the design settings of a chapter and offering every field of it - and every field
 * of the styles nested in it - as a property of its own.
 *
 * The wrapped object may be absent as long as no design sits above this property, so every field
 * property answers with a neutral value and drops what is written to it until then.
 *
 * This property model is handed out with its own type, so a caller reaches both styles directly; it
 * is built by the design carrying it alone and therefore carries an internal constructor.
 */
class ChapterDesignProperty internal constructor() : SimpleObjectProperty<ChapterDesign?>() {

    private val fields = BeanFields<ChapterDesign> { fireValueChangedEvent() }

    /** Appearance of a chapter heading, as a property of its own. */
    val titleStyleProperty: StyleDataProperty = StyleDataProperty()

    /** Appearance of a chapter heading. */
    var titleStyle: StyleData?
        get() = titleStyleProperty.get()
        set(value) {
            titleStyleProperty.set(value)
        }

    /** Appearance of the further chapter heading lines, as a property of its own. */
    val titleAppendixStyleProperty: StyleDataProperty = StyleDataProperty()

    /** Appearance of the further chapter heading lines. */
    var titleAppendixStyle: StyleData?
        get() = titleAppendixStyleProperty.get()
        set(value) {
            titleAppendixStyleProperty.set(value)
        }

    init {
        fields.model(titleStyleProperty, "titleStyle", titleStyleProperty::refresh)
        fields.model(titleAppendixStyleProperty, "titleAppendixStyle", titleAppendixStyleProperty::refresh)

        // The field properties belong to another object after every exchange, so they are tied to the
        // one this property carries now.
        addListener { _, _, newValue -> fields.rebind(newValue) }
        fields.rebind(get())
    }

    /**
     * Reads both styles of the wrapped object again - and every field of them - and hands what changed
     * to the field properties, for a caller that wrote on the object past this model.
     */
    fun refresh() = fields.refresh()

}

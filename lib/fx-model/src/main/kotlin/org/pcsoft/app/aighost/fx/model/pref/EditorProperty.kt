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

package org.pcsoft.app.aighost.fx.model.pref

import javafx.beans.property.BooleanProperty
import javafx.beans.property.LongProperty
import javafx.beans.property.ObjectProperty
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleLongProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.beans.property.StringProperty
import org.pcsoft.app.aighost.fx.model.internal.BeanFields
import org.pcsoft.app.aighost.model.pref.Editor
import org.pcsoft.app.aighost.model.pref.WritingMode

/**
 * Property wrapping the settings of the writing surface and offering every field of it as a property
 * of its own.
 *
 * The preferences always carry such an object, so the field properties answer with the values of the
 * one this property is tied to right now.
 *
 * The writing surface reads the typing pause from this model to configure how far apart two edits of
 * the same paragraph may be and still count as one undo step; it is built by the preferences alone
 * and therefore carries an internal constructor.
 */
class EditorProperty internal constructor() : SimpleObjectProperty<Editor>() {

    private val fields = BeanFields<Editor> { fireValueChangedEvent() }

    /** Typing pause folding consecutive edits of one paragraph into a single undo entry, as a property of its own. */
    val paragraphMergePauseMillisProperty: LongProperty = SimpleLongProperty()

    /** Typing pause folding consecutive edits of one paragraph into a single undo entry, in milliseconds. */
    var paragraphMergePauseMillis: Long
        get() = paragraphMergePauseMillisProperty.get()
        set(value) {
            paragraphMergePauseMillisProperty.set(value)
        }

    /** Whether the writing surface's single sheet is switched to writing or to preview, as a property of its own. */
    val writingModeProperty: ObjectProperty<WritingMode> = SimpleObjectProperty()

    /** Whether the writing surface's single sheet is switched to writing or to preview. */
    var writingMode: WritingMode
        get() = writingModeProperty.get()
        set(value) {
            writingModeProperty.set(value)
        }

    /** Whether the Inspector column of the editor is collapsed, as a property of its own. */
    val inspectorCollapsedProperty: BooleanProperty = SimpleBooleanProperty()

    /** Whether the Inspector column of the editor is collapsed. */
    var inspectorCollapsed: Boolean
        get() = inspectorCollapsedProperty.get()
        set(value) {
            inspectorCollapsedProperty.set(value)
        }

    /** Anchor id of the part last navigated to on the writing surface, as a property of its own. */
    val lastAnchorIdProperty: StringProperty = SimpleStringProperty()

    /** Anchor id of the part last navigated to on the writing surface; `null` as long as nothing was picked yet. */
    var lastAnchorId: String?
        get() = lastAnchorIdProperty.get()
        set(value) {
            lastAnchorIdProperty.set(value)
        }

    init {
        fields.long(paragraphMergePauseMillisProperty, "paragraphMergePauseMillis")
        fields.reference(writingModeProperty, "writingMode")
        fields.boolean(inspectorCollapsedProperty, "inspectorCollapsed")
        fields.string(lastAnchorIdProperty, "lastAnchorId")

        // The field properties belong to another object after every exchange, so they are tied to the
        // one this property carries now.
        addListener { _, _, newValue -> fields.rebind(newValue) }
        fields.rebind(get())
    }

    /**
     * Reads every field of the wrapped object again and hands what changed to the field properties,
     * for a caller that wrote on the object past this model.
     */
    fun refresh() = fields.refresh()

}

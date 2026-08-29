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

package org.pcsoft.app.aighost.fx.model.project.book

import javafx.beans.property.ListProperty
import javafx.beans.property.SimpleListProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.beans.property.StringProperty
import javafx.collections.FXCollections
import org.pcsoft.app.aighost.fx.model.internal.BeanFields
import org.pcsoft.app.aighost.fx.model.project.common.AIPromptProperty
import org.pcsoft.app.aighost.model.project.book.BookPart
import org.pcsoft.app.aighost.model.project.common.AIPrompt

/**
 * Property wrapping a written part of a book - a prolog, a chapter or an epilog - and offering the
 * fields they share as properties of their own.
 *
 * The wrapped object may be absent - a prolog the book does not carry - so every field property
 * answers with a neutral value and drops what is written to it as long as no part sits behind this
 * property.
 *
 * A part carrying further fields of its own registers them in [fields] and is then treated exactly
 * like the shared ones.
 */
internal abstract class BookPartProperty<T : BookPart?> : SimpleObjectProperty<T>() {

    /** The fields of the wrapped part, the shared ones and the ones a derived class adds. */
    protected val fields: BeanFields<BookPart> = BeanFields { fireValueChangedEvent() }

    /** Heading of the part, as a property of its own. */
    val titleProperty: StringProperty = SimpleStringProperty()

    /** Heading of the part. */
    var title: String?
        get() = titleProperty.get()
        set(value) {
            titleProperty.set(value)
        }

    /** Further heading lines shown below the title, as a property of their own. */
    val titleAppendixProperty: ListProperty<String> =
        SimpleListProperty(FXCollections.observableArrayList())

    /** Further heading lines shown below the title. */
    var titleAppendix: List<String>
        get() = titleAppendixProperty.get()
        set(value) {
            titleAppendixProperty.setAll(value)
        }

    /** Prompts the text of the part is generated from, as a property of their own. */
    val promptsProperty: AIPromptProperty = AIPromptProperty()

    /** Prompts the text of the part is generated from. */
    var prompts: AIPrompt?
        get() = promptsProperty.get()
        set(value) {
            promptsProperty.set(value)
        }

    /** Paragraphs of the part in their order, as a property of their own. */
    val paragraphProperty: ListProperty<String> =
        SimpleListProperty(FXCollections.observableArrayList())

    /** Paragraphs of the part in their order. */
    var paragraph: List<String>
        get() = paragraphProperty.get()
        set(value) {
            paragraphProperty.setAll(value)
        }

    init {
        fields.string(titleProperty, "title")
        fields.list(titleAppendixProperty, "titleAppendix")
        fields.model(promptsProperty, "prompts", promptsProperty::refresh)
        fields.list(paragraphProperty, "paragraph")

        // The field properties belong to another object after every exchange, so they are tied to the
        // one this property carries now. A derived class registers its own fields before the first
        // part arrives, so they are rebound along with the shared ones.
        addListener { _, _, newValue -> fields.rebind(newValue) }
    }

    /**
     * Reads every field of the wrapped part again - and both prompts nested in it - and hands what
     * changed to the field properties, for a caller that wrote on the part past this model.
     */
    open fun refresh() = fields.refresh()

}

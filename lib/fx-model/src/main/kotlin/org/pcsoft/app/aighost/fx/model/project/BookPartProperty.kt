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

package org.pcsoft.app.aighost.fx.model.project

import javafx.collections.FXCollections
import org.pcsoft.app.aighost.fx.model.internal.OverrideListProperty
import org.pcsoft.app.aighost.fx.model.internal.OverrideObjectProperty
import org.pcsoft.app.aighost.fx.model.internal.OverrideStringProperty
import org.pcsoft.app.aighost.model.project.BookPart

/**
 * Property wrapping a written part of a book - a prolog, a chapter or an epilog - and offering the
 * fields they share as properties of their own.
 *
 * The wrapped object may be absent - a prolog the book does not carry - so every field property
 * answers with a neutral value and drops what is written to it as long as no part sits behind this
 * property.
 */
internal abstract class BookPartProperty<T : BookPart?>(
    setter: (T) -> Unit,
    getter: () -> T,
    fireEvent: () -> Unit
) : OverrideObjectProperty<T>(setter, getter, fireEvent) {

    /** Heading of the part, as a property of its own. */
    val titleProperty: OverrideStringProperty = OverrideStringProperty(
        { newValue -> value?.also { it.title = newValue ?: "" } },
        { value?.title },
        { fireValueChangedEvent() }
    )

    /** Heading of the part. */
    var title: String?
        get() = titleProperty.get()
        set(value) {
            titleProperty.set(value)
        }

    /** Further heading lines shown below the title, as a property of their own. */
    val titleAppendixProperty: OverrideListProperty<String> = OverrideListProperty(
        { newValue -> value?.also { it.titleAppendix = newValue } },
        { value?.titleAppendix },
        { fireValueChangedEvent() }
    )

    /** Further heading lines shown below the title. */
    var titleAppendix: List<String>
        get() = titleAppendixProperty.get()
        set(value) {
            titleAppendixProperty.set(FXCollections.observableArrayList(value))
        }

    /** Paragraphs of the part in their order, as a property of their own. */
    val paragraphProperty: OverrideListProperty<String> = OverrideListProperty(
        { newValue -> value?.also { it.paragraph = newValue } },
        { value?.paragraph },
        { fireValueChangedEvent() }
    )

    /** Paragraphs of the part in their order. */
    var paragraph: List<String>
        get() = paragraphProperty.get()
        set(value) {
            paragraphProperty.set(FXCollections.observableArrayList(value))
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
     *
     * A part carrying further fields of its own extends this and refreshes them as well.
     */
    protected open fun refreshFields() {
        titleProperty.refresh()
        titleAppendixProperty.refresh()
        paragraphProperty.refresh()
    }

}

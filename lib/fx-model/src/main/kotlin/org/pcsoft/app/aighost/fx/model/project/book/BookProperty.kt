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

import javafx.collections.FXCollections
import org.pcsoft.app.aighost.fx.model.property.common.OverrideListProperty
import org.pcsoft.app.aighost.fx.model.project.ProjectPartProperty
import org.pcsoft.app.aighost.fx.model.property.common.OverrideStringProperty
import org.pcsoft.app.aighost.model.project.book.Blurb
import org.pcsoft.app.aighost.model.project.book.Book
import org.pcsoft.app.aighost.model.project.book.Chapter
import org.pcsoft.app.aighost.model.project.book.Epilog
import org.pcsoft.app.aighost.model.project.book.Prolog

/**
 * Property wrapping the manuscript of a project and offering every field of it - and every field of
 * the objects nested in it - as a property of its own.
 *
 * Prolog, epilog and blurb exist only after the user created them, so the properties standing for
 * them carry no object until then and their field properties answer with neutral values. The chapters
 * are offered as a list of the plain objects, because the user arranges them as a whole.
 */
internal class BookProperty(
    setter: (Book?) -> Unit,
    getter: () -> Book?,
    fireEvent: () -> Unit
) : ProjectPartProperty<Book>(setter, getter, fireEvent) {

    /** Main title of the book, as a property of its own. */
    val titleProperty: OverrideStringProperty = OverrideStringProperty(
        { newValue -> value?.also { it.title = newValue ?: "" } },
        { value?.title },
        { fireValueChangedEvent() }
    )

    /** Main title of the book. */
    var title: String?
        get() = titleProperty.get()
        set(value) {
            titleProperty.set(value)
        }

    /** Further title lines shown below the main title, as a property of their own. */
    val titleAppendixProperty: OverrideListProperty<String> = OverrideListProperty(
        { newValue -> value?.also { it.titleAppendix = newValue } },
        { value?.titleAppendix },
        { fireValueChangedEvent() }
    )

    /** Further title lines shown below the main title. */
    var titleAppendix: List<String>
        get() = titleAppendixProperty.get()
        set(value) {
            titleAppendixProperty.set(FXCollections.observableArrayList(value))
        }

    /** Prolog printed before the first chapter, as a property of its own. */
    val prologProperty: PrologProperty = PrologProperty(
        { newValue -> value?.also { it.prolog = newValue } },
        { value?.prolog },
        { fireValueChangedEvent() }
    )

    /** Prolog printed before the first chapter. */
    var prolog: Prolog?
        get() = prologProperty.get()
        set(value) {
            prologProperty.set(value)
        }

    /** Chapters of the book in their user defined order, as a property of their own. */
    val chaptersProperty: OverrideListProperty<Chapter> = OverrideListProperty(
        { newValue -> value?.also { it.chapters = newValue } },
        { value?.chapters },
        { fireValueChangedEvent() }
    )

    /** Chapters of the book in their user defined order. */
    var chapters: List<Chapter>
        get() = chaptersProperty.get()
        set(value) {
            chaptersProperty.set(FXCollections.observableArrayList(value))
        }

    /** Epilog printed after the last chapter, as a property of its own. */
    val epilogProperty: EpilogProperty = EpilogProperty(
        { newValue -> value?.also { it.epilog = newValue } },
        { value?.epilog },
        { fireValueChangedEvent() }
    )

    /** Epilog printed after the last chapter. */
    var epilog: Epilog?
        get() = epilogProperty.get()
        set(value) {
            epilogProperty.set(value)
        }

    /** Advertising text printed on the cover, as a property of its own. */
    val blurbProperty: BlurbProperty = BlurbProperty(
        { newValue -> value?.also { it.blurb = newValue } },
        { value?.blurb },
        { fireValueChangedEvent() }
    )

    /** Advertising text printed on the cover. */
    var blurb: Blurb?
        get() = blurbProperty.get()
        set(value) {
            blurbProperty.set(value)
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
        titleProperty.refresh()
        titleAppendixProperty.refresh()
        prologProperty.refresh()
        chaptersProperty.refresh()
        epilogProperty.refresh()
        blurbProperty.refresh()
    }

}

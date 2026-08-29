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
import javafx.beans.property.SimpleStringProperty
import javafx.beans.property.StringProperty
import javafx.collections.FXCollections
import org.pcsoft.app.aighost.fx.model.internal.BeanFields
import org.pcsoft.app.aighost.fx.model.project.ProjectPartProperty
import org.pcsoft.app.aighost.fx.model.project.common.AIPromptProperty
import org.pcsoft.app.aighost.model.project.book.Blurb
import org.pcsoft.app.aighost.model.project.book.Book
import org.pcsoft.app.aighost.model.project.book.Chapter
import org.pcsoft.app.aighost.model.project.book.Epilog
import org.pcsoft.app.aighost.model.project.book.Prolog
import org.pcsoft.app.aighost.model.project.common.AIPrompt

/**
 * Property wrapping the manuscript of a project and offering every field of it - and every field of
 * the objects nested in it - as a property of its own.
 *
 * Prolog, epilog and blurb exist only after the user created them, so the properties standing for
 * them carry no object until then and their field properties answer with neutral values. The chapters
 * are offered as a list of the plain objects, because the user arranges them as a whole.
 *
 * This is the one property model a user interface is handed, so what it needs is reachable from
 * outside: the title, the further title lines and the two prompts the manuscript is generated from.
 * The parts below the book stay inside this module and are reached through their own editors later.
 */
class BookProperty internal constructor() : ProjectPartProperty<Book>() {

    private val fields = BeanFields<Book> { fireValueChangedEvent() }

    /** Main title of the book, as a property of its own. */
    val titleProperty: StringProperty = SimpleStringProperty()

    /** Main title of the book. */
    var title: String?
        get() = titleProperty.get()
        set(value) {
            titleProperty.set(value)
        }

    /** Further title lines shown below the main title, as a property of their own. */
    val titleAppendixProperty: ListProperty<String> =
        SimpleListProperty(FXCollections.observableArrayList())

    /** Further title lines shown below the main title. */
    var titleAppendix: List<String>
        get() = titleAppendixProperty.get()
        set(value) {
            titleAppendixProperty.setAll(value)
        }

    /** Prompts the manuscript as a whole is generated from, as a property of their own. */
    internal val promptsProperty: AIPromptProperty = AIPromptProperty()

    /** Prompts the manuscript as a whole is generated from. */
    var prompts: AIPrompt?
        get() = promptsProperty.get()
        set(value) {
            promptsProperty.set(value)
        }

    // The two prompts are what a user interface reaches for, so they are offered here as well
    // instead of handing the prompt object itself outside of this module.

    /** Description of what the manuscript is about, as a property of its own. */
    val contentPromptProperty: StringProperty
        get() = promptsProperty.contentPromptProperty

    /** Description of what the manuscript is about. */
    var contentPrompt: String?
        get() = promptsProperty.contentPromptProperty.get()
        set(value) {
            promptsProperty.contentPromptProperty.set(value)
        }

    /** Description of the tone the manuscript is written in, as a property of its own. */
    val stylePromptProperty: StringProperty
        get() = promptsProperty.stylePromptProperty

    /** Description of the tone the manuscript is written in. */
    var stylePrompt: String?
        get() = promptsProperty.stylePromptProperty.get()
        set(value) {
            promptsProperty.stylePromptProperty.set(value)
        }

    /** Prolog printed before the first chapter, as a property of its own. */
    internal val prologProperty: PrologProperty = PrologProperty()

    /** Prolog printed before the first chapter. */
    var prolog: Prolog?
        get() = prologProperty.get()
        set(value) {
            prologProperty.set(value)
        }

    /** Chapters of the book in their user defined order, as a property of their own. */
    val chaptersProperty: ListProperty<Chapter> =
        SimpleListProperty(FXCollections.observableArrayList())

    /** Chapters of the book in their user defined order. */
    var chapters: List<Chapter>
        get() = chaptersProperty.get()
        set(value) {
            chaptersProperty.setAll(value)
        }

    /** Epilog printed after the last chapter, as a property of its own. */
    internal val epilogProperty: EpilogProperty = EpilogProperty()

    /** Epilog printed after the last chapter. */
    var epilog: Epilog?
        get() = epilogProperty.get()
        set(value) {
            epilogProperty.set(value)
        }

    /** Advertising text printed on the cover, as a property of its own. */
    internal val blurbProperty: BlurbProperty = BlurbProperty()

    /** Advertising text printed on the cover. */
    var blurb: Blurb?
        get() = blurbProperty.get()
        set(value) {
            blurbProperty.set(value)
        }

    init {
        fields.string(titleProperty, "title")
        fields.list(titleAppendixProperty, "titleAppendix")
        fields.model(promptsProperty, "prompts", promptsProperty::refresh)
        fields.model(prologProperty, "prolog", prologProperty::refresh)
        fields.list(chaptersProperty, "chapters")
        fields.model(epilogProperty, "epilog", epilogProperty::refresh)
        fields.model(blurbProperty, "blurb", blurbProperty::refresh)

        // The field properties belong to another object after every exchange, so they are tied to the
        // one this property carries now.
        addListener { _, _, newValue -> fields.rebind(newValue) }
        fields.rebind(get())
    }

    override fun refresh() = fields.refresh()

}

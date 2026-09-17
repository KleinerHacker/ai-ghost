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
import javafx.beans.property.ObjectProperty
import javafx.beans.property.SimpleListProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.collections.FXCollections
import org.pcsoft.app.aighost.fx.model.internal.BeanFields
import org.pcsoft.app.aighost.fx.model.project.ProjectPartProperty
import org.pcsoft.app.aighost.fx.model.project.common.AIPromptProperty
import org.pcsoft.app.aighost.model.project.book.Blurb
import org.pcsoft.app.aighost.model.project.book.Book
import org.pcsoft.app.aighost.model.project.book.Chapter
import org.pcsoft.app.aighost.model.project.book.Copyright
import org.pcsoft.app.aighost.model.project.book.Epilog
import org.pcsoft.app.aighost.model.project.book.Prolog
import org.pcsoft.app.aighost.model.project.common.AIPrompt
import org.pcsoft.framework.simplay.engine.model.Document

/**
 * Property wrapping the manuscript of a project and offering every field of it - and every field of
 * the objects nested in it - as a property of its own.
 *
 * Copyright page, prolog, epilog and blurb are always part of the manuscript, each of them carrying a
 * switch that tells whether it belongs to the book. The properties standing for them carry no object
 * only as long as no manuscript sits behind this property, and their field properties answer with
 * neutral values until then. The chapters are offered as a list of the plain objects, because the user
 * arranges them as a whole.
 *
 * Every part nested in the manuscript is handed out with its own type, so a user interface reaches the
 * fields of the prompts, of the copyright page, of the prolog, of the epilog and of the blurb through
 * the property standing for that part. The book itself is built by the project alone and therefore
 * carries an internal constructor.
 */
class BookProperty internal constructor() : ProjectPartProperty<Book>() {

    private val fields = BeanFields<Book> { fireValueChangedEvent() }

    /** Prompts the manuscript as a whole is generated from, as a property of their own. */
    val promptsProperty: AIPromptProperty = AIPromptProperty()

    /** Prompts the manuscript as a whole is generated from. */
    var prompts: AIPrompt?
        get() = promptsProperty.get()
        set(value) {
            promptsProperty.set(value)
        }

    /** Copyright page of the book, as a property of its own. */
    val copyrightProperty: CopyrightProperty = CopyrightProperty()

    /** Copyright page of the book. */
    var copyright: Copyright?
        get() = copyrightProperty.get()
        set(value) {
            copyrightProperty.set(value)
        }

    /** Prolog printed before the first chapter, as a property of its own. */
    val prologProperty: PrologProperty = PrologProperty()

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
    val epilogProperty: EpilogProperty = EpilogProperty()

    /** Epilog printed after the last chapter. */
    var epilog: Epilog?
        get() = epilogProperty.get()
        set(value) {
            epilogProperty.set(value)
        }

    /** Advertising text printed on the cover, as a property of its own. */
    val blurbProperty: BlurbProperty = BlurbProperty()

    /** Advertising text printed on the cover. */
    var blurb: Blurb?
        get() = blurbProperty.get()
        set(value) {
            blurbProperty.set(value)
        }

    /**
     * The manuscript's flowing text as simPlay's raw [Document], as a property of its own.
     *
     * A plain reference, not a nested model: [Document] is a value object from simPlay with no
     * property model of its own in `lib/fx-model`, so a change inside it is only ever picked up by
     * [refresh], the same as any other reference field.
     */
    val documentProperty: ObjectProperty<Document?> = SimpleObjectProperty()

    /** The manuscript's flowing text as simPlay's raw [Document]. */
    var document: Document?
        get() = documentProperty.get()
        set(value) {
            documentProperty.set(value)
        }

    init {
        fields.model(promptsProperty, "prompts", promptsProperty::refresh)
        fields.model(copyrightProperty, "copyright", copyrightProperty::refresh)
        fields.model(prologProperty, "prolog", prologProperty::refresh)
        fields.list(chaptersProperty, "chapters")
        fields.model(epilogProperty, "epilog", epilogProperty::refresh)
        fields.model(blurbProperty, "blurb", blurbProperty::refresh)
        fields.reference(documentProperty, "document")

        // The field properties belong to another object after every exchange, so they are tied to the
        // one this property carries now.
        addListener { fields.rebind(get()) }
        fields.rebind(get())
    }

    override fun refresh() = fields.refresh()

}

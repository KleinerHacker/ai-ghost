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

package org.pcsoft.app.aighost.model.project

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonPropertyOrder
import org.pcsoft.app.aighost.model.common.StyleData
import java.util.concurrent.CopyOnWriteArrayList

/**
 * A writing project of the user, the root object that is persisted as one JSON document.
 *
 * Beside the manuscript itself it carries the publishing data and the typographic settings the
 * manuscript is rendered with.
 *
 * The project is changed on the object itself, one property at a time. Every change notifies the
 * registered [Listener]s, so a part of the application follows the project instead of polling it. A
 * change is only reported when the value really differs from the one that was set before.
 *
 * Listeners are not part of the document and survive reading another project, because reading writes
 * into this object instead of replacing it.
 *
 * @param name Name of the project as shown to the user.
 * @param author Author printed in the manuscript.
 * @param copyright Copyright notice printed in the manuscript.
 * @param settings Typographic and page settings of the manuscript.
 * @param book The manuscript with its title and chapters.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonPropertyOrder("name", "author", "copyright", "settings", "book")
class Project(
    name: String = "New Project",
    author: String = "",
    copyright: String = "",

    settings: Settings = Settings(),
    book: Book = Book()
) {

    private val listeners = CopyOnWriteArrayList<Listener>()

    /** Name of the project as shown to the user. */
    var name: String = name
        set(value) {
            if (field == value) return

            field = value
            notifyChanged(Property.NAME)
        }

    /** Author printed in the manuscript. */
    var author: String = author
        set(value) {
            if (field == value) return

            field = value
            notifyChanged(Property.AUTHOR)
        }

    /** Copyright notice printed in the manuscript. */
    var copyright: String = copyright
        set(value) {
            if (field == value) return

            field = value
            notifyChanged(Property.COPYRIGHT)
        }

    /** Typographic and page settings of the manuscript. */
    var settings: Settings = settings
        set(value) {
            if (field == value) return

            field = value
            notifyChanged(Property.SETTINGS)
        }

    /** The manuscript with its title and chapters. */
    var book: Book = book
        set(value) {
            if (field == value) return

            field = value
            notifyChanged(Property.BOOK)
        }

    /**
     * Registers [listener] to be notified about every changed property.
     *
     * A listener is called on the thread that changed the property, after the new value is in place.
     * Registering the same listener twice makes it be called twice.
     */
    fun addListener(listener: Listener) {
        listeners.add(listener)
    }

    /** Removes [listener] again; a listener that is not registered is ignored. */
    fun removeListener(listener: Listener) {
        listeners.remove(listener)
    }

    private fun notifyChanged(property: Property) {
        listeners.forEach { it.onChanged(this, property) }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Project) return false

        return name == other.name &&
                author == other.author &&
                copyright == other.copyright &&
                settings == other.settings &&
                book == other.book
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + author.hashCode()
        result = 31 * result + copyright.hashCode()
        result = 31 * result + settings.hashCode()
        result = 31 * result + book.hashCode()

        return result
    }

    override fun toString(): String =
        "Project(name=$name, author=$author, copyright=$copyright, settings=$settings, book=$book)"

    /** Property of the [Project] a change is reported for. */
    enum class Property {

        /** [Project.name] was changed. */
        NAME,

        /** [Project.author] was changed. */
        AUTHOR,

        /** [Project.copyright] was changed. */
        COPYRIGHT,

        /** [Project.settings] were changed. */
        SETTINGS,

        /** [Project.book] was changed. */
        BOOK
    }

    /** Notified whenever a property of the project was changed. */
    fun interface Listener {

        /**
         * Called after [property] was changed to its new value.
         *
         * The new value is read from [project]; the value that was replaced is gone, because the
         * project is changed in place.
         *
         * @param project The project the change happened on.
         * @param property The property that was changed.
         */
        fun onChanged(project: Project, property: Property)
    }

    /**
     * Typographic and page settings a [Project] is rendered with.
     *
     * @property authorFont Style of the author name.
     * @property copyrightFont Style of the copyright notice.
     * @property titleFont Style of the book title.
     * @property titleAppendixFont Style of the further title lines.
     * @property chapterFont Style of a chapter heading.
     * @property chapterAppendixFont Style of the further chapter heading lines.
     * @property textFont Style of the chapter text.
     * @property copyrightPage Whether a separate copyright page is printed.
     * @property startWithEmptyPage Whether the manuscript starts with an empty page.
     * @property endWithEmptyPage Whether the manuscript ends with an empty page.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    data class Settings(
        val authorFont: StyleData = StyleData(),
        val copyrightFont: StyleData = StyleData(),
        val titleFont: StyleData = StyleData(),
        val titleAppendixFont: StyleData = StyleData(),
        val chapterFont: StyleData = StyleData(),
        val chapterAppendixFont: StyleData = StyleData(),
        val textFont: StyleData = StyleData(),

        val copyrightPage: Boolean = false,
        val startWithEmptyPage: Boolean = true,
        val endWithEmptyPage: Boolean = true
    )
}

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

import javafx.beans.property.ReadOnlyObjectProperty
import javafx.beans.property.ReadOnlyObjectWrapper
import javafx.beans.property.SimpleStringProperty
import javafx.beans.property.StringProperty
import org.pcsoft.app.aighost.model.project.book.Chapter
import java.util.UUID

/**
 * Property wrapping a single chapter of a book - the one the user is working on for instance - and
 * offering every field of it as a property of its own.
 *
 * The wrapped object may be absent as long as no chapter is picked, so every field property answers
 * with a neutral value and drops what is written to it until a chapter sits behind this property.
 *
 * This property model is handed out with its own type, so a caller reaches every field of the chapter
 * directly. Unlike the parts nested in [BookProperty] a chapter is only one entry of a plain list, so
 * this property is not built by the book itself; a caller that picked a chapter out of that list builds
 * one through [of] instead.
 */
class ChapterProperty internal constructor() : BookPartProperty<Chapter?>() {

    /** Name of the chapter as shown in the project tree, as a property of its own. */
    val nameProperty: StringProperty = SimpleStringProperty()

    // A property carries a name of its own, so the accessors of the wrapped field are given another
    // name on the JVM side - otherwise they would silently replace the one of the base class.
    /** Name of the chapter as shown in the project tree. */
    var name: String?
        @JvmName("getChapterName") get() = nameProperty.get()
        @JvmName("setChapterName") set(value) {
            nameProperty.set(value)
        }

    // The chapter's id is a `val` on the POJO and carries no setter, so it MUST NOT be registered with
    // BeanFields (see the fx-model skill) - it is taken over by hand instead, on every exchange of the
    // wrapped chapter. It never changes on its own once a chapter sits behind this property, so no
    // further reporting up to the root is needed for it.
    private val idWrapper: ReadOnlyObjectWrapper<UUID?> = ReadOnlyObjectWrapper(null)

    /** Stable id of the wrapped chapter, read only since [Chapter.id] itself is immutable. */
    val idProperty: ReadOnlyObjectProperty<UUID?> = idWrapper.readOnlyProperty

    /** Stable id of the wrapped chapter, `null` while no chapter is bound. */
    val id: UUID? get() = idProperty.get()

    init {
        fields.string(nameProperty, "name")
        addListener { _, _, newValue -> idWrapper.set(newValue?.id) }
    }

    companion object {
        /**
         * Builds a chapter property already bound to the given chapter.
         *
         * The chapter is a plain entry of [BookProperty.chaptersProperty] and therefore carries no
         * property model of its own; this factory is the way a caller that picked one out of that
         * list reaches its fields as properties.
         *
         * @param chapter the chapter to wrap
         * @return a chapter property wrapping the given chapter
         */
        @JvmStatic
        fun of(chapter: Chapter): ChapterProperty {
            val property = ChapterProperty()
            property.set(chapter)
            return property
        }
    }

}

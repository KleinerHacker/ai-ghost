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

import javafx.beans.property.ObjectProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.StringProperty
import org.pcsoft.app.aighost.model.project.Book
import org.pcsoft.app.aighost.model.project.Project

/**
 * Property holding the project the user is working on and offering every field of that object - and
 * every field of the objects nested in it - as a property of its own.
 *
 * A change travels through the whole object tree in both directions: a changed field is reported by
 * every property between that field and this one, and an exchanged object - a project file that was
 * loaded again for instance - is passed down to the properties of its fields, so a control bound to
 * any level shows the current value.
 *
 * The project object stays the same instance while one of its fields changes, so such a change reaches
 * a listener registered here as an invalidation. A `ChangeListener` registered directly on this
 * property compares the old value with the new one and therefore only sees the exchange of the whole
 * project object; a binding built on this property is re-evaluated in both cases.
 *
 * As long as no project sits behind this property every field property answers with a neutral value
 * and drops what is written to it, so a user interface may be built before a project is opened.
 */
class ProjectProperty(project: Project) : SimpleObjectProperty<Project>(project) {

    private val overrideName = org.pcsoft.app.aighost.fx.model.internal.OverrideStringProperty(
        { newValue -> value?.also { it.name = newValue ?: "" } },
        { value?.name },
        { fireValueChangedEvent() }
    )

    /** Name of the project as shown to the user, as a property of its own. */
    val nameProperty: StringProperty
        get() = overrideName

    // A property carries a name of its own, so the accessors of the wrapped field are given another
    // name on the JVM side - otherwise they would silently replace the one of the base class.
    /** Name of the project as shown to the user. */
    var name: String?
        @JvmName("getProjectName") get() = overrideName.get()
        @JvmName("setProjectName") set(value) {
            overrideName.set(value)
        }

    private val overrideAuthor = org.pcsoft.app.aighost.fx.model.internal.OverrideStringProperty(
        { newValue -> value?.also { it.author = newValue ?: "" } },
        { value?.author },
        { fireValueChangedEvent() }
    )

    /** Author printed in the manuscript, as a property of its own. */
    val authorProperty: StringProperty
        get() = overrideAuthor

    /** Author printed in the manuscript. */
    var author: String?
        get() = overrideAuthor.get()
        set(value) {
            overrideAuthor.set(value)
        }

    private val overrideCopyright = org.pcsoft.app.aighost.fx.model.internal.OverrideStringProperty(
        { newValue -> value?.also { it.copyright = newValue ?: "" } },
        { value?.copyright },
        { fireValueChangedEvent() }
    )

    /** Copyright notice printed in the manuscript, as a property of its own. */
    val copyrightProperty: StringProperty
        get() = overrideCopyright

    /** Copyright notice printed in the manuscript. */
    var copyright: String?
        get() = overrideCopyright.get()
        set(value) {
            overrideCopyright.set(value)
        }

    private val overrideSettings = SettingsProperty(
        setter = { newValue -> value?.also { it.settings = newValue ?: Project.Settings() } },
        getter = { value?.settings },
        fireEvent = { fireValueChangedEvent() }
    )

    /** Typographic and page settings of the manuscript, as a property of its own. */
    val settingsProperty: ObjectProperty<Project.Settings?>
        get() = overrideSettings

    /** Typographic and page settings of the manuscript. */
    var settings: Project.Settings?
        get() = overrideSettings.get()
        set(value) {
            overrideSettings.set(value)
        }

    private val overrideBook = BookProperty(
        setter = { newValue -> value?.also { it.book = newValue ?: Book() } },
        getter = { value?.book },
        fireEvent = { fireValueChangedEvent() }
    )

    /** The manuscript with its title and chapters, as a property of its own. */
    val bookProperty: ObjectProperty<Book?>
        get() = overrideBook

    /** The manuscript with its title and chapters. */
    var book: Book?
        get() = overrideBook.get()
        set(value) {
            overrideBook.set(value)
        }

    init {
        // The constructor of the base class stores the object without announcing it, so the field
        // properties have to take over its values here - otherwise they would only align on the
        // first exchange and report the initial values as a change of their own.
        invalidated()
    }

    /**
     * Called whenever the project object itself is exchanged - a freshly loaded project file for
     * instance - so the properties of its fields belong to another object afterwards and have to take
     * over its values.
     */
    override fun invalidated() {
        overrideName.refresh()
        overrideAuthor.refresh()
        overrideCopyright.refresh()
        overrideSettings.refresh()
        overrideBook.refresh()
    }

}

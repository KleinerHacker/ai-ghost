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
import org.pcsoft.app.aighost.fx.model.project.book.BookProperty
import org.pcsoft.app.aighost.fx.model.project.design.DesignProperty
import org.pcsoft.app.aighost.fx.model.project.meta.MetaProperty
import org.pcsoft.app.aighost.model.project.Project
import org.pcsoft.app.aighost.model.project.book.Book
import org.pcsoft.app.aighost.model.project.design.Design
import org.pcsoft.app.aighost.model.project.meta.Meta
import org.pcsoft.app.aighost.plugin.api.model.project.ProjectPart
import kotlin.reflect.KClass

/**
 * Property holding the project the user is working on and offering every part of that object - and
 * every field of the objects nested in those parts - as a property of its own.
 *
 * The three parts the application ships with are reached through [metaProperty], [designProperty] and
 * [bookProperty]. A part beyond them - one a plugin brings along - is reached through the property
 * [attachPart] hands out, so such a part is watched and written exactly like a standard one. Writing
 * a part replaces it in the project and leaves every other part where it is.
 *
 * A change travels through the whole object tree in both directions: a changed field is reported by
 * every property between that field and this one, and an exchanged object - a project file that was
 * loaded again for instance - is passed down to the properties of its parts, so a control bound to
 * any level shows the current value.
 *
 * The project object stays the same instance while one of its parts changes, so such a change reaches
 * a listener registered here as an invalidation. A `ChangeListener` registered directly on this
 * property compares the old value with the new one and therefore only sees the exchange of the whole
 * project object; a binding built on this property is re-evaluated in both cases.
 *
 * As long as no project sits behind this property every field property answers with a neutral value
 * and drops what is written to it, so a user interface may be built before a project is opened.
 */
class ProjectProperty(project: Project) : SimpleObjectProperty<Project>(project) {

    // The parts a plugin attached, in the order they were attached. Declared before everything that
    // may reach it, because the properties of this class are initialized from top to bottom.
    private val attachedParts = LinkedHashMap<String, ProjectPartProperty<*>>()

    private val overrideMeta = MetaProperty(
        setter = { newValue -> value?.also { it.meta = newValue ?: Meta() } },
        getter = { value?.meta },
        fireEvent = { fireValueChangedEvent() }
    )

    /** Meta data of the project - its name, its author and its copyright notice. */
    val metaProperty: ObjectProperty<Meta?>
        get() = overrideMeta

    /** Meta data of the project. */
    var meta: Meta?
        get() = overrideMeta.get()
        set(value) {
            overrideMeta.set(value)
        }

    // The meta data is what a user interface reaches for most, so the three texts it carries are
    // offered here as well instead of forcing every caller through the part property.

    /** Name of the project as shown to the user, as a property of its own. */
    val nameProperty: StringProperty
        get() = overrideMeta.nameProperty

    // A property carries a name of its own, so the accessors of the wrapped field are given another
    // name on the JVM side - otherwise they would silently replace the one of the base class.
    /** Name of the project as shown to the user. */
    var name: String?
        @JvmName("getProjectName") get() = overrideMeta.nameProperty.get()
        @JvmName("setProjectName") set(value) {
            overrideMeta.nameProperty.set(value)
        }

    /** Author printed in the manuscript, as a property of its own. */
    val authorProperty: StringProperty
        get() = overrideMeta.authorProperty

    /** Author printed in the manuscript. */
    var author: String?
        get() = overrideMeta.authorProperty.get()
        set(value) {
            overrideMeta.authorProperty.set(value)
        }

    /** Copyright notice printed in the manuscript, as a property of its own. */
    val copyrightProperty: StringProperty
        get() = overrideMeta.copyrightProperty

    /** Copyright notice printed in the manuscript. */
    var copyright: String?
        get() = overrideMeta.copyrightProperty.get()
        set(value) {
            overrideMeta.copyrightProperty.set(value)
        }

    private val overrideDesign = DesignProperty(
        setter = { newValue -> value?.also { it.design = newValue ?: Design() } },
        getter = { value?.design },
        fireEvent = { fireValueChangedEvent() }
    )

    /** Typographic and page settings of the manuscript, as a property of its own. */
    val designProperty: ObjectProperty<Design?>
        get() = overrideDesign

    /** Typographic and page settings of the manuscript. */
    var design: Design?
        get() = overrideDesign.get()
        set(value) {
            overrideDesign.set(value)
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
        // The constructor of the base class stores the object without announcing it, so the part
        // properties have to take over its values here - otherwise they would only align on the
        // first exchange and report the initial values as a change of their own.
        invalidated()
    }

    /**
     * Attaches the property model of a part beyond the three standard ones and hands it out.
     *
     * [factory] builds that model from the three accessors it is given: the setter puts the part into
     * the project - or takes it out again when it is set to `null` - the getter reads it back, and the
     * event lets a change of the part be reported by this property as its own. From then on the part
     * is treated like a standard one: an exchanged project reaches it, and a change of it reaches
     * everyone listening to the project.
     *
     * @param identifier The identifier the part is stored under, the one it declares through `ProjectPartInfo`.
     * @param partClass The type of the part, so an entry of another type is read as absent.
     * @param factory Builds the property model of the part from setter, getter and change event.
     * @return The property model that was built.
     * @throws IllegalArgumentException When [identifier] names one of the three standard parts.
     * @throws IllegalStateException When a part is already attached under that identifier.
     */
    fun <P : ProjectPart, M : ProjectPartProperty<P>> attachPart(
        identifier: String,
        partClass: KClass<P>,
        factory: (setter: (P?) -> Unit, getter: () -> P?, fireEvent: () -> Unit) -> M
    ): M {
        require(identifier !in Project.STANDARD_IDENTIFIERS) {
            "The standard project part '$identifier' is reached through its own property."
        }
        check(identifier !in attachedParts) {
            "A property is already attached for the project part '$identifier'."
        }

        val property = factory(
            { newValue ->
                value?.also { project ->
                    if (newValue == null) project.removePart(identifier)
                    else project.putPart(identifier, newValue)
                }
            },
            {
                value?.part(identifier)
                    ?.takeIf { partClass.java.isInstance(it) }
                    ?.let { partClass.java.cast(it) }
            },
            { fireValueChangedEvent() }
        )

        attachedParts[identifier] = property
        // The property was built empty, so it takes over what the open project carries right now
        // without announcing that as a change of the project.
        property.refresh()

        return property
    }

    /**
     * Attaches a plain property for a part beyond the three standard ones, without a property per
     * field of it.
     *
     * @param identifier The identifier the part is stored under.
     * @param partClass The type of the part.
     * @return The property the part is reached through.
     */
    fun <P : ProjectPart> attachPart(identifier: String, partClass: KClass<P>): ObjectProperty<P?> =
        attachPart(identifier, partClass) { setter, getter, fireEvent ->
            ProjectPartProperty(setter, getter, fireEvent)
        }

    /**
     * Detaches the property attached under [identifier], for instance when a plugin is unloaded.
     *
     * The detached property is left as it is and stops following the project; the part itself stays
     * in the project and is written into the document just the same.
     *
     * @param identifier The identifier the part is stored under.
     * @return `true` when a property was attached under that identifier, `false` otherwise.
     */
    fun detachPart(identifier: String): Boolean = attachedParts.remove(identifier) != null

    /**
     * The property attached under [identifier], or `null` when none is.
     *
     * @param identifier The identifier the part is stored under.
     */
    fun attachedPart(identifier: String): ProjectPartProperty<*>? = attachedParts[identifier]

    /**
     * Called whenever the project object itself is exchanged - a freshly loaded project file for
     * instance - so the properties of its parts belong to another object afterwards and have to take
     * over its values.
     */
    override fun invalidated() {
        overrideMeta.refresh()
        overrideDesign.refresh()
        overrideBook.refresh()

        for (part in attachedParts.values) {
            part.refresh()
        }
    }

}

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

import javafx.beans.InvalidationListener
import javafx.beans.property.ObjectProperty
import javafx.beans.property.SimpleObjectProperty
import org.pcsoft.app.aighost.fx.model.internal.BeanFields
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

    private val fields = BeanFields<Project> { fireValueChangedEvent() }

    // The parts a plugin attached, in the order they were attached. Declared before everything that
    // may reach it, because the properties of this class are initialized from top to bottom.
    private val attachedParts = LinkedHashMap<String, Attachment>()

    // Guards the project object while an attached part takes over what it carries, so such an
    // alignment is not written back into it.
    private var attaching = false

    /** Meta data of the project - its name, its author and its copyright notice. */
    val metaProperty: MetaProperty = MetaProperty()

    /** Meta data of the project. */
    var meta: Meta?
        get() = metaProperty.get()
        set(value) {
            metaProperty.set(value)
        }

    /** Typographic and page settings of the manuscript, as a property of its own. */
    val designProperty: DesignProperty = DesignProperty()

    /** Typographic and page settings of the manuscript. */
    var design: Design?
        get() = designProperty.get()
        set(value) {
            designProperty.set(value)
        }

    /** The manuscript with its title and chapters, as a property of its own. */
    val bookProperty: BookProperty = BookProperty()

    /** The manuscript with its title and chapters. */
    var book: Book?
        get() = bookProperty.get()
        set(value) {
            bookProperty.set(value)
        }

    init {
        fields.model(metaProperty, "meta", metaProperty::refresh)
        fields.model(designProperty, "design", designProperty::refresh)
        fields.model(bookProperty, "book", bookProperty::refresh)

        // The properties of the parts belong to another object after every exchange, so they are tied
        // to the one this property carries now. The constructor of the base class stored the project
        // without announcing it, so they are tied to it right here as well.
        addListener { _, _, newValue ->
            fields.rebind(newValue)
            refreshAttachedParts()
        }
        fields.rebind(get())
    }

    /**
     * Reads every part of the project again - and every field of the objects nested in those parts -
     * and hands what changed to the properties standing for them.
     *
     * This is what a caller uses after writing on the project object past this model: a plain project
     * reports nothing, so nobody would notice such a write otherwise.
     */
    fun refresh() {
        // The three standard parts are registered as models, so reading the fields reaches into them
        // as well. An attached part is not a field of the project and is read separately.
        fields.refresh()
        refreshAttachedParts()
    }

    /**
     * Attaches the property model of a part beyond the three standard ones and hands it out.
     *
     * [factory] builds that model, which is empty when it is built: an attached part is not a field of
     * the project but an entry of its map of parts, so this property is the one keeping the two in
     * step. It hands the part of the open project to the model right away, writes back what is set on
     * it, and reports a change of it as a change of its own. From then on the part is treated like a
     * standard one: an exchanged project reaches it, and a change of it reaches everyone listening to
     * the project.
     *
     * @param identifier The identifier the part is stored under, the one it declares through `ProjectPartInfo`.
     * @param partClass The type of the part, so an entry of another type is read as absent.
     * @param factory Builds the property model of the part.
     * @return The property model that was built.
     * @throws IllegalArgumentException When [identifier] names one of the three standard parts.
     * @throws IllegalStateException When a part is already attached under that identifier.
     */
    fun <P : ProjectPart, M : ProjectPartProperty<P>> attachPart(
        identifier: String,
        partClass: KClass<P>,
        factory: () -> M
    ): M {
        require(identifier !in Project.STANDARD_IDENTIFIERS) {
            "The standard project part '$identifier' is reached through its own property."
        }
        check(identifier !in attachedParts) {
            "A property is already attached for the project part '$identifier'."
        }

        val property = factory()

        property.addListener(
            InvalidationListener {
                // A property that was detached in the meantime stops following the project, and an
                // alignment with a freshly opened project is not written back into it.
                if (attaching || attachedParts[identifier]?.property !== property) {
                    return@InvalidationListener
                }

                value?.also { project ->
                    val newValue = property.get()
                    if (newValue == null) project.removePart(identifier)
                    else project.putPart(identifier, newValue)
                }

                fireValueChangedEvent()
            }
        )

        attachedParts[identifier] = Attachment(property) { project ->
            property.set(partOf(project, identifier, partClass))
        }

        // The property was built empty, so it takes over what the open project carries right now
        // without announcing that as a change of the project.
        refreshAttachedPart(attachedParts.getValue(identifier))

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
        attachPart(identifier, partClass) { ProjectPartProperty<P>() }

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
    fun attachedPart(identifier: String): ProjectPartProperty<*>? = attachedParts[identifier]?.property

    /**
     * The part [identifier] names in [project], read as absent when the project carries none or one of
     * another type.
     */
    private fun <P : ProjectPart> partOf(project: Project?, identifier: String, partClass: KClass<P>): P? =
        project?.part(identifier)
            ?.takeIf { partClass.java.isInstance(it) }
            ?.let { partClass.java.cast(it) }

    /** Lets every attached property take over the part of the project this property carries now. */
    private fun refreshAttachedParts() {
        for (attachment in attachedParts.values) {
            refreshAttachedPart(attachment)
        }
    }

    /**
     * Lets [attachment] take over the part of the project this property carries now, without writing
     * that alignment back into the project.
     */
    private fun refreshAttachedPart(attachment: Attachment) {
        attaching = true
        try {
            attachment.refresh(get())
        } finally {
            attaching = false
        }
    }

    /**
     * An attached part: the property it is reached through and the way it takes over the part of a
     * project, which needs the type the part was attached with.
     */
    private class Attachment(
        val property: ProjectPartProperty<*>,
        val refresh: (Project?) -> Unit
    )

}

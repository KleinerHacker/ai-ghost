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

package org.pcsoft.app.aighost.fx.model.project.meta

import javafx.beans.property.SimpleStringProperty
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.pcsoft.app.aighost.fx.model.ChangeRecorder
import org.pcsoft.app.aighost.model.project.meta.Meta

/**
 * Developer tests for [MetaProperty].
 *
 * The property wraps the meta data of a project and offers every text it carries as a property of its
 * own. Every test looks at the object tree the way the user interface uses it: a binding hangs on the
 * part itself and on each of its field properties, and the tests assert that a change reaches every
 * binding that has to know about it - upwards to the parent the property reports to as well as
 * downwards into the fields of an exchanged object.
 */
class MetaPropertyTest {

    /** Stands for the project carrying the meta data, the object a parent property writes into. */
    private class Holder(var meta: Meta?)

    private lateinit var holder: Holder
    private lateinit var property: MetaProperty
    private lateinit var recorder: ChangeRecorder

    /** Counts what the parent property is told, so the report up to the root becomes visible. */
    private var parentEvents = 0

    @BeforeEach
    fun setUp() {
        holder = Holder(newMeta())
        parentEvents = 0
        property = MetaProperty()
        // A parent property reports a change of a nested one as its own and writes an exchanged object
        // back into the one carrying it, which is what these two listeners stand for.
        property.addListener { _ -> parentEvents++ }
        property.addListener { _, _, newValue -> holder.meta = newValue }
        // A parent property hands the nested object to this property as soon as that object arrives.
        property.set(holder.meta)

        recorder = ChangeRecorder()
        recorder.watch("meta", property)
        recorder.watch("meta.name", property.nameProperty)
        recorder.watch("meta.author", property.authorProperty)

        parentEvents = 0
    }

    /** The meta data every test starts from, built fresh so no test sees the object of another. */
    private fun newMeta(): Meta = Meta(
        name = "My Novel",
        author = "Jane Doe"
    )

    /**
     * Use case: a view shows the meta data of the open project, so every field property answers with
     * the value the wrapped object carries instead of a copy made at some earlier point.
     */
    @Test
    fun readsEveryFieldFromTheModelObject() {
        assertEquals("My Novel", property.nameProperty.get())
        assertEquals("Jane Doe", property.authorProperty.get())
    }

    /**
     * Use case: the user types a new project name into a text field, so the value reaches the model
     * object and every property between the field and the root reports the change.
     */
    @Test
    fun writingAFieldReachesTheModelObject() {
        property.nameProperty.set("Renamed")

        assertEquals("Renamed", holder.meta?.name)
        assertEquals(1, recorder.countOf("meta.name"))
        assertEquals(1, recorder.countOf("meta"))
        assertEquals(1, parentEvents)
    }

    /**
     * Use case: a text field of the user interface is bound to the author, so what the user types
     * arrives in the model object through the binding without any code writing it there.
     */
    @Test
    fun writingThroughABindingReachesTheModelObject() {
        val input = SimpleStringProperty("Bound Author")
        property.authorProperty.bind(input)

        assertEquals("Bound Author", holder.meta?.author)

        input.set("Another Author")

        assertEquals("Another Author", holder.meta?.author)
        assertEquals(1, parentEvents.coerceAtMost(1))

        property.authorProperty.unbind()
    }

    /**
     * Use case: something changes the model object directly - an import for instance - so the field
     * properties take over the new values as soon as the part is refreshed.
     */
    @Test
    fun aChangeOnTheModelObjectBecomesVisible() {
        holder.meta?.name = "Renamed Past The Property"
        holder.meta?.author = "John Doe"

        property.refresh()

        assertEquals("Renamed Past The Property", property.nameProperty.get())
        assertEquals("John Doe", property.authorProperty.get())
    }

    /**
     * Use case: another project is opened, so the whole meta part is exchanged and every field
     * property takes over the value of the new object and reports it up to the root.
     */
    @Test
    fun exchangingTheModelObjectUpdatesEveryField() {
        recorder.reset()
        parentEvents = 0

        property.set(Meta(name = "Other Novel", author = "John Doe"))

        assertEquals("Other Novel", property.nameProperty.get())
        assertEquals("John Doe", property.authorProperty.get())
        recorder.assertAllFired("exchanging the meta data")
        assertEquals(1, parentEvents)
    }

    /**
     * Use case: a project is opened again without having been changed, so an exchange against an
     * equal object leaves every field property quiet instead of redrawing the whole user interface.
     */
    @Test
    fun exchangingAgainstAnEqualObjectStaysQuiet() {
        recorder.reset()

        property.set(newMeta())

        recorder.assertNoneFired("exchanging the meta data against an equal one")
    }

    /**
     * Use case: the user interface is built before a project is opened, so every field property
     * answers with a neutral value and drops what is written to it instead of failing.
     */
    @Test
    fun answersNeutrallyWithoutAModelObject() {
        // A parent property hands out nothing while it carries no project, which is what setting the
        // property itself to nothing stands for.
        property.set(null)

        assertNull(property.nameProperty.get())
        assertNull(property.authorProperty.get())

        property.nameProperty.set("Ignored")

        assertNull(holder.meta)
    }
}

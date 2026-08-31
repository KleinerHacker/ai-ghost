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

import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.collections.FXCollections
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.pcsoft.app.aighost.fx.model.ChangeRecorder
import org.pcsoft.app.aighost.model.project.book.Copyright

/**
 * Developer tests for [CopyrightProperty].
 *
 * The property wraps the copyright page of a book and offers its notice, the further lines below it
 * and its switch as properties of their own. Every test looks at the object tree the way the user
 * interface uses it: a binding hangs on the copyright page itself and on each of its field properties,
 * and the tests assert that a change reaches every binding that has to know about it - upwards to the
 * parent the property reports to as well as downwards into the fields of an exchanged object.
 */
class CopyrightPropertyTest {

    /** Stands for the book carrying the copyright page, the object a parent property writes into. */
    private class Holder(var copyright: Copyright?)

    private lateinit var holder: Holder
    private lateinit var property: CopyrightProperty
    private lateinit var recorder: ChangeRecorder

    /** Counts what the parent property is told, so the report up to the root becomes visible. */
    private var parentEvents = 0

    @BeforeEach
    fun setUp() {
        holder = Holder(newCopyright())
        parentEvents = 0
        property = CopyrightProperty()
        // A parent property reports a change of a nested one as its own and writes an exchanged object
        // back into the one carrying it, which is what these two listeners stand for.
        property.addListener { _ -> parentEvents++ }
        property.addListener { _, _, newValue -> holder.copyright = newValue }
        // A parent property hands the nested object to this property as soon as that object arrives.
        property.set(holder.copyright)

        recorder = ChangeRecorder()
        recorder.watch("copyrightPage", property)
        recorder.watch("copyrightPage.copyright", property.copyrightProperty)
        recorder.watch("copyrightPage.copyrightAppendix", property.copyrightAppendixProperty)
        recorder.watch("copyrightPage.included", property.includedProperty)

        parentEvents = 0
    }

    /** The copyright page every test starts from, built fresh so no test sees the object of another. */
    private fun newCopyright(): Copyright = Copyright(
        copyright = "Copyright 2026 Jane Doe",
        copyrightAppendix = listOf("All rights reserved."),
        included = true
    )

    /**
     * Use case: the editor of the copyright page is opened, so every field property answers with the
     * value the wrapped object carries instead of a copy made at some earlier point.
     */
    @Test
    fun readsEveryFieldFromTheModelObject() {
        assertEquals("Copyright 2026 Jane Doe", property.copyrightProperty.get())
        assertEquals(listOf("All rights reserved."), property.copyrightAppendixProperty)
        assertTrue(property.includedProperty.get())
    }

    /**
     * Use case: the user types another copyright notice, so the value reaches the model object and
     * every property between that field and the root reports the change.
     */
    @Test
    fun writingTheNoticeReachesTheModelObject() {
        property.copyrightProperty.set("Copyright 2027 Jane Doe")

        assertEquals("Copyright 2027 Jane Doe", holder.copyright?.copyright)
        assertEquals(1, recorder.countOf("copyrightPage.copyright"))
        assertEquals(1, recorder.countOf("copyrightPage"))
        assertEquals(1, parentEvents)
    }

    /**
     * Use case: the user adds a further line below the notice, so the content change alone reaches the
     * model object and every property above it reports the change.
     */
    @Test
    fun writingAFurtherLineReachesTheModelObject() {
        property.copyrightAppendixProperty.add("Printed in Germany.")

        assertEquals(
            listOf("All rights reserved.", "Printed in Germany."),
            holder.copyright?.copyrightAppendix
        )
        assertEquals(1, recorder.countOf("copyrightPage.copyrightAppendix"))
        assertEquals(1, recorder.countOf("copyrightPage"))
        assertTrue(parentEvents > 0) { "the parent property was not told about the change" }
    }

    /**
     * Use case: the further lines are written through the property itself, so the whole text reaches
     * the model object and every property above it reports the change.
     */
    @Test
    fun writingAllFurtherLinesReachesTheModelObject() {
        property.copyrightAppendix = listOf("Printed in Germany.")

        assertEquals(listOf("Printed in Germany."), holder.copyright?.copyrightAppendix)
        assertEquals(listOf("Printed in Germany."), property.copyrightAppendix)
        assertTrue(parentEvents > 0) { "the parent property was not told about the change" }
    }

    /**
     * Use case: the user takes the copyright page out of the book, so the switch reaches the model
     * object and the page as a whole reports the change instead of the switch alone.
     */
    @Test
    fun writingTheSwitchReachesTheModelObject() {
        property.includedProperty.set(false)

        assertFalse(holder.copyright?.included ?: true)
        assertEquals(1, recorder.countOf("copyrightPage.included"))
        assertEquals(1, recorder.countOf("copyrightPage"))
        assertEquals(1, parentEvents)
    }

    /**
     * Use case: the notice is bound to the text field of the editor, so every text that field produces
     * reaches the model object without any code writing it there.
     */
    @Test
    fun writingThroughABindingReachesTheModelObject() {
        val input = SimpleStringProperty("Copyright 2026 John Doe")
        property.copyrightProperty.bind(input)

        assertEquals("Copyright 2026 John Doe", holder.copyright?.copyright)

        input.set("Copyright 2028 John Doe")

        assertEquals("Copyright 2028 John Doe", holder.copyright?.copyright)

        property.copyrightProperty.unbind()
    }

    /**
     * Use case: the further lines are filled from a binding - the editor hands over its content - so
     * every list that binding produces reaches the model object.
     */
    @Test
    fun writingTheFurtherLinesThroughABindingReachesTheModelObject() {
        val input = SimpleObjectProperty(FXCollections.observableArrayList("A first line."))
        property.copyrightAppendixProperty.bind(input)

        input.set(FXCollections.observableArrayList("Printed in Germany."))

        assertEquals(listOf("Printed in Germany."), holder.copyright?.copyrightAppendix)

        property.copyrightAppendixProperty.unbind()
    }

    /**
     * Use case: a check box of the editor is bound to the switch, so every state that box produces
     * reaches the model object.
     */
    @Test
    fun writingTheSwitchThroughABindingReachesTheModelObject() {
        val input = SimpleBooleanProperty(true)
        property.includedProperty.bind(input)

        assertTrue(holder.copyright?.included ?: false)

        input.set(false)

        assertFalse(holder.copyright?.included ?: true)

        property.includedProperty.unbind()
    }

    /**
     * Use case: a field of the copyright page is changed by application code past the property, so the
     * property is told to read the page again and every field property delivers the current value.
     */
    @Test
    fun aChangeOnTheModelObjectBecomesVisible() {
        holder.copyright?.copyright = "Copyright 2029 Jane Doe"
        holder.copyright?.copyrightAppendix = listOf("Printed in Germany.")
        holder.copyright?.included = false

        property.refresh()

        assertEquals("Copyright 2029 Jane Doe", property.copyright)
        assertEquals(listOf("Printed in Germany."), property.copyrightAppendix)
        assertFalse(property.included)
    }

    /**
     * Use case: another project is opened, so the whole copyright page is exchanged and every field
     * property takes over the value of the new object and reports it up to the root.
     */
    @Test
    fun exchangingTheModelObjectUpdatesEveryField() {
        recorder.reset()
        parentEvents = 0

        property.set(
            Copyright(
                copyright = "Copyright 2030 John Doe",
                copyrightAppendix = listOf("Printed in Germany."),
                included = false
            )
        )

        assertEquals("Copyright 2030 John Doe", property.copyrightProperty.get())
        assertEquals(listOf("Printed in Germany."), property.copyrightAppendix)
        assertFalse(property.includedProperty.get())
        recorder.assertAllFired("exchanging the copyright page")
        assertEquals(1, parentEvents)
    }

    /**
     * Use case: a project is opened again without having been changed, so an exchange against an equal
     * object leaves every field property quiet instead of redrawing the editor.
     */
    @Test
    fun exchangingAgainstAnEqualObjectStaysQuiet() {
        recorder.reset()

        property.set(newCopyright())

        recorder.assertNoneFired("exchanging the copyright page against an equal one")
    }

    /**
     * Use case: no book sits behind the property standing for the copyright page because no project is
     * open, so every field property answers with a neutral value and drops what is written to it.
     */
    @Test
    fun answersNeutrallyWithoutAModelObject() {
        // A parent property hands out nothing while it carries no book, which is what setting the
        // property itself to nothing stands for.
        property.set(null)

        assertNull(property.copyright)
        assertEquals(emptyList<String>(), property.copyrightAppendix)
        assertFalse(property.included)

        property.copyright = "Ignored"
        property.copyrightAppendix = listOf("Ignored as well.")
        property.included = true

        assertNull(holder.copyright)
    }
}

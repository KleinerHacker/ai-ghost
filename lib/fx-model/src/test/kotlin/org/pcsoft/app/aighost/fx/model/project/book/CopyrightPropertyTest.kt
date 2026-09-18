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
 * The property wraps the copyright page of a book and offers its switch as a property of its own.
 * Every test looks at the object tree the way the user interface uses it: a binding hangs on the
 * copyright page itself and on its field property, and the tests assert that a change reaches every
 * binding that has to know about it - upwards to the parent the property reports to as well as
 * downwards into the field of an exchanged object.
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
        recorder.watch("copyrightPage.included", property.includedProperty)

        parentEvents = 0
    }

    /** The copyright page every test starts from, built fresh so no test sees the object of another. */
    private fun newCopyright(): Copyright = Copyright(included = true)

    /**
     * Use case: the editor of the copyright page is opened, so the field property answers with the
     * value the wrapped object carries instead of a copy made at some earlier point.
     */
    @Test
    fun readsEveryFieldFromTheModelObject() {
        assertTrue(property.includedProperty.get())
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
     * property is told to read the page again and the field property delivers the current value.
     */
    @Test
    fun aChangeOnTheModelObjectBecomesVisible() {
        holder.copyright?.included = false

        property.refresh()

        assertFalse(property.included)
    }

    /**
     * Use case: another project is opened, so the whole copyright page is exchanged and the field
     * property takes over the value of the new object and reports it up to the root.
     */
    @Test
    fun exchangingTheModelObjectUpdatesEveryField() {
        recorder.reset()
        parentEvents = 0

        property.set(Copyright(included = false))

        assertFalse(property.includedProperty.get())
        recorder.assertAllFired("exchanging the copyright page")
        assertEquals(1, parentEvents)
    }

    /**
     * Use case: a project is opened again without having been changed, so an exchange against an equal
     * object leaves the field property quiet instead of redrawing the editor.
     */
    @Test
    fun exchangingAgainstAnEqualObjectStaysQuiet() {
        recorder.reset()

        property.set(newCopyright())

        recorder.assertNoneFired("exchanging the copyright page against an equal one")
    }

    /**
     * Use case: no book sits behind the property standing for the copyright page because no project is
     * open, so the field property answers with a neutral value and drops what is written to it.
     */
    @Test
    fun answersNeutrallyWithoutAModelObject() {
        // A parent property hands out nothing while it carries no book, which is what setting the
        // property itself to nothing stands for.
        property.set(null)

        assertFalse(property.included)

        property.included = true

        assertNull(holder.copyright)
    }
}

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

package org.pcsoft.app.aighost.fx.model.project.design

import javafx.beans.property.SimpleBooleanProperty
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.pcsoft.app.aighost.fx.model.ChangeRecorder
import org.pcsoft.app.aighost.model.common.Alignment
import org.pcsoft.app.aighost.model.common.FontData
import org.pcsoft.app.aighost.model.common.StyleData
import org.pcsoft.app.aighost.model.project.design.CopyrightDesign

/**
 * Developer tests for [CopyrightDesignProperty].
 *
 * The property wraps the design of the copyright page and offers both fields of it - the style and
 * the flag deciding whether the page is printed - as properties of their own. Every test looks at the
 * object tree the way the user interface uses it: a binding hangs on each level of the tree, and the
 * tests assert that a change reaches every binding that has to know about it.
 */
class CopyrightDesignPropertyTest {

    /** Stands for the design carrying this part, the object a parent property writes into. */
    private class Holder(var design: CopyrightDesign?)

    private lateinit var holder: Holder
    private lateinit var property: CopyrightDesignProperty
    private lateinit var recorder: ChangeRecorder

    /** Counts what the parent property is told, so the report up to the root becomes visible. */
    private var parentEvents = 0

    @BeforeEach
    fun setUp() {
        holder = Holder(newDesign())
        parentEvents = 0
        property = CopyrightDesignProperty(
            { holder.design = it },
            { holder.design },
            { parentEvents++ }
        )
        // A parent property aligns a nested one with the model object as soon as that object arrives,
        // so the same alignment happens here before the views are built.
        property.refresh()

        recorder = ChangeRecorder()
        recorder.watch("design", property)
        recorder.watch("design.style", property.styleProperty)
        recorder.watch("design.style.font.name", property.styleProperty.fontProperty.nameProperty)
        recorder.watch("design.show", property.showProperty)

        parentEvents = 0
    }

    /** The design every test starts from, built fresh so no test sees the object of another. */
    private fun newDesign(): CopyrightDesign = CopyrightDesign(
        style = StyleData(FontData("Copyright Serif", 8, bold = false, italic = false), Alignment.LEFT),
        show = true
    )

    /**
     * Use case: a view shows how the copyright page is printed, so both the style and the flag answer
     * with the values the wrapped object carries instead of a copy made earlier.
     */
    @Test
    fun readsEveryFieldFromTheModelObject() {
        assertEquals("Copyright Serif", property.styleProperty.fontProperty.nameProperty.get())
        assertEquals(true, property.showProperty.get())
    }

    /**
     * Use case: the user turns the copyright page off in the design dialog, so the flag reaches the
     * model object and every property between it and the root reports the change.
     */
    @Test
    fun writingTheFlagReachesTheModelObject() {
        property.showProperty.set(false)

        assertFalse(holder.design?.show ?: true)
        assertEquals(1, recorder.countOf("design.show"))
        assertEquals(1, recorder.countOf("design"))
        assertEquals(1, parentEvents)
    }

    /**
     * Use case: a check box of the design dialog is bound to the flag, so what the user ticks arrives
     * in the model object through the binding without any code writing it there.
     */
    @Test
    fun writingThroughABindingReachesTheModelObject() {
        val input = SimpleBooleanProperty(false)
        property.showProperty.bind(input)

        assertFalse(holder.design?.show ?: true)

        input.set(true)

        assertEquals(true, holder.design?.show)

        property.showProperty.unbind()
    }

    /**
     * Use case: something changes the model object directly - an imported design for instance - so
     * the field properties take over the new values as soon as the part is refreshed.
     */
    @Test
    fun aChangeOnTheModelObjectBecomesVisible() {
        holder.design?.show = false
        holder.design?.style = StyleData(FontData("Imported Serif", 9), Alignment.RIGHT)

        property.refresh()

        assertEquals(false, property.showProperty.get())
        assertEquals("Imported Serif", property.styleProperty.fontProperty.nameProperty.get())
    }

    /**
     * Use case: another project is opened, so the whole design part is exchanged and every property
     * below it takes over the value of the new object and reports it up to the root.
     */
    @Test
    fun exchangingTheModelObjectUpdatesEveryField() {
        recorder.reset()
        parentEvents = 0

        property.set(
            CopyrightDesign(
                style = StyleData(FontData("Other Serif", 44), Alignment.BLOCK),
                show = false
            )
        )

        assertEquals("Other Serif", property.styleProperty.fontProperty.nameProperty.get())
        assertEquals(false, property.showProperty.get())
        recorder.assertAllFired("exchanging the copyright design")
        assertEquals(1, parentEvents)
    }

    /**
     * Use case: a project is opened again without having been changed, so an exchange against an
     * equal object leaves every field property quiet instead of redrawing the whole dialog.
     */
    @Test
    fun exchangingAgainstAnEqualObjectStaysQuiet() {
        recorder.reset()

        property.set(newDesign())

        assertEquals(0, recorder.countOf("design.style.font.name"))
        assertEquals(0, recorder.countOf("design.show"))
    }

    /**
     * Use case: the user interface is built before a project is opened, so every field property
     * answers with a neutral value and drops what is written to it instead of failing.
     */
    @Test
    fun answersNeutrallyWithoutAModelObject() {
        holder.design = null
        property.refresh()

        assertNull(property.styleProperty.get())
        assertFalse(property.showProperty.get())

        property.showProperty.set(true)

        assertNull(holder.design)
    }
}

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

import javafx.beans.property.SimpleObjectProperty
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.pcsoft.app.aighost.fx.model.ChangeRecorder
import org.pcsoft.app.aighost.model.common.Alignment
import org.pcsoft.app.aighost.model.common.FontData
import org.pcsoft.app.aighost.model.common.StyleData
import org.pcsoft.app.aighost.model.project.design.AuthorDesign

/**
 * Developer tests for [AuthorDesignProperty].
 *
 * The property wraps the design of the author name of a project and offers the style it carries - and every field of
 * that style - as a property of its own. Every test looks at the object tree the way the user
 * interface uses it: a binding hangs on each level of the tree, and the tests assert that a change
 * reaches every binding that has to know about it - upwards to the parent the property reports to as
 * well as downwards into the fields of an exchanged object.
 */
class AuthorDesignPropertyTest {

    /** Stands for the design carrying this part, the object a parent property writes into. */
    private class Holder(var design: AuthorDesign?)

    private lateinit var holder: Holder
    private lateinit var property: AuthorDesignProperty
    private lateinit var recorder: ChangeRecorder

    /** Counts what the parent property is told, so the report up to the root becomes visible. */
    private var parentEvents = 0

    @BeforeEach
    fun setUp() {
        holder = Holder(newDesign())
        parentEvents = 0
        property = AuthorDesignProperty()
        // A parent property reports a change of a nested one as its own and writes an exchanged object
        // back into the one carrying it, which is what these two listeners stand for.
        property.addListener { _ -> parentEvents++ }
        property.addListener { _, _, newValue -> holder.design = newValue }
        // A parent property hands the nested object to this property as soon as that object arrives.
        property.set(holder.design)

        recorder = ChangeRecorder()
        recorder.watch("design", property)
        recorder.watch("design.style", property.styleProperty)
        recorder.watch("design.style.font", property.styleProperty.fontProperty)
        recorder.watch("design.style.font.name", property.styleProperty.fontProperty.nameProperty)
        recorder.watch("design.style.alignment", property.styleProperty.alignmentProperty)

        parentEvents = 0
    }

    /** The design every test starts from, built fresh so no test sees the object of another. */
    private fun newDesign(): AuthorDesign = AuthorDesign(
        style = StyleData(FontData("Author Serif", 16, bold = false, italic = false), Alignment.CENTER)
    )

    /**
     * Use case: a view shows how the author name is drawn, so the style property and every field of it
     * answer with the values the wrapped object carries instead of a copy made earlier.
     */
    @Test
    fun readsTheStyleFromTheModelObject() {
        assertEquals("Author Serif", property.styleProperty.fontProperty.nameProperty.get())
        assertEquals(16, property.styleProperty.fontProperty.sizeProperty.get())
        assertEquals(Alignment.CENTER, property.styleProperty.alignmentProperty.get())
    }

    /**
     * Use case: the user picks another font family in the design dialog, so the value reaches the
     * model object and every property between that field and the root reports the change.
     */
    @Test
    fun writingAFieldOfTheStyleReachesTheModelObject() {
        property.styleProperty.fontProperty.nameProperty.set("Chosen Serif")

        assertEquals("Chosen Serif", holder.design?.style?.font?.name)
        assertEquals(1, recorder.countOf("design.style.font.name"))
        assertEquals(1, recorder.countOf("design.style.font"))
        assertEquals(1, recorder.countOf("design.style"))
        assertEquals(1, recorder.countOf("design"))
        assertEquals(1, parentEvents)
    }

    /**
     * Use case: a control of the design dialog is bound to the style, so what the user picks arrives
     * in the model object through the binding without any code writing it there.
     */
    @Test
    fun writingThroughABindingReachesTheModelObject() {
        val input = SimpleObjectProperty(StyleData(FontData("Bound Serif", 21), Alignment.RIGHT))
        property.styleProperty.bind(input)

        assertEquals("Bound Serif", holder.design?.style?.font?.name)
        assertEquals(Alignment.RIGHT, holder.design?.style?.alignment)

        property.styleProperty.unbind()
    }

    /**
     * Use case: something changes the model object directly - an imported design for instance - so
     * the field properties take over the new values as soon as the part is refreshed.
     */
    @Test
    fun aChangeOnTheModelObjectBecomesVisible() {
        holder.design?.style = StyleData(FontData("Imported Serif", 33), Alignment.BLOCK)

        property.refresh()

        assertEquals("Imported Serif", property.styleProperty.fontProperty.nameProperty.get())
        assertEquals(Alignment.BLOCK, property.styleProperty.alignmentProperty.get())
    }

    /**
     * Use case: another project is opened, so the whole design part is exchanged and every property
     * below it takes over the value of the new object and reports it up to the root.
     */
    @Test
    fun exchangingTheModelObjectUpdatesEveryField() {
        recorder.reset()
        parentEvents = 0

        property.set(AuthorDesign(style = StyleData(FontData("Other Serif", 44), Alignment.BLOCK)))

        assertEquals("Other Serif", property.styleProperty.fontProperty.nameProperty.get())
        assertEquals(Alignment.BLOCK, property.styleProperty.alignmentProperty.get())
        recorder.assertAllFired("exchanging the design part")
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
        assertEquals(0, recorder.countOf("design.style.alignment"))
    }

    /**
     * Use case: the user interface is built before a project is opened, so every field property
     * answers with a neutral value and drops what is written to it instead of failing.
     */
    @Test
    fun answersNeutrallyWithoutAModelObject() {
        // A parent property hands out nothing while it carries no design, which is what setting the
        // property itself to nothing stands for.
        property.set(null)

        assertNull(property.styleProperty.get())

        property.styleProperty.fontProperty.nameProperty.set("Ignored")

        assertNull(holder.design)
    }
}

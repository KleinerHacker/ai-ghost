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

import javafx.beans.property.SimpleIntegerProperty
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.pcsoft.app.aighost.fx.model.ChangeRecorder
import org.pcsoft.app.aighost.model.project.design.PageNumberCountingMode
import org.pcsoft.app.aighost.model.project.design.PageNumberDesign
import org.pcsoft.app.aighost.model.project.design.PageNumberPosition

/**
 * Developer tests for [PageNumberDesignProperty].
 *
 * The property wraps the page numbering settings of a project and offers every field of it as a
 * property of its own. The tests watch the whole object at once - every single field and the page
 * numbering itself - so a value that fails to travel between the property and the wrapped object is
 * named by the assertion.
 */
class PageNumberDesignPropertyTest {

    /** Stands for the design carrying the page numbering, the object a parent property writes into. */
    private class Holder(var pageNumbering: PageNumberDesign?)

    private lateinit var holder: Holder
    private lateinit var property: PageNumberDesignProperty
    private lateinit var recorder: ChangeRecorder

    /** Counts what the parent property is told, so the report up to the root becomes visible. */
    private var parentEvents = 0

    @BeforeEach
    fun setUp() {
        holder = Holder(newPageNumberDesign())
        parentEvents = 0
        property = PageNumberDesignProperty()
        // A parent property reports a change of a nested one as its own and writes an exchanged object
        // back into the one carrying it, which is what these two listeners stand for.
        property.addListener { _ -> parentEvents++ }
        property.addListener { _, _, newValue -> holder.pageNumbering = newValue }
        // A parent property hands the nested object to this property as soon as that object arrives.
        property.set(holder.pageNumbering)

        recorder = ChangeRecorder()
        recorder.watch("pageNumbering", property)
        recorder.watch("pageNumbering.position", property.positionProperty)
        recorder.watch("pageNumbering.startNumber", property.startNumberProperty)
        recorder.watch("pageNumbering.countingMode", property.countingModeProperty)

        parentEvents = 0
    }

    /** The page numbering every test starts from, built fresh so no test sees the object of another. */
    private fun newPageNumberDesign(): PageNumberDesign = PageNumberDesign(
        position = PageNumberPosition.BOTTOM_CENTER,
        startNumber = 1,
        countingMode = PageNumberCountingMode.CONTINUOUS
    )

    /**
     * Use case: the project settings dialog is opened, so every field answers with the value the
     * wrapped page numbering carries instead of a copy made at some earlier point.
     */
    @Test
    fun readsEveryFieldFromTheModelObject() {
        assertEquals(PageNumberPosition.BOTTOM_CENTER, property.positionProperty.get())
        assertEquals(1, property.startNumberProperty.get())
        assertEquals(PageNumberCountingMode.CONTINUOUS, property.countingModeProperty.get())
    }

    /**
     * Use case: the user picks another spot for the page number, so the position reaches the page
     * numbering and the numbering as a whole reports the change up to the design carrying it.
     */
    @Test
    fun writingThePositionReachesTheModelObject() {
        property.positionProperty.set(PageNumberPosition.TOP_INNER)

        assertEquals(PageNumberPosition.TOP_INNER, holder.pageNumbering?.position)
        assertEquals(1, recorder.countOf("pageNumbering.position"))
        assertEquals(1, recorder.countOf("pageNumbering"))
        assertEquals(1, parentEvents)
    }

    /**
     * Use case: the user sets the first counted sheet to a value other than one, so the start number
     * reaches the page numbering and the numbering as a whole reports the change.
     */
    @Test
    fun writingTheStartNumberReachesTheModelObject() {
        property.startNumberProperty.set(5)

        assertEquals(5, holder.pageNumbering?.startNumber)
        assertEquals(1, recorder.countOf("pageNumbering.startNumber"))
        assertEquals(1, recorder.countOf("pageNumbering"))
        assertEquals(1, parentEvents)
    }

    /**
     * Use case: the user lets an unnumbered sheet skip the counter instead of merely hiding its own
     * label, so the counting mode reaches the page numbering and reports the change up to the root.
     */
    @Test
    fun writingTheCountingModeReachesTheModelObject() {
        property.countingModeProperty.set(PageNumberCountingMode.SKIP_EXCLUDED)

        assertEquals(PageNumberCountingMode.SKIP_EXCLUDED, holder.pageNumbering?.countingMode)
        assertEquals(1, recorder.countOf("pageNumbering.countingMode"))
        assertEquals(1, recorder.countOf("pageNumbering"))
        assertEquals(1, parentEvents)
    }

    /**
     * Use case: a spinner of the settings dialog is bound to the start number, so what the user dials
     * in arrives in the page numbering through the binding without any code writing it there.
     */
    @Test
    fun writingThroughABindingReachesTheModelObject() {
        val input = SimpleIntegerProperty(4)
        property.startNumberProperty.bind(input)

        assertEquals(4, holder.pageNumbering?.startNumber)

        input.set(7)

        assertEquals(7, holder.pageNumbering?.startNumber)

        property.startNumberProperty.unbind()
    }

    /**
     * Use case: something changes the page numbering directly - an imported design for instance - so
     * every field takes over the new value as soon as the numbering is refreshed.
     */
    @Test
    fun aChangeOnTheModelObjectBecomesVisible() {
        holder.pageNumbering?.position = PageNumberPosition.BOTTOM_LEFT
        holder.pageNumbering?.startNumber = 9
        holder.pageNumbering?.countingMode = PageNumberCountingMode.SKIP_EXCLUDED

        property.refresh()

        assertEquals(PageNumberPosition.BOTTOM_LEFT, property.positionProperty.get())
        assertEquals(9, property.startNumberProperty.get())
        assertEquals(PageNumberCountingMode.SKIP_EXCLUDED, property.countingModeProperty.get())
    }

    /**
     * Use case: another project is opened, so the whole page numbering is exchanged and every field
     * takes over the value of the new object and reports it up to the root.
     */
    @Test
    fun exchangingTheModelObjectUpdatesEveryField() {
        recorder.reset()
        parentEvents = 0

        property.set(
            PageNumberDesign(
                position = PageNumberPosition.TOP_OUTER,
                startNumber = 2,
                countingMode = PageNumberCountingMode.SKIP_EXCLUDED
            )
        )

        assertEquals(PageNumberPosition.TOP_OUTER, property.positionProperty.get())
        assertEquals(2, property.startNumberProperty.get())
        assertEquals(PageNumberCountingMode.SKIP_EXCLUDED, property.countingModeProperty.get())
        recorder.assertAllFired("exchanging the page numbering")
        assertEquals(1, parentEvents)
    }

    /**
     * Use case: a project is opened again without having been changed, so an exchange against an equal
     * page numbering leaves every field quiet instead of redrawing the whole dialog.
     */
    @Test
    fun exchangingAgainstAnEqualObjectStaysQuiet() {
        recorder.reset()

        property.set(newPageNumberDesign())

        recorder.assertNoneFired("exchanging the page numbering against an equal one")
    }

    /**
     * Use case: the settings dialog is built before a project is opened, so every field answers with a
     * neutral value and drops what is written to it instead of failing.
     */
    @Test
    fun answersNeutrallyWithoutAModelObject() {
        property.set(null)

        assertNull(property.positionProperty.get())
        assertEquals(0, property.startNumberProperty.get())
        assertNull(property.countingModeProperty.get())

        property.startNumberProperty.set(5)

        assertNull(holder.pageNumbering)
    }
}

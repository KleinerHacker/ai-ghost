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

import javafx.beans.property.SimpleDoubleProperty
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.pcsoft.app.aighost.fx.model.ChangeRecorder
import org.pcsoft.app.aighost.model.project.design.PageFormat

/**
 * Developer tests for [PageFormatProperty].
 *
 * The property wraps the geometry of a page and offers every measure of it as a property of its own.
 * The tests watch the whole object at once - every single measure and the page format itself - so a
 * value that fails to travel between the property and the wrapped object is named by the assertion.
 */
class PageFormatPropertyTest {

    /** Stands for the design carrying the page format, the object a parent property writes into. */
    private class Holder(var pageFormat: PageFormat?)

    private lateinit var holder: Holder
    private lateinit var property: PageFormatProperty
    private lateinit var recorder: ChangeRecorder

    /** Counts what the parent property is told, so the report up to the root becomes visible. */
    private var parentEvents = 0

    @BeforeEach
    fun setUp() {
        holder = Holder(newPageFormat())
        parentEvents = 0
        property = PageFormatProperty()
        // A parent property reports a change of a nested one as its own and writes an exchanged object
        // back into the one carrying it, which is what these two listeners stand for.
        property.addListener { _ -> parentEvents++ }
        property.addListener { _, _, newValue -> holder.pageFormat = newValue }
        // A parent property hands the nested object to this property as soon as that object arrives.
        property.set(holder.pageFormat)

        recorder = ChangeRecorder()
        recorder.watch("pageFormat", property)
        recorder.watch("pageFormat.width", property.widthProperty)
        recorder.watch("pageFormat.height", property.heightProperty)
        recorder.watch("pageFormat.innerMargin", property.innerMarginProperty)
        recorder.watch("pageFormat.outerMargin", property.outerMarginProperty)
        recorder.watch("pageFormat.topMargin", property.topMarginProperty)
        recorder.watch("pageFormat.bottomMargin", property.bottomMarginProperty)
        recorder.watch("pageFormat.mirroredMargins", property.mirroredMarginsProperty)

        parentEvents = 0
    }

    /** The page format every test starts from, built fresh so no test sees the object of another. */
    private fun newPageFormat(): PageFormat = PageFormat(
        width = 400.0,
        height = 600.0,
        innerMargin = 25.0,
        outerMargin = 18.0,
        topMargin = 12.0,
        bottomMargin = 22.0,
        mirroredMargins = false
    )

    /**
     * Use case: the project settings dialog is opened, so every measure answers with the value the
     * wrapped page format carries instead of a copy made at some earlier point.
     */
    @Test
    fun readsEveryMeasureFromTheModelObject() {
        assertEquals(400.0, property.widthProperty.get())
        assertEquals(600.0, property.heightProperty.get())
        assertEquals(25.0, property.innerMarginProperty.get())
        assertEquals(18.0, property.outerMarginProperty.get())
        assertEquals(12.0, property.topMarginProperty.get())
        assertEquals(22.0, property.bottomMarginProperty.get())
        assertEquals(false, property.mirroredMarginsProperty.get())
    }

    /**
     * Use case: the user turns on mirrored margins for a printed layout, so the switch reaches the
     * page format and the format as a whole reports the change up to the design carrying it.
     */
    @Test
    fun writingMirroredMarginsReachesTheModelObject() {
        property.mirroredMarginsProperty.set(true)

        assertEquals(true, holder.pageFormat?.mirroredMargins)
        assertEquals(1, recorder.countOf("pageFormat.mirroredMargins"))
        assertEquals(1, recorder.countOf("pageFormat"))
        assertEquals(1, parentEvents)
    }

    /**
     * Use case: the user widens the paper, so the new width reaches the page format and the format as
     * a whole reports the change up to the design carrying it.
     */
    @Test
    fun writingAMeasureReachesTheModelObject() {
        property.widthProperty.set(430.0)

        assertEquals(430.0, holder.pageFormat?.width)
        assertEquals(1, recorder.countOf("pageFormat.width"))
        assertEquals(1, recorder.countOf("pageFormat"))
        assertEquals(1, parentEvents)
    }

    /**
     * Use case: the user sets every margin of the page, so each value lands in its own field of the
     * page format instead of overwriting one of the others.
     */
    @Test
    fun writingEveryMarginReachesItsOwnField() {
        property.innerMargin = 30.0
        property.outerMargin = 10.0
        property.topMargin = 11.0
        property.bottomMargin = 12.0
        property.height = 650.0

        assertEquals(30.0, holder.pageFormat?.innerMargin)
        assertEquals(10.0, holder.pageFormat?.outerMargin)
        assertEquals(11.0, holder.pageFormat?.topMargin)
        assertEquals(12.0, holder.pageFormat?.bottomMargin)
        assertEquals(650.0, holder.pageFormat?.height)
    }

    /**
     * Use case: a spinner of the settings dialog is bound to the inner margin, so what the user dials
     * in arrives in the page format through the binding without any code writing it there.
     */
    @Test
    fun writingThroughABindingReachesTheModelObject() {
        val input = SimpleDoubleProperty(28.0)
        property.innerMarginProperty.bind(input)

        assertEquals(28.0, holder.pageFormat?.innerMargin)

        input.set(32.0)

        assertEquals(32.0, holder.pageFormat?.innerMargin)

        property.innerMarginProperty.unbind()
    }

    /**
     * Use case: something changes the page format directly - an imported design for instance - so
     * every measure takes over the new value as soon as the format is refreshed.
     */
    @Test
    fun aChangeOnTheModelObjectBecomesVisible() {
        holder.pageFormat?.width = 500.0
        holder.pageFormat?.bottomMargin = 40.0
        holder.pageFormat?.mirroredMargins = true

        property.refresh()

        assertEquals(500.0, property.widthProperty.get())
        assertEquals(40.0, property.bottomMarginProperty.get())
        assertEquals(true, property.mirroredMarginsProperty.get())
        assertTrue(parentEvents > 0) { "the parent property was not told about the reading" }
    }

    /**
     * Use case: another paper size is chosen, so the whole page format is exchanged and every measure
     * takes over the value of the new object and reports it up to the root.
     */
    @Test
    fun exchangingTheModelObjectUpdatesEveryMeasure() {
        recorder.reset()
        parentEvents = 0

        property.set(
            PageFormat(
                width = 300.0,
                height = 500.0,
                innerMargin = 21.0,
                outerMargin = 14.0,
                topMargin = 16.0,
                bottomMargin = 19.0,
                mirroredMargins = true
            )
        )

        assertEquals(300.0, property.widthProperty.get())
        assertEquals(19.0, property.bottomMarginProperty.get())
        assertEquals(true, property.mirroredMarginsProperty.get())
        recorder.assertAllFired("exchanging the page format")
        assertEquals(1, parentEvents)
    }

    /**
     * Use case: a project is opened again without having been changed, so an exchange against an
     * equal page format leaves every measure quiet instead of redrawing the whole dialog.
     */
    @Test
    fun exchangingAgainstAnEqualObjectStaysQuiet() {
        recorder.reset()

        property.set(newPageFormat())

        recorder.assertNoneFired("exchanging the page format against an equal one")
    }

    /**
     * Use case: the settings dialog is built before a project is opened, so every measure answers with
     * a neutral value and drops what is written to it instead of failing.
     */
    @Test
    fun answersNeutrallyWithoutAModelObject() {
        property.set(null)

        assertEquals(0.0, property.widthProperty.get())
        assertEquals(0.0, property.innerMarginProperty.get())
        assertEquals(false, property.mirroredMarginsProperty.get())

        property.widthProperty.set(500.0)

        assertNull(holder.pageFormat)
    }
}

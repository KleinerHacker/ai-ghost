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
import org.pcsoft.app.aighost.model.project.design.ChapterDesign

/**
 * Developer tests for [ChapterDesignProperty].
 *
 * The property wraps the design of a chapter and offers both styles it carries - the one of the
 * heading and the one of the further heading lines - as properties of their own. The two styles are
 * wrapped exactly the same way, so every test walks through both of them instead of picking one.
 */
class ChapterDesignPropertyTest {

    /** Stands for the design carrying this part, the object a parent property writes into. */
    private class Holder(var design: ChapterDesign?)

    private lateinit var holder: Holder
    private lateinit var property: ChapterDesignProperty
    private lateinit var recorder: ChangeRecorder

    /** Counts what the parent property is told, so the report up to the root becomes visible. */
    private var parentEvents = 0

    @BeforeEach
    fun setUp() {
        holder = Holder(newDesign())
        parentEvents = 0
        property = ChapterDesignProperty(
            { holder.design = it },
            { holder.design },
            { parentEvents++ }
        )
        // A parent property aligns a nested one with the model object as soon as that object arrives,
        // so the same alignment happens here before the views are built.
        property.refresh()

        recorder = ChangeRecorder()
        recorder.watch("design", property)
        recorder.watch("design.titleStyle", property.titleStyleProperty)
        recorder.watch("design.titleStyle.font.name", property.titleStyleProperty.fontProperty.nameProperty)
        recorder.watch("design.titleAppendixStyle", property.titleAppendixStyleProperty)
        recorder.watch(
            "design.titleAppendixStyle.font.name",
            property.titleAppendixStyleProperty.fontProperty.nameProperty
        )

        parentEvents = 0
    }

    /** The design every test starts from, built fresh so no test sees the object of another. */
    private fun newDesign(): ChapterDesign = ChapterDesign(
        titleStyle = StyleData(FontData("Chapter Serif", 20, bold = true, italic = false), Alignment.LEFT),
        titleAppendixStyle = StyleData(
            FontData("Chapter Appendix Serif", 14, bold = false, italic = true),
            Alignment.CENTER
        )
    )

    /**
     * Use case: a view shows how a chapter heading is drawn, so both styles answer with the values
     * the wrapped object carries instead of a copy made earlier.
     */
    @Test
    fun readsBothStylesFromTheModelObject() {
        assertEquals("Chapter Serif", property.titleStyleProperty.fontProperty.nameProperty.get())
        assertEquals(20, property.titleStyleProperty.fontProperty.sizeProperty.get())
        assertEquals("Chapter Appendix Serif", property.titleAppendixStyleProperty.fontProperty.nameProperty.get())
        assertEquals(Alignment.CENTER, property.titleAppendixStyleProperty.alignmentProperty.get())
    }

    /**
     * Use case: the user styles the heading and its appendix separately, so each write reaches its
     * own field of the model object and reports the change up to the root.
     */
    @Test
    fun writingEachStyleReachesItsOwnField() {
        property.titleStyleProperty.fontProperty.nameProperty.set("Chosen Heading")
        property.titleAppendixStyleProperty.fontProperty.nameProperty.set("Chosen Appendix")

        assertEquals("Chosen Heading", holder.design?.titleStyle?.font?.name)
        assertEquals("Chosen Appendix", holder.design?.titleAppendixStyle?.font?.name)
        assertEquals(1, recorder.countOf("design.titleStyle.font.name"))
        assertEquals(1, recorder.countOf("design.titleAppendixStyle.font.name"))
        assertEquals(2, recorder.countOf("design"))
        assertEquals(2, parentEvents)
    }

    /**
     * Use case: a control of the design dialog is bound to the heading style, so what the user picks
     * arrives in the model object through the binding without any code writing it there.
     */
    @Test
    fun writingThroughABindingReachesTheModelObject() {
        val input = SimpleObjectProperty(StyleData(FontData("Bound Serif", 21), Alignment.RIGHT))
        property.titleStyleProperty.bind(input)

        assertEquals("Bound Serif", holder.design?.titleStyle?.font?.name)
        assertEquals(Alignment.RIGHT, holder.design?.titleStyle?.alignment)

        property.titleStyleProperty.unbind()
    }

    /**
     * Use case: something changes the model object directly - an imported design for instance - so
     * both style properties take over the new values as soon as the part is refreshed.
     */
    @Test
    fun aChangeOnTheModelObjectBecomesVisible() {
        holder.design?.titleStyle = StyleData(FontData("Imported Heading", 33), Alignment.BLOCK)
        holder.design?.titleAppendixStyle = StyleData(FontData("Imported Appendix", 9), Alignment.RIGHT)

        property.refresh()

        assertEquals("Imported Heading", property.titleStyleProperty.fontProperty.nameProperty.get())
        assertEquals("Imported Appendix", property.titleAppendixStyleProperty.fontProperty.nameProperty.get())
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
            ChapterDesign(
                titleStyle = StyleData(FontData("Other Heading", 44), Alignment.BLOCK),
                titleAppendixStyle = StyleData(FontData("Other Appendix", 12), Alignment.RIGHT)
            )
        )

        assertEquals("Other Heading", property.titleStyleProperty.fontProperty.nameProperty.get())
        assertEquals("Other Appendix", property.titleAppendixStyleProperty.fontProperty.nameProperty.get())
        recorder.assertAllFired("exchanging the chapter design")
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

        assertEquals(0, recorder.countOf("design.titleStyle.font.name"))
        assertEquals(0, recorder.countOf("design.titleAppendixStyle.font.name"))
    }

    /**
     * Use case: the user interface is built before a project is opened, so every field property
     * answers with a neutral value and drops what is written to it instead of failing.
     */
    @Test
    fun answersNeutrallyWithoutAModelObject() {
        holder.design = null
        property.refresh()

        assertNull(property.titleStyleProperty.get())
        assertNull(property.titleAppendixStyleProperty.get())

        property.titleStyleProperty.fontProperty.nameProperty.set("Ignored")

        assertNull(holder.design)
    }
}

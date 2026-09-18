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
import javafx.beans.property.SimpleObjectProperty
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.pcsoft.app.aighost.fx.model.ChangeRecorder
import org.pcsoft.app.aighost.model.common.Alignment
import org.pcsoft.app.aighost.model.common.FontData
import org.pcsoft.app.aighost.model.common.StyleData
import org.pcsoft.app.aighost.model.project.design.ChapterPageDesign

/**
 * Developer tests for [ChapterPageDesignProperty].
 *
 * The property wraps the design settings of the chapter pages and offers the three styles they carry -
 * and every field of those styles - as well as the switch pushing the heading onto a page of its own
 * as a property of its own. Every test looks at the object tree the way the user interface uses it: a
 * binding hangs on each level of the tree, and the tests assert that a change reaches every binding
 * that has to know about it - upwards to the parent the property reports to as well as downwards into
 * the fields of an exchanged object.
 */
class ChapterPageDesignPropertyTest {

    /** Stands for the design carrying this page, the object a parent property writes into. */
    private class Holder(var page: ChapterPageDesign?)

    private lateinit var holder: Holder
    private lateinit var property: ChapterPageDesignProperty
    private lateinit var recorder: ChangeRecorder

    /** Counts what the parent property is told, so the report up to the root becomes visible. */
    private var parentEvents = 0

    @BeforeEach
    fun setUp() {
        holder = Holder(newPage())
        parentEvents = 0
        property = ChapterPageDesignProperty()
        // A parent property reports a change of a nested one as its own and writes an exchanged object
        // back into the one carrying it, which is what these two listeners stand for.
        property.addListener { _ -> parentEvents++ }
        property.addListener { _, _, newValue -> holder.page = newValue }
        // A parent property hands the nested object to this property as soon as that object arrives.
        property.set(holder.page)

        recorder = ChangeRecorder()
        recorder.watch("page", property)
        recorder.watch("page.titleStyle", property.titleStyleProperty)
        recorder.watch("page.titleStyle.font.name", property.titleStyleProperty.fontProperty.nameProperty)
        recorder.watch("page.titleAppendixStyle", property.titleAppendixStyleProperty)
        recorder.watch(
            "page.titleAppendixStyle.alignment",
            property.titleAppendixStyleProperty.alignmentProperty
        )
        recorder.watch("page.textStyle", property.textStyleProperty)
        recorder.watch("page.textStyle.font.size", property.textStyleProperty.fontProperty.sizeProperty)
        recorder.watch("page.textStyle.textLineSpacing", property.textStyleProperty.textLineSpacingProperty)
        recorder.watch("page.titleOnSeparatePage", property.titleOnSeparatePageProperty)

        parentEvents = 0
    }

    /** The page design every test starts from, built fresh so no test sees the object of another. */
    private fun newPage(): ChapterPageDesign = ChapterPageDesign(
        titleStyle = StyleData(
            font = FontData("Chapter Title Serif", 20, bold = true),
            textLineSpacing = 1.3,
            alignment = Alignment.LEFT
        ),
        titleAppendixStyle = StyleData(
            font = FontData("Chapter Appendix Serif", 14, italic = true),
            textLineSpacing = 1.2,
            alignment = Alignment.CENTER
        ),
        textStyle = StyleData(
            font = FontData("Chapter Text Serif", 11),
            textLineSpacing = 1.4,
            alignment = Alignment.BLOCK
        ),
        titleOnSeparatePage = true
    )

    /**
     * Use case: the design dialog of the chapters is opened, so every style property, every field of it
     * and the switch answer with the values the wrapped object carries instead of a copy made earlier.
     */
    @Test
    fun readsEveryFieldFromTheModelObject() {
        assertEquals("Chapter Title Serif", property.titleStyleProperty.fontProperty.nameProperty.get())
        assertEquals(Alignment.CENTER, property.titleAppendixStyleProperty.alignmentProperty.get())
        assertEquals(11, property.textStyleProperty.fontProperty.sizeProperty.get())
        assertEquals(1.4, property.textStyleProperty.textLineSpacingProperty.get())
        assertTrue(property.titleOnSeparatePageProperty.get())
    }

    /**
     * Use case: the user picks another font family for the chapter heading, so the value reaches the
     * model object and every property between that field and the root reports the change.
     */
    @Test
    fun writingAFieldOfAStyleReachesTheModelObject() {
        property.titleStyleProperty.fontProperty.nameProperty.set("Chosen Serif")

        assertEquals("Chosen Serif", holder.page?.titleStyle?.font?.name)
        assertEquals(1, recorder.countOf("page.titleStyle.font.name"))
        assertEquals(1, recorder.countOf("page.titleStyle"))
        assertEquals(1, recorder.countOf("page"))
        assertEquals(1, parentEvents)
    }

    /**
     * Use case: the user sets the lines of the chapter body text further apart, so the factor reaches
     * the style nested in the page design and every level above it reports the change.
     */
    @Test
    fun writingTheLineSpacingOfAStyleReachesTheModelObject() {
        property.textStyleProperty.textLineSpacingProperty.set(1.9)

        assertEquals(1.9, holder.page?.textStyle?.textLineSpacing)
        assertEquals(1, recorder.countOf("page.textStyle.textLineSpacing"))
        assertEquals(1, recorder.countOf("page.textStyle"))
        assertEquals(1, recorder.countOf("page"))
        assertEquals(1, parentEvents)
    }

    /**
     * Use case: the user lets the chapter heading share the page with the text again, so the switch
     * reaches the model object and the page design as a whole reports the change.
     */
    @Test
    fun writingTheSwitchReachesTheModelObject() {
        property.titleOnSeparatePageProperty.set(false)

        assertFalse(holder.page?.titleOnSeparatePage ?: true)
        assertEquals(1, recorder.countOf("page.titleOnSeparatePage"))
        assertEquals(1, recorder.countOf("page"))
        assertEquals(1, parentEvents)
    }

    /**
     * Use case: a whole style of the page is replaced - a preset was applied - so the field properties
     * below it belong to another object afterwards and the change reaches the model object.
     */
    @Test
    fun writingAWholeStyleReachesTheModelObject() {
        property.titleAppendixStyle = StyleData(
            font = FontData("Replaced Serif", 15),
            textLineSpacing = 1.8,
            alignment = Alignment.LEFT
        )

        assertEquals("Replaced Serif", holder.page?.titleAppendixStyle?.font?.name)
        assertEquals(Alignment.LEFT, property.titleAppendixStyleProperty.alignmentProperty.get())
        assertEquals(1, recorder.countOf("page.titleAppendixStyle"))
        assertEquals(1, recorder.countOf("page"))
        assertEquals(1, parentEvents)
    }

    /**
     * Use case: a control of the design dialog is bound to a style, so what the user picks arrives in
     * the model object through the binding without any code writing it there.
     */
    @Test
    fun writingThroughABindingReachesTheModelObject() {
        val input = SimpleObjectProperty(
            StyleData(font = FontData("Bound Serif", 12), textLineSpacing = 1.6, alignment = Alignment.RIGHT)
        )
        property.textStyleProperty.bind(input)

        assertEquals("Bound Serif", holder.page?.textStyle?.font?.name)
        assertEquals(Alignment.RIGHT, holder.page?.textStyle?.alignment)

        property.textStyleProperty.unbind()
    }

    /**
     * Use case: a check box of the design dialog is bound to the switch of the heading page, so what
     * the user ticks arrives in the model object through the binding.
     */
    @Test
    fun writingTheSwitchThroughABindingReachesTheModelObject() {
        val input = SimpleBooleanProperty(true)
        property.titleOnSeparatePageProperty.bind(input)

        assertTrue(holder.page?.titleOnSeparatePage ?: false)

        input.set(false)

        assertFalse(holder.page?.titleOnSeparatePage ?: true)

        property.titleOnSeparatePageProperty.unbind()
    }

    /**
     * Use case: something changes the model object directly - an imported design for instance - so the
     * field properties take over the new values as soon as the page design is refreshed.
     */
    @Test
    fun aChangeOnTheModelObjectBecomesVisible() {
        holder.page?.titleStyle?.font?.name = "Imported Serif"
        holder.page?.titleAppendixStyle?.alignment = Alignment.RIGHT
        holder.page?.textStyle?.textLineSpacing = 2.0
        holder.page?.titleOnSeparatePage = false

        property.refresh()

        assertEquals("Imported Serif", property.titleStyleProperty.fontProperty.nameProperty.get())
        assertEquals(Alignment.RIGHT, property.titleAppendixStyleProperty.alignmentProperty.get())
        assertEquals(2.0, property.textStyleProperty.textLineSpacingProperty.get())
        assertFalse(property.titleOnSeparatePageProperty.get())
    }

    /**
     * Use case: another project is opened, so the whole page design is exchanged and every property
     * below it takes over the value of the new object and reports it up to the root.
     */
    @Test
    fun exchangingTheModelObjectUpdatesEveryField() {
        recorder.reset()
        parentEvents = 0

        property.set(
            ChapterPageDesign(
                titleStyle = StyleData(
                    font = FontData("Other Title", 25),
                    textLineSpacing = 2.1,
                    alignment = Alignment.RIGHT
                ),
                titleAppendixStyle = StyleData(
                    font = FontData("Other Appendix", 18),
                    textLineSpacing = 2.2,
                    alignment = Alignment.LEFT
                ),
                textStyle = StyleData(
                    font = FontData("Other Text", 13),
                    textLineSpacing = 2.3,
                    alignment = Alignment.LEFT
                ),
                titleOnSeparatePage = false
            )
        )

        assertEquals("Other Title", property.titleStyleProperty.fontProperty.nameProperty.get())
        assertEquals(Alignment.LEFT, property.titleAppendixStyleProperty.alignmentProperty.get())
        assertEquals(13, property.textStyleProperty.fontProperty.sizeProperty.get())
        assertFalse(property.titleOnSeparatePageProperty.get())
        recorder.assertAllFired("exchanging the page design")
        assertEquals(1, parentEvents)
    }

    /**
     * Use case: a project is opened again without having been changed, so an exchange against an equal
     * object leaves every field property quiet instead of redrawing the whole dialog.
     */
    @Test
    fun exchangingAgainstAnEqualObjectStaysQuiet() {
        recorder.reset()

        property.set(newPage())

        recorder.assertNoneFired("exchanging the page design against an equal one")
    }

    /**
     * Use case: the user interface is built before a project is opened, so every field property answers
     * with a neutral value and drops what is written to it instead of failing.
     */
    @Test
    fun answersNeutrallyWithoutAModelObject() {
        // A parent property hands out nothing while it carries no design, which is what setting the
        // property itself to nothing stands for.
        property.set(null)

        assertNull(property.titleStyleProperty.get())
        assertNull(property.titleAppendixStyleProperty.get())
        assertNull(property.textStyleProperty.get())
        assertFalse(property.titleOnSeparatePageProperty.get())

        property.textStyleProperty.fontProperty.nameProperty.set("Ignored")
        property.titleOnSeparatePageProperty.set(true)

        assertNull(holder.page)
    }
}

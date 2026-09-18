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
import org.pcsoft.app.aighost.model.project.design.TitlePageDesign

/**
 * Developer tests for [TitlePageDesignProperty].
 *
 * The property wraps the design settings of the title page and offers the three styles it carries -
 * and every field of those styles - as a property of its own. Every test looks at the object tree the
 * way the user interface uses it: a binding hangs on each level of the tree, and the tests assert that
 * a change reaches every binding that has to know about it - upwards to the parent the property
 * reports to as well as downwards into the fields of an exchanged object.
 */
class TitlePageDesignPropertyTest {

    /** Stands for the design carrying this page, the object a parent property writes into. */
    private class Holder(var page: TitlePageDesign?)

    private lateinit var holder: Holder
    private lateinit var property: TitlePageDesignProperty
    private lateinit var recorder: ChangeRecorder

    /** Counts what the parent property is told, so the report up to the root becomes visible. */
    private var parentEvents = 0

    @BeforeEach
    fun setUp() {
        holder = Holder(newPage())
        parentEvents = 0
        property = TitlePageDesignProperty()
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
        recorder.watch("page.titleStyle.textLineSpacing", property.titleStyleProperty.textLineSpacingProperty)
        recorder.watch("page.titleStyle.alignment", property.titleStyleProperty.alignmentProperty)
        recorder.watch("page.titleAppendixStyle", property.titleAppendixStyleProperty)
        recorder.watch(
            "page.titleAppendixStyle.font.size",
            property.titleAppendixStyleProperty.fontProperty.sizeProperty
        )
        recorder.watch("page.showAuthor", property.showAuthorProperty)
        recorder.watch("page.authorStyle", property.authorStyleProperty)
        recorder.watch("page.authorStyle.font.name", property.authorStyleProperty.fontProperty.nameProperty)

        parentEvents = 0
    }

    /** The page design every test starts from, built fresh so no test sees the object of another. */
    private fun newPage(): TitlePageDesign = TitlePageDesign(
        titleStyle = StyleData(
            font = FontData("Title Serif", 28, bold = true),
            textLineSpacing = 1.5,
            alignment = Alignment.CENTER
        ),
        titleAppendixStyle = StyleData(
            font = FontData("Title Appendix Serif", 18, italic = true),
            textLineSpacing = 1.3,
            alignment = Alignment.CENTER
        ),
        showAuthor = true,
        authorStyle = StyleData(
            font = FontData("Author Serif", 16),
            textLineSpacing = 1.1,
            alignment = Alignment.RIGHT
        )
    )

    /**
     * Use case: the design dialog of the title page is opened, so every style property and every field
     * of it answer with the values the wrapped object carries instead of a copy made earlier.
     */
    @Test
    fun readsEveryFieldFromTheModelObject() {
        assertEquals("Title Serif", property.titleStyleProperty.fontProperty.nameProperty.get())
        assertEquals(1.5, property.titleStyleProperty.textLineSpacingProperty.get())
        assertEquals(Alignment.CENTER, property.titleStyleProperty.alignmentProperty.get())
        assertEquals(18, property.titleAppendixStyleProperty.fontProperty.sizeProperty.get())
        assertTrue(property.showAuthorProperty.get())
        assertEquals("Author Serif", property.authorStyleProperty.fontProperty.nameProperty.get())
        assertEquals(1.1, property.authorStyleProperty.textLineSpacingProperty.get())
    }

    /**
     * Use case: the user picks another font family for the main title, so the value reaches the model
     * object and every property between that field and the root reports the change.
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
     * Use case: the user sets the lines of the further title lines further apart, so the factor reaches
     * the style nested in the page design and every level above it reports the change.
     */
    @Test
    fun writingTheLineSpacingOfAStyleReachesTheModelObject() {
        property.titleAppendixStyleProperty.textLineSpacingProperty.set(1.9)

        assertEquals(1.9, holder.page?.titleAppendixStyle?.textLineSpacing)
        assertEquals(1, recorder.countOf("page.titleAppendixStyle"))
        assertEquals(1, recorder.countOf("page"))
        assertEquals(1, parentEvents)
    }

    /**
     * Use case: the user takes the author name off the title page, so the switch reaches the model
     * object and the page design as a whole reports the change instead of the switch alone.
     */
    @Test
    fun writingTheSwitchReachesTheModelObject() {
        property.showAuthorProperty.set(false)

        assertFalse(holder.page?.showAuthor ?: true)
        assertEquals(1, recorder.countOf("page.showAuthor"))
        assertEquals(1, recorder.countOf("page"))
        assertEquals(1, parentEvents)
    }

    /**
     * Use case: a whole style of the page is replaced - a preset was applied - so the field properties
     * below it belong to another object afterwards and the change reaches the model object.
     */
    @Test
    fun writingAWholeStyleReachesTheModelObject() {
        property.authorStyle = StyleData(
            font = FontData("Replaced Serif", 20),
            textLineSpacing = 1.7,
            alignment = Alignment.LEFT
        )

        assertEquals("Replaced Serif", holder.page?.authorStyle?.font?.name)
        assertEquals(1.7, holder.page?.authorStyle?.textLineSpacing)
        assertEquals("Replaced Serif", property.authorStyleProperty.fontProperty.nameProperty.get())
        assertEquals(1, recorder.countOf("page.authorStyle"))
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
            StyleData(font = FontData("Bound Serif", 21), textLineSpacing = 1.4, alignment = Alignment.RIGHT)
        )
        property.titleAppendixStyleProperty.bind(input)

        assertEquals("Bound Serif", holder.page?.titleAppendixStyle?.font?.name)
        assertEquals(Alignment.RIGHT, holder.page?.titleAppendixStyle?.alignment)

        property.titleAppendixStyleProperty.unbind()
    }

    /**
     * Use case: a check box of the design dialog is bound to the switch of the author name, so what the
     * user ticks arrives in the model object through the binding.
     */
    @Test
    fun writingTheSwitchThroughABindingReachesTheModelObject() {
        val input = SimpleBooleanProperty(true)
        property.showAuthorProperty.bind(input)

        assertTrue(holder.page?.showAuthor ?: false)

        input.set(false)

        assertFalse(holder.page?.showAuthor ?: true)

        property.showAuthorProperty.unbind()
    }

    /**
     * Use case: something changes the model object directly - an imported design for instance - so the
     * field properties take over the new values as soon as the page design is refreshed.
     */
    @Test
    fun aChangeOnTheModelObjectBecomesVisible() {
        holder.page?.titleStyle?.font?.name = "Imported Serif"
        holder.page?.titleStyle?.textLineSpacing = 2.0
        holder.page?.showAuthor = false
        holder.page?.authorStyle = StyleData(font = FontData("Imported Author", 13), alignment = Alignment.LEFT)

        property.refresh()

        assertEquals("Imported Serif", property.titleStyleProperty.fontProperty.nameProperty.get())
        assertEquals(2.0, property.titleStyleProperty.textLineSpacingProperty.get())
        assertFalse(property.showAuthorProperty.get())
        assertEquals("Imported Author", property.authorStyleProperty.fontProperty.nameProperty.get())
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
            TitlePageDesign(
                titleStyle = StyleData(
                    font = FontData("Other Title", 30),
                    textLineSpacing = 2.1,
                    alignment = Alignment.LEFT
                ),
                titleAppendixStyle = StyleData(
                    font = FontData("Other Appendix", 19),
                    textLineSpacing = 2.2,
                    alignment = Alignment.LEFT
                ),
                showAuthor = false,
                authorStyle = StyleData(
                    font = FontData("Other Author", 17),
                    textLineSpacing = 2.3,
                    alignment = Alignment.LEFT
                )
            )
        )

        assertEquals("Other Title", property.titleStyleProperty.fontProperty.nameProperty.get())
        assertEquals(19, property.titleAppendixStyleProperty.fontProperty.sizeProperty.get())
        assertFalse(property.showAuthorProperty.get())
        assertEquals("Other Author", property.authorStyleProperty.fontProperty.nameProperty.get())
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
        assertNull(property.authorStyleProperty.get())
        assertFalse(property.showAuthorProperty.get())

        property.titleStyleProperty.fontProperty.nameProperty.set("Ignored")
        property.showAuthorProperty.set(true)

        assertNull(holder.page)
    }
}

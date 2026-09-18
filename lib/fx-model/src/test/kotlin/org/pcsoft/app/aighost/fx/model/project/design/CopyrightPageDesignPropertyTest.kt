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
import org.pcsoft.app.aighost.model.project.design.CopyrightPageDesign

/**
 * Developer tests for [CopyrightPageDesignProperty].
 *
 * The property wraps the design settings of the copyright page and offers the three styles it carries -
 * and every field of those styles - as a property of its own. Every test looks at the object tree the
 * way the user interface uses it: a binding hangs on each level of the tree, and the tests assert that
 * a change reaches every binding that has to know about it - upwards to the parent the property
 * reports to as well as downwards into the fields of an exchanged object.
 */
class CopyrightPageDesignPropertyTest {

    /** Stands for the design carrying this page, the object a parent property writes into. */
    private class Holder(var page: CopyrightPageDesign?)

    private lateinit var holder: Holder
    private lateinit var property: CopyrightPageDesignProperty
    private lateinit var recorder: ChangeRecorder

    /** Counts what the parent property is told, so the report up to the root becomes visible. */
    private var parentEvents = 0

    @BeforeEach
    fun setUp() {
        holder = Holder(newPage())
        parentEvents = 0
        property = CopyrightPageDesignProperty()
        // A parent property reports a change of a nested one as its own and writes an exchanged object
        // back into the one carrying it, which is what these two listeners stand for.
        property.addListener { _ -> parentEvents++ }
        property.addListener { _, _, newValue -> holder.page = newValue }
        // A parent property hands the nested object to this property as soon as that object arrives.
        property.set(holder.page)

        recorder = ChangeRecorder()
        recorder.watch("page", property)
        recorder.watch("page.copyrightStyle", property.copyrightStyleProperty)
        recorder.watch(
            "page.copyrightStyle.font.name",
            property.copyrightStyleProperty.fontProperty.nameProperty
        )
        recorder.watch(
            "page.copyrightStyle.textLineSpacing",
            property.copyrightStyleProperty.textLineSpacingProperty
        )
        recorder.watch("page.copyrightAppendixStyle", property.copyrightAppendixStyleProperty)
        recorder.watch(
            "page.copyrightAppendixStyle.alignment",
            property.copyrightAppendixStyleProperty.alignmentProperty
        )
        recorder.watch("page.showAuthor", property.showAuthorProperty)
        recorder.watch("page.authorStyle", property.authorStyleProperty)
        recorder.watch("page.authorStyle.font.size", property.authorStyleProperty.fontProperty.sizeProperty)

        parentEvents = 0
    }

    /** The page design every test starts from, built fresh so no test sees the object of another. */
    private fun newPage(): CopyrightPageDesign = CopyrightPageDesign(
        copyrightStyle = StyleData(
            font = FontData("Copyright Serif", 9),
            textLineSpacing = 1.0,
            alignment = Alignment.LEFT
        ),
        copyrightAppendixStyle = StyleData(
            font = FontData("Copyright Appendix Serif", 8),
            textLineSpacing = 1.1,
            alignment = Alignment.LEFT
        ),
        showAuthor = true,
        authorStyle = StyleData(
            font = FontData("Author Serif", 10),
            textLineSpacing = 1.2,
            alignment = Alignment.CENTER
        )
    )

    /**
     * Use case: the design dialog of the copyright page is opened, so every style property and every
     * field of it answer with the values the wrapped object carries instead of a copy made earlier.
     */
    @Test
    fun readsEveryFieldFromTheModelObject() {
        assertEquals("Copyright Serif", property.copyrightStyleProperty.fontProperty.nameProperty.get())
        assertEquals(1.0, property.copyrightStyleProperty.textLineSpacingProperty.get())
        assertEquals(Alignment.LEFT, property.copyrightAppendixStyleProperty.alignmentProperty.get())
        assertTrue(property.showAuthorProperty.get())
        assertEquals(10, property.authorStyleProperty.fontProperty.sizeProperty.get())
    }

    /**
     * Use case: the user picks another font family for the copyright notice, so the value reaches the
     * model object and every property between that field and the root reports the change.
     */
    @Test
    fun writingAFieldOfAStyleReachesTheModelObject() {
        property.copyrightStyleProperty.fontProperty.nameProperty.set("Chosen Serif")

        assertEquals("Chosen Serif", holder.page?.copyrightStyle?.font?.name)
        assertEquals(1, recorder.countOf("page.copyrightStyle.font.name"))
        assertEquals(1, recorder.countOf("page.copyrightStyle"))
        assertEquals(1, recorder.countOf("page"))
        assertEquals(1, parentEvents)
    }

    /**
     * Use case: the user sets the lines of the copyright notice further apart, so the factor reaches
     * the style nested in the page design and every level above it reports the change.
     */
    @Test
    fun writingTheLineSpacingOfAStyleReachesTheModelObject() {
        property.copyrightStyleProperty.textLineSpacingProperty.set(1.6)

        assertEquals(1.6, holder.page?.copyrightStyle?.textLineSpacing)
        assertEquals(1, recorder.countOf("page.copyrightStyle.textLineSpacing"))
        assertEquals(1, recorder.countOf("page.copyrightStyle"))
        assertEquals(1, recorder.countOf("page"))
        assertEquals(1, parentEvents)
    }

    /**
     * Use case: the user takes the author name off the copyright page, so the switch reaches the model
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
        property.copyrightAppendixStyle = StyleData(
            font = FontData("Replaced Serif", 7),
            textLineSpacing = 1.8,
            alignment = Alignment.RIGHT
        )

        assertEquals("Replaced Serif", holder.page?.copyrightAppendixStyle?.font?.name)
        assertEquals(Alignment.RIGHT, property.copyrightAppendixStyleProperty.alignmentProperty.get())
        assertEquals(1, recorder.countOf("page.copyrightAppendixStyle"))
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
            StyleData(font = FontData("Bound Serif", 11), textLineSpacing = 1.4, alignment = Alignment.BLOCK)
        )
        property.authorStyleProperty.bind(input)

        assertEquals("Bound Serif", holder.page?.authorStyle?.font?.name)
        assertEquals(Alignment.BLOCK, holder.page?.authorStyle?.alignment)

        property.authorStyleProperty.unbind()
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
        holder.page?.copyrightStyle?.font?.name = "Imported Serif"
        holder.page?.copyrightAppendixStyle?.alignment = Alignment.RIGHT
        holder.page?.showAuthor = false

        property.refresh()

        assertEquals("Imported Serif", property.copyrightStyleProperty.fontProperty.nameProperty.get())
        assertEquals(Alignment.RIGHT, property.copyrightAppendixStyleProperty.alignmentProperty.get())
        assertFalse(property.showAuthorProperty.get())
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
            CopyrightPageDesign(
                copyrightStyle = StyleData(
                    font = FontData("Other Copyright", 10),
                    textLineSpacing = 2.1,
                    alignment = Alignment.RIGHT
                ),
                copyrightAppendixStyle = StyleData(
                    font = FontData("Other Appendix", 9),
                    textLineSpacing = 2.2,
                    alignment = Alignment.RIGHT
                ),
                showAuthor = false,
                authorStyle = StyleData(
                    font = FontData("Other Author", 12),
                    textLineSpacing = 2.3,
                    alignment = Alignment.RIGHT
                )
            )
        )

        assertEquals("Other Copyright", property.copyrightStyleProperty.fontProperty.nameProperty.get())
        assertEquals(Alignment.RIGHT, property.copyrightAppendixStyleProperty.alignmentProperty.get())
        assertFalse(property.showAuthorProperty.get())
        assertEquals(12, property.authorStyleProperty.fontProperty.sizeProperty.get())
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

        assertNull(property.copyrightStyleProperty.get())
        assertNull(property.copyrightAppendixStyleProperty.get())
        assertNull(property.authorStyleProperty.get())
        assertFalse(property.showAuthorProperty.get())

        property.copyrightStyleProperty.fontProperty.nameProperty.set("Ignored")
        property.showAuthorProperty.set(true)

        assertNull(holder.page)
    }
}

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
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.pcsoft.app.aighost.fx.model.ChangeRecorder
import org.pcsoft.app.aighost.model.common.Alignment
import org.pcsoft.app.aighost.model.common.FontData
import org.pcsoft.app.aighost.model.common.StyleData
import org.pcsoft.app.aighost.model.project.design.AuthorDesign
import org.pcsoft.app.aighost.model.project.design.ChapterDesign
import org.pcsoft.app.aighost.model.project.design.CopyrightDesign
import org.pcsoft.app.aighost.model.project.design.Design
import org.pcsoft.app.aighost.model.project.design.TextDesign
import org.pcsoft.app.aighost.model.project.design.TitleDesign

/**
 * Developer tests for [DesignProperty].
 *
 * The property wraps the typographic and page settings of a project and offers every part of that
 * object - and every field of the styles nested in those parts - as a property of its own. The tests
 * watch the whole tree at once: every design part, the style below it, the font of that style and the
 * page flags, so a change that fails to travel through one of the levels is named by the assertion.
 */
class DesignPropertyTest {

    /** Stands for the project carrying the design, the object a parent property writes into. */
    private class Holder(var design: Design?)

    private lateinit var holder: Holder
    private lateinit var property: DesignProperty
    private lateinit var recorder: ChangeRecorder

    /** Counts what the parent property is told, so the report up to the root becomes visible. */
    private var parentEvents = 0

    @BeforeEach
    fun setUp() {
        holder = Holder(newDesign())
        parentEvents = 0
        property = DesignProperty()
        // A parent property reports a change of a nested one as its own and writes an exchanged object
        // back into the one carrying it, which is what these two listeners stand for.
        property.addListener { _ -> parentEvents++ }
        property.addListener { _, _, newValue -> holder.design = newValue }
        // A parent property hands the nested object to this property as soon as that object arrives.
        property.set(holder.design)

        recorder = ChangeRecorder()
        recorder.watch("design", property)
        recorder.watch("design.author", property.authorDesignProperty)
        recorder.watch("design.author.style.font.name", property.authorDesignProperty.styleProperty.fontProperty.nameProperty)
        recorder.watch("design.copyright", property.copyrightDesignProperty)
        recorder.watch("design.copyright.show", property.copyrightDesignProperty.showProperty)
        recorder.watch("design.title", property.titleDesignProperty)
        recorder.watch("design.title.style.alignment", property.titleDesignProperty.styleProperty.alignmentProperty)
        recorder.watch("design.chapter", property.chapterDesignProperty)
        recorder.watch("design.chapter.titleStyle", property.chapterDesignProperty.titleStyleProperty)
        recorder.watch("design.chapter.titleAppendixStyle", property.chapterDesignProperty.titleAppendixStyleProperty)
        recorder.watch("design.text", property.textDesignProperty)
        recorder.watch("design.text.style.font.size", property.textDesignProperty.styleProperty.fontProperty.sizeProperty)
        recorder.watch("design.startWithEmptyPage", property.startWithEmptyPageProperty)
        recorder.watch("design.endWithEmptyPage", property.endWithEmptyPageProperty)

        parentEvents = 0
    }

    /** The design every test starts from, built fresh so no test sees the object of another. */
    private fun newDesign(): Design = Design(
        authorDesign = AuthorDesign(StyleData(FontData("Author Serif", 16), Alignment.CENTER)),
        copyrightDesign = CopyrightDesign(StyleData(FontData("Copyright Serif", 8), Alignment.LEFT), show = true),
        titleDesign = TitleDesign(StyleData(FontData("Title Serif", 28, bold = true), Alignment.CENTER)),
        chapterDesign = ChapterDesign(
            titleStyle = StyleData(FontData("Chapter Serif", 20, bold = true), Alignment.LEFT),
            titleAppendixStyle = StyleData(FontData("Chapter Appendix Serif", 14, italic = true), Alignment.CENTER)
        ),
        textDesign = TextDesign(StyleData(FontData("Text Serif", 11), Alignment.BLOCK)),
        startWithEmptyPage = true,
        endWithEmptyPage = false
    )

    /**
     * Use case: the design dialog is opened, so every design part and every page flag answers with
     * the value the wrapped object carries instead of a copy made at some earlier point.
     */
    @Test
    fun readsEveryPartFromTheModelObject() {
        assertEquals("Author Serif", property.authorDesignProperty.styleProperty.fontProperty.nameProperty.get())
        assertEquals(true, property.copyrightDesignProperty.showProperty.get())
        assertEquals(Alignment.CENTER, property.titleDesignProperty.styleProperty.alignmentProperty.get())
        assertEquals("Chapter Serif", property.chapterDesignProperty.titleStyleProperty.fontProperty.nameProperty.get())
        assertEquals(11, property.textDesignProperty.styleProperty.fontProperty.sizeProperty.get())
        assertTrue(property.startWithEmptyPageProperty.get())
        assertFalse(property.endWithEmptyPageProperty.get())
    }

    /**
     * Use case: the user picks another font for the body text, so the value reaches the model object
     * through every level of the tree and each of them reports the change up to the root.
     */
    @Test
    fun writingDeepInsideTheTreeReachesTheModelObject() {
        property.textDesignProperty.styleProperty.fontProperty.sizeProperty.set(13)

        assertEquals(13, holder.design?.textDesign?.style?.font?.size)
        assertEquals(1, recorder.countOf("design.text.style.font.size"))
        assertEquals(1, recorder.countOf("design.text"))
        assertEquals(1, recorder.countOf("design"))
        assertEquals(1, parentEvents)
    }

    /**
     * Use case: the user turns the leading empty page off, so the flag reaches the model object and
     * the design as a whole reports the change instead of the flag alone.
     */
    @Test
    fun writingAPageFlagReachesTheModelObject() {
        property.startWithEmptyPageProperty.set(false)

        assertFalse(holder.design?.startWithEmptyPage ?: true)
        assertEquals(1, recorder.countOf("design.startWithEmptyPage"))
        assertEquals(1, recorder.countOf("design"))
        assertEquals(1, parentEvents)
    }

    /**
     * Use case: a check box of the design dialog is bound to a page flag, so what the user ticks
     * arrives in the model object through the binding without any code writing it there.
     */
    @Test
    fun writingThroughABindingReachesTheModelObject() {
        val input = SimpleBooleanProperty(true)
        property.endWithEmptyPageProperty.bind(input)

        assertEquals(true, holder.design?.endWithEmptyPage)

        input.set(false)

        assertFalse(holder.design?.endWithEmptyPage ?: true)

        property.endWithEmptyPageProperty.unbind()
    }

    /**
     * Use case: something changes the model object directly - an imported design for instance - so
     * every property of the tree takes over the new values as soon as the design is refreshed.
     */
    @Test
    fun aChangeOnTheModelObjectBecomesVisible() {
        holder.design?.textDesign = TextDesign(StyleData(FontData("Imported Serif", 19), Alignment.LEFT))
        holder.design?.endWithEmptyPage = true

        property.refresh()

        assertEquals("Imported Serif", property.textDesignProperty.styleProperty.fontProperty.nameProperty.get())
        assertTrue(property.endWithEmptyPageProperty.get())
    }

    /**
     * Use case: another project is opened, so the whole design is exchanged and every property of the
     * tree takes over the value of the new object and reports it up to the root.
     */
    @Test
    fun exchangingTheModelObjectUpdatesEveryPart() {
        recorder.reset()
        parentEvents = 0

        property.set(
            Design(
                authorDesign = AuthorDesign(StyleData(FontData("Other Author", 17), Alignment.LEFT)),
                copyrightDesign = CopyrightDesign(StyleData(FontData("Other Copyright", 9), Alignment.RIGHT)),
                titleDesign = TitleDesign(StyleData(FontData("Other Title", 29), Alignment.LEFT)),
                chapterDesign = ChapterDesign(
                    titleStyle = StyleData(FontData("Other Chapter", 21), Alignment.RIGHT),
                    titleAppendixStyle = StyleData(FontData("Other Appendix", 15), Alignment.LEFT)
                ),
                textDesign = TextDesign(StyleData(FontData("Other Text", 12), Alignment.LEFT)),
                startWithEmptyPage = false,
                endWithEmptyPage = true
            )
        )

        assertEquals("Other Author", property.authorDesignProperty.styleProperty.fontProperty.nameProperty.get())
        assertFalse(property.copyrightDesignProperty.showProperty.get())
        assertEquals(12, property.textDesignProperty.styleProperty.fontProperty.sizeProperty.get())
        recorder.assertAllFired("exchanging the design")
        assertEquals(1, parentEvents)
    }

    /**
     * Use case: a project is opened again without having been changed, so an exchange against an
     * equal design leaves every property quiet instead of redrawing the whole dialog.
     */
    @Test
    fun exchangingAgainstAnEqualObjectStaysQuiet() {
        recorder.reset()

        property.set(newDesign())

        recorder.assertNoneFired("exchanging the design against an equal one")
    }

    /**
     * Use case: the user interface is built before a project is opened, so every property of the tree
     * answers with a neutral value and drops what is written to it instead of failing.
     */
    @Test
    fun answersNeutrallyWithoutAModelObject() {
        // A parent property hands out nothing while it carries no project, which is what setting the
        // property itself to nothing stands for.
        property.set(null)

        assertNull(property.authorDesignProperty.get())
        assertNull(property.textDesignProperty.get())
        assertFalse(property.startWithEmptyPageProperty.get())

        property.startWithEmptyPageProperty.set(true)

        assertNull(holder.design)
    }
}

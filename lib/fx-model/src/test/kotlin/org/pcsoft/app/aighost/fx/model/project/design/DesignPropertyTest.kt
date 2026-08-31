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
import javafx.beans.property.SimpleDoubleProperty
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
import org.pcsoft.app.aighost.model.project.design.BlurbPageDesign
import org.pcsoft.app.aighost.model.project.design.ChapterPageDesign
import org.pcsoft.app.aighost.model.project.design.CopyrightPageDesign
import org.pcsoft.app.aighost.model.project.design.Design
import org.pcsoft.app.aighost.model.project.design.EpilogPageDesign
import org.pcsoft.app.aighost.model.project.design.PageFormat
import org.pcsoft.app.aighost.model.project.design.PrologPageDesign
import org.pcsoft.app.aighost.model.project.design.TitlePageDesign

/**
 * Developer tests for [DesignProperty].
 *
 * The property wraps the typographic and page settings of a project and offers every part of that
 * object - the page format, every page design and every field of the styles nested in those designs -
 * as a property of its own. The tests watch the whole tree at once: the page geometry, every page
 * design, the styles below them, the fonts of those styles, their line spacings and the page flags, so
 * a change that fails to travel through one of the levels is named by the assertion.
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
        recorder.watch("design.pageFormat", property.pageFormatProperty)
        recorder.watch("design.pageFormat.innerMargin", property.pageFormatProperty.innerMarginProperty)
        recorder.watch("design.titlePage", property.titlePageProperty)
        recorder.watch(
            "design.titlePage.titleStyle.font.name",
            property.titlePageProperty.titleStyleProperty.fontProperty.nameProperty
        )
        recorder.watch("design.titlePage.showAuthor", property.titlePageProperty.showAuthorProperty)
        recorder.watch("design.copyrightPage", property.copyrightPageProperty)
        recorder.watch(
            "design.copyrightPage.copyrightStyle.font.size",
            property.copyrightPageProperty.copyrightStyleProperty.fontProperty.sizeProperty
        )
        recorder.watch("design.prologPage", property.prologPageProperty)
        recorder.watch(
            "design.prologPage.textStyle.alignment",
            property.prologPageProperty.textStyleProperty.alignmentProperty
        )
        recorder.watch("design.blurbPage", property.blurbPageProperty)
        recorder.watch(
            "design.blurbPage.textStyle.font.size",
            property.blurbPageProperty.textStyleProperty.fontProperty.sizeProperty
        )
        recorder.watch("design.chapterPage", property.chapterPageProperty)
        recorder.watch(
            "design.chapterPage.textStyle.textLineSpacing",
            property.chapterPageProperty.textStyleProperty.textLineSpacingProperty
        )
        recorder.watch(
            "design.chapterPage.titleOnSeparatePage",
            property.chapterPageProperty.titleOnSeparatePageProperty
        )
        recorder.watch("design.epilogPage", property.epilogPageProperty)
        recorder.watch(
            "design.epilogPage.titleStyle.font.name",
            property.epilogPageProperty.titleStyleProperty.fontProperty.nameProperty
        )
        recorder.watch("design.startWithEmptyPage", property.startWithEmptyPageProperty)
        recorder.watch("design.endWithEmptyPage", property.endWithEmptyPageProperty)

        parentEvents = 0
    }

    /** The design every test starts from, built fresh so no test sees the object of another. */
    private fun newDesign(): Design = Design(
        pageFormat = PageFormat(
            width = 400.0,
            height = 600.0,
            innerMargin = 25.0,
            outerMargin = 18.0,
            topMargin = 12.0,
            bottomMargin = 22.0
        ),
        titlePage = TitlePageDesign(
            titleStyle = StyleData(
                font = FontData("Title Serif", 28, bold = true),
                textLineSpacing = 1.5,
                alignment = Alignment.CENTER
            ),
            titleAppendixStyle = StyleData(
                font = FontData("Title Appendix Serif", 18),
                textLineSpacing = 1.3,
                alignment = Alignment.CENTER
            ),
            showAuthor = true,
            authorStyle = StyleData(
                font = FontData("Author Serif", 16),
                textLineSpacing = 1.1,
                alignment = Alignment.CENTER
            )
        ),
        copyrightPage = CopyrightPageDesign(
            copyrightStyle = StyleData(
                font = FontData("Copyright Serif", 9),
                textLineSpacing = 1.0,
                alignment = Alignment.LEFT
            ),
            copyrightAppendixStyle = StyleData(
                font = FontData("Copyright Appendix Serif", 8),
                textLineSpacing = 1.0,
                alignment = Alignment.LEFT
            ),
            showAuthor = false,
            authorStyle = StyleData(
                font = FontData("Copyright Author Serif", 9),
                textLineSpacing = 1.0,
                alignment = Alignment.LEFT
            )
        ),
        prologPage = PrologPageDesign(
            titleStyle = StyleData(
                font = FontData("Prolog Title Serif", 22),
                textLineSpacing = 1.3,
                alignment = Alignment.LEFT
            ),
            titleAppendixStyle = StyleData(
                font = FontData("Prolog Appendix Serif", 15),
                textLineSpacing = 1.2,
                alignment = Alignment.LEFT
            ),
            textStyle = StyleData(
                font = FontData("Prolog Text Serif", 11),
                textLineSpacing = 1.4,
                alignment = Alignment.BLOCK
            )
        ),
        blurbPage = BlurbPageDesign(
            textStyle = StyleData(
                font = FontData("Blurb Serif", 12),
                textLineSpacing = 1.4,
                alignment = Alignment.BLOCK
            )
        ),
        chapterPage = ChapterPageDesign(
            titleStyle = StyleData(
                font = FontData("Chapter Serif", 20, bold = true),
                textLineSpacing = 1.3,
                alignment = Alignment.LEFT
            ),
            titleAppendixStyle = StyleData(
                font = FontData("Chapter Appendix Serif", 14, italic = true),
                textLineSpacing = 1.2,
                alignment = Alignment.LEFT
            ),
            textStyle = StyleData(
                font = FontData("Chapter Text Serif", 11),
                textLineSpacing = 1.4,
                alignment = Alignment.BLOCK
            ),
            titleOnSeparatePage = true
        ),
        epilogPage = EpilogPageDesign(
            titleStyle = StyleData(
                font = FontData("Epilog Title Serif", 21),
                textLineSpacing = 1.3,
                alignment = Alignment.LEFT
            ),
            titleAppendixStyle = StyleData(
                font = FontData("Epilog Appendix Serif", 14),
                textLineSpacing = 1.2,
                alignment = Alignment.LEFT
            ),
            textStyle = StyleData(
                font = FontData("Epilog Text Serif", 11),
                textLineSpacing = 1.4,
                alignment = Alignment.BLOCK
            )
        ),
        startWithEmptyPage = true,
        endWithEmptyPage = false
    )

    /** A design differing from [newDesign] in every watched field of the tree. */
    private fun otherDesign(): Design = Design(
        pageFormat = PageFormat(
            width = 300.0,
            height = 500.0,
            innerMargin = 21.0,
            outerMargin = 14.0,
            topMargin = 16.0,
            bottomMargin = 19.0
        ),
        titlePage = TitlePageDesign(
            titleStyle = StyleData(
                font = FontData("Other Title", 29),
                textLineSpacing = 2.1,
                alignment = Alignment.LEFT
            ),
            titleAppendixStyle = StyleData(
                font = FontData("Other Title Appendix", 19),
                textLineSpacing = 2.1,
                alignment = Alignment.LEFT
            ),
            showAuthor = false,
            authorStyle = StyleData(
                font = FontData("Other Author", 17),
                textLineSpacing = 2.1,
                alignment = Alignment.LEFT
            )
        ),
        copyrightPage = CopyrightPageDesign(
            copyrightStyle = StyleData(
                font = FontData("Other Copyright", 10),
                textLineSpacing = 2.2,
                alignment = Alignment.RIGHT
            ),
            copyrightAppendixStyle = StyleData(
                font = FontData("Other Copyright Appendix", 9),
                textLineSpacing = 2.2,
                alignment = Alignment.RIGHT
            ),
            showAuthor = true,
            authorStyle = StyleData(
                font = FontData("Other Copyright Author", 10),
                textLineSpacing = 2.2,
                alignment = Alignment.RIGHT
            )
        ),
        prologPage = PrologPageDesign(
            titleStyle = StyleData(
                font = FontData("Other Prolog Title", 23),
                textLineSpacing = 2.3,
                alignment = Alignment.RIGHT
            ),
            titleAppendixStyle = StyleData(
                font = FontData("Other Prolog Appendix", 16),
                textLineSpacing = 2.3,
                alignment = Alignment.RIGHT
            ),
            textStyle = StyleData(
                font = FontData("Other Prolog Text", 12),
                textLineSpacing = 2.3,
                alignment = Alignment.RIGHT
            )
        ),
        blurbPage = BlurbPageDesign(
            textStyle = StyleData(
                font = FontData("Other Blurb", 15),
                textLineSpacing = 2.4,
                alignment = Alignment.LEFT
            )
        ),
        chapterPage = ChapterPageDesign(
            titleStyle = StyleData(
                font = FontData("Other Chapter", 24),
                textLineSpacing = 2.5,
                alignment = Alignment.RIGHT
            ),
            titleAppendixStyle = StyleData(
                font = FontData("Other Chapter Appendix", 17),
                textLineSpacing = 2.5,
                alignment = Alignment.RIGHT
            ),
            textStyle = StyleData(
                font = FontData("Other Chapter Text", 13),
                textLineSpacing = 2.5,
                alignment = Alignment.RIGHT
            ),
            titleOnSeparatePage = false
        ),
        epilogPage = EpilogPageDesign(
            titleStyle = StyleData(
                font = FontData("Other Epilog Title", 25),
                textLineSpacing = 2.6,
                alignment = Alignment.RIGHT
            ),
            titleAppendixStyle = StyleData(
                font = FontData("Other Epilog Appendix", 18),
                textLineSpacing = 2.6,
                alignment = Alignment.RIGHT
            ),
            textStyle = StyleData(
                font = FontData("Other Epilog Text", 14),
                textLineSpacing = 2.6,
                alignment = Alignment.RIGHT
            )
        ),
        startWithEmptyPage = false,
        endWithEmptyPage = true
    )

    /**
     * Use case: the design dialog is opened, so every page design and every page flag answers with
     * the value the wrapped object carries instead of a copy made at some earlier point.
     */
    @Test
    fun readsEveryPageDesignFromTheModelObject() {
        assertEquals("Title Serif", property.titlePageProperty.titleStyleProperty.fontProperty.nameProperty.get())
        assertTrue(property.titlePageProperty.showAuthorProperty.get())
        assertEquals(9, property.copyrightPageProperty.copyrightStyleProperty.fontProperty.sizeProperty.get())
        assertFalse(property.copyrightPageProperty.showAuthorProperty.get())
        assertEquals(Alignment.BLOCK, property.prologPageProperty.textStyleProperty.alignmentProperty.get())
        assertEquals(12, property.blurbPageProperty.textStyleProperty.fontProperty.sizeProperty.get())
        assertEquals("Chapter Serif", property.chapterPageProperty.titleStyleProperty.fontProperty.nameProperty.get())
        assertTrue(property.chapterPageProperty.titleOnSeparatePageProperty.get())
        assertEquals(
            "Epilog Title Serif",
            property.epilogPageProperty.titleStyleProperty.fontProperty.nameProperty.get()
        )
        assertTrue(property.startWithEmptyPageProperty.get())
        assertFalse(property.endWithEmptyPageProperty.get())
    }

    /**
     * Use case: the project settings dialog is opened, so the page geometry and the line spacings of
     * the styles answer with the values the wrapped design carries.
     */
    @Test
    fun readsThePageLayoutFromTheModelObject() {
        assertEquals(400.0, property.pageFormatProperty.widthProperty.get())
        assertEquals(25.0, property.pageFormatProperty.innerMarginProperty.get())
        assertEquals(22.0, property.pageFormatProperty.bottomMarginProperty.get())
        assertEquals(1.5, property.titlePageProperty.titleStyleProperty.textLineSpacingProperty.get())
        assertEquals(1.4, property.chapterPageProperty.textStyleProperty.textLineSpacingProperty.get())
        assertEquals(1.4, property.blurbPageProperty.textStyleProperty.textLineSpacingProperty.get())
    }

    /**
     * Use case: the user picks another size for the body text of a chapter, so the value reaches the
     * model object through every level of the tree and each of them reports the change up to the root.
     */
    @Test
    fun writingDeepInsideTheTreeReachesTheModelObject() {
        property.chapterPageProperty.textStyleProperty.fontProperty.sizeProperty.set(13)

        assertEquals(13, holder.design?.chapterPage?.textStyle?.font?.size)
        assertEquals(1, recorder.countOf("design.chapterPage"))
        assertEquals(1, recorder.countOf("design"))
        assertEquals(1, parentEvents)
    }

    /**
     * Use case: the user sets the lines of the chapter body text further apart, so the factor reaches
     * the style nested in the chapter design and every level above it reports the change.
     */
    @Test
    fun writingALineSpacingReachesTheModelObject() {
        property.chapterPageProperty.textStyleProperty.textLineSpacingProperty.set(1.8)

        assertEquals(1.8, holder.design?.chapterPage?.textStyle?.textLineSpacing)
        assertEquals(1, recorder.countOf("design.chapterPage.textStyle.textLineSpacing"))
        assertEquals(1, recorder.countOf("design.chapterPage"))
        assertEquals(1, recorder.countOf("design"))
        assertEquals(1, parentEvents)
    }

    /**
     * Use case: the user widens the margin at the spine, so the value reaches the page format nested
     * in the design and both levels report the change up to the root.
     */
    @Test
    fun writingAMarginReachesTheModelObject() {
        property.pageFormatProperty.innerMarginProperty.set(30.0)

        assertEquals(30.0, holder.design?.pageFormat?.innerMargin)
        assertEquals(1, recorder.countOf("design.pageFormat.innerMargin"))
        assertEquals(1, recorder.countOf("design.pageFormat"))
        assertEquals(1, recorder.countOf("design"))
        assertEquals(1, parentEvents)
    }

    /**
     * Use case: the user takes the author name off the title page, so the switch of that page design
     * reaches the model object and every level above it reports the change.
     */
    @Test
    fun writingASwitchOfAPageDesignReachesTheModelObject() {
        property.titlePageProperty.showAuthorProperty.set(false)

        assertFalse(holder.design?.titlePage?.showAuthor ?: true)
        assertEquals(1, recorder.countOf("design.titlePage.showAuthor"))
        assertEquals(1, recorder.countOf("design.titlePage"))
        assertEquals(1, recorder.countOf("design"))
        assertEquals(1, parentEvents)
    }

    /**
     * Use case: a whole page design is replaced - a preset was applied to the blurb - so the field
     * properties below it belong to another object afterwards and the change reaches the model object.
     */
    @Test
    fun writingAWholePageDesignReachesTheModelObject() {
        property.blurbPage = BlurbPageDesign(
            textStyle = StyleData(
                font = FontData("Replaced Blurb", 14),
                textLineSpacing = 1.9,
                alignment = Alignment.LEFT
            )
        )

        assertEquals("Replaced Blurb", holder.design?.blurbPage?.textStyle?.font?.name)
        assertEquals(14, property.blurbPageProperty.textStyleProperty.fontProperty.sizeProperty.get())
        assertEquals(1, recorder.countOf("design.blurbPage"))
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
     * Use case: a spinner of the design dialog is bound to the line spacing of the chapter headings,
     * so what the user dials in arrives in the model object through the binding.
     */
    @Test
    fun writingALineSpacingThroughABindingReachesTheModelObject() {
        val input = SimpleDoubleProperty(1.6)
        property.chapterPageProperty.titleStyleProperty.textLineSpacingProperty.bind(input)

        assertEquals(1.6, holder.design?.chapterPage?.titleStyle?.textLineSpacing)

        input.set(2.0)

        assertEquals(2.0, holder.design?.chapterPage?.titleStyle?.textLineSpacing)

        property.chapterPageProperty.titleStyleProperty.textLineSpacingProperty.unbind()
    }

    /**
     * Use case: something changes the model object directly - an imported design for instance - so
     * every property of the tree takes over the new values as soon as the design is refreshed.
     */
    @Test
    fun aChangeOnTheModelObjectBecomesVisible() {
        holder.design?.chapterPage?.textStyle?.font?.name = "Imported Serif"
        holder.design?.chapterPage?.titleOnSeparatePage = false
        holder.design?.pageFormat?.topMargin = 33.0
        holder.design?.blurbPage?.textStyle?.textLineSpacing = 1.9
        holder.design?.endWithEmptyPage = true

        property.refresh()

        assertEquals(
            "Imported Serif",
            property.chapterPageProperty.textStyleProperty.fontProperty.nameProperty.get()
        )
        assertFalse(property.chapterPageProperty.titleOnSeparatePageProperty.get())
        assertEquals(33.0, property.pageFormatProperty.topMarginProperty.get())
        assertEquals(1.9, property.blurbPageProperty.textStyleProperty.textLineSpacingProperty.get())
        assertTrue(property.endWithEmptyPageProperty.get())
    }

    /**
     * Use case: another project is opened, so the whole design is exchanged and every property of the
     * tree takes over the value of the new object and reports it up to the root.
     */
    @Test
    fun exchangingTheModelObjectUpdatesEveryPageDesign() {
        recorder.reset()
        parentEvents = 0

        property.set(otherDesign())

        assertEquals("Other Title", property.titlePageProperty.titleStyleProperty.fontProperty.nameProperty.get())
        assertFalse(property.titlePageProperty.showAuthorProperty.get())
        assertEquals(10, property.copyrightPageProperty.copyrightStyleProperty.fontProperty.sizeProperty.get())
        assertEquals(Alignment.RIGHT, property.prologPageProperty.textStyleProperty.alignmentProperty.get())
        assertEquals(15, property.blurbPageProperty.textStyleProperty.fontProperty.sizeProperty.get())
        assertEquals(2.5, property.chapterPageProperty.textStyleProperty.textLineSpacingProperty.get())
        assertFalse(property.chapterPageProperty.titleOnSeparatePageProperty.get())
        assertEquals(
            "Other Epilog Title",
            property.epilogPageProperty.titleStyleProperty.fontProperty.nameProperty.get()
        )
        assertEquals(300.0, property.pageFormatProperty.widthProperty.get())
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

        assertNull(property.pageFormatProperty.get())
        assertNull(property.titlePageProperty.get())
        assertNull(property.copyrightPageProperty.get())
        assertNull(property.prologPageProperty.get())
        assertNull(property.blurbPageProperty.get())
        assertNull(property.chapterPageProperty.get())
        assertNull(property.epilogPageProperty.get())
        assertFalse(property.startWithEmptyPageProperty.get())
        assertFalse(property.endWithEmptyPageProperty.get())

        property.startWithEmptyPageProperty.set(true)
        property.chapterPageProperty.textStyleProperty.textLineSpacingProperty.set(1.7)

        assertNull(holder.design)
    }
}

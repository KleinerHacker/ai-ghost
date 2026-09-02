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

package org.pcsoft.app.aighost.layouting

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/**
 * Developer tests for [LayoutEngine].
 *
 * Every part is a hand built [LaidOutText] whose lines sit ten points apart, so a content height of a
 * whole multiple of ten places a known, arithmetic number of lines per page.
 */
class LayoutEngineTest {

    private val style = TextStyle(family = "Test Family", size = 10.0)

    private fun line(y: Double) = LaidOutLine(
        x = 0.0, y = y, baseline = y + 8.0, width = 10.0, text = "line", style = style,
        blockIndex = 0, charStart = 0, charEnd = 4, wordSpacing = 0.0
    )

    /** A part of [lineCount] lines, ten points apart. */
    private fun text(lineCount: Int) = LaidOutText(
        lines = (0 until lineCount).map { line(it * 10.0) },
        columnWidth = 100.0,
        height = lineCount * 10.0
    )

    private fun geometry(mirroredMargins: Boolean = false, height: Double = 100.0) = PageGeometry(
        width = 200.0,
        height = height,
        innerMargin = 20.0,
        outerMargin = 10.0,
        topMargin = 0.0,
        bottomMargin = 0.0,
        mirroredMargins = mirroredMargins
    )

    /**
     * Use case: a part longer than one page is distributed onto as many pages as it needs, each
     * carrying a page number one higher than the one before.
     */
    @Test
    fun aLongPartIsDistributedOverConsecutivePages() {
        val layout = LayoutEngine.layout(text(25), geometry())

        assertEquals(listOf(0, 1, 2), layout.pages.map { it.position })
        assertEquals(listOf(1, 2, 3), layout.pages.map { it.pageNumber })
        assertEquals(listOf(11, 11, 3), layout.pages.map { it.lines.size })
    }

    /**
     * Use case: a switched off optional part is still laid out, so its greyed out pages exist, but
     * they are marked inactive instead of active like the rest of the book.
     */
    @Test
    fun anInactivePartIsMarkedAccordingly() {
        val layout = LayoutEngine.layout(text(3), geometry(), active = false)

        assertTrue(layout.pages.all { !it.active })
    }

    /**
     * Use case: the title page carries no page number at all, so a caller passes `null` as the start
     * number and every produced page stays unnumbered instead of counting from zero.
     */
    @Test
    fun anUnnumberedPartProducesNoPageNumber() {
        val layout = LayoutEngine.layout(text(3), geometry(), startPageNumber = null)

        assertTrue(layout.pages.all { it.pageNumber == null })
    }

    /**
     * Use case: without mirrored margins every page - odd or even - keeps the inner margin on the
     * same side, so a plain manuscript is not thrown off by an unrequested layout change.
     */
    @Test
    fun withoutMirroredMarginsEveryPageKeepsTheSameSide() {
        val recto = LayoutEngine.layout(text(1), geometry(mirroredMargins = false), startPosition = 0)
        val verso = LayoutEngine.layout(text(1), geometry(mirroredMargins = false), startPosition = 1)

        assertEquals(20.0, recto.pages.single().leftMargin)
        assertEquals(20.0, verso.pages.single().leftMargin)
    }

    /**
     * Use case: with mirrored margins on, the inner margin sits on the left of a recto page (an even
     * position) and on the right of a verso page (an odd position).
     */
    @Test
    fun mirroredMarginsAlternateByPhysicalPosition() {
        val recto = LayoutEngine.layout(text(1), geometry(mirroredMargins = true), startPosition = 0)
        val verso = LayoutEngine.layout(text(1), geometry(mirroredMargins = true), startPosition = 1)

        assertEquals(20.0, recto.pages.single().leftMargin)
        assertEquals(10.0, recto.pages.single().rightMargin)
        assertEquals(10.0, verso.pages.single().leftMargin)
        assertEquals(20.0, verso.pages.single().rightMargin)
    }

    /**
     * Use case: title page and copyright page are unnumbered parts of the book; the running page
     * number therefore only starts counting once the first numbered part - the prolog or the first
     * chapter - is reached.
     */
    @Test
    fun unnumberedPartsDoNotAdvanceTheRunningPageNumber() {
        val layout = LayoutEngine.layoutBook(
            listOf(
                LayoutEngine.PartInput(text(1), numbered = false),
                LayoutEngine.PartInput(text(1), numbered = false),
                LayoutEngine.PartInput(text(1), numbered = true)
            ),
            geometry()
        )

        assertEquals(listOf(null, null, 1), layout.pages.map { it.pageNumber })
    }

    /**
     * Use case: a switched off optional part still occupies its position among the physical pages, so
     * switching it back on later moves no other part's position - only the running page number shifts.
     */
    @Test
    fun aSwitchedOffPartKeepsItsPositionOutOfTheNumbering() {
        val layout = LayoutEngine.layoutBook(
            listOf(
                LayoutEngine.PartInput(text(1), active = false, numbered = false),
                LayoutEngine.PartInput(text(1), active = true, numbered = true)
            ),
            geometry()
        )

        assertEquals(listOf(0, 1), layout.pages.map { it.position })
        assertEquals(listOf(false, true), layout.pages.map { it.active })
        assertEquals(listOf(null, 1), layout.pages.map { it.pageNumber })
    }

    /**
     * Use case: two parts are always kept on separate pages, even when the first part's last page has
     * room left over - a chapter never continues on the same sheet as the one before it.
     */
    @Test
    fun everyPartStartsOnAPageOfItsOwn() {
        val layout = LayoutEngine.layoutBook(
            listOf(
                LayoutEngine.PartInput(text(1)),
                LayoutEngine.PartInput(text(1))
            ),
            geometry()
        )

        assertEquals(2, layout.pages.size)
        assertEquals(listOf(1, 1), layout.pages.map { it.lines.size })
    }

    /**
     * Use case: the project asks for a blank leaf before and after the book, so both appear as
     * unnumbered, active, empty pages without shifting any other page's number.
     */
    @Test
    fun blankPagesFrameTheBookWithoutCarryingANumber() {
        val layout = LayoutEngine.layoutBook(
            listOf(LayoutEngine.PartInput(text(1))),
            geometry(),
            startWithEmptyPage = true,
            endWithEmptyPage = true
        )

        assertEquals(3, layout.pages.size)
        assertNull(layout.pages.first().pageNumber)
        assertTrue(layout.pages.first().lines.isEmpty())
        assertEquals(1, layout.pages[1].pageNumber)
        assertNull(layout.pages.last().pageNumber)
        assertTrue(layout.pages.last().lines.isEmpty())
    }

    /**
     * Use case: a book with no part at all still respects the two blank page switches instead of
     * producing an empty layout.
     */
    @Test
    fun blankPagesAppearEvenForAnEmptyBook() {
        val layout = LayoutEngine.layoutBook(
            emptyList(),
            geometry(),
            startWithEmptyPage = true,
            endWithEmptyPage = true
        )

        assertEquals(2, layout.pages.size)
    }
}

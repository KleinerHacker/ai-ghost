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

/**
 * Distributes lines broken by a [LineBreaker] onto the pages of a [PageGeometry].
 *
 * The engine knows two shapes of input: [layout] paginates one already broken part on its own,
 * [layoutBook] stitches several parts into the continuous run of physical pages a whole book prints
 * as. Neither knows what a part is beyond [LaidOutText] plus the two switches every part carries in
 * the manuscript - whether it belongs to the book at all, and whether its pages are numbered - so the
 * engine stays free of the manuscript model, exactly like the rest of this module.
 */
object LayoutEngine {

    /**
     * Lays out one part's already broken lines onto pages of [geometry].
     *
     * @param text Lines broken for one part, by a [LineBreaker].
     * @param geometry Page geometry the lines are placed onto.
     * @param startPosition Physical position, 0-based, the first produced page takes. Only relevant to
     * pick the correct margin side when [PageGeometry.mirroredMargins] is on.
     * @param startPageNumber First page number handed out, or `null` if the produced pages carry none.
     * @param active Whether the produced pages belong to a part that is currently part of the book.
     * @param policy Decides where a page breaks; [NonePageBreakPolicy] by default.
     * @return The pages [text] was distributed onto, empty when [text] carries no line.
     */
    fun layout(
        text: LaidOutText,
        geometry: PageGeometry,
        startPosition: Int = 0,
        startPageNumber: Int? = 1,
        active: Boolean = true,
        policy: PageBreakPolicy = NonePageBreakPolicy
    ): DocumentLayout {
        val pageLines = policy.breakPages(text.lines, geometry.contentHeight)

        var pageNumber = startPageNumber
        val pages = pageLines.mapIndexed { index, lines ->
            val assignedNumber = pageNumber
            pageNumber = assignedNumber?.plus(1)
            page(startPosition + index, assignedNumber, active, lines, geometry)
        }

        return DocumentLayout(pages)
    }

    /**
     * Input of one part of the book, for [layoutBook].
     *
     * @property text Lines broken for this part, by a [LineBreaker].
     * @property active Whether the part currently belongs to the book - a switched off optional part
     * still occupies pages, it is only left out of the numbering by way of [numbered].
     * @property numbered Whether this part's pages count towards the running page number of the book.
     */
    data class PartInput(
        val text: LaidOutText,
        val active: Boolean = true,
        val numbered: Boolean = true
    )

    /**
     * Lays out every part of a book, in order, onto one continuous run of physical pages.
     *
     * A part always starts on a page of its own: the lines of one [PartInput] are never continued on
     * the same page as the previous part's, since every part was broken by the line breaker against
     * its own column. Page numbering runs across every [PartInput.numbered] part; a part that is not
     * numbered - the title page and the copyright page, for instance - keeps the running number
     * frozen while its own pages carry none.
     *
     * @param parts The parts of the book, in the order they are printed.
     * @param geometry Page geometry every page of the book shares.
     * @param startWithEmptyPage Whether an unnumbered blank page is inserted before the first part.
     * @param endWithEmptyPage Whether an unnumbered blank page is appended after the last part.
     * @param policy Decides where a page breaks; [NonePageBreakPolicy] by default.
     * @return The pages of the whole book, in printing order.
     */
    fun layoutBook(
        parts: List<PartInput>,
        geometry: PageGeometry,
        startWithEmptyPage: Boolean = false,
        endWithEmptyPage: Boolean = false,
        policy: PageBreakPolicy = NonePageBreakPolicy
    ): DocumentLayout {
        val pages = mutableListOf<Page>()
        var position = 0
        var pageNumber = 1

        if (startWithEmptyPage) {
            pages += page(position, null, active = true, lines = emptyList(), geometry = geometry)
            position++
        }

        for (part in parts) {
            val pageLines = policy.breakPages(part.text.lines, geometry.contentHeight)
            for (lines in pageLines) {
                val assignedNumber = if (part.numbered) pageNumber else null
                pages += page(position, assignedNumber, part.active, lines, geometry)
                if (part.numbered) {
                    pageNumber++
                }
                position++
            }
        }

        if (endWithEmptyPage) {
            pages += page(position, null, active = true, lines = emptyList(), geometry = geometry)
        }

        return DocumentLayout(pages)
    }

    /**
     * Builds one [Page] at [position], resolving [PageGeometry.mirroredMargins] into a concrete left
     * and right margin.
     *
     * Position `0` is the first physical page of the document and counts as a recto page - the
     * printer's convention this module follows throughout: a recto page carries the inner margin on
     * its left, a verso page on its right.
     */
    private fun page(
        position: Int,
        pageNumber: Int?,
        active: Boolean,
        lines: List<LaidOutLine>,
        geometry: PageGeometry
    ): Page {
        val recto = position % 2 == 0
        val leftMargin: Double
        val rightMargin: Double
        if (geometry.mirroredMargins && !recto) {
            leftMargin = geometry.outerMargin
            rightMargin = geometry.innerMargin
        } else {
            leftMargin = geometry.innerMargin
            rightMargin = geometry.outerMargin
        }

        return Page(
            position = position,
            pageNumber = pageNumber,
            active = active,
            lines = lines,
            leftMargin = leftMargin,
            rightMargin = rightMargin,
            topMargin = geometry.topMargin,
            bottomMargin = geometry.bottomMargin
        )
    }
}

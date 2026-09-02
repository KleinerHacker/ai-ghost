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
 * Decides where a run of lines breaks into pages.
 *
 * The hook exists although [NonePageBreakPolicy] is the only implementation shipped today: widow and
 * orphan avoidance changes where a page ends, and retrofitting the decision point later would reshape
 * every caller of [LayoutEngine] instead of adding an implementation here.
 */
interface PageBreakPolicy {

    /**
     * Splits [lines] into the lines of consecutive pages.
     *
     * @param lines Lines of one part, in the order they are set.
     * @param contentHeight Vertical space one page's text may occupy, in points.
     * @return The lines of each page, in printing order. Empty when [lines] is empty.
     */
    fun breakPages(lines: List<LaidOutLine>, contentHeight: Double): List<List<LaidOutLine>>
}

/**
 * The [PageBreakPolicy] with no widow or orphan avoidance: a page holds as many lines as fit under
 * [PageBreakPolicy.breakPages]'s `contentHeight`, decided line by line with no look-ahead.
 *
 * A line is placed on the current page as long as its own top edge still falls within the page's
 * content height, measured from the top edge of the first line placed on that page. The line's own
 * height below its top edge is not accounted for, which is the simplification this policy is named
 * for.
 */
object NonePageBreakPolicy : PageBreakPolicy {

    override fun breakPages(lines: List<LaidOutLine>, contentHeight: Double): List<List<LaidOutLine>> {
        if (lines.isEmpty()) {
            return emptyList()
        }

        val pages = mutableListOf<MutableList<LaidOutLine>>()
        var pageStartY = lines.first().y
        var currentPage = mutableListOf<LaidOutLine>()

        for (line in lines) {
            if (currentPage.isNotEmpty() && line.y - pageStartY > contentHeight) {
                pages += currentPage
                currentPage = mutableListOf()
                pageStartY = line.y
            }
            currentPage += line
        }
        pages += currentPage

        return pages
    }
}

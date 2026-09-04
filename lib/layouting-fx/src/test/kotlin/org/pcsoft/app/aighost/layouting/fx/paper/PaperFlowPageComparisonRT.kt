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

package org.pcsoft.app.aighost.layouting.fx.paper

import javafx.scene.Scene
import javafx.scene.canvas.Canvas
import javafx.scene.layout.HBox
import javafx.stage.Stage
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.pcsoft.app.aighost.layouting.DocumentLayout
import org.pcsoft.app.aighost.layouting.FixedTextMetrics
import org.pcsoft.app.aighost.layouting.GreedyLineBreaker
import org.pcsoft.app.aighost.layouting.LayoutEngine
import org.pcsoft.app.aighost.layouting.PageGeometry
import org.pcsoft.app.aighost.layouting.TextBlock
import org.pcsoft.app.aighost.layouting.TextStyle
import org.testfx.framework.junit5.ApplicationTest
import org.testfx.util.WaitForAsyncUtils

/**
 * Regression tests (IP-06) proving that [PaperFlowView] (the writing surface) and [PaperPageView]
 * (the read-only preview) never disagree on where a document breaks, since both are fed the exact
 * same [DocumentLayout] computed by [LayoutEngine].
 *
 * The two views are put on the same stage and given the same layout. [PaperPageView] renders one
 * [Canvas] per [org.pcsoft.app.aighost.layouting.Page], so its count of pages is read directly off
 * the scene graph. [PaperFlowView] renders one text control per block, with a `.paper-flow-view-gap`
 * region between every two adjacent blocks - whether or not a page break falls exactly there, per
 * `PaperFlowViewSkin.buildBlocks` - and, independently, a dashed `.paper-flow-view-break-mark` for
 * every page break that falls *inside* one block instead of at a boundary. Two counts are therefore
 * pinned against the layout: the number of gaps must always equal `blocks - 1`, and the number of
 * break marks must equal the number of page boundaries whose neighbouring lines share a `blockIndex`.
 */
class PaperFlowPageComparisonRT : ApplicationTest() {

    private lateinit var stage: Stage
    private lateinit var flowView: PaperFlowView
    private lateinit var pageView: PaperPageView

    override fun start(stage: Stage) {
        this.stage = stage
        flowView = PaperFlowView()
        pageView = PaperPageView()
        stage.scene = Scene(HBox(flowView, pageView), 800.0, 500.0)
        stage.show()
    }

    private val geometry = PageGeometry(
        width = 260.0,
        height = 260.0,
        innerMargin = 20.0,
        outerMargin = 20.0,
        topMargin = 20.0,
        bottomMargin = 20.0
    )

    private val breaker = GreedyLineBreaker(FixedTextMetrics())

    private fun columnWidth() = geometry.width - geometry.innerMargin - geometry.outerMargin

    /**
     * Builds a [DocumentLayout] out of several long blocks, long enough that the greedy breaker and
     * [LayoutEngine] together always spread it over more than one page.
     */
    private fun documentOf(vararg texts: String): DocumentLayout {
        val style = TextStyle(family = "Test Family", size = 12.0)
        val blocks = texts.map { TextBlock(it, style) }
        val laidOut = breaker.breakText(blocks, columnWidth())
        return LayoutEngine.layout(laidOut, geometry)
    }

    /** Number of distinct blocks the layout's lines were set from. */
    private fun blockCount(layout: DocumentLayout): Int =
        layout.pages.flatMap { it.lines }.map { it.blockIndex }.toSet().size

    /** Number of page boundaries whose neighbouring lines belong to the very same block. */
    private fun expectedBreakMarkCount(layout: DocumentLayout): Int =
        (0 until layout.pages.size - 1).count { index ->
            val lastLineOfPage = layout.pages[index].lines.last()
            val firstLineOfNextPage = layout.pages[index + 1].lines.first()
            lastLineOfPage.blockIndex == firstLineOfNextPage.blockIndex
        }

    private fun setDocument(layout: DocumentLayout) {
        interact {
            flowView.pageGeometry = geometry
            flowView.documentLayout = layout
            pageView.pageGeometry = geometry
            pageView.documentLayout = layout
        }
        interact { stage.scene.root.layout() }
        WaitForAsyncUtils.waitForFxEvents()
    }

    /**
     * A single block long enough to fill several pages produces exactly one break mark per page
     * boundary and no gap at all, since a lone block has no boundary to another block.
     */
    @Test
    fun `a single long block reports one break mark per page boundary and no gap`() {
        val text = "This single block repeats itself so it fills several pages of the column. ".repeat(30)
        val layout = documentOf(text)

        setDocument(layout)

        val pageCount = pageView.lookupAll(".paper-page-view-sheet").size
        assertEquals(layout.pages.size, pageCount, "PaperPageView must render exactly one canvas per page")

        val breakMarks = flowView.lookupAll(".paper-flow-view-break-mark").size
        val gaps = flowView.lookupAll(".paper-flow-view-gap").size

        assertEquals(
            expectedBreakMarkCount(layout), breakMarks,
            "Every page boundary landing inside the single block must draw a break mark"
        )
        assertEquals(0, gaps, "A single block has no boundary to another block, so no gap must be drawn")
    }

    /**
     * Several short blocks, long enough together to spread over multiple pages, produce exactly one
     * gap per block boundary - independent of where the pages break - and exactly one break mark per
     * page boundary that falls inside one block instead of between two.
     */
    @Test
    fun `several blocks report one gap per block boundary and one break mark per inner page break`() {
        val texts = (1 until 25).map { index ->
            "Paragraph number $index repeats a few words so it takes a visible amount of space on the page."
        }
        val layout = documentOf(*texts.toTypedArray())

        setDocument(layout)

        val pageCount = pageView.lookupAll(".paper-page-view-sheet").size
        assertEquals(layout.pages.size, pageCount, "PaperPageView must render exactly one canvas per page")

        val breakMarks = flowView.lookupAll(".paper-flow-view-break-mark").size
        val gaps = flowView.lookupAll(".paper-flow-view-gap").size

        assertEquals(
            blockCount(layout) - 1, gaps,
            "A gap must separate every two adjacent blocks, whether or not a page breaks there"
        )
        assertEquals(
            expectedBreakMarkCount(layout), breakMarks,
            "A break mark must appear exactly where a page break falls inside one block"
        )
    }
}

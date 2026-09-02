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
import javafx.scene.layout.StackPane
import javafx.stage.Stage
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.pcsoft.app.aighost.layouting.DocumentLayout
import org.pcsoft.app.aighost.layouting.LaidOutLine
import org.pcsoft.app.aighost.layouting.Page
import org.pcsoft.app.aighost.layouting.PageGeometry
import org.pcsoft.app.aighost.layouting.TextStyle
import org.testfx.framework.junit5.ApplicationTest

/**
 * Developer tests of [PaperPageView] and its skin: rendering, scrolling to a page or a block,
 * inactive pages and virtualization of a long document.
 */
class PaperPageViewTest : ApplicationTest() {

    private lateinit var stage: Stage
    private lateinit var view: PaperPageView

    override fun start(stage: Stage) {
        this.stage = stage
        view = PaperPageView()
        stage.scene = Scene(StackPane(view), 400.0, 500.0)
        stage.show()
    }

    private val geometry = PageGeometry(
        width = 200.0,
        height = 300.0,
        innerMargin = 20.0,
        outerMargin = 20.0,
        topMargin = 20.0,
        bottomMargin = 20.0
    )

    private fun style(): TextStyle = TextStyle(family = "Serif", size = 12.0)

    private fun page(position: Int, pageNumber: Int?, active: Boolean = true, blockIndex: Int = position) = Page(
        position = position,
        pageNumber = pageNumber,
        active = active,
        lines = listOf(
            LaidOutLine(
                x = 20.0,
                y = 20.0,
                baseline = 33.0,
                width = 160.0,
                text = "Page $position",
                style = style(),
                blockIndex = blockIndex,
                charStart = 0,
                charEnd = 6,
                wordSpacing = 0.0
            )
        ),
        leftMargin = 20.0,
        rightMargin = 20.0,
        topMargin = 20.0,
        bottomMargin = 20.0
    )

    private fun documentOf(count: Int): DocumentLayout =
        DocumentLayout((0 until count).map { page(it, it + 1) })

    private fun setDocument(layout: DocumentLayout) {
        interact {
            view.pageGeometry = geometry
            view.documentLayout = layout
        }
        interact { stage.scene.root.layout() }
    }

    /**
     * A view carrying a small document renders exactly one live [Canvas] per page, each of them
     * bearing the sheet style class.
     */
    @Test
    fun `small document renders one canvas per page`() {
        setDocument(documentOf(3))

        val canvases = view.lookupAll(".paper-page-view-sheet")

        assertEquals(3, canvases.size, "Every one of the three pages must have its own canvas")
        assertTrue(canvases.all { it is Canvas }, "Every found node must be a canvas")
    }

    /**
     * A page whose [Page.active] is `false` carries the inactive style class on its canvas, while an
     * active neighbour does not.
     */
    @Test
    fun `inactive page carries the inactive style class`() {
        val layout = DocumentLayout(listOf(page(0, 1, active = true), page(1, null, active = false)))
        setDocument(layout)

        val inactiveCanvases = view.lookupAll(".paper-page-view-page-inactive")

        assertEquals(1, inactiveCanvases.size, "Exactly the inactive page must carry the style class")
    }

    /**
     * [PaperPageView.scrollToPage] on a long document moves the scroll position away from the top,
     * proving the requested page was located inside the virtual document.
     */
    @Test
    fun `scrollToPage moves the viewport`() {
        setDocument(documentOf(50))

        interact { view.scrollToPage(40) }
        interact { stage.scene.root.layout() }

        val scrollPane = view.lookup(".paper-page-view-scroll-pane") as javafx.scene.control.ScrollPane
        assertTrue(scrollPane.vvalue > 0.0, "Scrolling to a late page must move the viewport down")
    }

    /**
     * [PaperPageView.scrollToBlock] finds the page carrying the requested block index and scrolls to
     * it, the same way [PaperPageView.scrollToPage] does for a page position.
     */
    @Test
    fun `scrollToBlock moves the viewport to the owning page`() {
        setDocument(documentOf(50))

        interact { view.scrollToBlock(45) }
        interact { stage.scene.root.layout() }

        val scrollPane = view.lookup(".paper-page-view-scroll-pane") as javafx.scene.control.ScrollPane
        assertTrue(scrollPane.vvalue > 0.0, "Scrolling to a late block must move the viewport down")
    }

    /**
     * On a long document only the pages intersecting the viewport - plus the small buffer - keep a
     * live [Canvas]; the vast majority of a thousand-page document must never reach the scene graph.
     */
    @Test
    fun `long document keeps only visible pages in the scene graph`() {
        setDocument(documentOf(1000))

        val canvases = view.lookupAll(".paper-page-view-sheet")

        assertTrue(canvases.isNotEmpty(), "At least the pages inside the viewport must be rendered")
        assertTrue(canvases.size < 50, "A thousand-page document must not keep most pages in the scene graph")
    }

    /**
     * Scrolling a long document to a late page tears down the canvases that are no longer near the
     * viewport and builds up the ones that now are.
     */
    @Test
    fun `scrolling a long document swaps out the visible canvases`() {
        setDocument(documentOf(1000))
        val firstPageCanvasCountBefore = view.lookupAll(".paper-page-view-sheet").size
        assertFalse(firstPageCanvasCountBefore == 0, "The initial viewport must render some pages")

        interact { view.scrollToPage(900) }
        interact { stage.scene.root.layout() }

        val scrollPane = view.lookup(".paper-page-view-scroll-pane") as javafx.scene.control.ScrollPane
        assertTrue(scrollPane.vvalue > 0.5, "The viewport must have moved deep into the document")
        val canvasesAfter = view.lookupAll(".paper-page-view-sheet")
        assertTrue(canvasesAfter.isNotEmpty(), "The new viewport position must render its own pages")
    }
}

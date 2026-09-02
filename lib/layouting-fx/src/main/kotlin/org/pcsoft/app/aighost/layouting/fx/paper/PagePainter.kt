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

import javafx.geometry.VPos
import javafx.scene.canvas.GraphicsContext
import javafx.scene.paint.Color
import javafx.scene.text.Font
import javafx.scene.text.FontWeight
import javafx.scene.text.TextAlignment
import org.pcsoft.app.aighost.layouting.LaidOutLine
import org.pcsoft.app.aighost.layouting.Page
import org.pcsoft.app.aighost.layouting.PageGeometry
import org.pcsoft.app.aighost.layouting.fx.font.FontDescription
import org.pcsoft.app.aighost.layouting.fx.font.FontResolver
import org.pcsoft.app.aighost.layouting.fx.font.JavaFxTextMetrics

/**
 * Draws one [Page] onto a [GraphicsContext].
 *
 * The routine is kept behind this small interface so [PaperPageViewSkin] never has to know how a
 * page is actually painted, and a caller of the library may supply its own implementation - a
 * printer preview with a different chrome, for instance - without touching the skin.
 *
 * A painter reads every state it draws from [Page] itself; it never derives a state that is not
 * already there, with the single exception of the hard edge between two neighbouring pages of
 * differing [Page.active], which by nature only exists in the comparison of two pages and is passed
 * in explicitly instead of being invented.
 *
 * **Threading:** [paint] draws onto a live [GraphicsContext] of a [javafx.scene.canvas.Canvas], so it
 * must run on the JavaFX application thread.
 */
interface PagePainter {

    /**
     * Paints [page] onto [graphicsContext], which is already cleared and sized to exactly one sheet
     * at [zoom].
     *
     * @param graphicsContext Target to draw onto, sized `geometry.width * zoom` by
     *   `geometry.height * zoom`.
     * @param page Page to draw.
     * @param geometry Geometry [page] was laid out with.
     * @param zoom Scale applied to every coordinate of [page] before it is drawn.
     * @param previousActive [Page.active] of the page painted immediately before this one in the
     *   [org.pcsoft.app.aighost.layouting.DocumentLayout], or `null` when this is the first page.
     * @param nextActive [Page.active] of the page painted immediately after this one, or `null` when
     *   this is the last page.
     */
    fun paint(
        graphicsContext: GraphicsContext,
        page: Page,
        geometry: PageGeometry,
        zoom: Double,
        previousActive: Boolean?,
        nextActive: Boolean?
    )
}

/**
 * The neutral default [PagePainter] shipped with [PaperPageView].
 *
 * Every colour used here is a shade of grey or white: the library carries no palette of its own, an
 * application overrides the look through its own [PagePainter] instead of a stylesheet, since the
 * drawing happens on a [javafx.scene.canvas.Canvas] and cannot be reached by CSS selectors.
 */
object DefaultPagePainter : PagePainter {

    private val SHEET_COLOR: Color = Color.WHITE
    private val SHEET_BORDER_COLOR: Color = Color.gray(0.75)
    private val INACTIVE_OVERLAY_COLOR: Color = Color.gray(0.5, 0.45)
    private val HARD_EDGE_COLOR: Color = Color.gray(0.2)
    private val TEXT_COLOR: Color = Color.gray(0.1)
    private val PAGE_NUMBER_COLOR: Color = Color.gray(0.4)
    private const val HARD_EDGE_WIDTH = 2.0
    private const val PAGE_NUMBER_FONT_SIZE = 9.0

    override fun paint(
        graphicsContext: GraphicsContext,
        page: Page,
        geometry: PageGeometry,
        zoom: Double,
        previousActive: Boolean?,
        nextActive: Boolean?
    ) {
        val width = geometry.width * zoom
        val height = geometry.height * zoom

        graphicsContext.clearRect(0.0, 0.0, width, height)
        graphicsContext.fill = SHEET_COLOR
        graphicsContext.fillRect(0.0, 0.0, width, height)
        graphicsContext.stroke = SHEET_BORDER_COLOR
        graphicsContext.lineWidth = 1.0
        graphicsContext.strokeRect(0.5, 0.5, width - 1.0, height - 1.0)

        for (line in page.lines) {
            drawLine(graphicsContext, line, zoom)
        }

        if (!page.active) {
            graphicsContext.fill = INACTIVE_OVERLAY_COLOR
            graphicsContext.fillRect(0.0, 0.0, width, height)
        }

        if (previousActive != null && previousActive != page.active) {
            graphicsContext.stroke = HARD_EDGE_COLOR
            graphicsContext.lineWidth = HARD_EDGE_WIDTH
            graphicsContext.strokeLine(0.0, HARD_EDGE_WIDTH / 2.0, width, HARD_EDGE_WIDTH / 2.0)
        }
        if (nextActive != null && nextActive != page.active) {
            graphicsContext.stroke = HARD_EDGE_COLOR
            graphicsContext.lineWidth = HARD_EDGE_WIDTH
            graphicsContext.strokeLine(0.0, height - HARD_EDGE_WIDTH / 2.0, width, height - HARD_EDGE_WIDTH / 2.0)
        }

        page.pageNumber?.let { number ->
            drawPageNumber(graphicsContext, number, width, height)
        }
    }

    private fun drawLine(graphicsContext: GraphicsContext, line: LaidOutLine, zoom: Double) {
        val description = FontDescription(line.style.family, line.style.size.toInt(), line.style.bold, line.style.italic)
        val font = FontResolver.font(description)
        graphicsContext.font = Font.font(font.family, font.size * zoom)
        graphicsContext.fill = TEXT_COLOR
        graphicsContext.textAlign = TextAlignment.LEFT
        graphicsContext.textBaseline = VPos.BASELINE

        val x = line.x * zoom
        val baseline = line.baseline * zoom

        if (line.wordSpacing > 0.0) {
            // A justified line is not one string: each word is placed by hand, separated by the
            // stored word spacing instead of the plain space of the font.
            var cursor = x
            val gap = line.wordSpacing * zoom
            for (word in line.text.split(" ")) {
                if (word.isEmpty()) {
                    cursor += gap
                    continue
                }
                graphicsContext.fillText(word, cursor, baseline)
                cursor += JavaFxTextMetrics.wordWidth(description, word) * zoom + gap
            }
        } else {
            graphicsContext.fillText(line.text, x, baseline)
        }
    }

    private fun drawPageNumber(graphicsContext: GraphicsContext, number: Int, width: Double, height: Double) {
        graphicsContext.font = Font.font("System", FontWeight.NORMAL, PAGE_NUMBER_FONT_SIZE)
        graphicsContext.fill = PAGE_NUMBER_COLOR
        graphicsContext.textAlign = TextAlignment.CENTER
        graphicsContext.textBaseline = VPos.BASELINE
        graphicsContext.fillText(number.toString(), width / 2.0, height - PAGE_NUMBER_FONT_SIZE)
    }
}

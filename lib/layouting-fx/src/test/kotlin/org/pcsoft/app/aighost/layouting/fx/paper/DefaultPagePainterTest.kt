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
import javafx.scene.image.WritableImage
import javafx.scene.layout.StackPane
import javafx.scene.paint.Color
import javafx.stage.Stage
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.pcsoft.app.aighost.layouting.LaidOutLine
import org.pcsoft.app.aighost.layouting.Page
import org.pcsoft.app.aighost.layouting.PageGeometry
import org.pcsoft.app.aighost.layouting.TextStyle
import org.testfx.framework.junit5.ApplicationTest

/**
 * Developer tests of [DefaultPagePainter], proving what it puts onto a [Canvas] pixel by pixel.
 */
class DefaultPagePainterTest : ApplicationTest() {

    private lateinit var root: StackPane

    override fun start(stage: Stage) {
        root = StackPane()
        stage.scene = Scene(root, 50.0, 50.0)
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

    private fun basePage(pageNumber: Int?, active: Boolean = true, lines: List<LaidOutLine> = emptyList()) = Page(
        position = 0,
        pageNumber = pageNumber,
        active = active,
        lines = lines,
        leftMargin = 20.0,
        rightMargin = 20.0,
        topMargin = 20.0,
        bottomMargin = 20.0
    )

    private fun snapshotOf(page: Page): WritableImage {
        val canvas = Canvas(geometry.width, geometry.height)
        DefaultPagePainter.paint(canvas.graphicsContext2D, page, geometry, 1.0, null, null)
        val image = WritableImage(geometry.width.toInt(), geometry.height.toInt())
        interact { canvas.snapshot(null, image) }
        return image
    }

    private fun hasInk(image: WritableImage, xRange: IntRange, yRange: IntRange): Boolean {
        for (x in xRange) {
            for (y in yRange) {
                if (image.pixelReader.getColor(x, y) != Color.WHITE) return true
            }
        }
        return false
    }

    /**
     * A page whose [Page.pageNumber] is `null` must not have anything drawn into the number area -
     * the plan requires the number to be skipped, not drawn as an empty string.
     */
    @Test
    fun `page without number draws nothing in the number area`() {
        val image = snapshotOf(basePage(pageNumber = null))

        val numberArea = hasInk(image, 80..120, (geometry.height.toInt() - 16)..(geometry.height.toInt() - 4))

        assertFalse(numberArea, "No ink is expected where the page number would be drawn")
    }

    /**
     * A page with a [Page.pageNumber] must have visible ink in the number area at the bottom of the
     * sheet.
     */
    @Test
    fun `page with number draws ink in the number area`() {
        val image = snapshotOf(basePage(pageNumber = 7))

        val numberArea = hasInk(image, 80..120, (geometry.height.toInt() - 16)..(geometry.height.toInt() - 4))

        assertTrue(numberArea, "Ink is expected where the page number is drawn")
    }

    /**
     * An inactive page must be visibly greyed out: a pixel deep inside the sheet, far from any text
     * or border, must no longer be pure white once the inactive overlay is painted.
     */
    @Test
    fun `inactive page is grayed out`() {
        val image = snapshotOf(basePage(pageNumber = null, active = false))

        val centerColor = image.pixelReader.getColor(geometry.width.toInt() / 2, geometry.height.toInt() / 2)

        assertFalse(centerColor == Color.WHITE, "An inactive page must not stay pure white")
    }

    /**
     * An active page with no lines and no page number must stay pure white deep inside the sheet -
     * the overlay of an inactive page must not appear here.
     */
    @Test
    fun `active page keeps a white sheet`() {
        val image = snapshotOf(basePage(pageNumber = null, active = true))

        val centerColor = image.pixelReader.getColor(geometry.width.toInt() / 2, geometry.height.toInt() / 2)

        assertEquals(Color.WHITE, centerColor, "An active, empty page stays pure white")
    }

    /**
     * A justified line, recognised by a non-zero [LaidOutLine.wordSpacing], must be drawn word by
     * word: with a very large word spacing, ink must appear both close to the left margin and far to
     * the right of it, which a single un-spread `fillText` call could never produce.
     */
    @Test
    fun `justified line is spread across the column by word spacing`() {
        val style = TextStyle(family = "Serif", size = 14.0)
        val line = LaidOutLine(
            x = 20.0,
            y = 30.0,
            baseline = 44.0,
            width = 160.0,
            text = "Hi you",
            style = style,
            blockIndex = 0,
            charStart = 0,
            charEnd = 6,
            wordSpacing = 120.0
        )
        val image = snapshotOf(basePage(pageNumber = null, lines = listOf(line)))

        val nearLeft = hasInk(image, 15..40, 20..44)
        val farRight = hasInk(image, 150..190, 20..44)

        assertTrue(nearLeft, "The first word must be drawn near the left margin")
        assertTrue(farRight, "The large word spacing must push the second word far to the right")
    }
}

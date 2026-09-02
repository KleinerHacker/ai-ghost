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

import javafx.scene.canvas.Canvas
import javafx.scene.control.ScrollPane
import javafx.scene.control.SkinBase
import javafx.scene.layout.Pane
import org.pcsoft.app.aighost.layouting.DocumentLayout
import org.pcsoft.app.aighost.layouting.Page
import org.pcsoft.app.aighost.layouting.PageGeometry

/**
 * The [javafx.scene.control.Skin] of [PaperPageView].
 *
 * The skin owns everything the control itself must not know about: a [ScrollPane] carrying a
 * [Pane] sized to the full virtual height of the document, and one [Canvas] per page that is
 * currently close enough to the viewport to matter. Pages far outside the viewport keep no node at
 * all - [updateVisiblePages] adds and removes canvases as the user scrolls, zooms or resizes the
 * view, so a document of a thousand pages costs no more scene graph than a handful of them.
 *
 * No global registration is made anywhere in this class: every listener here observes either a
 * property of [PaperPageView] itself or a property of a node this skin owns, so nothing needs the
 * on-screen guard of `showingBinding()` - there is nothing outside this object's own graph to leak.
 *
 * **Threading:** like every JavaFX skin, every member of this class must be used on the JavaFX
 * application thread only.
 *
 * @constructor Builds the skin for [control]; called by [PaperPageView.createDefaultSkin] only.
 */
class PaperPageViewSkin(control: PaperPageView) : SkinBase<PaperPageView>(control) {

    private companion object {
        /** Empty space between two neighbouring sheets, and around the whole stack, in pixels. */
        const val PAGE_GAP: Double = 24.0

        /** Extra pixels kept live above and below the viewport, so a small scroll needs no repaint. */
        const val VIRTUALIZATION_BUFFER: Double = 400.0
    }

    private val scrollPane = ScrollPane()
    private val pageContainer = Pane()
    private val canvasesByPosition: MutableMap<Int, Canvas> = HashMap()

    /** The painter every page is currently drawn with. Replaceable for a caller with its own chrome. */
    var painter: PagePainter = DefaultPagePainter
        set(value) {
            field = value
            canvasesByPosition.clear()
            pageContainer.children.clear()
            updateVisiblePages()
        }

    init {
        pageContainer.styleClass.add("paper-page-view-container")
        scrollPane.styleClass.add("paper-page-view-scroll-pane")
        scrollPane.content = pageContainer
        scrollPane.isFitToWidth = false
        scrollPane.isFitToHeight = false
        children.add(scrollPane)

        control.documentLayoutProperty().addListener { _, _, _ -> rebuild() }
        control.pageGeometryProperty().addListener { _, _, _ -> rebuild() }
        control.zoomProperty().addListener { _, _, _ -> rebuild() }
        scrollPane.vvalueProperty().addListener { _, _, _ -> updateVisiblePages() }
        scrollPane.viewportBoundsProperty().addListener { _, _, _ -> updateVisiblePages() }

        rebuild()
    }

    /** Recomputes the height and width of the virtual document and refreshes what is on screen. */
    private fun rebuild() {
        canvasesByPosition.clear()
        pageContainer.children.clear()

        val geometry = skinnable.pageGeometry
        val layout = skinnable.documentLayout
        if (geometry == null || layout == null || layout.pages.isEmpty()) {
            pageContainer.prefWidth = 0.0
            pageContainer.prefHeight = 0.0
            return
        }

        val zoom = skinnable.zoom
        pageContainer.prefWidth = geometry.width * zoom + 2 * PAGE_GAP
        pageContainer.prefHeight = layout.pages.size * pageStep(geometry, zoom) + PAGE_GAP

        updateVisiblePages()
    }

    /** Vertical space one page occupies in the virtual document, its own height plus the gap below it. */
    private fun pageStep(geometry: PageGeometry, zoom: Double): Double =
        geometry.height * zoom + PAGE_GAP

    /** Top edge of the page at [position] inside the virtual document. */
    private fun pageTop(position: Int, geometry: PageGeometry, zoom: Double): Double =
        PAGE_GAP + position * pageStep(geometry, zoom)

    /**
     * Adds a [Canvas] for every page that intersects the viewport plus [VIRTUALIZATION_BUFFER], and
     * removes every canvas that no longer does.
     */
    private fun updateVisiblePages() {
        val geometry = skinnable.pageGeometry ?: return
        val layout = skinnable.documentLayout ?: return
        if (layout.pages.isEmpty()) return

        val zoom = skinnable.zoom
        val step = pageStep(geometry, zoom)
        val viewportHeight = scrollPane.viewportBounds.height
        val totalHeight = pageContainer.prefHeight(-1.0)
        val maxScroll = (totalHeight - viewportHeight).coerceAtLeast(0.0)
        val scrollY = scrollPane.vvalue * maxScroll

        val firstVisible = (((scrollY - VIRTUALIZATION_BUFFER) / step).toInt()).coerceAtLeast(0)
        val lastVisible = (((scrollY + viewportHeight + VIRTUALIZATION_BUFFER) / step).toInt())
            .coerceAtMost(layout.pages.size - 1)

        if (firstVisible > lastVisible) {
            canvasesByPosition.values.forEach { pageContainer.children.remove(it) }
            canvasesByPosition.clear()
            return
        }
        val wanted = firstVisible..lastVisible

        val toRemove = canvasesByPosition.keys.filter { it !in wanted }
        for (position in toRemove) {
            canvasesByPosition.remove(position)?.let { pageContainer.children.remove(it) }
        }

        for (position in wanted) {
            if (position !in canvasesByPosition) {
                val canvas = buildCanvas(layout, layout.pages[position], geometry, zoom)
                canvas.layoutX = PAGE_GAP
                canvas.layoutY = pageTop(position, geometry, zoom)
                canvasesByPosition[position] = canvas
                pageContainer.children.add(canvas)
            }
        }
    }

    private fun buildCanvas(layout: DocumentLayout, page: Page, geometry: PageGeometry, zoom: Double): Canvas {
        val canvas = Canvas(geometry.width * zoom, geometry.height * zoom)
        canvas.styleClass.add("paper-page-view-sheet")
        if (!page.active) {
            canvas.styleClass.add("paper-page-view-page-inactive")
        }

        val previousActive = layout.pages.getOrNull(page.position - 1)?.active
        val nextActive = layout.pages.getOrNull(page.position + 1)?.active
        painter.paint(canvas.graphicsContext2D, page, geometry, zoom, previousActive, nextActive)
        return canvas
    }

    /**
     * Sets the zoom of [PaperPageView] so a page fills the current width of the viewport.
     *
     * A no-op before the viewport was measured at least once.
     */
    fun fitToWidth() {
        val geometry = skinnable.pageGeometry ?: return
        val viewportWidth = scrollPane.viewportBounds.width - 2 * PAGE_GAP
        if (viewportWidth <= 0.0 || geometry.width <= 0.0) return
        skinnable.zoom = viewportWidth / geometry.width
    }

    /** @see PaperPageView.scrollToPage */
    fun scrollToPage(position: Int) {
        val geometry = skinnable.pageGeometry ?: return
        val layout = skinnable.documentLayout ?: return
        if (position !in layout.pages.indices) return

        scrollToOffset(pageTop(position, geometry, skinnable.zoom))
    }

    /** @see PaperPageView.scrollToBlock */
    fun scrollToBlock(blockIndex: Int) {
        val geometry = skinnable.pageGeometry ?: return
        val layout = skinnable.documentLayout ?: return

        for (page in layout.pages) {
            val line = page.lines.firstOrNull { it.blockIndex == blockIndex } ?: continue
            scrollToOffset(pageTop(page.position, geometry, skinnable.zoom) + line.y * skinnable.zoom)
            return
        }
    }

    private fun scrollToOffset(offsetY: Double) {
        val viewportHeight = scrollPane.viewportBounds.height
        val totalHeight = pageContainer.prefHeight(-1.0)
        val maxScroll = (totalHeight - viewportHeight).coerceAtLeast(0.0)
        scrollPane.vvalue = if (maxScroll <= 0.0) 0.0 else (offsetY / maxScroll).coerceIn(0.0, 1.0)
        updateVisiblePages()
    }

    /**
     * Number of [Canvas] nodes currently held by the viewport, exposed for tests proving
     * virtualization.
     */
    val visiblePageCount: Int
        get() = canvasesByPosition.size
}

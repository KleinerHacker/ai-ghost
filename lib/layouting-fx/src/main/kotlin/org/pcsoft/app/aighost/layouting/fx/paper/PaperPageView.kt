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

import javafx.beans.property.DoubleProperty
import javafx.beans.property.ObjectProperty
import javafx.beans.property.SimpleDoubleProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.scene.control.Control
import javafx.scene.control.Skin
import org.pcsoft.app.aighost.layouting.DocumentLayout
import org.pcsoft.app.aighost.layouting.PageGeometry

/**
 * A read-only, scrollable and zoomable renderer of a [DocumentLayout].
 *
 * The control holds no data of its own: it takes a finished [DocumentLayout] and a [PageGeometry]
 * through a property each and paints exactly what they describe. Nothing shown here can be edited -
 * there is no caret, no selection, no input handler - the control is a print preview, not an editor.
 * Neither this class nor its [Skin] ever refers to a type of `ai-ghost-model`: the library that
 * renders a book does not have to know what a book is made of, only how a page of it was laid out.
 *
 * The actual drawing, scrolling and virtualization live in [PaperPageViewSkin]; this class only
 * carries the public properties and the public operations a caller drives the view through.
 *
 * **Threading:** like every JavaFX [Control], every member of this class must be used on the JavaFX
 * application thread only.
 */
class PaperPageView : Control() {

    /** Style class carried by every instance, for a stylesheet to hook a default appearance onto. */
    companion object {
        private const val DEFAULT_STYLE_CLASS = "paper-page-view"

        /** Location of the neutral default stylesheet shipped with this control. */
        private const val DEFAULT_STYLESHEET =
            "/org/pcsoft/app/aighost/layouting/fx/paper/paper-page-view.css"
    }

    init {
        styleClass.add(DEFAULT_STYLE_CLASS)
    }

    private val documentLayoutProperty: ObjectProperty<DocumentLayout?> =
        SimpleObjectProperty(this, "documentLayout", null)

    /** The document currently shown, or `null` while nothing was handed to the view yet. */
    fun documentLayoutProperty(): ObjectProperty<DocumentLayout?> = documentLayoutProperty

    /** @see documentLayoutProperty */
    var documentLayout: DocumentLayout?
        get() = documentLayoutProperty.get()
        set(value) = documentLayoutProperty.set(value)

    private val pageGeometryProperty: ObjectProperty<PageGeometry?> =
        SimpleObjectProperty(this, "pageGeometry", null)

    /** Geometry every page of [documentLayoutProperty] is painted with. */
    fun pageGeometryProperty(): ObjectProperty<PageGeometry?> = pageGeometryProperty

    /** @see pageGeometryProperty */
    var pageGeometry: PageGeometry?
        get() = pageGeometryProperty.get()
        set(value) = pageGeometryProperty.set(value)

    private val zoomProperty: DoubleProperty = SimpleDoubleProperty(this, "zoom", 1.0)

    /** Scale applied to every page, `1.0` painting a page at its true size in points-as-pixels. */
    fun zoomProperty(): DoubleProperty = zoomProperty

    /** @see zoomProperty */
    var zoom: Double
        get() = zoomProperty.get()
        set(value) = zoomProperty.set(value)

    /**
     * Sets [zoomProperty] so the current page width exactly fills the viewport.
     *
     * Has no effect before the control was laid out at least once, since the width of the viewport is
     * not known before then.
     */
    fun fitToWidth() {
        (skin as? PaperPageViewSkin)?.fitToWidth()
    }

    /**
     * Scrolls the view so the page at [position] becomes visible.
     *
     * @param position 0-based [org.pcsoft.app.aighost.layouting.Page.position] of the page to jump to.
     */
    fun scrollToPage(position: Int) {
        (skin as? PaperPageViewSkin)?.scrollToPage(position)
    }

    /**
     * Scrolls the view so the first line of [blockIndex] becomes visible.
     *
     * @param blockIndex Index of the block, matching
     *   [org.pcsoft.app.aighost.layouting.LaidOutLine.blockIndex].
     */
    fun scrollToBlock(blockIndex: Int) {
        (skin as? PaperPageViewSkin)?.scrollToBlock(blockIndex)
    }

    override fun createDefaultSkin(): Skin<*> = PaperPageViewSkin(this)

    override fun getUserAgentStylesheet(): String =
        PaperPageView::class.java.getResource(DEFAULT_STYLESHEET)?.toExternalForm()
            ?: super.getUserAgentStylesheet()
}

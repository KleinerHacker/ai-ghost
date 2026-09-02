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

import javafx.beans.property.ObjectProperty
import javafx.beans.property.ReadOnlyDoubleProperty
import javafx.beans.property.ReadOnlyDoubleWrapper
import javafx.beans.property.SimpleObjectProperty
import javafx.scene.control.Control
import javafx.scene.control.Skin
import org.pcsoft.app.aighost.layouting.DocumentLayout
import org.pcsoft.app.aighost.layouting.PageGeometry

/**
 * An editable, scrollable writing surface rendering a [DocumentLayout] as native text controls.
 *
 * Unlike [PaperPageView], which only paints a finished layout, this control lets the person at the
 * keyboard type into it: it places one native text control per block, sized to the same column width
 * [org.pcsoft.app.aighost.layouting.LayoutEngine] measured against, and marks every page break the
 * layout carries - a real gap in the sheets where a break falls between two blocks, a dashed overlay
 * with the page number where a break falls inside one.
 *
 * The control owns the caret, the selection and the focus of every block, and nothing else: it never
 * applies a change to [documentLayoutProperty] on its own and never keeps a copy of the document
 * behind the caller's back. Every keystroke, cursor move, focus change and every request to split,
 * merge or remove a block is only ever reported to a [PaperFlowListener] - applying it, updating the
 * bound model and recomputing the layout is entirely up to the caller, which then hands a fresh
 * [DocumentLayout] back in.
 *
 * Neither this class nor its [Skin] ever refers to a type of `ai-ghost-model`, for the same reason as
 * [PaperPageView]: the library that lets someone write a book does not have to know what a book is
 * made of.
 *
 * **Threading:** like every JavaFX [Control], every member of this class must be used on the JavaFX
 * application thread only.
 */
class PaperFlowView : Control() {

    companion object {
        private const val DEFAULT_STYLE_CLASS = "paper-flow-view"

        /** Location of the neutral default stylesheet shipped with this control. */
        private const val DEFAULT_STYLESHEET =
            "/org/pcsoft/app/aighost/layouting/fx/paper/paper-flow-view.css"
    }

    init {
        styleClass.add(DEFAULT_STYLE_CLASS)
    }

    private val documentLayoutProperty: ObjectProperty<DocumentLayout?> =
        SimpleObjectProperty(this, "documentLayout", null)

    /** The document currently edited, or `null` while nothing was handed to the view yet. */
    fun documentLayoutProperty(): ObjectProperty<DocumentLayout?> = documentLayoutProperty

    /** @see documentLayoutProperty */
    var documentLayout: DocumentLayout?
        get() = documentLayoutProperty.get()
        set(value) = documentLayoutProperty.set(value)

    private val pageGeometryProperty: ObjectProperty<PageGeometry?> =
        SimpleObjectProperty(this, "pageGeometry", null)

    /** Geometry every block of [documentLayoutProperty] is measured and framed with. */
    fun pageGeometryProperty(): ObjectProperty<PageGeometry?> = pageGeometryProperty

    /** @see pageGeometryProperty */
    var pageGeometry: PageGeometry?
        get() = pageGeometryProperty.get()
        set(value) = pageGeometryProperty.set(value)

    private val columnWidthProperty: ReadOnlyDoubleWrapper = ReadOnlyDoubleWrapper(this, "columnWidth", 0.0)

    /**
     * The width actually left for text inside a block's text control, [PageGeometry] with the text
     * control's own insets and padding already subtracted - the same column width the layout engine
     * measured the [documentLayoutProperty] against.
     */
    fun columnWidthProperty(): ReadOnlyDoubleProperty = columnWidthProperty.readOnlyProperty

    /** @see columnWidthProperty */
    val columnWidth: Double
        get() = columnWidthProperty.get()

    internal fun setColumnWidth(value: Double) {
        columnWidthProperty.set(value)
    }

    private val listeners: MutableList<PaperFlowListener> = ArrayList()

    /** Registers [listener] to be told about every change and every intent reported by this view. */
    fun addPaperFlowListener(listener: PaperFlowListener) {
        listeners.add(listener)
    }

    /** Reverses [addPaperFlowListener]; does nothing if [listener] is not currently registered. */
    fun removePaperFlowListener(listener: PaperFlowListener) {
        listeners.remove(listener)
    }

    /** Read-only view of the currently registered listeners, for the skin to notify. */
    internal val paperFlowListeners: List<PaperFlowListener>
        get() = listeners

    /**
     * Scrolls the view so the block at [blockIndex] becomes visible.
     *
     * @param blockIndex Index of the block, matching
     *   [org.pcsoft.app.aighost.layouting.LaidOutLine.blockIndex].
     */
    fun scrollToBlock(blockIndex: Int) {
        (skin as? PaperFlowViewSkin)?.scrollToBlock(blockIndex)
    }

    override fun createDefaultSkin(): Skin<*> = PaperFlowViewSkin(this)

    override fun getUserAgentStylesheet(): String =
        PaperFlowView::class.java.getResource(DEFAULT_STYLESHEET)?.toExternalForm()
            ?: super.getUserAgentStylesheet()
}

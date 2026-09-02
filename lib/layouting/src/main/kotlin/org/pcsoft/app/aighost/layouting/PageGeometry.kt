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
 * Geometry of a page, as [LayoutEngine] needs it to place lines on it.
 *
 * The type mirrors the page format of the manuscript, but belongs to the layout core, which knows
 * neither the manuscript model nor a toolkit; a caller translates its own page format into this type,
 * the same way a stored style becomes a [TextStyle].
 *
 * The inner and outer margin are named after the binding, not after the screen: the inner margin sits
 * at the spine, the outer one at the open edge. Which side of a page that ends up on depends on
 * [mirroredMargins] and the page's own position, decided by [LayoutEngine].
 *
 * @property width Width of the page, in points.
 * @property height Height of the page, in points.
 * @property innerMargin Empty space at the spine of the page, in points.
 * @property outerMargin Empty space at the open edge of the page, in points.
 * @property topMargin Empty space above the text of the page, in points.
 * @property bottomMargin Empty space below the text of the page, in points.
 * @property mirroredMargins Whether the inner and outer margin swap sides between an odd and an even
 * page, as in a printed book. When `false`, every page uses the inner margin on the same side.
 */
data class PageGeometry(
    val width: Double,
    val height: Double,
    val innerMargin: Double,
    val outerMargin: Double,
    val topMargin: Double,
    val bottomMargin: Double,
    val mirroredMargins: Boolean = false
) {

    /** Vertical space a page's text may occupy, [height] with [topMargin] and [bottomMargin] removed. */
    val contentHeight: Double
        get() = height - topMargin - bottomMargin
}

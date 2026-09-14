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

package org.pcsoft.app.aighost.model.project.design

/**
 * Where the page number is printed on a sheet, or whether it is printed at all.
 *
 * The value is part of a [PageNumberDesign] and is written to JSON by its constant name, so a stored
 * document stays readable when new constants are added.
 *
 * `INNER`/`OUTER` alternate their horizontal side by sheet parity, the same way [PageFormat]'s inner
 * and outer margin do: an odd sheet is treated as a right-hand page (inner = left, outer = right), an
 * even sheet as a left-hand page.
 */
enum class PageNumberPosition {

    /** No page number is printed. */
    OFF,

    /** Top of the sheet, at the left margin. */
    TOP_LEFT,

    /** Top of the sheet, centred between the margins. */
    TOP_CENTER,

    /** Top of the sheet, at the right margin. */
    TOP_RIGHT,

    /** Top of the sheet, at the spine on an odd page and at the open edge on an even page. */
    TOP_INNER,

    /** Top of the sheet, at the open edge on an odd page and at the spine on an even page. */
    TOP_OUTER,

    /** Bottom of the sheet, at the left margin. */
    BOTTOM_LEFT,

    /** Bottom of the sheet, centred between the margins. */
    BOTTOM_CENTER,

    /** Bottom of the sheet, at the right margin. */
    BOTTOM_RIGHT,

    /** Bottom of the sheet, at the spine on an odd page and at the open edge on an even page. */
    BOTTOM_INNER,

    /** Bottom of the sheet, at the open edge on an odd page and at the spine on an even page. */
    BOTTOM_OUTER
}

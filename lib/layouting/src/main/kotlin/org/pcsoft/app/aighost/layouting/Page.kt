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
 * A single page of a [DocumentLayout].
 *
 * [position] and [pageNumber] answer two different questions and must not be confused: [position] is
 * where the page sits in the physical run of sheets, counted for every page including an inactive
 * one, while [pageNumber] is what is printed on the page, `null` where the page carries none - the
 * title page, the copyright page or a switched off optional part, for instance. Switching a part on
 * or off therefore only ever renumbers [pageNumber], it never moves a page's [position].
 *
 * [leftMargin] and [rightMargin] are already resolved to a physical side: a caller painting the page
 * does not have to know [PageGeometry.mirroredMargins] or work out whether this page is a recto or a
 * verso page - [LayoutEngine] decided that once, from [position].
 *
 * @property position Position of the page in the physical run of the document, 0-based.
 * @property pageNumber Number printed on the page, or `null` if the page carries none.
 * @property active Whether the page belongs to a part that is currently part of the book.
 * @property lines Lines placed on this page.
 * @property leftMargin Empty space at the left edge of the page, in points.
 * @property rightMargin Empty space at the right edge of the page, in points.
 * @property topMargin Empty space above the text of the page, in points.
 * @property bottomMargin Empty space below the text of the page, in points.
 */
data class Page(
    val position: Int,
    val pageNumber: Int?,
    val active: Boolean,
    val lines: List<LaidOutLine>,
    val leftMargin: Double,
    val rightMargin: Double,
    val topMargin: Double,
    val bottomMargin: Double
)

/**
 * The complete result of paginating one part or a whole book.
 *
 * @property pages Pages in the order they are printed.
 */
data class DocumentLayout(
    val pages: List<Page>
)

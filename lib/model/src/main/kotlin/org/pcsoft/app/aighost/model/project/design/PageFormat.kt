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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

/** Width of an A5 page in points, the page size a fresh project is written on. */
const val A5_WIDTH: Double = 419.53

/** Height of an A5 page in points, the page size a fresh project is written on. */
const val A5_HEIGHT: Double = 595.28

/**
 * Geometry of a single page of the manuscript: how large the sheet is and how much of it stays empty
 * on each of its four sides.
 *
 * Every value is given in points, the unit the layout and the renderer work in.
 *
 * The horizontal margins are named after the binding and not after the screen: the inner margin is
 * the one at the spine, the outer margin the one at the open edge of the book. Which of them ends up
 * on the left of a page therefore depends on whether that page carries an odd or an even number.
 *
 * @property width Width of the page, an A5 sheet by default.
 * @property height Height of the page, an A5 sheet by default.
 * @property innerMargin Empty space at the spine of the page, 20 points by default.
 * @property outerMargin Empty space at the open edge of the page, 15 points by default.
 * @property topMargin Empty space above the text of the page, 15 points by default.
 * @property bottomMargin Empty space below the text of the page, 20 points by default.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class PageFormat(
    var width: Double = A5_WIDTH,
    var height: Double = A5_HEIGHT,

    var innerMargin: Double = 20.0,
    var outerMargin: Double = 15.0,
    var topMargin: Double = 15.0,
    var bottomMargin: Double = 20.0
)

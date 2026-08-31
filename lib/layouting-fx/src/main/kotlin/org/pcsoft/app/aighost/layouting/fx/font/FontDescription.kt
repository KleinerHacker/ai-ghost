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

package org.pcsoft.app.aighost.layouting.fx.font

/**
 * The face a piece of text is resolved and measured with, in the form the JavaFX font system needs.
 *
 * This is the input of font resolution, not of the layout: line breaking and pagination take a
 * [org.pcsoft.app.aighost.layouting.TextStyle] of the core, which also carries alignment and spacing.
 * A description names only what picks a font file - the family, the size in points and the two
 * switches of the cut - so [FontResolver] and [JavaFxTextMetrics] never depend on how a caller
 * stores its fonts.
 *
 * The size is a whole number of points on purpose: a stored font never carries a fraction, and the
 * measurement cache keys on this value.
 *
 * @property family Family name of the face, as the resolving side looks it up.
 * @property size Size of the face in whole points.
 * @property bold Whether the text is set in a bold weight.
 * @property italic Whether the text is set slanted.
 */
data class FontDescription(
    val family: String,
    val size: Int,
    val bold: Boolean = false,
    val italic: Boolean = false
)

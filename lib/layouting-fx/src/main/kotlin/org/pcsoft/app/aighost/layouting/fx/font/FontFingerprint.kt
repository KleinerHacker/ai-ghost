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
 * What one font family measures like on the machine that took the measurement.
 *
 * The fingerprint is an identity, not an input of the layout: nothing here is fed into line breaking
 * or pagination. It exists so a caller can tell whether the family it draws with today is the same
 * one - in the same version, from the same file - that a document was written with, because two
 * machines can both know a family called `Garamond` and still set it differently.
 *
 * It is taken from measurements rather than from the font file, so it captures exactly what
 * influences the layout and stays quiet about everything else. Two fingerprints are comparable only
 * when they were taken over the same reference text at the same size, which is why
 * [FontFingerprints] fixes both for good.
 *
 * @property widths Digest over the width of every character of the reference text, in its order.
 * @property ascent Height of a line above the baseline, in points.
 * @property descent Depth of a line below the baseline, in points.
 * @property leading Gap the face asks for between two lines, in points.
 */
data class FontFingerprint(
    val widths: String,
    val ascent: Double,
    val descent: Double,
    val leading: Double
)

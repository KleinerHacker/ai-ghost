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
 * The only way the layout core learns how wide and how high a piece of text is set.
 *
 * Breaking a line needs three numbers and no more: the width of a word, the width of the single
 * space between two words and the vertical metrics of a line. Everything else - which font file is
 * opened, whether a family is installed at all, how a measurement is cached - belongs to the side
 * that can actually draw and stays behind this interface.
 *
 * That split is what keeps the core testable: the production implementation measures with the very
 * toolkit that later paints, while [FixedTextMetrics] answers with numbers a test can predict.
 *
 * An implementation is expected to be **stable**: the same arguments must give the same number
 * within one run, otherwise a layout cannot be reproduced.
 */
interface TextMetrics {

    /**
     * Width of a single word, without any surrounding space.
     *
     * @param style Style the word is set in.
     * @param word Word to measure; it carries no line break and no space.
     * @return Width in points, unrounded.
     */
    fun wordWidth(style: TextStyle, word: String): Double

    /**
     * Width of the single space between two words of the given style.
     *
     * @param style Style the words are set in.
     * @return Width in points, unrounded.
     */
    fun spaceWidth(style: TextStyle): Double

    /**
     * Ascent, descent and leading of a line set in the given style.
     *
     * The returned metrics are those of the bare face; the line spacing of the style is applied by
     * the caller, not here.
     *
     * @param style Style the line is set in.
     */
    fun lineMetrics(style: TextStyle): LineMetrics
}

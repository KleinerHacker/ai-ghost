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

package org.pcsoft.app.aighost.app.font

import org.pcsoft.app.aighost.layouting.fx.font.FontFingerprints
import org.pcsoft.app.aighost.model.common.StyleData
import org.pcsoft.app.aighost.model.project.design.Design

/**
 * The fonts of a design, fingerprinted when the project is written and compared when it is read.
 *
 * The two halves belong together and are therefore in one place: [stamp] records what a font
 * measured like on the machine the manuscript was written on, and [check] tells afterwards which
 * elements are not set the way they were written.
 *
 * A font is stamped once and then left alone. Overwriting a fingerprint on every save would turn the
 * record into "what the last machine measured" and the comparison would never report anything.
 *
 * **Threading:** both measure, so both must run on the JavaFX application thread.
 */
object FontIdentityCheck {

    /** Key of the text naming the title of the book. */
    const val ELEMENT_TITLE: String = "text.font.element.title"

    /** Key of the text naming the author line. */
    const val ELEMENT_AUTHOR: String = "text.font.element.author"

    /** Key of the text naming the copyright page. */
    const val ELEMENT_COPYRIGHT: String = "text.font.element.copyright"

    /** Key of the text naming the title of a chapter. */
    const val ELEMENT_CHAPTER_TITLE: String = "text.font.element.chapterTitle"

    /** Key of the text naming the line below the title of a chapter. */
    const val ELEMENT_CHAPTER_TITLE_APPENDIX: String = "text.font.element.chapterTitleAppendix"

    /** Key of the text naming the body text. */
    const val ELEMENT_TEXT: String = "text.font.element.text"

    /**
     * One element of the design that is not set in the font it was written in.
     *
     * @property elementKey Key of the text naming the element, to be read from the message bundle.
     * @property identity What is wrong - a substituted family or a family that measures differently.
     */
    data class Finding(val elementKey: String, val identity: FontIdentity)

    /**
     * Records the fingerprint of every font of [design] that carries none yet.
     *
     * A font whose family is not installed is left without one: the fingerprint of a substitute
     * would describe the wrong family and would make every other machine report a deviation.
     *
     * @param design Design of the project that is about to be written.
     */
    fun stamp(design: Design) {
        stylesOf(design).forEach { (_, style) ->
            if (style.font.metrics != null) {
                return@forEach
            }

            style.font.metrics = FontFingerprints.of(style.font.name)?.toMetricsData()
        }
    }

    /**
     * Collects every element of [design] whose font is not the font it was written in.
     *
     * An element without a fingerprint and an element that matches are both left out, so the result
     * is empty for a project written on this machine and for a project older than the fingerprint.
     *
     * @param design Design of the project that was read.
     * @return The findings in the order the elements appear in a book.
     */
    fun check(design: Design): List<Finding> =
        stylesOf(design).mapNotNull { (key, style) ->
            when (val identity = FontIdentity.of(style.font)) {
                is FontIdentity.Substituted, is FontIdentity.Deviates -> Finding(key, identity)
                else -> null
            }
        }

    /** Every style of the design that carries a font of its own, named by its element. */
    private fun stylesOf(design: Design): List<Pair<String, StyleData>> = listOf(
        ELEMENT_TITLE to design.titleDesign.style,
        ELEMENT_AUTHOR to design.authorDesign.style,
        ELEMENT_COPYRIGHT to design.copyrightDesign.style,
        ELEMENT_CHAPTER_TITLE to design.chapterDesign.titleStyle,
        ELEMENT_CHAPTER_TITLE_APPENDIX to design.chapterDesign.titleAppendixStyle,
        ELEMENT_TEXT to design.textDesign.style
    )
}

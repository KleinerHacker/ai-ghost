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

package org.pcsoft.app.aighost.ai.action

/**
 * Splits the text an [AiAction] completed with into paragraphs.
 *
 * A blank line ends a paragraph, matching the paragraph model of the manuscript: a book part's text
 * is a list of strings, one per paragraph, none of them carrying a line break of its own. Without this
 * rule, a generated chapter would land in the model as a single, unusably long paragraph.
 */
object ParagraphSplitter {

    private val BLANK_LINE = Regex("\r\n|\r|\n")
    private val PARAGRAPH_BREAK = Regex("(?:\r\n|\r|\n)\\s*(?:\r\n|\r|\n)")

    /**
     * Splits [text] into paragraphs.
     *
     * Consecutive blank lines count as a single paragraph break. A line break that survived inside a
     * paragraph is folded into a single space, since a paragraph is a single line in the model. Empty
     * paragraphs, for example from leading or trailing blank lines, are dropped.
     *
     * @param text The text to split, as it was collected from [AiActionCallback.onChunk].
     * @return The paragraphs of [text], in order.
     */
    fun split(text: String): List<String> =
        text.split(PARAGRAPH_BREAK)
            .map { it.replace(BLANK_LINE, " ").trim() }
            .filter { it.isNotEmpty() }
}

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

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

/**
 * Developer tests for [ParagraphSplitter].
 */
class ParagraphSplitterTest {

    /**
     * A text without any blank line is one single paragraph.
     */
    @Test
    fun `text without a blank line is one paragraph`() {
        assertEquals(listOf("A single paragraph."), ParagraphSplitter.split("A single paragraph."))
    }

    /**
     * A blank line between two lines of text ends the first paragraph and starts the next one.
     */
    @Test
    fun `blank line ends a paragraph`() {
        val text = "First paragraph.\n\nSecond paragraph."

        assertEquals(listOf("First paragraph.", "Second paragraph."), ParagraphSplitter.split(text))
    }

    /**
     * Several consecutive blank lines still count as a single paragraph break, not as empty
     * paragraphs of their own.
     */
    @Test
    fun `several consecutive blank lines count as one break`() {
        val text = "First paragraph.\n\n\n\nSecond paragraph."

        assertEquals(listOf("First paragraph.", "Second paragraph."), ParagraphSplitter.split(text))
    }

    /**
     * A line break that survives inside a paragraph is folded into a single space, since a paragraph
     * is one string without a line break of its own in the manuscript model.
     */
    @Test
    fun `line break inside a paragraph becomes a space`() {
        val text = "First line\nstill the same paragraph."

        assertEquals(listOf("First line still the same paragraph."), ParagraphSplitter.split(text))
    }

    /**
     * Leading and trailing blank lines must not produce empty paragraphs.
     */
    @Test
    fun `leading and trailing blank lines are dropped`() {
        val text = "\n\nOnly paragraph.\n\n"

        assertEquals(listOf("Only paragraph."), ParagraphSplitter.split(text))
    }

    /**
     * An empty text has no paragraph at all.
     */
    @Test
    fun `empty text has no paragraph`() {
        assertEquals(emptyList<String>(), ParagraphSplitter.split(""))
    }
}

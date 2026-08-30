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

import java.text.BreakIterator
import java.util.Locale

/**
 * Breaks blocks line by line, taking every word that still fits.
 *
 * Where a line *may* be broken is not decided here: `BreakIterator.getLineInstance` is asked, so the
 * rules of the platform apply and a hyphen is a break opportunity just as a space is. Which of those
 * opportunities is used is then the plain greedy walk - a word is added while the line still fits
 * into the column, and the line is closed when it does not.
 *
 * A break opportunity is not the same as a gap: after a space the next word is set a space further
 * on, after a hyphen it follows immediately. What the iterator hands out therefore carries whether it
 * was separated by whitespace, and only then does a gap take part in the width.
 *
 * A word that is wider than the whole column is **not** cut. It is set alone on its line and sticks
 * out; cutting inside a word needs a rule per language and would silently change what was written.
 *
 * Justified lines are stretched by widening the gaps, never by touching the words. The last line of
 * a block keeps the plain space of the font and stays at the left edge, which is what makes a
 * justified paragraph look like one.
 *
 * The breaker holds no state of its own; two runs over the same input give the same numbers as long
 * as [metrics] answers stably.
 *
 * @property metrics The measuring the break decisions are taken with.
 */
class GreedyLineBreaker(private val metrics: TextMetrics) : LineBreaker {

    override fun breakText(blocks: List<TextBlock>, columnWidth: Double): LaidOutText {
        require(columnWidth > 0.0) { "The column width must be greater than zero, but was $columnWidth" }

        val lines = ArrayList<LaidOutLine>()
        var top = 0.0

        blocks.forEachIndexed { blockIndex, block ->
            val style = block.style
            val line = metrics.lineMetrics(style)
            val advance = line.lineHeight * style.lineSpacing
            val space = metrics.spaceWidth(style)

            top += style.spaceBefore

            val rows = rowsOf(block, columnWidth, space)
            rows.forEachIndexed { rowIndex, row ->
                lines += place(
                    row = row,
                    last = rowIndex == rows.lastIndex,
                    style = style,
                    ascent = line.ascent,
                    top = top,
                    columnWidth = columnWidth,
                    space = space,
                    blockIndex = blockIndex
                )
                top += advance
            }

            top += style.spaceAfter
        }

        return LaidOutText(lines = lines, columnWidth = columnWidth, height = top)
    }

    /**
     * Walks the words of one block and collects them into rows that fit the column.
     *
     * A block without a single word gives exactly one empty row, so an empty paragraph keeps its
     * vertical space instead of disappearing.
     */
    private fun rowsOf(block: TextBlock, columnWidth: Double, space: Double): List<Row> {
        val words = wordsOf(block.text, block.style)
        if (words.isEmpty()) {
            return listOf(Row(emptyList(), 0.0, 0))
        }

        val rows = ArrayList<Row>()
        var current = ArrayList<Word>()
        var width = 0.0
        var gaps = 0

        words.forEach { word ->
            val gap = current.isNotEmpty() && current.last().spaceAfter
            val grown = if (current.isEmpty()) word.width else width + (if (gap) space else 0.0) + word.width
            // An overlong word is never split; it opens its own row and overflows the column.
            if (current.isNotEmpty() && grown > columnWidth) {
                rows += Row(current, width, gaps)
                current = ArrayList()
                width = word.width
                gaps = 0
            } else {
                width = grown
                if (gap) {
                    gaps++
                }
            }
            current += word
        }
        rows += Row(current, width, gaps)

        return rows
    }

    /** Cuts a text into the words the greedy walk moves in, keeping where each of them started. */
    private fun wordsOf(text: String, style: TextStyle): List<Word> {
        if (text.isEmpty()) {
            return emptyList()
        }

        val iterator = BreakIterator.getLineInstance(Locale.ROOT)
        iterator.setText(text)

        val words = ArrayList<Word>()
        var start = iterator.first()
        var end = iterator.next()
        while (end != BreakIterator.DONE) {
            // The segment carries the whitespace that separates it from the next one; that whitespace
            // is the gap and is measured, not set.
            val segment = text.substring(start, end)
            val word = segment.trimEnd()
            if (word.isNotEmpty()) {
                words += Word(
                    text = word,
                    start = start,
                    end = start + word.length,
                    width = metrics.wordWidth(style, word),
                    spaceAfter = word.length < segment.length
                )
            }
            start = end
            end = iterator.next()
        }

        return words
    }

    /** Gives one collected row its position, its gap width and its range inside the block. */
    private fun place(
        row: Row,
        last: Boolean,
        style: TextStyle,
        ascent: Double,
        top: Double,
        columnWidth: Double,
        space: Double,
        blockIndex: Int
    ): LaidOutLine {
        // Only a justified line that is neither the last one nor free of gaps is stretched.
        val stretched = style.alignment == TextAlignment.JUSTIFY &&
                !last && row.gaps > 0 && row.width < columnWidth
        val wordSpacing = if (stretched) space + (columnWidth - row.width) / row.gaps else space
        val width = if (stretched) columnWidth else row.width

        val x = when (style.alignment) {
            TextAlignment.LEFT, TextAlignment.JUSTIFY -> 0.0
            TextAlignment.CENTER -> (columnWidth - width) / 2.0
            TextAlignment.RIGHT -> columnWidth - width
        }

        return LaidOutLine(
            x = x,
            y = top,
            baseline = top + ascent,
            width = width,
            text = textOf(row.words),
            style = style,
            blockIndex = blockIndex,
            charStart = row.words.firstOrNull()?.start ?: 0,
            charEnd = row.words.lastOrNull()?.end ?: 0,
            wordSpacing = wordSpacing
        )
    }

    /** Puts the words of a row back together, with a space exactly where the input had one. */
    private fun textOf(words: List<Word>): String {
        val builder = StringBuilder()
        words.forEachIndexed { index, word ->
            if (index > 0 && words[index - 1].spaceAfter) {
                builder.append(' ')
            }
            builder.append(word.text)
        }
        return builder.toString()
    }

    /**
     * One word of a block: where it came from, how wide it is and whether a gap follows it.
     *
     * @property spaceAfter Whether the input separated this word from the next one by whitespace.
     */
    private data class Word(
        val text: String,
        val start: Int,
        val end: Int,
        val width: Double,
        val spaceAfter: Boolean
    )

    /** Words collected for one line, the width they take with plain gaps and the number of gaps. */
    private data class Row(val words: List<Word>, val width: Double, val gaps: Int)
}

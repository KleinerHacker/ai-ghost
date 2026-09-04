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
 * A [LineBreaker] that keeps the broken lines of every block and only breaks the ones that changed.
 *
 * Breaking a block is the step that measures - a word at a time, through [TextMetrics] - and on the
 * production side measuring belongs to a single UI thread. While the user types, all but one block of
 * a part stay exactly as they were, so re-breaking the whole part on every keystroke measures the
 * same thousands of words again for nothing.
 *
 * This breaker sits in front of a real one - [GreedyLineBreaker] in the application - and remembers
 * the result of breaking each block **on its own**, keyed by the block's text, its [TextStyle] and
 * the column width. A later call reuses that result for every block whose key is unchanged and asks
 * the delegate only for the rest. The blocks are then stacked back together into one [LaidOutText],
 * which is plain arithmetic and carries no measurement.
 *
 * The reassembled result is the same as breaking every block in one go with the delegate: a block
 * broken alone starts at `y = 0`, and stacking shifts each of its lines down by the height of the
 * blocks before it, exactly the way the delegate accumulates its vertical cursor. Only the last bit
 * of a coordinate can differ, and only when the delegate measures with real font metrics, because
 * adding the shift as one number rounds a hair differently than adding it line by line; with a
 * deterministic [TextMetrics] the two results are identical down to the bit.
 *
 * The cache only ever grows while the column width is held: a changed width drops it, since every key
 * would miss anyway and the old entries would never be read again. A design change - a new family, a
 * new size, a new spacing - is not visible in the key, so the caller clears the cache itself through
 * [clear] when the design changes.
 *
 * The breaker holds mutable state and is not thread safe; call it from the same thread that owns the
 * [TextMetrics] of its delegate.
 *
 * @property delegate The real line breaker a changed block is handed to.
 */
class IncrementalLineBreaker(private val delegate: LineBreaker) : LineBreaker {

    private val cache: MutableMap<BlockKey, LaidOutText> = HashMap()
    private var lastColumnWidth: Double? = null

    /** Number of block results currently held. */
    val cacheSize: Int
        get() = cache.size

    /** Number of blocks answered from the cache instead of the delegate, since the last [clear]. */
    var cacheHits: Long = 0L
        private set

    /** Number of blocks handed to the delegate, since the last [clear]. */
    var cacheMisses: Long = 0L
        private set

    /**
     * Breaks [blocks] against [columnWidth], reusing every block whose text, style and column width
     * are unchanged since a previous call.
     *
     * @param blocks Blocks in the order they are set; an empty list gives an empty result.
     * @param columnWidth Width of the column in points, greater than zero.
     * @return The stacked lines of every block and the space they take, equal to what [delegate]
     * produces for the same input.
     */
    override fun breakText(blocks: List<TextBlock>, columnWidth: Double): LaidOutText {
        require(columnWidth > 0.0) { "The column width must be greater than zero, but was $columnWidth" }

        if (lastColumnWidth != null && lastColumnWidth != columnWidth) {
            cache.clear()
        }
        lastColumnWidth = columnWidth

        val lines = ArrayList<LaidOutLine>()
        var top = 0.0

        blocks.forEachIndexed { blockIndex, block ->
            val fragment = fragmentOf(block, columnWidth)
            fragment.lines.forEach { line ->
                lines += line.copy(
                    y = line.y + top,
                    baseline = line.baseline + top,
                    blockIndex = blockIndex
                )
            }
            top += fragment.height
        }

        return LaidOutText(lines = lines, columnWidth = columnWidth, height = top)
    }

    /**
     * Breaks [blocks] once so their words are measured and their results are held, without using the
     * stacked layout for anything.
     *
     * A caller that knows which blocks it is about to lay out can call this ahead of time - off a
     * pause, say - so the first real [breakText] after a keystroke is a pure cache read.
     *
     * @param blocks Blocks to break and remember.
     * @param columnWidth Width the blocks are broken against, greater than zero.
     */
    fun prewarm(blocks: List<TextBlock>, columnWidth: Double) {
        require(columnWidth > 0.0) { "The column width must be greater than zero, but was $columnWidth" }

        if (lastColumnWidth != null && lastColumnWidth != columnWidth) {
            cache.clear()
        }
        lastColumnWidth = columnWidth

        blocks.forEach { block -> fragmentOf(block, columnWidth) }
    }

    /** Drops every held block result, for instance after the design changed the styles. */
    fun clear() {
        cache.clear()
        lastColumnWidth = null
        cacheHits = 0L
        cacheMisses = 0L
    }

    private fun fragmentOf(block: TextBlock, columnWidth: Double): LaidOutText {
        val key = BlockKey(block.text, block.style, columnWidth)
        cache[key]?.let { hit ->
            cacheHits++
            return hit
        }

        cacheMisses++
        val fragment = delegate.breakText(listOf(block), columnWidth)
        cache[key] = fragment
        return fragment
    }

    /** Identity of one block as far as breaking is concerned. */
    private data class BlockKey(
        val text: String,
        val style: TextStyle,
        val columnWidth: Double
    )
}

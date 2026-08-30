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
 * Turns a sequence of blocks into set lines.
 *
 * The step is an interface of its own because how a paragraph is broken is a decision that will be
 * taken more than once: the greedy walk of [GreedyLineBreaker] is what a text editor needs, while a
 * printed page may later want a breaking that looks at the paragraph as a whole. Everything above
 * works on this interface, so such a second implementation costs nothing but itself.
 */
interface LineBreaker {

    /**
     * Breaks the given blocks against a column of the given width.
     *
     * @param blocks Blocks in the order they are set; an empty list gives an empty result.
     * @param columnWidth Width of the column in points, greater than zero.
     * @return The set lines and the space they take.
     */
    fun breakText(blocks: List<TextBlock>, columnWidth: Double): LaidOutText
}

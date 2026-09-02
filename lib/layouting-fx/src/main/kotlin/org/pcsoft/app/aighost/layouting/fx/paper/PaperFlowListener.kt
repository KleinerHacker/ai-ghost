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

package org.pcsoft.app.aighost.layouting.fx.paper

/**
 * Everything [PaperFlowView] reports about what happens inside it.
 *
 * The view never applies a change and never holds the document itself - it only tells a listener
 * what the person at the keyboard did or asked for. A consumer decides on its own whether and how to
 * apply a change, typically by writing the new text back into its own model and handing the view a
 * freshly recomputed [org.pcsoft.app.aighost.layouting.DocumentLayout], and by wrapping the change
 * into its own undo entry - none of that is this interface's concern.
 *
 * Every method carries a default empty body, so an implementor only overrides what it actually needs.
 */
interface PaperFlowListener {

    /**
     * The text of the block at [blockIndex] was changed by the person editing it.
     *
     * @param blockIndex Index of the changed block, matching
     *   [org.pcsoft.app.aighost.layouting.LaidOutLine.blockIndex].
     * @param text Full text of the block after the change.
     */
    fun onTextChanged(blockIndex: Int, text: String) {}

    /**
     * The caret moved inside the block at [blockIndex], through typing, a click or a key press.
     *
     * @param blockIndex Index of the block the caret sits in.
     * @param caretPosition Character offset of the caret inside the text of that block.
     */
    fun onCaretMoved(blockIndex: Int, caretPosition: Int) {}

    /**
     * The block at [blockIndex] gained or lost focus.
     *
     * @param blockIndex Index of the block whose focus changed.
     * @param focused `true` if the block just gained focus, `false` if it just lost it.
     */
    fun onFocusChanged(blockIndex: Int, focused: Boolean) {}

    /**
     * The person asked to split the block at [blockIndex] into two, at [charIndex].
     *
     * @param blockIndex Index of the block to split.
     * @param charIndex Character offset the split was requested at.
     */
    fun onSplitRequested(blockIndex: Int, charIndex: Int) {}

    /**
     * The person asked to merge the block at [blockIndex] with a neighbour.
     *
     * @param blockIndex Index of the block the request originated from.
     * @param withPrevious `true` to merge with the block before it, `false` for the block after it.
     */
    fun onMergeRequested(blockIndex: Int, withPrevious: Boolean) {}

    /**
     * The person asked to remove the block at [blockIndex] entirely.
     *
     * @param blockIndex Index of the block to remove.
     */
    fun onRemoveRequested(blockIndex: Int) {}
}

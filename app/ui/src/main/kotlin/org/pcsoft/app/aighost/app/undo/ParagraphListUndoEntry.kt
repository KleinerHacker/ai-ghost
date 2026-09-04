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

package org.pcsoft.app.aighost.app.undo

import javafx.beans.property.ListProperty

/**
 * Undo entry for one structural change of a part's paragraph list - a split, a merge, a move or a
 * removal - remembering the whole list before and after the change, together with the caret target it
 * carried at each side.
 *
 * Unlike [PropertyUndoEntry], which folds consecutive changes of the same source into one entry, a
 * structural change is never merged with another: every split, merge, move and removal pushes its own
 * entry, one call to [UndoStack.push] each.
 *
 * The entry never talks to a flow view or a view model directly - [restoreCaret] is handed the block
 * index and the character offset to place the caret at, and does whatever it takes to get the caret
 * there, including recomputing the layout the paragraph list is shown through.
 */
class ParagraphListUndoEntry(
    override val label: String,
    private val paragraphs: ListProperty<String>,
    private val before: List<String>,
    private val after: List<String>,
    private val caretBefore: Pair<Int, Int>,
    private val caretAfter: Pair<Int, Int>,
    private val restoreCaret: (blockIndex: Int, charOffset: Int) -> Unit,
) : UndoEntry {

    override fun undo() {
        paragraphs.setAll(before)
        restoreCaret(caretBefore.first, caretBefore.second)
    }

    override fun redo() {
        paragraphs.setAll(after)
        restoreCaret(caretAfter.first, caretAfter.second)
    }
}

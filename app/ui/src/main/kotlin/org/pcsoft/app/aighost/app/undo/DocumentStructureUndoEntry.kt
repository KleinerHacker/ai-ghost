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

import org.pcsoft.app.aighost.app.controller.PendingCaretTarget
import org.pcsoft.framework.simplay.engine.model.Document

/**
 * Undo entry for one structural change of a page's block list (IP-32) - a split, a remove or a move -
 * remembering the whole book [Document] before and after the change, together with the caret target it
 * carried at each side.
 *
 * Unlike [PropertyUndoEntry], which folds consecutive changes of the same source into one entry, a
 * structural change is never merged with another: every split, remove and move pushes its own entry,
 * one call to [UndoStack.push] each.
 *
 * The entry never talks to the sheet or the view model directly - [apply] is handed the [Document] and
 * [PendingCaretTarget] to put in place and does whatever it takes to get both there, the same way
 * [org.pcsoft.app.aighost.app.ui.component.BookPartEditorViewModel] applies a fresh document on every
 * other path.
 */
class DocumentStructureUndoEntry(
    override val label: String,
    private val before: Document,
    private val after: Document,
    private val caretBefore: PendingCaretTarget,
    private val caretAfter: PendingCaretTarget,
    private val apply: (Document, PendingCaretTarget) -> Unit,
) : UndoEntry {

    override fun undo() = apply(before, caretBefore)

    override fun redo() = apply(after, caretAfter)
}

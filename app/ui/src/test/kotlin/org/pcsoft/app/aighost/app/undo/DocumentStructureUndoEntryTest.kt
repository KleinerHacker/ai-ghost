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

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.pcsoft.app.aighost.app.controller.PendingCaretTarget
import org.pcsoft.framework.simplay.engine.model.Document

/**
 * Developer tests for [DocumentStructureUndoEntry].
 *
 * The entry never applies anything on its own - it hands [Document] and [PendingCaretTarget] of the
 * requested side to the callback given at construction, exactly once per [undo]/[redo] call, the same
 * way [org.pcsoft.app.aighost.app.ui.component.BookPartEditorViewModel] restores its structural state.
 */
class DocumentStructureUndoEntryTest {

    private val before = Document(pages = emptyList())
    private val after = Document(pages = emptyList())
    private val caretBefore = PendingCaretTarget("prolog", 0, 0)
    private val caretAfter = PendingCaretTarget("prolog", 1, 0)

    /**
     * Use case: undoing the entry hands the "before" document and caret target back to the callback.
     */
    @Test
    fun undoAppliesTheDocumentAndCaretTargetFromBeforeTheChange() {
        var appliedDocument: Document? = null
        var appliedCaret: PendingCaretTarget? = null
        val entry = DocumentStructureUndoEntry("Split paragraph", before, after, caretBefore, caretAfter) { document, caret ->
            appliedDocument = document
            appliedCaret = caret
        }

        entry.undo()

        assertEquals(before, appliedDocument)
        assertEquals(caretBefore, appliedCaret)
    }

    /**
     * Use case: redoing the entry hands the "after" document and caret target back to the callback.
     */
    @Test
    fun redoAppliesTheDocumentAndCaretTargetFromAfterTheChange() {
        var appliedDocument: Document? = null
        var appliedCaret: PendingCaretTarget? = null
        val entry = DocumentStructureUndoEntry("Split paragraph", before, after, caretBefore, caretAfter) { document, caret ->
            appliedDocument = document
            appliedCaret = caret
        }

        entry.redo()

        assertEquals(after, appliedDocument)
        assertEquals(caretAfter, appliedCaret)
    }

    /**
     * Use case: the entry exposes the human readable label it was constructed with, shown in the Edit
     * menu, the tool bar tooltip and the history dropdown of [UndoStack].
     */
    @Test
    fun exposesItsLabel() {
        val entry = DocumentStructureUndoEntry("Split paragraph", before, after, caretBefore, caretAfter) { _, _ -> }

        assertEquals("Split paragraph", entry.label)
    }
}

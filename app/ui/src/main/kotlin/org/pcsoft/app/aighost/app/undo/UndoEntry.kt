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

/**
 * A single recorded change on the undo history of a project.
 *
 * An entry knows how to take the change it stands for back and how to apply it again, and carries the
 * [label] shown for it in the Edit menu, the tool bar tooltip and the history dropdown of [UndoStack].
 */
interface UndoEntry {
    /** Human readable description of the change this entry stands for. */
    val label: String

    /** Reverts the change this entry stands for. */
    fun undo()

    /** Applies the change this entry stands for again. */
    fun redo()
}

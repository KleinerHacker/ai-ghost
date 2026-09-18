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

import javafx.beans.property.SimpleStringProperty
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/**
 * Developer tests for [UndoStack].
 */
class UndoStackTest {

    /**
     * Use case: a single recorded change can be reverted and, once reverted, applied again.
     */
    @Test
    fun recordedChangeCanBeUndoneAndRedone() {
        val title = SimpleStringProperty("Old title")
        val stack = UndoStack()

        stack.record("Change title", title, "Old title", "New title")
        title.value = "New title"

        assertTrue(stack.canUndoProperty.value)
        stack.undo()
        assertEquals("Old title", title.value)
        assertFalse(stack.canUndoProperty.value)
        assertTrue(stack.canRedoProperty.value)

        stack.redo()
        assertEquals("New title", title.value)
        assertTrue(stack.canUndoProperty.value)
        assertFalse(stack.canRedoProperty.value)
    }

    /**
     * Use case: several consecutive changes of the same source, such as keystrokes typed into one
     * text field, fall together into a single undo step instead of one step per keystroke.
     */
    @Test
    fun consecutiveChangesOfTheSameSourceMergeIntoOneEntry() {
        val text = SimpleStringProperty("")
        val stack = UndoStack()

        stack.record("Type text", text, "", "H")
        stack.record("Type text", text, "H", "He")
        stack.record("Type text", text, "He", "Hel")
        stack.record("Type text", text, "Hel", "Hell")
        stack.record("Type text", text, "Hell", "Hello")
        text.value = "Hello"

        assertEquals(1, stack.undoEntries.size)

        stack.undo()
        assertEquals("", text.value)
        assertFalse(stack.canUndoProperty.value)
    }

    /**
     * Use case: ending the merge, as happens on a focus change, makes the next change of the same
     * source start a new entry instead of extending the previous one.
     */
    @Test
    fun endingTheMergeStartsANewEntryForTheNextChange() {
        val text = SimpleStringProperty("")
        val stack = UndoStack()

        stack.record("Type text", text, "", "Hello")
        stack.endMerging()
        stack.record("Type text", text, "Hello", "Hello world")
        text.value = "Hello world"

        assertEquals(2, stack.undoEntries.size)

        stack.undo()
        assertEquals("Hello", text.value)
        stack.undo()
        assertEquals("", text.value)
    }

    /**
     * Use case: a merge is only extended within [UndoStack.mergeTimeoutMillis] of the last change of
     * the same source - once that time has passed, the next change starts a new entry.
     */
    @Test
    fun mergeEndsOnItsOwnAfterTheTimeoutElapsed() {
        val text = SimpleStringProperty("")
        val stack = UndoStack().apply { mergeTimeoutMillis = 1 }

        stack.record("Type text", text, "", "Hello")
        Thread.sleep(20)
        stack.record("Type text", text, "Hello", "Hello world")
        text.value = "Hello world"

        assertEquals(2, stack.undoEntries.size)
    }

    /**
     * Use case: pushing a new entry clears the redo history, so an alternative change replaces the
     * path that was undone instead of standing next to it.
     */
    @Test
    fun recordingAfterAnUndoClearsTheRedoHistory() {
        val title = SimpleStringProperty("A")
        val stack = UndoStack()

        stack.record("Change title", title, "A", "B")
        title.value = "B"
        stack.undo()
        assertTrue(stack.canRedoProperty.value)

        stack.record("Change title", title, "A", "C")
        title.value = "C"

        assertFalse(stack.canRedoProperty.value)
    }

    /**
     * Use case: choosing an older entry from the history dropdown reverts every change up to and
     * including it in one step.
     */
    @Test
    fun undoUntilRevertsEveryEntryUpToTheChosenOne() {
        val value = SimpleStringProperty("0")
        val stack = UndoStack()

        stack.record("Set 1", value, "0", "1")
        stack.endMerging()
        stack.record("Set 2", value, "1", "2")
        stack.endMerging()
        stack.record("Set 3", value, "2", "3")
        value.value = "3"

        val target = stack.undoEntries[1]
        stack.undoUntil(target)

        assertEquals("1", value.value)
        assertEquals(1, stack.undoEntries.size)
        assertEquals(2, stack.redoEntries.size)
    }

    /**
     * Use case: choosing a later entry from the redo dropdown applies every change up to and
     * including it in one step.
     */
    @Test
    fun redoUntilAppliesEveryEntryUpToTheChosenOne() {
        val value = SimpleStringProperty("0")
        val stack = UndoStack()

        stack.record("Set 1", value, "0", "1")
        stack.endMerging()
        stack.record("Set 2", value, "1", "2")
        stack.endMerging()
        stack.record("Set 3", value, "2", "3")
        value.value = "3"

        stack.undo()
        stack.undo()
        stack.undo()

        val target = stack.redoEntries[1]
        stack.redoUntil(target)

        assertEquals("2", value.value)
        assertEquals(2, stack.undoEntries.size)
        assertEquals(1, stack.redoEntries.size)
    }

    /**
     * Use case: clearing the history, as happens when the open project changes, empties both the
     * undo and the redo side and disables both actions.
     */
    @Test
    fun clearEmptiesBothHistories() {
        val title = SimpleStringProperty("A")
        val stack = UndoStack()

        stack.record("Change title", title, "A", "B")
        title.value = "B"
        stack.undo()

        stack.clear()

        assertFalse(stack.canUndoProperty.value)
        assertFalse(stack.canRedoProperty.value)
        assertTrue(stack.undoEntries.isEmpty())
        assertTrue(stack.redoEntries.isEmpty())
    }

    /**
     * Use case: the history dropdown only ever exposes as many entries as configured, even though
     * every change is still kept internally for a plain undo/redo.
     */
    @Test
    fun visibleEntryCountLimitsTheExposedHistory() {
        val value = SimpleStringProperty("0")
        val stack = UndoStack().apply { visibleEntryCount.value = 2 }

        stack.record("Set 1", value, "0", "1")
        stack.endMerging()
        stack.record("Set 2", value, "1", "2")
        stack.endMerging()
        stack.record("Set 3", value, "2", "3")
        value.value = "3"

        assertEquals(2, stack.undoEntries.size)
        assertEquals(listOf("Set 3", "Set 2"), stack.undoEntries.map { it.label })
    }

    /**
     * Use case: recording a change where the old and the new value are equal is ignored, so a field
     * that was focused but never actually changed does not clutter the history.
     */
    @Test
    fun recordingAnUnchangedValueIsIgnored() {
        val title = SimpleStringProperty("A")
        val stack = UndoStack()

        stack.record("Change title", title, "A", "A")

        assertFalse(stack.canUndoProperty.value)
        assertTrue(stack.undoEntries.isEmpty())
    }
}

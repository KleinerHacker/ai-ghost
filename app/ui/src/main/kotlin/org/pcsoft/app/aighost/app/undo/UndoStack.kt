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

import javafx.beans.property.IntegerProperty
import javafx.beans.property.Property
import javafx.beans.property.ReadOnlyBooleanProperty
import javafx.beans.property.ReadOnlyBooleanWrapper
import javafx.beans.property.SimpleIntegerProperty
import javafx.collections.FXCollections
import javafx.collections.ObservableList

/**
 * Undo/redo history over the changes of a single open project.
 *
 * A change reaches the history through [record] or [push]. Consecutive changes coming from the same
 * source are merged into one entry as long as [mergeTimeoutMillis] does not elapse between them;
 * [endMerging] breaks a running merge early, for example when a text field loses focus.
 * [visibleEntryCount] limits how many entries [undoEntries] and [redoEntries] expose, for a history
 * dropdown that lets the user jump back or forward several steps at once through [undoUntil] and
 * [redoUntil].
 *
 * One instance belongs to exactly one open project; [clear] empties it when the project changes.
 */
class UndoStack {

    /** How many milliseconds may pass between two changes of the same source and still merge. */
    var mergeTimeoutMillis: Long = 1000

    /** How many entries [undoEntries] and [redoEntries] expose at most, for a history dropdown. */
    val visibleEntryCount: IntegerProperty = SimpleIntegerProperty(this, "visibleEntryCount", 10)

    private val undoDeque: ArrayDeque<UndoEntry> = ArrayDeque()
    private val redoDeque: ArrayDeque<UndoEntry> = ArrayDeque()

    private val undoEntriesList: ObservableList<UndoEntry> = FXCollections.observableArrayList()
    private val redoEntriesList: ObservableList<UndoEntry> = FXCollections.observableArrayList()

    /** Entries that can be undone, most recent first, limited to [visibleEntryCount]. */
    val undoEntries: ObservableList<UndoEntry> = FXCollections.unmodifiableObservableList(undoEntriesList)

    /** Entries that can be redone, most recent first, limited to [visibleEntryCount]. */
    val redoEntries: ObservableList<UndoEntry> = FXCollections.unmodifiableObservableList(redoEntriesList)

    private val canUndoWrapper = ReadOnlyBooleanWrapper(this, "canUndo", false)
    private val canRedoWrapper = ReadOnlyBooleanWrapper(this, "canRedo", false)

    /** Whether at least one entry can be undone. */
    val canUndoProperty: ReadOnlyBooleanProperty = canUndoWrapper.readOnlyProperty

    /** Whether at least one entry can be redone. */
    val canRedoProperty: ReadOnlyBooleanProperty = canRedoWrapper.readOnlyProperty

    /** The entry a following [record] call of the same source is folded into, if any. */
    private var pending: PropertyUndoEntry<*>? = null

    init {
        visibleEntryCount.addListener { _ -> refresh() }
    }

    /**
     * Records a change of [property] from [oldValue] to [newValue] under [label].
     *
     * A pending entry with the same [mergeKey] that is still within [mergeTimeoutMillis] is extended
     * instead of a new entry being pushed - this is how consecutive keystrokes of the same field
     * become a single undo step. [mergeKey] defaults to [property] itself, which is right for every
     * source except a control that is reused across several pages of the model.
     *
     * A call where [oldValue] equals [newValue] is ignored, so a field left unchanged does not clutter
     * the history.
     *
     * @param label human readable description of the change.
     * @param property the property that changed.
     * @param oldValue value of [property] before the change.
     * @param newValue value of [property] after the change.
     * @param mergeKey identifies the source of the change for merging, defaults to [property].
     */
    fun <T> record(label: String, property: Property<T>, oldValue: T, newValue: T, mergeKey: Any = property) {
        if (oldValue == newValue) return

        val current = pending
        if (current != null && current.mergeKey == mergeKey &&
            System.currentTimeMillis() - current.lastChangedAt <= mergeTimeoutMillis
        ) {
            @Suppress("UNCHECKED_CAST")
            (current as PropertyUndoEntry<T>).extend(newValue)
            refresh()
            return
        }

        val entry = PropertyUndoEntry(label, property, oldValue, newValue, mergeKey)
        push(entry)
        pending = entry
    }

    /**
     * Ends the currently pending merge, so the next [record] call of the same source starts a new
     * entry instead of extending the last one - called on focus loss or after a timeout.
     */
    fun endMerging() {
        pending = null
    }

    /**
     * Pushes a ready-made entry onto the history, clearing the redo history.
     *
     * @param entry the entry to push.
     */
    fun push(entry: UndoEntry) {
        undoDeque.addFirst(entry)
        redoDeque.clear()
        pending = null
        refresh()
    }

    /** Reverts the most recent entry and moves it to the redo history. */
    fun undo() {
        val entry = undoDeque.removeFirstOrNull() ?: return
        entry.undo()
        redoDeque.addFirst(entry)
        pending = null
        refresh()
    }

    /** Applies the most recently undone entry again and moves it back to the undo history. */
    fun redo() {
        val entry = redoDeque.removeFirstOrNull() ?: return
        entry.redo()
        undoDeque.addFirst(entry)
        pending = null
        refresh()
    }

    /**
     * Reverts entries up to and including [entry], for a multi-step jump chosen from a history
     * dropdown.
     *
     * @param entry the entry of [undoEntries] to revert up to.
     */
    fun undoUntil(entry: UndoEntry) {
        while (undoDeque.isNotEmpty()) {
            val next = undoDeque.first()
            undo()
            if (next === entry) break
        }
    }

    /**
     * Applies entries up to and including [entry] again, for a multi-step jump chosen from a history
     * dropdown.
     *
     * @param entry the entry of [redoEntries] to apply up to.
     */
    fun redoUntil(entry: UndoEntry) {
        while (redoDeque.isNotEmpty()) {
            val next = redoDeque.first()
            redo()
            if (next === entry) break
        }
    }

    /** Empties both histories, called whenever the open project changes. */
    fun clear() {
        undoDeque.clear()
        redoDeque.clear()
        pending = null
        refresh()
    }

    private fun refresh() {
        undoEntriesList.setAll(undoDeque.take(visibleEntryCount.value))
        redoEntriesList.setAll(redoDeque.take(visibleEntryCount.value))
        canUndoWrapper.value = undoDeque.isNotEmpty()
        canRedoWrapper.value = redoDeque.isNotEmpty()
    }
}

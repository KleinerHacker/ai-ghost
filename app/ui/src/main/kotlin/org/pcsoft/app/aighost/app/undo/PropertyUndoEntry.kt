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

import javafx.beans.property.Property

/**
 * Undo entry for a single change of a JavaFX [Property], remembering the value before and after.
 *
 * Consecutive changes coming from the same source - for example every keystroke typed into one text
 * field - are folded into one entry through [extend] instead of pushing a new entry for each of them.
 * [UndoStack.record] decides when that applies, based on [mergeKey].
 */
class PropertyUndoEntry<T> internal constructor(
    override val label: String,
    private val property: Property<T>,
    private val oldValue: T,
    private var newValue: T,
    /** Identifies the source the change came from, used to decide whether two entries may merge. */
    internal val mergeKey: Any,
) : UndoEntry {

    /** Point in time this entry was last extended, used to end merging after a timeout. */
    internal var lastChangedAt: Long = System.currentTimeMillis()
        private set

    override fun undo() {
        property.value = oldValue
    }

    override fun redo() {
        property.value = newValue
    }

    /**
     * Extends this entry with a further change of the same [mergeKey] instead of a new entry being
     * created for it.
     *
     * @param value the newest value of the property.
     */
    internal fun extend(value: T) {
        newValue = value
        lastChangedAt = System.currentTimeMillis()
    }
}

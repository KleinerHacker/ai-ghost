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

package org.pcsoft.app.aighost.app.ui.component.base

import javafx.beans.InvalidationListener
import javafx.collections.FXCollections
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/**
 * Developer tests for [AiTextFieldListViewModel].
 */
class AiTextFieldListViewModelTest {

    private val viewModel = AiTextFieldListViewModel()

    /**
     * Use case: a fresh list carries no entry, no hint and no wording of its own, so it shows an
     * empty area and explains its buttons in general words.
     */
    @Test
    fun freshListIsEmpty() {
        assertTrue(viewModel.entries.isEmpty())
        assertNull(viewModel.promptText.value)
        assertNull(viewModel.emptyText.value)
        assertNull(viewModel.addTooltip.value)
        assertNull(viewModel.deleteTooltip.value)
    }

    /**
     * Use case: the user writes into an entry, so the text stands in the list handed in from outside
     * instead of in a copy of it.
     */
    @Test
    fun writtenTextReachesTheListHandedIn() {
        val entries = FXCollections.observableArrayList("A ghost story", "Book one")
        viewModel.entries.set(entries)

        viewModel.setEntry(1, "Book two")

        assertEquals(listOf("A ghost story", "Book two"), entries)
    }

    /**
     * Use case: a position beyond the entries is written to, so nothing happens instead of an error.
     */
    @Test
    fun entryBeyondTheListIsDropped() {
        val entries = FXCollections.observableArrayList("A ghost story")
        viewModel.entries.set(entries)

        viewModel.setEntry(5, "Book one")
        viewModel.setEntry(-1, "Book one")

        assertEquals(listOf("A ghost story"), entries)
    }

    /**
     * Use case: an entry reports the text it was just given, so it is not written a second time and
     * whoever owns the entries does not see a change that is none.
     */
    @Test
    fun unchangedTextIsDropped() {
        val entries = FXCollections.observableArrayList("A ghost story")
        var changes = 0
        entries.addListener(InvalidationListener { changes++ })
        viewModel.entries.set(entries)

        viewModel.setEntry(0, "A ghost story")

        assertEquals(0, changes)
    }

    /**
     * Use case: the plus was pressed, so the request is handed on to the list with the position the
     * new entry would take, without an entry being added here.
     */
    @Test
    fun additionIsHandedOn() {
        val positions = ArrayList<Int>()
        viewModel.entries.set(FXCollections.observableArrayList("A ghost story"))
        viewModel.onAdd = { index -> positions += index }

        viewModel.add()

        assertEquals(listOf(1), positions)
        assertEquals(listOf("A ghost story"), viewModel.entries)
    }

    /**
     * Use case: the bin of an entry was pressed, so the request is handed on to the list with the
     * position of that entry, without the entry being taken out here.
     */
    @Test
    fun removalIsHandedOn() {
        val positions = ArrayList<Int>()
        viewModel.entries.set(FXCollections.observableArrayList("A ghost story", "Book one"))
        viewModel.onDelete = { index -> positions += index }

        viewModel.delete(1)

        assertEquals(listOf(1), positions)
        assertEquals(listOf("A ghost story", "Book one"), viewModel.entries)
    }

    /**
     * Use case: the wand of an entry was pressed, so the request is handed on to the list with the
     * position of that entry.
     */
    @Test
    fun creationIsHandedOn() {
        val positions = ArrayList<Int>()
        viewModel.entries.set(FXCollections.observableArrayList("A ghost story"))
        viewModel.onCreate = { index -> positions += index }

        viewModel.create(0)

        assertEquals(listOf(0), positions)
    }

    /**
     * Use case: nobody listens to the list, so pressing the plus, a bin or a wand is worked off
     * without anything happening.
     */
    @Test
    fun requestsWithoutAListenerAreWorkedOff() {
        viewModel.add()
        viewModel.delete(0)
        viewModel.create(0)
    }
}

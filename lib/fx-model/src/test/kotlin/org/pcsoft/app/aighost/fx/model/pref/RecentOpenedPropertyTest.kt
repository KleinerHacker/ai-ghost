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

package org.pcsoft.app.aighost.fx.model.pref

import javafx.beans.binding.Bindings
import javafx.beans.property.SimpleIntegerProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.beans.property.StringProperty
import javafx.collections.FXCollections
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.pcsoft.app.aighost.model.pref.Preferences
import org.pcsoft.app.aighost.model.pref.RecentOpened

/**
 * Developer tests for [RecentOpenedProperty].
 *
 * The property wraps the recently opened files of a parent model object and offers every field of
 * that object as a property of its own. Every test checks the object tree the way the user interface
 * uses it: a binding hangs on each property of the tree and the tests assert that every one of these
 * bindings is re-evaluated and delivers the current state - the property of the changed field, the
 * property of the wrapped object and the parent property above it.
 */
class RecentOpenedPropertyTest {

    private lateinit var preferences: Preferences
    private lateinit var property: RecentOpenedProperty

    /** Number of changes the parent property was told about. */
    private var parentEvents = 0

    /** Binding on the wrapped object, standing for a view bound to the recently opened files. */
    private lateinit var objectView: StringProperty
    private var objectViewChanges = 0

    /** Binding on the maximum, standing for a view bound to that single field. */
    private lateinit var maxView: StringProperty
    private var maxViewChanges = 0

    /** Binding on the entries, standing for a view bound to that single field. */
    private lateinit var entriesView: StringProperty
    private var entriesViewChanges = 0

    @BeforeEach
    fun setUp() {
        preferences = Preferences(recentOpened = RecentOpened(max = 10, entries = emptyList()))
        parentEvents = 0
        property = RecentOpenedProperty(
            setter = { preferences.recentOpened = it },
            getter = { preferences.recentOpened },
            fireEvent = { parentEvents++ }
        )

        objectView = SimpleStringProperty()
        val objectBinding = Bindings.createStringBinding({ state(property.value) }, property)
        // A listener keeps the binding eager, so an invalidation that is never followed by a read
        // still shows up as a change.
        objectBinding.addListener { _, _, _ -> objectViewChanges++ }
        objectView.bind(objectBinding)

        maxView = SimpleStringProperty()
        val maxBinding = property.maxProperty.asString()
        maxBinding.addListener { _, _, _ -> maxViewChanges++ }
        maxView.bind(maxBinding)

        entriesView = SimpleStringProperty()
        val entriesBinding = Bindings.createStringBinding(
            { property.entriesProperty.joinToString(";") },
            property.entriesProperty
        )
        entriesBinding.addListener { _, _, _ -> entriesViewChanges++ }
        entriesView.bind(entriesBinding)

        parentEvents = 0
        objectViewChanges = 0
        maxViewChanges = 0
        entriesViewChanges = 0
    }

    /** Text form of a recently opened object, used as the value of the binding on the whole object. */
    private fun state(recentOpened: RecentOpened): String =
        "${recentOpened.max}|${recentOpened.entries.joinToString(";")}"

    /**
     * Asserts that every binding of the object tree delivers the given state, so no view keeps the
     * value of a previous object or of a previous field value.
     */
    private fun assertTreeShows(max: Int, entries: List<String>) {
        assertEquals("$max|${entries.joinToString(";")}", objectView.get()) {
            "the binding on the recently opened object delivers an outdated state"
        }
        assertEquals(max.toString(), maxView.get()) {
            "the binding on the maximum delivers an outdated value"
        }
        assertEquals(entries.joinToString(";"), entriesView.get()) {
            "the binding on the entries delivers outdated entries"
        }
    }

    /**
     * Asserts that a change of a field was reported by the property of that field, by the property of
     * the wrapped object and by the parent property above it.
     */
    private fun assertFieldChangeReachedWholeTree(fieldViewChanges: Int) {
        assertTrue(fieldViewChanges > 0) { "the binding on the changed field was not re-evaluated" }
        assertTrue(objectViewChanges > 0) { "the binding on the recently opened object was not re-evaluated" }
        assertTrue(parentEvents > 0) { "the parent property was not told about the change" }
    }

    /**
     * Use case: the preferences are read from the settings file before the user interface is built, so
     * the property delivers the object that already sits in the model object.
     */
    @Test
    fun readsInitialValueFromModel() {
        assertEquals(RecentOpened(max = 10, entries = emptyList()), property.value)
        assertEquals(10, property.max)
        assertEquals(emptyList<String>(), property.entries)
        assertTreeShows(max = 10, entries = emptyList())
    }

    /**
     * Use case: the user raises the number of recently opened files in the settings dialog, so the new
     * limit lands in the model object and every binding of the object tree shows it.
     */
    @Test
    fun writesMaxToModelAndNotifiesTree() {
        property.max = 7

        assertEquals(7, preferences.recentOpened.max)
        assertTreeShows(max = 7, entries = emptyList())
        assertFieldChangeReachedWholeTree(maxViewChanges)
    }

    /**
     * Use case: the maximum is bound to a spinner of the settings dialog, so every value that spinner
     * produces reaches the model object and every binding of the object tree shows it.
     */
    @Test
    fun writesBoundMaxToModelAndNotifiesTree() {
        val source = SimpleIntegerProperty(2)
        property.maxProperty.bind(source)

        source.set(6)

        assertEquals(6, preferences.recentOpened.max)
        assertTreeShows(max = 6, entries = emptyList())
        assertFieldChangeReachedWholeTree(maxViewChanges)
    }

    /**
     * Use case: the recent files list is replaced as a whole, so the entries land in the model object
     * and every binding of the object tree shows them.
     */
    @Test
    fun writesReplacedEntriesToModelAndNotifiesTree() {
        property.entries = listOf("/books/first.md", "/books/second.md")

        assertEquals(listOf("/books/first.md", "/books/second.md"), preferences.recentOpened.entries)
        assertTreeShows(max = 10, entries = listOf("/books/first.md", "/books/second.md"))
        assertFieldChangeReachedWholeTree(entriesViewChanges)
    }

    /**
     * Use case: a newly opened file is appended to the list bound to the recent files menu, so the
     * content change alone reaches the model object and every binding of the object tree shows it.
     */
    @Test
    fun writesEntryAddedToModelAndNotifiesTree() {
        property.entriesProperty.add("/books/first.md")

        assertEquals(listOf("/books/first.md"), preferences.recentOpened.entries)
        assertTreeShows(max = 10, entries = listOf("/books/first.md"))
        assertFieldChangeReachedWholeTree(entriesViewChanges)
    }

    /**
     * Use case: a file the user deleted is dropped from the list bound to the recent files menu, so
     * the removal reaches the model object and every binding of the object tree shows it.
     */
    @Test
    fun writesEntryRemovedToModelAndNotifiesTree() {
        property.entries = listOf("/books/first.md", "/books/second.md")
        entriesViewChanges = 0
        objectViewChanges = 0
        parentEvents = 0

        property.entriesProperty.remove("/books/first.md")

        assertEquals(listOf("/books/second.md"), preferences.recentOpened.entries)
        assertTreeShows(max = 10, entries = listOf("/books/second.md"))
        assertFieldChangeReachedWholeTree(entriesViewChanges)
    }

    /**
     * Use case: the entries are bound to a list produced elsewhere, so every list that binding hands
     * over reaches the model object and every binding of the object tree shows it.
     */
    @Test
    fun writesBoundEntriesToModelAndNotifiesTree() {
        val source = SimpleObjectProperty(FXCollections.observableArrayList("/books/first.md"))
        property.entriesProperty.bind(source)

        source.set(FXCollections.observableArrayList("/books/second.md"))

        assertEquals(listOf("/books/second.md"), preferences.recentOpened.entries)
        assertTreeShows(max = 10, entries = listOf("/books/second.md"))
        assertFieldChangeReachedWholeTree(entriesViewChanges)
    }

    /**
     * Use case: the whole recently opened block is replaced - a settings file that was loaded again
     * for instance - so the field properties belong to another object afterwards and every binding of
     * the object tree shows the values of that object instead of the previous ones.
     */
    @Test
    fun writesReplacedObjectToModelAndNotifiesTree() {
        property.value = RecentOpened(max = 5, entries = listOf("/books/second.md"))

        assertEquals(RecentOpened(max = 5, entries = listOf("/books/second.md")), preferences.recentOpened)
        assertTreeShows(max = 5, entries = listOf("/books/second.md"))
        assertTrue(objectViewChanges > 0) { "the binding on the recently opened object was not re-evaluated" }
        assertTrue(maxViewChanges > 0) { "the binding on the maximum was not re-evaluated" }
        assertTrue(entriesViewChanges > 0) { "the binding on the entries was not re-evaluated" }
        assertTrue(parentEvents > 0) { "the parent property was not told about the change" }
    }

    /**
     * Use case: a field of the wrapped object is changed by application code past the property, so
     * reading a field property delivers the current value instead of a cached copy.
     */
    @Test
    fun readsFieldsChangedOnModel() {
        preferences.recentOpened.max = 4
        preferences.recentOpened.entries = listOf("/books/third.md")

        assertEquals(4, property.max)
        assertEquals(listOf("/books/third.md"), property.entries)
    }
}

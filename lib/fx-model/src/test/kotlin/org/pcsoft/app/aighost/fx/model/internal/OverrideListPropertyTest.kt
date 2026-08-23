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

package org.pcsoft.app.aighost.fx.model.internal

import javafx.beans.property.SimpleListProperty
import javafx.collections.FXCollections
import javafx.collections.ListChangeListener
import javafx.collections.ObservableList
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

/**
 * Developer tests for [OverrideListProperty].
 *
 * The property is a wrapper around a plain list field of a parent POJO: every read goes through the
 * getter of that POJO and every write - no matter whether it comes from application code, from a
 * content change or from a binding - goes through its setter.
 */
class OverrideListPropertyTest {

    /**
     * Plain model object standing in for the POJO the property writes into.
     */
    private class Pojo(var entries: List<String> = emptyList())

    private val pojo = Pojo()
    private var firedEvents = 0

    private val property = OverrideListProperty<String>(
        { pojo.entries = it },
        { pojo.entries },
        { firedEvents++ }
    )

    private fun observableListOf(vararg entries: String): ObservableList<String> =
        FXCollections.observableArrayList(*entries)

    /**
     * Use case: the POJO is filled from a stored file before the user interface is built, so reading
     * the property returns the entries that already sit in the POJO.
     */
    @Test
    fun readsInitialValueFromPojo() {
        pojo.entries = listOf("first.json", "second.json")

        assertEquals(listOf("first.json", "second.json"), property.get())
    }

    /**
     * Use case: the POJO is changed by application code behind the property, so the next read
     * delivers the current entries instead of a cached copy.
     */
    @Test
    fun readsLaterPojoChanges() {
        pojo.entries = listOf("first.json")
        assertEquals(listOf("first.json"), property.get())

        pojo.entries = listOf("second.json")

        assertEquals(listOf("second.json"), property.get())
    }

    /**
     * Use case: the user picks a new set of recent files, so the list is written straight into the
     * POJO.
     */
    @Test
    fun writesSetToPojo() {
        property.set(observableListOf("a.json", "b.json"))

        assertEquals(listOf("a.json", "b.json"), pojo.entries)
    }

    /**
     * Use case: the property is filled through the Kotlin value accessor, so the POJO carries the new
     * entries afterwards.
     */
    @Test
    fun writesValueToPojo() {
        property.value = observableListOf("a.json")

        assertEquals(listOf("a.json"), pojo.entries)
    }

    /**
     * Use case: the list is emptied, so the POJO holds an empty list instead of the previous entries.
     */
    @Test
    fun writesEmptyListToPojo() {
        pojo.entries = listOf("a.json")

        property.value = observableListOf()

        assertEquals(emptyList<String>(), pojo.entries)
    }

    /**
     * Use case: the property is bound to another list property - for example the items of a list view
     * - so every list produced by that binding lands in the POJO.
     */
    @Test
    fun writesBoundValueToPojo() {
        val source = SimpleListProperty(observableListOf("a.json"))

        property.bind(source)

        assertEquals(listOf("a.json"), pojo.entries)
        assertEquals(listOf("a.json"), property.get())

        source.set(observableListOf("b.json", "c.json"))

        assertEquals(listOf("b.json", "c.json"), pojo.entries)
        assertEquals(listOf("b.json", "c.json"), property.get())
    }

    /**
     * Use case: the property is bound bidirectionally to a view model property, so a change on either
     * side reaches the POJO and the other property.
     */
    @Test
    fun writesBidirectionallyBoundValueToPojo() {
        val other = SimpleListProperty(observableListOf("a.json"))

        property.bindBidirectional(other)

        other.set(observableListOf("b.json"))
        assertEquals(listOf("b.json"), pojo.entries)

        property.value = observableListOf("c.json")
        assertEquals(listOf("c.json"), other.get())
        assertEquals(listOf("c.json"), pojo.entries)
    }

    /**
     * Use case: a view listens for changes of the wrapped field, so writing the property notifies the
     * registered listener with the entries taken from the POJO.
     */
    @Test
    fun notifiesChangeListenerOnWrite() {
        val observed = mutableListOf<List<String>>()
        property.addListener { _, _, newValue -> observed.add(ArrayList(newValue)) }

        property.value = observableListOf("a.json")
        property.value = observableListOf("b.json")

        assertEquals(listOf(listOf("a.json"), listOf("b.json")), observed)
    }

    /**
     * Use case: the parent property has to report the change of a nested field as its own change, so
     * the callback handed in at construction is invoked for every write.
     */
    @Test
    fun invokesFireEventCallbackOnWrite() {
        property.value = observableListOf("a.json")
        property.value = observableListOf("b.json")

        assertEquals(2, firedEvents)
    }

    /**
     * Use case: a single recent file is appended through the property itself instead of replacing the
     * whole list, so the entry reaches the POJO and the change is reported.
     */
    @Test
    fun writesElementAddedOnPropertyToPojo() {
        property.add("a.json")

        assertEquals(listOf("a.json"), pojo.entries)
        assertEquals(1, firedEvents)
    }

    /**
     * Use case: a recent file is dropped through the property itself, so the POJO loses that entry and
     * the change is reported.
     */
    @Test
    fun writesElementRemovedOnPropertyToPojo() {
        pojo.entries = listOf("a.json", "b.json")

        property.remove("a.json")

        assertEquals(listOf("b.json"), pojo.entries)
        assertEquals(1, firedEvents)
    }

    /**
     * Use case: the recent files list is cleared through the property itself, so the POJO holds no
     * entry any more and the change is reported.
     */
    @Test
    fun writesClearOnPropertyToPojo() {
        pojo.entries = listOf("a.json")

        property.clear()

        assertEquals(emptyList<String>(), pojo.entries)
        assertEquals(1, firedEvents)
    }

    /**
     * Use case: the list instance handed to the property is mutated afterwards - the usual case for an
     * observable list shown in a list view - so the content change reaches the POJO and is reported.
     */
    @Test
    fun writesContentChangeOfAssignedListToPojo() {
        val list = observableListOf("a.json")
        property.value = list
        firedEvents = 0

        list.add("b.json")

        assertEquals(listOf("a.json", "b.json"), pojo.entries)
        assertEquals(1, firedEvents)
    }

    /**
     * Use case: the property is bound to a list that is filled later on, so entries appearing in the
     * bound list are written into the POJO as well and are reported.
     */
    @Test
    fun writesContentChangeOfBoundListToPojo() {
        val source = SimpleListProperty(observableListOf("a.json"))
        property.bind(source)
        firedEvents = 0

        source.add("b.json")

        assertEquals(listOf("a.json", "b.json"), pojo.entries)
        assertEquals(1, firedEvents)
    }

    /**
     * Use case: a view listens for content changes of the wrapped field, so appending an entry through
     * the property notifies the registered list change listener.
     */
    @Test
    fun notifiesListChangeListenerOnContentChange() {
        val added = mutableListOf<String>()
        property.addListener(ListChangeListener { change ->
            while (change.next()) {
                added.addAll(change.addedSubList)
            }
        })

        property.add("a.json")

        assertEquals(listOf("a.json"), added)
    }
}

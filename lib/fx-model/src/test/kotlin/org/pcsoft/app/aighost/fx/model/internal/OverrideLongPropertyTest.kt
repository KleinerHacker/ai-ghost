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

import javafx.beans.property.SimpleLongProperty
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

/**
 * Developer tests for [OverrideLongProperty].
 *
 * The property is a wrapper around a plain field of a parent POJO: every read goes through the
 * getter of that POJO and every write - no matter whether it comes from application code or from a
 * binding - goes through its setter.
 */
class OverrideLongPropertyTest {

    /**
     * Plain model object standing in for the POJO the property writes into.
     */
    private class Pojo(var size: Long = 0L)

    private val pojo = Pojo()
    private var firedEvents = 0

    private val property = OverrideLongProperty(
        { pojo.size = it },
        { pojo.size },
        { firedEvents++ }
    )

    /**
     * Use case: the POJO is filled from a stored file before the user interface is built, so reading
     * the property returns the value that already sits in the POJO.
     */
    @Test
    fun readsInitialValueFromPojo() {
        pojo.size = 7L

        assertEquals(7L, property.get())
        assertEquals(7L, property.value)
    }

    /**
     * Use case: the POJO is changed by application code behind the property, so the next read
     * delivers the current field value instead of a cached copy.
     */
    @Test
    fun readsLaterPojoChanges() {
        pojo.size = 3L
        assertEquals(3L, property.get())

        pojo.size = 9L

        assertEquals(9L, property.get())
    }

    /**
     * Use case: the user types a new limit into a text field bound to the property, so the value is
     * written straight into the POJO.
     */
    @Test
    fun writesSetToPojo() {
        property.set(5L)

        assertEquals(5L, pojo.size)
    }

    /**
     * Use case: the property is filled through the Kotlin value accessor, so the POJO carries the new
     * number afterwards.
     */
    @Test
    fun writesValueToPojo() {
        property.value = 11L

        assertEquals(11L, pojo.size)
    }

    /**
     * Use case: a caller clears the property, so the POJO falls back to zero rather than keeping the
     * previous number.
     */
    @Test
    fun writesNullAsZeroToPojo() {
        pojo.size = 4L

        property.setValue(null)

        assertEquals(0L, pojo.size)
    }

    /**
     * Use case: the property is bound to another property - for example a spinner value - so every
     * value produced by that binding lands in the POJO.
     */
    @Test
    fun writesBoundValueToPojo() {
        val source = SimpleLongProperty(2L)

        property.bind(source)

        assertEquals(2L, pojo.size)
        assertEquals(2L, property.get())

        source.set(8L)

        assertEquals(8L, pojo.size)
        assertEquals(8L, property.get())
    }

    /**
     * Use case: the property is bound bidirectionally to a view model property, so a change on either
     * side reaches the POJO and the other property.
     */
    @Test
    fun writesBidirectionallyBoundValueToPojo() {
        val other = SimpleLongProperty(1L)

        property.bindBidirectional(other)

        other.set(6L)
        assertEquals(6L, pojo.size)

        property.value = 12L
        assertEquals(12L, other.get())
        assertEquals(12L, pojo.size)
    }

    /**
     * Use case: a view listens for changes of the wrapped field, so writing the property notifies the
     * registered listener with the value taken from the POJO.
     */
    @Test
    fun notifiesChangeListenerOnWrite() {
        val observed = mutableListOf<Number>()
        property.addListener { _, _, newValue -> observed.add(newValue) }

        property.value = 3L
        property.value = 4L

        assertEquals(listOf<Number>(3L, 4L), observed)
    }

    /**
     * Use case: the parent property has to report the change of a nested field as its own change, so
     * the callback handed in at construction is invoked for every write.
     */
    @Test
    fun invokesFireEventCallbackOnWrite() {
        property.value = 1L
        property.value = 2L

        assertEquals(2, firedEvents)
    }
}

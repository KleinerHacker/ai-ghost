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

package org.pcsoft.app.aighost.fx.model.property.common

import javafx.beans.property.SimpleIntegerProperty
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

/**
 * Developer tests for [OverrideIntegerProperty].
 *
 * The property is a wrapper around a plain field of a parent POJO: every read goes through the
 * getter of that POJO and every write - no matter whether it comes from application code or from a
 * binding - goes through its setter.
 */
class OverrideIntegerPropertyTest {

    /**
     * Plain model object standing in for the POJO the property writes into.
     */
    private class Pojo(var max: Int = 0)

    private val pojo = Pojo()
    private var firedEvents = 0

    private val property = OverrideIntegerProperty(
        { pojo.max = it },
        { pojo.max },
        { firedEvents++ }
    )

    /**
     * Use case: the POJO is filled from a stored file before the user interface is built, so reading
     * the property returns the value that already sits in the POJO.
     */
    @Test
    fun readsInitialValueFromPojo() {
        pojo.max = 7

        assertEquals(7, property.get())
        assertEquals(7, property.value)
    }

    /**
     * Use case: the POJO is changed by application code behind the property, so the next read
     * delivers the current field value instead of a cached copy.
     */
    @Test
    fun readsLaterPojoChanges() {
        pojo.max = 3
        assertEquals(3, property.get())

        pojo.max = 9

        assertEquals(9, property.get())
    }

    /**
     * Use case: the user types a new limit into a text field bound to the property, so the value is
     * written straight into the POJO.
     */
    @Test
    fun writesSetToPojo() {
        property.set(5)

        assertEquals(5, pojo.max)
    }

    /**
     * Use case: the property is filled through the Kotlin value accessor, so the POJO carries the new
     * number afterwards.
     */
    @Test
    fun writesValueToPojo() {
        property.value = 11

        assertEquals(11, pojo.max)
    }

    /**
     * Use case: a caller clears the property, so the POJO falls back to zero rather than keeping the
     * previous number.
     */
    @Test
    fun writesNullAsZeroToPojo() {
        pojo.max = 4

        property.setValue(null)

        assertEquals(0, pojo.max)
    }

    /**
     * Use case: the property is bound to another property - for example a spinner value - so every
     * value produced by that binding lands in the POJO.
     */
    @Test
    fun writesBoundValueToPojo() {
        val source = SimpleIntegerProperty(2)

        property.bind(source)

        assertEquals(2, pojo.max)
        assertEquals(2, property.get())

        source.set(8)

        assertEquals(8, pojo.max)
        assertEquals(8, property.get())
    }

    /**
     * Use case: the property is bound bidirectionally to a view model property, so a change on either
     * side reaches the POJO and the other property.
     */
    @Test
    fun writesBidirectionallyBoundValueToPojo() {
        val other = SimpleIntegerProperty(1)

        property.bindBidirectional(other)

        other.set(6)
        assertEquals(6, pojo.max)

        property.value = 12
        assertEquals(12, other.get())
        assertEquals(12, pojo.max)
    }

    /**
     * Use case: a view listens for changes of the wrapped field, so writing the property notifies the
     * registered listener with the value taken from the POJO.
     */
    @Test
    fun notifiesChangeListenerOnWrite() {
        val observed = mutableListOf<Number>()
        property.addListener { _, _, newValue -> observed.add(newValue) }

        property.value = 3
        property.value = 4

        assertEquals(listOf<Number>(3, 4), observed)
    }

    /**
     * Use case: the parent property has to report the change of a nested field as its own change, so
     * the callback handed in at construction is invoked for every write.
     */
    @Test
    fun invokesFireEventCallbackOnWrite() {
        property.value = 1
        property.value = 2

        assertEquals(2, firedEvents)
    }
}

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

import javafx.beans.property.SimpleDoubleProperty
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

/**
 * Developer tests for [OverrideDoubleProperty].
 *
 * The property is a wrapper around a plain field of a parent POJO: every read goes through the
 * getter of that POJO and every write - no matter whether it comes from application code or from a
 * binding - goes through its setter.
 */
class OverrideDoublePropertyTest {

    /**
     * Plain model object standing in for the POJO the property writes into.
     */
    private class Pojo(var ratio: Double = 0.0)

    private val pojo = Pojo()
    private var firedEvents = 0

    private val property = OverrideDoubleProperty(
        { pojo.ratio = it },
        { pojo.ratio },
        { firedEvents++ }
    )

    /**
     * Use case: the POJO is filled from a stored file before the user interface is built, so reading
     * the property returns the value that already sits in the POJO.
     */
    @Test
    fun readsInitialValueFromPojo() {
        pojo.ratio = 7.5

        assertEquals(7.5, property.get())
        assertEquals(7.5, property.value)
    }

    /**
     * Use case: the POJO is changed by application code behind the property, so the next read
     * delivers the current field value instead of a cached copy.
     */
    @Test
    fun readsLaterPojoChanges() {
        pojo.ratio = 3.25
        assertEquals(3.25, property.get())

        pojo.ratio = 9.75

        assertEquals(9.75, property.get())
    }

    /**
     * Use case: the user drags a slider bound to the property, so the value is written straight into
     * the POJO.
     */
    @Test
    fun writesSetToPojo() {
        property.set(5.5)

        assertEquals(5.5, pojo.ratio)
    }

    /**
     * Use case: the property is filled through the Kotlin value accessor, so the POJO carries the new
     * number afterwards.
     */
    @Test
    fun writesValueToPojo() {
        property.value = 11.25

        assertEquals(11.25, pojo.ratio)
    }

    /**
     * Use case: a caller clears the property, so the POJO falls back to zero rather than keeping the
     * previous number.
     */
    @Test
    fun writesNullAsZeroToPojo() {
        pojo.ratio = 4.5

        property.setValue(null)

        assertEquals(0.0, pojo.ratio)
    }

    /**
     * Use case: the property is bound to another property - for example a slider value - so every
     * value produced by that binding lands in the POJO.
     */
    @Test
    fun writesBoundValueToPojo() {
        val source = SimpleDoubleProperty(2.5)

        property.bind(source)

        assertEquals(2.5, pojo.ratio)
        assertEquals(2.5, property.get())

        source.set(8.5)

        assertEquals(8.5, pojo.ratio)
        assertEquals(8.5, property.get())
    }

    /**
     * Use case: the property is bound bidirectionally to a view model property, so a change on either
     * side reaches the POJO and the other property.
     */
    @Test
    fun writesBidirectionallyBoundValueToPojo() {
        val other = SimpleDoubleProperty(1.5)

        property.bindBidirectional(other)

        other.set(6.5)
        assertEquals(6.5, pojo.ratio)

        property.value = 12.5
        assertEquals(12.5, other.get())
        assertEquals(12.5, pojo.ratio)
    }

    /**
     * Use case: a view listens for changes of the wrapped field, so writing the property notifies the
     * registered listener with the value taken from the POJO.
     */
    @Test
    fun notifiesChangeListenerOnWrite() {
        val observed = mutableListOf<Number>()
        property.addListener { _, _, newValue -> observed.add(newValue) }

        property.value = 3.5
        property.value = 4.5

        assertEquals(listOf<Number>(3.5, 4.5), observed)
    }

    /**
     * Use case: the parent property has to report the change of a nested field as its own change, so
     * the callback handed in at construction is invoked for every write.
     */
    @Test
    fun invokesFireEventCallbackOnWrite() {
        property.value = 1.5
        property.value = 2.5

        assertEquals(2, firedEvents)
    }
}

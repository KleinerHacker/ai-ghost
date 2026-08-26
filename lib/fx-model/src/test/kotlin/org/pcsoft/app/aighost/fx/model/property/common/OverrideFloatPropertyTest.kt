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

import javafx.beans.property.SimpleFloatProperty
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

/**
 * Developer tests for [OverrideFloatProperty].
 *
 * The property is a wrapper around a plain field of a parent POJO: every read goes through the
 * getter of that POJO and every write - no matter whether it comes from application code or from a
 * binding - goes through its setter.
 */
class OverrideFloatPropertyTest {

    /**
     * Plain model object standing in for the POJO the property writes into.
     */
    private class Pojo(var zoom: Float = 0f)

    private val pojo = Pojo()
    private var firedEvents = 0

    private val property = OverrideFloatProperty(
        { pojo.zoom = it },
        { pojo.zoom },
        { firedEvents++ }
    )

    /**
     * Use case: the POJO is filled from a stored file before the user interface is built, so reading
     * the property returns the value that already sits in the POJO.
     */
    @Test
    fun readsInitialValueFromPojo() {
        pojo.zoom = 7.5f

        assertEquals(7.5f, property.get())
        assertEquals(7.5f, property.value)
    }

    /**
     * Use case: the POJO is changed by application code behind the property, so the next read
     * delivers the current field value instead of a cached copy.
     */
    @Test
    fun readsLaterPojoChanges() {
        pojo.zoom = 3.25f
        assertEquals(3.25f, property.get())

        pojo.zoom = 9.75f

        assertEquals(9.75f, property.get())
    }

    /**
     * Use case: the user drags a slider bound to the property, so the value is written straight into
     * the POJO.
     */
    @Test
    fun writesSetToPojo() {
        property.set(5.5f)

        assertEquals(5.5f, pojo.zoom)
    }

    /**
     * Use case: the property is filled through the Kotlin value accessor, so the POJO carries the new
     * number afterwards.
     */
    @Test
    fun writesValueToPojo() {
        property.value = 11.25f

        assertEquals(11.25f, pojo.zoom)
    }

    /**
     * Use case: a caller clears the property, so the POJO falls back to zero rather than keeping the
     * previous number.
     */
    @Test
    fun writesNullAsZeroToPojo() {
        pojo.zoom = 4.5f

        property.setValue(null)

        assertEquals(0f, pojo.zoom)
    }

    /**
     * Use case: the property is bound to another property - for example a slider value - so every
     * value produced by that binding lands in the POJO.
     */
    @Test
    fun writesBoundValueToPojo() {
        val source = SimpleFloatProperty(2.5f)

        property.bind(source)

        assertEquals(2.5f, pojo.zoom)
        assertEquals(2.5f, property.get())

        source.set(8.5f)

        assertEquals(8.5f, pojo.zoom)
        assertEquals(8.5f, property.get())
    }

    /**
     * Use case: the property is bound bidirectionally to a view model property, so a change on either
     * side reaches the POJO and the other property.
     */
    @Test
    fun writesBidirectionallyBoundValueToPojo() {
        val other = SimpleFloatProperty(1.5f)

        property.bindBidirectional(other)

        other.set(6.5f)
        assertEquals(6.5f, pojo.zoom)

        property.value = 12.5f
        assertEquals(12.5f, other.get())
        assertEquals(12.5f, pojo.zoom)
    }

    /**
     * Use case: a view listens for changes of the wrapped field, so writing the property notifies the
     * registered listener with the value taken from the POJO.
     */
    @Test
    fun notifiesChangeListenerOnWrite() {
        val observed = mutableListOf<Number>()
        property.addListener { _, _, newValue -> observed.add(newValue) }

        property.value = 3.5f
        property.value = 4.5f

        assertEquals(listOf<Number>(3.5f, 4.5f), observed)
    }

    /**
     * Use case: the parent property has to report the change of a nested field as its own change, so
     * the callback handed in at construction is invoked for every write.
     */
    @Test
    fun invokesFireEventCallbackOnWrite() {
        property.value = 1.5f
        property.value = 2.5f

        assertEquals(2, firedEvents)
    }

    /**
     * Use case: the model object behind the parent property is exchanged - another project file was
     * loaded for instance - so the wrapped field belongs to another object afterwards and the parent
     * property lets this property take over the value of that object. Everyone listening here is told
     * about it, so a control bound to this property stops showing the value of the previous object.
     */
    @Test
    fun takesOverPojoValueOnRefresh() {
        val observed = mutableListOf<Number>()
        property.addListener { _, _, newValue -> observed.add(newValue) }

        pojo.zoom = 42.5f
        property.refresh()

        assertEquals(listOf<Number>(42.5f), observed)
        assertEquals(42.5f, property.get())
    }

    /**
     * Use case: the parent property announces the exchanged model object itself, so the alignment of
     * this property must not travel back to it - otherwise the same exchange would be reported once
     * more for every single field of the object.
     */
    @Test
    fun keepsParentQuietOnRefresh() {
        pojo.zoom = 42.5f

        property.refresh()

        assertEquals(0, firedEvents)
    }

    /**
     * Use case: the property is bound to a control while the model object behind the parent property
     * is exchanged, so the binding stays the source of the value: the alignment leaves the property
     * untouched and the next value of the binding still reaches the POJO.
     */
    @Test
    fun keepsBoundValueOnRefresh() {
        val source = SimpleFloatProperty(5.25f)
        property.bind(source)
        firedEvents = 0

        property.refresh()

        assertEquals(5.25f, pojo.zoom)
        assertEquals(0, firedEvents)

        source.set(8.75f)

        assertEquals(8.75f, pojo.zoom)
    }
}

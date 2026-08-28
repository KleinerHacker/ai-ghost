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

import javafx.beans.property.SimpleBooleanProperty
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/**
 * Developer tests for [OverrideBooleanProperty].
 *
 * The property is a wrapper around a plain field of a parent POJO: every read goes through the
 * getter of that POJO and every write - no matter whether it comes from application code or from a
 * binding - goes through its setter.
 */
class OverrideBooleanPropertyTest {

    /**
     * Plain model object standing in for the POJO the property writes into.
     */
    private class Pojo(var enabled: Boolean = false)

    private val pojo = Pojo()
    private var firedEvents = 0

    private val property = OverrideBooleanProperty(
        { pojo.enabled = it },
        { pojo.enabled },
        { firedEvents++ }
    )

    /**
     * Use case: the POJO is filled from a stored file before the user interface is built, so reading
     * the property returns the flag that already sits in the POJO.
     */
    @Test
    fun readsInitialValueFromPojo() {
        pojo.enabled = true

        assertTrue(property.get())
        assertTrue(property.value)
    }

    /**
     * Use case: the POJO is changed by application code behind the property, so the next read
     * delivers the current field value instead of a cached copy.
     */
    @Test
    fun readsLaterPojoChanges() {
        pojo.enabled = true
        assertTrue(property.get())

        pojo.enabled = false

        assertFalse(property.get())
    }

    /**
     * Use case: the user ticks a check box bound to the property, so the flag is written straight
     * into the POJO.
     */
    @Test
    fun writesSetToPojo() {
        property.set(true)

        assertTrue(pojo.enabled)
    }

    /**
     * Use case: the property is filled through the Kotlin value accessor, so the POJO carries the new
     * flag afterwards.
     */
    @Test
    fun writesValueToPojo() {
        property.value = true

        assertTrue(pojo.enabled)
    }

    /**
     * Use case: a caller clears the property, so the POJO falls back to false rather than keeping the
     * previous flag.
     */
    @Test
    fun writesNullAsFalseToPojo() {
        pojo.enabled = true

        property.setValue(null)

        assertFalse(pojo.enabled)
    }

    /**
     * Use case: the property is bound to another property - for example a toggle state - so every
     * value produced by that binding lands in the POJO.
     */
    @Test
    fun writesBoundValueToPojo() {
        val source = SimpleBooleanProperty(true)

        property.bind(source)

        assertTrue(pojo.enabled)
        assertTrue(property.get())

        source.set(false)

        assertFalse(pojo.enabled)
        assertFalse(property.get())
    }

    /**
     * Use case: the property is bound bidirectionally to a view model property, so a change on either
     * side reaches the POJO and the other property.
     */
    @Test
    fun writesBidirectionallyBoundValueToPojo() {
        val other = SimpleBooleanProperty(false)

        property.bindBidirectional(other)

        other.set(true)
        assertTrue(pojo.enabled)

        property.value = false
        assertFalse(other.get())
        assertFalse(pojo.enabled)
    }

    /**
     * Use case: a view listens for changes of the wrapped field, so writing the property notifies the
     * registered listener with the value taken from the POJO.
     */
    @Test
    fun notifiesChangeListenerOnWrite() {
        val observed = mutableListOf<Boolean>()
        property.addListener { _, _, newValue -> observed.add(newValue) }

        property.value = true
        property.value = false

        assertEquals(listOf(true, false), observed)
    }

    /**
     * Use case: the parent property has to report the change of a nested field as its own change, so
     * the callback handed in at construction is invoked for every write.
     */
    @Test
    fun invokesFireEventCallbackOnWrite() {
        property.value = true
        property.value = false

        assertEquals(2, firedEvents)
    }

    /**
     * Use case: the model object behind the parent property is exchanged - another project file was
     * loaded for instance - so the wrapped field belongs to another object afterwards and the parent
     * property lets this property take over the flag of that object. Everyone listening here is told
     * about it, so a check box bound to this property stops showing the flag of the previous object.
     */
    @Test
    fun takesOverPojoValueOnRefresh() {
        val observed = mutableListOf<Boolean>()
        property.addListener { _, _, newValue -> observed.add(newValue) }

        pojo.enabled = true
        property.refresh()

        assertEquals(listOf(true), observed)
        assertTrue(property.get())
    }

    /**
     * Use case: the parent property announces the exchanged model object itself, so the alignment of
     * this property must not travel back to it - otherwise the same exchange would be reported once
     * more for every single field of the object.
     */
    @Test
    fun keepsParentQuietOnRefresh() {
        pojo.enabled = true

        property.refresh()

        assertEquals(0, firedEvents)
    }

    /**
     * Use case: the property is bound to a check box while the model object behind the parent
     * property is exchanged, so the binding stays the source of the value: the alignment leaves the
     * property untouched and the next value of the binding still reaches the POJO.
     */
    @Test
    fun keepsBoundValueOnRefresh() {
        val source = SimpleBooleanProperty(true)
        property.bind(source)
        firedEvents = 0

        property.refresh()

        assertTrue(pojo.enabled)
        assertEquals(0, firedEvents)

        source.set(false)

        assertFalse(pojo.enabled)
    }
}

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

import javafx.beans.property.SimpleObjectProperty
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

/**
 * Developer tests for [OverrideObjectProperty].
 *
 * The property is a wrapper around a plain field of a parent POJO: every read goes through the
 * getter of that POJO and every write - no matter whether it comes from application code or from a
 * binding - goes through its setter.
 */
class OverrideObjectPropertyTest {

    /**
     * Value object stored in the wrapped field.
     */
    private data class Font(val name: String)

    /**
     * Plain model object standing in for the POJO the property writes into.
     */
    private class Pojo(var font: Font? = null)

    private val pojo = Pojo()
    private var firedEvents = 0

    private val property = OverrideObjectProperty(
        { pojo.font = it },
        { pojo.font },
        { firedEvents++ }
    )

    /**
     * Use case: the POJO is filled from a stored file before the user interface is built, so reading
     * the property returns the object that already sits in the POJO.
     */
    @Test
    fun readsInitialValueFromPojo() {
        pojo.font = Font("Serif")

        assertEquals(Font("Serif"), property.get())
        assertEquals(Font("Serif"), property.value)
    }

    /**
     * Use case: the POJO is changed by application code behind the property, so the next read
     * delivers the current field value instead of a cached copy.
     */
    @Test
    fun readsLaterPojoChanges() {
        pojo.font = Font("Serif")
        assertEquals(Font("Serif"), property.get())

        pojo.font = Font("Mono")

        assertEquals(Font("Mono"), property.get())
    }

    /**
     * Use case: the user picks an entry in a combo box bound to the property, so the object is
     * written straight into the POJO.
     */
    @Test
    fun writesSetToPojo() {
        property.set(Font("Sans"))

        assertEquals(Font("Sans"), pojo.font)
    }

    /**
     * Use case: the property is filled through the Kotlin value accessor, so the POJO carries the new
     * object afterwards.
     */
    @Test
    fun writesValueToPojo() {
        property.value = Font("Mono")

        assertEquals(Font("Mono"), pojo.font)
    }

    /**
     * Use case: a caller clears the property, so the POJO holds no object any more instead of keeping
     * the previous one.
     */
    @Test
    fun writesNullToPojo() {
        pojo.font = Font("Serif")

        property.setValue(null)

        assertNull(pojo.font)
    }

    /**
     * Use case: the property is bound to another property - for example a selection model value - so
     * every value produced by that binding lands in the POJO.
     */
    @Test
    fun writesBoundValueToPojo() {
        val source = SimpleObjectProperty<Font?>(Font("Serif"))

        property.bind(source)

        assertEquals(Font("Serif"), pojo.font)
        assertEquals(Font("Serif"), property.get())

        source.set(Font("Mono"))

        assertEquals(Font("Mono"), pojo.font)
        assertEquals(Font("Mono"), property.get())
    }

    /**
     * Use case: the property is bound bidirectionally to a view model property, so a change on either
     * side reaches the POJO and the other property.
     */
    @Test
    fun writesBidirectionallyBoundValueToPojo() {
        val other = SimpleObjectProperty<Font?>(Font("Serif"))

        property.bindBidirectional(other)

        other.set(Font("Sans"))
        assertEquals(Font("Sans"), pojo.font)

        property.value = Font("Mono")
        assertEquals(Font("Mono"), other.get())
        assertEquals(Font("Mono"), pojo.font)
    }

    /**
     * Use case: a view listens for changes of the wrapped field, so writing the property notifies the
     * registered listener with the value taken from the POJO.
     */
    @Test
    fun notifiesChangeListenerOnWrite() {
        val observed = mutableListOf<Font?>()
        property.addListener { _, _, newValue -> observed.add(newValue) }

        property.value = Font("Serif")
        property.value = Font("Mono")

        assertEquals(listOf(Font("Serif"), Font("Mono")), observed)
    }

    /**
     * Use case: the parent property has to report the change of a nested field as its own change, so
     * the callback handed in at construction is invoked for every write.
     */
    @Test
    fun invokesFireEventCallbackOnWrite() {
        property.value = Font("Serif")
        property.value = Font("Mono")

        assertEquals(2, firedEvents)
    }
}

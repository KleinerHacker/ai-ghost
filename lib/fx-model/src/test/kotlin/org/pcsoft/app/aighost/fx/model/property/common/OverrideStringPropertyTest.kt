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

import javafx.beans.property.SimpleStringProperty
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

/**
 * Developer tests for [OverrideStringProperty].
 *
 * The property is a wrapper around a plain field of a parent POJO: every read goes through the
 * getter of that POJO and every write - no matter whether it comes from application code or from a
 * binding - goes through its setter.
 */
class OverrideStringPropertyTest {

    /**
     * Plain model object standing in for the POJO the property writes into.
     */
    private class Pojo(var title: String? = null)

    private val pojo = Pojo()
    private var firedEvents = 0

    private val property = OverrideStringProperty(
        { pojo.title = it },
        { pojo.title },
        { firedEvents++ }
    )

    /**
     * Use case: the POJO is filled from a stored file before the user interface is built, so reading
     * the property returns the text that already sits in the POJO.
     */
    @Test
    fun readsInitialValueFromPojo() {
        pojo.title = "First chapter"

        assertEquals("First chapter", property.get())
        assertEquals("First chapter", property.value)
    }

    /**
     * Use case: the POJO is changed by application code behind the property, so the next read
     * delivers the current field value instead of a cached copy.
     */
    @Test
    fun readsLaterPojoChanges() {
        pojo.title = "Draft"
        assertEquals("Draft", property.get())

        pojo.title = "Final"

        assertEquals("Final", property.get())
    }

    /**
     * Use case: the user types a new title into a text field bound to the property, so the text is
     * written straight into the POJO.
     */
    @Test
    fun writesSetToPojo() {
        property.set("Second chapter")

        assertEquals("Second chapter", pojo.title)
    }

    /**
     * Use case: the property is filled through the Kotlin value accessor, so the POJO carries the new
     * text afterwards.
     */
    @Test
    fun writesValueToPojo() {
        property.value = "Prologue"

        assertEquals("Prologue", pojo.title)
    }

    /**
     * Use case: a caller clears the property, so the POJO holds no text any more instead of keeping
     * the previous one.
     */
    @Test
    fun writesNullToPojo() {
        pojo.title = "Epilogue"

        property.setValue(null)

        assertNull(pojo.title)
    }

    /**
     * Use case: the property is bound to another property - for example a text field content - so
     * every value produced by that binding lands in the POJO.
     */
    @Test
    fun writesBoundValueToPojo() {
        val source = SimpleStringProperty("Chapter one")

        property.bind(source)

        assertEquals("Chapter one", pojo.title)
        assertEquals("Chapter one", property.get())

        source.set("Chapter two")

        assertEquals("Chapter two", pojo.title)
        assertEquals("Chapter two", property.get())
    }

    /**
     * Use case: the property is bound bidirectionally to a view model property, so a change on either
     * side reaches the POJO and the other property.
     */
    @Test
    fun writesBidirectionallyBoundValueToPojo() {
        val other = SimpleStringProperty("Start")

        property.bindBidirectional(other)

        other.set("Middle")
        assertEquals("Middle", pojo.title)

        property.value = "End"
        assertEquals("End", other.get())
        assertEquals("End", pojo.title)
    }

    /**
     * Use case: a view listens for changes of the wrapped field, so writing the property notifies the
     * registered listener with the value taken from the POJO.
     */
    @Test
    fun notifiesChangeListenerOnWrite() {
        val observed = mutableListOf<String?>()
        property.addListener { _, _, newValue -> observed.add(newValue) }

        property.value = "One"
        property.value = "Two"

        assertEquals(listOf("One", "Two"), observed)
    }

    /**
     * Use case: the parent property has to report the change of a nested field as its own change, so
     * the callback handed in at construction is invoked for every write.
     */
    @Test
    fun invokesFireEventCallbackOnWrite() {
        property.value = "One"
        property.value = "Two"

        assertEquals(2, firedEvents)
    }

    /**
     * Use case: the model object behind the parent property is exchanged - another project file was
     * loaded for instance - so the wrapped field belongs to another object afterwards and the parent
     * property lets this property take over the text of that object. Everyone listening here is told
     * about it, so a text field bound to this property stops showing the text of the previous object.
     */
    @Test
    fun takesOverPojoValueOnRefresh() {
        val observed = mutableListOf<String?>()
        property.addListener { _, _, newValue -> observed.add(newValue) }

        pojo.title = "Refreshed chapter"
        property.refresh()

        assertEquals(listOf("Refreshed chapter"), observed)
        assertEquals("Refreshed chapter", property.get())
    }

    /**
     * Use case: the parent property announces the exchanged model object itself, so the alignment of
     * this property must not travel back to it - otherwise the same exchange would be reported once
     * more for every single field of the object.
     */
    @Test
    fun keepsParentQuietOnRefresh() {
        pojo.title = "Refreshed chapter"

        property.refresh()

        assertEquals(0, firedEvents)
    }

    /**
     * Use case: the property is bound to a text field while the model object behind the parent
     * property is exchanged, so the binding stays the source of the value: the alignment leaves the
     * property untouched and the next value of the binding still reaches the POJO.
     */
    @Test
    fun keepsBoundValueOnRefresh() {
        val source = SimpleStringProperty("Chapter one")
        property.bind(source)
        firedEvents = 0

        property.refresh()

        assertEquals("Chapter one", pojo.title)
        assertEquals(0, firedEvents)

        source.set("Chapter two")

        assertEquals("Chapter two", pojo.title)
    }
}

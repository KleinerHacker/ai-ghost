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

package org.pcsoft.app.aighost.fx.model.common

import javafx.beans.binding.Bindings
import javafx.beans.property.SimpleDoubleProperty
import javafx.beans.property.SimpleStringProperty
import javafx.beans.property.StringProperty
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.pcsoft.app.aighost.model.common.FontMetricsData

/**
 * Developer tests for [FontMetricsDataProperty].
 *
 * The property wraps the fingerprint of a font family and offers every field of that object as a
 * property of its own. Every test checks the object tree the way the user interface uses it: a binding
 * hangs on the fingerprint itself and on every single field of it, and the tests assert that a change
 * reaches every binding that has to know about it - upwards to the parent the property reports to as
 * well as downwards into the fields of an exchanged fingerprint.
 */
class FontMetricsDataPropertyTest {

    /** Stands for the model object carrying the fingerprint, the object a parent property writes into. */
    private class Holder(var metrics: FontMetricsData?)

    private lateinit var holder: Holder
    private lateinit var property: FontMetricsDataProperty

    /** Counts what the parent property is told, so the report up to the root becomes visible. */
    private var parentEvents = 0

    /** Binding on the whole fingerprint, standing for a view bound to the root of this object tree. */
    private lateinit var rootView: StringProperty
    private var rootViewChanges = 0

    /** Binding on the digest of the widths, standing for a view bound to that single field. */
    private lateinit var widthsView: StringProperty
    private var widthsViewChanges = 0

    /** Binding on the distance above the base line. */
    private lateinit var ascentView: StringProperty
    private var ascentViewChanges = 0

    /** Binding on the distance below the base line. */
    private lateinit var descentView: StringProperty
    private var descentViewChanges = 0

    /** Binding on the gap between two lines. */
    private lateinit var leadingView: StringProperty
    private var leadingViewChanges = 0

    @BeforeEach
    fun setUp() {
        holder = Holder(
            FontMetricsData(widths = "a1b2c3d4", ascent = 9.0, descent = 2.0, leading = 1.0)
        )
        parentEvents = 0

        property = FontMetricsDataProperty()
        // A parent property reports a change of this property as its own and writes an exchanged
        // fingerprint back into the object carrying it, which is what these two listeners stand for.
        property.addListener { parentEvents++ }
        property.addListener { _, _, newValue -> holder.metrics = newValue }
        // A parent property hands the nested object to this property as soon as that object arrives.
        property.set(holder.metrics)

        rootView = SimpleStringProperty()
        val rootBinding = Bindings.createStringBinding({ state(property.value) }, property)
        // A listener keeps the binding eager, so an invalidation that is never followed by a read
        // still shows up as a change.
        rootBinding.addListener { _, _, _ -> rootViewChanges++ }
        rootView.bind(rootBinding)

        widthsView = SimpleStringProperty()
        val widthsBinding = Bindings.createStringBinding(
            { property.widthsProperty.get() ?: MISSING },
            property.widthsProperty
        )
        widthsBinding.addListener { _, _, _ -> widthsViewChanges++ }
        widthsView.bind(widthsBinding)

        ascentView = SimpleStringProperty()
        val ascentBinding = property.ascentProperty.asString()
        ascentBinding.addListener { _, _, _ -> ascentViewChanges++ }
        ascentView.bind(ascentBinding)

        descentView = SimpleStringProperty()
        val descentBinding = property.descentProperty.asString()
        descentBinding.addListener { _, _, _ -> descentViewChanges++ }
        descentView.bind(descentBinding)

        leadingView = SimpleStringProperty()
        val leadingBinding = property.leadingProperty.asString()
        leadingBinding.addListener { _, _, _ -> leadingViewChanges++ }
        leadingView.bind(leadingBinding)

        resetCounters()
    }

    private fun resetCounters() {
        parentEvents = 0
        rootViewChanges = 0
        widthsViewChanges = 0
        ascentViewChanges = 0
        descentViewChanges = 0
        leadingViewChanges = 0
    }

    /** Text form of the whole fingerprint, used as the value of the binding on the root. */
    private fun state(metrics: FontMetricsData?): String =
        "${metrics?.widths ?: MISSING}|${metrics?.ascent ?: 0.0}|${metrics?.descent ?: 0.0}|" +
            "${metrics?.leading ?: 0.0}"

    /**
     * Asserts that every binding of the object tree delivers the given state, so no view keeps the
     * value of a previous fingerprint or of a previous field value.
     */
    private fun assertTreeShows(widths: String?, ascent: Double, descent: Double, leading: Double) {
        assertEquals("${widths ?: MISSING}|$ascent|$descent|$leading", rootView.get()) {
            "the binding on the fingerprint delivers an outdated state"
        }
        assertEquals(widths ?: MISSING, widthsView.get()) {
            "the binding on the digest of the widths delivers an outdated value"
        }
        assertEquals(ascent.toString(), ascentView.get()) {
            "the binding on the distance above the base line delivers an outdated value"
        }
        assertEquals(descent.toString(), descentView.get()) {
            "the binding on the distance below the base line delivers an outdated value"
        }
        assertEquals(leading.toString(), leadingView.get()) {
            "the binding on the gap between two lines delivers an outdated value"
        }
    }

    /**
     * Use case: the project is read from its file before the user interface is built, so every binding
     * of the object tree delivers the fingerprint that already sits in the model object.
     */
    @Test
    fun readsInitialValuesFromModel() {
        assertTreeShows(widths = "a1b2c3d4", ascent = 9.0, descent = 2.0, leading = 1.0)
    }

    /**
     * Use case: the family is measured again on this machine, so the freshly taken digest lands in the
     * fingerprint object and both the binding on that field and the binding on the fingerprint show it.
     */
    @Test
    fun writesWidthsToModelAndNotifiesTree() {
        property.widths = "ffeeddcc"

        assertEquals("ffeeddcc", holder.metrics!!.widths)
        assertTreeShows(widths = "ffeeddcc", ascent = 9.0, descent = 2.0, leading = 1.0)
        assertTrue(widthsViewChanges > 0) { "the binding on the digest was not re-evaluated" }
        assertTrue(rootViewChanges > 0) { "the binding on the fingerprint was not re-evaluated" }
        assertTrue(parentEvents > 0) { "the parent property was not told about the change" }
    }

    /**
     * Use case: the digest is bound to the field the measuring step writes its result into, so every
     * digest that step produces reaches the fingerprint object and every binding above it shows it.
     */
    @Test
    fun writesBoundWidthsToModelAndNotifiesTree() {
        val source = SimpleStringProperty("11223344")
        property.widthsProperty.bind(source)

        source.set("ffeeddcc")

        assertEquals("ffeeddcc", holder.metrics!!.widths)
        assertTreeShows(widths = "ffeeddcc", ascent = 9.0, descent = 2.0, leading = 1.0)
        assertTrue(widthsViewChanges > 0) { "the binding on the digest was not re-evaluated" }
        assertTrue(rootViewChanges > 0) { "the binding on the fingerprint was not re-evaluated" }
        assertTrue(parentEvents > 0) { "the parent property was not told about the change" }
    }

    /**
     * Use case: the distance above the base line is taken again, so the new measure lands in the
     * fingerprint object and both the binding on that field and the binding on the fingerprint show it.
     */
    @Test
    fun writesAscentToModelAndNotifiesTree() {
        property.ascent = 10.5

        assertEquals(10.5, holder.metrics!!.ascent)
        assertTreeShows(widths = "a1b2c3d4", ascent = 10.5, descent = 2.0, leading = 1.0)
        assertTrue(ascentViewChanges > 0) { "the binding on the ascent was not re-evaluated" }
        assertTrue(rootViewChanges > 0) { "the binding on the fingerprint was not re-evaluated" }
        assertTrue(parentEvents > 0) { "the parent property was not told about the change" }
    }

    /**
     * Use case: the ascent is bound to the value the measuring step produces, so every measure that
     * step delivers reaches the fingerprint object and every binding above it shows it.
     */
    @Test
    fun writesBoundAscentToModelAndNotifiesTree() {
        val source = SimpleDoubleProperty(9.0)
        property.ascentProperty.bind(source)

        source.set(10.5)

        assertEquals(10.5, holder.metrics!!.ascent)
        assertTreeShows(widths = "a1b2c3d4", ascent = 10.5, descent = 2.0, leading = 1.0)
        assertTrue(ascentViewChanges > 0) { "the binding on the ascent was not re-evaluated" }
        assertTrue(rootViewChanges > 0) { "the binding on the fingerprint was not re-evaluated" }
        assertTrue(parentEvents > 0) { "the parent property was not told about the change" }
    }

    /**
     * Use case: the distance below the base line is taken again, so the new measure lands in the
     * fingerprint object and both the binding on that field and the binding on the fingerprint show it.
     */
    @Test
    fun writesDescentToModelAndNotifiesTree() {
        property.descent = 3.25

        assertEquals(3.25, holder.metrics!!.descent)
        assertTreeShows(widths = "a1b2c3d4", ascent = 9.0, descent = 3.25, leading = 1.0)
        assertTrue(descentViewChanges > 0) { "the binding on the descent was not re-evaluated" }
        assertTrue(rootViewChanges > 0) { "the binding on the fingerprint was not re-evaluated" }
        assertTrue(parentEvents > 0) { "the parent property was not told about the change" }
    }

    /**
     * Use case: the descent is bound to the value the measuring step produces, so every measure that
     * step delivers reaches the fingerprint object and every binding above it shows it.
     */
    @Test
    fun writesBoundDescentToModelAndNotifiesTree() {
        val source = SimpleDoubleProperty(2.0)
        property.descentProperty.bind(source)

        source.set(3.25)

        assertEquals(3.25, holder.metrics!!.descent)
        assertTreeShows(widths = "a1b2c3d4", ascent = 9.0, descent = 3.25, leading = 1.0)
        assertTrue(descentViewChanges > 0) { "the binding on the descent was not re-evaluated" }
        assertTrue(rootViewChanges > 0) { "the binding on the fingerprint was not re-evaluated" }
        assertTrue(parentEvents > 0) { "the parent property was not told about the change" }
    }

    /**
     * Use case: the gap the family asks for between two lines is taken again, so the new measure lands
     * in the fingerprint object and both bindings on it show it.
     */
    @Test
    fun writesLeadingToModelAndNotifiesTree() {
        property.leading = 1.75

        assertEquals(1.75, holder.metrics!!.leading)
        assertTreeShows(widths = "a1b2c3d4", ascent = 9.0, descent = 2.0, leading = 1.75)
        assertTrue(leadingViewChanges > 0) { "the binding on the leading was not re-evaluated" }
        assertTrue(rootViewChanges > 0) { "the binding on the fingerprint was not re-evaluated" }
        assertTrue(parentEvents > 0) { "the parent property was not told about the change" }
    }

    /**
     * Use case: the leading is bound to the value the measuring step produces, so every measure that
     * step delivers reaches the fingerprint object and every binding above it shows it.
     */
    @Test
    fun writesBoundLeadingToModelAndNotifiesTree() {
        val source = SimpleDoubleProperty(1.0)
        property.leadingProperty.bind(source)

        source.set(1.75)

        assertEquals(1.75, holder.metrics!!.leading)
        assertTreeShows(widths = "a1b2c3d4", ascent = 9.0, descent = 2.0, leading = 1.75)
        assertTrue(leadingViewChanges > 0) { "the binding on the leading was not re-evaluated" }
        assertTrue(rootViewChanges > 0) { "the binding on the fingerprint was not re-evaluated" }
        assertTrue(parentEvents > 0) { "the parent property was not told about the change" }
    }

    /**
     * Use case: a field of the fingerprint is changed by application code past the property, so the
     * property is told to read the fingerprint again and every field property delivers the current
     * value afterwards.
     */
    @Test
    fun readsFieldsChangedOnModel() {
        holder.metrics!!.widths = "ffeeddcc"
        holder.metrics!!.ascent = 10.5
        holder.metrics!!.descent = 3.25
        holder.metrics!!.leading = 1.75

        property.refresh()

        assertEquals("ffeeddcc", property.widths)
        assertEquals(10.5, property.ascent)
        assertEquals(3.25, property.descent)
        assertEquals(1.75, property.leading)
        assertTreeShows(widths = "ffeeddcc", ascent = 10.5, descent = 3.25, leading = 1.75)
    }

    /**
     * Use case: the whole fingerprint is replaced - the family was measured anew on this machine - so
     * the field properties belong to another object afterwards and every binding of the object tree
     * shows the values of that object instead of the previous ones.
     */
    @Test
    fun writesReplacedMetricsToModelAndNotifiesWholeTree() {
        property.value = FontMetricsData(
            widths = "ffeeddcc",
            ascent = 10.5,
            descent = 3.25,
            leading = 1.75
        )

        assertEquals(FontMetricsData("ffeeddcc", 10.5, 3.25, 1.75), holder.metrics)
        assertTreeShows(widths = "ffeeddcc", ascent = 10.5, descent = 3.25, leading = 1.75)
        assertTrue(widthsViewChanges > 0) { "the binding on the digest was not re-evaluated" }
        assertTrue(ascentViewChanges > 0) { "the binding on the ascent was not re-evaluated" }
        assertTrue(descentViewChanges > 0) { "the binding on the descent was not re-evaluated" }
        assertTrue(leadingViewChanges > 0) { "the binding on the leading was not re-evaluated" }
        assertTrue(rootViewChanges > 0) { "the binding on the fingerprint was not re-evaluated" }
        assertTrue(parentEvents > 0) { "the parent property was not told about the change" }
    }

    /**
     * Use case: the fingerprint is exchanged for an object carrying the same measures - the family
     * measured exactly as before - so nothing the user interface shows changes and no field property
     * reports a change of its own.
     */
    @Test
    fun keepsFieldsQuietWhenReplacedMetricsCarryTheSameValues() {
        property.value = FontMetricsData(widths = "a1b2c3d4", ascent = 9.0, descent = 2.0, leading = 1.0)

        assertTreeShows(widths = "a1b2c3d4", ascent = 9.0, descent = 2.0, leading = 1.0)
        assertEquals(0, widthsViewChanges) { "the digest was reported as changed although it did not change" }
        assertEquals(0, ascentViewChanges) { "the ascent was reported as changed although it did not change" }
        assertEquals(0, descentViewChanges) { "the descent was reported as changed although it did not change" }
        assertEquals(0, leadingViewChanges) { "the leading was reported as changed although it did not change" }
    }

    /**
     * Use case: the family has never been measured, so every field property answers with a neutral
     * value and a view showing the fingerprint can be built nevertheless.
     */
    @Test
    fun readsNeutralValuesWhenMetricsAreAbsent() {
        property.value = null

        assertNull(property.widths)
        assertEquals(0.0, property.ascent)
        assertEquals(0.0, property.descent)
        assertEquals(0.0, property.leading)
        assertTreeShows(widths = null, ascent = 0.0, descent = 0.0, leading = 0.0)
    }

    /**
     * Use case: something writes into the property while no fingerprint sits behind it, so the values
     * are dropped instead of creating a fingerprint object nobody measured.
     */
    @Test
    fun dropsWritesWhenMetricsAreAbsent() {
        property.value = null

        property.widths = "ffeeddcc"
        property.ascent = 10.5
        property.descent = 3.25
        property.leading = 1.75

        assertNull(holder.metrics)
    }

    private companion object {
        /** Stands for a value the model object does not carry at all. */
        const val MISSING = "-"
    }
}

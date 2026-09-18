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
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleIntegerProperty
import javafx.beans.property.SimpleStringProperty
import javafx.beans.property.StringProperty
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.pcsoft.app.aighost.model.common.FontData

/**
 * Developer tests for [FontDataProperty].
 *
 * The property wraps the font of a piece of text and offers every field of that object as a property
 * of its own. Every test checks the object tree the way the user interface uses it: a binding hangs
 * on the font itself and on every single field, and the tests assert that a change reaches every
 * binding that has to know about it - upwards to the parent the property reports to as well as
 * downwards into the fields of an exchanged font.
 */
class FontDataPropertyTest {

    /** Stands for the model object carrying the font, the object a parent property writes into. */
    private class Holder(var font: FontData?)

    private lateinit var holder: Holder
    private lateinit var property: FontDataProperty

    /** Counts what the parent property is told, so the report up to the root becomes visible. */
    private var parentEvents = 0

    /** Binding on the whole font, standing for a view bound to the root of this object tree. */
    private lateinit var rootView: StringProperty
    private var rootViewChanges = 0

    /** Binding on the family name, standing for a view bound to that single field. */
    private lateinit var nameView: StringProperty
    private var nameViewChanges = 0

    /** Binding on the font size. */
    private lateinit var sizeView: StringProperty
    private var sizeViewChanges = 0

    /** Binding on the bold flag. */
    private lateinit var boldView: StringProperty
    private var boldViewChanges = 0

    /** Binding on the italic flag. */
    private lateinit var italicView: StringProperty
    private var italicViewChanges = 0

    /** Binding on the fingerprint. */
    private lateinit var fingerprintView: StringProperty
    private var fingerprintViewChanges = 0

    @BeforeEach
    fun setUp() {
        holder = Holder(
            FontData(
                name = "Times New Roman",
                size = 12,
                bold = false,
                italic = false,
                fingerprint = "v1|100.0|9.0|2.0|1.0,2.0"
            )
        )
        parentEvents = 0

        property = FontDataProperty()
        // A parent property reports a change of this property as its own and writes an exchanged font
        // back into the object carrying it, which is what these two listeners stand for.
        property.addListener { parentEvents++ }
        property.addListener { _, _, newValue -> holder.font = newValue }
        // A parent property hands the nested object to this property as soon as that object arrives.
        property.set(holder.font)

        rootView = SimpleStringProperty()
        val rootBinding = Bindings.createStringBinding({ state(property.value) }, property)
        // A listener keeps the binding eager, so an invalidation that is never followed by a read
        // still shows up as a change.
        rootBinding.addListener { _, _, _ -> rootViewChanges++ }
        rootView.bind(rootBinding)

        nameView = SimpleStringProperty()
        val nameBinding = Bindings.createStringBinding(
            { property.nameProperty.get() ?: MISSING },
            property.nameProperty
        )
        nameBinding.addListener { _, _, _ -> nameViewChanges++ }
        nameView.bind(nameBinding)

        sizeView = SimpleStringProperty()
        val sizeBinding = property.sizeProperty.asString()
        sizeBinding.addListener { _, _, _ -> sizeViewChanges++ }
        sizeView.bind(sizeBinding)

        boldView = SimpleStringProperty()
        val boldBinding = property.boldProperty.asString()
        boldBinding.addListener { _, _, _ -> boldViewChanges++ }
        boldView.bind(boldBinding)

        italicView = SimpleStringProperty()
        val italicBinding = property.italicProperty.asString()
        italicBinding.addListener { _, _, _ -> italicViewChanges++ }
        italicView.bind(italicBinding)

        fingerprintView = SimpleStringProperty()
        val fingerprintBinding = Bindings.createStringBinding(
            { property.fingerprintProperty.get() ?: MISSING },
            property.fingerprintProperty
        )
        fingerprintBinding.addListener { _, _, _ -> fingerprintViewChanges++ }
        fingerprintView.bind(fingerprintBinding)

        resetCounters()
    }

    private fun resetCounters() {
        parentEvents = 0
        rootViewChanges = 0
        nameViewChanges = 0
        sizeViewChanges = 0
        boldViewChanges = 0
        italicViewChanges = 0
        fingerprintViewChanges = 0
    }

    /** The fingerprint the font of every test starts with. */
    private fun initialFingerprint(): String = "v1|100.0|9.0|2.0|1.0,2.0"

    /** Text form of the whole font, used as the value of the binding on the root. */
    private fun state(font: FontData?): String =
        "${font?.name ?: MISSING}|${font?.size ?: 0}|${font?.bold ?: false}|${font?.italic ?: false}|" +
                (font?.fingerprint ?: MISSING)

    /**
     * Asserts that every binding of the object tree delivers the given state, so no view keeps the
     * value of a previous font or of a previous field value.
     */
    private fun assertTreeShows(
        name: String?,
        size: Int,
        bold: Boolean,
        italic: Boolean,
        fingerprint: String? = initialFingerprint()
    ) {
        assertEquals("${name ?: MISSING}|$size|$bold|$italic|${fingerprint ?: MISSING}", rootView.get()) {
            "the binding on the font delivers an outdated state"
        }
        assertEquals(name ?: MISSING, nameView.get()) {
            "the binding on the family name delivers an outdated value"
        }
        assertEquals(size.toString(), sizeView.get()) {
            "the binding on the font size delivers an outdated value"
        }
        assertEquals(bold.toString(), boldView.get()) {
            "the binding on the bold flag delivers an outdated value"
        }
        assertEquals(italic.toString(), italicView.get()) {
            "the binding on the italic flag delivers an outdated value"
        }
        assertEquals(fingerprint ?: MISSING, fingerprintView.get()) {
            "the binding on the fingerprint delivers an outdated value"
        }
    }

    /**
     * Use case: the project is read from its file before the user interface is built, so every binding
     * of the object tree delivers the font that already sits in the model object.
     */
    @Test
    fun readsInitialValuesFromModel() {
        assertTreeShows(name = "Times New Roman", size = 12, bold = false, italic = false)
    }

    /**
     * Use case: the user picks another type face in the font dialog, so the chosen family name lands in
     * the font object and both the binding on that field and the binding on the font show it.
     */
    @Test
    fun writesNameToModelAndNotifiesTree() {
        property.name = "Georgia"

        assertEquals("Georgia", holder.font?.name)
        assertTreeShows(name = "Georgia", size = 12, bold = false, italic = false)
        assertTrue(nameViewChanges > 0) { "the binding on the family name was not re-evaluated" }
        assertTrue(rootViewChanges > 0) { "the binding on the font was not re-evaluated" }
        assertTrue(parentEvents > 0) { "the parent property was not told about the change" }
    }

    /**
     * Use case: the family name is bound to the choice box of the font dialog, so every name that
     * choice box produces reaches the font object and every binding above it shows it.
     */
    @Test
    fun writesBoundNameToModelAndNotifiesTree() {
        val source = SimpleStringProperty("Garamond")
        property.nameProperty.bind(source)

        source.set("Georgia")

        assertEquals("Georgia", holder.font?.name)
        assertTreeShows(name = "Georgia", size = 12, bold = false, italic = false)
        assertTrue(nameViewChanges > 0) { "the binding on the family name was not re-evaluated" }
        assertTrue(rootViewChanges > 0) { "the binding on the font was not re-evaluated" }
        assertTrue(parentEvents > 0) { "the parent property was not told about the change" }
    }

    /**
     * Use case: the user enlarges the text, so the new point size lands in the font object and both the
     * binding on that field and the binding on the font show it.
     */
    @Test
    fun writesSizeToModelAndNotifiesTree() {
        property.size = 18

        assertEquals(18, holder.font?.size)
        assertTreeShows(name = "Times New Roman", size = 18, bold = false, italic = false)
        assertTrue(sizeViewChanges > 0) { "the binding on the font size was not re-evaluated" }
        assertTrue(rootViewChanges > 0) { "the binding on the font was not re-evaluated" }
        assertTrue(parentEvents > 0) { "the parent property was not told about the change" }
    }

    /**
     * Use case: the point size is bound to the spinner of the font dialog, so every number that spinner
     * produces reaches the font object and every binding above it shows it.
     */
    @Test
    fun writesBoundSizeToModelAndNotifiesTree() {
        val source = SimpleIntegerProperty(14)
        property.sizeProperty.bind(source)

        source.set(18)

        assertEquals(18, holder.font?.size)
        assertTreeShows(name = "Times New Roman", size = 18, bold = false, italic = false)
        assertTrue(sizeViewChanges > 0) { "the binding on the font size was not re-evaluated" }
        assertTrue(rootViewChanges > 0) { "the binding on the font was not re-evaluated" }
        assertTrue(parentEvents > 0) { "the parent property was not told about the change" }
    }

    /**
     * Use case: the user switches the text to a bold weight, so the flag lands in the font object and
     * both the binding on that field and the binding on the font show it.
     */
    @Test
    fun writesBoldToModelAndNotifiesTree() {
        property.bold = true

        assertEquals(true, holder.font?.bold)
        assertTreeShows(name = "Times New Roman", size = 12, bold = true, italic = false)
        assertTrue(boldViewChanges > 0) { "the binding on the bold flag was not re-evaluated" }
        assertTrue(rootViewChanges > 0) { "the binding on the font was not re-evaluated" }
        assertTrue(parentEvents > 0) { "the parent property was not told about the change" }
    }

    /**
     * Use case: the bold flag is bound to the toggle button of the font dialog, so every state that
     * button produces reaches the font object and every binding above it shows it.
     */
    @Test
    fun writesBoundBoldToModelAndNotifiesTree() {
        val source = SimpleBooleanProperty(false)
        property.boldProperty.bind(source)

        source.set(true)

        assertEquals(true, holder.font?.bold)
        assertTreeShows(name = "Times New Roman", size = 12, bold = true, italic = false)
        assertTrue(boldViewChanges > 0) { "the binding on the bold flag was not re-evaluated" }
        assertTrue(rootViewChanges > 0) { "the binding on the font was not re-evaluated" }
        assertTrue(parentEvents > 0) { "the parent property was not told about the change" }
    }

    /**
     * Use case: the user switches the text to a slanted face, so the flag lands in the font object and
     * both the binding on that field and the binding on the font show it.
     */
    @Test
    fun writesItalicToModelAndNotifiesTree() {
        property.italic = true

        assertEquals(true, holder.font?.italic)
        assertTreeShows(name = "Times New Roman", size = 12, bold = false, italic = true)
        assertTrue(italicViewChanges > 0) { "the binding on the italic flag was not re-evaluated" }
        assertTrue(rootViewChanges > 0) { "the binding on the font was not re-evaluated" }
        assertTrue(parentEvents > 0) { "the parent property was not told about the change" }
    }

    /**
     * Use case: the italic flag is bound to the toggle button of the font dialog, so every state that
     * button produces reaches the font object and every binding above it shows it.
     */
    @Test
    fun writesBoundItalicToModelAndNotifiesTree() {
        val source = SimpleBooleanProperty(false)
        property.italicProperty.bind(source)

        source.set(true)

        assertEquals(true, holder.font?.italic)
        assertTreeShows(name = "Times New Roman", size = 12, bold = false, italic = true)
        assertTrue(italicViewChanges > 0) { "the binding on the italic flag was not re-evaluated" }
        assertTrue(rootViewChanges > 0) { "the binding on the font was not re-evaluated" }
        assertTrue(parentEvents > 0) { "the parent property was not told about the change" }
    }

    /**
     * Use case: the family is measured on this machine, so the freshly taken and encoded fingerprint
     * lands in the font object and both the binding on that field and the binding on the font show it.
     */
    @Test
    fun writesFingerprintToModelAndNotifiesTree() {
        property.fingerprint = "v1|100.0|10.5|3.25|1.0,2.0"

        assertEquals("v1|100.0|10.5|3.25|1.0,2.0", holder.font?.fingerprint)
        assertTreeShows(
            name = "Times New Roman",
            size = 12,
            bold = false,
            italic = false,
            fingerprint = "v1|100.0|10.5|3.25|1.0,2.0"
        )
        assertTrue(fingerprintViewChanges > 0) { "the binding on the fingerprint was not re-evaluated" }
        assertTrue(rootViewChanges > 0) { "the binding on the font was not re-evaluated" }
        assertTrue(parentEvents > 0) { "the parent property was not told about the change" }
    }

    /**
     * Use case: the encoded fingerprint is bound to the value the measuring step produces, so every
     * fingerprint that step delivers reaches the font object and every binding above it shows it.
     */
    @Test
    fun writesBoundFingerprintToModelAndNotifiesTree() {
        val source = SimpleStringProperty("v1|100.0|9.0|2.0|1.0,2.0")
        property.fingerprintProperty.bind(source)

        source.set("v1|100.0|10.5|3.25|1.0,2.0")

        assertEquals("v1|100.0|10.5|3.25|1.0,2.0", holder.font?.fingerprint)
        assertTreeShows(
            name = "Times New Roman",
            size = 12,
            bold = false,
            italic = false,
            fingerprint = "v1|100.0|10.5|3.25|1.0,2.0"
        )
        assertTrue(fingerprintViewChanges > 0) { "the binding on the fingerprint was not re-evaluated" }
        assertTrue(rootViewChanges > 0) { "the binding on the font was not re-evaluated" }
        assertTrue(parentEvents > 0) { "the parent property was not told about the change" }
    }

    /**
     * Use case: the font of a project written before fingerprints existed carries none, so the field
     * property answers with a neutral value and the font dialog can be built nevertheless.
     */
    @Test
    fun readsNeutralValueWhenFingerprintIsAbsent() {
        property.fingerprint = null

        assertNull(holder.font?.fingerprint)
        assertTreeShows(
            name = "Times New Roman",
            size = 12,
            bold = false,
            italic = false,
            fingerprint = null
        )
    }

    /**
     * Use case: a field of the font is changed by application code past the property, so the property
     * is told to read the font again and every field property delivers the current value afterwards.
     */
    @Test
    fun readsFieldsChangedOnModel() {
        holder.font?.name = "Garamond"
        holder.font?.size = 16
        holder.font?.bold = true
        holder.font?.italic = true
        holder.font?.fingerprint = "v1|100.0|10.5|3.25|1.0,2.0"

        property.refresh()

        assertEquals("Garamond", property.name)
        assertEquals(16, property.size)
        assertTrue(property.bold)
        assertTrue(property.italic)
        assertEquals("v1|100.0|10.5|3.25|1.0,2.0", property.fingerprint)
        assertTreeShows(
            name = "Garamond",
            size = 16,
            bold = true,
            italic = true,
            fingerprint = "v1|100.0|10.5|3.25|1.0,2.0"
        )
    }

    /**
     * Use case: the whole font is replaced - the user applied another style - so every field property
     * belongs to the new object afterwards and every binding of the object tree shows its values
     * instead of the previous ones.
     */
    @Test
    fun writesReplacedFontToModelAndNotifiesWholeTree() {
        property.value = FontData(
            name = "Garamond",
            size = 16,
            bold = true,
            italic = true,
            fingerprint = "v1|100.0|10.5|3.25|1.0,2.0"
        )

        assertEquals(
            FontData("Garamond", 16, bold = true, italic = true, fingerprint = "v1|100.0|10.5|3.25|1.0,2.0"),
            holder.font
        )
        assertTreeShows(
            name = "Garamond",
            size = 16,
            bold = true,
            italic = true,
            fingerprint = "v1|100.0|10.5|3.25|1.0,2.0"
        )
        assertTrue(nameViewChanges > 0) { "the binding on the family name was not re-evaluated" }
        assertTrue(sizeViewChanges > 0) { "the binding on the font size was not re-evaluated" }
        assertTrue(boldViewChanges > 0) { "the binding on the bold flag was not re-evaluated" }
        assertTrue(italicViewChanges > 0) { "the binding on the italic flag was not re-evaluated" }
        assertTrue(fingerprintViewChanges > 0) { "the binding on the fingerprint was not re-evaluated" }
        assertTrue(rootViewChanges > 0) { "the binding on the font was not re-evaluated" }
        assertTrue(parentEvents > 0) { "the parent property was not told about the change" }
    }

    /**
     * Use case: the font is exchanged for an object carrying the same values - the same fingerprint
     * included - so nothing the user interface shows changes and no field property reports a change of
     * its own.
     */
    @Test
    fun keepsFieldsQuietWhenReplacedFontCarriesTheSameValues() {
        property.value = FontData(
            name = "Times New Roman",
            size = 12,
            bold = false,
            italic = false,
            fingerprint = initialFingerprint()
        )

        assertTreeShows(name = "Times New Roman", size = 12, bold = false, italic = false)
        assertEquals(0, nameViewChanges) { "the family name was reported as changed although it did not change" }
        assertEquals(0, sizeViewChanges) { "the font size was reported as changed although it did not change" }
        assertEquals(0, boldViewChanges) { "the bold flag was reported as changed although it did not change" }
        assertEquals(0, italicViewChanges) { "the italic flag was reported as changed although it did not change" }
        assertEquals(0, fingerprintViewChanges) { "the fingerprint was reported as changed although it did not change" }
    }

    /**
     * Use case: the object carrying the font does not exist at all - a style the user has not created
     * yet - so every field property answers with a neutral value and the font dialog can be built
     * nevertheless.
     */
    @Test
    fun readsNeutralValuesWhenFontIsAbsent() {
        property.value = null

        assertNull(property.name)
        assertEquals(0, property.size)
        assertFalse(property.bold)
        assertFalse(property.italic)
        assertNull(property.fingerprint)
        assertTreeShows(name = null, size = 0, bold = false, italic = false, fingerprint = null)
    }

    /**
     * Use case: the font dialog writes into the property while no font sits behind it, so the values
     * are dropped instead of creating a font object nobody asked for.
     */
    @Test
    fun dropsWritesWhenFontIsAbsent() {
        property.value = null

        property.name = "Georgia"
        property.size = 18
        property.bold = true
        property.italic = true
        property.fingerprint = "v1|100.0|10.5|3.25|1.0,2.0"

        assertNull(holder.font)
    }

    private companion object {
        /** Stands for a value the model object does not carry at all. */
        const val MISSING = "-"
    }
}

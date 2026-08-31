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
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.beans.property.StringProperty
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.pcsoft.app.aighost.model.common.Alignment
import org.pcsoft.app.aighost.model.common.FontData
import org.pcsoft.app.aighost.model.common.StyleData

/**
 * Developer tests for [StyleDataProperty].
 *
 * The property wraps the appearance of a piece of text and offers every field of that object - and
 * every field of the font nested in it - as a property of its own. Every test checks the object tree
 * the way the user interface uses it: a binding hangs on each property of the tree - the style itself,
 * the font below it and every single field - and the tests assert that a change reaches every binding
 * that has to know about it, upwards to the parent as well as downwards into the fields of an
 * exchanged object.
 */
class StyleDataPropertyTest {

    /** Stands for the model object carrying the style, the object a parent property writes into. */
    private class Holder(var style: StyleData?)

    private lateinit var holder: Holder
    private lateinit var property: StyleDataProperty

    /** Counts what the parent property is told, so the report up to the root becomes visible. */
    private var parentEvents = 0

    /** Binding on the whole style, standing for a view bound to the root of this object tree. */
    private lateinit var rootView: StringProperty
    private var rootViewChanges = 0

    /** Binding on the font, standing for a view bound to that nested object. */
    private lateinit var fontView: StringProperty
    private var fontViewChanges = 0

    /** Binding on the family name nested in the font. */
    private lateinit var nameView: StringProperty
    private var nameViewChanges = 0

    /** Binding on the font size nested in the font. */
    private lateinit var sizeView: StringProperty
    private var sizeViewChanges = 0

    /** Binding on the bold flag nested in the font. */
    private lateinit var boldView: StringProperty
    private var boldViewChanges = 0

    /** Binding on the italic flag nested in the font. */
    private lateinit var italicView: StringProperty
    private var italicViewChanges = 0

    /** Binding on the horizontal placement of the text. */
    private lateinit var alignmentView: StringProperty
    private var alignmentViewChanges = 0

    @BeforeEach
    fun setUp() {
        holder = Holder(
            StyleData(
                font = FontData(name = "Times New Roman", size = 12, bold = false, italic = false),
                alignment = Alignment.LEFT
            )
        )
        parentEvents = 0
        property = StyleDataProperty()
        // A parent property reports a change of a nested one as its own and writes an exchanged object
        // back into the one carrying it, which is what these two listeners stand for.
        property.addListener { _ -> parentEvents++ }
        property.addListener { _, _, newValue -> holder.style = newValue }
        // A parent property hands the nested object to this property as soon as that object arrives.
        property.set(holder.style)

        rootView = SimpleStringProperty()
        val rootBinding = Bindings.createStringBinding({ state(property.value) }, property)
        // A listener keeps the binding eager, so an invalidation that is never followed by a read
        // still shows up as a change.
        rootBinding.addListener { _, _, _ -> rootViewChanges++ }
        rootView.bind(rootBinding)

        fontView = SimpleStringProperty()
        val fontBinding = Bindings.createStringBinding(
            { state(property.fontProperty.value) },
            property.fontProperty
        )
        fontBinding.addListener { _, _, _ -> fontViewChanges++ }
        fontView.bind(fontBinding)

        nameView = SimpleStringProperty()
        val nameBinding = Bindings.createStringBinding(
            { property.fontProperty.nameProperty.get() ?: MISSING },
            property.fontProperty.nameProperty
        )
        nameBinding.addListener { _, _, _ -> nameViewChanges++ }
        nameView.bind(nameBinding)

        sizeView = SimpleStringProperty()
        val sizeBinding = property.fontProperty.sizeProperty.asString()
        sizeBinding.addListener { _, _, _ -> sizeViewChanges++ }
        sizeView.bind(sizeBinding)

        boldView = SimpleStringProperty()
        val boldBinding = property.fontProperty.boldProperty.asString()
        boldBinding.addListener { _, _, _ -> boldViewChanges++ }
        boldView.bind(boldBinding)

        italicView = SimpleStringProperty()
        val italicBinding = property.fontProperty.italicProperty.asString()
        italicBinding.addListener { _, _, _ -> italicViewChanges++ }
        italicView.bind(italicBinding)

        alignmentView = SimpleStringProperty()
        val alignmentBinding = Bindings.createStringBinding(
            { property.alignmentProperty.get()?.toString() ?: MISSING },
            property.alignmentProperty
        )
        alignmentBinding.addListener { _, _, _ -> alignmentViewChanges++ }
        alignmentView.bind(alignmentBinding)

        resetCounters()
    }

    private fun resetCounters() {
        parentEvents = 0
        rootViewChanges = 0
        fontViewChanges = 0
        nameViewChanges = 0
        sizeViewChanges = 0
        boldViewChanges = 0
        italicViewChanges = 0
        alignmentViewChanges = 0
    }

    /** Text form of the whole style, used as the value of the binding on the root. */
    private fun state(style: StyleData?): String =
        "${state(style?.font)}|${style?.alignment ?: MISSING}"

    /**
     * Text form of the font, used as the value of the binding on that nested object. The fingerprint
     * of the family belongs to the font, so it is part of that text as well.
     */
    private fun state(font: FontData?): String =
        "${font?.name ?: MISSING}|${font?.size ?: 0}|${font?.bold ?: false}|${font?.italic ?: false}|" +
            "${font?.metrics?.widths ?: MISSING}|${font?.metrics?.ascent ?: 0.0}|" +
            "${font?.metrics?.descent ?: 0.0}|${font?.metrics?.leading ?: 0.0}"

    /**
     * Asserts that every binding of the object tree delivers the given state, so no view keeps the
     * value of a previous object or of a previous field value.
     *
     * No font of this test has ever been measured, so the fingerprint stays the neutral one throughout -
     * what a fingerprint does to the object tree is proven on the font itself.
     */
    private fun assertTreeShows(
        name: String?,
        size: Int,
        bold: Boolean,
        italic: Boolean,
        alignment: Alignment?
    ) {
        val fontState = "${name ?: MISSING}|$size|$bold|$italic|$MISSING|0.0|0.0|0.0"

        assertEquals("$fontState|${alignment ?: MISSING}", rootView.get()) {
            "the binding on the style delivers an outdated state"
        }
        assertEquals(fontState, fontView.get()) {
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
        assertEquals((alignment ?: MISSING).toString(), alignmentView.get()) {
            "the binding on the placement delivers an outdated value"
        }
    }

    /**
     * Use case: the project is read from its file before the user interface is built, so every binding
     * of the object tree delivers the style that already sits in the model object.
     */
    @Test
    fun readsInitialValuesFromModel() {
        assertTreeShows("Times New Roman", 12, bold = false, italic = false, alignment = Alignment.LEFT)
    }

    /**
     * Use case: the user centres a heading, so the chosen placement lands in the style object and both
     * the binding on that field and the binding on the style show it.
     */
    @Test
    fun writesAlignmentToModelAndNotifiesTree() {
        property.alignment = Alignment.CENTER

        assertEquals(Alignment.CENTER, holder.style?.alignment)
        assertTreeShows("Times New Roman", 12, bold = false, italic = false, alignment = Alignment.CENTER)
        assertTrue(alignmentViewChanges > 0) { "the binding on the placement was not re-evaluated" }
        assertTrue(rootViewChanges > 0) { "the binding on the style was not re-evaluated" }
        assertTrue(parentEvents > 0) { "the parent property was not told about the change" }
    }

    /**
     * Use case: the placement is bound to the choice box of the style dialog, so every value that
     * choice box produces reaches the style object and every binding above it shows it.
     */
    @Test
    fun writesBoundAlignmentToModelAndNotifiesTree() {
        val source = SimpleObjectProperty<Alignment?>(Alignment.RIGHT)
        property.alignmentProperty.bind(source)

        source.set(Alignment.BLOCK)

        assertEquals(Alignment.BLOCK, holder.style?.alignment)
        assertTreeShows("Times New Roman", 12, bold = false, italic = false, alignment = Alignment.BLOCK)
        assertTrue(alignmentViewChanges > 0) { "the binding on the placement was not re-evaluated" }
        assertTrue(rootViewChanges > 0) { "the binding on the style was not re-evaluated" }
        assertTrue(parentEvents > 0) { "the parent property was not told about the change" }
    }

    /**
     * Use case: the user picks another type face for the text, so the family name lands in the font
     * nested in the style and the bindings on that field, on the font and on the style show it.
     */
    @Test
    fun writesNestedFontNameToModelAndNotifiesTree() {
        property.fontProperty.name = "Georgia"

        assertEquals("Georgia", holder.style?.font?.name)
        assertTreeShows("Georgia", 12, bold = false, italic = false, alignment = Alignment.LEFT)
        assertTrue(nameViewChanges > 0) { "the binding on the family name was not re-evaluated" }
        assertTrue(fontViewChanges > 0) { "the binding on the font was not re-evaluated" }
        assertTrue(rootViewChanges > 0) { "the binding on the style was not re-evaluated" }
        assertTrue(parentEvents > 0) { "the parent property was not told about the change" }
    }

    /**
     * Use case: the family name is bound to the choice box of the font dialog, so every name that
     * choice box produces reaches the nested font and every binding above it shows it.
     */
    @Test
    fun writesBoundNestedFontNameToModelAndNotifiesTree() {
        val source = SimpleStringProperty("Garamond")
        property.fontProperty.nameProperty.bind(source)

        source.set("Georgia")

        assertEquals("Georgia", holder.style?.font?.name)
        assertTreeShows("Georgia", 12, bold = false, italic = false, alignment = Alignment.LEFT)
        assertTrue(nameViewChanges > 0) { "the binding on the family name was not re-evaluated" }
        assertTrue(fontViewChanges > 0) { "the binding on the font was not re-evaluated" }
        assertTrue(rootViewChanges > 0) { "the binding on the style was not re-evaluated" }
        assertTrue(parentEvents > 0) { "the parent property was not told about the change" }
    }

    /**
     * Use case: the user enlarges the text, so the point size lands in the nested font and the bindings
     * on that field, on the font and on the style show it.
     */
    @Test
    fun writesNestedFontSizeToModelAndNotifiesTree() {
        val source = SimpleIntegerProperty(12)
        property.fontProperty.sizeProperty.bind(source)

        source.set(18)

        assertEquals(18, holder.style?.font?.size)
        assertTreeShows("Times New Roman", 18, bold = false, italic = false, alignment = Alignment.LEFT)
        assertTrue(sizeViewChanges > 0) { "the binding on the font size was not re-evaluated" }
        assertTrue(fontViewChanges > 0) { "the binding on the font was not re-evaluated" }
        assertTrue(rootViewChanges > 0) { "the binding on the style was not re-evaluated" }
        assertTrue(parentEvents > 0) { "the parent property was not told about the change" }
    }

    /**
     * Use case: the user switches the text to a bold weight, so the flag lands in the nested font and
     * the bindings on that field, on the font and on the style show it.
     */
    @Test
    fun writesNestedFontBoldToModelAndNotifiesTree() {
        val source = SimpleBooleanProperty(false)
        property.fontProperty.boldProperty.bind(source)

        source.set(true)

        assertEquals(true, holder.style?.font?.bold)
        assertTreeShows("Times New Roman", 12, bold = true, italic = false, alignment = Alignment.LEFT)
        assertTrue(boldViewChanges > 0) { "the binding on the bold flag was not re-evaluated" }
        assertTrue(fontViewChanges > 0) { "the binding on the font was not re-evaluated" }
        assertTrue(rootViewChanges > 0) { "the binding on the style was not re-evaluated" }
        assertTrue(parentEvents > 0) { "the parent property was not told about the change" }
    }

    /**
     * Use case: the user switches the text to a slanted face, so the flag lands in the nested font and
     * the bindings on that field, on the font and on the style show it.
     */
    @Test
    fun writesNestedFontItalicToModelAndNotifiesTree() {
        property.fontProperty.italic = true

        assertEquals(true, holder.style?.font?.italic)
        assertTreeShows("Times New Roman", 12, bold = false, italic = true, alignment = Alignment.LEFT)
        assertTrue(italicViewChanges > 0) { "the binding on the italic flag was not re-evaluated" }
        assertTrue(fontViewChanges > 0) { "the binding on the font was not re-evaluated" }
        assertTrue(rootViewChanges > 0) { "the binding on the style was not re-evaluated" }
        assertTrue(parentEvents > 0) { "the parent property was not told about the change" }
    }

    /**
     * Use case: the whole font of the style is replaced, so the field properties below it belong to
     * another object afterwards and every binding of the object tree shows the values of that object
     * instead of the previous ones.
     */
    @Test
    fun writesFontToModelAndNotifiesTree() {
        property.font = FontData(name = "Garamond", size = 16, bold = true, italic = true)

        assertEquals(FontData("Garamond", 16, bold = true, italic = true), holder.style?.font)
        assertTreeShows("Garamond", 16, bold = true, italic = true, alignment = Alignment.LEFT)
        assertTrue(nameViewChanges > 0) { "the binding on the family name was not re-evaluated" }
        assertTrue(sizeViewChanges > 0) { "the binding on the font size was not re-evaluated" }
        assertTrue(boldViewChanges > 0) { "the binding on the bold flag was not re-evaluated" }
        assertTrue(italicViewChanges > 0) { "the binding on the italic flag was not re-evaluated" }
        assertTrue(fontViewChanges > 0) { "the binding on the font was not re-evaluated" }
        assertTrue(rootViewChanges > 0) { "the binding on the style was not re-evaluated" }
        assertTrue(parentEvents > 0) { "the parent property was not told about the change" }
    }

    /**
     * Use case: a field of the style or of the font nested in it is changed by application code past
     * the property, so the property is told to read the style again and every field property delivers
     * the current value afterwards - down into the font, which nothing else would reach.
     */
    @Test
    fun readsFieldsChangedOnModel() {
        holder.style?.alignment = Alignment.RIGHT
        holder.style?.font?.name = "Garamond"
        holder.style?.font?.size = 16
        holder.style?.font?.bold = true
        holder.style?.font?.italic = true

        property.refresh()

        assertEquals(Alignment.RIGHT, property.alignment)
        assertEquals("Garamond", property.fontProperty.name)
        assertEquals(16, property.fontProperty.size)
        assertTrue(property.fontProperty.bold)
        assertTrue(property.fontProperty.italic)
    }

    /**
     * Use case: the whole style is replaced - another style was applied to the text - so every property
     * of the object tree belongs to another object afterwards and every binding shows the values of
     * that object instead of the previous ones.
     */
    @Test
    fun writesReplacedStyleToModelAndNotifiesWholeTree() {
        property.value = StyleData(
            font = FontData(name = "Garamond", size = 16, bold = true, italic = true),
            alignment = Alignment.BLOCK
        )

        assertTreeShows("Garamond", 16, bold = true, italic = true, alignment = Alignment.BLOCK)
        assertTrue(nameViewChanges > 0) { "the binding on the family name was not re-evaluated" }
        assertTrue(sizeViewChanges > 0) { "the binding on the font size was not re-evaluated" }
        assertTrue(boldViewChanges > 0) { "the binding on the bold flag was not re-evaluated" }
        assertTrue(italicViewChanges > 0) { "the binding on the italic flag was not re-evaluated" }
        assertTrue(alignmentViewChanges > 0) { "the binding on the placement was not re-evaluated" }
        assertTrue(fontViewChanges > 0) { "the binding on the font was not re-evaluated" }
        assertTrue(rootViewChanges > 0) { "the binding on the style was not re-evaluated" }
    }

    /**
     * Use case: the style is exchanged for an object carrying the same values, so nothing the user
     * interface shows changes and no field property reports a change of its own.
     */
    @Test
    fun keepsFieldsQuietWhenReplacedStyleCarriesTheSameValues() {
        property.value = StyleData(
            font = FontData(name = "Times New Roman", size = 12, bold = false, italic = false),
            alignment = Alignment.LEFT
        )

        assertTreeShows("Times New Roman", 12, bold = false, italic = false, alignment = Alignment.LEFT)
        assertEquals(0, nameViewChanges) { "the family name was reported as changed although it did not change" }
        assertEquals(0, sizeViewChanges) { "the font size was reported as changed although it did not change" }
        assertEquals(0, boldViewChanges) { "the bold flag was reported as changed although it did not change" }
        assertEquals(0, italicViewChanges) { "the italic flag was reported as changed although it did not change" }
        assertEquals(0, alignmentViewChanges) { "the placement was reported as changed although it did not change" }
        assertEquals(0, fontViewChanges) { "the font was reported as changed although it did not change" }
    }

    /**
     * Use case: the object carrying the style does not exist at all - no project is open yet - so every
     * field property answers with a neutral value and the style dialog can be built nevertheless.
     */
    @Test
    fun readsNeutralValuesWhenStyleIsAbsent() {
        property.value = null

        assertNull(property.font)
        assertNull(property.alignment)
        assertNull(property.fontProperty.name)
        assertEquals(0, property.fontProperty.size)
        assertFalse(property.fontProperty.bold)
        assertFalse(property.fontProperty.italic)
        assertTreeShows(null, 0, bold = false, italic = false, alignment = null)
    }

    /**
     * Use case: the style dialog writes into the property while no style sits behind it, so the values
     * are dropped instead of creating a style object nobody asked for.
     */
    @Test
    fun dropsWritesWhenStyleIsAbsent() {
        property.value = null

        property.alignment = Alignment.CENTER
        property.font = FontData(name = "Georgia")
        property.fontProperty.name = "Garamond"

        assertNull(holder.style)
    }

    private companion object {
        /** Stands for a value the model object does not carry at all. */
        const val MISSING = "-"
    }
}

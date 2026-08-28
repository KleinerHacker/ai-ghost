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

package org.pcsoft.app.aighost.fx.model.project.book

import javafx.beans.binding.Bindings
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.beans.property.StringProperty
import javafx.collections.FXCollections
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.pcsoft.app.aighost.model.project.book.Blurb

/**
 * Developer tests for [BlurbProperty].
 *
 * The property wraps the blurb of a book and offers its text as a property of its own. Every test
 * checks the object tree the way the user interface uses it: a binding hangs on the blurb itself and
 * on its paragraphs, and the tests assert that a change reaches every binding that has to know about
 * it - upwards to the parent the property reports to as well as downwards into the field of an
 * exchanged blurb. A book carries a blurb only after the user created it, so the behaviour without any
 * blurb is checked as well.
 */
class BlurbPropertyTest {

    /** Stands for the book carrying the blurb, the object a parent property writes into. */
    private class Holder(var blurb: Blurb?)

    private lateinit var holder: Holder
    private lateinit var property: BlurbProperty

    /** Counts what the parent property is told, so the report up to the root becomes visible. */
    private var parentEvents = 0

    /** Binding on the whole blurb, standing for a view bound to the root of this object tree. */
    private lateinit var rootView: StringProperty
    private var rootViewChanges = 0

    /** Binding on the paragraphs of the blurb. */
    private lateinit var paragraphView: StringProperty
    private var paragraphViewChanges = 0

    @BeforeEach
    fun setUp() {
        holder = Holder(Blurb(paragraph = listOf("A story about a long journey.")))
        parentEvents = 0
        property = BlurbProperty(
            { holder.blurb = it },
            { holder.blurb },
            { parentEvents++ }
        )
        // A parent property aligns a nested one with the model object as soon as that object arrives,
        // so the same alignment happens here before the views are built.
        property.refresh()

        rootView = SimpleStringProperty()
        val rootBinding = Bindings.createStringBinding({ state(property.value) }, property)
        // A listener keeps the binding eager, so an invalidation that is never followed by a read
        // still shows up as a change.
        rootBinding.addListener { _, _, _ -> rootViewChanges++ }
        rootView.bind(rootBinding)

        paragraphView = SimpleStringProperty()
        val paragraphBinding = Bindings.createStringBinding(
            { property.paragraphProperty.joinToString(";") },
            property.paragraphProperty
        )
        paragraphBinding.addListener { _, _, _ -> paragraphViewChanges++ }
        paragraphView.bind(paragraphBinding)

        resetCounters()
    }

    private fun resetCounters() {
        parentEvents = 0
        rootViewChanges = 0
        paragraphViewChanges = 0
    }

    /** Text form of the whole blurb, used as the value of the binding on the root. */
    private fun state(blurb: Blurb?): String = blurb?.paragraph.orEmpty().joinToString(";")

    /**
     * Asserts that every binding of the object tree delivers the given state, so no view keeps the
     * paragraphs of a previous blurb.
     */
    private fun assertTreeShows(paragraph: List<String>) {
        val paragraphText = paragraph.joinToString(";")

        assertEquals(paragraphText, rootView.get()) {
            "the binding on the blurb delivers an outdated state"
        }
        assertEquals(paragraphText, paragraphView.get()) {
            "the binding on the paragraphs delivers outdated paragraphs"
        }
    }

    /**
     * Use case: the project is read from its file before the user interface is built, so every binding
     * of the object tree delivers the blurb that already sits in the model object.
     */
    @Test
    fun readsInitialValuesFromModel() {
        assertTreeShows(listOf("A story about a long journey."))
    }

    /**
     * Use case: the user writes a further paragraph into the blurb, so the content change alone reaches
     * the model object and both the binding on that field and the binding on the blurb show it.
     */
    @Test
    fun writesParagraphEntryAddedToModelAndNotifiesTree() {
        property.paragraphProperty.add("For everyone who ever left home.")

        assertEquals(
            listOf("A story about a long journey.", "For everyone who ever left home."),
            holder.blurb?.paragraph
        )
        assertTreeShows(listOf("A story about a long journey.", "For everyone who ever left home."))
        assertTrue(paragraphViewChanges > 0) { "the binding on the paragraphs was not re-evaluated" }
        assertTrue(rootViewChanges > 0) { "the binding on the blurb was not re-evaluated" }
        assertTrue(parentEvents > 0) { "the parent property was not told about the change" }
    }

    /**
     * Use case: the paragraphs are written through the property itself, so the whole text reaches the
     * model object and every binding above it shows it.
     */
    @Test
    fun writesParagraphToModelAndNotifiesTree() {
        property.paragraph = listOf("For everyone who ever left home.")

        assertEquals(listOf("For everyone who ever left home."), holder.blurb?.paragraph)
        assertTreeShows(listOf("For everyone who ever left home."))
        assertTrue(paragraphViewChanges > 0) { "the binding on the paragraphs was not re-evaluated" }
        assertTrue(rootViewChanges > 0) { "the binding on the blurb was not re-evaluated" }
        assertTrue(parentEvents > 0) { "the parent property was not told about the change" }
    }

    /**
     * Use case: the paragraphs are filled from a binding - the text editor hands over its content - so
     * every list that binding produces reaches the model object and every binding above it shows it.
     */
    @Test
    fun writesBoundParagraphToModelAndNotifiesTree() {
        val source = SimpleObjectProperty(FXCollections.observableArrayList("A first line."))
        property.paragraphProperty.bind(source)

        source.set(FXCollections.observableArrayList("For everyone who ever left home."))

        assertEquals(listOf("For everyone who ever left home."), holder.blurb?.paragraph)
        assertTreeShows(listOf("For everyone who ever left home."))
        assertTrue(paragraphViewChanges > 0) { "the binding on the paragraphs was not re-evaluated" }
        assertTrue(rootViewChanges > 0) { "the binding on the blurb was not re-evaluated" }
        assertTrue(parentEvents > 0) { "the parent property was not told about the change" }
    }

    /**
     * Use case: the text of the blurb is changed by application code past the property, so the field
     * property delivers the current paragraphs instead of a cached copy.
     */
    @Test
    fun readsFieldsChangedOnModel() {
        holder.blurb?.paragraph = listOf("For everyone who ever left home.")

        assertEquals(listOf("For everyone who ever left home."), property.paragraph)
    }

    /**
     * Use case: the whole blurb is replaced - another project file was loaded - so the field property
     * belongs to another object afterwards and every binding of the object tree shows the paragraphs of
     * that object instead of the previous ones.
     */
    @Test
    fun writesReplacedBlurbToModelAndNotifiesWholeTree() {
        property.value = Blurb(paragraph = listOf("For everyone who ever left home."))

        assertEquals(Blurb(listOf("For everyone who ever left home.")), holder.blurb)
        assertTreeShows(listOf("For everyone who ever left home."))
        assertTrue(paragraphViewChanges > 0) { "the binding on the paragraphs was not re-evaluated" }
        assertTrue(rootViewChanges > 0) { "the binding on the blurb was not re-evaluated" }
        assertTrue(parentEvents > 0) { "the parent property was not told about the change" }
    }

    /**
     * Use case: the blurb is exchanged for an object carrying the same paragraphs, so nothing the user
     * interface shows changes and the field property reports no change of its own.
     */
    @Test
    fun keepsFieldsQuietWhenReplacedBlurbCarriesTheSameValues() {
        property.value = Blurb(paragraph = listOf("A story about a long journey."))

        assertTreeShows(listOf("A story about a long journey."))
        assertEquals(0, paragraphViewChanges) {
            "the paragraphs were reported as changed although they did not change"
        }
    }

    /**
     * Use case: the book carries no blurb at all because the user never created one, so the field
     * property answers with no paragraphs and the editor can be built nevertheless.
     */
    @Test
    fun readsNeutralValuesWhenBlurbIsAbsent() {
        property.value = null

        assertEquals(emptyList<String>(), property.paragraph)
        assertTreeShows(emptyList())
    }

    /**
     * Use case: the editor writes into the property while the book carries no blurb, so the paragraphs
     * are dropped instead of creating a blurb nobody asked for.
     */
    @Test
    fun dropsWritesWhenBlurbIsAbsent() {
        property.value = null

        property.paragraph = listOf("For everyone who ever left home.")

        assertNull(holder.blurb)
    }
}

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
import org.pcsoft.app.aighost.model.project.book.Prolog

/**
 * Developer tests for [PrologProperty].
 *
 * The property wraps the prolog of a book and offers every field of that object as a property of its
 * own. Every test checks the object tree the way the user interface uses it: a binding hangs on the
 * prolog itself and on every single field of it, and the tests assert that a change reaches every
 * binding that has to know about it - upwards to the parent the property reports to as well as
 * downwards into the fields of an exchanged prolog. A book carries a prolog only after the user
 * created it, so the behaviour without any prolog is checked as well.
 */
class PrologPropertyTest {

    /** Stands for the book carrying the prolog, the object a parent property writes into. */
    private class Holder(var prolog: Prolog?)

    private lateinit var holder: Holder
    private lateinit var property: PrologProperty

    /** Counts what the parent property is told, so the report up to the root becomes visible. */
    private var parentEvents = 0

    /** Binding on the whole prolog, standing for a view bound to the root of this object tree. */
    private lateinit var rootView: StringProperty
    private var rootViewChanges = 0

    /** Binding on the heading, standing for a view bound to that single field. */
    private lateinit var titleView: StringProperty
    private var titleViewChanges = 0

    /** Binding on the further heading lines. */
    private lateinit var titleAppendixView: StringProperty
    private var titleAppendixViewChanges = 0

    /** Binding on the paragraphs of the prolog. */
    private lateinit var paragraphView: StringProperty
    private var paragraphViewChanges = 0

    @BeforeEach
    fun setUp() {
        holder = Holder(
            Prolog(
                title = "Before the storm",
                titleAppendix = listOf("A short note"),
                paragraph = listOf("The night was calm.")
            )
        )
        parentEvents = 0
        property = PrologProperty(
            { holder.prolog = it },
            { holder.prolog },
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

        titleView = SimpleStringProperty()
        val titleBinding = Bindings.createStringBinding(
            { property.titleProperty.get() ?: MISSING },
            property.titleProperty
        )
        titleBinding.addListener { _, _, _ -> titleViewChanges++ }
        titleView.bind(titleBinding)

        titleAppendixView = SimpleStringProperty()
        val titleAppendixBinding = Bindings.createStringBinding(
            { property.titleAppendixProperty.joinToString(";") },
            property.titleAppendixProperty
        )
        titleAppendixBinding.addListener { _, _, _ -> titleAppendixViewChanges++ }
        titleAppendixView.bind(titleAppendixBinding)

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
        titleViewChanges = 0
        titleAppendixViewChanges = 0
        paragraphViewChanges = 0
    }

    /** Text form of the whole prolog, used as the value of the binding on the root. */
    private fun state(prolog: Prolog?): String =
        "${prolog?.title ?: MISSING}|${prolog?.titleAppendix.orEmpty().joinToString(";")}|" +
                prolog?.paragraph.orEmpty().joinToString(";")

    /**
     * Asserts that every binding of the object tree delivers the given state, so no view keeps the
     * value of a previous prolog or of a previous field value.
     */
    private fun assertTreeShows(title: String?, titleAppendix: List<String>, paragraph: List<String>) {
        val titleAppendixText = titleAppendix.joinToString(";")
        val paragraphText = paragraph.joinToString(";")

        assertEquals("${title ?: MISSING}|$titleAppendixText|$paragraphText", rootView.get()) {
            "the binding on the prolog delivers an outdated state"
        }
        assertEquals(title ?: MISSING, titleView.get()) {
            "the binding on the heading delivers an outdated value"
        }
        assertEquals(titleAppendixText, titleAppendixView.get()) {
            "the binding on the further heading lines delivers outdated lines"
        }
        assertEquals(paragraphText, paragraphView.get()) {
            "the binding on the paragraphs delivers outdated paragraphs"
        }
    }

    /**
     * Use case: the project is read from its file before the user interface is built, so every binding
     * of the object tree delivers the prolog that already sits in the model object.
     */
    @Test
    fun readsInitialValuesFromModel() {
        assertTreeShows("Before the storm", listOf("A short note"), listOf("The night was calm."))
    }

    /**
     * Use case: the user renames the heading of the prolog, so the text lands in the model object and
     * both the binding on that field and the binding on the prolog show it.
     */
    @Test
    fun writesTitleToModelAndNotifiesTree() {
        property.title = "After the storm"

        assertEquals("After the storm", holder.prolog?.title)
        assertTreeShows("After the storm", listOf("A short note"), listOf("The night was calm."))
        assertTrue(titleViewChanges > 0) { "the binding on the heading was not re-evaluated" }
        assertTrue(rootViewChanges > 0) { "the binding on the prolog was not re-evaluated" }
        assertTrue(parentEvents > 0) { "the parent property was not told about the change" }
    }

    /**
     * Use case: the heading is bound to the text field of the editor, so every text that field produces
     * reaches the model object and every binding above it shows it.
     */
    @Test
    fun writesBoundTitleToModelAndNotifiesTree() {
        val source = SimpleStringProperty("Draft heading")
        property.titleProperty.bind(source)

        source.set("After the storm")

        assertEquals("After the storm", holder.prolog?.title)
        assertTreeShows("After the storm", listOf("A short note"), listOf("The night was calm."))
        assertTrue(titleViewChanges > 0) { "the binding on the heading was not re-evaluated" }
        assertTrue(rootViewChanges > 0) { "the binding on the prolog was not re-evaluated" }
        assertTrue(parentEvents > 0) { "the parent property was not told about the change" }
    }

    /**
     * Use case: the user adds a further heading line below the title, so the content change alone
     * reaches the model object and every binding above it shows it.
     */
    @Test
    fun writesTitleAppendixEntryAddedToModelAndNotifiesTree() {
        property.titleAppendixProperty.add("Written in winter")

        assertEquals(listOf("A short note", "Written in winter"), holder.prolog?.titleAppendix)
        assertTreeShows(
            "Before the storm",
            listOf("A short note", "Written in winter"),
            listOf("The night was calm.")
        )
        assertTrue(titleAppendixViewChanges > 0) { "the binding on the further heading lines was not re-evaluated" }
        assertTrue(rootViewChanges > 0) { "the binding on the prolog was not re-evaluated" }
        assertTrue(parentEvents > 0) { "the parent property was not told about the change" }
    }

    /**
     * Use case: the further heading lines are filled from a binding, so every list that binding
     * produces reaches the model object and every binding above it shows it.
     */
    @Test
    fun writesBoundTitleAppendixToModelAndNotifiesTree() {
        val source = SimpleObjectProperty(FXCollections.observableArrayList("A first note"))
        property.titleAppendixProperty.bind(source)

        source.set(FXCollections.observableArrayList("Written in winter"))

        assertEquals(listOf("Written in winter"), holder.prolog?.titleAppendix)
        assertTreeShows("Before the storm", listOf("Written in winter"), listOf("The night was calm."))
        assertTrue(titleAppendixViewChanges > 0) { "the binding on the further heading lines was not re-evaluated" }
        assertTrue(rootViewChanges > 0) { "the binding on the prolog was not re-evaluated" }
        assertTrue(parentEvents > 0) { "the parent property was not told about the change" }
    }

    /**
     * Use case: the user writes a further paragraph into the prolog, so the content change alone
     * reaches the model object and every binding above it shows it.
     */
    @Test
    fun writesParagraphEntryAddedToModelAndNotifiesTree() {
        property.paragraphProperty.add("Then the wind came.")

        assertEquals(listOf("The night was calm.", "Then the wind came."), holder.prolog?.paragraph)
        assertTreeShows(
            "Before the storm",
            listOf("A short note"),
            listOf("The night was calm.", "Then the wind came.")
        )
        assertTrue(paragraphViewChanges > 0) { "the binding on the paragraphs was not re-evaluated" }
        assertTrue(rootViewChanges > 0) { "the binding on the prolog was not re-evaluated" }
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

        source.set(FXCollections.observableArrayList("Then the wind came."))

        assertEquals(listOf("Then the wind came."), holder.prolog?.paragraph)
        assertTreeShows("Before the storm", listOf("A short note"), listOf("Then the wind came."))
        assertTrue(paragraphViewChanges > 0) { "the binding on the paragraphs was not re-evaluated" }
        assertTrue(rootViewChanges > 0) { "the binding on the prolog was not re-evaluated" }
        assertTrue(parentEvents > 0) { "the parent property was not told about the change" }
    }

    /**
     * Use case: a field of the prolog is changed by application code past the property, so every field
     * property delivers the current value instead of a cached copy.
     */
    @Test
    fun readsFieldsChangedOnModel() {
        holder.prolog?.title = "After the storm"
        holder.prolog?.titleAppendix = listOf("Written in winter")
        holder.prolog?.paragraph = listOf("Then the wind came.")

        assertEquals("After the storm", property.title)
        assertEquals(listOf("Written in winter"), property.titleAppendix)
        assertEquals(listOf("Then the wind came."), property.paragraph)
    }

    /**
     * Use case: the whole prolog is replaced - another project file was loaded - so the field
     * properties belong to another object afterwards and every binding of the object tree shows the
     * values of that object instead of the previous ones.
     */
    @Test
    fun writesReplacedPrologToModelAndNotifiesWholeTree() {
        property.value = Prolog(
            title = "After the storm",
            titleAppendix = listOf("Written in winter"),
            paragraph = listOf("Then the wind came.")
        )

        assertEquals("After the storm", holder.prolog?.title)
        assertTreeShows("After the storm", listOf("Written in winter"), listOf("Then the wind came."))
        assertTrue(titleViewChanges > 0) { "the binding on the heading was not re-evaluated" }
        assertTrue(titleAppendixViewChanges > 0) { "the binding on the further heading lines was not re-evaluated" }
        assertTrue(paragraphViewChanges > 0) { "the binding on the paragraphs was not re-evaluated" }
        assertTrue(rootViewChanges > 0) { "the binding on the prolog was not re-evaluated" }
        assertTrue(parentEvents > 0) { "the parent property was not told about the change" }
    }

    /**
     * Use case: the prolog is exchanged for an object carrying the same values, so nothing the user
     * interface shows changes and no field property reports a change of its own.
     */
    @Test
    fun keepsFieldsQuietWhenReplacedPrologCarriesTheSameValues() {
        property.value = Prolog(
            title = "Before the storm",
            titleAppendix = listOf("A short note"),
            paragraph = listOf("The night was calm.")
        )

        assertTreeShows("Before the storm", listOf("A short note"), listOf("The night was calm."))
        assertEquals(0, titleViewChanges) { "the heading was reported as changed although it did not change" }
        assertEquals(0, titleAppendixViewChanges) {
            "the further heading lines were reported as changed although they did not change"
        }
        assertEquals(0, paragraphViewChanges) {
            "the paragraphs were reported as changed although they did not change"
        }
    }

    /**
     * Use case: the book carries no prolog at all because the user never created one, so every field
     * property answers with a neutral value and the editor can be built nevertheless.
     */
    @Test
    fun readsNeutralValuesWhenPrologIsAbsent() {
        property.value = null

        assertNull(property.title)
        assertEquals(emptyList<String>(), property.titleAppendix)
        assertEquals(emptyList<String>(), property.paragraph)
        assertTreeShows(null, emptyList(), emptyList())
    }

    /**
     * Use case: the editor writes into the property while the book carries no prolog, so the values are
     * dropped instead of creating a prolog nobody asked for.
     */
    @Test
    fun dropsWritesWhenPrologIsAbsent() {
        property.value = null

        property.title = "After the storm"
        property.titleAppendix = listOf("Written in winter")
        property.paragraph = listOf("Then the wind came.")

        assertNull(holder.prolog)
    }

    private companion object {
        /** Stands for a value the model object does not carry at all. */
        const val MISSING = "-"
    }
}

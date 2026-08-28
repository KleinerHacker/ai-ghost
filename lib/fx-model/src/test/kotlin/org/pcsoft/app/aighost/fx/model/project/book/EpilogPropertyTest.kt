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
import org.pcsoft.app.aighost.model.project.book.Epilog
import org.pcsoft.app.aighost.model.project.common.AIPrompt

/**
 * Developer tests for [EpilogProperty].
 *
 * The property wraps the epilog of a book and offers every field of that object as a property of its
 * own. Every test checks the object tree the way the user interface uses it: a binding hangs on the
 * epilog itself and on every single field of it, and the tests assert that a change reaches every
 * binding that has to know about it - upwards to the parent the property reports to as well as
 * downwards into the fields of an exchanged epilog. A book carries an epilog only after the user
 * created it, so the behaviour without any epilog is checked as well.
 */
class EpilogPropertyTest {

    /** Stands for the book carrying the epilog, the object a parent property writes into. */
    private class Holder(var epilog: Epilog?)

    private lateinit var holder: Holder
    private lateinit var property: EpilogProperty

    /** Counts what the parent property is told, so the report up to the root becomes visible. */
    private var parentEvents = 0

    /** Binding on the whole epilog, standing for a view bound to the root of this object tree. */
    private lateinit var rootView: StringProperty
    private var rootViewChanges = 0

    /** Binding on the heading, standing for a view bound to that single field. */
    private lateinit var titleView: StringProperty
    private var titleViewChanges = 0

    /** Binding on the further heading lines. */
    private lateinit var titleAppendixView: StringProperty
    private var titleAppendixViewChanges = 0

    /** Binding on the prompts the epilog is generated from. */
    private lateinit var promptsView: StringProperty
    private var promptsViewChanges = 0

    /** Binding on the paragraphs of the epilog. */
    private lateinit var paragraphView: StringProperty
    private var paragraphViewChanges = 0

    @BeforeEach
    fun setUp() {
        holder = Holder(
            Epilog(
                title = "What remains",
                titleAppendix = listOf("A last word"),
                prompts = INITIAL_PROMPTS,
                paragraph = listOf("The house stood empty.")
            )
        )
        parentEvents = 0
        property = EpilogProperty(
            { holder.epilog = it },
            { holder.epilog },
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

        promptsView = SimpleStringProperty()
        val promptsBinding = Bindings.createStringBinding(
            { promptText(property.promptsProperty.get()) },
            property.promptsProperty
        )
        promptsBinding.addListener { _, _, _ -> promptsViewChanges++ }
        promptsView.bind(promptsBinding)

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
        promptsViewChanges = 0
        paragraphViewChanges = 0
    }

    /** Text form of a prompt pair, used as the value of the binding on the prompts. */
    private fun promptText(prompts: AIPrompt?): String =
        "${prompts?.contentPrompt ?: MISSING}/${prompts?.stylePrompt ?: MISSING}"

    /** Text form of the whole epilog, used as the value of the binding on the root. */
    private fun state(epilog: Epilog?): String =
        "${epilog?.title ?: MISSING}|${epilog?.titleAppendix.orEmpty().joinToString(";")}|" +
                "${promptText(epilog?.prompts)}|" +
                epilog?.paragraph.orEmpty().joinToString(";")

    /**
     * Asserts that every binding of the object tree delivers the given state, so no view keeps the
     * value of a previous epilog or of a previous field value.
     */
    private fun assertTreeShows(
        title: String?,
        titleAppendix: List<String>,
        paragraph: List<String>,
        prompts: AIPrompt? = INITIAL_PROMPTS
    ) {
        val titleAppendixText = titleAppendix.joinToString(";")
        val paragraphText = paragraph.joinToString(";")
        val promptsText = promptText(prompts)

        assertEquals(
            "${title ?: MISSING}|$titleAppendixText|$promptsText|$paragraphText",
            rootView.get()
        ) {
            "the binding on the epilog delivers an outdated state"
        }
        assertEquals(title ?: MISSING, titleView.get()) {
            "the binding on the heading delivers an outdated value"
        }
        assertEquals(titleAppendixText, titleAppendixView.get()) {
            "the binding on the further heading lines delivers outdated lines"
        }
        assertEquals(promptsText, promptsView.get()) {
            "the binding on the prompts delivers outdated prompts"
        }
        assertEquals(paragraphText, paragraphView.get()) {
            "the binding on the paragraphs delivers outdated paragraphs"
        }
    }

    /**
     * Use case: the project is read from its file before the user interface is built, so every binding
     * of the object tree delivers the epilog that already sits in the model object.
     */
    @Test
    fun readsInitialValuesFromModel() {
        assertTreeShows("What remains", listOf("A last word"), listOf("The house stood empty."))
    }

    /**
     * Use case: the user renames the heading of the epilog, so the text lands in the model object and
     * both the binding on that field and the binding on the epilog show it.
     */
    @Test
    fun writesTitleToModelAndNotifiesTree() {
        property.title = "The years after"

        assertEquals("The years after", holder.epilog?.title)
        assertTreeShows("The years after", listOf("A last word"), listOf("The house stood empty."))
        assertTrue(titleViewChanges > 0) { "the binding on the heading was not re-evaluated" }
        assertTrue(rootViewChanges > 0) { "the binding on the epilog was not re-evaluated" }
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

        source.set("The years after")

        assertEquals("The years after", holder.epilog?.title)
        assertTreeShows("The years after", listOf("A last word"), listOf("The house stood empty."))
        assertTrue(titleViewChanges > 0) { "the binding on the heading was not re-evaluated" }
        assertTrue(rootViewChanges > 0) { "the binding on the epilog was not re-evaluated" }
        assertTrue(parentEvents > 0) { "the parent property was not told about the change" }
    }

    /**
     * Use case: the user adds a further heading line below the title, so the content change alone
     * reaches the model object and every binding above it shows it.
     */
    @Test
    fun writesTitleAppendixEntryAddedToModelAndNotifiesTree() {
        property.titleAppendixProperty.add("Written in spring")

        assertEquals(listOf("A last word", "Written in spring"), holder.epilog?.titleAppendix)
        assertTreeShows(
            "What remains",
            listOf("A last word", "Written in spring"),
            listOf("The house stood empty.")
        )
        assertTrue(titleAppendixViewChanges > 0) { "the binding on the further heading lines was not re-evaluated" }
        assertTrue(rootViewChanges > 0) { "the binding on the epilog was not re-evaluated" }
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

        source.set(FXCollections.observableArrayList("Written in spring"))

        assertEquals(listOf("Written in spring"), holder.epilog?.titleAppendix)
        assertTreeShows("What remains", listOf("Written in spring"), listOf("The house stood empty."))
        assertTrue(titleAppendixViewChanges > 0) { "the binding on the further heading lines was not re-evaluated" }
        assertTrue(rootViewChanges > 0) { "the binding on the epilog was not re-evaluated" }
        assertTrue(parentEvents > 0) { "the parent property was not told about the change" }
    }

    /**
     * Use case: the user describes what the epilog is about, so the single prompt field lands in the
     * model object and every binding above it shows it.
     */
    @Test
    fun writesContentPromptToModelAndNotifiesTree() {
        property.promptsProperty.contentPromptProperty.set("Tell what nobody expected.")

        assertEquals("Tell what nobody expected.", holder.epilog?.prompts?.contentPrompt)
        assertTreeShows(
            "What remains",
            listOf("A last word"),
            listOf("The house stood empty."),
            AIPrompt("Tell what nobody expected.", INITIAL_PROMPTS.stylePrompt)
        )
        assertTrue(promptsViewChanges > 0) { "the binding on the prompts was not re-evaluated" }
        assertTrue(rootViewChanges > 0) { "the binding on the epilog was not re-evaluated" }
        assertTrue(parentEvents > 0) { "the parent property was not told about the change" }
    }

    /**
     * Use case: the prompt editor is bound to the prompts of the epilog, so every prompt pair that
     * editor produces reaches the model object and every binding above it shows it.
     */
    @Test
    fun writesBoundPromptsToModelAndNotifiesTree() {
        val source = SimpleObjectProperty(AIPrompt("A first draft.", "Neutral."))
        property.promptsProperty.bind(source)

        source.set(AIPrompt("Tell what nobody expected.", "Dark and short."))

        assertEquals(
            AIPrompt("Tell what nobody expected.", "Dark and short."),
            holder.epilog?.prompts
        )
        assertTreeShows(
            "What remains",
            listOf("A last word"),
            listOf("The house stood empty."),
            AIPrompt("Tell what nobody expected.", "Dark and short.")
        )
        assertTrue(promptsViewChanges > 0) { "the binding on the prompts was not re-evaluated" }
        assertTrue(rootViewChanges > 0) { "the binding on the epilog was not re-evaluated" }
        assertTrue(parentEvents > 0) { "the parent property was not told about the change" }

        property.promptsProperty.unbind()
    }

    /**
     * Use case: the user writes a further paragraph into the epilog, so the content change alone
     * reaches the model object and every binding above it shows it.
     */
    @Test
    fun writesParagraphEntryAddedToModelAndNotifiesTree() {
        property.paragraphProperty.add("Nobody came back.")

        assertEquals(listOf("The house stood empty.", "Nobody came back."), holder.epilog?.paragraph)
        assertTreeShows(
            "What remains",
            listOf("A last word"),
            listOf("The house stood empty.", "Nobody came back.")
        )
        assertTrue(paragraphViewChanges > 0) { "the binding on the paragraphs was not re-evaluated" }
        assertTrue(rootViewChanges > 0) { "the binding on the epilog was not re-evaluated" }
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

        source.set(FXCollections.observableArrayList("Nobody came back."))

        assertEquals(listOf("Nobody came back."), holder.epilog?.paragraph)
        assertTreeShows("What remains", listOf("A last word"), listOf("Nobody came back."))
        assertTrue(paragraphViewChanges > 0) { "the binding on the paragraphs was not re-evaluated" }
        assertTrue(rootViewChanges > 0) { "the binding on the epilog was not re-evaluated" }
        assertTrue(parentEvents > 0) { "the parent property was not told about the change" }
    }

    /**
     * Use case: a field of the epilog is changed by application code past the property, so every field
     * property delivers the current value instead of a cached copy.
     */
    @Test
    fun readsFieldsChangedOnModel() {
        holder.epilog?.title = "The years after"
        holder.epilog?.titleAppendix = listOf("Written in spring")
        holder.epilog?.prompts = AIPrompt("Tell what nobody expected.", "Dark and short.")
        holder.epilog?.paragraph = listOf("Nobody came back.")

        assertEquals("The years after", property.title)
        assertEquals(listOf("Written in spring"), property.titleAppendix)
        assertEquals(AIPrompt("Tell what nobody expected.", "Dark and short."), property.prompts)
        assertEquals(listOf("Nobody came back."), property.paragraph)
    }

    /**
     * Use case: the whole epilog is replaced - another project file was loaded - so the field
     * properties belong to another object afterwards and every binding of the object tree shows the
     * values of that object instead of the previous ones.
     */
    @Test
    fun writesReplacedEpilogToModelAndNotifiesWholeTree() {
        property.value = Epilog(
            title = "The years after",
            titleAppendix = listOf("Written in spring"),
            prompts = AIPrompt("Tell what nobody expected.", "Dark and short."),
            paragraph = listOf("Nobody came back.")
        )

        assertEquals("The years after", holder.epilog?.title)
        assertTreeShows(
            "The years after",
            listOf("Written in spring"),
            listOf("Nobody came back."),
            AIPrompt("Tell what nobody expected.", "Dark and short.")
        )
        assertTrue(titleViewChanges > 0) { "the binding on the heading was not re-evaluated" }
        assertTrue(titleAppendixViewChanges > 0) { "the binding on the further heading lines was not re-evaluated" }
        assertTrue(promptsViewChanges > 0) { "the binding on the prompts was not re-evaluated" }
        assertTrue(paragraphViewChanges > 0) { "the binding on the paragraphs was not re-evaluated" }
        assertTrue(rootViewChanges > 0) { "the binding on the epilog was not re-evaluated" }
        assertTrue(parentEvents > 0) { "the parent property was not told about the change" }
    }

    /**
     * Use case: the epilog is exchanged for an object carrying the same values, so nothing the user
     * interface shows changes and no field property reports a change of its own.
     */
    @Test
    fun keepsFieldsQuietWhenReplacedEpilogCarriesTheSameValues() {
        property.value = Epilog(
            title = "What remains",
            titleAppendix = listOf("A last word"),
            prompts = INITIAL_PROMPTS,
            paragraph = listOf("The house stood empty.")
        )

        assertTreeShows("What remains", listOf("A last word"), listOf("The house stood empty."))
        assertEquals(0, titleViewChanges) { "the heading was reported as changed although it did not change" }
        assertEquals(0, titleAppendixViewChanges) {
            "the further heading lines were reported as changed although they did not change"
        }
        assertEquals(0, promptsViewChanges) {
            "the prompts were reported as changed although they did not change"
        }
        assertEquals(0, paragraphViewChanges) {
            "the paragraphs were reported as changed although they did not change"
        }
    }

    /**
     * Use case: the book carries no epilog at all because the user never created one, so every field
     * property answers with a neutral value and the editor can be built nevertheless.
     */
    @Test
    fun readsNeutralValuesWhenEpilogIsAbsent() {
        property.value = null

        assertNull(property.title)
        assertEquals(emptyList<String>(), property.titleAppendix)
        assertNull(property.prompts)
        assertEquals(emptyList<String>(), property.paragraph)
        assertTreeShows(null, emptyList(), emptyList(), null)
    }

    /**
     * Use case: the editor writes into the property while the book carries no epilog, so the values are
     * dropped instead of creating an epilog nobody asked for.
     */
    @Test
    fun dropsWritesWhenEpilogIsAbsent() {
        property.value = null

        property.title = "The years after"
        property.titleAppendix = listOf("Written in spring")
        property.prompts = AIPrompt("Tell what nobody expected.", "Dark and short.")
        property.paragraph = listOf("Nobody came back.")

        assertNull(holder.epilog)
    }

    private companion object {
        /** Stands for a value the model object does not carry at all. */
        const val MISSING = "-"

        /** The prompts every test starts from, so a changed pair shows up in an assertion. */
        val INITIAL_PROMPTS: AIPrompt
            get() = AIPrompt("Tell how everybody went on.", "Quiet and slow.")
    }
}

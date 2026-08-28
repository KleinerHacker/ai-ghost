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
import org.pcsoft.app.aighost.model.project.book.Chapter
import org.pcsoft.app.aighost.model.project.common.AIPrompt

/**
 * Developer tests for [ChapterProperty].
 *
 * The property wraps the chapter the user is working on and offers every field of that object as a
 * property of its own. Every test checks the object tree the way the user interface uses it: a binding
 * hangs on the chapter itself and on every single field of it, and the tests assert that a change
 * reaches every binding that has to know about it - upwards to the parent the property reports to as
 * well as downwards into the fields of an exchanged chapter. No chapter is picked while the project
 * tree is empty, so the behaviour without any chapter is checked as well.
 */
class ChapterPropertyTest {

    /** Stands for the object carrying the picked chapter, the object a parent property writes into. */
    private class Holder(var chapter: Chapter?)

    private lateinit var holder: Holder
    private lateinit var property: ChapterProperty

    /** Counts what the parent property is told, so the report up to the root becomes visible. */
    private var parentEvents = 0

    /** Binding on the whole chapter, standing for a view bound to the root of this object tree. */
    private lateinit var rootView: StringProperty
    private var rootViewChanges = 0

    /** Binding on the name shown in the project tree. */
    private lateinit var nameView: StringProperty
    private var nameViewChanges = 0

    /** Binding on the heading printed in the manuscript. */
    private lateinit var titleView: StringProperty
    private var titleViewChanges = 0

    /** Binding on the further heading lines. */
    private lateinit var titleAppendixView: StringProperty
    private var titleAppendixViewChanges = 0

    /** Binding on the prompts the chapter is generated from. */
    private lateinit var promptsView: StringProperty
    private var promptsViewChanges = 0

    /** Binding on the paragraphs of the chapter. */
    private lateinit var paragraphView: StringProperty
    private var paragraphViewChanges = 0

    @BeforeEach
    fun setUp() {
        holder = Holder(
            Chapter(
                name = "Chapter one",
                title = "The arrival",
                titleAppendix = listOf("An early morning"),
                prompts = INITIAL_PROMPTS,
                paragraph = listOf("The train was late.")
            )
        )
        parentEvents = 0
        property = ChapterProperty(
            { holder.chapter = it },
            { holder.chapter },
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

        nameView = SimpleStringProperty()
        val nameBinding = Bindings.createStringBinding(
            { property.nameProperty.get() ?: MISSING },
            property.nameProperty
        )
        nameBinding.addListener { _, _, _ -> nameViewChanges++ }
        nameView.bind(nameBinding)

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
        nameViewChanges = 0
        titleViewChanges = 0
        titleAppendixViewChanges = 0
        promptsViewChanges = 0
        paragraphViewChanges = 0
    }

    /** Text form of a prompt pair, used as the value of the binding on the prompts. */
    private fun promptText(prompts: AIPrompt?): String =
        "${prompts?.contentPrompt ?: MISSING}/${prompts?.stylePrompt ?: MISSING}"

    /** Text form of the whole chapter, used as the value of the binding on the root. */
    private fun state(chapter: Chapter?): String =
        "${chapter?.name ?: MISSING}|${chapter?.title ?: MISSING}|" +
                "${chapter?.titleAppendix.orEmpty().joinToString(";")}|" +
                "${promptText(chapter?.prompts)}|" +
                chapter?.paragraph.orEmpty().joinToString(";")

    /**
     * Asserts that every binding of the object tree delivers the given state, so no view keeps the
     * value of a previous chapter or of a previous field value.
     */
    private fun assertTreeShows(
        name: String?,
        title: String?,
        titleAppendix: List<String>,
        paragraph: List<String>,
        prompts: AIPrompt? = INITIAL_PROMPTS
    ) {
        val titleAppendixText = titleAppendix.joinToString(";")
        val paragraphText = paragraph.joinToString(";")
        val promptsText = promptText(prompts)

        assertEquals(
            "${name ?: MISSING}|${title ?: MISSING}|$titleAppendixText|$promptsText|$paragraphText",
            rootView.get()
        ) { "the binding on the chapter delivers an outdated state" }
        assertEquals(name ?: MISSING, nameView.get()) {
            "the binding on the name delivers an outdated value"
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
     * of the object tree delivers the chapter that already sits in the model object.
     */
    @Test
    fun readsInitialValuesFromModel() {
        assertTreeShows(
            "Chapter one",
            "The arrival",
            listOf("An early morning"),
            listOf("The train was late.")
        )
    }

    /**
     * Use case: the user renames the chapter in the project tree, so the name lands in the model object
     * and both the binding on that field and the binding on the chapter show it.
     */
    @Test
    fun writesNameToModelAndNotifiesTree() {
        property.name = "Chapter two"

        assertEquals("Chapter two", holder.chapter?.name)
        assertTreeShows(
            "Chapter two",
            "The arrival",
            listOf("An early morning"),
            listOf("The train was late.")
        )
        assertTrue(nameViewChanges > 0) { "the binding on the name was not re-evaluated" }
        assertTrue(rootViewChanges > 0) { "the binding on the chapter was not re-evaluated" }
        assertTrue(parentEvents > 0) { "the parent property was not told about the change" }
    }

    /**
     * Use case: the name is bound to the text field of the rename dialog, so every text that field
     * produces reaches the model object and every binding above it shows it.
     */
    @Test
    fun writesBoundNameToModelAndNotifiesTree() {
        val source = SimpleStringProperty("Draft name")
        property.nameProperty.bind(source)

        source.set("Chapter two")

        assertEquals("Chapter two", holder.chapter?.name)
        assertTreeShows(
            "Chapter two",
            "The arrival",
            listOf("An early morning"),
            listOf("The train was late.")
        )
        assertTrue(nameViewChanges > 0) { "the binding on the name was not re-evaluated" }
        assertTrue(rootViewChanges > 0) { "the binding on the chapter was not re-evaluated" }
        assertTrue(parentEvents > 0) { "the parent property was not told about the change" }
    }

    /**
     * Use case: the user renames the heading printed in the manuscript, so the text lands in the model
     * object and both the binding on that field and the binding on the chapter show it.
     */
    @Test
    fun writesTitleToModelAndNotifiesTree() {
        property.title = "The departure"

        assertEquals("The departure", holder.chapter?.title)
        assertTreeShows(
            "Chapter one",
            "The departure",
            listOf("An early morning"),
            listOf("The train was late.")
        )
        assertTrue(titleViewChanges > 0) { "the binding on the heading was not re-evaluated" }
        assertTrue(rootViewChanges > 0) { "the binding on the chapter was not re-evaluated" }
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

        source.set("The departure")

        assertEquals("The departure", holder.chapter?.title)
        assertTreeShows(
            "Chapter one",
            "The departure",
            listOf("An early morning"),
            listOf("The train was late.")
        )
        assertTrue(titleViewChanges > 0) { "the binding on the heading was not re-evaluated" }
        assertTrue(rootViewChanges > 0) { "the binding on the chapter was not re-evaluated" }
        assertTrue(parentEvents > 0) { "the parent property was not told about the change" }
    }

    /**
     * Use case: the user adds a further heading line below the title, so the content change alone
     * reaches the model object and every binding above it shows it.
     */
    @Test
    fun writesTitleAppendixEntryAddedToModelAndNotifiesTree() {
        property.titleAppendixProperty.add("In the rain")

        assertEquals(listOf("An early morning", "In the rain"), holder.chapter?.titleAppendix)
        assertTreeShows(
            "Chapter one",
            "The arrival",
            listOf("An early morning", "In the rain"),
            listOf("The train was late.")
        )
        assertTrue(titleAppendixViewChanges > 0) { "the binding on the further heading lines was not re-evaluated" }
        assertTrue(rootViewChanges > 0) { "the binding on the chapter was not re-evaluated" }
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

        source.set(FXCollections.observableArrayList("In the rain"))

        assertEquals(listOf("In the rain"), holder.chapter?.titleAppendix)
        assertTreeShows(
            "Chapter one",
            "The arrival",
            listOf("In the rain"),
            listOf("The train was late.")
        )
        assertTrue(titleAppendixViewChanges > 0) { "the binding on the further heading lines was not re-evaluated" }
        assertTrue(rootViewChanges > 0) { "the binding on the chapter was not re-evaluated" }
        assertTrue(parentEvents > 0) { "the parent property was not told about the change" }
    }

    /**
     * Use case: the user describes what the chapter is about, so the single prompt field lands in the
     * model object and every binding above it shows it.
     */
    @Test
    fun writesContentPromptToModelAndNotifiesTree() {
        property.promptsProperty.contentPromptProperty.set("Tell how the two finally met.")

        assertEquals("Tell how the two finally met.", holder.chapter?.prompts?.contentPrompt)
        assertTreeShows(
            "Chapter one",
            "The arrival",
            listOf("An early morning"),
            listOf("The train was late."),
            AIPrompt("Tell how the two finally met.", INITIAL_PROMPTS.stylePrompt)
        )
        assertTrue(promptsViewChanges > 0) { "the binding on the prompts was not re-evaluated" }
        assertTrue(rootViewChanges > 0) { "the binding on the chapter was not re-evaluated" }
        assertTrue(parentEvents > 0) { "the parent property was not told about the change" }
    }

    /**
     * Use case: the prompt editor is bound to the prompts of the chapter, so every prompt pair that
     * editor produces reaches the model object and every binding above it shows it.
     */
    @Test
    fun writesBoundPromptsToModelAndNotifiesTree() {
        val source = SimpleObjectProperty(AIPrompt("A first draft.", "Neutral."))
        property.promptsProperty.bind(source)

        source.set(AIPrompt("Tell how the two finally met.", "Dry and short."))

        assertEquals(
            AIPrompt("Tell how the two finally met.", "Dry and short."),
            holder.chapter?.prompts
        )
        assertTreeShows(
            "Chapter one",
            "The arrival",
            listOf("An early morning"),
            listOf("The train was late."),
            AIPrompt("Tell how the two finally met.", "Dry and short.")
        )
        assertTrue(promptsViewChanges > 0) { "the binding on the prompts was not re-evaluated" }
        assertTrue(rootViewChanges > 0) { "the binding on the chapter was not re-evaluated" }
        assertTrue(parentEvents > 0) { "the parent property was not told about the change" }

        property.promptsProperty.unbind()
    }

    /**
     * Use case: the user writes a further paragraph into the chapter, so the content change alone
     * reaches the model object and every binding above it shows it.
     */
    @Test
    fun writesParagraphEntryAddedToModelAndNotifiesTree() {
        property.paragraphProperty.add("Nobody was waiting.")

        assertEquals(listOf("The train was late.", "Nobody was waiting."), holder.chapter?.paragraph)
        assertTreeShows(
            "Chapter one",
            "The arrival",
            listOf("An early morning"),
            listOf("The train was late.", "Nobody was waiting.")
        )
        assertTrue(paragraphViewChanges > 0) { "the binding on the paragraphs was not re-evaluated" }
        assertTrue(rootViewChanges > 0) { "the binding on the chapter was not re-evaluated" }
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

        source.set(FXCollections.observableArrayList("Nobody was waiting."))

        assertEquals(listOf("Nobody was waiting."), holder.chapter?.paragraph)
        assertTreeShows(
            "Chapter one",
            "The arrival",
            listOf("An early morning"),
            listOf("Nobody was waiting.")
        )
        assertTrue(paragraphViewChanges > 0) { "the binding on the paragraphs was not re-evaluated" }
        assertTrue(rootViewChanges > 0) { "the binding on the chapter was not re-evaluated" }
        assertTrue(parentEvents > 0) { "the parent property was not told about the change" }
    }

    /**
     * Use case: a field of the chapter is changed by application code past the property, so every field
     * property delivers the current value instead of a cached copy.
     */
    @Test
    fun readsFieldsChangedOnModel() {
        holder.chapter?.name = "Chapter two"
        holder.chapter?.title = "The departure"
        holder.chapter?.titleAppendix = listOf("In the rain")
        holder.chapter?.prompts = AIPrompt("Tell how the two finally met.", "Dry and short.")
        holder.chapter?.paragraph = listOf("Nobody was waiting.")

        assertEquals("Chapter two", property.name)
        assertEquals("The departure", property.title)
        assertEquals(listOf("In the rain"), property.titleAppendix)
        assertEquals(AIPrompt("Tell how the two finally met.", "Dry and short."), property.prompts)
        assertEquals(listOf("Nobody was waiting."), property.paragraph)
    }

    /**
     * Use case: the user picks another chapter in the project tree, so the field properties belong to
     * another object afterwards and every binding of the object tree shows the values of that object
     * instead of the previous ones.
     */
    @Test
    fun writesReplacedChapterToModelAndNotifiesWholeTree() {
        property.value = Chapter(
            name = "Chapter two",
            title = "The departure",
            titleAppendix = listOf("In the rain"),
            prompts = AIPrompt("Tell how the two finally met.", "Dry and short."),
            paragraph = listOf("Nobody was waiting.")
        )

        assertEquals("Chapter two", holder.chapter?.name)
        assertTreeShows(
            "Chapter two",
            "The departure",
            listOf("In the rain"),
            listOf("Nobody was waiting."),
            AIPrompt("Tell how the two finally met.", "Dry and short.")
        )
        assertTrue(nameViewChanges > 0) { "the binding on the name was not re-evaluated" }
        assertTrue(promptsViewChanges > 0) { "the binding on the prompts was not re-evaluated" }
        assertTrue(titleViewChanges > 0) { "the binding on the heading was not re-evaluated" }
        assertTrue(titleAppendixViewChanges > 0) { "the binding on the further heading lines was not re-evaluated" }
        assertTrue(paragraphViewChanges > 0) { "the binding on the paragraphs was not re-evaluated" }
        assertTrue(rootViewChanges > 0) { "the binding on the chapter was not re-evaluated" }
        assertTrue(parentEvents > 0) { "the parent property was not told about the change" }
    }

    /**
     * Use case: the chapter is exchanged for an object carrying the same values, so nothing the user
     * interface shows changes and no field property reports a change of its own.
     */
    @Test
    fun keepsFieldsQuietWhenReplacedChapterCarriesTheSameValues() {
        property.value = Chapter(
            name = "Chapter one",
            title = "The arrival",
            titleAppendix = listOf("An early morning"),
            prompts = INITIAL_PROMPTS,
            paragraph = listOf("The train was late.")
        )

        assertTreeShows(
            "Chapter one",
            "The arrival",
            listOf("An early morning"),
            listOf("The train was late.")
        )
        assertEquals(0, nameViewChanges) { "the name was reported as changed although it did not change" }
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
     * Use case: no chapter is picked at all - the project tree carries none yet - so every field
     * property answers with a neutral value and the editor can be built nevertheless.
     */
    @Test
    fun readsNeutralValuesWhenChapterIsAbsent() {
        property.value = null

        assertNull(property.name)
        assertNull(property.title)
        assertEquals(emptyList<String>(), property.titleAppendix)
        assertNull(property.prompts)
        assertEquals(emptyList<String>(), property.paragraph)
        assertTreeShows(null, null, emptyList(), emptyList(), null)
    }

    /**
     * Use case: the editor writes into the property while no chapter is picked, so the values are
     * dropped instead of creating a chapter nobody asked for.
     */
    @Test
    fun dropsWritesWhenChapterIsAbsent() {
        property.value = null

        property.name = "Chapter two"
        property.title = "The departure"
        property.titleAppendix = listOf("In the rain")
        property.prompts = AIPrompt("Tell how the two finally met.", "Dry and short.")
        property.paragraph = listOf("Nobody was waiting.")

        assertNull(holder.chapter)
    }

    private companion object {
        /** Stands for a value the model object does not carry at all. */
        const val MISSING = "-"

        /** The prompts every test starts from, so a changed pair shows up in an assertion. */
        val INITIAL_PROMPTS: AIPrompt
            get() = AIPrompt("Tell how the journey started.", "Lively and warm.")
    }
}

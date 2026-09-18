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
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.pcsoft.app.aighost.model.project.book.Chapter
import org.pcsoft.app.aighost.model.project.common.AIPrompt
import java.util.UUID

/**
 * Developer tests for [ChapterProperty].
 *
 * The property wraps the chapter the user is working on and offers every field of that object as a
 * property of its own. Every test checks the object tree the way the user interface uses it: a binding
 * hangs on the chapter itself and on every single field of it, and the tests assert that a change
 * reaches every binding that has to know about it - upwards to the parent the property reports to as
 * well as downwards into the fields of an exchanged chapter. [Chapter.id] is a `val` with no setter and
 * is therefore never registered with `BeanFields` (see the fx-model skill); [idProperty] is proven
 * separately: it takes over the id of whatever chapter is bound and never changes on its own.
 */
class ChapterPropertyTest {

    /** Stands for the object carrying the picked chapter, the object a parent property writes into. */
    private class Holder(var chapter: Chapter?)

    private lateinit var holder: Holder
    private lateinit var property: ChapterProperty
    private lateinit var initialId: UUID

    /** Counts what the parent property is told, so the report up to the root becomes visible. */
    private var parentEvents = 0

    /** Binding on the whole chapter, standing for a view bound to the root of this object tree. */
    private lateinit var rootView: StringProperty
    private var rootViewChanges = 0

    /** Binding on the name shown in the project tree. */
    private lateinit var nameView: StringProperty
    private var nameViewChanges = 0

    /** Binding on the prompts the chapter is generated from. */
    private lateinit var promptsView: StringProperty
    private var promptsViewChanges = 0

    @BeforeEach
    fun setUp() {
        val chapter = Chapter(name = "Chapter one", prompts = INITIAL_PROMPTS)
        initialId = chapter.id
        holder = Holder(chapter)
        parentEvents = 0
        property = ChapterProperty()
        // A parent property reports a change of a nested one as its own and writes an exchanged object
        // back into the one carrying it, which is what these two listeners stand for.
        property.addListener { _ -> parentEvents++ }
        property.addListener { _, _, newValue -> holder.chapter = newValue }
        // A parent property hands the nested object to this property as soon as that object arrives.
        property.set(holder.chapter)

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

        promptsView = SimpleStringProperty()
        val promptsBinding = Bindings.createStringBinding(
            { promptText(property.promptsProperty.get()) },
            property.promptsProperty
        )
        promptsBinding.addListener { _, _, _ -> promptsViewChanges++ }
        promptsView.bind(promptsBinding)

        resetCounters()
    }

    private fun resetCounters() {
        parentEvents = 0
        rootViewChanges = 0
        nameViewChanges = 0
        promptsViewChanges = 0
    }

    /** Text form of a prompt pair, used as the value of the binding on the prompts. */
    private fun promptText(prompts: AIPrompt?): String =
        "${prompts?.contentPrompt ?: MISSING}/${prompts?.stylePrompt ?: MISSING}"

    /** Text form of the whole chapter, used as the value of the binding on the root. */
    private fun state(chapter: Chapter?): String =
        "${chapter?.name ?: MISSING}|${promptText(chapter?.prompts)}"

    /**
     * Asserts that every binding of the object tree delivers the given state, so no view keeps the
     * value of a previous chapter or of a previous field value.
     */
    private fun assertTreeShows(name: String?, prompts: AIPrompt? = INITIAL_PROMPTS) {
        val promptsText = promptText(prompts)

        assertEquals("${name ?: MISSING}|$promptsText", rootView.get()) {
            "the binding on the chapter delivers an outdated state"
        }
        assertEquals(name ?: MISSING, nameView.get()) {
            "the binding on the name delivers an outdated value"
        }
        assertEquals(promptsText, promptsView.get()) {
            "the binding on the prompts delivers outdated prompts"
        }
    }

    /**
     * Use case: the project is read from its file before the user interface is built, so every binding
     * of the object tree delivers the chapter that already sits in the model object.
     */
    @Test
    fun readsInitialValuesFromModel() {
        assertTreeShows("Chapter one")
        assertEquals(initialId, property.id)
    }

    /**
     * Use case: the user renames the chapter in the project tree, so the name lands in the model object
     * and both the binding on that field and the binding on the chapter show it.
     */
    @Test
    fun writesNameToModelAndNotifiesTree() {
        property.name = "Chapter two"

        assertEquals("Chapter two", holder.chapter?.name)
        assertTreeShows("Chapter two")
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
        assertTreeShows("Chapter two")
        assertTrue(nameViewChanges > 0) { "the binding on the name was not re-evaluated" }
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
        assertTreeShows("Chapter one", AIPrompt("Tell how the two finally met.", INITIAL_PROMPTS.stylePrompt))
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

        assertEquals(AIPrompt("Tell how the two finally met.", "Dry and short."), holder.chapter?.prompts)
        assertTreeShows("Chapter one", AIPrompt("Tell how the two finally met.", "Dry and short."))
        assertTrue(promptsViewChanges > 0) { "the binding on the prompts was not re-evaluated" }
        assertTrue(rootViewChanges > 0) { "the binding on the chapter was not re-evaluated" }
        assertTrue(parentEvents > 0) { "the parent property was not told about the change" }

        property.promptsProperty.unbind()
    }

    /**
     * Use case: a field of the chapter is changed by application code past the property, so the
     * property is told to read the chapter again and every field property delivers the current value
     * afterwards.
     */
    @Test
    fun readsFieldsChangedOnModel() {
        holder.chapter?.name = "Chapter two"
        holder.chapter?.prompts = AIPrompt("Tell how the two finally met.", "Dry and short.")

        property.refresh()

        assertEquals("Chapter two", property.name)
        assertEquals(AIPrompt("Tell how the two finally met.", "Dry and short."), property.prompts)
    }

    /**
     * Use case: the user picks another chapter in the project tree, so the field properties belong to
     * another object afterwards and every binding of the object tree shows the values of that object
     * instead of the previous ones - the id property follows as well.
     */
    @Test
    fun writesReplacedChapterToModelAndNotifiesWholeTree() {
        val replacement = Chapter(
            name = "Chapter two",
            prompts = AIPrompt("Tell how the two finally met.", "Dry and short.")
        )
        property.value = replacement

        assertEquals("Chapter two", holder.chapter?.name)
        assertTreeShows("Chapter two", AIPrompt("Tell how the two finally met.", "Dry and short."))
        assertEquals(replacement.id, property.id)
        assertTrue(nameViewChanges > 0) { "the binding on the name was not re-evaluated" }
        assertTrue(promptsViewChanges > 0) { "the binding on the prompts was not re-evaluated" }
        assertTrue(rootViewChanges > 0) { "the binding on the chapter was not re-evaluated" }
        assertTrue(parentEvents > 0) { "the parent property was not told about the change" }
    }

    /**
     * Use case: the chapter is exchanged for an object carrying the same values (a `copy()` with only
     * the name changed keeps the id, but two independently built chapters do not) - here the copy
     * carries the very same field values, so nothing the user interface shows changes and no field
     * property reports a change of its own.
     */
    @Test
    fun keepsFieldsQuietWhenReplacedChapterCarriesTheSameValues() {
        property.value = holder.chapter!!.copy()

        assertTreeShows("Chapter one")
        assertEquals(0, nameViewChanges) { "the name was reported as changed although it did not change" }
        assertEquals(0, promptsViewChanges) {
            "the prompts were reported as changed although they did not change"
        }
    }

    /**
     * Use case: the chapter is copied and only its name is changed, so [Chapter.id] carries over
     * unchanged - the stability [Chapter.id] promises once it was assigned.
     */
    @Test
    fun idStaysStableAcrossACopyThatChangesOtherFields() {
        val renamed = holder.chapter!!.copy(name = "Chapter two", prompts = AIPrompt("Changed.", "Changed."))

        assertEquals(initialId, renamed.id, "copying a chapter must not change its id")
    }

    /**
     * Use case: no chapter is picked at all - the project tree carries none yet - so every field
     * property answers with a neutral value and the editor can be built nevertheless.
     */
    @Test
    fun readsNeutralValuesWhenChapterIsAbsent() {
        property.value = null

        assertNull(property.name)
        assertNull(property.prompts)
        assertNull(property.id)
        assertTreeShows(null, null)
    }

    /**
     * Use case: the editor writes into the property while no chapter is picked, so the values are
     * dropped instead of creating a chapter nobody asked for.
     */
    @Test
    fun dropsWritesWhenChapterIsAbsent() {
        property.value = null

        property.name = "Chapter two"
        property.prompts = AIPrompt("Tell how the two finally met.", "Dry and short.")

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

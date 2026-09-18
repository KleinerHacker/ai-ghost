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
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.beans.property.StringProperty
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
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
 * downwards into the fields of an exchanged epilog. A book always carries its epilog, but the property
 * carries no object as long as no book sits behind the one standing for it, so that state is checked
 * as well.
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

    /** Binding on the prompts the epilog is generated from. */
    private lateinit var promptsView: StringProperty
    private var promptsViewChanges = 0

    /** Binding on the switch telling whether the epilog belongs to the book. */
    private lateinit var includedView: StringProperty
    private var includedViewChanges = 0

    @BeforeEach
    fun setUp() {
        holder = Holder(Epilog(prompts = INITIAL_PROMPTS))
        parentEvents = 0
        property = EpilogProperty()
        // A parent property reports a change of a nested one as its own and writes an exchanged object
        // back into the one carrying it, which is what these two listeners stand for.
        property.addListener { _ -> parentEvents++ }
        property.addListener { _, _, newValue -> holder.epilog = newValue }
        // A parent property hands the nested object to this property as soon as that object arrives.
        property.set(holder.epilog)

        rootView = SimpleStringProperty()
        val rootBinding = Bindings.createStringBinding({ state(property.value) }, property)
        // A listener keeps the binding eager, so an invalidation that is never followed by a read
        // still shows up as a change.
        rootBinding.addListener { _, _, _ -> rootViewChanges++ }
        rootView.bind(rootBinding)

        promptsView = SimpleStringProperty()
        val promptsBinding = Bindings.createStringBinding(
            { promptText(property.promptsProperty.get()) },
            property.promptsProperty
        )
        promptsBinding.addListener { _, _, _ -> promptsViewChanges++ }
        promptsView.bind(promptsBinding)

        includedView = SimpleStringProperty()
        val includedBinding = Bindings.createStringBinding(
            { property.includedProperty.get().toString() },
            property.includedProperty
        )
        includedBinding.addListener { _, _, _ -> includedViewChanges++ }
        includedView.bind(includedBinding)

        resetCounters()
    }

    private fun resetCounters() {
        parentEvents = 0
        rootViewChanges = 0
        promptsViewChanges = 0
        includedViewChanges = 0
    }

    /** Text form of a prompt pair, used as the value of the binding on the prompts. */
    private fun promptText(prompts: AIPrompt?): String =
        "${prompts?.contentPrompt ?: MISSING}/${prompts?.stylePrompt ?: MISSING}"

    /** Text form of the whole epilog, used as the value of the binding on the root. */
    private fun state(epilog: Epilog?): String =
        "${promptText(epilog?.prompts)}|${epilog?.included ?: false}"

    /**
     * Asserts that every binding of the object tree delivers the given state, so no view keeps the
     * value of a previous epilog or of a previous field value.
     */
    private fun assertTreeShows(prompts: AIPrompt? = INITIAL_PROMPTS, included: Boolean = false) {
        val promptsText = promptText(prompts)

        assertEquals("$promptsText|$included", rootView.get()) {
            "the binding on the epilog delivers an outdated state"
        }
        assertEquals(promptsText, promptsView.get()) {
            "the binding on the prompts delivers outdated prompts"
        }
        assertEquals(included.toString(), includedView.get()) {
            "the binding on the switch delivers an outdated value"
        }
    }

    /**
     * Use case: the project is read from its file before the user interface is built, so every binding
     * of the object tree delivers the epilog that already sits in the model object.
     */
    @Test
    fun readsInitialValuesFromModel() {
        assertTreeShows()
    }

    /**
     * Use case: the user describes what the epilog is about, so the single prompt field lands in the
     * model object and every binding above it shows it.
     */
    @Test
    fun writesContentPromptToModelAndNotifiesTree() {
        property.promptsProperty.contentPromptProperty.set("Tell what nobody expected.")

        assertEquals("Tell what nobody expected.", holder.epilog?.prompts?.contentPrompt)
        assertTreeShows(AIPrompt("Tell what nobody expected.", INITIAL_PROMPTS.stylePrompt))
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

        assertEquals(AIPrompt("Tell what nobody expected.", "Dark and short."), holder.epilog?.prompts)
        assertTreeShows(AIPrompt("Tell what nobody expected.", "Dark and short."))
        assertTrue(promptsViewChanges > 0) { "the binding on the prompts was not re-evaluated" }
        assertTrue(rootViewChanges > 0) { "the binding on the epilog was not re-evaluated" }
        assertTrue(parentEvents > 0) { "the parent property was not told about the change" }

        property.promptsProperty.unbind()
    }

    /**
     * Use case: the user takes the epilog into the book, so the switch lands in the model object and
     * both the binding on that field and the binding on the epilog show it.
     */
    @Test
    fun writesIncludedToModelAndNotifiesTree() {
        property.included = true

        assertEquals(true, holder.epilog?.included)
        assertTreeShows(included = true)
        assertTrue(includedViewChanges > 0) { "the binding on the switch was not re-evaluated" }
        assertTrue(rootViewChanges > 0) { "the binding on the epilog was not re-evaluated" }
        assertTrue(parentEvents > 0) { "the parent property was not told about the change" }
    }

    /**
     * Use case: the switch is bound to the check box of the editor, so every state that box produces
     * reaches the model object and every binding above it shows it.
     */
    @Test
    fun writesBoundIncludedToModelAndNotifiesTree() {
        val source = SimpleBooleanProperty(false)
        property.includedProperty.bind(source)

        source.set(true)

        assertEquals(true, holder.epilog?.included)
        assertTreeShows(included = true)
        assertTrue(includedViewChanges > 0) { "the binding on the switch was not re-evaluated" }
        assertTrue(rootViewChanges > 0) { "the binding on the epilog was not re-evaluated" }
        assertTrue(parentEvents > 0) { "the parent property was not told about the change" }

        property.includedProperty.unbind()
    }

    /**
     * Use case: a field of the epilog is changed by application code past the property, so the property
     * is told to read the epilog again and every field property delivers the current value afterwards.
     */
    @Test
    fun readsFieldsChangedOnModel() {
        holder.epilog?.prompts = AIPrompt("Tell what nobody expected.", "Dark and short.")
        holder.epilog?.included = true

        property.refresh()

        assertEquals(AIPrompt("Tell what nobody expected.", "Dark and short."), property.prompts)
        assertTrue(property.included)
    }

    /**
     * Use case: the whole epilog is replaced - another project file was loaded - so the field
     * properties belong to another object afterwards and every binding of the object tree shows the
     * values of that object instead of the previous ones.
     */
    @Test
    fun writesReplacedEpilogToModelAndNotifiesWholeTree() {
        property.value = Epilog(
            prompts = AIPrompt("Tell what nobody expected.", "Dark and short."),
            included = true
        )

        assertTreeShows(AIPrompt("Tell what nobody expected.", "Dark and short."), included = true)
        assertTrue(promptsViewChanges > 0) { "the binding on the prompts was not re-evaluated" }
        assertTrue(includedViewChanges > 0) { "the binding on the switch was not re-evaluated" }
        assertTrue(rootViewChanges > 0) { "the binding on the epilog was not re-evaluated" }
        assertTrue(parentEvents > 0) { "the parent property was not told about the change" }
    }

    /**
     * Use case: the epilog is exchanged for an object carrying the same values, so nothing the user
     * interface shows changes and no field property reports a change of its own.
     */
    @Test
    fun keepsFieldsQuietWhenReplacedEpilogCarriesTheSameValues() {
        property.value = Epilog(prompts = INITIAL_PROMPTS)

        assertTreeShows()
        assertEquals(0, promptsViewChanges) {
            "the prompts were reported as changed although they did not change"
        }
        assertEquals(0, includedViewChanges) {
            "the switch was reported as changed although it did not change"
        }
    }

    /**
     * Use case: no book sits behind the property standing for the epilog because no project is open,
     * so every field property answers with a neutral value and the editor can be built nevertheless.
     */
    @Test
    fun readsNeutralValuesWhenEpilogIsAbsent() {
        property.value = null

        assertNull(property.prompts)
        assertFalse(property.included)
        assertTreeShows(null)
    }

    /**
     * Use case: the editor writes into the property while no epilog sits behind it, so the values are
     * dropped instead of creating an epilog nobody asked for.
     */
    @Test
    fun dropsWritesWhenEpilogIsAbsent() {
        property.value = null

        property.prompts = AIPrompt("Tell what nobody expected.", "Dark and short.")
        property.included = true

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

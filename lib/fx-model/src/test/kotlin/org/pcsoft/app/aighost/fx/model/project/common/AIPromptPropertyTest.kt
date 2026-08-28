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

package org.pcsoft.app.aighost.fx.model.project.common

import javafx.beans.property.SimpleStringProperty
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.pcsoft.app.aighost.fx.model.ChangeRecorder
import org.pcsoft.app.aighost.model.project.common.AIPrompt

/**
 * Developer tests for [AIPromptProperty].
 *
 * The property wraps the prompts a part of the manuscript is generated from and offers both texts it
 * carries - the one describing the content and the one describing the style - as properties of their
 * own. The two texts are wrapped exactly the same way, so every test walks through both of them
 * instead of picking one.
 */
class AIPromptPropertyTest {

    /** Stands for the part carrying the prompts, the object a parent property writes into. */
    private class Holder(var prompts: AIPrompt?)

    private lateinit var holder: Holder
    private lateinit var property: AIPromptProperty
    private lateinit var recorder: ChangeRecorder

    /** Counts what the parent property is told, so the report up to the root becomes visible. */
    private var parentEvents = 0

    @BeforeEach
    fun setUp() {
        holder = Holder(newPrompts())
        parentEvents = 0
        property = AIPromptProperty(
            { holder.prompts = it },
            { holder.prompts },
            { parentEvents++ }
        )
        // A parent property aligns a nested one with the model object as soon as that object arrives,
        // so the same alignment happens here before the views are built.
        property.refresh()

        recorder = ChangeRecorder()
        recorder.watch("prompts", property)
        recorder.watch("prompts.contentPrompt", property.contentPromptProperty)
        recorder.watch("prompts.stylePrompt", property.stylePromptProperty)

        parentEvents = 0
    }

    /** The prompts every test starts from, built fresh so no test sees the object of another. */
    private fun newPrompts(): AIPrompt = AIPrompt(
        contentPrompt = "Tell how the journey started.",
        stylePrompt = "Lively and warm."
    )

    /**
     * Use case: a view shows what a part is generated from, so both texts answer with the values the
     * wrapped object carries instead of a copy made earlier.
     */
    @Test
    fun readsBothTextsFromTheModelObject() {
        assertEquals("Tell how the journey started.", property.contentPromptProperty.get())
        assertEquals("Lively and warm.", property.stylePromptProperty.get())
    }

    /**
     * Use case: the user describes content and style separately, so each write reaches its own field
     * of the model object and reports the change up to the root.
     */
    @Test
    fun writingEachTextReachesItsOwnField() {
        property.contentPromptProperty.set("Tell how the journey ended.")
        property.stylePromptProperty.set("Dry and short.")

        assertEquals("Tell how the journey ended.", holder.prompts?.contentPrompt)
        assertEquals("Dry and short.", holder.prompts?.stylePrompt)
        assertEquals(1, recorder.countOf("prompts.contentPrompt"))
        assertEquals(1, recorder.countOf("prompts.stylePrompt"))
        assertEquals(2, recorder.countOf("prompts"))
        assertEquals(2, parentEvents)
    }

    /**
     * Use case: a text area of the prompt editor is bound to the content text, so what the user types
     * arrives in the model object through the binding without any code writing it there.
     */
    @Test
    fun writingThroughABindingReachesTheModelObject() {
        val input = SimpleStringProperty("Tell how the journey ended.")
        property.contentPromptProperty.bind(input)

        assertEquals("Tell how the journey ended.", holder.prompts?.contentPrompt)

        input.set("Tell what nobody expected.")

        assertEquals("Tell what nobody expected.", holder.prompts?.contentPrompt)

        property.contentPromptProperty.unbind()
    }

    /**
     * Use case: something changes the model object directly - an imported prompt for instance - so
     * both text properties take over the new values as soon as the prompts are refreshed.
     */
    @Test
    fun aChangeOnTheModelObjectBecomesVisible() {
        holder.prompts?.contentPrompt = "Tell what nobody expected."
        holder.prompts?.stylePrompt = "Dark and short."

        property.refresh()

        assertEquals("Tell what nobody expected.", property.contentPromptProperty.get())
        assertEquals("Dark and short.", property.stylePromptProperty.get())
    }

    /**
     * Use case: another project is opened, so the whole prompt pair is exchanged and every property
     * below it takes over the value of the new object and reports it up to the root.
     */
    @Test
    fun exchangingTheModelObjectUpdatesEveryField() {
        recorder.reset()
        parentEvents = 0

        property.set(AIPrompt("Tell what nobody expected.", "Dark and short."))

        assertEquals("Tell what nobody expected.", property.contentPromptProperty.get())
        assertEquals("Dark and short.", property.stylePromptProperty.get())
        recorder.assertAllFired("exchanging the prompts")
        assertEquals(1, parentEvents)
    }

    /**
     * Use case: a project is opened again without having been changed, so an exchange against an
     * equal object leaves every field property quiet instead of redrawing the whole editor.
     */
    @Test
    fun exchangingAgainstAnEqualObjectStaysQuiet() {
        recorder.reset()

        property.set(newPrompts())

        assertEquals(0, recorder.countOf("prompts.contentPrompt"))
        assertEquals(0, recorder.countOf("prompts.stylePrompt"))
    }

    /**
     * Use case: the user interface is built before a project is opened, so every field property
     * answers with a neutral value and drops what is written to it instead of failing.
     */
    @Test
    fun answersNeutrallyWithoutAModelObject() {
        holder.prompts = null
        property.refresh()

        assertNull(property.contentPromptProperty.get())
        assertNull(property.stylePromptProperty.get())

        property.contentPromptProperty.set("Ignored")

        assertNull(holder.prompts)
    }
}

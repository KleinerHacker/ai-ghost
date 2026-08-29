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

package org.pcsoft.app.aighost.fx.model.pref

import javafx.beans.binding.Bindings
import javafx.beans.property.SimpleLongProperty
import javafx.beans.property.SimpleStringProperty
import javafx.beans.property.StringProperty
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.pcsoft.app.aighost.model.pref.Ai
import org.pcsoft.app.aighost.model.pref.Preferences

/**
 * Developer tests for [AiProperty].
 *
 * The property wraps the settings of the AI functionality of a parent model object and offers every
 * field of that object as a property of its own. Every test checks the object tree the way the user
 * interface uses it: a binding hangs on each property of the tree and the tests assert that every one
 * of these bindings is re-evaluated and delivers the current state - the property of the changed
 * field, the property of the wrapped object and the parent property above it.
 */
class AiPropertyTest {

    private lateinit var preferences: Preferences
    private lateinit var property: AiProperty

    /** Number of changes the parent property was told about. */
    private var parentEvents = 0

    /** Binding on the wrapped object, standing for a view bound to the AI settings. */
    private lateinit var objectView: StringProperty
    private var objectViewChanges = 0

    /** Binding on the story limit, standing for a view bound to that single field. */
    private lateinit var storyView: StringProperty
    private var storyViewChanges = 0

    /** Binding on the style limit, standing for a view bound to that single field. */
    private lateinit var styleView: StringProperty
    private var styleViewChanges = 0

    @BeforeEach
    fun setUp() {
        preferences = Preferences(ai = Ai(maxStoryCharacters = 5000, maxStyleCharacters = 1000))
        parentEvents = 0
        property = AiProperty()
        // A parent property reports a change of a nested one as its own and writes an exchanged object
        // back into the one carrying it, which is what these two listeners stand for.
        property.addListener { _ -> parentEvents++ }
        property.addListener { _, _, newValue -> preferences.ai = newValue }
        // A parent property hands the nested object to this property as soon as that object arrives.
        property.set(preferences.ai)

        objectView = SimpleStringProperty()
        val objectBinding = Bindings.createStringBinding({ state(property.value) }, property)
        // A listener keeps the binding eager, so an invalidation that is never followed by a read
        // still shows up as a change.
        objectBinding.addListener { _, _, _ -> objectViewChanges++ }
        objectView.bind(objectBinding)

        storyView = SimpleStringProperty()
        val storyBinding = property.maxStoryCharactersProperty.asString()
        storyBinding.addListener { _, _, _ -> storyViewChanges++ }
        storyView.bind(storyBinding)

        styleView = SimpleStringProperty()
        val styleBinding = property.maxStyleCharactersProperty.asString()
        styleBinding.addListener { _, _, _ -> styleViewChanges++ }
        styleView.bind(styleBinding)

        parentEvents = 0
        objectViewChanges = 0
        storyViewChanges = 0
        styleViewChanges = 0
    }

    /** Text form of an AI settings object, used as the value of the binding on the whole object. */
    private fun state(ai: Ai): String = "${ai.maxStoryCharacters}|${ai.maxStyleCharacters}"

    /**
     * Asserts that every binding of the object tree delivers the given state, so no view keeps the
     * value of a previous object or of a previous field value.
     */
    private fun assertTreeShows(maxStoryCharacters: Long, maxStyleCharacters: Long) {
        assertEquals("$maxStoryCharacters|$maxStyleCharacters", objectView.get()) {
            "the binding on the AI settings delivers an outdated state"
        }
        assertEquals(maxStoryCharacters.toString(), storyView.get()) {
            "the binding on the story limit delivers an outdated value"
        }
        assertEquals(maxStyleCharacters.toString(), styleView.get()) {
            "the binding on the style limit delivers an outdated value"
        }
    }

    /**
     * Asserts that a change of a field was reported by the property of that field, by the property of
     * the wrapped object and by the parent property above it.
     */
    private fun assertFieldChangeReachedWholeTree(fieldViewChanges: Int) {
        assertTrue(fieldViewChanges > 0) { "the binding on the changed field was not re-evaluated" }
        assertTrue(objectViewChanges > 0) { "the binding on the AI settings was not re-evaluated" }
        assertTrue(parentEvents > 0) { "the parent property was not told about the change" }
    }

    /**
     * Use case: the preferences are read from the settings file before the user interface is built, so
     * the property delivers the object that already sits in the model object.
     */
    @Test
    fun readsInitialValuesFromModel() {
        assertEquals(Ai(maxStoryCharacters = 5000, maxStyleCharacters = 1000), property.value)
        assertEquals(5000, property.maxStoryCharacters)
        assertEquals(1000, property.maxStyleCharacters)
        assertTreeShows(maxStoryCharacters = 5000, maxStyleCharacters = 1000)
    }

    /**
     * Use case: the user raises the length a story may reach, so the new limit lands in the model
     * object and every binding of the object tree shows it.
     */
    @Test
    fun writesMaxStoryCharactersToModelAndNotifiesTree() {
        property.maxStoryCharacters = 12000

        assertEquals(12000, preferences.ai.maxStoryCharacters)
        assertTreeShows(maxStoryCharacters = 12000, maxStyleCharacters = 1000)
        assertFieldChangeReachedWholeTree(storyViewChanges)
    }

    /**
     * Use case: the story limit is bound to a spinner of the settings dialog, so every value that
     * spinner produces reaches the model object and every binding of the object tree shows it.
     */
    @Test
    fun writesBoundMaxStoryCharactersToModelAndNotifiesTree() {
        val source = SimpleLongProperty(2000)
        property.maxStoryCharactersProperty.bind(source)

        source.set(9000)

        assertEquals(9000, preferences.ai.maxStoryCharacters)
        assertTreeShows(maxStoryCharacters = 9000, maxStyleCharacters = 1000)
        assertFieldChangeReachedWholeTree(storyViewChanges)
    }

    /**
     * Use case: the user lowers the length a style definition may reach, so the new limit lands in the
     * model object and every binding of the object tree shows it.
     */
    @Test
    fun writesMaxStyleCharactersToModelAndNotifiesTree() {
        property.maxStyleCharacters = 400

        assertEquals(400, preferences.ai.maxStyleCharacters)
        assertTreeShows(maxStoryCharacters = 5000, maxStyleCharacters = 400)
        assertFieldChangeReachedWholeTree(styleViewChanges)
    }

    /**
     * Use case: the style limit is bound to a spinner of the settings dialog, so every value that
     * spinner produces reaches the model object and every binding of the object tree shows it.
     */
    @Test
    fun writesBoundMaxStyleCharactersToModelAndNotifiesTree() {
        val source = SimpleLongProperty(500)
        property.maxStyleCharactersProperty.bind(source)

        source.set(750)

        assertEquals(750, preferences.ai.maxStyleCharacters)
        assertTreeShows(maxStoryCharacters = 5000, maxStyleCharacters = 750)
        assertFieldChangeReachedWholeTree(styleViewChanges)
    }

    /**
     * Use case: a limit beyond the range of a 32 bit number is configured, so the whole value reaches
     * the model object instead of being cut off on its way.
     */
    @Test
    fun writesALimitBeyondTheIntegerRange() {
        property.maxStoryCharacters = 5_000_000_000L

        assertEquals(5_000_000_000L, preferences.ai.maxStoryCharacters)
        assertTreeShows(maxStoryCharacters = 5_000_000_000L, maxStyleCharacters = 1000)
    }

    /**
     * Use case: the whole AI block is replaced - a settings file that was loaded again for instance -
     * so the field properties belong to another object afterwards and every binding of the object tree
     * shows the values of that object instead of the previous ones.
     */
    @Test
    fun writesReplacedObjectToModelAndNotifiesTree() {
        property.value = Ai(maxStoryCharacters = 3000, maxStyleCharacters = 800)

        assertEquals(Ai(maxStoryCharacters = 3000, maxStyleCharacters = 800), preferences.ai)
        assertTreeShows(maxStoryCharacters = 3000, maxStyleCharacters = 800)
        assertTrue(objectViewChanges > 0) { "the binding on the AI settings was not re-evaluated" }
        assertTrue(storyViewChanges > 0) { "the binding on the story limit was not re-evaluated" }
        assertTrue(styleViewChanges > 0) { "the binding on the style limit was not re-evaluated" }
        assertTrue(parentEvents > 0) { "the parent property was not told about the change" }
    }

    /**
     * Use case: the AI settings are exchanged for an object carrying the same values, so nothing the
     * user interface shows changes and no field property reports a change of its own.
     */
    @Test
    fun keepsFieldsQuietWhenReplacedObjectCarriesTheSameValues() {
        property.value = Ai(maxStoryCharacters = 5000, maxStyleCharacters = 1000)

        assertTreeShows(maxStoryCharacters = 5000, maxStyleCharacters = 1000)
        assertEquals(0, storyViewChanges) { "the story limit was reported as changed although it did not change" }
        assertEquals(0, styleViewChanges) { "the style limit was reported as changed although it did not change" }
    }

    /**
     * Use case: a field of the wrapped object is changed by application code past the property, so the
     * property is told to read that object again and every field property delivers the current value
     * afterwards.
     */
    @Test
    fun readsFieldsChangedOnModel() {
        preferences.ai.maxStoryCharacters = 2500
        preferences.ai.maxStyleCharacters = 600

        property.refresh()

        assertEquals(2500, property.maxStoryCharacters)
        assertEquals(600, property.maxStyleCharacters)
        assertTreeShows(maxStoryCharacters = 2500, maxStyleCharacters = 600)
    }
}

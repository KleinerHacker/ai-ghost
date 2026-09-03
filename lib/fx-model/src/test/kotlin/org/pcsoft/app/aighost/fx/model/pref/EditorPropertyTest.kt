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
import org.pcsoft.app.aighost.model.pref.Editor
import org.pcsoft.app.aighost.model.pref.Preferences

/**
 * Developer tests for [EditorProperty].
 *
 * The property wraps the settings of the writing surface of a parent model object and offers every
 * field of that object as a property of its own. Every test checks the object tree the way the user
 * interface uses it: a binding hangs on each property of the tree and the tests assert that every one
 * of these bindings is re-evaluated and delivers the current state - the property of the changed
 * field, the property of the wrapped object and the parent property above it.
 */
class EditorPropertyTest {

    private lateinit var preferences: Preferences
    private lateinit var property: EditorProperty

    /** Number of changes the parent property was told about. */
    private var parentEvents = 0

    /** Binding on the wrapped object, standing for a view bound to the writing-surface settings. */
    private lateinit var objectView: StringProperty
    private var objectViewChanges = 0

    /** Binding on the typing pause, standing for a view bound to that single field. */
    private lateinit var pauseView: StringProperty
    private var pauseViewChanges = 0

    @BeforeEach
    fun setUp() {
        preferences = Preferences(editor = Editor(paragraphMergePauseMillis = 600))
        parentEvents = 0
        property = EditorProperty()
        // A parent property reports a change of a nested one as its own and writes an exchanged object
        // back into the one carrying it, which is what these two listeners stand for.
        property.addListener { _ -> parentEvents++ }
        property.addListener { _, _, newValue -> preferences.editor = newValue }
        // A parent property hands the nested object to this property as soon as that object arrives.
        property.set(preferences.editor)

        objectView = SimpleStringProperty()
        val objectBinding = Bindings.createStringBinding({ state(property.value) }, property)
        // A listener keeps the binding eager, so an invalidation that is never followed by a read
        // still shows up as a change.
        objectBinding.addListener { _, _, _ -> objectViewChanges++ }
        objectView.bind(objectBinding)

        pauseView = SimpleStringProperty()
        val pauseBinding = property.paragraphMergePauseMillisProperty.asString()
        pauseBinding.addListener { _, _, _ -> pauseViewChanges++ }
        pauseView.bind(pauseBinding)

        parentEvents = 0
        objectViewChanges = 0
        pauseViewChanges = 0
    }

    /** Text form of a writing-surface settings object, used as the value of the binding on the whole object. */
    private fun state(editor: Editor): String = editor.paragraphMergePauseMillis.toString()

    /**
     * Asserts that every binding of the object tree delivers the given state, so no view keeps the
     * value of a previous object or of a previous field value.
     */
    private fun assertTreeShows(paragraphMergePauseMillis: Long) {
        assertEquals(paragraphMergePauseMillis.toString(), objectView.get()) {
            "the binding on the writing-surface settings delivers an outdated state"
        }
        assertEquals(paragraphMergePauseMillis.toString(), pauseView.get()) {
            "the binding on the typing pause delivers an outdated value"
        }
    }

    /**
     * Asserts that a change of a field was reported by the property of that field, by the property of
     * the wrapped object and by the parent property above it.
     */
    private fun assertFieldChangeReachedWholeTree(fieldViewChanges: Int) {
        assertTrue(fieldViewChanges > 0) { "the binding on the changed field was not re-evaluated" }
        assertTrue(objectViewChanges > 0) { "the binding on the writing-surface settings was not re-evaluated" }
        assertTrue(parentEvents > 0) { "the parent property was not told about the change" }
    }

    /**
     * Use case: the preferences are read from the settings file before the user interface is built, so
     * the property delivers the object that already sits in the model object.
     */
    @Test
    fun readsInitialValuesFromModel() {
        assertEquals(Editor(paragraphMergePauseMillis = 600), property.value)
        assertEquals(600, property.paragraphMergePauseMillis)
        assertTreeShows(paragraphMergePauseMillis = 600)
    }

    /**
     * Use case: the user changes the typing pause, so the new value lands in the model object and
     * every binding of the object tree shows it.
     */
    @Test
    fun writesParagraphMergePauseMillisToModelAndNotifiesTree() {
        property.paragraphMergePauseMillis = 900

        assertEquals(900, preferences.editor.paragraphMergePauseMillis)
        assertTreeShows(paragraphMergePauseMillis = 900)
        assertFieldChangeReachedWholeTree(pauseViewChanges)
    }

    /**
     * Use case: the typing pause is bound to a control of a settings dialog, so every value that
     * control produces reaches the model object and every binding of the object tree shows it.
     */
    @Test
    fun writesBoundParagraphMergePauseMillisToModelAndNotifiesTree() {
        val source = SimpleLongProperty(300)
        property.paragraphMergePauseMillisProperty.bind(source)

        source.set(1200)

        assertEquals(1200, preferences.editor.paragraphMergePauseMillis)
        assertTreeShows(paragraphMergePauseMillis = 1200)
        assertFieldChangeReachedWholeTree(pauseViewChanges)
    }

    /**
     * Use case: the whole writing-surface block is replaced - a settings file that was loaded again
     * for instance - so the field properties belong to another object afterwards and every binding of
     * the object tree shows the values of that object instead of the previous ones.
     */
    @Test
    fun writesReplacedObjectToModelAndNotifiesTree() {
        property.value = Editor(paragraphMergePauseMillis = 450)

        assertEquals(Editor(paragraphMergePauseMillis = 450), preferences.editor)
        assertTreeShows(paragraphMergePauseMillis = 450)
        assertTrue(objectViewChanges > 0) { "the binding on the writing-surface settings was not re-evaluated" }
        assertTrue(pauseViewChanges > 0) { "the binding on the typing pause was not re-evaluated" }
        assertTrue(parentEvents > 0) { "the parent property was not told about the change" }
    }

    /**
     * Use case: the writing-surface settings are exchanged for an object carrying the same values, so
     * nothing the user interface shows changes and no field property reports a change of its own.
     */
    @Test
    fun keepsFieldsQuietWhenReplacedObjectCarriesTheSameValues() {
        property.value = Editor(paragraphMergePauseMillis = 600)

        assertTreeShows(paragraphMergePauseMillis = 600)
        assertEquals(0, pauseViewChanges) { "the typing pause was reported as changed although it did not change" }
    }

    /**
     * Use case: a field of the wrapped object is changed by application code past the property, so the
     * property is told to read that object again and the field property delivers the current value
     * afterwards.
     */
    @Test
    fun readsFieldsChangedOnModel() {
        preferences.editor.paragraphMergePauseMillis = 250

        property.refresh()

        assertEquals(250, property.paragraphMergePauseMillis)
        assertTreeShows(paragraphMergePauseMillis = 250)
    }
}

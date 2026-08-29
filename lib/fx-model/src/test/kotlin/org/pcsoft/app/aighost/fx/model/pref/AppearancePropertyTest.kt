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
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.beans.property.StringProperty
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.pcsoft.app.aighost.model.pref.Appearance
import org.pcsoft.app.aighost.model.pref.Preferences
import org.pcsoft.app.aighost.model.pref.ThemeMode

/**
 * Developer tests for [AppearanceProperty].
 *
 * The property wraps the appearance settings of a parent model object and offers every field of that
 * object as a property of its own. Every test checks the object tree the way the user interface uses
 * it: a binding hangs on each property of the tree and the tests assert that every one of these
 * bindings is re-evaluated and delivers the current state - the property of the changed field, the
 * property of the wrapped object and the parent property above it.
 */
class AppearancePropertyTest {

    private lateinit var preferences: Preferences
    private lateinit var property: AppearanceProperty

    /** Number of changes the parent property was told about. */
    private var parentEvents = 0

    /** Binding on the wrapped object, standing for a view bound to the appearance settings. */
    private lateinit var objectView: StringProperty
    private var objectViewChanges = 0

    /** Binding on the theme, standing for a view bound to that single field. */
    private lateinit var themeView: StringProperty
    private var themeViewChanges = 0

    @BeforeEach
    fun setUp() {
        preferences = Preferences(appearance = Appearance(themeMode = ThemeMode.SYSTEM))
        parentEvents = 0
        property = AppearanceProperty()
        // A parent property reports a change of a nested one as its own and writes an exchanged object
        // back into the one carrying it, which is what these two listeners stand for.
        property.addListener { _ -> parentEvents++ }
        property.addListener { _, _, newValue -> preferences.appearance = newValue }
        // A parent property hands the nested object to this property as soon as that object arrives.
        property.set(preferences.appearance)

        objectView = SimpleStringProperty()
        val objectBinding = Bindings.createStringBinding({ state(property.value) }, property)
        // A listener keeps the binding eager, so an invalidation that is never followed by a read
        // still shows up as a change.
        objectBinding.addListener { _, _, _ -> objectViewChanges++ }
        objectView.bind(objectBinding)

        themeView = SimpleStringProperty()
        val themeBinding = property.themeModeProperty.asString()
        themeBinding.addListener { _, _, _ -> themeViewChanges++ }
        themeView.bind(themeBinding)

        parentEvents = 0
        objectViewChanges = 0
        themeViewChanges = 0
    }

    /** Text form of an appearance object, used as the value of the binding on the whole object. */
    private fun state(appearance: Appearance): String = appearance.themeMode.toString()

    /**
     * Asserts that every binding of the object tree delivers the given state, so no view keeps the
     * value of a previous object or of a previous field value.
     */
    private fun assertTreeShows(themeMode: ThemeMode) {
        assertEquals(themeMode.toString(), objectView.get()) {
            "the binding on the appearance object delivers an outdated state"
        }
        assertEquals(themeMode.toString(), themeView.get()) {
            "the binding on the theme delivers an outdated value"
        }
    }

    /**
     * Asserts that a change of a field was reported by the property of that field, by the property of
     * the wrapped object and by the parent property above it.
     */
    private fun assertFieldChangeReachedWholeTree(fieldViewChanges: Int) {
        assertTrue(fieldViewChanges > 0) { "the binding on the changed field was not re-evaluated" }
        assertTrue(objectViewChanges > 0) { "the binding on the appearance object was not re-evaluated" }
        assertTrue(parentEvents > 0) { "the parent property was not told about the change" }
    }

    /**
     * Use case: the preferences are read from the settings file before the user interface is built, so
     * the property delivers the object that already sits in the model object.
     */
    @Test
    fun readsInitialValueFromModel() {
        assertEquals(Appearance(themeMode = ThemeMode.SYSTEM), property.value)
        assertEquals(ThemeMode.SYSTEM, property.themeMode)
        assertTreeShows(ThemeMode.SYSTEM)
    }

    /**
     * Use case: the user picks another appearance in the settings dialog, so the chosen mode lands in
     * the model object and every binding of the object tree shows it.
     */
    @Test
    fun writesThemeModeToModelAndNotifiesTree() {
        property.themeMode = ThemeMode.DARK

        assertEquals(ThemeMode.DARK, preferences.appearance.themeMode)
        assertTreeShows(ThemeMode.DARK)
        assertFieldChangeReachedWholeTree(themeViewChanges)
    }

    /**
     * Use case: the theme is bound to a choice box of the settings dialog, so every mode that choice
     * box produces reaches the model object and every binding of the object tree shows it.
     */
    @Test
    fun writesBoundThemeModeToModelAndNotifiesTree() {
        val source = SimpleObjectProperty(ThemeMode.LIGHT)
        property.themeModeProperty.bind(source)

        source.set(ThemeMode.DARK)

        assertEquals(ThemeMode.DARK, preferences.appearance.themeMode)
        assertTreeShows(ThemeMode.DARK)
        assertFieldChangeReachedWholeTree(themeViewChanges)
    }

    /**
     * Use case: the whole appearance block is replaced - a settings file that was loaded again for
     * instance - so the field properties belong to another object afterwards and every binding of the
     * object tree shows the values of that object instead of the previous ones.
     */
    @Test
    fun writesReplacedObjectToModelAndNotifiesTree() {
        property.value = Appearance(themeMode = ThemeMode.LIGHT)

        assertEquals(Appearance(themeMode = ThemeMode.LIGHT), preferences.appearance)
        assertTreeShows(ThemeMode.LIGHT)
        assertTrue(objectViewChanges > 0) { "the binding on the appearance object was not re-evaluated" }
        assertTrue(themeViewChanges > 0) { "the binding on the theme was not re-evaluated" }
        assertTrue(parentEvents > 0) { "the parent property was not told about the change" }
    }

    /**
     * Use case: the appearance is exchanged for an object carrying the same value, so nothing the user
     * interface shows changes and no field property reports a change of its own.
     */
    @Test
    fun keepsFieldsQuietWhenReplacedObjectCarriesTheSameValues() {
        property.value = Appearance(themeMode = ThemeMode.SYSTEM)

        assertTreeShows(ThemeMode.SYSTEM)
        assertEquals(0, themeViewChanges) { "the theme was reported as changed although it did not change" }
    }

    /**
     * Use case: a field of the wrapped object is changed by application code past the property, so the
     * property is told to read that object again and the field property delivers the current value
     * afterwards.
     */
    @Test
    fun readsFieldsChangedOnModel() {
        preferences.appearance.themeMode = ThemeMode.DARK

        property.refresh()

        assertEquals(ThemeMode.DARK, property.themeMode)
        assertTreeShows(ThemeMode.DARK)
    }
}

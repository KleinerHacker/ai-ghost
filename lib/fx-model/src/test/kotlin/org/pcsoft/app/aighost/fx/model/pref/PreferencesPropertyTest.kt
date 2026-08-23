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
import javafx.collections.FXCollections
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.pcsoft.app.aighost.model.pref.Preferences
import org.pcsoft.app.aighost.model.pref.RecentOpened
import org.pcsoft.app.aighost.model.pref.ThemeMode

/**
 * Developer tests for [PreferencesProperty].
 *
 * The property wraps the preferences of the user and offers every field of that object - and every
 * field of the objects nested in it - as a property of its own. Every test checks the object tree the
 * way the user interface uses it: a binding hangs on each property of the tree - the preferences
 * themselves, the recently opened files below them and every single field - and the tests assert that
 * a change reaches every binding that has to know about it, upwards to the root as well as downwards
 * into the fields of an exchanged object.
 */
class PreferencesPropertyTest {

    private lateinit var preferences: Preferences
    private lateinit var property: PreferencesProperty
    private lateinit var recentOpenedProperty: RecentOpenedProperty

    /** Binding on the whole preferences, standing for a view bound to the root of the object tree. */
    private lateinit var rootView: StringProperty
    private var rootViewChanges = 0

    /** Binding on the appearance, standing for a view bound to that single field. */
    private lateinit var themeView: StringProperty
    private var themeViewChanges = 0

    /** Binding on the recently opened files, standing for a view bound to that nested object. */
    private lateinit var recentOpenedView: StringProperty
    private var recentOpenedViewChanges = 0

    /** Binding on the maximum nested in the recently opened files. */
    private lateinit var maxView: StringProperty
    private var maxViewChanges = 0

    /** Binding on the entries nested in the recently opened files. */
    private lateinit var entriesView: StringProperty
    private var entriesViewChanges = 0

    @BeforeEach
    fun setUp() {
        preferences = Preferences(
            recentOpened = RecentOpened(max = 10, entries = emptyList()),
            themeMode = ThemeMode.SYSTEM
        )
        property = PreferencesProperty()
        property.value = preferences
        recentOpenedProperty = property.recentOpenedProperty as RecentOpenedProperty

        rootView = SimpleStringProperty()
        val rootBinding = Bindings.createStringBinding({ state(property.value) }, property)
        // A listener keeps the binding eager, so an invalidation that is never followed by a read
        // still shows up as a change.
        rootBinding.addListener { _, _, _ -> rootViewChanges++ }
        rootView.bind(rootBinding)

        themeView = SimpleStringProperty()
        val themeBinding = property.themeModeProperty.asString()
        themeBinding.addListener { _, _, _ -> themeViewChanges++ }
        themeView.bind(themeBinding)

        recentOpenedView = SimpleStringProperty()
        val recentOpenedBinding = Bindings.createStringBinding(
            { state(recentOpenedProperty.value) },
            recentOpenedProperty
        )
        recentOpenedBinding.addListener { _, _, _ -> recentOpenedViewChanges++ }
        recentOpenedView.bind(recentOpenedBinding)

        maxView = SimpleStringProperty()
        val maxBinding = recentOpenedProperty.maxProperty.asString()
        maxBinding.addListener { _, _, _ -> maxViewChanges++ }
        maxView.bind(maxBinding)

        entriesView = SimpleStringProperty()
        val entriesBinding = Bindings.createStringBinding(
            { recentOpenedProperty.entriesProperty.joinToString(";") },
            recentOpenedProperty.entriesProperty
        )
        entriesBinding.addListener { _, _, _ -> entriesViewChanges++ }
        entriesView.bind(entriesBinding)

        rootViewChanges = 0
        themeViewChanges = 0
        recentOpenedViewChanges = 0
        maxViewChanges = 0
        entriesViewChanges = 0
    }

    /** Text form of the whole preferences, used as the value of the binding on the root. */
    private fun state(preferences: Preferences): String =
        "${preferences.themeMode}|${state(preferences.recentOpened)}"

    /** Text form of the recently opened files, used as the value of the binding on that object. */
    private fun state(recentOpened: RecentOpened): String =
        "${recentOpened.max}|${recentOpened.entries.joinToString(";")}"

    /**
     * Asserts that every binding of the object tree delivers the given state, so no view keeps the
     * value of a previous object or of a previous field value.
     */
    private fun assertTreeShows(themeMode: ThemeMode, max: Int, entries: List<String>) {
        val entriesText = entries.joinToString(";")

        assertEquals("$themeMode|$max|$entriesText", rootView.get()) {
            "the binding on the preferences delivers an outdated state"
        }
        assertEquals(themeMode.toString(), themeView.get()) {
            "the binding on the appearance delivers an outdated value"
        }
        assertEquals("$max|$entriesText", recentOpenedView.get()) {
            "the binding on the recently opened files delivers an outdated state"
        }
        assertEquals(max.toString(), maxView.get()) {
            "the binding on the maximum delivers an outdated value"
        }
        assertEquals(entriesText, entriesView.get()) {
            "the binding on the entries delivers outdated entries"
        }
    }

    /**
     * Use case: the preferences are read from the settings file before the user interface is built, so
     * every binding of the object tree delivers the state that already sits in the model object.
     */
    @Test
    fun readsInitialValuesFromModel() {
        assertTreeShows(ThemeMode.SYSTEM, max = 10, entries = emptyList())
    }

    /**
     * Use case: the user picks another appearance in the settings dialog, so the chosen mode lands in
     * the preferences object and both the binding on that field and the binding on the root show it.
     */
    @Test
    fun writesThemeModeToModelAndNotifiesTree() {
        property.themeMode = ThemeMode.DARK

        assertEquals(ThemeMode.DARK, preferences.themeMode)
        assertTreeShows(ThemeMode.DARK, max = 10, entries = emptyList())
        assertTrue(themeViewChanges > 0) { "the binding on the appearance was not re-evaluated" }
        assertTrue(rootViewChanges > 0) { "the binding on the preferences was not re-evaluated" }
    }

    /**
     * Use case: the appearance is bound to a choice box of the settings dialog, so every mode that
     * choice box produces reaches the preferences object and both bindings above it show it.
     */
    @Test
    fun writesBoundThemeModeToModelAndNotifiesTree() {
        val source = SimpleObjectProperty(ThemeMode.LIGHT)
        property.themeModeProperty.bind(source)

        source.set(ThemeMode.DARK)

        assertEquals(ThemeMode.DARK, preferences.themeMode)
        assertTreeShows(ThemeMode.DARK, max = 10, entries = emptyList())
        assertTrue(themeViewChanges > 0) { "the binding on the appearance was not re-evaluated" }
        assertTrue(rootViewChanges > 0) { "the binding on the preferences was not re-evaluated" }
    }

    /**
     * Use case: the user raises the number of recently opened files, so the new limit lands in the
     * nested model object and the bindings on that field, on the nested object and on the root show it.
     */
    @Test
    fun writesNestedMaxToModelAndNotifiesTree() {
        recentOpenedProperty.max = 7

        assertEquals(7, preferences.recentOpened.max)
        assertTreeShows(ThemeMode.SYSTEM, max = 7, entries = emptyList())
        assertTrue(maxViewChanges > 0) { "the binding on the maximum was not re-evaluated" }
        assertTrue(recentOpenedViewChanges > 0) { "the binding on the recently opened files was not re-evaluated" }
        assertTrue(rootViewChanges > 0) { "the binding on the preferences was not re-evaluated" }
    }

    /**
     * Use case: a newly opened file is appended to the list bound to the recent files menu, so the
     * content change alone reaches the nested model object and the bindings on that field, on the
     * nested object and on the root show it.
     */
    @Test
    fun writesNestedEntryAddedToModelAndNotifiesTree() {
        recentOpenedProperty.entriesProperty.add("/books/third.md")

        assertEquals(listOf("/books/third.md"), preferences.recentOpened.entries)
        assertTreeShows(ThemeMode.SYSTEM, max = 10, entries = listOf("/books/third.md"))
        assertTrue(entriesViewChanges > 0) { "the binding on the entries was not re-evaluated" }
        assertTrue(recentOpenedViewChanges > 0) { "the binding on the recently opened files was not re-evaluated" }
        assertTrue(rootViewChanges > 0) { "the binding on the preferences was not re-evaluated" }
    }

    /**
     * Use case: the recent files list is filled from a binding, so every list that binding produces
     * reaches the nested model object and the bindings on the nested object and on the root show it.
     */
    @Test
    fun writesBoundNestedEntriesToModelAndNotifiesTree() {
        val source = SimpleObjectProperty(FXCollections.observableArrayList("/books/first.md"))
        recentOpenedProperty.entriesProperty.bind(source)

        source.set(FXCollections.observableArrayList("/books/second.md"))

        assertEquals(listOf("/books/second.md"), preferences.recentOpened.entries)
        assertTreeShows(ThemeMode.SYSTEM, max = 10, entries = listOf("/books/second.md"))
        assertTrue(entriesViewChanges > 0) { "the binding on the entries was not re-evaluated" }
        assertTrue(recentOpenedViewChanges > 0) { "the binding on the recently opened files was not re-evaluated" }
        assertTrue(rootViewChanges > 0) { "the binding on the preferences was not re-evaluated" }
    }

    /**
     * Use case: the whole recently opened block is replaced, so the field properties below it belong
     * to another object afterwards and every binding of the object tree shows the values of that
     * object instead of the previous ones.
     */
    @Test
    fun writesRecentOpenedToModelAndNotifiesTree() {
        property.recentOpened = RecentOpened(max = 5, entries = listOf("/books/second.md"))

        assertEquals(RecentOpened(max = 5, entries = listOf("/books/second.md")), preferences.recentOpened)
        assertTreeShows(ThemeMode.SYSTEM, max = 5, entries = listOf("/books/second.md"))
        assertTrue(maxViewChanges > 0) { "the binding on the maximum was not re-evaluated" }
        assertTrue(entriesViewChanges > 0) { "the binding on the entries was not re-evaluated" }
        assertTrue(recentOpenedViewChanges > 0) { "the binding on the recently opened files was not re-evaluated" }
        assertTrue(rootViewChanges > 0) { "the binding on the preferences was not re-evaluated" }
    }

    /**
     * Use case: another settings file is loaded, so the whole preferences object behind the property
     * is exchanged and every binding of the object tree - down to the fields of the objects nested in
     * it - shows the values of the new object instead of the previous ones.
     */
    @Test
    fun writesReplacedPreferencesToModelAndNotifiesWholeTree() {
        property.value = Preferences(
            recentOpened = RecentOpened(max = 2, entries = listOf("/books/fourth.md")),
            themeMode = ThemeMode.LIGHT
        )

        assertTreeShows(ThemeMode.LIGHT, max = 2, entries = listOf("/books/fourth.md"))
        assertTrue(themeViewChanges > 0) { "the binding on the appearance was not re-evaluated" }
        assertTrue(maxViewChanges > 0) { "the binding on the maximum was not re-evaluated" }
        assertTrue(entriesViewChanges > 0) { "the binding on the entries was not re-evaluated" }
        assertTrue(recentOpenedViewChanges > 0) { "the binding on the recently opened files was not re-evaluated" }
        assertTrue(rootViewChanges > 0) { "the binding on the preferences was not re-evaluated" }
    }

    /**
     * Use case: the preferences are exchanged for an object carrying the same values, so nothing the
     * user interface shows changes and no field property reports a change of its own.
     */
    @Test
    fun keepsFieldsQuietWhenReplacedPreferencesCarryTheSameValues() {
        property.value = Preferences(
            recentOpened = RecentOpened(max = 10, entries = emptyList()),
            themeMode = ThemeMode.SYSTEM
        )

        assertTreeShows(ThemeMode.SYSTEM, max = 10, entries = emptyList())
        assertEquals(0, themeViewChanges) { "the appearance was reported as changed although it did not change" }
        assertEquals(0, maxViewChanges) { "the maximum was reported as changed although it did not change" }
        assertEquals(0, entriesViewChanges) { "the entries were reported as changed although they did not change" }
    }

    /**
     * Use case: a field of a nested object is changed by application code past the property, so every
     * field property delivers the current value instead of a cached copy.
     */
    @Test
    fun readsFieldsChangedOnModel() {
        preferences.themeMode = ThemeMode.DARK
        preferences.recentOpened.max = 4
        preferences.recentOpened.entries = listOf("/books/third.md")

        assertEquals(ThemeMode.DARK, property.themeMode)
        assertEquals(4, recentOpenedProperty.max)
        assertEquals(listOf("/books/third.md"), recentOpenedProperty.entries)
    }
}

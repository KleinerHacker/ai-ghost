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

package org.pcsoft.app.aighost.model.pref

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource

/**
 * Developer tests for [Preferences] and [ThemeMode].
 */
class PreferencesTest {

    private val mapper: ObjectMapper = ObjectMapper().registerKotlinModule()

    /**
     * Use case: a user who never touched the settings gets the system appearance, so the application
     * follows the operating system until an explicit choice is made.
     */
    @Test
    fun defaultsToSystemThemeMode() {
        assertEquals(ThemeMode.SYSTEM, Preferences().themeMode)
    }

    /**
     * Use case: a user who never opened a file gets an empty recent files list with the default
     * limit, so the list is ready to fill without further setup.
     */
    @Test
    fun defaultsToEmptyRecentOpened() {
        assertEquals(RecentOpened(max = 10), Preferences().recentOpened)
    }

    /**
     * Use case: the user picks another appearance, so the setting is in effect right away without a
     * copy of the preferences having to be handed around.
     */
    @Test
    fun changesThemeModeInPlace() {
        val preferences = Preferences()

        preferences.themeMode = ThemeMode.DARK

        assertEquals(ThemeMode.DARK, preferences.themeMode)
    }

    /**
     * Use case: any setting may change, so the recent files list is taken over on the object itself
     * just like the appearance is.
     */
    @Test
    fun changesEveryPropertyInPlace() {
        val preferences = Preferences()

        preferences.recentOpened = preferences.recentOpened.add("a.json")
        preferences.themeMode = ThemeMode.DARK

        assertEquals(listOf("a.json"), preferences.recentOpened.entries)
        assertEquals(ThemeMode.DARK, preferences.themeMode)
    }

    /**
     * Use case: a caller wants the settings of another object without changing what it was handed,
     * so the copy carries the new value while the original keeps its own.
     */
    @Test
    fun copiesWithASingleChangedProperty() {
        val preferences = Preferences(themeMode = ThemeMode.DARK)

        val copy = preferences.copy(themeMode = ThemeMode.LIGHT)

        assertEquals(ThemeMode.LIGHT, copy.themeMode)
        assertEquals(ThemeMode.DARK, preferences.themeMode)
    }

    /**
     * Use case: two users configured the same settings, so their preferences count as equal no
     * matter which object holds them.
     */
    @Test
    fun comparesByItsSettings() {
        val one = Preferences(themeMode = ThemeMode.DARK)
        val other = Preferences(themeMode = ThemeMode.DARK)

        assertEquals(one, other)
        assertEquals(one.hashCode(), other.hashCode())
    }

    /**
     * Use case: the preferences are written to disk, so the theme selection and the recent files
     * appear in the JSON under the stable property names the file format promises.
     */
    @Test
    fun serialisesThemeModeByName() {
        val json = mapper.writeValueAsString(Preferences(themeMode = ThemeMode.DARK))

        assertEquals("""{"recentOpened":{"max":10,"entries":[]},"themeMode":"DARK"}""", json)
    }

    /**
     * Use case: a stored preferences file is read at start up, so every selectable appearance is
     * restored exactly as it was written.
     */
    @ParameterizedTest
    @EnumSource(ThemeMode::class)
    fun roundTripsEveryThemeMode(themeMode: ThemeMode) {
        val preferences = Preferences(themeMode = themeMode)

        val restored: Preferences = mapper.readValue(mapper.writeValueAsString(preferences))

        assertEquals(preferences, restored)
    }

    /**
     * Use case: a preferences file written by an older version does not contain the theme yet, so
     * reading it falls back to the default instead of failing.
     */
    @Test
    fun readsEmptyDocumentWithDefault() {
        val preferences: Preferences = mapper.readValue("{}")

        assertEquals(ThemeMode.SYSTEM, preferences.themeMode)
    }

    /**
     * Use case: a preferences file written by a newer version carries additional properties, so
     * reading it ignores what is unknown instead of failing.
     */
    @Test
    fun ignoresUnknownProperties() {
        val preferences: Preferences = mapper.readValue("""{"themeMode":"LIGHT","language":"en"}""")

        assertEquals(Preferences(themeMode = ThemeMode.LIGHT), preferences)
    }
}

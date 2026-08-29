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
 * Developer tests for [Appearance] and [ThemeMode].
 */
class AppearanceTest {

    private val mapper: ObjectMapper = ObjectMapper().registerKotlinModule()

    /**
     * Use case: a user who never touched the settings gets the system appearance, so the application
     * follows the operating system until an explicit choice is made.
     */
    @Test
    fun defaultsToSystemThemeMode() {
        assertEquals(ThemeMode.SYSTEM, Appearance().themeMode)
    }

    /**
     * Use case: the user picks another appearance, so the setting is in effect right away without a
     * copy of the object having to be handed around.
     */
    @Test
    fun changesThemeModeInPlace() {
        val appearance = Appearance()

        appearance.themeMode = ThemeMode.DARK

        assertEquals(ThemeMode.DARK, appearance.themeMode)
    }

    /**
     * Use case: a caller wants the appearance of another object without changing what it was handed,
     * so the copy carries the new value while the original keeps its own.
     */
    @Test
    fun copiesWithAChangedThemeMode() {
        val appearance = Appearance(themeMode = ThemeMode.DARK)

        val copy = appearance.copy(themeMode = ThemeMode.LIGHT)

        assertEquals(ThemeMode.LIGHT, copy.themeMode)
        assertEquals(ThemeMode.DARK, appearance.themeMode)
    }

    /**
     * Use case: two users configured the same appearance, so the objects count as equal no matter
     * which one holds the setting.
     */
    @Test
    fun comparesByItsSettings() {
        val one = Appearance(themeMode = ThemeMode.DARK)
        val other = Appearance(themeMode = ThemeMode.DARK)

        assertEquals(one, other)
        assertEquals(one.hashCode(), other.hashCode())
    }

    /**
     * Use case: the appearance is written to disk as part of the preferences, so the selected theme
     * appears in the JSON under the stable property name the file format promises.
     */
    @Test
    fun serialisesThemeModeByName() {
        val json = mapper.writeValueAsString(Appearance(themeMode = ThemeMode.DARK))

        assertEquals("""{"themeMode":"DARK"}""", json)
    }

    /**
     * Use case: a stored preferences file is read at start up, so every selectable appearance is
     * restored exactly as it was written.
     */
    @ParameterizedTest
    @EnumSource(ThemeMode::class)
    fun roundTripsEveryThemeMode(themeMode: ThemeMode) {
        val appearance = Appearance(themeMode = themeMode)

        val restored: Appearance = mapper.readValue(mapper.writeValueAsString(appearance))

        assertEquals(appearance, restored)
    }

    /**
     * Use case: a preferences file written by an older version does not contain the theme yet, so
     * reading it falls back to the default instead of failing.
     */
    @Test
    fun readsEmptyDocumentWithDefault() {
        val appearance: Appearance = mapper.readValue("{}")

        assertEquals(ThemeMode.SYSTEM, appearance.themeMode)
    }

    /**
     * Use case: a preferences file written by a newer version carries additional properties, so
     * reading it ignores what is unknown instead of failing.
     */
    @Test
    fun ignoresUnknownProperties() {
        val appearance: Appearance = mapper.readValue("""{"themeMode":"LIGHT","accent":"indigo"}""")

        assertEquals(Appearance(themeMode = ThemeMode.LIGHT), appearance)
    }

    /**
     * Use case: the appearance is part of the preferences document, so it is written and read back
     * together with the other settings.
     */
    @Test
    fun roundTripsInsidePreferences() {
        val preferences = Preferences(appearance = Appearance(themeMode = ThemeMode.DARK))

        val restored: Preferences = mapper.readValue(mapper.writeValueAsString(preferences))

        assertEquals(preferences, restored)
    }
}

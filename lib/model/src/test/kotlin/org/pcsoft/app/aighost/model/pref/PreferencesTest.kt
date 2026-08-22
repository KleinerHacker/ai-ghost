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

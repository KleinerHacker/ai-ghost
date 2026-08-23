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

package org.pcsoft.app.aighost.model

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import org.pcsoft.app.aighost.model.pref.Preferences
import org.pcsoft.app.aighost.model.pref.RecentOpened
import org.pcsoft.app.aighost.model.pref.ThemeMode
import java.io.File

/**
 * Developer tests for the preferences [PreferencesStorage] holds and hands out.
 */
class PreferencesStorageStateTest {

    @TempDir
    lateinit var directory: File

    private val file: File get() = File(directory, "preferences.json")

    private lateinit var applicationFile: File

    /**
     * The storage is a singleton working on the file of the current user, so a test points it at a
     * file of its own instead of at the preferences of whoever runs the build.
     */
    @BeforeEach
    fun redirectStorage() {
        applicationFile = PreferencesStorage.defaultFile
        PreferencesStorage.defaultFile = file
    }

    /**
     * The storage is a singleton, so both the redirected file and the values a test set would
     * survive into the next one.
     */
    @AfterEach
    fun restoreStorage() {
        PreferencesStorage.defaultFile = applicationFile
    }

    /**
     * Use case: the application starts without a stored file, so the preferences in effect are the
     * defaults instead of the caller having to handle a missing file.
     */
    @Test
    fun currentFallsBackToDefaults() {
        assertEquals(Preferences(), PreferencesStorage.current)
    }

    /**
     * Use case: a stored file exists, so the preferences in effect are the ones the user configured
     * earlier.
     */
    @Test
    fun currentReadsStoredFile() {
        file.writeText("""{"themeMode":"DARK"}""")

        assertEquals(ThemeMode.DARK, PreferencesStorage.current.themeMode)
    }

    /**
     * Use case: a damaged file cannot be parsed, so the application still comes up with the defaults
     * rather than without any settings.
     */
    @Test
    fun currentFallsBackToDefaultsOnMalformedFile() {
        file.writeText("{ this is not json")

        assertEquals(Preferences(), PreferencesStorage.current)
    }

    /**
     * Use case: the file is edited from outside while the application runs, so the preferences in
     * effect stay the ones read at the start until the application is started again.
     */
    @Test
    fun currentReadsTheFileOnlyOnce() {
        assertEquals(ThemeMode.SYSTEM, PreferencesStorage.current.themeMode)

        file.writeText("""{"themeMode":"DARK"}""")

        assertEquals(ThemeMode.SYSTEM, PreferencesStorage.current.themeMode)
    }

    /**
     * Use case: a part of the application holds the preferences, so it keeps working on them after
     * the file was read again instead of using an instance nobody else knows.
     */
    @Test
    fun keepsTheSameInstanceAcrossReload() {
        val before = PreferencesStorage.current

        file.writeText("""{"themeMode":"LIGHT"}""")
        assertTrue(PreferencesStorage.load().isRight())

        assertSame(before, PreferencesStorage.current)
        assertEquals(ThemeMode.LIGHT, before.themeMode)
    }

    /**
     * Use case: the file is read again while the application runs, so every property the new
     * document brought is in effect on the instance the application works on.
     */
    @Test
    fun reloadAppliesEveryPropertyOfTheDocument() {
        file.writeText("""{"themeMode":"DARK","recentOpened":{"max":10,"entries":["a.json"]}}""")

        assertTrue(PreferencesStorage.load().isRight())

        assertEquals(ThemeMode.DARK, PreferencesStorage.current.themeMode)
        assertEquals(listOf("a.json"), PreferencesStorage.current.recentOpened.entries)
    }

    /**
     * Use case: the storage is pointed at another file, so the next access answers with the values
     * of the new file.
     */
    @Test
    fun redirectingTheFileReadsTheNewOne() {
        assertEquals(ThemeMode.SYSTEM, PreferencesStorage.current.themeMode)

        val other = File(directory, "other-preferences.json")
        other.writeText("""{"themeMode":"LIGHT"}""")
        PreferencesStorage.defaultFile = other

        assertEquals(ThemeMode.LIGHT, PreferencesStorage.current.themeMode)
    }

    /**
     * Use case: the user changes a setting and saves, so the change is in effect and stored in the
     * file the application works on.
     */
    @Test
    fun changedSettingsAreInEffectAndSaved() {
        PreferencesStorage.current.themeMode = ThemeMode.DARK
        PreferencesStorage.current.recentOpened = RecentOpened().add("a.json")

        assertTrue(PreferencesStorage.save().isRight())
        assertTrue(PreferencesStorage.load().isRight())

        assertEquals(ThemeMode.DARK, PreferencesStorage.current.themeMode)
        assertEquals(listOf("a.json"), PreferencesStorage.current.recentOpened.entries)
    }
}

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
import org.junit.jupiter.api.Assertions.*
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
     *
     * The temporary directory carries no file yet, so the load fails and the defaults are put in
     * place the way the application answers a missing file. A test needing another state loads or
     * resets on its own.
     */
    @BeforeEach
    fun redirectStorage() {
        applicationFile = PreferencesStorage.defaultFile
        PreferencesStorage.defaultFile = file
        establishPreferences()
    }

    /**
     * The storage is a singleton, so both the redirected file and the values a test set would
     * survive into the next one.
     */
    @AfterEach
    fun restoreStorage() {
        PreferencesStorage.defaultFile = applicationFile
        establishPreferences()
    }

    /** Loads the preferences and answers a failure with the defaults, the way the application does. */
    private fun establishPreferences() {
        if (PreferencesStorage.load().isLeft()) {
            PreferencesStorage.reset()
        }
    }

    /**
     * Use case: the application starts without a stored file, so nothing was established yet and
     * reading the preferences fails instead of handing out settings nobody agreed on.
     */
    @Test
    fun currentFailsWithoutAStoredFile() {
        assertTrue(PreferencesStorage.load().isLeft())
    }

    /**
     * Use case: the caller answered a failed read by resetting, so the defaults are the preferences
     * in effect from then on.
     */
    @Test
    fun resetPutsTheDefaultsInPlace() {
        assertTrue(PreferencesStorage.load().isLeft())

        PreferencesStorage.reset()

        assertEquals(Preferences(), PreferencesStorage.current)
    }

    /**
     * Use case: a stored file exists, so the preferences in effect are the ones the user configured
     * earlier once loaded.
     */
    @Test
    fun currentReadsStoredFile() {
        file.writeText("""{"themeMode":"DARK"}""")
        assertTrue(PreferencesStorage.load().isRight())

        assertEquals(ThemeMode.DARK, PreferencesStorage.current.themeMode)
    }

    /**
     * Use case: a damaged file cannot be parsed, so the storage stays unloaded and the caller has to
     * decide what happens to the file before any settings are handed out.
     */
    @Test
    fun malformedFileLeavesTheStorageUnloaded() {
        file.writeText("{ this is not json")
        assertTrue(PreferencesStorage.load().isLeft())

        PreferencesStorage.reset()
        assertEquals(Preferences(), PreferencesStorage.current)
    }

    /**
     * Use case: the file is edited from outside while the application runs, so the preferences in
     * effect stay in-memory until an explicit load is performed.
     */
    @Test
    fun currentDoesNotReadTheDiskAutomatically() {
        assertEquals(ThemeMode.SYSTEM, PreferencesStorage.current.themeMode)

        file.writeText("""{"themeMode":"DARK"}""")

        assertEquals(ThemeMode.SYSTEM, PreferencesStorage.current.themeMode)
    }

    /**
     * Use case: the file is read again while the application runs, so the document brings the
     * preferences in effect as a new instance and whoever held the previous one asks again.
     */
    @Test
    fun reloadPutsANewInstanceInPlace() {
        val before = PreferencesStorage.current

        file.writeText("""{"themeMode":"LIGHT"}""")
        assertTrue(PreferencesStorage.load().isRight())

        assertNotSame(before, PreferencesStorage.current)
        assertEquals(ThemeMode.LIGHT, PreferencesStorage.current.themeMode)
    }

    /**
     * Use case: the file is read again while the application runs, so every property the new
     * document brought is in effect on the preferences the application works on.
     */
    @Test
    fun reloadAppliesEveryPropertyOfTheDocument() {
        file.writeText("""{"themeMode":"DARK","recentOpened":{"max":10,"entries":["a.json"]}}""")

        assertTrue(PreferencesStorage.load().isRight())

        assertEquals(ThemeMode.DARK, PreferencesStorage.current.themeMode)
        assertEquals(listOf("a.json"), PreferencesStorage.current.recentOpened.entries)
    }

    /**
     * Use case: the storage is pointed at another file, so loading reads the values of the new file.
     */
    @Test
    fun redirectingTheFileReadsTheNewOne() {
        assertEquals(ThemeMode.SYSTEM, PreferencesStorage.current.themeMode)

        val other = File(directory, "other-preferences.json")
        other.writeText("""{"themeMode":"LIGHT"}""")
        PreferencesStorage.defaultFile = other
        assertTrue(PreferencesStorage.load().isRight())

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

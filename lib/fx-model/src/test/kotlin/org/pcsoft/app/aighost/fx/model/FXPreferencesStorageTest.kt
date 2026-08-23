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

package org.pcsoft.app.aighost.fx.model

import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import org.pcsoft.app.aighost.fx.model.pref.RecentOpenedProperty
import org.pcsoft.app.aighost.model.PreferencesStorage
import org.pcsoft.app.aighost.model.pref.RecentOpened
import org.pcsoft.app.aighost.model.pref.ThemeMode
import java.io.File

/**
 * Developer tests for [FXPreferencesStorage].
 *
 * The storage hands the preferences of the user to the user interface as a property tree, and it
 * replaces the object behind that tree whenever the preferences are loaded from the file or reset to
 * the defaults. Every control of the preferences dialog hangs on one of those properties, so such an
 * exchange has to reach every single one of them - the preferences themselves, the recently opened
 * files nested in them and every field of both objects. A property that stays quiet would leave the
 * old value on screen, which is what these tests rule out.
 *
 * The storage reads a file in the home directory of the user, so the tests move the home directory
 * into a temporary one before the storage is touched for the first time. The real preferences of the
 * user are therefore never read and never written.
 */
class FXPreferencesStorageTest {

    companion object {

        /** Stands in for the home directory of the user while the tests run. */
        @JvmStatic
        @TempDir
        lateinit var homeDirectory: File

        private var originalUserHome: String? = null

        /**
         * Points the home directory at the temporary one and establishes the defaults.
         *
         * Both has to happen before [FXPreferencesStorage] is touched for the first time: the
         * storage resolves its file when it is initialised, and reading preferences that were never
         * loaded throws.
         */
        @JvmStatic
        @BeforeAll
        fun prepareStorage() {
            originalUserHome = System.getProperty("user.home")
            System.setProperty("user.home", homeDirectory.absolutePath)

            PreferencesStorage.reset()

            assertTrue(
                PreferencesStorage.defaultFile.absolutePath.startsWith(homeDirectory.absolutePath)
            ) {
                "The preferences storage was already initialised on the real home directory " +
                        "(${PreferencesStorage.defaultFile}), so this test would read and write the " +
                        "preferences of the user"
            }
        }

        /** Hands the home directory of the user back, so no following test works on the temporary one. */
        @JvmStatic
        @AfterAll
        fun restoreUserHome() {
            originalUserHome?.let { System.setProperty("user.home", it) }
        }
    }

    private lateinit var recentOpenedProperty: RecentOpenedProperty
    private lateinit var recorder: ChangeRecorder

    /**
     * Puts every property of the tree under observation, from the preferences themselves down to
     * every single field.
     */
    @BeforeEach
    fun setUp() {
        recentOpenedProperty = FXPreferencesStorage.current.recentOpenedProperty as RecentOpenedProperty

        recorder = ChangeRecorder()
        recorder.watch("preferences", FXPreferencesStorage.current)
        recorder.watch("preferences.themeMode", FXPreferencesStorage.current.themeModeProperty)
        recorder.watch("preferences.recentOpened", recentOpenedProperty)
        recorder.watch("preferences.recentOpened.max", recentOpenedProperty.maxProperty)
        recorder.watch("preferences.recentOpened.entries", recentOpenedProperty.entriesProperty)
    }

    /**
     * Loading the preferences from the file has to reach every property of the tree.
     *
     * The defaults are in effect, a file carrying a different value in every single field is written
     * next to them, and the storage is asked to load it. Afterwards every property of the tree - the
     * preferences, the recently opened files and every field of both - must have reported a change,
     * and each of them must hand out the loaded value.
     */
    @Test
    fun `loading the preferences fires a change on every property of the tree`() {
        FXPreferencesStorage.reset()
        writePreferencesFile()
        recorder.reset()

        val result = FXPreferencesStorage.load()

        assertTrue(result.isRight()) { "Loading the written preferences failed: $result" }
        recorder.assertAllFired("Loading the preferences")
        assertEquals(ThemeMode.DARK, FXPreferencesStorage.current.themeMode)
        assertEquals(5, recentOpenedProperty.max)
        assertEquals(listOf("first-book.aig", "second-book.aig"), recentOpenedProperty.entries.toList())
    }

    /**
     * Resetting the preferences has to reach every property of the tree.
     *
     * The preferences loaded from the file are in effect, so every field differs from its default.
     * After the reset every property of the tree must have reported a change and must hand out the
     * default again.
     */
    @Test
    fun `resetting the preferences fires a change on every property of the tree`() {
        writePreferencesFile()
        assertTrue(FXPreferencesStorage.load().isRight()) { "The test could not establish loaded preferences" }
        recorder.reset()

        FXPreferencesStorage.reset()

        recorder.assertAllFired("Resetting the preferences")
        assertEquals(ThemeMode.SYSTEM, FXPreferencesStorage.current.themeMode)
        assertEquals(RecentOpened.DEFAULT_MAX, recentOpenedProperty.max)
        assertEquals(emptyList<String>(), recentOpenedProperty.entries.toList())
    }

    /**
     * A reset that changes nothing has to keep the whole tree quiet.
     *
     * The defaults are already in effect, so a second reset puts the same values in place. No
     * property may report a change for that, otherwise every bound control would be redrawn for
     * nothing.
     */
    @Test
    fun `resetting onto the same values keeps every property quiet`() {
        FXPreferencesStorage.reset()
        recorder.reset()

        FXPreferencesStorage.reset()

        recorder.assertNoneFired("Resetting onto the same values")
    }

    /**
     * A failing load must not touch the property tree.
     *
     * Nothing is stored yet, so the storage answers with [PreferencesStorage.Error.NotFound]. The
     * preferences in effect stay what they were, so no property may report a change.
     */
    @Test
    fun `a load that finds no file keeps every property quiet`() {
        FXPreferencesStorage.reset()
        preferencesFile().delete()
        recorder.reset()

        val result = FXPreferencesStorage.load()

        assertTrue(result.isLeft()) { "Loading a missing file was expected to fail, but was $result" }
        recorder.assertNoneFired("A load that finds no file")
        assertEquals(ThemeMode.SYSTEM, FXPreferencesStorage.current.themeMode)
    }

    /** The file the storage reads the preferences from while the tests run. */
    private fun preferencesFile(): File = PreferencesStorage.defaultFile

    /**
     * Writes preferences carrying a value different from the default in every single field, so a
     * property that does not report a change can only be a property that was not refreshed.
     */
    private fun writePreferencesFile() {
        val file = preferencesFile()
        file.parentFile?.mkdirs()
        file.writeText(
            """
            {
              "recentOpened": {
                "max": 5,
                "entries": ["first-book.aig", "second-book.aig"]
              },
              "themeMode": "DARK"
            }
            """.trimIndent()
        )
    }
}

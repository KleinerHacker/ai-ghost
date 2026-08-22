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

import arrow.core.getOrElse
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import org.pcsoft.app.aighost.model.pref.Preferences
import org.pcsoft.app.aighost.model.pref.RecentOpened
import org.pcsoft.app.aighost.model.pref.ThemeMode
import java.io.File

/**
 * Developer tests for [PreferencesStorage] and [PreferencesStorage.Error].
 */
class PreferencesStorageTest {

    @TempDir
    lateinit var directory: File

    private val file: File get() = File(directory, "preferences.json")

    /**
     * Use case: the application starts on a fresh installation, so loading reports that nothing is
     * stored yet instead of failing, and the caller can fall back to the defaults.
     */
    @Test
    fun reportsNotFoundWithoutStoredFile() {
        val result = PreferencesStorage.load(file)

        assertEquals(PreferencesStorage.Error.NotFound(file), result.leftOrNull())
        assertEquals(Preferences(), result.getOrElse { Preferences() })
    }

    /**
     * Use case: the configured path exists but is a directory, so loading reports that the path is
     * not a file rather than pretending nothing is stored yet.
     */
    @Test
    fun reportsNotAFileForDirectory() {
        val asDirectory = File(directory, "preferences.json").apply { mkdirs() }

        val result = PreferencesStorage.load(asDirectory)

        assertEquals(PreferencesStorage.Error.NotAFile(asDirectory), result.leftOrNull())
    }

    /**
     * Use case: the caller distinguishes a fresh installation from a misconfigured path, so a
     * directory never arrives as [PreferencesStorage.Error.NotFound] and a missing file never as
     * [PreferencesStorage.Error.NotAFile].
     */
    @Test
    fun separatesMissingPathFromWrongPath() {
        val asDirectory = File(directory, "config-directory").apply { mkdirs() }
        val missing = File(directory, "missing.json")

        val onDirectory = PreferencesStorage.load(asDirectory).leftOrNull()
        val onMissing = PreferencesStorage.load(missing).leftOrNull()

        assertInstanceOf(PreferencesStorage.Error.NotAFile::class.java, onDirectory)
        assertInstanceOf(PreferencesStorage.Error.NotFound::class.java, onMissing)
    }

    /**
     * Use case: an error is shown to the user, so every failure names the file it happened on and
     * the caller does not have to remember which path it asked for.
     */
    @Test
    fun everyErrorCarriesItsFile() {
        val asDirectory = File(directory, "config-directory").apply { mkdirs() }
        val broken = File(directory, "broken.json").apply { writeText("{ this is not json") }

        assertEquals(file, PreferencesStorage.load(file).leftOrNull()?.file)
        assertEquals(asDirectory, PreferencesStorage.load(asDirectory).leftOrNull()?.file)
        assertEquals(broken, PreferencesStorage.load(broken).leftOrNull()?.file)
    }

    /**
     * Use case: the target path is a directory, so saving reports it as not a file and leaves the
     * directory untouched instead of replacing it with the preferences document.
     */
    @Test
    fun reportsNotAFileWhenSavingOntoDirectory() {
        val asDirectory = File(directory, "preferences.json").apply { mkdirs() }

        val result = PreferencesStorage.save(Preferences(), asDirectory)

        assertEquals(PreferencesStorage.Error.NotAFile(asDirectory), result.leftOrNull())
        assertTrue(asDirectory.isDirectory)
    }

    /**
     * Use case: the user changes a setting and the application is restarted, so the preferences come
     * back from disk exactly as they were saved.
     */
    @Test
    fun roundTripsPreferences() {
        val preferences = Preferences(
            recentOpened = RecentOpened(max = 3).add("a.json").add("b.json"),
            themeMode = ThemeMode.DARK
        )

        assertTrue(PreferencesStorage.save(preferences, file).isRight())
        assertEquals(preferences, PreferencesStorage.load(file).getOrNull())
    }

    /**
     * Use case: the preferences are saved for the first time, so the missing configuration directory
     * is created instead of the write failing.
     */
    @Test
    fun createsMissingParentDirectory() {
        val nested = File(directory, "config/nested/preferences.json")

        assertTrue(PreferencesStorage.save(Preferences(), nested).isRight())
        assertTrue(nested.isFile)
    }

    /**
     * Use case: the user opens the file to edit it by hand, so it is written as indented JSON rather
     * than as a single line.
     */
    @Test
    fun writesIndentedJson() {
        PreferencesStorage.save(Preferences(themeMode = ThemeMode.LIGHT), file)

        val content = file.readText()

        assertTrue(content.contains("\n"), "expected indented JSON but was: $content")
        assertTrue(content.contains("\"themeMode\""))
    }

    /**
     * Use case: the preferences are saved again, so the previous document is replaced instead of
     * being appended to, and no temporary file is left behind.
     */
    @Test
    fun overwritesPreviousFileWithoutLeavingTemporaries() {
        PreferencesStorage.save(Preferences(themeMode = ThemeMode.LIGHT), file)
        PreferencesStorage.save(Preferences(themeMode = ThemeMode.DARK), file)

        assertEquals(ThemeMode.DARK, PreferencesStorage.load(file).getOrNull()?.themeMode)
        assertEquals(listOf("preferences.json"), directory.list()?.sorted())
    }

    /**
     * Use case: the file was damaged by another program, so loading reports it as malformed and
     * carries the parse failure instead of throwing at start up.
     */
    @Test
    fun reportsMalformedFileAsError() {
        file.writeText("{ this is not json")

        val error = PreferencesStorage.load(file).leftOrNull()

        val malformed = assertInstanceOf(PreferencesStorage.Error.Malformed::class.java, error)
        assertEquals(file, malformed.file)
    }

    /**
     * Use case: the file holds valid JSON that is not a preferences document, so loading reports it
     * as malformed rather than returning half filled preferences.
     */
    @Test
    fun reportsWrongDocumentAsError() {
        file.writeText("""{"themeMode":"NEON"}""")

        val error = PreferencesStorage.load(file).leftOrNull()

        assertInstanceOf(PreferencesStorage.Error.Malformed::class.java, error)
    }

    /**
     * Use case: an older preferences file does not know the newer properties yet, so it is read with
     * the defaults filled in instead of being rejected.
     */
    @Test
    fun readsPartialDocumentWithDefaults() {
        file.writeText("""{"themeMode":"LIGHT"}""")

        val preferences = PreferencesStorage.load(file).getOrNull()

        assertEquals(ThemeMode.LIGHT, preferences?.themeMode)
        assertEquals(RecentOpened(max = 10), preferences?.recentOpened)
    }

    /**
     * Use case: no explicit file is passed, so both operations work on the file inside the user's
     * home directory that the application ships with.
     */
    @Test
    fun defaultFileLivesInUserHome() {
        val home = File(System.getProperty("user.home"))

        assertEquals(File(home, ".ai-ghost/preferences.json"), PreferencesStorage.defaultFile)
    }
}

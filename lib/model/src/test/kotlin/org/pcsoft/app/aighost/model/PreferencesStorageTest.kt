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
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import org.pcsoft.app.aighost.model.pref.RecentOpened
import org.pcsoft.app.aighost.model.pref.ThemeMode
import java.io.File

/**
 * Developer tests for the file handling of [PreferencesStorage] and for [PreferencesStorage.Error].
 */
class PreferencesStorageTest {

    @TempDir
    lateinit var directory: File

    private val file: File get() = File(directory, "preferences.json")

    private lateinit var applicationFile: File

    /**
     * The storage is a singleton working on the file of the current user, so a test points it at a
     * file of its own instead of at the preferences of whoever runs the build.
     *
     * The temporary directory carries no file yet, so the load fails and the defaults are put in
     * place the way the application answers a missing file.
     */
    @BeforeEach
    fun redirectStorage() {
        applicationFile = PreferencesStorage.defaultFile
        PreferencesStorage.defaultFile = file
        establishPreferences()
    }

    /**
     * The storage is a singleton, so the redirected file would survive into the next test and let it
     * work on preferences that do not belong to it.
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
     * Use case: the application starts on a fresh installation, so loading reports that nothing is
     * stored yet instead of failing, and leaves the storage unloaded.
     */
    @Test
    fun reportsNotFoundWithoutStoredFile() {
        val result = PreferencesStorage.load()

        assertEquals(PreferencesStorage.Error.NotFound(file), result.leftOrNull())
        assertThrows(IllegalStateException::class.java) { PreferencesStorage.current }
    }

    /**
     * Use case: the configured path exists but is a directory, so loading reports that the path is
     * not a file rather than pretending nothing is stored yet.
     */
    @Test
    fun reportsNotAFileForDirectory() {
        val asDirectory = File(directory, "preferences.json").apply { mkdirs() }
        PreferencesStorage.defaultFile = asDirectory

        val result = PreferencesStorage.load()

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

        PreferencesStorage.defaultFile = asDirectory
        val onDirectory = PreferencesStorage.load().leftOrNull()
        PreferencesStorage.defaultFile = missing
        val onMissing = PreferencesStorage.load().leftOrNull()

        assertInstanceOf(PreferencesStorage.Error.NotAFile::class.java, onDirectory)
        assertInstanceOf(PreferencesStorage.Error.NotFound::class.java, onMissing)
    }

    /**
     * Use case: an error is shown to the user, so every failure names the file it happened on and
     * the caller does not have to remember which path the storage works on.
     */
    @Test
    fun everyErrorCarriesItsFile() {
        val asDirectory = File(directory, "config-directory").apply { mkdirs() }
        val broken = File(directory, "broken.json").apply { writeText("{ this is not json") }

        assertEquals(file, PreferencesStorage.load().leftOrNull()?.file)
        PreferencesStorage.defaultFile = asDirectory
        assertEquals(asDirectory, PreferencesStorage.load().leftOrNull()?.file)
        PreferencesStorage.defaultFile = broken
        assertEquals(broken, PreferencesStorage.load().leftOrNull()?.file)
    }

    /**
     * Use case: the stored file cannot be read, so the storage stays unloaded and the caller has to
     * decide what happens to the file before any settings are handed out again.
     */
    @Test
    fun failedLoadLeavesTheStorageUnloaded() {
        PreferencesStorage.current.themeMode = ThemeMode.DARK

        assertTrue(PreferencesStorage.load().isLeft())

        assertThrows(IllegalStateException::class.java) { PreferencesStorage.current }
    }

    /**
     * Use case: the target path is a directory, so saving reports it as not a file and leaves the
     * directory untouched instead of replacing it with the preferences document.
     */
    @Test
    fun reportsNotAFileWhenSavingOntoDirectory() {
        val asDirectory = File(directory, "preferences.json").apply { mkdirs() }
        PreferencesStorage.defaultFile = asDirectory

        val result = PreferencesStorage.save()

        assertEquals(PreferencesStorage.Error.NotAFile(asDirectory), result.leftOrNull())
        assertTrue(asDirectory.isDirectory)
    }

    /**
     * Use case: the user changes a setting and the application is restarted, so the preferences come
     * back from disk exactly as they were saved.
     */
    @Test
    fun roundTripsPreferences() {
        PreferencesStorage.current.recentOpened = RecentOpened(max = 3).add("a.json").add("b.json")
        PreferencesStorage.current.themeMode = ThemeMode.DARK

        assertTrue(PreferencesStorage.save().isRight())
        assertTrue(PreferencesStorage.load().isRight())

        assertEquals(
            RecentOpened(max = 3, entries = listOf("b.json", "a.json")),
            PreferencesStorage.current.recentOpened
        )
        assertEquals(ThemeMode.DARK, PreferencesStorage.current.themeMode)
    }

    /**
     * Use case: the preferences are saved for the first time, so the missing configuration directory
     * is created instead of the write failing.
     */
    @Test
    fun createsMissingParentDirectory() {
        val nested = File(directory, "config/nested/preferences.json")
        PreferencesStorage.defaultFile = nested

        assertTrue(PreferencesStorage.save().isRight())
        assertTrue(nested.isFile)
    }

    /**
     * Use case: the user opens the file to edit it by hand, so it is written as indented JSON rather
     * than as a single line.
     */
    @Test
    fun writesIndentedJson() {
        PreferencesStorage.current.themeMode = ThemeMode.LIGHT
        PreferencesStorage.save()

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
        PreferencesStorage.current.themeMode = ThemeMode.LIGHT
        PreferencesStorage.save()
        PreferencesStorage.current.themeMode = ThemeMode.DARK
        PreferencesStorage.save()

        assertTrue(PreferencesStorage.load().isRight())
        assertEquals(ThemeMode.DARK, PreferencesStorage.current.themeMode)
        assertEquals(listOf("preferences.json"), directory.list()?.sorted())
    }

    /**
     * Use case: the file was damaged by another program, so loading reports it as malformed and
     * carries the parse failure instead of throwing at start up.
     */
    @Test
    fun reportsMalformedFileAsError() {
        file.writeText("{ this is not json")

        val error = PreferencesStorage.load().leftOrNull()

        val malformed = assertInstanceOf(PreferencesStorage.Error.Malformed::class.java, error)
        assertEquals(file, malformed.file)
    }

    /**
     * Use case: the file holds valid JSON that is not a preferences document, so loading reports it
     * as malformed rather than putting half filled preferences in effect.
     */
    @Test
    fun reportsWrongDocumentAsError() {
        file.writeText("""{"themeMode":"NEON"}""")

        val error = PreferencesStorage.load().leftOrNull()

        assertInstanceOf(PreferencesStorage.Error.Malformed::class.java, error)
    }

    /**
     * Use case: an older preferences file does not know the newer properties yet, so it is read with
     * the defaults filled in instead of being rejected.
     */
    @Test
    fun readsPartialDocumentWithDefaults() {
        file.writeText("""{"themeMode":"LIGHT"}""")

        assertTrue(PreferencesStorage.load().isRight())

        assertEquals(ThemeMode.LIGHT, PreferencesStorage.current.themeMode)
        assertEquals(RecentOpened(max = 10), PreferencesStorage.current.recentOpened)
    }

    /**
     * Use case: the application is started without anything redirecting the storage, so it works on
     * the file inside the user's home directory that the application ships with.
     */
    @Test
    fun defaultFileLivesInUserHome() {
        val home = File(System.getProperty("user.home"))

        assertEquals(File(home, ".ai-ghost/preferences.json"), applicationFile)
    }
}

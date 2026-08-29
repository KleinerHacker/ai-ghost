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
import org.pcsoft.app.aighost.model.pref.Ai
import org.pcsoft.app.aighost.model.pref.Preferences
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
     * The storage works on the file of the current user, so a test points it at a file of its own
     * instead of at the preferences of whoever runs the build.
     */
    @BeforeEach
    fun redirectStorage() {
        applicationFile = PreferencesStorage.defaultFile
        PreferencesStorage.defaultFile = file
    }

    /**
     * The storage is a singleton, so the redirected file would survive into the next test and let it
     * work on preferences that do not belong to it.
     */
    @AfterEach
    fun restoreStorage() {
        PreferencesStorage.defaultFile = applicationFile
    }

    /**
     * Use case: the application starts on a fresh installation, so loading reports that nothing is
     * stored yet instead of failing, and the caller answers that with the defaults.
     */
    @Test
    fun reportsNotFoundWithoutStoredFile() {
        val result = PreferencesStorage.load()

        assertEquals(PreferencesStorage.Error.NotFound(file), result.leftOrNull())
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
     * Use case: the target path is a directory, so saving reports it as not a file and leaves the
     * directory untouched instead of replacing it with the preferences document.
     */
    @Test
    fun reportsNotAFileWhenSavingOntoDirectory() {
        val asDirectory = File(directory, "preferences.json").apply { mkdirs() }
        PreferencesStorage.defaultFile = asDirectory

        val result = PreferencesStorage.save(Preferences())

        assertEquals(PreferencesStorage.Error.NotAFile(asDirectory), result.leftOrNull())
        assertTrue(asDirectory.isDirectory)
    }

    /**
     * Use case: the user changes a setting and the application is restarted, so the preferences come
     * back from disk exactly as they were saved - every block of them, not only the topmost one.
     */
    @Test
    fun roundTripsPreferences() {
        val preferences = Preferences().apply {
            recentOpened = RecentOpened(max = 3).add("a.json").add("b.json")
            appearance.themeMode = ThemeMode.DARK
            ai = Ai(maxStoryCharacters = 4200, maxStyleCharacters = 750)
        }

        assertTrue(PreferencesStorage.save(preferences).isRight())
        val loaded = PreferencesStorage.load().getOrNull()

        assertNotNull(loaded)
        assertEquals(RecentOpened(max = 3, entries = listOf("b.json", "a.json")), loaded?.recentOpened)
        assertEquals(ThemeMode.DARK, loaded?.appearance?.themeMode)
        assertEquals(Ai(maxStoryCharacters = 4200, maxStyleCharacters = 750), loaded?.ai)
    }

    /**
     * Use case: the preferences are handed out to the caller, so every load builds an instance of its
     * own and two callers never write into the same object by accident.
     */
    @Test
    fun handsOutANewInstancePerLoad() {
        PreferencesStorage.save(Preferences())

        val first = PreferencesStorage.load().getOrNull()
        val second = PreferencesStorage.load().getOrNull()

        assertNotNull(first)
        assertNotNull(second)
        assertNotSame(first, second)
    }

    /**
     * Use case: the preferences are saved for the first time, so the missing configuration directory
     * is created instead of the write failing.
     */
    @Test
    fun createsMissingParentDirectory() {
        val nested = File(directory, "config/nested/preferences.json")
        PreferencesStorage.defaultFile = nested

        assertTrue(PreferencesStorage.save(Preferences()).isRight())
        assertTrue(nested.isFile)
    }

    /**
     * Use case: the user opens the file to edit it by hand, so it is written as indented JSON rather
     * than as a single line, with every block under the name the file format promises.
     */
    @Test
    fun writesIndentedJson() {
        PreferencesStorage.save(Preferences().apply { appearance.themeMode = ThemeMode.LIGHT })

        val content = file.readText()

        assertTrue(content.contains("\n"), "expected indented JSON but was: $content")
        assertTrue(content.contains("\"appearance\""))
        assertTrue(content.contains("\"themeMode\""))
        assertTrue(content.contains("\"ai\""))
    }

    /**
     * Use case: the preferences are saved again, so the previous document is replaced instead of
     * being appended to, and no temporary file is left behind.
     */
    @Test
    fun overwritesPreviousFileWithoutLeavingTemporaries() {
        PreferencesStorage.save(Preferences().apply { appearance.themeMode = ThemeMode.LIGHT })
        PreferencesStorage.save(Preferences().apply { appearance.themeMode = ThemeMode.DARK })

        assertEquals(ThemeMode.DARK, PreferencesStorage.load().getOrNull()?.appearance?.themeMode)
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
     * as malformed rather than handing out half filled preferences.
     */
    @Test
    fun reportsWrongDocumentAsError() {
        file.writeText("""{"appearance":{"themeMode":"NEON"}}""")

        val error = PreferencesStorage.load().leftOrNull()

        assertInstanceOf(PreferencesStorage.Error.Malformed::class.java, error)
    }

    /**
     * Use case: an older preferences file does not know the newer properties yet, so it is read with
     * the defaults filled in instead of being rejected.
     */
    @Test
    fun readsPartialDocumentWithDefaults() {
        file.writeText("""{"appearance":{"themeMode":"LIGHT"}}""")

        val loaded = PreferencesStorage.load().getOrNull()

        assertNotNull(loaded)
        assertEquals(ThemeMode.LIGHT, loaded?.appearance?.themeMode)
        assertEquals(RecentOpened(max = 10), loaded?.recentOpened)
        assertEquals(Ai(), loaded?.ai)
    }

    /**
     * Use case: the file is redirected to another document while the application runs, so the next
     * load reads that document and not the one read before.
     */
    @Test
    fun redirectingTheFileReadsTheNewOne() {
        PreferencesStorage.save(Preferences().apply { appearance.themeMode = ThemeMode.LIGHT })

        val other = File(directory, "other.json")
        PreferencesStorage.defaultFile = other
        PreferencesStorage.save(Preferences().apply { appearance.themeMode = ThemeMode.DARK })

        assertEquals(ThemeMode.DARK, PreferencesStorage.load().getOrNull()?.appearance?.themeMode)
        PreferencesStorage.defaultFile = file
        assertEquals(ThemeMode.LIGHT, PreferencesStorage.load().getOrNull()?.appearance?.themeMode)
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

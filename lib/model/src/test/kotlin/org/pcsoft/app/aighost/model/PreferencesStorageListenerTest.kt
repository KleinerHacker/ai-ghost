package org.pcsoft.app.aighost.model

import org.junit.jupiter.api.AfterEach
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
 * Developer tests for the kept state and the change notification of [PreferencesStorage].
 */
class PreferencesStorageListenerTest {

    @TempDir
    lateinit var directory: File

    private val file: File get() = File(directory, "preferences.json")

    private val recorded = mutableListOf<Pair<Preferences, Preferences>>()

    private val listener = PreferencesStorage.Listener { old, new -> recorded += old to new }

    /**
     * The storage is a singleton, so a listener registered by a test would survive into the next
     * one and count changes that do not belong to it.
     */
    @AfterEach
    fun removeListener() {
        PreferencesStorage.removeListener(listener)
    }

    /**
     * Use case: the application starts without a stored file, so the preferences in effect are the
     * defaults instead of the caller having to handle a missing file.
     */
    @Test
    fun currentFallsBackToDefaults() {
        assertEquals(Preferences(), PreferencesStorage.current(file))
    }

    /**
     * Use case: a stored file exists, so the preferences in effect are the ones the user configured
     * earlier.
     */
    @Test
    fun currentReadsStoredFile() {
        PreferencesStorage.save(Preferences(themeMode = ThemeMode.DARK), file)

        assertEquals(ThemeMode.DARK, PreferencesStorage.current(file).themeMode)
    }

    /**
     * Use case: a damaged file cannot be parsed, so the application still comes up with the defaults
     * rather than without any settings.
     */
    @Test
    fun currentFallsBackToDefaultsOnMalformedFile() {
        file.writeText("{ this is not json")

        assertEquals(Preferences(), PreferencesStorage.current(file))
    }

    /**
     * Use case: the user changes a setting, so the new preferences are written, become the ones in
     * effect and are handed back to the caller.
     */
    @Test
    fun updateStoresAndReturnsNewPreferences() {
        val result = PreferencesStorage.update(file) { it.copy(themeMode = ThemeMode.DARK) }

        assertEquals(ThemeMode.DARK, result.getOrNull()?.themeMode)
        assertEquals(ThemeMode.DARK, PreferencesStorage.current(file).themeMode)
        assertEquals(ThemeMode.DARK, PreferencesStorage.load(file).getOrNull()?.themeMode)
    }

    /**
     * Use case: a part of the application follows the theme, so it is notified with the previous and
     * the new preferences as soon as a change was written.
     */
    @Test
    fun notifiesListenerWithOldAndNewPreferences() {
        PreferencesStorage.addListener(listener)

        PreferencesStorage.update(file) { it.copy(themeMode = ThemeMode.LIGHT) }

        assertEquals(1, recorded.size)
        assertEquals(ThemeMode.SYSTEM, recorded[0].first.themeMode)
        assertEquals(ThemeMode.LIGHT, recorded[0].second.themeMode)
    }

    /**
     * Use case: any field of the preferences may change, so a changed recent files list notifies just
     * like a changed theme does.
     */
    @Test
    fun notifiesOnAnyChangedField() {
        PreferencesStorage.addListener(listener)

        PreferencesStorage.update(file) { it.copy(recentOpened = it.recentOpened.add("a.json")) }

        assertEquals(1, recorded.size)
        assertEquals(emptyList<String>(), recorded[0].first.recentOpened.entries)
        assertEquals(listOf("a.json"), recorded[0].second.recentOpened.entries)
    }

    /**
     * Use case: the user picks the setting that is already active, so nothing is written and no
     * listener is woken for a change that did not happen.
     */
    @Test
    fun doesNotNotifyWhenNothingChanged() {
        PreferencesStorage.update(file) { it.copy(themeMode = ThemeMode.DARK) }
        PreferencesStorage.addListener(listener)

        val result = PreferencesStorage.update(file) { it.copy(themeMode = ThemeMode.DARK) }

        assertEquals(ThemeMode.DARK, result.getOrNull()?.themeMode)
        assertTrue(recorded.isEmpty(), "expected no notification but was: $recorded")
    }

    /**
     * Use case: the preferences cannot be written because the path is a directory, so the change is
     * reported as an error, the state in effect stays as it was and no listener is misled.
     */
    @Test
    fun doesNotNotifyWhenWritingFails() {
        val asDirectory = File(directory, "blocked.json").apply { mkdirs() }
        PreferencesStorage.addListener(listener)

        val result = PreferencesStorage.update(asDirectory) { it.copy(themeMode = ThemeMode.DARK) }

        assertInstanceOf(PreferencesStorage.Error.NotAFile::class.java, result.leftOrNull())
        assertEquals(ThemeMode.SYSTEM, PreferencesStorage.current(asDirectory).themeMode)
        assertTrue(recorded.isEmpty(), "expected no notification but was: $recorded")
    }

    /**
     * Use case: several parts of the application follow the preferences, so every registered
     * listener is notified about the same change.
     */
    @Test
    fun notifiesEveryListener() {
        val second = mutableListOf<Pair<Preferences, Preferences>>()
        val secondListener = PreferencesStorage.Listener { old, new -> second += old to new }

        PreferencesStorage.addListener(listener)
        PreferencesStorage.addListener(secondListener)
        try {
            PreferencesStorage.update(file) { it.copy(themeMode = ThemeMode.DARK) }
        } finally {
            PreferencesStorage.removeListener(secondListener)
        }

        assertEquals(1, recorded.size)
        assertEquals(1, second.size)
    }

    /**
     * Use case: a window that followed the preferences is closed, so its listener stops being called
     * after it was removed.
     */
    @Test
    fun stopsNotifyingRemovedListener() {
        PreferencesStorage.addListener(listener)
        PreferencesStorage.update(file) { it.copy(themeMode = ThemeMode.DARK) }

        PreferencesStorage.removeListener(listener)
        PreferencesStorage.update(file) { it.copy(themeMode = ThemeMode.LIGHT) }

        assertEquals(1, recorded.size)
        assertEquals(ThemeMode.DARK, recorded[0].second.themeMode)
    }

    /**
     * Use case: several changes follow each other, so each one starts from the result of the
     * previous one instead of from the state the application started with.
     */
    @Test
    fun chainsSubsequentUpdates() {
        PreferencesStorage.addListener(listener)

        PreferencesStorage.update(file) { it.copy(recentOpened = it.recentOpened.add("a.json")) }
        PreferencesStorage.update(file) { it.copy(recentOpened = it.recentOpened.add("b.json")) }

        assertEquals(2, recorded.size)
        assertEquals(listOf("a.json"), recorded[1].first.recentOpened.entries)
        assertEquals(listOf("b.json", "a.json"), recorded[1].second.recentOpened.entries)
        assertEquals(
            RecentOpened(max = 10, entries = listOf("b.json", "a.json")),
            PreferencesStorage.load(file).getOrNull()?.recentOpened
        )
    }
}

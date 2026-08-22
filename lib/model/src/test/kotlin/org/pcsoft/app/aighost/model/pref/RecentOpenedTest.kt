package org.pcsoft.app.aighost.model.pref

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

/**
 * Developer tests for [RecentOpened].
 */
class RecentOpenedTest {

    private val mapper: ObjectMapper = ObjectMapper().registerKotlinModule()

    /**
     * Use case: the application starts for the first time, so the recent files list is empty and
     * keeps the default number of entries until the user configures another limit.
     */
    @Test
    fun defaultsToEmptyListWithDefaultMax() {
        val recentOpened = RecentOpened()

        assertEquals(RecentOpened.DEFAULT_MAX, recentOpened.max)
        assertEquals(emptyList<String>(), recentOpened.entries)
    }

    /**
     * Use case: a limit of zero or less would make the list useless, so such a value is rejected at
     * construction instead of producing a list that silently drops everything.
     */
    @ParameterizedTest
    @ValueSource(ints = [0, -1])
    fun rejectsNonPositiveMax(max: Int) {
        assertThrows<IllegalArgumentException> { RecentOpened(max) }
    }

    /**
     * Use case: the user opens a file, so it becomes the first entry of the list and the previously
     * opened files move down.
     */
    @Test
    fun addsNewestEntryFirst() {
        val recentOpened = RecentOpened(max = 3).add("first.json").add("second.json")

        assertEquals(listOf("second.json", "first.json"), recentOpened.entries)
    }

    /**
     * Use case: the user opens a file that is already in the list, so it moves to the front instead
     * of appearing twice.
     */
    @Test
    fun movesKnownEntryToFront() {
        val recentOpened = RecentOpened(max = 3)
            .add("a.json")
            .add("b.json")
            .add("c.json")
            .add("a.json")

        assertEquals(listOf("a.json", "c.json", "b.json"), recentOpened.entries)
    }

    /**
     * Use case: the user opens more files than the menu can show, so the oldest entries drop out and
     * the list never grows beyond the configured limit.
     */
    @Test
    fun dropsOldestEntryBeyondMax() {
        val recentOpened = RecentOpened(max = 2)
            .add("a.json")
            .add("b.json")
            .add("c.json")

        assertEquals(listOf("c.json", "b.json"), recentOpened.entries)
    }

    /**
     * Use case: a preferences file written with a larger limit is read, so the surplus entries are
     * cut back as soon as the user opens the next file.
     */
    @Test
    fun appliesMaxToEntriesReadFromFile() {
        val recentOpened = RecentOpened(max = 2, entries = listOf("a.json", "b.json", "c.json"))

        assertEquals(listOf("d.json", "a.json"), recentOpened.add("d.json").entries)
    }

    /**
     * Use case: the user deletes a file, so its entry disappears from the list while the remaining
     * order stays untouched.
     */
    @Test
    fun removesEntry() {
        val recentOpened = RecentOpened(max = 3).add("a.json").add("b.json").add("c.json")

        assertEquals(listOf("c.json", "a.json"), recentOpened.remove("b.json").entries)
    }

    /**
     * Use case: a path that was never opened is removed, so the list stays exactly as it was instead
     * of failing.
     */
    @Test
    fun removingUnknownEntryChangesNothing() {
        val recentOpened = RecentOpened(max = 3).add("a.json")

        assertEquals(recentOpened, recentOpened.remove("b.json"))
    }

    /**
     * Use case: the user clears the recent files list, so no entry is left while the configured limit
     * survives for the files opened next.
     */
    @Test
    fun clearsEntriesButKeepsMax() {
        val cleared = RecentOpened(max = 5).add("a.json").clear()

        assertEquals(emptyList<String>(), cleared.entries)
        assertEquals(5, cleared.max)
    }

    /**
     * Use case: preferences are handed to several parts of the application, so adding a file returns
     * a new instance and leaves the original list untouched.
     */
    @Test
    fun leavesOriginalUnchanged() {
        val original = RecentOpened(max = 3).add("a.json")

        original.add("b.json")

        assertEquals(listOf("a.json"), original.entries)
    }

    /**
     * Use case: the recent files list is written to disk, so limit and entries appear in the JSON
     * under the stable property names the file format promises.
     */
    @Test
    fun serialisesMaxAndEntries() {
        val json = mapper.writeValueAsString(RecentOpened(max = 2).add("a.json"))

        assertEquals("""{"max":2,"entries":["a.json"]}""", json)
    }

    /**
     * Use case: a stored preferences file is read at start up, so limit and entry order are restored
     * exactly as they were written.
     */
    @Test
    fun roundTripsThroughJson() {
        val recentOpened = RecentOpened(max = 4).add("a.json").add("b.json")

        val restored: RecentOpened = mapper.readValue(mapper.writeValueAsString(recentOpened))

        assertEquals(recentOpened, restored)
    }

    /**
     * Use case: a preferences file written by an older version does not know the limit yet, so
     * reading it falls back to the default instead of failing.
     */
    @Test
    fun readsDocumentWithoutMax() {
        val recentOpened: RecentOpened = mapper.readValue("""{"entries":["a.json"]}""")

        assertEquals(RecentOpened.DEFAULT_MAX, recentOpened.max)
        assertEquals(listOf("a.json"), recentOpened.entries)
    }

    /**
     * Use case: the recent files list is part of the preferences document, so it is written and read
     * back together with the theme selection.
     */
    @Test
    fun roundTripsInsidePreferences() {
        val preferences = Preferences(
            recentOpened = RecentOpened(max = 3).add("a.json"),
            themeMode = ThemeMode.DARK
        )

        val restored: Preferences = mapper.readValue(mapper.writeValueAsString(preferences))

        assertEquals(preferences, restored)
    }
}

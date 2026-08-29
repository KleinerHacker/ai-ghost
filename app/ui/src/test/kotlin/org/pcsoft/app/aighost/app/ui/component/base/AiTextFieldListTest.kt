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

package org.pcsoft.app.aighost.app.ui.component.base

import de.saxsys.mvvmfx.MvvmFX
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.scene.Node
import javafx.scene.Scene
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.control.TextField
import javafx.scene.image.ImageView
import javafx.scene.layout.VBox
import javafx.stage.Stage
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.pcsoft.app.aighost.app.Messages
import org.testfx.framework.junit5.ApplicationTest
import org.testfx.util.WaitForAsyncUtils
import java.util.Locale
import java.util.ResourceBundle

/**
 * Developer tests for [AiTextFieldList].
 *
 * The list shows the entries handed in from outside and writes into that very list, so both
 * directions are proven on the input lines the user really works with. Everything that changes the
 * list itself - another entry, an entry to be removed, an AI text - is only reported, so it is
 * proven as well that the list carries none of it out on its own.
 */
class AiTextFieldListTest : ApplicationTest() {

    private lateinit var list: AiTextFieldList

    /** The rows the list shows, one per entry. */
    private val rows: List<Node>
        get() = (list.lookup(".ai-list-entries") as VBox).children.toList()

    /** The hint standing in for the entries while there is none. */
    private val hint: Label
        get() = list.lookup(".ai-list-empty") as Label

    /** The plus asking for another entry. */
    private val addButton: Button
        get() = list.lookup(".ai-list-add") as Button

    override fun start(stage: Stage) {
        // No fallback, so the English base bundle is used no matter which locale the build runs under.
        MvvmFX.setGlobalResourceBundle(
            ResourceBundle.getBundle(
                Messages.BUNDLE_NAME,
                Locale.ROOT,
                ResourceBundle.Control.getNoFallbackControl(ResourceBundle.Control.FORMAT_PROPERTIES)
            )
        )

        list = AiTextFieldList()
        stage.scene = Scene(list, 400.0, 300.0)
        stage.show()
    }

    /**
     * Use case: a list of texts is handed in, so one row per entry is shown, each carrying the text
     * of its entry.
     */
    @Test
    fun entriesAreShownAsRows() {
        show("A ghost story", "Book one")

        assertEquals(2, rows.size)
        assertEquals("A ghost story", inputOfRow(0).text)
        assertEquals("Book one", inputOfRow(1).text)
    }

    /**
     * Use case: the user types into a row, so the text stands in the list handed in from outside,
     * where whoever owns it reads the change.
     */
    @Test
    fun typedTextReachesTheListHandedIn() {
        val entries = show("")

        clickOn(inputOfRow(0)).write("A ghost story")
        WaitForAsyncUtils.waitForFxEvents()

        assertEquals(listOf("A ghost story"), entries)
    }

    /**
     * Use case: an entry is rewritten past the list, so the row of that entry shows the new text and
     * keeps its place.
     */
    @Test
    fun rewrittenEntryReachesItsRow() {
        val entries = show("A ghost story", "Book one")

        interact { entries[1] = "Book two" }
        WaitForAsyncUtils.waitForFxEvents()

        assertEquals(2, rows.size)
        assertEquals("Book two", inputOfRow(1).text)
    }

    /**
     * Use case: entries are added and taken out past the list, so the rows follow and the rows left
     * behind still write into their own entry.
     */
    @Test
    fun rowsFollowAddedAndRemovedEntries() {
        val entries = show("A ghost story")

        interact { entries.add("Book one") }
        WaitForAsyncUtils.waitForFxEvents()
        assertEquals(2, rows.size)

        interact { entries.removeAt(0) }
        WaitForAsyncUtils.waitForFxEvents()
        assertEquals(1, rows.size)
        assertEquals("Book one", inputOfRow(0).text)

        interact { inputOfRow(0).text = "Book two" }
        WaitForAsyncUtils.waitForFxEvents()

        assertEquals(listOf("Book two"), entries)
    }

    /**
     * Use case: another list takes the place of the current one, so the rows show the new list and
     * write into it only, while the list left behind stays untouched.
     */
    @Test
    fun exchangedListIsFollowed() {
        val first = show("A ghost story")
        val second = FXCollections.observableArrayList("Book one", "Book two")

        interact { list.entries.set(second) }
        WaitForAsyncUtils.waitForFxEvents()
        assertEquals(2, rows.size)

        interact { inputOfRow(0).text = "Book three" }
        WaitForAsyncUtils.waitForFxEvents()

        assertEquals(listOf("Book three", "Book two"), second)
        assertEquals(listOf("A ghost story"), first)
    }

    /**
     * Use case: the list is empty and whoever shows it said what that means, so the hint stands in
     * place of the rows and gives way as soon as an entry is there.
     */
    @Test
    fun emptyListShowsTheHintFromOutside() {
        val entries = show()
        interact { list.emptyText.value = "No title appendix yet" }
        WaitForAsyncUtils.waitForFxEvents()

        assertTrue(hint.isVisible, "the hint of the empty list is missing")
        assertEquals("No title appendix yet", hint.text)

        interact { entries.add("A ghost story") }
        WaitForAsyncUtils.waitForFxEvents()

        assertFalse(hint.isVisible, "the hint stands beside an entry")
    }

    /**
     * Use case: nobody says what an empty list means, so no hint is shown at all instead of an empty
     * line taking up room.
     */
    @Test
    fun emptyListWithoutWordsShowsNoHint() {
        show()

        assertFalse(hint.isVisible, "a hint without words is shown")
        assertFalse(hint.isManaged, "a hint without words takes up room")
    }

    /**
     * Use case: a hint for the empty entry is given, so every row shows it while nothing is written.
     */
    @Test
    fun promptTextReachesEveryRow() {
        show("", "")
        interact { list.promptText.value = "Further title line" }
        WaitForAsyncUtils.waitForFxEvents()

        assertEquals("Further title line", inputOfRow(0).promptText)
        assertEquals("Further title line", inputOfRow(1).promptText)
    }

    /**
     * Use case: nobody says what the list holds, so the plus explains itself in general words and
     * carries its icon.
     */
    @Test
    fun plusExplainsItselfInGeneralWords() {
        assertEquals("Add entry", addButton.tooltip.text)
        assertNotNull(addButton.graphic as ImageView)
    }

    /**
     * Use case: whoever shows the list says what adding and removing mean here, so the plus and the
     * bin of a row explain themselves with those words.
     */
    @Test
    fun buttonsExplainThemselvesWithTheGivenWords() {
        show("A ghost story")
        interact {
            list.addTooltip.value = "Add title line"
            list.deleteTooltip.value = "Remove title line"
        }
        WaitForAsyncUtils.waitForFxEvents()

        assertEquals("Add title line", addButton.tooltip.text)
        assertEquals("Remove title line", removeButtonOfRow(0).tooltip.text)
    }

    /**
     * Use case: the user presses the plus, so the list reports the request with the position the new
     * entry would take, without adding an entry itself.
     */
    @Test
    fun plusReportsTheAdditionWithoutCarryingItOut() {
        val entries = show("A ghost story")
        val positions = ArrayList<Int>()
        list.setOnAddEntry { event -> positions += event.index }

        clickOn(addButton)
        WaitForAsyncUtils.waitForFxEvents()

        assertEquals(listOf(1), positions)
        assertEquals(listOf("A ghost story"), entries)
        assertEquals(1, rows.size)
    }

    /**
     * Use case: the user presses the bin of a row, so the list reports the request with the position
     * of that row, without taking the entry out itself.
     */
    @Test
    fun binReportsTheRemovalWithoutCarryingItOut() {
        val entries = show("A ghost story", "Book one")
        val positions = ArrayList<Int>()
        list.setOnDeleteEntry { event -> positions += event.index }

        clickOn(removeButtonOfRow(1))
        WaitForAsyncUtils.waitForFxEvents()

        assertEquals(listOf(1), positions)
        assertEquals(listOf("A ghost story", "Book one"), entries)
        assertEquals(2, rows.size)
    }

    /**
     * Use case: the user asks the AI for the text of an empty row, so the list reports the request
     * with the position of that row and does not pass it off as one of its other requests.
     *
     * The row is empty on purpose: a row carrying a text asks whether it may be overwritten, which
     * is the business of the entry and is proven with it.
     */
    @Test
    fun wandReportsTheRequestForAnAiText() {
        show("")
        val positions = ArrayList<Int>()
        var otherRequests = 0
        list.setOnCreateAiText { event -> positions += event.index }
        list.setOnAddEntry { otherRequests++ }
        list.setOnDeleteEntry { otherRequests++ }

        clickOn(createButtonOfRow(0))
        WaitForAsyncUtils.waitForFxEvents()

        assertEquals(listOf(0), positions)
        assertEquals(0, otherRequests, "the wand passed as another request")
    }

    /**
     * Hands the given entries to the list and waits until its rows stand.
     *
     * @param entries the texts the list shows
     * @return the list handed in, so a test reads the changes of the user there
     */
    private fun show(vararg entries: String): ObservableList<String> {
        val handedIn = FXCollections.observableArrayList(*entries)
        interact { list.entries.set(handedIn) }
        WaitForAsyncUtils.waitForFxEvents()

        return handedIn
    }

    /**
     * Reads the entry at the given position.
     *
     * @param index position of the row
     * @return the entry of that row
     */
    private fun itemOfRow(index: Int): AiTextFieldListItem = rows[index] as AiTextFieldListItem

    /**
     * Reads the input line of the entry at the given position.
     *
     * @param index position of the row
     * @return the input line of that row
     */
    private fun inputOfRow(index: Int): TextField = itemOfRow(index).lookup(".text-field") as TextField

    /**
     * Reads the button asking the AI for the text of the entry at the given position.
     *
     * @param index position of the row
     * @return the wand of that row
     */
    private fun createButtonOfRow(index: Int): Button = itemOfRow(index).lookup(".ai-create") as Button

    /**
     * Reads the button removing the entry at the given position.
     *
     * @param index position of the row
     * @return the bin of that row
     */
    private fun removeButtonOfRow(index: Int): Button = itemOfRow(index).lookup(".ai-delete") as Button
}

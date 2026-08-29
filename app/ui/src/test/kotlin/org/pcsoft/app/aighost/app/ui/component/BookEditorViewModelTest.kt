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

package org.pcsoft.app.aighost.app.ui.component

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.pcsoft.app.aighost.fx.model.project.ProjectProperty
import org.pcsoft.app.aighost.fx.model.project.book.BookProperty
import org.pcsoft.app.aighost.model.project.Project
import org.pcsoft.app.aighost.model.project.book.Book
import org.pcsoft.app.aighost.model.project.common.AIPrompt

/**
 * Developer tests for [BookEditorViewModel].
 */
class BookEditorViewModelTest {

    private val viewModel = BookEditorViewModel()

    /**
     * Builds a manuscript property of its own, so a test may exchange the manuscript the view model
     * follows.
     *
     * @param book the manuscript the property carries
     * @return the property of the manuscript
     */
    private fun bookPropertyOf(book: Book): BookProperty = ProjectProperty(Project(book = book)).bookProperty

    /**
     * Use case: a manuscript is handed to the view model, so every field shows what that manuscript
     * carries.
     */
    @Test
    fun boundBookIsShown() {
        val book = Book(
            title = "The Silent House",
            titleAppendix = listOf("A ghost story"),
            prompts = AIPrompt(contentPrompt = "A house nobody lives in", stylePrompt = "Dark and quiet")
        )

        viewModel.bind(bookPropertyOf(book))

        assertEquals("The Silent House", viewModel.title.value)
        assertEquals(listOf("A ghost story"), viewModel.titleAppendix)
        assertEquals("A house nobody lives in", viewModel.contentPrompt.value)
        assertEquals("Dark and quiet", viewModel.stylePrompt.value)
    }

    /**
     * Use case: the user writes into the fields, so the manuscript behind them carries the written
     * texts.
     */
    @Test
    fun writtenTextReachesTheBook() {
        val book = Book()
        viewModel.bind(bookPropertyOf(book))

        viewModel.title.value = "The Silent House"
        viewModel.contentPrompt.value = "A house nobody lives in"
        viewModel.stylePrompt.value = "Dark and quiet"

        assertEquals("The Silent House", book.title)
        assertEquals("A house nobody lives in", book.prompts.contentPrompt)
        assertEquals("Dark and quiet", book.prompts.stylePrompt)
    }

    /**
     * Use case: a title line is added and filled in, so the manuscript carries it in the position it
     * is shown at.
     */
    @Test
    fun addedTitleLineReachesTheBook() {
        val book = Book()
        viewModel.bind(bookPropertyOf(book))

        viewModel.addTitleAppendix()
        viewModel.setTitleAppendix(0, "A ghost story")

        assertEquals(listOf("A ghost story"), viewModel.titleAppendix)
        assertEquals(listOf("A ghost story"), book.titleAppendix)
    }

    /**
     * Use case: a position beyond the title lines is written to, so nothing happens instead of an
     * error.
     */
    @Test
    fun titleLineBeyondTheListIsDropped() {
        val book = Book(titleAppendix = listOf("A ghost story"))
        viewModel.bind(bookPropertyOf(book))

        viewModel.setTitleAppendix(5, "Book one")
        viewModel.setTitleAppendix(-1, "Book one")

        assertEquals(listOf("A ghost story"), book.titleAppendix)
    }

    /**
     * Use case: the user agrees to lose a title line, so it is taken out of the manuscript.
     */
    @Test
    fun titleLineIsRemovedAfterAgreement() {
        val book = Book(titleAppendix = listOf("A ghost story", "Book one"))
        viewModel.bind(bookPropertyOf(book))

        var asked: String? = null
        viewModel.confirmRemove = { line -> asked = line; true }
        viewModel.removeTitleAppendix(0)

        assertEquals("A ghost story", asked)
        assertEquals(listOf("Book one"), viewModel.titleAppendix)
        assertEquals(listOf("Book one"), book.titleAppendix)
    }

    /**
     * Use case: the user refuses to lose a title line, so it stays where it is.
     */
    @Test
    fun titleLineIsKeptAfterRefusal() {
        val book = Book(titleAppendix = listOf("A ghost story", "Book one"))
        viewModel.bind(bookPropertyOf(book))

        viewModel.confirmRemove = { false }
        viewModel.removeTitleAppendix(0)

        assertEquals(listOf("A ghost story", "Book one"), book.titleAppendix)
    }

    /**
     * Use case: a position beyond the title lines is removed, so nothing is asked and nothing is
     * lost.
     */
    @Test
    fun removingBeyondTheListIsHarmless() {
        val book = Book(titleAppendix = listOf("A ghost story"))
        viewModel.bind(bookPropertyOf(book))

        var asked = false
        viewModel.confirmRemove = { asked = true; true }
        viewModel.removeTitleAppendix(3)

        assertFalse(asked, "a line that is not there was asked about")
        assertEquals(listOf("A ghost story"), book.titleAppendix)
    }

    /**
     * Use case: the manuscript is changed past the view model - a project that was loaded again for
     * instance - so the fields show the values of that manuscript.
     */
    @Test
    fun changedBookReachesTheFields() {
        val book = Book()
        val property = bookPropertyOf(book)
        viewModel.bind(property)

        property.title = "The Silent House"
        property.contentPrompt = "A house nobody lives in"

        assertEquals("The Silent House", viewModel.title.value)
        assertEquals("A house nobody lives in", viewModel.contentPrompt.value)
    }

    /**
     * Use case: another manuscript takes the place of the current one, so the fields show the new one
     * and the one left behind is no longer written to.
     */
    @Test
    fun exchangedBookIsFollowed() {
        val first = Book(title = "The Silent House", titleAppendix = listOf("A ghost story"))
        val second = Book(title = "The Open Door", titleAppendix = listOf("Book one", "Book two"))

        viewModel.bind(bookPropertyOf(first))
        viewModel.bind(bookPropertyOf(second))

        assertEquals("The Open Door", viewModel.title.value)
        assertEquals(listOf("Book one", "Book two"), viewModel.titleAppendix)

        viewModel.title.value = "The Locked Door"
        viewModel.setTitleAppendix(0, "Book three")

        assertEquals("The Locked Door", second.title)
        assertEquals(listOf("Book three", "Book two"), second.titleAppendix)
        assertEquals("The Silent House", first.title)
        assertEquals(listOf("A ghost story"), first.titleAppendix)
    }

    /**
     * Use case: the project is closed, so the fields stand empty and the manuscript left behind keeps
     * what was written into it.
     */
    @Test
    fun withoutBookTheFieldsAreEmpty() {
        val book = Book(
            title = "The Silent House",
            titleAppendix = listOf("A ghost story"),
            prompts = AIPrompt(contentPrompt = "A house nobody lives in", stylePrompt = "Dark and quiet")
        )
        viewModel.bind(bookPropertyOf(book))

        viewModel.bind(null)

        assertEquals("", viewModel.title.value)
        assertEquals("", viewModel.contentPrompt.value)
        assertEquals("", viewModel.stylePrompt.value)
        assertTrue(viewModel.titleAppendix.isEmpty(), "the title lines were kept")
        assertEquals("The Silent House", book.title)
        assertEquals(listOf("A ghost story"), book.titleAppendix)
    }

    /**
     * Use case: the component is used without anybody answering the question, so a title line is
     * removed instead of staying forever.
     */
    @Test
    fun removingWithoutQuestionIsCarriedOut() {
        val book = Book(titleAppendix = listOf("A ghost story"))
        viewModel.bind(bookPropertyOf(book))

        viewModel.removeTitleAppendix(0)

        assertTrue(book.titleAppendix.isEmpty(), "the line was kept although nobody objected")
    }
}

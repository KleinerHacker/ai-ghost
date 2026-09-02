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

import javafx.beans.property.SimpleObjectProperty
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.pcsoft.app.aighost.fx.model.project.ProjectProperty
import org.pcsoft.app.aighost.model.project.Project
import org.pcsoft.app.aighost.model.project.book.Blurb
import org.pcsoft.app.aighost.model.project.book.Book
import org.pcsoft.app.aighost.model.project.book.Chapter
import org.pcsoft.app.aighost.model.project.book.Copyright
import org.pcsoft.app.aighost.model.project.book.Epilog
import org.pcsoft.app.aighost.model.project.book.Prolog
import org.pcsoft.app.aighost.model.project.common.AIPrompt
import org.pcsoft.app.aighost.model.project.meta.Meta

/**
 * Developer tests for [InspectorViewModel].
 */
class InspectorViewModelTest {

    private val viewModel = InspectorViewModel()

    /**
     * Builds a project property of its own, so a test may exchange the project the view model
     * follows.
     *
     * @param book the manuscript the project carries
     * @param author the author written into the meta data of the project
     * @return the property of the project
     */
    private fun projectPropertyOf(book: Book, author: String = "Jane Doe"): ProjectProperty =
        ProjectProperty(Project(meta = Meta(author = author), book = book))

    /**
     * Use case: a project is handed to the view model, so the "Book" section shows what it carries and
     * is no longer marked as empty.
     */
    @Test
    fun boundProjectFillsTheBookSection() {
        val book = Book(
            title = "The Silent House",
            titleAppendix = listOf("A ghost story"),
            prompts = AIPrompt(contentPrompt = "A house nobody lives in", stylePrompt = "Dark and quiet"),
            copyright = Copyright(copyright = "(c) 2026 Jane Doe")
        )

        viewModel.bindProject(projectPropertyOf(book))

        assertTrue(viewModel.bookAvailable.value, "the book section is still marked as empty")
        assertEquals("The Silent House", viewModel.title.value)
        assertEquals(listOf("A ghost story"), viewModel.titleAppendix)
        assertEquals("A house nobody lives in", viewModel.contentPrompt.value)
        assertEquals("Dark and quiet", viewModel.stylePrompt.value)
        assertEquals("Jane Doe", viewModel.author.value)
        assertEquals("(c) 2026 Jane Doe", viewModel.copyright.value)
    }

    /**
     * Use case: the user writes into the fields of the "Book" section, so the project bound to it
     * carries the written texts.
     */
    @Test
    fun writtenBookTextReachesTheProject() {
        val book = Book()
        val project = projectPropertyOf(book)
        viewModel.bindProject(project)

        viewModel.title.value = "The Silent House"
        viewModel.contentPrompt.value = "A house nobody lives in"
        viewModel.stylePrompt.value = "Dark and quiet"
        viewModel.author.value = "John Smith"
        viewModel.copyright.value = "(c) 2026 John Smith"

        assertEquals("The Silent House", book.title)
        assertEquals("A house nobody lives in", book.prompts.contentPrompt)
        assertEquals("Dark and quiet", book.prompts.stylePrompt)
        assertEquals("John Smith", project.metaProperty.author)
        assertEquals("(c) 2026 John Smith", book.copyright.copyright)
    }

    /**
     * Use case: no project is open, so the "Book" section is marked as empty and every field stands
     * empty.
     */
    @Test
    fun withoutProjectTheBookSectionIsEmpty() {
        assertFalse(viewModel.bookAvailable.value, "the book section is not marked as empty")
        assertEquals("", viewModel.title.value)
        assertEquals("", viewModel.author.value)
        assertEquals("", viewModel.copyright.value)
        assertTrue(viewModel.titleAppendix.isEmpty())
    }

    /**
     * Use case: the project is closed while the inspector stands, so the "Book" section is emptied and
     * the project left behind keeps what was written into it.
     */
    @Test
    fun closedProjectEmptiesTheBookSection() {
        val book = Book(title = "The Silent House")
        viewModel.bindProject(projectPropertyOf(book))

        viewModel.bindProject(null)

        assertFalse(viewModel.bookAvailable.value)
        assertEquals("", viewModel.title.value)
        assertEquals("The Silent House", book.title)
    }

    /**
     * Use case: a chapter is picked in the project tree, so the "Chapter" section shows its name and
     * both its prompts.
     */
    @Test
    fun pickedChapterFillsTheChapterSection() {
        val chapter = Chapter(
            name = "Chapter one",
            title = "The arrival",
            prompts = AIPrompt(contentPrompt = "Tell how it began", stylePrompt = "Slow and quiet")
        )
        val selection = SimpleObjectProperty<ProjectListItem?>(ProjectListItem.ChapterItem(chapter))
        viewModel.bindSelection(selection)

        assertEquals(InspectorViewModel.ChapterSelection.CHAPTER, viewModel.chapterSelection.value)
        assertEquals("Chapter one", viewModel.chapterName.value)
        assertEquals("Tell how it began", viewModel.chapterContentPrompt.value)
        assertEquals("Slow and quiet", viewModel.chapterStylePrompt.value)
    }

    /**
     * Use case: the user renames the picked chapter through the "Chapter" section, so the text lands on
     * the very chapter object the project tree still holds.
     */
    @Test
    fun writtenChapterNameReachesTheChapter() {
        val chapter = Chapter(name = "Chapter one", title = "The arrival")
        val selection = SimpleObjectProperty<ProjectListItem?>(ProjectListItem.ChapterItem(chapter))
        viewModel.bindSelection(selection)

        viewModel.chapterName.value = "Chapter two"
        viewModel.chapterContentPrompt.value = "Tell how it began"
        viewModel.chapterStylePrompt.value = "Slow and quiet"

        assertEquals("Chapter two", chapter.name)
        assertEquals("Tell how it began", chapter.prompts.contentPrompt)
        assertEquals("Slow and quiet", chapter.prompts.stylePrompt)
    }

    /**
     * Use case: the blurb is picked in the project tree and already created, so the "Chapter" section
     * shows its prompt instead of its empty state.
     */
    @Test
    fun pickedBlurbFillsTheChapterSectionWithItsPrompt() {
        val blurb = Blurb(prompt = "A gripping tale")
        val book = Book(blurb = blurb)
        val project = projectPropertyOf(book)
        viewModel.bindProject(project)

        val selection = SimpleObjectProperty<ProjectListItem?>(ProjectListItem.BlurbItem(blurb))
        viewModel.bindSelection(selection)

        assertEquals(InspectorViewModel.ChapterSelection.BLURB, viewModel.chapterSelection.value)
        assertEquals("A gripping tale", viewModel.blurbPrompt.value)

        viewModel.blurbPrompt.value = "A haunting tale"
        assertEquals("A haunting tale", book.blurb.prompt)
    }

    /**
     * Use case: the blurb is picked but not created yet, so the "Chapter" section shows its empty
     * state instead of a prompt nobody wrote yet.
     */
    @Test
    fun uncreatedBlurbLeavesTheChapterSectionEmpty() {
        viewModel.bindProject(projectPropertyOf(Book()))

        val selection = SimpleObjectProperty<ProjectListItem?>(ProjectListItem.BlurbItem(null))
        viewModel.bindSelection(selection)

        assertEquals(InspectorViewModel.ChapterSelection.NONE, viewModel.chapterSelection.value)
    }

    /**
     * Use case: a node that stands for neither a chapter nor the blurb is picked - the root, the
     * chapters category, a prolog or an epilog without content - so the "Chapter" section shows its
     * empty state.
     */
    @Test
    fun structuralSelectionLeavesTheChapterSectionEmpty() {
        val selection = SimpleObjectProperty<ProjectListItem?>(ProjectListItem.Root)
        viewModel.bindSelection(selection)
        assertEquals(InspectorViewModel.ChapterSelection.NONE, viewModel.chapterSelection.value)

        selection.value = ProjectListItem.Chapters
        assertEquals(InspectorViewModel.ChapterSelection.NONE, viewModel.chapterSelection.value)

        selection.value = ProjectListItem.PrologItem(null)
        assertEquals(InspectorViewModel.ChapterSelection.NONE, viewModel.chapterSelection.value)

        selection.value = ProjectListItem.EpilogItem(Epilog())
        assertEquals(InspectorViewModel.ChapterSelection.NONE, viewModel.chapterSelection.value)
        assertEquals("", viewModel.chapterName.value)
    }

    /**
     * Use case: the user picks another chapter after having picked one already, so the "Chapter"
     * section follows the new one and writing into it no longer reaches the chapter left behind.
     */
    @Test
    fun switchesTheChapterSectionToTheNewlyPickedChapter() {
        val first = Chapter(name = "Chapter one", title = "The arrival")
        val second = Chapter(name = "Chapter two", title = "The departure")
        val selection = SimpleObjectProperty<ProjectListItem?>(ProjectListItem.ChapterItem(first))
        viewModel.bindSelection(selection)

        selection.value = ProjectListItem.ChapterItem(second)

        assertEquals("Chapter two", viewModel.chapterName.value)

        viewModel.chapterName.value = "Chapter three"
        assertEquals("Chapter three", second.name)
        assertEquals("Chapter one", first.name)
    }

    /**
     * Use case: a prolog exists but is not what the "Chapter" section edits, matching the plan's rule
     * that only a chapter or the blurb are shown there - so the section stays in its empty state even
     * though the prolog itself carries content.
     */
    @Test
    fun prologSelectionNeverFillsTheChapterSection() {
        val selection = SimpleObjectProperty<ProjectListItem?>(ProjectListItem.PrologItem(Prolog()))
        viewModel.bindSelection(selection)

        assertEquals(InspectorViewModel.ChapterSelection.NONE, viewModel.chapterSelection.value)
    }

    /**
     * Use case: no project is open, so the "Design" section is marked as empty.
     *
     * The four style editors of the section are themselves real Java FX components and therefore
     * belong to [InspectorTest], which exercises the assembled [Inspector] on a running Java FX
     * toolkit; this plain unit test only proves the availability flag this view model owns on its
     * own, without ever setting the lateinit editor fields [InspectorView] would otherwise wire up.
     */
    @Test
    fun withoutProjectTheDesignSectionIsEmpty() {
        assertFalse(viewModel.designAvailable.value, "the design section is not marked as empty")
    }

    /**
     * Use case: a project is bound and later closed again, so the "Design" section's availability flag
     * follows - and, crucially, neither call crashes even though no style editor was ever wired up, as
     * is the case for this view model built without [InspectorView] around it.
     */
    @Test
    fun designAvailabilityFollowsTheBoundProject() {
        viewModel.bindProject(projectPropertyOf(Book()))
        assertTrue(viewModel.designAvailable.value, "the design section is still marked as empty")

        viewModel.bindProject(null)
        assertFalse(viewModel.designAvailable.value, "the design section is not marked as empty")
    }
}

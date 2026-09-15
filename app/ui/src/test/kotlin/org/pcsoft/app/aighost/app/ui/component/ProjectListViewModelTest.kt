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

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.pcsoft.app.aighost.fx.model.project.ProjectProperty
import org.pcsoft.app.aighost.model.common.Alignment
import org.pcsoft.app.aighost.model.common.FontData
import org.pcsoft.app.aighost.model.common.StyleData
import org.pcsoft.app.aighost.model.project.Project
import org.pcsoft.app.aighost.model.project.book.Chapter
import org.pcsoft.app.aighost.model.project.design.ChapterPageDesign
import org.pcsoft.app.aighost.model.project.design.Design

/**
 * Developer tests for [ProjectListViewModel]'s chapter management: [ProjectListViewModel.addChapter],
 * [ProjectListViewModel.removeChapter] and [ProjectListViewModel.renameChapter] (IP-38).
 *
 * Whether removing a chapter needs the user's confirmation first is [ProjectListCell]'s concern, not
 * this view model's - every method here is proven to carry out its change unconditionally, the moment
 * it is called.
 */
class ProjectListViewModelTest {

    private lateinit var viewModel: ProjectListViewModel
    private lateinit var project: ProjectProperty

    private fun style(): StyleData =
        StyleData(font = FontData("Serif", 12, bold = false, italic = false), alignment = Alignment.LEFT)

    @BeforeEach
    fun setUp() {
        viewModel = ProjectListViewModel()
        project = ProjectProperty(
            Project(design = Design(chapterPage = ChapterPageDesign(style(), style(), style())))
        )
        viewModel.bind(project)
    }

    /**
     * Use case: the user adds a chapter to an empty manuscript, so it turns up in the chapter list
     * with a fresh id and a default name, and its anchor's page exists in the book's document, seeded
     * and empty.
     */
    @Test
    fun addsAChapterWithASeededAnchorPage() {
        viewModel.addChapter()

        val chapter = project.value.book.chapters.single()
        assertEquals(listOf(chapter), viewModel.chapters)

        val anchorId = chapter.id.toString()
        val page = project.value.book.document.pages.single { it.id == anchorId }
        assertEquals("\${$anchorId}", page.blocks.single().toString())
    }

    /**
     * Use case: the user adds a second chapter, so it is appended after the first instead of
     * replacing it, and both keep their own anchor page.
     */
    @Test
    fun appendsAFurtherChapterAfterTheExistingOnes() {
        viewModel.addChapter()
        val first = project.value.book.chapters.single()

        viewModel.addChapter()

        assertEquals(2, project.value.book.chapters.size)
        assertEquals(first, project.value.book.chapters[0])
        assertEquals(2, project.value.book.document.pages.size)
    }

    /**
     * Use case: the user removes a chapter, so it disappears from the chapter list and its page is
     * gone from the book's document as well - nothing keeps naming an anchor nobody can reach any more.
     */
    @Test
    fun removesAChapterAndItsAnchorPage() {
        viewModel.addChapter()
        val chapter = project.value.book.chapters.single()

        viewModel.removeChapter(chapter)

        assertTrue(project.value.book.chapters.isEmpty())
        assertFalse(project.value.book.document.pages.any { it.id == chapter.id.toString() })
    }

    /**
     * Use case: the user removes one of several chapters, so only that one and its page are gone; the
     * other chapters and their pages are untouched.
     */
    @Test
    fun removesOnlyTheGivenChapter() {
        viewModel.addChapter()
        val first = project.value.book.chapters.single()
        viewModel.addChapter()
        val second = project.value.book.chapters[1]

        viewModel.removeChapter(first)

        assertEquals(listOf(second), project.value.book.chapters)
        assertTrue(project.value.book.document.pages.any { it.id == second.id.toString() })
        assertFalse(project.value.book.document.pages.any { it.id == first.id.toString() })
    }

    /**
     * Use case: the user renames a chapter, so its name changes in the chapter list while its id, and
     * therefore its anchor and its page, stay exactly as they were.
     */
    @Test
    fun renamesAChapterKeepingItsIdAndAnchorPage() {
        viewModel.addChapter()
        val chapter = project.value.book.chapters.single()
        val anchorId = chapter.id.toString()

        viewModel.renameChapter(chapter, "Chapter One")

        val renamed = project.value.book.chapters.single()
        assertEquals("Chapter One", renamed.name)
        assertEquals(chapter.id, renamed.id)
        assertTrue(project.value.book.document.pages.any { it.id == anchorId })
    }

    /**
     * Use case: no project is open, so adding, removing or renaming a chapter is simply a no-op
     * instead of failing.
     */
    @Test
    fun chapterManagementIsANoOpWithoutAProject() {
        val withoutProject = ProjectListViewModel()

        withoutProject.addChapter()
        withoutProject.removeChapter(Chapter("first"))
        withoutProject.renameChapter(Chapter("first"), "Renamed")

        assertTrue(withoutProject.chapters.isEmpty())
    }
}

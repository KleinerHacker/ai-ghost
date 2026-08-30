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

import de.saxsys.mvvmfx.MvvmFX
import javafx.scene.Scene
import javafx.scene.control.TreeItem
import javafx.scene.control.TreeView
import javafx.stage.Stage
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.pcsoft.app.aighost.app.Messages
import org.pcsoft.app.aighost.fx.model.project.ProjectProperty
import org.pcsoft.app.aighost.model.project.book.Blurb
import org.pcsoft.app.aighost.model.project.book.Book
import org.pcsoft.app.aighost.model.project.book.Chapter
import org.pcsoft.app.aighost.model.project.book.Epilog
import org.pcsoft.app.aighost.model.project.Project
import org.pcsoft.app.aighost.model.project.design.AuthorDesign
import org.pcsoft.app.aighost.model.project.design.ChapterDesign
import org.pcsoft.app.aighost.model.project.design.CopyrightDesign
import org.pcsoft.app.aighost.model.project.design.Design
import org.pcsoft.app.aighost.model.project.design.TextDesign
import org.pcsoft.app.aighost.model.project.design.TitleDesign
import org.pcsoft.app.aighost.model.project.meta.Meta
import org.pcsoft.app.aighost.model.project.book.Prolog
import org.pcsoft.app.aighost.model.common.Alignment
import org.pcsoft.app.aighost.model.common.FontData
import org.pcsoft.app.aighost.model.common.StyleData
import org.testfx.framework.junit5.ApplicationTest
import org.testfx.util.WaitForAsyncUtils
import java.util.Locale
import java.util.ResourceBundle

/**
 * Developer tests for [ProjectList].
 */
class ProjectListTest : ApplicationTest() {

    private lateinit var projectList: ProjectList

    @Suppress("UNCHECKED_CAST")
    // The tree follows the property model of the surrounding window, which is handed over once and
    // carries another project whenever the user opens or closes one.
    private val projectModel = ProjectProperty(Project())

    @Suppress("UNCHECKED_CAST")
    private val tree: TreeView<ProjectListItem>
        get() = projectList.lookup(".tree-view") as TreeView<ProjectListItem>

    override fun start(stage: Stage) {
        // No fallback, so the English base bundle is used no matter which locale the build runs under.
        MvvmFX.setGlobalResourceBundle(
            ResourceBundle.getBundle(
                Messages.BUNDLE_NAME,
                Locale.ROOT,
                ResourceBundle.Control.getNoFallbackControl(ResourceBundle.Control.FORMAT_PROPERTIES)
            )
        )

        projectList = ProjectList()
        projectList.bindProject(projectModel)
        stage.scene = Scene(projectList, 300.0, 400.0)
        stage.show()
    }

    private fun style(): StyleData =
        StyleData(font = FontData("Serif", 12, bold = false, italic = false), alignment = Alignment.LEFT)

    private fun project(book: Book): Project = Project(
        meta = Meta(
            name = "My Novel",
            author = "Jane Doe",
            copyright = "(c) 2026 Jane Doe"
        ),
        design = Design(
            authorDesign = AuthorDesign(style()),
            copyrightDesign = CopyrightDesign(style(), show = false),
            titleDesign = TitleDesign(style()),
            chapterDesign = ChapterDesign(style(), style()),
            textDesign = TextDesign(style()),
            startWithEmptyPage = false,
            endWithEmptyPage = false
        ),
        book = book
    )

    private fun setProject(project: Project) {
        interact { projectModel.value = project }
        // The cells are rebuilt on the next pulse, so a test looking at them waits for it.
        WaitForAsyncUtils.waitForFxEvents()
    }

    private fun chaptersItem(): TreeItem<ProjectListItem> =
        tree.root.children.first { it.value is ProjectListItem.Chapters }

    /**
     * Use case: the user looks at a project that has not been written yet, so the tree shows the
     * project with its four branches and hands on the empty parts every book carries.
     */
    @Test
    fun showsTheFixedBranchesBelowTheProject() {
        assertEquals(ProjectListItem.Root, tree.root.value)
        assertEquals(
            listOf(
                ProjectListItem.PrologItem(Prolog()),
                ProjectListItem.Chapters,
                ProjectListItem.EpilogItem(Epilog()),
                ProjectListItem.BlurbItem(Blurb())
            ),
            tree.root.children.map { it.value }
        )
    }

    /**
     * Use case: the branches are meant to be read at a glance, so the project and the chapters
     * branch are expanded when the component comes up.
     */
    @Test
    fun expandsProjectAndChaptersBranch() {
        assertTrue(tree.root.isExpanded, "the project node must be expanded")
        assertTrue(chaptersItem().isExpanded, "the chapters branch must be expanded")
    }

    /**
     * Use case: a project is bound to the component, so every chapter of the book turns up under the
     * chapters branch in the order the user arranged it.
     */
    @Test
    fun listsEveryChapterOfTheBoundProject() {
        setProject(
            project(
                Book(title = "My Novel",
                    chapters = listOf(
                        Chapter("first", "The First Part"),
                        Chapter("second", "The Second Part")
                    )
                )
            )
        )

        assertEquals(
            listOf(
                ProjectListItem.ChapterItem(Chapter("first", "The First Part")),
                ProjectListItem.ChapterItem(Chapter("second", "The Second Part"))
            ),
            chaptersItem().children.map { it.value }
        )
    }

    /**
     * Use case: a chapter is shown by the name the user gave it, because its printed heading may
     * still be empty while the chapter is only outlined.
     */
    @Test
    fun labelsAChapterByItsNameNotByItsTitle() {
        setProject(project(Book(title = "My Novel", chapters = listOf(Chapter("draft-01", "The First Part")))))

        val chapterItem = chaptersItem().children.single()
        val cell = tree.lookupAll(".tree-cell")
            .filterIsInstance<ProjectListCell>()
            .first { it.treeItem === chapterItem }

        assertEquals("draft-01", cell.text)
        assertNotNull(cell.graphic, "a chapter node must show the chapter icon")
    }

    /**
     * Use case: the project the user works on carries a prolog, an epilog and a blurb, so the fixed
     * branches hand those parts on instead of staying empty.
     */
    @Test
    fun handsOnPrologEpilogAndBlurbOfTheBoundProject() {
        val prolog = Prolog("Before It All")
        val epilog = Epilog("After It All")
        val blurb = Blurb(paragraph = listOf("A gripping tale."))

        setProject(project(Book(title = "My Novel", prolog = prolog, epilog = epilog, blurb = blurb)))

        assertEquals(
            listOf(
                ProjectListItem.PrologItem(prolog),
                ProjectListItem.Chapters,
                ProjectListItem.EpilogItem(epilog),
                ProjectListItem.BlurbItem(blurb)
            ),
            tree.root.children.map { it.value }
        )
    }

    /**
     * Use case: the user picks a chapter, so the component reports the change and names the chapter
     * that was picked, which is what the surrounding window opens.
     */
    @Test
    fun reportsTheSelectedChapter() {
        setProject(project(Book(title = "My Novel", chapters = listOf(Chapter("first", "The First Part")))))

        val reported = mutableListOf<ProjectListItem?>()
        projectList.selectedItem.addListener { _, _, new -> reported += new }

        interact { tree.selectionModel.select(chaptersItem().children.single()) }

        assertEquals(listOf(ProjectListItem.ChapterItem(Chapter("first", "The First Part"))), reported)
        assertEquals(
            ProjectListItem.ChapterItem(Chapter("first", "The First Part")),
            projectList.selectedItem.value
        )
    }

    /**
     * Use case: the user picks the blurb branch, so the component reports that branch instead of a
     * chapter, which lets the window open the blurb editor.
     */
    @Test
    fun reportsASelectedFixedBranch() {
        interact { tree.selectionModel.select(tree.root.children.last()) }

        assertEquals(ProjectListItem.BlurbItem(Blurb()), projectList.selectedItem.value)
    }

    /**
     * Use case: another project is opened while a chapter of the previous one was selected, so the
     * selection is dropped instead of reporting a chapter that is no longer part of the tree.
     */
    @Test
    fun clearsTheSelectionWhenAnotherProjectIsBound() {
        setProject(project(Book(title = "My Novel", chapters = listOf(Chapter("first", "The First Part")))))
        interact { tree.selectionModel.select(chaptersItem().children.single()) }

        setProject(project(Book(title = "Another Novel", chapters = listOf(Chapter("other", "Another Part")))))

        assertNull(projectList.selectedItem.value)
        assertEquals(
            listOf(ProjectListItem.ChapterItem(Chapter("other", "Another Part"))),
            chaptersItem().children.map { it.value }
        )
    }

    /**
     * Use case: the open project is closed and a fresh one takes its place, so the chapters
     * disappear from the tree while the fixed branches stay in place.
     */
    @Test
    fun emptiesTheChaptersBranchForAFreshProject() {
        setProject(project(Book(title = "My Novel", chapters = listOf(Chapter("first", "The First Part")))))

        setProject(Project())

        assertEquals(emptyList<ProjectListItem>(), chaptersItem().children.map { it.value })
        assertEquals(4, tree.root.children.size)
    }
}

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
import javafx.scene.control.CheckBox
import javafx.scene.control.TreeItem
import javafx.scene.control.TreeView
import javafx.stage.Stage
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
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
import org.pcsoft.app.aighost.model.project.design.BlurbPageDesign
import org.pcsoft.app.aighost.model.project.design.ChapterPageDesign
import org.pcsoft.app.aighost.model.project.design.CopyrightPageDesign
import org.pcsoft.app.aighost.model.project.design.Design
import org.pcsoft.app.aighost.model.project.design.EpilogPageDesign
import org.pcsoft.app.aighost.model.project.design.PrologPageDesign
import org.pcsoft.app.aighost.model.project.design.TitlePageDesign
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
            author = "Jane Doe"
        ),
        design = Design(
            titlePage = TitlePageDesign(style(), style(), showAuthor = true, authorStyle = style()),
            copyrightPage = CopyrightPageDesign(style(), style(), showAuthor = false, authorStyle = style()),
            prologPage = PrologPageDesign(style(), style(), style()),
            blurbPage = BlurbPageDesign(style()),
            chapterPage = ChapterPageDesign(style(), style(), style()),
            epilogPage = EpilogPageDesign(style(), style(), style()),
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
     * project with its fixed branches - front matter first - and hands on the empty parts every book
     * carries.
     */
    @Test
    fun showsTheFixedBranchesBelowTheProject() {
        assertEquals(ProjectListItem.Root, tree.root.value)
        assertEquals(
            listOf(
                ProjectListItem.TitlePageItem,
                ProjectListItem.CopyrightPageItem,
                ProjectListItem.PrologItem(Prolog()),
                ProjectListItem.Chapters,
                ProjectListItem.EpilogItem(Epilog()),
                ProjectListItem.BlurbItem(Blurb())
            ),
            tree.root.children.map { it.value }
        )
    }

    /**
     * Use case: the front matter is written on the paper as well, so the title page and the copyright
     * page each get a node of their own, labelled from the message bundle and placed ahead of the
     * prolog.
     */
    @Test
    fun showsTheFrontMatterBranchesLabelledAheadOfTheProlog() {
        val titleItem = tree.root.children[0]
        val copyrightItem = tree.root.children[1]

        assertEquals(ProjectListItem.TitlePageItem, titleItem.value)
        assertEquals(ProjectListItem.CopyrightPageItem, copyrightItem.value)

        val cells = tree.lookupAll(".tree-cell").filterIsInstance<ProjectListCell>()
        assertEquals("Title Page", cells.first { it.treeItem === titleItem }.text)
        assertEquals("Copyright Page", cells.first { it.treeItem === copyrightItem }.text)
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
        val first = Chapter("first")
        val second = Chapter("second")
        setProject(project(Book(chapters = listOf(first, second))))

        assertEquals(
            listOf(
                ProjectListItem.ChapterItem(first),
                ProjectListItem.ChapterItem(second)
            ),
            chaptersItem().children.map { it.value }
        )
    }

    /**
     * Use case: a chapter is shown by the name the user gave it, since no printed heading lives on the
     * model anymore.
     */
    @Test
    fun labelsAChapterByItsName() {
        setProject(project(Book(chapters = listOf(Chapter("draft-01")))))

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
        val prolog = Prolog(included = true)
        val epilog = Epilog(included = true)
        val blurb = Blurb(paragraph = listOf("A gripping tale."))

        setProject(project(Book(prolog = prolog, epilog = epilog, blurb = blurb)))

        assertEquals(
            listOf(
                ProjectListItem.TitlePageItem,
                ProjectListItem.CopyrightPageItem,
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
        val chapter = Chapter("first")
        setProject(project(Book(chapters = listOf(chapter))))

        val reported = mutableListOf<ProjectListItem?>()
        projectList.selectedItem.addListener { _, _, new -> reported += new }

        interact { tree.selectionModel.select(chaptersItem().children.single()) }

        assertEquals(listOf(ProjectListItem.ChapterItem(chapter)), reported)
        assertEquals(
            ProjectListItem.ChapterItem(chapter),
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
     * Use case: the user picks the title page branch, so the component reports it, which lets the
     * surrounding window open the title page on the paper.
     */
    @Test
    fun reportsTheSelectedTitlePageBranch() {
        interact { tree.selectionModel.select(tree.root.children.first()) }

        assertEquals(ProjectListItem.TitlePageItem, projectList.selectedItem.value)
    }

    /**
     * Use case: another project is opened while a chapter of the previous one was selected, so the
     * selection is dropped instead of reporting a chapter that is no longer part of the tree.
     */
    @Test
    fun clearsTheSelectionWhenAnotherProjectIsBound() {
        setProject(project(Book(chapters = listOf(Chapter("first")))))
        interact { tree.selectionModel.select(chaptersItem().children.single()) }

        val other = Chapter("other")
        setProject(project(Book(chapters = listOf(other))))

        assertNull(projectList.selectedItem.value)
        assertEquals(
            listOf(ProjectListItem.ChapterItem(other)),
            chaptersItem().children.map { it.value }
        )
    }

    /**
     * Use case: the open project is closed and a fresh one takes its place, so the chapters
     * disappear from the tree while the fixed branches stay in place.
     */
    @Test
    fun emptiesTheChaptersBranchForAFreshProject() {
        setProject(project(Book(chapters = listOf(Chapter("first")))))

        setProject(Project())

        assertEquals(emptyList<ProjectListItem>(), chaptersItem().children.map { it.value })
        assertEquals(6, tree.root.children.size)
    }

    private fun cellFor(item: TreeItem<ProjectListItem>): ProjectListCell =
        tree.lookupAll(".tree-cell").filterIsInstance<ProjectListCell>().first { it.treeItem === item }

    private fun checkBoxOf(item: TreeItem<ProjectListItem>): CheckBox =
        cellFor(item).graphic!!.lookup(".check-box") as CheckBox

    /**
     * Use case: the project carries an included prolog, so the checkbox next to its icon shows
     * checked instead of the neutral, unchecked default.
     */
    @Test
    fun showsACheckedCheckboxForAnIncludedProlog() {
        setProject(project(Book(prolog = Prolog(included = true))))

        val prologItem = tree.root.children[2]
        assertTrue(checkBoxOf(prologItem).isSelected)
    }

    /**
     * Use case: the project carries an epilog that was never switched on, so its checkbox shows
     * unchecked.
     */
    @Test
    fun showsAnUncheckedCheckboxForAnExcludedEpilog() {
        setProject(project(Book()))

        val epilogItem = tree.root.children[4]
        assertFalse(checkBoxOf(epilogItem).isSelected)
    }

    /**
     * Use case: the user clicks the blurb's checkbox, so the switch reaches the model right away.
     */
    @Test
    fun togglingTheCheckboxSwitchesInclusionInTheModel() {
        setProject(project(Book()))
        val blurbItem = tree.root.children.last()

        interact { checkBoxOf(blurbItem).fire() }
        WaitForAsyncUtils.waitForFxEvents()

        assertTrue(projectModel.value.book.blurb.included)
        assertTrue(checkBoxOf(blurbItem).isSelected)
    }

    /**
     * Use case: a structural node - the chapters branch - carries no switch of its own, so its graphic
     * is the plain chapter icon instead of a checkbox.
     */
    @Test
    fun showsNoCheckboxForAStructuralNode() {
        assertNull(cellFor(chaptersItem()).graphic!!.lookup(".check-box"))
    }
}

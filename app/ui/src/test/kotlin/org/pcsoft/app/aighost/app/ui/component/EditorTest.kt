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
import javafx.geometry.Orientation
import javafx.scene.Scene
import javafx.scene.control.Label
import javafx.scene.control.SplitPane
import javafx.stage.Stage
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.pcsoft.app.aighost.app.Messages
import org.pcsoft.app.aighost.model.common.Alignment
import org.pcsoft.app.aighost.model.common.FontData
import org.pcsoft.app.aighost.model.common.StyleData
import org.pcsoft.app.aighost.model.project.book.Book
import org.pcsoft.app.aighost.model.project.book.Chapter
import org.pcsoft.app.aighost.model.project.Project
import org.pcsoft.app.aighost.model.project.design.AuthorDesign
import org.pcsoft.app.aighost.model.project.design.ChapterDesign
import org.pcsoft.app.aighost.model.project.design.CopyrightDesign
import org.pcsoft.app.aighost.model.project.design.Design
import org.pcsoft.app.aighost.model.project.design.TextDesign
import org.pcsoft.app.aighost.model.project.design.TitleDesign
import org.pcsoft.app.aighost.model.project.meta.Meta
import org.testfx.framework.junit5.ApplicationTest
import org.testfx.util.WaitForAsyncUtils
import java.util.Locale
import java.util.ResourceBundle

/**
 * Developer tests for [Editor].
 */
class EditorTest : ApplicationTest() {

    private lateinit var editor: Editor

    private val splitPane: SplitPane
        get() = editor.lookup(".split-pane") as SplitPane

    private val projectList: ProjectList
        get() = splitPane.items.first() as ProjectList

    override fun start(stage: Stage) {
        // No fallback, so the English base bundle is used no matter which locale the build runs under.
        MvvmFX.setGlobalResourceBundle(
            ResourceBundle.getBundle(
                Messages.BUNDLE_NAME,
                Locale.ROOT,
                ResourceBundle.Control.getNoFallbackControl(ResourceBundle.Control.FORMAT_PROPERTIES)
            )
        )

        editor = Editor()
        stage.scene = Scene(editor, 900.0, 600.0)
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

    /**
     * Use case: the user opens the editor, so the project tree sits on the left of a horizontal split
     * and the editing area fills the rest, which is what the splitter divides.
     */
    @Test
    fun showsTheProjectTreeLeftOfTheEditingArea() {
        assertEquals(Orientation.HORIZONTAL, splitPane.orientation)
        assertEquals(2, splitPane.items.size, "the split holds the tree and the editing area")
        assertTrue(splitPane.items.first() is ProjectList, "the tree sits on the left")
    }

    /**
     * Use case: the user drags the splitter to the far left, so the project tree still keeps its
     * minimum width of 250px instead of collapsing to nothing.
     */
    @Test
    fun keepsTheProjectTreeAtLeast250PixelsWide() {
        assertEquals(250.0, projectList.minWidth, 0.0)

        interact { splitPane.setDividerPosition(0, 0.0) }
        WaitForAsyncUtils.waitForFxEvents()

        assertTrue(
            projectList.width >= 250.0,
            "the tree must not become narrower than 250px, but was ${projectList.width}"
        )
    }

    /**
     * Use case: the window is made wider, so the extra room goes to the editing area while the tree
     * keeps the width the user gave it.
     */
    @Test
    fun leavesTheProjectTreeOutOfTheParentResize() {
        assertFalse(
            SplitPane.isResizableWithParent(projectList),
            "the tree must not grow with the surrounding window"
        )
    }

    /**
     * Use case: nothing can be edited yet, so the area right of the splitter shows a placeholder
     * instead of staying empty.
     */
    @Test
    fun showsAPlaceholderInTheEditingArea() {
        val placeholder = editor.lookup(".editor-placeholder") as Label

        assertNotNull(placeholder)
        assertEquals("Select an entry in the project tree to edit it.", placeholder.text)
        assertSame(
            splitPane.items.last(),
            placeholder.parent,
            "the placeholder belongs to the area right of the splitter"
        )
    }

    /**
     * Use case: a project is bound to the editor from the outside, so the project tree on the left
     * shows exactly that project without anyone binding it separately.
     */
    @Test
    fun handsTheBoundProjectOnToTheProjectTree() {
        val project = project(Book(title = "My Novel", chapters = listOf(Chapter("first", "The First Part"))))

        interact { editor.project.value = project }
        WaitForAsyncUtils.waitForFxEvents()

        assertSame(project, projectList.project.value)
    }

    /**
     * Use case: the open project is closed, so the tree is emptied along with the editor instead of
     * keeping the project that is no longer open.
     */
    @Test
    fun handsTheClosedProjectOnToTheProjectTree() {
        interact { editor.project.value = project(Book(title = "My Novel")) }
        WaitForAsyncUtils.waitForFxEvents()

        interact { editor.project.value = null }
        WaitForAsyncUtils.waitForFxEvents()

        assertNull(projectList.project.value)
    }
}

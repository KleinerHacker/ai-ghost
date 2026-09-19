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
import javafx.beans.property.SimpleObjectProperty
import javafx.geometry.Point2D
import javafx.scene.Scene
import javafx.stage.Stage
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.pcsoft.app.aighost.app.Messages
import org.pcsoft.app.aighost.app.controller.IoController
import org.pcsoft.app.aighost.app.undo.UndoStack
import org.pcsoft.app.aighost.fx.model.project.ProjectProperty
import org.pcsoft.app.aighost.model.common.Alignment
import org.pcsoft.app.aighost.model.common.FontData
import org.pcsoft.app.aighost.model.common.StyleData
import org.pcsoft.app.aighost.model.project.Project
import org.pcsoft.app.aighost.model.project.book.Blurb
import org.pcsoft.app.aighost.model.project.book.Book
import org.pcsoft.app.aighost.model.project.book.Chapter
import org.pcsoft.app.aighost.model.project.book.Epilog
import org.pcsoft.app.aighost.model.project.book.Prolog
import org.pcsoft.app.aighost.model.project.design.BlurbPageDesign
import org.pcsoft.app.aighost.model.project.design.ChapterPageDesign
import org.pcsoft.app.aighost.model.project.design.CopyrightPageDesign
import org.pcsoft.app.aighost.model.project.design.Design
import org.pcsoft.app.aighost.model.project.design.EpilogPageDesign
import org.pcsoft.app.aighost.model.project.design.PrologPageDesign
import org.pcsoft.app.aighost.model.project.design.TitlePageDesign
import org.pcsoft.app.aighost.model.project.meta.Meta
import org.pcsoft.framework.simplay.fx.FloatingOverlay
import org.pcsoft.framework.simplay.fx.FloatingOverlayTrigger
import org.pcsoft.framework.simplay.fx.PaperSheetView
import org.testfx.framework.junit5.ApplicationTest
import org.testfx.util.WaitForAsyncUtils
import java.util.Locale
import java.util.ResourceBundle
import java.util.concurrent.TimeUnit

/**
 * Developer tests for the floating AI action bar registered on [BookPartEditorView]'s sheet (IP-18):
 * the two [FloatingOverlay]s driving it, one per trigger, show and hide themselves exactly as
 * `simplay-fx`'s `PaperSheetView` decides, without this plan wiring any logic of its own.
 */
class BookPartEditorAiActionBarTest : ApplicationTest() {

    private lateinit var editor: BookPartEditor
    private lateinit var sheet: PaperSheetView
    private lateinit var projectModel: ProjectProperty

    private var originalLastAnchorId: String? = null

    @BeforeEach
    fun rememberPreferences() {
        originalLastAnchorId = IoController.preferences.editorProperty.lastAnchorId
    }

    @AfterEach
    fun restorePreferences() {
        IoController.preferences.editorProperty.lastAnchorId = originalLastAnchorId
    }

    override fun start(stage: Stage) {
        MvvmFX.setGlobalResourceBundle(
            ResourceBundle.getBundle(
                Messages.BUNDLE_NAME,
                Locale.ROOT,
                ResourceBundle.Control.getNoFallbackControl(ResourceBundle.Control.FORMAT_PROPERTIES)
            )
        )

        projectModel = ProjectProperty(project())

        editor = BookPartEditor()
        editor.bindProject(projectModel)
        editor.bindUndoStack(UndoStack())
        editor.bindSelection(SimpleObjectProperty(null))
        sheet = editor.lookup(".paper-sheet-view") as PaperSheetView

        stage.scene = Scene(editor, 700.0, 600.0)
        stage.show()
    }

    private fun style(size: Int = 12): StyleData =
        StyleData(font = FontData("Serif", size, bold = false, italic = false), alignment = Alignment.LEFT)

    private fun project(): Project = Project(
        meta = Meta(name = "My Novel", author = "Jane Doe"),
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
        book = Book(
            prolog = Prolog(included = true),
            chapters = listOf(Chapter("first")),
            epilog = Epilog(),
            blurb = Blurb(paragraph = listOf("The first paragraph"), included = true)
        )
    )

    private val blurb: Blurb get() = projectModel.value.book.blurb

    private fun globalBlockIndex(pageId: String, indexInPage: Int): Int {
        var offset = 0
        for (page in sheet.document?.pages.orEmpty()) {
            if (page.id == pageId) return offset + indexInPage
            offset += page.blocks.size
        }
        return offset + indexInPage
    }

    private val caretOverlay: FloatingOverlay
        get() = sheet.floatingOverlays.single { it.trigger == FloatingOverlayTrigger.CARET }

    private val hoverOverlay: FloatingOverlay
        get() = sheet.floatingOverlays.single { it.trigger == FloatingOverlayTrigger.PARAGRAPH_HOVER }

    private fun select(item: ProjectListItem?) {
        interact { editor.bindSelection(SimpleObjectProperty(item)) }
        WaitForAsyncUtils.waitForFxEvents()
        interact { editor.scene.root.layout() }
        WaitForAsyncUtils.waitForFxEvents()
    }

    /**
     * Use case: the two overlays are registered on the real sheet, so the bar's two instances are
     * actually reachable by `simplay-fx` instead of only existing in the FXML.
     */
    @Test
    fun registersOneOverlayPerTrigger() {
        assertTrue(sheet.floatingOverlays.any { it.trigger == FloatingOverlayTrigger.PARAGRAPH_HOVER })
        assertTrue(sheet.floatingOverlays.any { it.trigger == FloatingOverlayTrigger.CARET })
    }

    /**
     * Use case: the user places the edit caret in a paragraph, so the bar tied to [FloatingOverlayTrigger.CARET]
     * appears next to it - the sheet is in `EDITABLE` mode, where a `CARET` overlay is not inert.
     */
    @Test
    fun showsTheCaretBarOnceTheCaretIsPlaced() {
        select(ProjectListItem.BlurbItem(blurb))
        interact {
            sheet.requestFocus()
            sheet.caretModel.moveToEndOfBlock(globalBlockIndex("blurb", 0))
        }
        WaitForAsyncUtils.waitForFxEvents()
        interact { editor.scene.root.layout() }
        WaitForAsyncUtils.waitForFxEvents()

        assertTrue(caretOverlay.isActive, "the caret bar must show once the caret sits in a paragraph")
    }

    /**
     * Use case: the user moves the mouse over the hovered paragraph, so the bar tied to
     * [FloatingOverlayTrigger.PARAGRAPH_HOVER] appears above it, and it disappears again once the
     * mouse leaves the sheet - simPlay's own `autoHide` behaviour, not logic of this plan.
     */
    @Test
    fun showsAndHidesTheHoverBarWithTheMouse() {
        select(ProjectListItem.BlurbItem(blurb))
        interact {
            sheet.requestFocus()
            sheet.caretModel.moveToEndOfBlock(globalBlockIndex("blurb", 0))
        }
        WaitForAsyncUtils.waitForFxEvents()
        interact { editor.scene.root.layout() }
        WaitForAsyncUtils.waitForFxEvents()

        // The caret bar is already known to be active (showsTheCaretBarOnceTheCaretIsPlaced), and its
        // active bounds name the same paragraph the caret sits in - reused here as the point to hover,
        // instead of duplicating simPlay's own text-to-pixel geometry in this test.
        val bounds = requireNotNull(caretOverlay.activeBounds) { "the caret bar must be active first" }
        val centerInSheet = Point2D(bounds.minX + bounds.width / 2.0, bounds.minY + bounds.height / 2.0)
        val centerOnScreen = sheet.localToScreen(centerInSheet.x, centerInSheet.y)
        requireNotNull(centerOnScreen) { "the sheet must be laid out and showing" }

        moveTo(centerOnScreen)
        WaitForAsyncUtils.waitForFxEvents()
        interact { editor.scene.root.layout() }
        WaitForAsyncUtils.waitFor(2, TimeUnit.SECONDS) { hoverOverlay.isActive }
        assertTrue(hoverOverlay.isActive, "the hover bar must show while the mouse sits over the paragraph")

        // Moved into the empty margin outside the sheet's content, well away from any paragraph.
        moveTo(sheet).moveBy(-sheet.width / 2.0 + 2.0, -sheet.height / 2.0 + 2.0)
        WaitForAsyncUtils.waitForFxEvents()
        interact { editor.scene.root.layout() }
        WaitForAsyncUtils.waitFor(2, TimeUnit.SECONDS) { !hoverOverlay.isActive }
        assertFalse(hoverOverlay.isActive, "the hover bar must disappear once the mouse leaves the paragraph")
    }
}

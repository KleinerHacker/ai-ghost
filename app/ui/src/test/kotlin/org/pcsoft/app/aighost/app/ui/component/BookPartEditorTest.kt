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
import javafx.scene.Scene
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyEvent
import javafx.stage.Stage
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.pcsoft.app.aighost.app.Messages
import org.pcsoft.app.aighost.app.controller.IoController
import org.pcsoft.app.aighost.app.undo.UndoStack
import org.pcsoft.app.aighost.fx.model.project.ProjectProperty
import org.pcsoft.app.aighost.model.pref.WritingMode
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
import org.pcsoft.framework.simplay.fx.PaperSheetMode
import org.pcsoft.framework.simplay.fx.PaperSheetView
import org.pcsoft.framework.simplay.uicommon.PageMode
import org.testfx.framework.junit5.ApplicationTest
import org.testfx.util.WaitForAsyncUtils
import java.util.Locale
import java.util.ResourceBundle

/**
 * Developer tests for [BookPartEditor] - binding, typing on the sheet, undo and the read-only front
 * matter, all headless.
 *
 * Splitting, merging and reordering a paragraph by key are not covered here: `PaperSheetView`'s own
 * editing turns a line break into a space and never creates a new block, so those use cases only exist
 * once IP-32 wires its own key handlers over the sheet.
 *
 * Every typed continuation here is a plain run of letters with no symbol and no space, extending the
 * paragraph's last word instead of starting a new one. simPlay's `TextBlock`/`DocumentEditor` currently
 * desyncs the caret from the stored text on any edit where a word directly follows a symbol without a
 * real space between them (`TextBlock.toString()` inserts a space there that was never typed), and on
 * any edit that leaves a lone trailing space applied on its own (a space with nothing after it yet is
 * dropped on retokenizing) - both silently shift every following keystroke one position early. Reported
 * upstream; a letter-only continuation sidesteps both and still exercises this plan's own pipeline
 * (model write-back, undo, caret preservation). IP-32, which does insert real symbols and spaces via its
 * own key handling, will need this fixed upstream first.
 */
class BookPartEditorTest : ApplicationTest() {

    private lateinit var editor: BookPartEditor
    private lateinit var sheet: PaperSheetView
    private lateinit var projectModel: ProjectProperty
    private lateinit var selection: SimpleObjectProperty<ProjectListItem?>
    private lateinit var undoStack: UndoStack
    private lateinit var stage: Stage

    private var originalWritingMode: WritingMode = WritingMode.WRITING
    private var originalLastAnchorId: String? = null

    @BeforeEach
    fun rememberPreferences() {
        originalWritingMode = IoController.preferences.editorProperty.writingMode
        originalLastAnchorId = IoController.preferences.editorProperty.lastAnchorId
    }

    @AfterEach
    fun restorePreferences() {
        IoController.preferences.editorProperty.writingMode = originalWritingMode
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
        selection = SimpleObjectProperty(null)
        undoStack = UndoStack()

        editor = BookPartEditor()
        editor.bindProject(projectModel)
        editor.bindUndoStack(undoStack)
        editor.bindSelection(selection)
        sheet = editor.lookup(".paper-sheet-view") as PaperSheetView

        this.stage = stage
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
            blurb = Blurb(
                // No trailing punctuation: the fixture ends on a word, so a typed continuation extends
                // it without ever putting a word directly after a symbol - see the class KDoc.
                paragraph = listOf("The first paragraph"),
                included = true
            )
        )
    )

    private val blurb: Blurb get() = projectModel.value.book.blurb
    private val chapter: Chapter get() = projectModel.value.book.chapters.single()

    /** Text of the block at [index] of the page whose id is [pageId], or `null` past its end. */
    private fun blockText(pageId: String, index: Int): String? =
        sheet.document?.pages?.firstOrNull { it.id == pageId }?.blocks?.getOrNull(index)?.toString()

    /**
     * The whole document shown by the sheet flattens every page's blocks into one sequence the caret
     * moves through, so a page-local block index must be translated into that global index before it
     * is handed to [org.pcsoft.framework.simplay.fx.CaretModel].
     */
    private fun globalBlockIndex(pageId: String, indexInPage: Int): Int {
        var offset = 0
        for (page in sheet.document?.pages.orEmpty()) {
            if (page.id == pageId) return offset + indexInPage
            offset += page.blocks.size
        }
        return offset + indexInPage
    }

    private fun select(item: ProjectListItem?) {
        interact { selection.value = item }
        WaitForAsyncUtils.waitForFxEvents()
        interact { editor.scene.root.layout() }
        WaitForAsyncUtils.waitForFxEvents()
    }

    // PaperSheetEditor.onKeyTyped reads event.character straight off a KEY_TYPED event, so a
    // character is fired the same way a real key press resolves to one - the OS-level robot write()
    // goes through native key synthesis instead, which raced this new control's key handling on this
    // platform and reordered typed characters.
    private fun typeSlowly(text: String) {
        for (character in text) {
            interact {
                sheet.fireEvent(
                    KeyEvent(KeyEvent.KEY_TYPED, character.toString(), character.toString(), KeyCode.UNDEFINED, false, false, false, false)
                )
            }
            WaitForAsyncUtils.waitForFxEvents()
        }
    }

    /**
     * Use case: nothing is picked yet, so the sheet already shows the whole book - since IP-39 it is
     * never swapped per selection, a tree pick only navigates within it.
     */
    @Test
    fun showsTheWholeBookBeforeAnythingIsPicked() {
        // The very first measure after opening a project is deferred by one pulse, so its progress
        // indicator gets a chance to paint before the synchronous measure call blocks the FX thread.
        WaitForAsyncUtils.waitForFxEvents()

        assertTrue(sheet.isVisible, "the sheet shows the whole book as soon as a project is open")
    }

    /**
     * Use case: the user picks the blurb, so its paragraph turns up on the sheet as its one block.
     */
    @Test
    fun opensTheBlurbTextOnTheSheet() {
        select(ProjectListItem.BlurbItem(blurb))

        // The block itself still carries the blurb's anchor token at its start (IP-39); the model
        // field the user actually cares about is clean, proven separately in writesEveryKeystrokeIntoTheModel.
        assertEquals("\${blurb}The first paragraph", blockText("blurb", 0))
    }

    /**
     * Use case: the user types at the end of a paragraph on the sheet, so the new text lands in the
     * model of the part.
     */
    @Test
    fun writesEveryKeystrokeIntoTheModel() {
        select(ProjectListItem.BlurbItem(blurb))
        interact {
            sheet.requestFocus()
            sheet.caretModel.moveToEndOfBlock(globalBlockIndex("blurb", 0))
        }
        WaitForAsyncUtils.waitForFxEvents()

        typeSlowly("Extended")

        assertEquals(listOf("The first paragraphExtended"), blurb.paragraph)
    }

    /**
     * Use case: the user picks a chapter, so its anchor seed turns up on the sheet - invisible, since
     * a `TextAnchor` renders as nothing - and typing lands right after it, in the chapter's own page of
     * the book's document (IP-38).
     */
    @Test
    fun opensAChapterOnTheSheetAndWritesTypedTextIntoItsAnchorPage() {
        select(ProjectListItem.ChapterItem(chapter))
        val anchorId = chapter.id.toString()
        interact {
            sheet.requestFocus()
            sheet.caretModel.moveToEndOfBlock(globalBlockIndex(anchorId, 0))
        }
        WaitForAsyncUtils.waitForFxEvents()

        // Letter-only continuation, no space and no symbol - see the class KDoc.
        typeSlowly("Onceuponatime")

        val page = projectModel.value.book.document.pages.single { it.id == anchorId }
        assertEquals("\${$anchorId}Onceuponatime", page.blocks.single().toString())
    }

    /**
     * Use case: the user undoes a text change on the sheet, so the paragraph returns to what it was.
     */
    @Test
    fun undoesATextChange() {
        select(ProjectListItem.BlurbItem(blurb))
        interact {
            sheet.requestFocus()
            sheet.caretModel.moveToEndOfBlock(globalBlockIndex("blurb", 0))
        }
        WaitForAsyncUtils.waitForFxEvents()

        typeSlowly("More")
        assertTrue(undoStack.canUndoProperty.get(), "a text change must be undoable")

        interact { undoStack.undo() }
        WaitForAsyncUtils.waitForFxEvents()

        assertEquals(listOf("The first paragraph"), blurb.paragraph)
    }

    /**
     * Use case: a design value changes while the blurb is open, so the sheet is rebuilt with the new
     * style and the caret keeps its linear place, since restyling never changes the text itself.
     */
    @Test
    fun keepsTheCaretAcrossADesignChange() {
        select(ProjectListItem.BlurbItem(blurb))
        interact {
            sheet.requestFocus()
            sheet.caretModel.moveIntoBlock(globalBlockIndex("blurb", 0), 4)
        }
        WaitForAsyncUtils.waitForFxEvents()
        val caretBefore = sheet.caretModel.position

        interact {
            projectModel.value.design.blurbPage = BlurbPageDesign(style(size = 16))
            projectModel.designProperty.refresh()
        }
        WaitForAsyncUtils.waitForFxEvents()
        interact { editor.scene.root.layout() }
        WaitForAsyncUtils.waitForFxEvents()

        assertEquals(caretBefore, sheet.caretModel.position, "the caret keeps its offset across the restyling")
    }

    /**
     * Use case: the user picks the title page, so it turns up on the sheet like every other part of
     * the whole book document and a typed change reaches the book's `Document`, in the title's own
     * anchor block.
     */
    @Test
    fun showsTheTitlePageWritable() {
        select(ProjectListItem.TitlePageItem)

        // Block 0 is the title's own anchor seed (IP-38); the author name - still built fresh from
        // Meta, never a write-back target - follows it as block 1.
        assertEquals("Jane Doe", blockText("title", 1), "the title page is rendered on the sheet")
        assertEquals(PaperSheetMode.EDITABLE, sheet.mode)

        interact {
            sheet.requestFocus()
            sheet.caretModel.moveToEndOfBlock(globalBlockIndex("title", 0))
        }
        WaitForAsyncUtils.waitForFxEvents()
        typeSlowly("ATitle")

        val titlePage = projectModel.value.book.document.pages.single { it.id == "title" }
        assertEquals("\${title}ATitle", titlePage.blocks.single().toString())
        assertEquals("Jane Doe", projectModel.value.meta.author, "the author name stays a Meta field")
        assertTrue(undoStack.canUndoProperty.get(), "the title page is writable since IP-39")
    }

    /**
     * Use case: the user types several characters in a row at the end of a paragraph, so each one
     * lands after the one typed before it and the caret advances with them - the sheet is never
     * rebuilt from the model between keystrokes, so there is no rebuild to reset it.
     */
    @Test
    fun caretAdvancesWhileTypingSeveralCharactersInARow() {
        select(ProjectListItem.BlurbItem(blurb))
        interact {
            sheet.requestFocus()
            sheet.caretModel.moveToEndOfBlock(globalBlockIndex("blurb", 0))
        }
        WaitForAsyncUtils.waitForFxEvents()
        val caretBefore = sheet.caretModel.position

        typeSlowly("XYZ")

        assertEquals("\${blurb}The first paragraphXYZ", blockText("blurb", 0))
        assertEquals(caretBefore + 3, sheet.caretModel.position, "the caret must sit right after the last character typed")
    }

    /**
     * Use case: the user switches the sheet to the preview, so the real `PaperSheetView` mode changes
     * from writable to selectable and the choice is saved to the preferences.
     */
    @Test
    fun switchesTheRealSheetToPreview() {
        interact { editor.setWritingMode(WritingMode.PREVIEW) }
        WaitForAsyncUtils.waitForFxEvents()

        assertEquals(PaperSheetMode.SELECTABLE, sheet.mode)
        assertEquals(WritingMode.PREVIEW, IoController.preferences.editorProperty.writingMode)
    }

    /**
     * Use case: the user navigates to a chapter, so the anchor id is saved to the preferences and a
     * freshly bound editor - the next time the project is opened - lands right back on it.
     */
    @Test
    fun restoresTheLastAnchorOnAFreshBind() {
        select(ProjectListItem.ChapterItem(chapter))
        val anchorId = chapter.id.toString()
        assertEquals(anchorId, IoController.preferences.editorProperty.lastAnchorId)
        val positionAfterNavigating = sheet.caretModel.position

        val freshEditor = BookPartEditor()
        interact {
            // Swapped into the real, shown stage before binding - like `select()` relies on for the
            // original editor - since the sheet only resolves an anchor to a caret position once it is
            // actually part of a shown scene and laid out.
            stage.scene = Scene(freshEditor, 700.0, 600.0)
            freshEditor.bindProject(projectModel)
            freshEditor.bindUndoStack(UndoStack())
            freshEditor.bindSelection(SimpleObjectProperty(null))
        }
        WaitForAsyncUtils.waitForFxEvents()
        interact { freshEditor.scene.root.layout() }
        WaitForAsyncUtils.waitForFxEvents()
        val freshSheet = freshEditor.lookup(".paper-sheet-view") as PaperSheetView

        // Same document, same anchor, so the deterministic linear caret position matches, without
        // depending on a way to read the anchor id back off the caret.
        assertEquals(positionAfterNavigating, freshSheet.caretModel.position)
    }

    /**
     * Use case: the project is opened with the epilog switched off (IP-23), so its page is marked
     * [PageMode.DISABLED] on the real sheet right from the start, without a tree pick being needed
     * first - `project()`'s epilog carries no `included = true`, unlike its prolog and blurb.
     */
    @Test
    fun marksASwitchedOffPartDisabledAsSoonAsTheProjectIsShown() {
        WaitForAsyncUtils.waitForFxEvents()

        assertEquals(PageMode.DISABLED, sheet.pageModes["epilog"])
        assertFalse(sheet.pageModes.containsKey("prolog"), "an included part must carry no override")
        assertFalse(sheet.pageModes.containsKey("blurb"), "an included part must carry no override")
    }

    /**
     * Use case: the user switches the epilog on, so its page immediately stops being disabled -
     * without the sheet being rebuilt, since the switch alone drives `PaperSheetView.pageModes`.
     */
    @Test
    fun clearsTheDisabledPageModeAsSoonAsThePartIsSwitchedOn() {
        WaitForAsyncUtils.waitForFxEvents()

        interact { projectModel.bookProperty.epilogProperty.includedProperty.value = true }

        assertFalse(sheet.pageModes.containsKey("epilog"))
    }

    /**
     * Use case: the user switches the prolog off, so its page is disabled on the sheet, the anchor
     * seed already on it staying exactly where it is.
     */
    @Test
    fun disablesAPartAsSoonAsItIsSwitchedOff() {
        WaitForAsyncUtils.waitForFxEvents()
        val textBefore = blockText("prolog", 0)

        interact { projectModel.bookProperty.prologProperty.includedProperty.value = false }

        assertEquals(PageMode.DISABLED, sheet.pageModes["prolog"])
        assertEquals(textBefore, blockText("prolog", 0), "switching a part off must not touch its text")
    }

    /**
     * Use case: a design change rebuilds the whole document from the model, so the switched-off
     * epilog's page mode override survives the rebuild instead of being lost with the fresh document
     * `PaperSheetView.pageModes` would otherwise be cleared for.
     */
    @Test
    fun keepsTheDisabledPageModeAcrossADesignChange() {
        WaitForAsyncUtils.waitForFxEvents()

        interact {
            projectModel.value.design.blurbPage = BlurbPageDesign(style(size = 16))
            projectModel.designProperty.refresh()
        }
        WaitForAsyncUtils.waitForFxEvents()

        assertEquals(PageMode.DISABLED, sheet.pageModes["epilog"])
    }
}

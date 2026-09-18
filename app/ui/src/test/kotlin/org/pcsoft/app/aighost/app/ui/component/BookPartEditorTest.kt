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
import org.pcsoft.app.aighost.layouting.model.common.toPageLayout
import org.pcsoft.app.aighost.layouting.model.common.toTextStyle
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
import org.pcsoft.framework.simplay.engine.model.Document
import org.pcsoft.framework.simplay.engine.model.FlowPage
import org.pcsoft.framework.simplay.engine.model.TextBlock
import org.pcsoft.framework.simplay.fx.PaperSheetMode
import org.pcsoft.framework.simplay.fx.PaperSheetView
import org.pcsoft.framework.simplay.uicommon.PageMode
import org.testfx.framework.junit5.ApplicationTest
import org.testfx.util.WaitForAsyncUtils
import java.util.Locale
import java.util.ResourceBundle

/**
 * Developer tests for [BookPartEditor] - binding, typing on the sheet, undo/redo of both text and
 * structural changes (IP-33), and the read-only front matter, all headless.
 *
 * Splitting, merging and reordering a paragraph by key are covered here through the sheet's context
 * menu ([BookPartEditorView.buildContextMenu]) instead of firing the real `Enter`/`Ctrl+Shift+Up`/
 * `Ctrl+Shift+Down` key combination - the menu items call the exact same `internal` view model methods
 * a key press does, without depending on the platform's native key synthesis.
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

    /** Every block of the page whose id is [pageId], read straight off the book's stored `Document`. */
    private fun storedBlocks(pageId: String): List<String> =
        projectModel.value.book.document.pages.single { it.id == pageId }.blocks.map { it.toString() }

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

    // Index of an item in the sheet's context menu built by BookPartEditorView.buildContextMenu -
    // split, merge with previous, merge with next, remove, move up, move down, in that order (IP-32).
    // Firing the MenuItem calls the same internal view model method a key press or a real click would.
    private fun fireContextMenuItem(index: Int) {
        interact { sheet.contextMenu.items[index].fire() }
        WaitForAsyncUtils.waitForFxEvents()
    }

    private fun fireSplit() = fireContextMenuItem(0)
    private fun fireMergeWithNext() = fireContextMenuItem(2)
    private fun fireMoveUp() = fireContextMenuItem(4)
    private fun fireMoveDown() = fireContextMenuItem(5)

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
     * Use case: the user redoes a text change undone right before, so the typed text reappears in the
     * model exactly as it was typed, and the redo history is empty again afterwards (IP-33).
     */
    @Test
    fun redoesATextChange() {
        select(ProjectListItem.BlurbItem(blurb))
        interact {
            sheet.requestFocus()
            sheet.caretModel.moveToEndOfBlock(globalBlockIndex("blurb", 0))
        }
        WaitForAsyncUtils.waitForFxEvents()

        typeSlowly("More")
        interact { undoStack.undo() }
        WaitForAsyncUtils.waitForFxEvents()

        interact { undoStack.redo() }
        WaitForAsyncUtils.waitForFxEvents()

        assertEquals(listOf("The first paragraphMore"), blurb.paragraph, "redo must bring the typed text back")
        assertTrue(undoStack.canUndoProperty.get(), "the redone change must be undoable again")
        assertFalse(undoStack.canRedoProperty.get(), "nothing must remain to redo once it was replayed")
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
     * Use case: the user splits a paragraph on the sheet, so the block is cut in two and the change is
     * one single undo step that undo/redo replays exactly (IP-32/IP-33). The exact linear caret offset
     * [DocumentStructureUndoEntry] hands back is already proven, against a mocked callback, by
     * `DocumentStructureUndoEntryTest`; a caret sitting at the very end of a page's last block is a
     * boundary `CaretModel` itself may resolve onto the following page, so this test does not read the
     * caret back at all - it proves the wiring around it instead: the whole book `Document` really is
     * swapped, on every one of split, undo and redo.
     *
     * The split happens right at the end of the typed text - the same caret position every other test
     * here types into - so it needs no extra, ambiguous caret arithmetic of its own: the block is cut
     * into itself and a trailing empty block, which is exactly as real a structural change as a mid-word
     * split for undo/redo purposes.
     */
    @Test
    fun splitPushesOneUndoStepAndRestoresStructureOnUndoRedo() {
        select(ProjectListItem.ChapterItem(chapter))
        val anchorId = chapter.id.toString()
        interact {
            sheet.requestFocus()
            sheet.caretModel.moveToEndOfBlock(globalBlockIndex(anchorId, 0))
        }
        WaitForAsyncUtils.waitForFxEvents()
        typeSlowly("HelloWorld")
        val countBeforeSplit = undoStack.undoEntries.size

        fireSplit()

        assertEquals(2, storedBlocks(anchorId).size, "the split must create a second block")
        assertEquals(countBeforeSplit + 1, undoStack.undoEntries.size, "the split must push exactly one undo entry")

        interact { undoStack.undo() }
        WaitForAsyncUtils.waitForFxEvents()

        assertEquals(listOf("\${$anchorId}HelloWorld"), storedBlocks(anchorId), "undo must merge the split back into one block")

        interact { undoStack.redo() }
        WaitForAsyncUtils.waitForFxEvents()

        assertEquals(2, storedBlocks(anchorId).size, "redo must split the block again")
    }

    /**
     * Use case: the user merges two blocks of a paragraph back together on the sheet, so the change is
     * one single undo step and undo/redo replays the block count exactly (IP-32/IP-33).
     */
    @Test
    fun mergePushesOneUndoStepAndRestoresStructureOnUndoRedo() {
        select(ProjectListItem.ChapterItem(chapter))
        val anchorId = chapter.id.toString()
        interact {
            sheet.requestFocus()
            sheet.caretModel.moveToEndOfBlock(globalBlockIndex(anchorId, 0))
        }
        WaitForAsyncUtils.waitForFxEvents()
        typeSlowly("HelloWorld")
        fireSplit()
        assertEquals(2, storedBlocks(anchorId).size, "the fixture for this test must start out split in two")

        // The split leaves the caret on the new, trailing empty block - moved back onto the first block
        // so "merge with next" pulls the empty one back into it, undoing the split in effect.
        interact { sheet.caretModel.moveToEndOfBlock(globalBlockIndex(anchorId, 0)) }
        WaitForAsyncUtils.waitForFxEvents()
        val countBeforeMerge = undoStack.undoEntries.size

        fireMergeWithNext()

        assertEquals(1, storedBlocks(anchorId).size, "the merge must combine both blocks back into one")
        assertEquals(countBeforeMerge + 1, undoStack.undoEntries.size, "the merge must push exactly one undo entry")

        interact { undoStack.undo() }
        WaitForAsyncUtils.waitForFxEvents()

        assertEquals(2, storedBlocks(anchorId).size, "undo must restore both blocks split apart")

        interact { undoStack.redo() }
        WaitForAsyncUtils.waitForFxEvents()

        assertEquals(1, storedBlocks(anchorId).size, "redo must merge the blocks again")
    }

    /**
     * Use case: the user moves a block of a paragraph on the sheet, so the change is one single undo
     * step and undo/redo replays the block order exactly (IP-32/IP-33).
     *
     * `BookPartEditorController.moveTextBlock` never moves the block at index `0`, since it carries the
     * page's `TextAnchor`, so this needs three blocks - and every one of them has to carry text, because
     * `CaretModel` cannot put the caret into an empty block at all: it slides on to the next block that
     * has some, across a page boundary if need be. Splitting on the sheet cannot produce three such
     * blocks (a split at a block's end leaves the new one empty), so they are seeded straight into the
     * book's `Document` instead, the way `BookPartEditorControllerTest` seeds a chapter that already
     * carries text. Seeding also makes the move readable in the block list itself - three blocks that
     * differ show the swap, where two empty ones read the same whether it ran or not.
     */
    @Test
    fun movePushesOneUndoStepAndRestoresBlockCountOnUndoRedo() {
        val anchorId = chapter.id.toString()
        val seeded = listOf("\${$anchorId}Alpha", "Beta", "Gamma")
        interact {
            val design = projectModel.value.design
            projectModel.bookProperty.document = Document(
                pages = listOf(
                    FlowPage(
                        design.pageFormat.toPageLayout(),
                        seeded.map { TextBlock.of(it, design.chapterPage.textStyle.toTextStyle()) },
                        id = anchorId
                    )
                )
            )
            projectModel.designProperty.refresh()
        }
        WaitForAsyncUtils.waitForFxEvents()
        select(ProjectListItem.ChapterItem(chapter))
        assertEquals(seeded, storedBlocks(anchorId), "the fixture must start from three blocks that all carry text")

        interact {
            sheet.requestFocus()
            sheet.caretModel.moveIntoBlock(globalBlockIndex(anchorId, 1), 0)
        }
        WaitForAsyncUtils.waitForFxEvents()
        val countBeforeMove = undoStack.undoEntries.size

        // Moving that block up would land on the immovable anchor block at index 0, so it is moved down.
        fireMoveDown()

        val movedOrder = listOf(seeded[0], seeded[2], seeded[1])
        assertEquals(movedOrder, storedBlocks(anchorId), "the move must swap the block with the one after it")
        assertEquals(countBeforeMove + 1, undoStack.undoEntries.size, "the move must push exactly one undo entry")

        interact { undoStack.undo() }
        WaitForAsyncUtils.waitForFxEvents()

        assertEquals(seeded, storedBlocks(anchorId), "undo must put the block back where it was")

        interact { undoStack.redo() }
        WaitForAsyncUtils.waitForFxEvents()

        assertEquals(movedOrder, storedBlocks(anchorId), "redo must move the block again")
    }

    /**
     * Use case: the surrounding window's undo history is cleared, as it is on every project switch
     * (`MainWindowViewModel.newProject`/`openProject`), so neither an undo nor a redo can reach into a
     * project that no longer applies (IP-33).
     */
    @Test
    fun clearingTheUndoStackDiscardsUndoAndRedoHistory() {
        select(ProjectListItem.BlurbItem(blurb))
        interact {
            sheet.requestFocus()
            sheet.caretModel.moveToEndOfBlock(globalBlockIndex("blurb", 0))
        }
        WaitForAsyncUtils.waitForFxEvents()
        typeSlowly("More")
        interact { undoStack.undo() }
        WaitForAsyncUtils.waitForFxEvents()
        assertTrue(undoStack.canRedoProperty.get(), "the undone change must be redoable before the stack is cleared")

        interact { undoStack.clear() }

        assertFalse(undoStack.canUndoProperty.get(), "a cleared stack must have nothing left to undo")
        assertFalse(undoStack.canRedoProperty.get(), "a cleared stack must have nothing left to redo")
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

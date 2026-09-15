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
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.pcsoft.app.aighost.app.Messages
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
import org.pcsoft.framework.simplay.fx.PaperSheetMode
import org.pcsoft.framework.simplay.fx.PaperSheetView
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

    /** Text of the block at [index] of the sheet's current document, or `null` past its end. */
    private fun blockText(index: Int): String? =
        sheet.document?.pages?.firstOrNull()?.blocks?.getOrNull(index)?.toString()

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
     * Use case: nothing is picked, so the sheet shows its hint instead of a writing surface.
     */
    @Test
    fun showsAHintWhileNothingIsPicked() {
        assertFalse(sheet.isVisible, "the sheet stays hidden while nothing is picked")
    }

    /**
     * Use case: the user picks the blurb, so its paragraph turns up on the sheet as its one block.
     */
    @Test
    fun opensTheBlurbTextOnTheSheet() {
        select(ProjectListItem.BlurbItem(blurb))

        assertEquals("The first paragraph", blockText(0))
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
            sheet.caretModel.moveToEndOfBlock(0)
        }
        WaitForAsyncUtils.waitForFxEvents()

        typeSlowly("Extended")

        assertEquals(listOf("The first paragraphExtended"), blurb.paragraph)
    }

    /**
     * Use case: the user undoes a text change on the sheet, so the paragraph returns to what it was.
     */
    @Test
    fun undoesATextChange() {
        select(ProjectListItem.BlurbItem(blurb))
        interact {
            sheet.requestFocus()
            sheet.caretModel.moveToEndOfBlock(0)
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
            sheet.caretModel.moveIntoBlock(0, 4)
        }
        WaitForAsyncUtils.waitForFxEvents()

        interact {
            projectModel.value.design.blurbPage = BlurbPageDesign(style(size = 16))
            projectModel.designProperty.refresh()
        }
        WaitForAsyncUtils.waitForFxEvents()
        interact { editor.scene.root.layout() }
        WaitForAsyncUtils.waitForFxEvents()

        assertEquals(4, sheet.caretModel.position, "the caret keeps its offset across the restyling")
    }

    /**
     * Use case: the user picks the title page, so it is shown read only and a typed change is
     * discarded instead of reaching the model.
     */
    @Test
    fun showsTheTitlePageReadOnly() {
        select(ProjectListItem.TitlePageItem)

        assertEquals("Jane Doe", blockText(0), "the title page is rendered on the sheet")
        assertEquals(PaperSheetMode.SELECTABLE, sheet.mode)

        interact { sheet.requestFocus() }
        typeSlowly("A Different Title")

        assertEquals("Jane Doe", projectModel.value.meta.author, "the title page must not be writable")
        assertFalse(undoStack.canUndoProperty.get(), "a read-only sheet records no undo entry")
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
            sheet.caretModel.moveToEndOfBlock(0)
        }
        WaitForAsyncUtils.waitForFxEvents()

        typeSlowly("XYZ")

        assertEquals("The first paragraphXYZ", blockText(0))
        assertEquals(22, sheet.caretModel.position, "the caret must sit right after the last character typed")
    }
}

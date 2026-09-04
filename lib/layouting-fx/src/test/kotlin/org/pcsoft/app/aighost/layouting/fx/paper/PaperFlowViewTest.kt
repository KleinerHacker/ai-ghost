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

package org.pcsoft.app.aighost.layouting.fx.paper

import javafx.scene.Scene
import javafx.scene.control.TextArea
import javafx.scene.input.KeyCode
import javafx.scene.layout.StackPane
import javafx.stage.Stage
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.pcsoft.app.aighost.layouting.DocumentLayout
import org.pcsoft.app.aighost.layouting.LaidOutLine
import org.pcsoft.app.aighost.layouting.Page
import org.pcsoft.app.aighost.layouting.PageGeometry
import org.pcsoft.app.aighost.layouting.TextStyle
import org.testfx.framework.junit5.ApplicationTest
import org.testfx.util.WaitForAsyncUtils

/**
 * Developer tests of [PaperFlowView] and its skin: block reconstruction, page break marks, the
 * reported column width, reported editing events and the preservation of caret and focus across a
 * fresh layout.
 */
class PaperFlowViewTest : ApplicationTest() {

    private lateinit var stage: Stage
    private lateinit var view: PaperFlowView

    override fun start(stage: Stage) {
        this.stage = stage
        view = PaperFlowView()
        stage.scene = Scene(StackPane(view), 400.0, 500.0)
        stage.show()
    }

    private val geometry = PageGeometry(
        width = 200.0,
        height = 300.0,
        innerMargin = 20.0,
        outerMargin = 20.0,
        topMargin = 20.0,
        bottomMargin = 20.0
    )

    private fun style(): TextStyle = TextStyle(family = "Serif", size = 12.0)

    private fun line(text: String, blockIndex: Int, charStart: Int, charEnd: Int) = LaidOutLine(
        x = 20.0,
        y = 20.0,
        baseline = 33.0,
        width = 160.0,
        text = text,
        style = style(),
        blockIndex = blockIndex,
        charStart = charStart,
        charEnd = charEnd,
        wordSpacing = 0.0
    )

    private fun page(position: Int, pageNumber: Int?, lines: List<LaidOutLine>) = Page(
        position = position,
        pageNumber = pageNumber,
        active = true,
        lines = lines,
        leftMargin = 20.0,
        rightMargin = 20.0,
        topMargin = 20.0,
        bottomMargin = 20.0
    )

    private fun setDocument(layout: DocumentLayout) {
        interact {
            view.pageGeometry = geometry
            view.documentLayout = layout
        }
        interact { stage.scene.root.layout() }
    }

    /** Two blocks on the same page each get their own text control, carrying the block's own text. */
    @Test
    fun `one text control is built per block`() {
        val layout = DocumentLayout(
            listOf(
                page(
                    0, 1, listOf(
                        line("First block", 0, 0, 11),
                        line("Second block", 1, 0, 12)
                    )
                )
            )
        )
        setDocument(layout)

        val textAreas = view.lookupAll(".paper-flow-view-block").toList().filterIsInstance<TextArea>()

        assertEquals(2, textAreas.size, "Every block must get its own text control")
        assertTrue(textAreas.any { it.text == "First block" })
        assertTrue(textAreas.any { it.text == "Second block" })
    }

    /** A page break falling between two blocks leaves a real gap between their text controls. */
    @Test
    fun `page break between blocks leaves a real gap`() {
        val layout = DocumentLayout(
            listOf(
                page(0, 1, listOf(line("First block", 0, 0, 11))),
                page(1, 2, listOf(line("Second block", 1, 0, 12)))
            )
        )
        setDocument(layout)

        val gaps = view.lookupAll(".paper-flow-view-gap")

        assertTrue(gaps.isNotEmpty(), "A block boundary crossing a page must render a gap region")
    }

    /** A page break falling inside one block draws a dashed break mark instead of a gap. */
    @Test
    fun `page break inside a block draws a break mark`() {
        val layout = DocumentLayout(
            listOf(
                page(0, 1, listOf(line("First half of block", 0, 0, 20))),
                page(1, 2, listOf(line("second half of block", 0, 21, 42)))
            )
        )
        setDocument(layout)
        WaitForAsyncUtils.waitForFxEvents()

        val marks = view.lookupAll(".paper-flow-view-break-mark")

        assertTrue(marks.isNotEmpty(), "A break inside a block must render a dashed mark")
    }

    /** The reported column width is positive once a document and a geometry were set. */
    @Test
    fun `column width is reported once laid out`() {
        val layout = DocumentLayout(listOf(page(0, 1, listOf(line("Text", 0, 0, 4)))))
        setDocument(layout)
        WaitForAsyncUtils.waitForFxEvents()

        assertTrue(view.columnWidth > 0.0, "The effective column width must be reported once the block is laid out")
    }

    /** Typing into a block's text control reports the change through [PaperFlowListener]. */
    @Test
    fun `typing reports a text change`() {
        val layout = DocumentLayout(listOf(page(0, 1, listOf(line("Text", 0, 0, 4)))))
        setDocument(layout)

        var reportedText: String? = null
        view.addPaperFlowListener(object : PaperFlowListener {
            override fun onTextChanged(blockIndex: Int, text: String) {
                reportedText = text
            }
        })

        val textArea = view.lookupAll(".paper-flow-view-block").first() as TextArea
        interact { textArea.text = "Text changed" }

        assertEquals("Text changed", reportedText, "The listener must see the new text of the block")
    }

    /** Pressing Enter inside a block reports a split request instead of inserting a newline. */
    @Test
    fun `enter reports a split request`() {
        val layout = DocumentLayout(listOf(page(0, 1, listOf(line("Text", 0, 0, 4)))))
        setDocument(layout)

        var splitBlockIndex: Int? = null
        var splitCharIndex: Int? = null
        view.addPaperFlowListener(object : PaperFlowListener {
            override fun onSplitRequested(blockIndex: Int, charIndex: Int) {
                splitBlockIndex = blockIndex
                splitCharIndex = charIndex
            }
        })

        val textArea = view.lookupAll(".paper-flow-view-block").first() as TextArea
        interact {
            textArea.requestFocus()
            textArea.positionCaret(2)
        }
        interact { textArea.fireEvent(keyPressed(KeyCode.ENTER)) }

        assertEquals(0, splitBlockIndex, "The split request must name the block the caret sat in")
        assertEquals(2, splitCharIndex, "The split request must carry the caret position")
        assertEquals("Text", textArea.text, "Enter must not insert a newline into the block")
    }

    /** Backspace at the very start of a block reports a merge request with the previous block. */
    @Test
    fun `backspace at block start reports a merge request`() {
        val layout = DocumentLayout(
            listOf(
                page(
                    0, 1, listOf(
                        line("First", 0, 0, 5),
                        line("Second", 1, 0, 6)
                    )
                )
            )
        )
        setDocument(layout)

        var mergedBlockIndex: Int? = null
        var mergedWithPrevious: Boolean? = null
        view.addPaperFlowListener(object : PaperFlowListener {
            override fun onMergeRequested(blockIndex: Int, withPrevious: Boolean) {
                mergedBlockIndex = blockIndex
                mergedWithPrevious = withPrevious
            }
        })

        val textAreas = view.lookupAll(".paper-flow-view-block").toList().filterIsInstance<TextArea>()
        val secondBlockArea = textAreas.first { it.text == "Second" }
        interact {
            secondBlockArea.requestFocus()
            secondBlockArea.positionCaret(0)
        }
        interact { secondBlockArea.fireEvent(keyPressed(KeyCode.BACK_SPACE)) }

        assertEquals(1, mergedBlockIndex, "The merge request must name the block the caret sat in")
        assertEquals(true, mergedWithPrevious, "Backspace at the start merges with the previous block")
    }

    /** Plain Up at the very start of a block moves the focus and the caret into the previous block. */
    @Test
    fun `up at block start moves focus to the previous block`() {
        val layout = DocumentLayout(
            listOf(
                page(
                    0, 1, listOf(
                        line("First", 0, 0, 5),
                        line("Second", 1, 0, 6)
                    )
                )
            )
        )
        setDocument(layout)

        val textAreas = view.lookupAll(".paper-flow-view-block").toList().filterIsInstance<TextArea>()
        val secondBlockArea = textAreas.first { it.text == "Second" }
        interact {
            secondBlockArea.requestFocus()
            secondBlockArea.positionCaret(0)
        }
        interact { secondBlockArea.fireEvent(keyPressed(KeyCode.UP)) }

        val firstBlockArea = textAreas.first { it.text == "First" }
        assertTrue(firstBlockArea.isFocused, "Up at the start of a block must move the focus to the previous block")
        assertEquals(5, firstBlockArea.caretPosition, "The caret must land at the end of the previous block's text")
    }

    /** Plain Down at the very end of a block moves the focus and the caret into the next block. */
    @Test
    fun `down at block end moves focus to the next block`() {
        val layout = DocumentLayout(
            listOf(
                page(
                    0, 1, listOf(
                        line("First", 0, 0, 5),
                        line("Second", 1, 0, 6)
                    )
                )
            )
        )
        setDocument(layout)

        val textAreas = view.lookupAll(".paper-flow-view-block").toList().filterIsInstance<TextArea>()
        val firstBlockArea = textAreas.first { it.text == "First" }
        interact {
            firstBlockArea.requestFocus()
            firstBlockArea.positionCaret(5)
        }
        interact { firstBlockArea.fireEvent(keyPressed(KeyCode.DOWN)) }

        val secondBlockArea = textAreas.first { it.text == "Second" }
        assertTrue(secondBlockArea.isFocused, "Down at the end of a block must move the focus to the next block")
        assertEquals(0, secondBlockArea.caretPosition, "The caret must land at the start of the next block's text")
    }

    /**
     * A block wrapped onto several lines: Up and Down must only cross into the neighbouring block
     * once the caret sits on the outermost of those lines, never merely at the very first or the very
     * last character - the same way a native multi-line field only exits at its own edge.
     */
    private fun wrappedLayout() = DocumentLayout(
        listOf(
            page(
                0, 1, listOf(
                    line("Before", 0, 0, 6),
                    line("First line", 1, 0, 10),
                    line("Second line", 1, 11, 22),
                    line("Third line", 1, 23, 33),
                    line("After", 2, 0, 5)
                )
            )
        )
    )

    /** Up or Down with the caret on a wrapped line that is neither the first nor the last is a no-op. */
    @Test
    fun `up and down on a middle wrapped line stay inside the block`() {
        setDocument(wrappedLayout())

        val textAreas = view.lookupAll(".paper-flow-view-block").toList().filterIsInstance<TextArea>()
        val middleArea = textAreas.first { it.text == "First line Second line Third line" }
        interact {
            middleArea.requestFocus()
            middleArea.positionCaret(15)
        }

        interact { middleArea.fireEvent(keyPressed(KeyCode.UP)) }
        assertTrue(middleArea.isFocused, "Up on a middle wrapped line must not leave the block")
        assertEquals(15, middleArea.caretPosition, "Up on a middle wrapped line must not move the caret")

        interact { middleArea.fireEvent(keyPressed(KeyCode.DOWN)) }
        assertTrue(middleArea.isFocused, "Down on a middle wrapped line must not leave the block")
        assertEquals(15, middleArea.caretPosition, "Down on a middle wrapped line must not move the caret")
    }

    /** Up anywhere on a block's first wrapped line - not only at its very first character - crosses over. */
    @Test
    fun `up on the first wrapped line moves focus even off its very start`() {
        setDocument(wrappedLayout())

        val textAreas = view.lookupAll(".paper-flow-view-block").toList().filterIsInstance<TextArea>()
        val middleArea = textAreas.first { it.text == "First line Second line Third line" }
        interact {
            middleArea.requestFocus()
            middleArea.positionCaret(3)
        }
        interact { middleArea.fireEvent(keyPressed(KeyCode.UP)) }

        val beforeArea = textAreas.first { it.text == "Before" }
        assertTrue(beforeArea.isFocused, "Up on the first wrapped line must move to the previous block")
        assertEquals(6, beforeArea.caretPosition, "The caret must land at the end of the previous block's text")
    }

    /** Down anywhere on a block's last wrapped line - not only at its very last character - crosses over. */
    @Test
    fun `down on the last wrapped line moves focus even off its very end`() {
        setDocument(wrappedLayout())

        val textAreas = view.lookupAll(".paper-flow-view-block").toList().filterIsInstance<TextArea>()
        val middleArea = textAreas.first { it.text == "First line Second line Third line" }
        interact {
            middleArea.requestFocus()
            middleArea.positionCaret(30)
        }
        interact { middleArea.fireEvent(keyPressed(KeyCode.DOWN)) }

        val afterArea = textAreas.first { it.text == "After" }
        assertTrue(afterArea.isFocused, "Down on the last wrapped line must move to the next block")
        assertEquals(0, afterArea.caretPosition, "The caret must land at the start of the next block's text")
    }

    /** Up at the start of the very first block has no previous block, so nothing happens. */
    @Test
    fun `up at the first block is a no-op`() {
        val layout = DocumentLayout(listOf(page(0, 1, listOf(line("Only", 0, 0, 4)))))
        setDocument(layout)

        val textArea = view.lookupAll(".paper-flow-view-block").first() as TextArea
        interact {
            textArea.requestFocus()
            textArea.positionCaret(0)
        }
        interact { textArea.fireEvent(keyPressed(KeyCode.UP)) }

        assertTrue(textArea.isFocused, "The only block must keep the focus")
        assertEquals(0, textArea.caretPosition, "The caret must stay where it was")
    }

    /**
     * Handing in a freshly recomputed [DocumentLayout] whose focused block carries the very same text
     * as before - a plain restyling, where no character was typed - keeps the caret at the exact
     * offset and the focus of the block the person was writing in.
     */
    @Test
    fun `caret and focus survive a restyling that leaves the text unchanged`() {
        val layout = DocumentLayout(listOf(page(0, 1, listOf(line("Text", 0, 0, 4)))))
        setDocument(layout)

        val textArea = view.lookupAll(".paper-flow-view-block").first() as TextArea
        interact {
            textArea.requestFocus()
            textArea.positionCaret(2)
        }

        val newLayout = DocumentLayout(listOf(page(0, 1, listOf(line("Text", 0, 0, 4)))))
        setDocument(newLayout)

        val newTextArea = view.lookupAll(".paper-flow-view-block").first() as TextArea
        assertTrue(newTextArea.isFocused, "The block that carried the focus must keep it after a new layout")
        assertEquals(2, newTextArea.caretPosition, "The caret position must be kept when the text did not change")
    }

    /**
     * Handing in a freshly recomputed [DocumentLayout] whose focused block's text grew by one
     * character - as a consumer would after applying a reported text change, the same shape a real
     * keystroke produces - shifts the caret by that same length difference instead of leaving it at
     * the offset it carried before the character was typed, which used to insert every further
     * keystroke in front of the one before it instead of after.
     */
    @Test
    fun `caret advances by the text length change after an edit`() {
        val layout = DocumentLayout(listOf(page(0, 1, listOf(line("Text", 0, 0, 4)))))
        setDocument(layout)

        val textArea = view.lookupAll(".paper-flow-view-block").first() as TextArea
        interact {
            textArea.requestFocus()
            textArea.positionCaret(4)
        }

        val newLayout = DocumentLayout(listOf(page(0, 1, listOf(line("Text!", 0, 0, 5)))))
        setDocument(newLayout)

        val newTextArea = view.lookupAll(".paper-flow-view-block").first() as TextArea
        assertTrue(newTextArea.isFocused, "The block that carried the focus must keep it after the edit")
        assertEquals(
            5,
            newTextArea.caretPosition,
            "The caret must land after the inserted character, not at its pre-edit offset"
        )
    }

    /** Ctrl+Shift+Up inside a block reports a move-up request for that block. */
    @Test
    fun `ctrl+shift+up reports a move-up request`() {
        val layout = DocumentLayout(
            listOf(
                page(
                    0, 1, listOf(
                        line("First", 0, 0, 5),
                        line("Second", 1, 0, 6)
                    )
                )
            )
        )
        setDocument(layout)

        var movedBlockIndex: Int? = null
        var movedUp: Boolean? = null
        view.addPaperFlowListener(object : PaperFlowListener {
            override fun onMoveRequested(blockIndex: Int, up: Boolean) {
                movedBlockIndex = blockIndex
                movedUp = up
            }
        })

        val textAreas = view.lookupAll(".paper-flow-view-block").toList().filterIsInstance<TextArea>()
        val secondBlockArea = textAreas.first { it.text == "Second" }
        interact { secondBlockArea.requestFocus() }
        interact { secondBlockArea.fireEvent(keyPressed(KeyCode.UP, control = true, shift = true)) }

        assertEquals(1, movedBlockIndex, "The move request must name the block the caret sat in")
        assertEquals(true, movedUp, "Ctrl+Shift+Up must move the block towards the start")
    }

    /** Ctrl+Shift+Down inside a block reports a move-down request for that block. */
    @Test
    fun `ctrl+shift+down reports a move-down request`() {
        val layout = DocumentLayout(
            listOf(
                page(
                    0, 1, listOf(
                        line("First", 0, 0, 5),
                        line("Second", 1, 0, 6)
                    )
                )
            )
        )
        setDocument(layout)

        var movedBlockIndex: Int? = null
        var movedUp: Boolean? = null
        view.addPaperFlowListener(object : PaperFlowListener {
            override fun onMoveRequested(blockIndex: Int, up: Boolean) {
                movedBlockIndex = blockIndex
                movedUp = up
            }
        })

        val textAreas = view.lookupAll(".paper-flow-view-block").toList().filterIsInstance<TextArea>()
        val firstBlockArea = textAreas.first { it.text == "First" }
        interact { firstBlockArea.requestFocus() }
        interact { firstBlockArea.fireEvent(keyPressed(KeyCode.DOWN, control = true, shift = true)) }

        assertEquals(0, movedBlockIndex, "The move request must name the block the caret sat in")
        assertEquals(false, movedUp, "Ctrl+Shift+Down must move the block towards the end")
    }

    /**
     * [PaperFlowView.requestCaret] places the caret on the block and offset it names on the next
     * layout, taking priority over the block the caret sat in before - the case a split, a merge or a
     * move needs, where the caret must land somewhere other than where it was.
     */
    @Test
    fun `requestCaret is applied on the next layout instead of the caret memo`() {
        val layout = DocumentLayout(
            listOf(
                page(
                    0, 1, listOf(
                        line("First", 0, 0, 5),
                        line("Second", 1, 0, 6)
                    )
                )
            )
        )
        setDocument(layout)

        val firstBlockArea = view.lookupAll(".paper-flow-view-block").toList()
            .filterIsInstance<TextArea>().first { it.text == "First" }
        interact {
            firstBlockArea.requestFocus()
            firstBlockArea.positionCaret(2)
        }

        interact { view.requestCaret(1, 3) }
        val newLayout = DocumentLayout(
            listOf(
                page(
                    0, 1, listOf(
                        line("First", 0, 0, 5),
                        line("Second!", 1, 0, 7)
                    )
                )
            )
        )
        setDocument(newLayout)

        val textAreas = view.lookupAll(".paper-flow-view-block").toList().filterIsInstance<TextArea>()
        val secondBlockArea = textAreas.first { it.text == "Second!" }
        assertTrue(secondBlockArea.isFocused, "The requested block must take the focus, not the previous one")
        assertEquals(3, secondBlockArea.caretPosition, "The caret must sit at the requested offset")

        // A further layout without a new request, and without the block's text changing, falls back
        // to the ordinary caret memo.
        val thirdLayout = DocumentLayout(
            listOf(
                page(
                    0, 1, listOf(
                        line("First", 0, 0, 5),
                        line("Second!", 1, 0, 7)
                    )
                )
            )
        )
        setDocument(thirdLayout)

        val finalTextAreas = view.lookupAll(".paper-flow-view-block").toList().filterIsInstance<TextArea>()
        val finalSecondBlockArea = finalTextAreas.first { it.text == "Second!" }
        assertTrue(finalSecondBlockArea.isFocused, "A caret request is consumed once, the caret memo keeps working after")
        assertEquals(3, finalSecondBlockArea.caretPosition, "The caret memo keeps the offset of the block that had it")
    }

    private fun keyPressed(code: KeyCode, control: Boolean = false, shift: Boolean = false) = javafx.scene.input.KeyEvent(
        javafx.scene.input.KeyEvent.KEY_PRESSED, "", "", code,
        shift, control, false, false
    )
}

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

    /**
     * Handing in a freshly recomputed [DocumentLayout] - as a consumer would after applying a
     * reported change - keeps the caret position and the focus of the block the person was writing
     * in.
     */
    @Test
    fun `caret and focus survive a new layout`() {
        val layout = DocumentLayout(listOf(page(0, 1, listOf(line("Text", 0, 0, 4)))))
        setDocument(layout)

        val textArea = view.lookupAll(".paper-flow-view-block").first() as TextArea
        interact {
            textArea.requestFocus()
            textArea.positionCaret(2)
        }

        val newLayout = DocumentLayout(listOf(page(0, 1, listOf(line("Text!", 0, 0, 5)))))
        setDocument(newLayout)

        val newTextArea = view.lookupAll(".paper-flow-view-block").first() as TextArea
        assertTrue(newTextArea.isFocused, "The block that carried the focus must keep it after a new layout")
        assertEquals(2, newTextArea.caretPosition, "The caret position must be kept after a new layout")
    }

    private fun keyPressed(code: KeyCode) = javafx.scene.input.KeyEvent(
        javafx.scene.input.KeyEvent.KEY_PRESSED, "", "", code,
        false, false, false, false
    )
}

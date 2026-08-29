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

import de.saxsys.mvvmfx.FxmlView
import de.saxsys.mvvmfx.InjectViewModel
import javafx.beans.Observable
import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.scene.control.Button
import javafx.scene.layout.VBox
import org.pcsoft.app.aighost.app.Messages
import org.pcsoft.app.aighost.app.ui.AiGhostDialog
import org.pcsoft.app.aighost.app.ui.component.base.AiPromptArea
import org.pcsoft.app.aighost.app.ui.component.base.AiTextField
import org.pcsoft.app.aighost.app.ui.component.base.AiTextFieldListItem
import java.net.URL
import java.text.MessageFormat
import java.util.ResourceBundle

/**
 * View of [BookEditor], holding the fields of the manuscript.
 *
 * The title lines are not laid out in the FXML, because there are as many of them as the user wants:
 * they are built here, one [org.pcsoft.app.aighost.app.ui.component.base.AiTextFieldListItem] per line, and follow the list of the view model. An
 * item is only rebuilt when a line was added or removed - a line that was merely rewritten keeps its
 * item, so the user does not lose the cursor while typing.
 *
 * The view asks the question the view model wants answered before a title line is thrown away, and
 * shows it in the window the editor is part of.
 */
class BookEditorView : FxmlView<BookEditorViewModel>, Initializable {

    @FXML
    private lateinit var txtTitle: AiTextField

    @FXML
    private lateinit var boxTitleAppendix: VBox

    @FXML
    private lateinit var btnAddTitleAppendix: Button

    @FXML
    private lateinit var txaContentPrompt: AiPromptArea

    @FXML
    private lateinit var txaStylePrompt: AiPromptArea

    @InjectViewModel
    private lateinit var viewModel: BookEditorViewModel

    // The item of every title line, in the order the lines are shown in, so an item reports the
    // position it sits at.
    private val appendixItems = ArrayList<AiTextFieldListItem>()

    private lateinit var messages: ResourceBundle

    override fun initialize(location: URL?, resources: ResourceBundle?) {
        // The items and the question are built outside of the FXML, so they need the same bundle the
        // FXML around them was resolved with.
        messages = resources ?: Messages.bundle

        txtTitle.text.bindBidirectional(viewModel.title)
        txtTitle.promptText.value = messages.getString(TITLE_PROMPT_KEY)

        txaContentPrompt.text.bindBidirectional(viewModel.contentPrompt)
        txaContentPrompt.maxCharacters.value = PROMPT_MAX_CHARACTERS
        txaStylePrompt.text.bindBidirectional(viewModel.stylePrompt)
        txaStylePrompt.maxCharacters.value = PROMPT_MAX_CHARACTERS

        btnAddTitleAppendix.setOnAction { viewModel.addTitleAppendix() }

        viewModel.confirmRemove = { line ->
            AiGhostDialog.showWarningConfirm(
                messages.getString(REMOVE_TITLE_KEY),
                messages.getString(REMOVE_HEADER_KEY),
                MessageFormat.format(messages.getString(REMOVE_CONTENT_KEY), line),
                boxTitleAppendix.scene?.window
            )
        }

        viewModel.titleAppendix.addListener { _: Observable -> updateTitleAppendix() }
        updateTitleAppendix()
    }

    /**
     * Lets the items follow the title lines of the view model.
     *
     * As long as there are as many items as lines, only the texts are taken over, so an item keeps
     * the field the user is writing in. A line that was added or removed changes the number of
     * items, and the items are built anew.
     */
    private fun updateTitleAppendix() {
        if (appendixItems.size != viewModel.titleAppendix.size) {
            rebuildTitleAppendix()
            return
        }

        for (index in appendixItems.indices) {
            val line = viewModel.titleAppendix[index]
            if (appendixItems[index].text.value != line) {
                appendixItems[index].text.value = line
            }
        }
    }

    /**
     * Builds one entry per title line, each holding the field of the line and the bin removing it.
     */
    private fun rebuildTitleAppendix() {
        appendixItems.clear()

        for (line in viewModel.titleAppendix) {
            val item = AiTextFieldListItem()
            item.text.value = line
            item.promptText.value = messages.getString(APPENDIX_PROMPT_KEY)
            // The entry holds a title line, which it does not know by itself, so the bin is told
            // what removing means here.
            item.deleteTooltip.value = messages.getString(REMOVE_TOOLTIP_KEY)
            // An entry is asked for its position only when the user acts, so an entry that moved
            // because a line above it was removed still reports where it sits now.
            item.text.addListener { _, _, newValue ->
                viewModel.setTitleAppendix(appendixItems.indexOf(item), newValue ?: "")
            }
            item.setOnDeleteAction { viewModel.removeTitleAppendix(appendixItems.indexOf(item)) }

            appendixItems += item
        }

        boxTitleAppendix.children.setAll(appendixItems)
    }

    private companion object {
        /** Number of characters a prompt of the manuscript may hold. */
        const val PROMPT_MAX_CHARACTERS: Long = 2000L

        /** Key of the hint shown in the empty title field inside the resource bundle. */
        const val TITLE_PROMPT_KEY: String = "component.bookEditor.title.prompt"

        /** Key of the hint shown in an empty title line inside the resource bundle. */
        const val APPENDIX_PROMPT_KEY: String = "component.bookEditor.titleAppendix.prompt"

        /** Key of the tooltip of the button removing a title line inside the resource bundle. */
        const val REMOVE_TOOLTIP_KEY: String = "component.bookEditor.titleAppendix.remove.tooltip"

        /** Key of the title of the remove question inside the resource bundle. */
        const val REMOVE_TITLE_KEY: String = "component.bookEditor.titleAppendix.remove.title"

        /** Key of the headline of the remove question inside the resource bundle. */
        const val REMOVE_HEADER_KEY: String = "component.bookEditor.titleAppendix.remove.header"

        /** Key of the text of the remove question inside the resource bundle. */
        const val REMOVE_CONTENT_KEY: String = "component.bookEditor.titleAppendix.remove.content"
    }
}

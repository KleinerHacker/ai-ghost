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
import javafx.fxml.FXML
import javafx.fxml.Initializable
import org.pcsoft.app.aighost.app.Messages
import org.pcsoft.app.aighost.app.ui.AiGhostDialog
import org.pcsoft.app.aighost.app.ui.component.base.AiPromptArea
import org.pcsoft.app.aighost.app.ui.component.base.AiTextField
import org.pcsoft.app.aighost.app.ui.component.base.AiTextFieldList
import java.net.URL
import java.text.MessageFormat
import java.util.ResourceBundle

/**
 * View of [BookEditor], holding the fields of the manuscript.
 *
 * The title lines are shown in an [AiTextFieldList], which is handed the list of the view model
 * itself: a line the user writes stands in that list afterwards, so the editor does not carry the
 * lines a second time. The list only reports that another line is wanted or that a line is to be
 * removed - both are decided by the view model.
 *
 * The view asks the question the view model wants answered before a title line is thrown away, and
 * shows it in the window the editor is part of.
 */
class BookEditorView : FxmlView<BookEditorViewModel>, Initializable {

    @FXML
    private lateinit var txtTitle: AiTextField

    @FXML
    private lateinit var lstTitleAppendix: AiTextFieldList

    @FXML
    private lateinit var txaContentPrompt: AiPromptArea

    @FXML
    private lateinit var txaStylePrompt: AiPromptArea

    @InjectViewModel
    private lateinit var viewModel: BookEditorViewModel

    private lateinit var messages: ResourceBundle

    override fun initialize(location: URL?, resources: ResourceBundle?) {
        // The wording of the list and the question are read outside of the FXML, so they need the
        // same bundle the FXML around them was resolved with.
        messages = resources ?: Messages.bundle

        txtTitle.text.bindBidirectional(viewModel.title)
        txtTitle.promptText.value = messages.getString(TITLE_PROMPT_KEY)

        txaContentPrompt.text.bindBidirectional(viewModel.contentPrompt)
        txaContentPrompt.maxCharacters.value = PROMPT_MAX_CHARACTERS
        txaStylePrompt.text.bindBidirectional(viewModel.stylePrompt)
        txaStylePrompt.maxCharacters.value = PROMPT_MAX_CHARACTERS

        // The list holds title lines, which it does not know by itself, so it is told here what an
        // empty list means and what adding and removing do.
        lstTitleAppendix.entries.set(viewModel.titleAppendix)
        lstTitleAppendix.promptText.value = messages.getString(APPENDIX_PROMPT_KEY)
        lstTitleAppendix.emptyText.value = messages.getString(APPENDIX_EMPTY_KEY)
        lstTitleAppendix.addTooltip.value = messages.getString(ADD_TOOLTIP_KEY)
        lstTitleAppendix.deleteTooltip.value = messages.getString(REMOVE_TOOLTIP_KEY)
        lstTitleAppendix.setOnAddEntry { event ->
            event.consume()
            viewModel.addTitleAppendix()
        }
        lstTitleAppendix.setOnDeleteEntry { event ->
            event.consume()
            viewModel.removeTitleAppendix(event.index)
        }

        viewModel.confirmRemove = { line ->
            AiGhostDialog.showWarningConfirm(
                messages.getString(REMOVE_TITLE_KEY),
                messages.getString(REMOVE_HEADER_KEY),
                MessageFormat.format(messages.getString(REMOVE_CONTENT_KEY), line),
                lstTitleAppendix.scene?.window
            )
        }
    }

    private companion object {
        /** Number of characters a prompt of the manuscript may hold. */
        const val PROMPT_MAX_CHARACTERS: Long = 2000L

        /** Key of the hint shown in the empty title field inside the resource bundle. */
        const val TITLE_PROMPT_KEY: String = "component.bookEditor.title.prompt"

        /** Key of the hint shown in an empty title line inside the resource bundle. */
        const val APPENDIX_PROMPT_KEY: String = "component.bookEditor.titleAppendix.prompt"

        /** Key of the text shown while no title line exists inside the resource bundle. */
        const val APPENDIX_EMPTY_KEY: String = "component.bookEditor.titleAppendix.empty"

        /** Key of the tooltip of the button adding a title line inside the resource bundle. */
        const val ADD_TOOLTIP_KEY: String = "component.bookEditor.titleAppendix.add.tooltip"

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

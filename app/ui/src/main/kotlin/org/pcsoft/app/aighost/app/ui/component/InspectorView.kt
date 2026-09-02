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
import javafx.beans.value.ObservableValue
import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.scene.Node
import javafx.scene.control.TextField
import javafx.scene.control.TitledPane
import javafx.scene.layout.VBox
import org.pcsoft.app.aighost.app.Messages
import org.pcsoft.app.aighost.app.ui.AiGhostDialog
import org.pcsoft.app.aighost.app.ui.component.base.AiPromptArea
import org.pcsoft.app.aighost.app.ui.component.base.AiTextField
import org.pcsoft.app.aighost.app.ui.component.base.AiTextFieldList
import java.net.URL
import java.text.MessageFormat
import java.util.ResourceBundle

/**
 * View of [Inspector], holding the two fixed sections "Book" and "Chapter".
 *
 * Each section is a [TitledPane] whose expanded state is bound bidirectionally to the view model, so
 * collapsing or expanding it is pure runtime state. Every section carries a fields box and one or more
 * empty state boxes stacked on top of each other in a [javafx.scene.layout.StackPane]; the view model
 * decides which one is visible by way of a boolean the view binds `visibleProperty` and `managedProperty`
 * to, so a hidden box neither shows nor reserves space.
 */
class InspectorView : FxmlView<InspectorViewModel>, Initializable {

    @FXML
    private lateinit var pnlBookSection: TitledPane

    @FXML
    private lateinit var boxBookEmpty: VBox

    @FXML
    private lateinit var boxBookFields: VBox

    @FXML
    private lateinit var txtBookTitle: AiTextField

    @FXML
    private lateinit var lstBookTitleAppendix: AiTextFieldList

    @FXML
    private lateinit var txtBookAuthor: TextField

    @FXML
    private lateinit var txtBookCopyright: TextField

    @FXML
    private lateinit var txaBookContentPrompt: AiPromptArea

    @FXML
    private lateinit var txaBookStylePrompt: AiPromptArea

    @FXML
    private lateinit var pnlChapterSection: TitledPane

    @FXML
    private lateinit var boxChapterEmpty: VBox

    @FXML
    private lateinit var boxChapterFields: VBox

    @FXML
    private lateinit var txtChapterName: AiTextField

    @FXML
    private lateinit var txaChapterContentPrompt: AiPromptArea

    @FXML
    private lateinit var txaChapterStylePrompt: AiPromptArea

    @FXML
    private lateinit var boxBlurbFields: VBox

    @FXML
    private lateinit var txaBlurbPrompt: AiPromptArea

    @FXML
    private lateinit var pnlDesignSection: TitledPane

    @FXML
    private lateinit var boxDesignEmpty: VBox

    @FXML
    private lateinit var boxDesignFields: VBox

    @FXML
    private lateinit var titleStyleEditor: StyleDataEditor

    @FXML
    private lateinit var chapterTitleStyleEditor: StyleDataEditor

    @FXML
    private lateinit var chapterTitleAppendixStyleEditor: StyleDataEditor

    @FXML
    private lateinit var bodyTextStyleEditor: StyleDataEditor

    @InjectViewModel
    private lateinit var viewModel: InspectorViewModel

    private lateinit var messages: ResourceBundle

    override fun initialize(location: URL?, resources: ResourceBundle?) {
        // The wording of the list and the question are read outside of the FXML, so they need the
        // same bundle the FXML around them was resolved with.
        messages = resources ?: Messages.bundle

        pnlBookSection.expandedProperty().bindBidirectional(viewModel.bookSectionExpanded)
        pnlChapterSection.expandedProperty().bindBidirectional(viewModel.chapterSectionExpanded)
        pnlDesignSection.expandedProperty().bindBidirectional(viewModel.designSectionExpanded)

        bindBookSection()
        bindChapterSection()
        bindDesignSection()
    }

    private fun bindBookSection() {
        showExactly(boxBookFields, viewModel.bookAvailable)
        showExactly(boxBookEmpty, viewModel.bookAvailable.not())

        txtBookTitle.text.bindBidirectional(viewModel.title)
        txtBookTitle.promptText.value = messages.getString(TITLE_PROMPT_KEY)

        txtBookAuthor.textProperty().bindBidirectional(viewModel.author)
        txtBookAuthor.promptText = messages.getString(AUTHOR_PROMPT_KEY)

        txtBookCopyright.textProperty().bindBidirectional(viewModel.copyright)
        txtBookCopyright.promptText = messages.getString(COPYRIGHT_PROMPT_KEY)

        txaBookContentPrompt.text.bindBidirectional(viewModel.contentPrompt)
        txaBookContentPrompt.maxCharacters.bind(viewModel.maxContentPromptCharacters)
        txaBookStylePrompt.text.bindBidirectional(viewModel.stylePrompt)
        txaBookStylePrompt.maxCharacters.bind(viewModel.maxStylePromptCharacters)

        // The list holds title lines, which it does not know by itself, so it is told here what an
        // empty list means and what adding and removing do.
        lstBookTitleAppendix.entries.set(viewModel.titleAppendix)
        lstBookTitleAppendix.promptText.value = messages.getString(APPENDIX_PROMPT_KEY)
        lstBookTitleAppendix.emptyText.value = messages.getString(APPENDIX_EMPTY_KEY)
        lstBookTitleAppendix.addTooltip.value = messages.getString(ADD_TOOLTIP_KEY)
        lstBookTitleAppendix.deleteTooltip.value = messages.getString(REMOVE_TOOLTIP_KEY)
        lstBookTitleAppendix.setOnAddEntry { event ->
            event.consume()
            viewModel.addTitleAppendix()
        }
        lstBookTitleAppendix.setOnDeleteEntry { event ->
            event.consume()
            viewModel.removeTitleAppendix(event.index)
        }

        viewModel.confirmRemoveTitleAppendix = { line ->
            AiGhostDialog.showWarningConfirm(
                messages.getString(REMOVE_TITLE_KEY),
                messages.getString(REMOVE_HEADER_KEY),
                MessageFormat.format(messages.getString(REMOVE_CONTENT_KEY), line),
                lstBookTitleAppendix.scene?.window
            )
        }
    }

    private fun bindChapterSection() {
        val isChapter = viewModel.chapterSelection.isEqualTo(InspectorViewModel.ChapterSelection.CHAPTER)
        val isBlurb = viewModel.chapterSelection.isEqualTo(InspectorViewModel.ChapterSelection.BLURB)
        val isNone = viewModel.chapterSelection.isEqualTo(InspectorViewModel.ChapterSelection.NONE)

        showExactly(boxChapterFields, isChapter)
        showExactly(boxBlurbFields, isBlurb)
        showExactly(boxChapterEmpty, isNone)

        txtChapterName.text.bindBidirectional(viewModel.chapterName)
        txtChapterName.promptText.value = messages.getString(CHAPTER_NAME_PROMPT_KEY)

        txaChapterContentPrompt.text.bindBidirectional(viewModel.chapterContentPrompt)
        txaChapterContentPrompt.maxCharacters.bind(viewModel.maxContentPromptCharacters)
        txaChapterStylePrompt.text.bindBidirectional(viewModel.chapterStylePrompt)
        txaChapterStylePrompt.maxCharacters.bind(viewModel.maxStylePromptCharacters)

        txaBlurbPrompt.text.bindBidirectional(viewModel.blurbPrompt)
        txaBlurbPrompt.maxCharacters.bind(viewModel.maxContentPromptCharacters)
    }

    private fun bindDesignSection() {
        showExactly(boxDesignFields, viewModel.designAvailable)
        showExactly(boxDesignEmpty, viewModel.designAvailable.not())

        // The section binds live to the design of whatever project is bound, not to the tree
        // selection, so the view model only needs to know the editors to forward that binding into.
        viewModel.titleStyleEditor = titleStyleEditor
        viewModel.chapterTitleStyleEditor = chapterTitleStyleEditor
        viewModel.chapterTitleAppendixStyleEditor = chapterTitleAppendixStyleEditor
        viewModel.bodyTextStyleEditor = bodyTextStyleEditor
    }

    /**
     * Ties a node's visibility and layout participation to the given condition, so a hidden box of a
     * section neither shows nor reserves space next to the one that replaces it.
     */
    private fun showExactly(node: Node, condition: ObservableValue<Boolean>) {
        node.visibleProperty().bind(condition)
        node.managedProperty().bind(condition)
    }

    private companion object {
        /** Key of the hint shown in the empty title field inside the resource bundle. */
        const val TITLE_PROMPT_KEY: String = "component.inspector.book.title.prompt"

        /** Key of the hint shown in the empty author field inside the resource bundle. */
        const val AUTHOR_PROMPT_KEY: String = "component.inspector.book.author.prompt"

        /** Key of the hint shown in the empty copyright field inside the resource bundle. */
        const val COPYRIGHT_PROMPT_KEY: String = "component.inspector.book.copyright.prompt"

        /** Key of the hint shown in an empty title line inside the resource bundle. */
        const val APPENDIX_PROMPT_KEY: String = "component.inspector.book.titleAppendix.prompt"

        /** Key of the text shown while no title line exists inside the resource bundle. */
        const val APPENDIX_EMPTY_KEY: String = "component.inspector.book.titleAppendix.empty"

        /** Key of the tooltip of the button adding a title line inside the resource bundle. */
        const val ADD_TOOLTIP_KEY: String = "component.inspector.book.titleAppendix.add.tooltip"

        /** Key of the tooltip of the button removing a title line inside the resource bundle. */
        const val REMOVE_TOOLTIP_KEY: String = "component.inspector.book.titleAppendix.remove.tooltip"

        /** Key of the title of the remove question inside the resource bundle. */
        const val REMOVE_TITLE_KEY: String = "component.inspector.book.titleAppendix.remove.title"

        /** Key of the headline of the remove question inside the resource bundle. */
        const val REMOVE_HEADER_KEY: String = "component.inspector.book.titleAppendix.remove.header"

        /** Key of the text of the remove question inside the resource bundle. */
        const val REMOVE_CONTENT_KEY: String = "component.inspector.book.titleAppendix.remove.content"

        /** Key of the hint shown in the empty chapter name field inside the resource bundle. */
        const val CHAPTER_NAME_PROMPT_KEY: String = "component.inspector.chapter.name.prompt"
    }
}

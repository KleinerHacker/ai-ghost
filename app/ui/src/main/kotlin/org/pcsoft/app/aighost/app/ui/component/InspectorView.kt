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
import javafx.scene.control.Button
import javafx.scene.control.TextField
import javafx.scene.control.TitledPane
import javafx.scene.layout.VBox
import org.pcsoft.app.aighost.app.Messages
import org.pcsoft.app.aighost.app.ui.component.base.AiPromptArea
import org.pcsoft.app.aighost.app.ui.component.base.AiTextField
import java.net.URL
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
    private lateinit var txtBookAuthor: TextField

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
    private lateinit var btnGenerateChapter: Button

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

        txtBookAuthor.textProperty().bindBidirectional(viewModel.author)
        txtBookAuthor.promptText = messages.getString(AUTHOR_PROMPT_KEY)

        txaBookContentPrompt.text.bindBidirectional(viewModel.contentPrompt)
        txaBookContentPrompt.maxCharacters.bind(viewModel.maxContentPromptCharacters)
        txaBookStylePrompt.text.bindBidirectional(viewModel.stylePrompt)
        txaBookStylePrompt.maxCharacters.bind(viewModel.maxStylePromptCharacters)
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

    /**
     * Bound to the "Generate chapter" button of the "Chapter" section.
     *
     * The actual generation belongs to the future plugin system feature; this method is intentionally
     * left unimplemented until that infrastructure exists.
     */
    fun generatePart() {
        TODO("AI action: generate-part")
    }

    private companion object {
        /** Key of the hint shown in the empty author field inside the resource bundle. */
        const val AUTHOR_PROMPT_KEY: String = "component.inspector.book.author.prompt"

        /** Key of the hint shown in the empty chapter name field inside the resource bundle. */
        const val CHAPTER_NAME_PROMPT_KEY: String = "component.inspector.chapter.name.prompt"
    }
}

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

import de.saxsys.mvvmfx.ViewModel
import javafx.beans.binding.Bindings
import javafx.beans.property.BooleanProperty
import javafx.beans.property.LongProperty
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.beans.property.StringProperty
import javafx.beans.value.ChangeListener
import javafx.beans.value.ObservableValue
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import org.pcsoft.app.aighost.app.controller.IoController
import org.pcsoft.app.aighost.fx.model.project.ProjectProperty
import org.pcsoft.app.aighost.fx.model.project.book.BookProperty
import org.pcsoft.app.aighost.fx.model.project.book.ChapterProperty

/**
 * View model of [Inspector].
 *
 * The open project is the input of the "Book" section and is handed over through [bindProject]; the
 * node the user picked in [ProjectList] is the input of the "Chapter" section and is handed over
 * through [bindSelection]. Both are taken as the model itself and not as the value of a property of
 * their own, so a property never carries another property.
 *
 * Both sections follow the same pattern: a field property is bound bidirectionally to the field of
 * the model it stands for and unbound again as soon as another object - or none at all - takes that
 * model's place.
 */
class InspectorViewModel : ViewModel {

    /** Whether the "Book" section is expanded, pure runtime state that is never persisted. */
    val bookSectionExpanded: BooleanProperty = SimpleBooleanProperty(this, "bookSectionExpanded", true)

    /** Whether the "Chapter" section is expanded, pure runtime state that is never persisted. */
    val chapterSectionExpanded: BooleanProperty = SimpleBooleanProperty(this, "chapterSectionExpanded", true)

    /** Whether the "Design" section is expanded, pure runtime state that is never persisted. */
    val designSectionExpanded: BooleanProperty = SimpleBooleanProperty(this, "designSectionExpanded", true)

    /** Whether a book is bound at all, so the "Book" section shows its fields instead of an empty state. */
    val bookAvailable: BooleanProperty = SimpleBooleanProperty(this, "bookAvailable", false)

    /** Whether a project is bound at all, so the "Design" section shows its fields instead of an empty state. */
    val designAvailable: BooleanProperty = SimpleBooleanProperty(this, "designAvailable", false)

    /** Main title of the manuscript, empty while no manuscript is bound. */
    val title: StringProperty = SimpleStringProperty(this, "title", "")

    /** Further title lines shown below the main title, in the order the user arranged them in. */
    val titleAppendix: ObservableList<String> = FXCollections.observableArrayList()

    /** Description of what the manuscript is about, empty while no manuscript is bound. */
    val contentPrompt: StringProperty = SimpleStringProperty(this, "contentPrompt", "")

    /** Description of the tone the manuscript is written in, empty while no manuscript is bound. */
    val stylePrompt: StringProperty = SimpleStringProperty(this, "stylePrompt", "")

    /** Author printed in the manuscript, empty while no project is bound. */
    val author: StringProperty = SimpleStringProperty(this, "author", "")

    /** Copyright notice printed in the manuscript, empty while no manuscript is bound. */
    val copyright: StringProperty = SimpleStringProperty(this, "copyright", "")

    /** Maximum number of characters allowed in the content prompt fields. */
    val maxContentPromptCharacters: LongProperty = IoController.preferences.aiProperty.maxStoryCharactersProperty

    /** Maximum number of characters allowed in the style prompt fields. */
    val maxStylePromptCharacters: LongProperty = IoController.preferences.aiProperty.maxStyleCharactersProperty

    /** Which kind of part the "Chapter" section currently shows, driving its empty state. */
    val chapterSelection: SimpleObjectProperty<ChapterSelection> =
        SimpleObjectProperty(this, "chapterSelection", ChapterSelection.NONE)

    /** Name of the chapter as shown in the project tree, empty while no chapter is bound. */
    val chapterName: StringProperty = SimpleStringProperty(this, "chapterName", "")

    /** Description of what the chapter is about, empty while no chapter is bound. */
    val chapterContentPrompt: StringProperty = SimpleStringProperty(this, "chapterContentPrompt", "")

    /** Description of the tone the chapter is written in, empty while no chapter is bound. */
    val chapterStylePrompt: StringProperty = SimpleStringProperty(this, "chapterStylePrompt", "")

    /** Prompt the blurb is generated from, empty while no blurb is bound. */
    val blurbPrompt: StringProperty = SimpleStringProperty(this, "blurbPrompt", "")

    /**
     * Embedded editor for the style of the book title, wired up by [InspectorView] once the FXML of
     * the "Design" section is loaded.
     */
    internal lateinit var titleStyleEditor: StyleDataEditor

    /** Embedded editor for the style of the chapter heading. */
    internal lateinit var chapterTitleStyleEditor: StyleDataEditor

    /** Embedded editor for the style of the further chapter heading lines. */
    internal lateinit var chapterTitleAppendixStyleEditor: StyleDataEditor

    /** Embedded editor for the style of the chapter body text. */
    internal lateinit var bodyTextStyleEditor: StyleDataEditor

    /**
     * Asks the user whether the title line holding the given text may be removed.
     *
     * Set by [InspectorView], which shows the question as a dialog. Answering with no, or not
     * answering at all, keeps the line.
     */
    internal var confirmRemoveTitleAppendix: ((String) -> Boolean)? = null

    // The project the "Book" section is bound to right now, so the bindings can be released again
    // when another project takes its place.
    private var boundProject: ProjectProperty? = null

    // The chapter picked out of the project tree, built through ChapterProperty.of, so it can be
    // unbound again as soon as another node is picked.
    private var boundChapter: ChapterProperty? = null

    // The book the blurb prompt is bound to right now, so it can be unbound again when another
    // project - or none at all - takes its place.
    private var boundBlurbBook: BookProperty? = null

    private val selectionListener =
        ChangeListener<ProjectListItem?> { _, _, newValue -> onSelectionChanged(newValue) }

    /**
     * Appends an empty title line, which the user fills in afterwards.
     */
    fun addTitleAppendix() {
        titleAppendix.add("")
    }

    /**
     * Removes the title line at the given position, after the user agreed to lose it.
     *
     * @param index position of the title line
     */
    fun removeTitleAppendix(index: Int) {
        if (index < 0 || index >= titleAppendix.size) return
        if (confirmRemoveTitleAppendix?.invoke(titleAppendix[index]) == false) return

        titleAppendix.removeAt(index)
    }

    /**
     * Binds the "Book" section to the given project and releases the one bound before.
     *
     * @param project the open project, `null` to follow none
     */
    internal fun bindProject(project: ProjectProperty?) {
        boundProject?.also { old ->
            title.unbindBidirectional(old.bookProperty.titleProperty)
            contentPrompt.unbindBidirectional(old.bookProperty.promptsProperty.contentPromptProperty)
            stylePrompt.unbindBidirectional(old.bookProperty.promptsProperty.stylePromptProperty)
            Bindings.unbindContentBidirectional(titleAppendix, old.bookProperty.titleAppendixProperty)
            author.unbindBidirectional(old.metaProperty.authorProperty)
            copyright.unbindBidirectional(old.bookProperty.copyrightProperty.copyrightProperty)
        }
        boundProject = project

        if (project == null) {
            bookAvailable.value = false
            title.value = ""
            contentPrompt.value = ""
            stylePrompt.value = ""
            titleAppendix.clear()
            author.value = ""
            copyright.value = ""
            unbindBlurb()
            bindDesign(null)
            return
        }

        bookAvailable.value = true

        // The project is the source of truth, so the fields take over its values instead of writing
        // their own into it: a bidirectional binding starts from the property it is called on.
        title.value = project.bookProperty.titleProperty.value
        contentPrompt.value = project.bookProperty.promptsProperty.contentPromptProperty.value
        stylePrompt.value = project.bookProperty.promptsProperty.stylePromptProperty.value
        author.value = project.metaProperty.authorProperty.value
        copyright.value = project.bookProperty.copyrightProperty.copyrightProperty.value

        title.bindBidirectional(project.bookProperty.titleProperty)
        contentPrompt.bindBidirectional(project.bookProperty.promptsProperty.contentPromptProperty)
        stylePrompt.bindBidirectional(project.bookProperty.promptsProperty.stylePromptProperty)
        Bindings.bindContentBidirectional(titleAppendix, project.bookProperty.titleAppendixProperty)
        author.bindBidirectional(project.metaProperty.authorProperty)
        copyright.bindBidirectional(project.bookProperty.copyrightProperty.copyrightProperty)

        // The blurb belongs to the book of this project, so it follows the project's lifetime; which
        // item of the tree is picked decides whether its prompt is shown.
        rebindBlurb(project)
        onSelectionChanged(lastSelection)

        bindDesign(project)
    }

    /**
     * Binds the "Design" section's four style editors live to the design of the given project, or
     * releases them while none is bound - unlike the "Book" and "Chapter" sections, this does not
     * depend on the tree selection.
     *
     * The four editor fields are only set once [InspectorView] wires them up after loading the FXML;
     * a plain unit test that builds this view model on its own therefore never sets them, so every
     * access below is guarded and simply skipped while an editor is not there yet.
     */
    private fun bindDesign(project: ProjectProperty?) {
        if (project == null) {
            designAvailable.value = false
            if (::titleStyleEditor.isInitialized) titleStyleEditor.release()
            if (::chapterTitleStyleEditor.isInitialized) chapterTitleStyleEditor.release()
            if (::chapterTitleAppendixStyleEditor.isInitialized) chapterTitleAppendixStyleEditor.release()
            if (::bodyTextStyleEditor.isInitialized) bodyTextStyleEditor.release()
            return
        }

        designAvailable.value = true
        if (::titleStyleEditor.isInitialized) {
            titleStyleEditor.bindStyle(project.designProperty.titlePageProperty.titleStyleProperty)
        }
        if (::chapterTitleStyleEditor.isInitialized) {
            chapterTitleStyleEditor.bindStyle(project.designProperty.chapterPageProperty.titleStyleProperty)
        }
        if (::chapterTitleAppendixStyleEditor.isInitialized) {
            chapterTitleAppendixStyleEditor.bindStyle(
                project.designProperty.chapterPageProperty.titleAppendixStyleProperty
            )
        }
        if (::bodyTextStyleEditor.isInitialized) {
            bodyTextStyleEditor.bindStyle(project.designProperty.chapterPageProperty.textStyleProperty)
        }
    }

    /**
     * Lets the "Chapter" section follow the picked node of the project tree.
     *
     * @param selection the selection reported by [ProjectList], read once and followed afterwards
     */
    internal fun bindSelection(selection: ObservableValue<ProjectListItem?>) {
        boundSelection?.removeListener(selectionListener)
        boundSelection = selection
        selection.addListener(selectionListener)

        onSelectionChanged(selection.value)
    }

    // The observable the "Chapter" section follows right now, so the listener can be moved to another
    // one without piling up.
    private var boundSelection: ObservableValue<ProjectListItem?>? = null

    // The last selection reported, re-applied whenever the project is exchanged so the section shows
    // the right state for whatever the tree still has selected.
    private var lastSelection: ProjectListItem? = null

    private fun onSelectionChanged(item: ProjectListItem?) {
        lastSelection = item

        boundChapter?.also { old ->
            chapterName.unbindBidirectional(old.nameProperty)
            chapterContentPrompt.unbindBidirectional(old.promptsProperty.contentPromptProperty)
            chapterStylePrompt.unbindBidirectional(old.promptsProperty.stylePromptProperty)
        }
        boundChapter = null
        boundBlurbBook?.also { book -> blurbPrompt.unbindBidirectional(book.blurbProperty.promptProperty) }

        when (item) {
            is ProjectListItem.ChapterItem -> {
                chapterSelection.value = ChapterSelection.CHAPTER
                val chapterProperty = ChapterProperty.of(item.chapter)
                boundChapter = chapterProperty

                chapterName.value = chapterProperty.nameProperty.value
                chapterContentPrompt.value = chapterProperty.promptsProperty.contentPromptProperty.value
                chapterStylePrompt.value = chapterProperty.promptsProperty.stylePromptProperty.value

                chapterName.bindBidirectional(chapterProperty.nameProperty)
                chapterContentPrompt.bindBidirectional(chapterProperty.promptsProperty.contentPromptProperty)
                chapterStylePrompt.bindBidirectional(chapterProperty.promptsProperty.stylePromptProperty)
            }

            is ProjectListItem.BlurbItem -> {
                if (item.blurb != null) {
                    chapterSelection.value = ChapterSelection.BLURB
                    boundBlurbBook?.also { book ->
                        blurbPrompt.value = book.blurbProperty.promptProperty.value
                        blurbPrompt.bindBidirectional(book.blurbProperty.promptProperty)
                    }
                } else {
                    chapterSelection.value = ChapterSelection.NONE
                    blurbPrompt.value = ""
                }
                chapterName.value = ""
                chapterContentPrompt.value = ""
                chapterStylePrompt.value = ""
            }

            else -> {
                chapterSelection.value = ChapterSelection.NONE
                chapterName.value = ""
                chapterContentPrompt.value = ""
                chapterStylePrompt.value = ""
                blurbPrompt.value = ""
            }
        }
    }

    /**
     * Rebinds the blurb prompt tracking to the blurb of the given project, releasing the one bound
     * before.
     */
    private fun rebindBlurb(project: ProjectProperty) {
        boundBlurbBook?.also { book -> blurbPrompt.unbindBidirectional(book.blurbProperty.promptProperty) }
        boundBlurbBook = project.bookProperty
    }

    /** Releases the blurb prompt tracking entirely, used while no project is bound. */
    private fun unbindBlurb() {
        boundBlurbBook?.also { book -> blurbPrompt.unbindBidirectional(book.blurbProperty.promptProperty) }
        boundBlurbBook = null
        blurbPrompt.value = ""
        chapterSelection.value = ChapterSelection.NONE
        chapterName.value = ""
        chapterContentPrompt.value = ""
        chapterStylePrompt.value = ""
    }

    /** Which kind of book part the "Chapter" section currently edits. */
    enum class ChapterSelection {
        /** Neither a chapter nor a blurb is picked, so the section shows its empty state. */
        NONE,

        /** A chapter is picked, so the section shows its name and its prompts. */
        CHAPTER,

        /** The blurb is picked and already created, so the section shows its prompt. */
        BLURB
    }
}

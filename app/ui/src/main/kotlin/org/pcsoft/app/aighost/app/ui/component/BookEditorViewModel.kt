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
import javafx.beans.property.ObjectProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.beans.property.StringProperty
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import org.pcsoft.app.aighost.fx.model.project.book.BookProperty

/**
 * View model of [BookEditor].
 *
 * [book] is the single input of the component: as long as it carries a manuscript, everything the
 * view shows is bound to the fields of that manuscript, so a change of the user reaches the model
 * object right away and a manuscript changed elsewhere reaches the view. An exchanged manuscript is
 * unbound from the old one and bound to the new one; without a manuscript the fields stand empty.
 *
 * Removing a title line throws written text away, so it is only carried out once [confirmRemove]
 * agreed to it. Asking is left to [BookEditorView], which owns the window the question is shown in.
 */
class BookEditorViewModel : ViewModel {

    /** The manuscript being edited, absent while no project is open. */
    val book: ObjectProperty<BookProperty?> = SimpleObjectProperty(this, "book", null)

    /** Main title of the manuscript, empty while no manuscript is bound. */
    val title: StringProperty = SimpleStringProperty(this, "title", "")

    /** Description of what the manuscript is about, empty while no manuscript is bound. */
    val contentPrompt: StringProperty = SimpleStringProperty(this, "contentPrompt", "")

    /** Description of the tone the manuscript is written in, empty while no manuscript is bound. */
    val stylePrompt: StringProperty = SimpleStringProperty(this, "stylePrompt", "")

    /** Further title lines shown below the main title, in the order the user arranged them in. */
    val titleAppendix: ObservableList<String> = FXCollections.observableArrayList()

    /**
     * Asks the user whether the title line holding the given text may be removed.
     *
     * Set by [BookEditorView], which shows the question as a dialog. Answering with no, or not
     * answering at all, keeps the line.
     */
    internal var confirmRemove: ((String) -> Boolean)? = null

    // The manuscript the properties are bound to right now, so the bindings can be released again
    // when another manuscript takes its place.
    private var boundBook: BookProperty? = null

    init {
        book.addListener { _, _, newValue -> bind(newValue) }
    }

    /**
     * Appends an empty title line, which the user fills in afterwards.
     */
    fun addTitleAppendix() {
        titleAppendix.add("")
    }

    /**
     * Writes the text of the title line at the given position.
     *
     * A position beyond the list and a text that is there already are dropped, so a field reporting
     * what it was just given does not travel through the model a second time.
     *
     * @param index position of the title line
     * @param value text the line carries
     */
    fun setTitleAppendix(index: Int, value: String) {
        if (index < 0 || index >= titleAppendix.size) return
        if (titleAppendix[index] == value) return

        titleAppendix[index] = value
    }

    /**
     * Removes the title line at the given position, after the user agreed to lose it.
     *
     * @param index position of the title line
     */
    fun removeTitleAppendix(index: Int) {
        if (index < 0 || index >= titleAppendix.size) return
        if (confirmRemove?.invoke(titleAppendix[index]) == false) return

        titleAppendix.removeAt(index)
    }

    /**
     * Binds every field to the given manuscript and releases the one bound before.
     *
     * @param newBook the manuscript to follow, `null` to follow none
     */
    private fun bind(newBook: BookProperty?) {
        boundBook?.also { old ->
            title.unbindBidirectional(old.titleProperty)
            contentPrompt.unbindBidirectional(old.contentPromptProperty)
            stylePrompt.unbindBidirectional(old.stylePromptProperty)
            Bindings.unbindContentBidirectional(titleAppendix, old.titleAppendixProperty)
        }
        boundBook = newBook

        if (newBook == null) {
            title.value = ""
            contentPrompt.value = ""
            stylePrompt.value = ""
            titleAppendix.clear()
            return
        }

        // The manuscript is the source of truth, so the fields take over its values instead of
        // writing their own into it: a bidirectional binding starts from the property it is called on.
        title.value = newBook.titleProperty.value
        contentPrompt.value = newBook.contentPromptProperty.value
        stylePrompt.value = newBook.stylePromptProperty.value

        title.bindBidirectional(newBook.titleProperty)
        contentPrompt.bindBidirectional(newBook.contentPromptProperty)
        stylePrompt.bindBidirectional(newBook.stylePromptProperty)
        Bindings.bindContentBidirectional(titleAppendix, newBook.titleAppendixProperty)
    }
}

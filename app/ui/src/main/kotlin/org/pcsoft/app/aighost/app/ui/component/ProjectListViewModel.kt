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
import javafx.beans.value.ChangeListener
import javafx.beans.property.ReadOnlyListProperty
import javafx.beans.property.ReadOnlyListWrapper
import javafx.beans.property.ReadOnlyObjectProperty
import javafx.beans.property.ReadOnlyObjectWrapper
import javafx.collections.FXCollections
import org.pcsoft.app.aighost.app.Messages
import org.pcsoft.app.aighost.fx.model.project.ProjectProperty
import org.pcsoft.app.aighost.fx.model.project.book.ChapterProperty
import org.pcsoft.app.aighost.layouting.model.common.toPageLayout
import org.pcsoft.app.aighost.layouting.model.common.toTextStyle
import org.pcsoft.app.aighost.layouting.model.project.book.BookPartBuilder
import org.pcsoft.app.aighost.model.project.book.Blurb
import org.pcsoft.app.aighost.model.project.book.Chapter
import org.pcsoft.app.aighost.model.project.book.Epilog
import org.pcsoft.app.aighost.model.project.Project
import org.pcsoft.app.aighost.model.project.book.Prolog
import org.pcsoft.framework.simplay.engine.model.Document
import org.pcsoft.framework.simplay.engine.model.FlowPage

/**
 * View model of [ProjectList].
 *
 * The property model of the project is the single input of the component and is handed over through
 * [bind]; it is taken as the model itself and not as the value of a property of its own, so a
 * property never carries another property. Everything the tree needs is derived from the project
 * inside it: a project always carries its book, so only the project itself can be absent, which is
 * the state before the user opened or created one. [selectedItem] is the single output and reports
 * which node the user picked.
 *
 * The tree follows the exchange of the project and not every write inside it: a rewritten chapter
 * title leaves the nodes where they are, so the user does not lose the selection while typing.
 *
 * [addChapter], [removeChapter] and [renameChapter] are the only way the tree itself changes the
 * manuscript (IP-38): adding or removing a chapter keeps [org.pcsoft.app.aighost.model.project.book.Book.chapters]
 * and the chapter's page in [org.pcsoft.app.aighost.model.project.book.Book.document] in lock step, so
 * neither ever names a page or an anchor the other does not know about. Whether removing a chapter
 * needs the user's confirmation first is a question of [ProjectListCell], which shows the tree - this
 * view model carries out the removal once it is asked to, unconditionally.
 */
class ProjectListViewModel : ViewModel {

    private val chaptersWrapper: ReadOnlyListWrapper<Chapter> =
        ReadOnlyListWrapper(this, "chapters", FXCollections.observableArrayList())

    private val prologWrapper: ReadOnlyObjectWrapper<Prolog?> = ReadOnlyObjectWrapper(this, "prolog", null)
    private val epilogWrapper: ReadOnlyObjectWrapper<Epilog?> = ReadOnlyObjectWrapper(this, "epilog", null)
    private val blurbWrapper: ReadOnlyObjectWrapper<Blurb?> = ReadOnlyObjectWrapper(this, "blurb", null)

    private val selectedItemWrapper: ReadOnlyObjectWrapper<ProjectListItem?> =
        ReadOnlyObjectWrapper(this, "selectedItem", null)

    // The model the tree follows right now and the listener it follows it with, so both can be
    // released again when another model takes its place.
    private var project: ProjectProperty? = null
    private val projectListener = ChangeListener<Project> { _, _, newValue -> onProjectChanged(newValue) }

    /** Chapters of the open project in the order the user arranged them, empty without a project. */
    val chapters: ReadOnlyListProperty<Chapter> get() = chaptersWrapper.readOnlyProperty

    /** Prolog of the open project, absent without a project or before the user created one. */
    val prolog: ReadOnlyObjectProperty<Prolog?> get() = prologWrapper.readOnlyProperty

    /** Epilog of the open project, absent without a project or before the user created one. */
    val epilog: ReadOnlyObjectProperty<Epilog?> get() = epilogWrapper.readOnlyProperty

    /** Blurb of the open project, absent without a project or before the user created one. */
    val blurb: ReadOnlyObjectProperty<Blurb?> get() = blurbWrapper.readOnlyProperty

    /** The node the user picked in the tree, absent while nothing is selected. */
    val selectedItem: ReadOnlyObjectProperty<ProjectListItem?> get() = selectedItemWrapper.readOnlyProperty

    /**
     * Lets the tree follow [project] and releases the model it followed before.
     *
     * @param project the project model of the surrounding window
     */
    internal fun bind(project: ProjectProperty) {
        this.project?.removeListener(projectListener)
        this.project = project
        project.addListener(projectListener)

        onProjectChanged(project.value)
    }

    /**
     * Takes over the node the user picked in the tree.
     *
     * Called by [ProjectListView] only; the outside world reads [selectedItem].
     *
     * @param item the picked node, `null` when the selection was cleared
     */
    internal fun select(item: ProjectListItem?) {
        selectedItemWrapper.value = item
    }

    /**
     * Adds a freshly named chapter at the end of the manuscript.
     *
     * The new chapter gets a fresh [Chapter.id] and, through [BookPartBuilder], a page in the book's
     * document that carries nothing but that id's anchor - the same seed a chapter gets the first time
     * its anchor is read at all.
     */
    fun addChapter() {
        val bookProperty = project?.bookProperty ?: return
        val design = project?.value?.design ?: return

        val chapter = Chapter(name = Messages["component.projectList.chapter.defaultName"])
        bookProperty.chaptersProperty.add(chapter)
        // chaptersWrapper is this view model's own copy for the tree, taken over once when the
        // project is bound - it follows a rebound project, not every write inside chaptersProperty.
        chaptersWrapper.add(chapter)

        val anchorId = chapter.id.toString()
        val document = bookProperty.document ?: Document()
        val style = design.chapterPage.textStyle.toTextStyle()
        val page = FlowPage(
            design.pageFormat.toPageLayout(),
            BookPartBuilder.build(document, anchorId, style),
            id = anchorId
        )
        bookProperty.document = document.copy(pages = document.pages + page)
    }

    /**
     * Removes [chapter] and its page from the manuscript.
     *
     * Carries the removal out unconditionally; asking the user first, since the chapter's text is
     * lost with it, is [ProjectListCell]'s job before this is ever called.
     *
     * @param chapter the chapter to remove, as shown by the tree node the user acted on
     */
    fun removeChapter(chapter: Chapter) {
        val bookProperty = project?.bookProperty ?: return

        bookProperty.chaptersProperty.remove(chapter)
        chaptersWrapper.remove(chapter)

        val document = bookProperty.document ?: return
        val anchorId = chapter.id.toString()
        bookProperty.document = document.copy(pages = document.pages.filterNot { it.id == anchorId })
    }

    /**
     * Renames [chapter] to [name].
     *
     * The chapter keeps its [Chapter.id] and therefore its anchor and its page in the document - only
     * the name shown in the tree changes.
     *
     * @param chapter the chapter to rename, as shown by the tree node the user acted on
     * @param name the new name, used as typed
     */
    fun renameChapter(chapter: Chapter, name: String) {
        val index = chapters.indexOf(chapter)
        if (index < 0) return

        ChapterProperty.of(chapter).name = name
        // The plain Chapter the property just wrote into is the same instance the list already holds,
        // so a re-set is enough to tell every listener - the tree cell included - to read it again.
        chaptersWrapper[index] = chapter
    }

    private fun onProjectChanged(project: Project?) {
        val book = project?.book

        chaptersWrapper.setAll(book?.chapters ?: emptyList())
        prologWrapper.value = book?.prolog
        epilogWrapper.value = book?.epilog
        blurbWrapper.value = book?.blurb

        // The previous selection points into the project that was just replaced, so keeping it would
        // report a chapter that is no longer part of the tree.
        selectedItemWrapper.value = null
    }
}

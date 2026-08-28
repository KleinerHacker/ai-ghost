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
import javafx.beans.property.ObjectProperty
import javafx.beans.property.ReadOnlyListProperty
import javafx.beans.property.ReadOnlyListWrapper
import javafx.beans.property.ReadOnlyObjectProperty
import javafx.beans.property.ReadOnlyObjectWrapper
import javafx.beans.property.SimpleObjectProperty
import javafx.collections.FXCollections
import org.pcsoft.app.aighost.model.project.book.Blurb
import org.pcsoft.app.aighost.model.project.book.Chapter
import org.pcsoft.app.aighost.model.project.book.Epilog
import org.pcsoft.app.aighost.model.project.Project
import org.pcsoft.app.aighost.model.project.book.Prolog

/**
 * View model of [ProjectList].
 *
 * [project] is the single input of the component and is bound by whoever shows it. Everything the
 * tree needs is derived from it: a project always carries its book, so only the project itself can
 * be absent, which is the state before the user opened or created one. [selectedItem] is the single
 * output and reports which node the user picked.
 */
class ProjectListViewModel : ViewModel {

    /** The project shown by the tree, absent while no project is open. */
    val project: ObjectProperty<Project?> = SimpleObjectProperty(this, "project", null)

    private val chaptersWrapper: ReadOnlyListWrapper<Chapter> =
        ReadOnlyListWrapper(this, "chapters", FXCollections.observableArrayList())

    private val prologWrapper: ReadOnlyObjectWrapper<Prolog?> = ReadOnlyObjectWrapper(this, "prolog", null)
    private val epilogWrapper: ReadOnlyObjectWrapper<Epilog?> = ReadOnlyObjectWrapper(this, "epilog", null)
    private val blurbWrapper: ReadOnlyObjectWrapper<Blurb?> = ReadOnlyObjectWrapper(this, "blurb", null)

    private val selectedItemWrapper: ReadOnlyObjectWrapper<ProjectListItem?> =
        ReadOnlyObjectWrapper(this, "selectedItem", null)

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

    init {
        project.addListener { _, _, new -> onProjectChanged(new) }
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

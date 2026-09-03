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

import de.saxsys.mvvmfx.FluentViewLoader
import javafx.beans.value.ObservableValue
import javafx.scene.layout.BorderPane
import org.pcsoft.app.aighost.app.undo.UndoStack
import org.pcsoft.app.aighost.fx.model.project.ProjectProperty

/**
 * The sheet in the centre of the editor, where a book part is written in the typography and the page
 * structure of the finished book.
 *
 * The component owns no data of its own: [bindProject] hands it the property model of the project,
 * [bindSelection] the node picked in the project tree and [bindUndoStack] the undo history the text
 * changes are recorded into. Prolog, chapter and epilog are edited through one flow; the title page
 * and the copyright page are shown read only; the blurb is a flow without a heading.
 */
class BookPartEditor : BorderPane() {

    private val viewModel: BookPartEditorViewModel

    init {
        FluentViewLoader.fxmlView(BookPartEditorView::class.java).let {
            it.root(this)
            it.load().let { loaded ->
                viewModel = loaded.viewModel
            }
        }
    }

    /**
     * Hands the property model of the project to the sheet.
     *
     * @param project the project model of the surrounding window, `null` to follow none
     */
    fun bindProject(project: ProjectProperty?) = viewModel.bindProject(project)

    /**
     * Lets the sheet follow the node picked in the project tree.
     *
     * @param selection the selection reported by [ProjectList]
     */
    fun bindSelection(selection: ObservableValue<ProjectListItem?>) = viewModel.bindSelection(selection)

    /**
     * Hands the undo history of the open project to the sheet.
     *
     * @param undoStack the one undo history of the surrounding window
     */
    fun bindUndoStack(undoStack: UndoStack) = viewModel.bindUndoStack(undoStack)

    /** Releases every binding of the component, used while it leaves the screen for good. */
    fun release() = viewModel.release()
}

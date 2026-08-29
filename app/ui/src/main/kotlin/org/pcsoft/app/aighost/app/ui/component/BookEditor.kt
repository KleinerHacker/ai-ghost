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
import javafx.scene.layout.BorderPane
import org.pcsoft.app.aighost.fx.model.project.book.BookProperty

/**
 * Editing surface for the manuscript itself: its title, the title lines below it and the two prompts
 * it is written from.
 *
 * The title and every title line are written into an [org.pcsoft.app.aighost.app.ui.component.base.AiTextField], so the AI may write them instead
 * of the user; the title lines grow and shrink with the buttons next to and below them. The prompts
 * are written into an [org.pcsoft.app.aighost.app.ui.component.base.AiPromptArea] each.
 *
 * The component owns no data of its own: [bindBook] hands it the property model of the manuscript,
 * which is the only way in and out, so everything the user writes lands in that manuscript right away.
 */
class BookEditor : BorderPane() {

    private val viewModel: BookEditorViewModel

    init {
        FluentViewLoader.fxmlView(BookEditorView::class.java).let {
            it.root(this)
            it.load().let { tuple ->
                viewModel = tuple.viewModel
            }
        }
    }

    /**
     * Hands the property model of the manuscript to the editor.
     *
     * The model is taken as it is and not as the value of a property of this component, so a
     * property never carries another property.
     *
     * @param book the manuscript to edit, `null` to edit none
     */
    fun bindBook(book: BookProperty?) = viewModel.bind(book)
}

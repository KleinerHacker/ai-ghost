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
import javafx.beans.binding.BooleanExpression
import javafx.scene.layout.BorderPane
import org.pcsoft.app.aighost.fx.model.common.StyleDataProperty

/**
 * Reusable group of three [StyleDataEditor] instances editing the styles a written book part
 * carries: its title, the further title lines below it and its body text.
 *
 * The component owns no data of its own. [bindStyles] hands it the three style properties of the
 * working copy a project settings section keeps; every embedded editor follows its style and writes
 * straight back into it. [valid] reports whether every embedded editor holds input that can be
 * stored, so the section can lock its buttons.
 */
class BookPartPageDesignSettings : BorderPane() {

    private val viewModel: BookPartPageDesignSettingsViewModel

    init {
        FluentViewLoader.fxmlView(BookPartPageDesignSettingsView::class.java).let {
            it.root(this)
            it.load().let { loaded ->
                viewModel = loaded.viewModel
            }
        }
    }

    /**
     * Hands the three style properties of the working copy to the embedded editors.
     *
     * @param titleStyle the style property of the title
     * @param titleAppendixStyle the style property of the further title lines
     * @param textStyle the style property of the body text
     */
    fun bindStyles(titleStyle: StyleDataProperty, titleAppendixStyle: StyleDataProperty, textStyle: StyleDataProperty) =
        viewModel.bind(titleStyle, titleAppendixStyle, textStyle)

    /** Whether the current input of every embedded editor can be stored. */
    val valid: BooleanExpression get() = viewModel.valid
}

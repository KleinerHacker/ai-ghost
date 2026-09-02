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
 * Reusable editor for the appearance of a piece of text: font family, size, weight and slant, the
 * horizontal placement and the space between the lines.
 *
 * The component owns no data of its own. [bindStyle] hands it the style property of the working copy
 * a design section keeps; every field follows that model and writes straight back into it. [valid]
 * reports whether the current input can be stored, so the section can lock its buttons.
 */
class StyleDataEditor : BorderPane() {

    private val viewModel: StyleDataEditorViewModel

    init {
        FluentViewLoader.fxmlView(StyleDataEditorView::class.java).let {
            it.root(this)
            it.load().let { loaded ->
                viewModel = loaded.viewModel
            }
        }
    }

    /**
     * Hands the style property of the working copy to the editor.
     *
     * @param style the style property to edit
     */
    fun bindStyle(style: StyleDataProperty) = viewModel.bind(style)

    /** Drops the binding to the style property handed to [bindStyle], if any. */
    fun release() = viewModel.release()

    /** Whether the current input can be stored. */
    val valid: BooleanExpression get() = viewModel.valid
}

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
import org.pcsoft.app.aighost.fx.model.project.design.DesignProperty

/**
 * The "General" section of the project settings: the page geometry and the two blank pages that
 * frame the book.
 *
 * The component owns no data of its own. [bindDesign] hands it the design property of the working
 * copy the dialog keeps; every field follows that model and writes straight back into it. [valid]
 * reports whether the current input can be stored, so the dialog can lock its buttons.
 */
class GeneralSettings : BorderPane() {

    private val viewModel: GeneralSettingsViewModel

    init {
        FluentViewLoader.fxmlView(GeneralSettingsView::class.java).let {
            it.root(this)
            it.load().let { loaded ->
                viewModel = loaded.viewModel
            }
        }
    }

    /**
     * Hands the design property of the working copy to the section.
     *
     * @param design the design property to edit
     */
    fun bindDesign(design: DesignProperty) = viewModel.bind(design)

    /** Whether the current input can be stored. */
    val valid: BooleanExpression get() = viewModel.valid
}

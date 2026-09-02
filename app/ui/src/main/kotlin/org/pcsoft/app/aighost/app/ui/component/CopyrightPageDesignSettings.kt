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
import org.pcsoft.app.aighost.fx.model.project.design.CopyrightPageDesignProperty

/**
 * Die "Copyright-Seite"-Design-Sektion: Erscheinungsbild von Copyright-Text, Copyright-Zusatz und
 * optional des Autors.
 *
 * The component owns no data of its own. [bindDesign] hands it the [CopyrightPageDesignProperty] of
 * the working copy a project settings section keeps; every embedded editor and the switch follow it
 * and write straight back into it. The author style editor stays reachable but disabled while the
 * switch is off, so the style is not lost - the "greyed out but stays writable" principle for
 * optional parts. [valid] reports whether every embedded editor holds input that can be stored, so
 * the section can lock its buttons.
 */
class CopyrightPageDesignSettings : BorderPane() {

    private val view: CopyrightPageDesignSettingsView
    private val viewModel: CopyrightPageDesignSettingsViewModel

    init {
        FluentViewLoader.fxmlView(CopyrightPageDesignSettingsView::class.java).let {
            it.root(this)
            it.load().let { loaded ->
                view = loaded.codeBehind
                viewModel = loaded.viewModel
            }
        }
    }

    /**
     * Hands the design property of the working copy to the embedded editors and the switch.
     *
     * @param design the copyright page design property of the working copy
     */
    fun bindDesign(design: CopyrightPageDesignProperty) {
        viewModel.bind(design)
        view.bindEditors(design)
    }

    /** Whether the current input of every embedded editor can be stored. */
    val valid: BooleanExpression get() = view.valid
}

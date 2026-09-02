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

import de.saxsys.mvvmfx.FxmlView
import de.saxsys.mvvmfx.InjectViewModel
import javafx.beans.binding.Bindings
import javafx.beans.binding.BooleanExpression
import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.scene.control.CheckBox
import org.pcsoft.app.aighost.fx.model.project.design.TitlePageDesignProperty
import java.net.URL
import java.util.ResourceBundle

/**
 * View of [TitlePageDesignSettings].
 *
 * The three embedded [StyleDataEditor] instances are addressed only through their public API -
 * [StyleDataEditor.bindStyle] and [StyleDataEditor.valid] - the same way [TitlePageDesignSettings]
 * itself is meant to be used from outside; this view never reaches into their private view model.
 * [bindEditors] forwards the three style properties of [TitlePageDesignProperty] straight to the
 * matching editor, [valid] links their validity with a logical AND. The "show author" check box binds
 * to [TitlePageDesignSettingsViewModel.showAuthor] and, in turn, disables the author style editor while
 * it is off - the editor stays reachable and keeps writing to the model, it is only greyed out.
 */
class TitlePageDesignSettingsView : FxmlView<TitlePageDesignSettingsViewModel>, Initializable {

    @FXML
    private lateinit var titleEditor: StyleDataEditor

    @FXML
    private lateinit var titleAppendixEditor: StyleDataEditor

    @FXML
    private lateinit var chkShowAuthor: CheckBox

    @FXML
    private lateinit var authorEditor: StyleDataEditor

    @InjectViewModel
    private lateinit var viewModel: TitlePageDesignSettingsViewModel

    override fun initialize(location: URL?, resources: ResourceBundle?) {
        chkShowAuthor.selectedProperty().bindBidirectional(viewModel.showAuthor)
        authorEditor.disableProperty().bind(viewModel.showAuthor.not())
    }

    /**
     * Forwards the three style properties of [design] to the matching embedded editor.
     *
     * @param design the title page design property of the working copy
     */
    fun bindEditors(design: TitlePageDesignProperty) {
        titleEditor.bindStyle(design.titleStyleProperty)
        titleAppendixEditor.bindStyle(design.titleAppendixStyleProperty)
        authorEditor.bindStyle(design.authorStyleProperty)
    }

    /** Whether the current input of every embedded editor can be stored. */
    val valid: BooleanExpression
        get() = Bindings.and(titleEditor.valid, titleAppendixEditor.valid).and(authorEditor.valid)
}

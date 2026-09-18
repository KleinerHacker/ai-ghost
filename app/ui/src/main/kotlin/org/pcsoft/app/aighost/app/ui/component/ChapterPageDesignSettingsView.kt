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
import org.pcsoft.app.aighost.fx.model.project.design.ChapterPageDesignProperty
import java.net.URL
import java.util.ResourceBundle

/**
 * View of [ChapterPageDesignSettings].
 *
 * The three style editors are plain [StyleDataEditor] instances declared in the FXML; [bindEditors]
 * hands the three nested style properties of a [ChapterPageDesignProperty] to them, called by
 * [ChapterPageDesignSettings.bindDesign] alongside [ChapterPageDesignSettingsViewModel.bind]. [valid]
 * is the conjunction of the three editors' own `valid` expressions, built once here since the editors
 * are only known to the view.
 */
class ChapterPageDesignSettingsView : FxmlView<ChapterPageDesignSettingsViewModel>, Initializable {

    @FXML
    private lateinit var editorTitle: StyleDataEditor

    @FXML
    private lateinit var editorTitleAppendix: StyleDataEditor

    @FXML
    private lateinit var editorText: StyleDataEditor

    @FXML
    private lateinit var chkTitleOnSeparatePage: CheckBox

    @InjectViewModel
    private lateinit var viewModel: ChapterPageDesignSettingsViewModel

    /** Whether the current input can be stored. */
    lateinit var valid: BooleanExpression
        private set

    override fun initialize(location: URL?, resources: ResourceBundle?) {
        chkTitleOnSeparatePage.selectedProperty().bindBidirectional(viewModel.titleOnSeparatePage)

        valid = Bindings.createBooleanBinding(
            { editorTitle.valid.get() && editorTitleAppendix.valid.get() && editorText.valid.get() },
            editorTitle.valid, editorTitleAppendix.valid, editorText.valid
        )
    }

    /**
     * Hands the three nested style properties of [design] to the three style editors.
     *
     * @param design the design property of the working copy
     */
    fun bindEditors(design: ChapterPageDesignProperty) {
        editorTitle.bindStyle(design.titleStyleProperty)
        editorTitleAppendix.bindStyle(design.titleAppendixStyleProperty)
        editorText.bindStyle(design.textStyleProperty)
    }
}

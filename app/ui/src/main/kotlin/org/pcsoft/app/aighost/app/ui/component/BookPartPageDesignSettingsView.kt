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
import javafx.fxml.FXML
import javafx.fxml.Initializable
import java.net.URL
import java.util.ResourceBundle

/**
 * View of [BookPartPageDesignSettings].
 *
 * The three embedded [StyleDataEditor] instances build their own view model on load already; this
 * view merely hands each control over to [BookPartPageDesignSettingsViewModel], so
 * [BookPartPageDesignSettingsViewModel.bind] can forward the style properties through the editors'
 * public [StyleDataEditor.bindStyle] without owning any state of its own.
 */
class BookPartPageDesignSettingsView : FxmlView<BookPartPageDesignSettingsViewModel>, Initializable {

    @FXML
    private lateinit var titleEditor: StyleDataEditor

    @FXML
    private lateinit var titleAppendixEditor: StyleDataEditor

    @FXML
    private lateinit var textEditor: StyleDataEditor

    @InjectViewModel
    private lateinit var viewModel: BookPartPageDesignSettingsViewModel

    override fun initialize(location: URL?, resources: ResourceBundle?) {
        viewModel.titleEditor = titleEditor
        viewModel.titleAppendixEditor = titleAppendixEditor
        viewModel.textEditor = textEditor
    }
}

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
import javafx.scene.Node
import javafx.scene.control.Label
import org.pcsoft.app.aighost.layouting.fx.paper.PaperFlowView
import java.net.URL
import java.util.ResourceBundle

/**
 * View of [BookPartEditor].
 *
 * The FXML holds the [PaperFlowView] and an empty-state label stacked on top of each other; exactly
 * one of them is shown, decided by whether a writable or readable part is picked. The view hands the
 * flow view to the view model once and lets the view model do the rest.
 */
class BookPartEditorView : FxmlView<BookPartEditorViewModel>, Initializable {

    @FXML
    private lateinit var paper: PaperFlowView

    @FXML
    private lateinit var lblEmpty: Label

    @InjectViewModel
    private lateinit var viewModel: BookPartEditorViewModel

    override fun initialize(location: URL?, resources: ResourceBundle?) {
        viewModel.attach(paper)

        showExactly(paper, viewModel.contentAvailable.get())
        showExactly(lblEmpty, !viewModel.contentAvailable.get())
        viewModel.contentAvailable.addListener { _, _, available ->
            showExactly(paper, available)
            showExactly(lblEmpty, !available)
        }
    }

    private fun showExactly(node: Node, visible: Boolean) {
        node.isVisible = visible
        node.isManaged = visible
    }
}

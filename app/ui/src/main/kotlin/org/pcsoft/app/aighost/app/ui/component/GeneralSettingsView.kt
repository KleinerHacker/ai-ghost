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
import javafx.beans.value.ObservableValue
import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.scene.Node
import javafx.scene.control.CheckBox
import javafx.scene.control.ComboBox
import javafx.scene.control.Label
import javafx.scene.control.TextField
import javafx.util.StringConverter
import org.pcsoft.app.aighost.app.Messages
import java.net.URL
import java.util.ResourceBundle

/**
 * View of [GeneralSettings].
 *
 * The controls are bound to the view model in both directions: the page size preset, the six
 * millimetre fields and the two check boxes. A preset other than "custom" sizes the page and locks
 * the width and height fields. A field that carries an impossible value is marked, and the matching
 * error line below the group is shown.
 */
class GeneralSettingsView : FxmlView<GeneralSettingsViewModel>, Initializable {

    private companion object {
        /** Style class put on a field whose value cannot be stored. */
        const val FIELD_ERROR: String = "field-error"
    }

    @FXML
    private lateinit var cmbPreset: ComboBox<PagePreset>

    @FXML
    private lateinit var txtWidth: TextField

    @FXML
    private lateinit var txtHeight: TextField

    @FXML
    private lateinit var txtInner: TextField

    @FXML
    private lateinit var txtOuter: TextField

    @FXML
    private lateinit var txtTop: TextField

    @FXML
    private lateinit var txtBottom: TextField

    @FXML
    private lateinit var lblSizeError: Label

    @FXML
    private lateinit var lblMarginError: Label

    @FXML
    private lateinit var chkStart: CheckBox

    @FXML
    private lateinit var chkEnd: CheckBox

    @InjectViewModel
    private lateinit var viewModel: GeneralSettingsViewModel

    override fun initialize(location: URL?, resources: ResourceBundle?) {
        val messages = resources ?: Messages.bundle

        cmbPreset.items.setAll(PagePreset.entries)
        cmbPreset.converter = object : StringConverter<PagePreset>() {
            override fun toString(preset: PagePreset?): String =
                preset?.let { messages.getString(it.bundleKey) } ?: ""

            override fun fromString(string: String?): PagePreset? = null
        }
        cmbPreset.valueProperty().bindBidirectional(viewModel.preset)

        txtWidth.textProperty().bindBidirectional(viewModel.widthMm)
        txtHeight.textProperty().bindBidirectional(viewModel.heightMm)
        txtInner.textProperty().bindBidirectional(viewModel.innerMm)
        txtOuter.textProperty().bindBidirectional(viewModel.outerMm)
        txtTop.textProperty().bindBidirectional(viewModel.topMm)
        txtBottom.textProperty().bindBidirectional(viewModel.bottomMm)

        txtWidth.disableProperty().bind(viewModel.customSize.not())
        txtHeight.disableProperty().bind(viewModel.customSize.not())

        chkStart.selectedProperty().bindBidirectional(viewModel.startWithEmptyPage)
        chkEnd.selectedProperty().bindBidirectional(viewModel.endWithEmptyPage)

        bindError(lblSizeError, viewModel.sizeError, txtWidth, txtHeight)
        bindError(lblMarginError, viewModel.marginError, txtInner, txtOuter, txtTop, txtBottom)
    }

    /**
     * Shows [label] and marks [fields] while [error] is set, and hides the label and clears the marks
     * again once the value can be stored.
     */
    private fun bindError(label: Label, error: ObservableValue<Boolean>, vararg fields: Node) {
        label.visibleProperty().bind(error)
        label.managedProperty().bind(error)
        markError(fields, error.value == true)
        error.addListener { _, _, on -> markError(fields, on == true) }
    }

    private fun markError(fields: Array<out Node>, on: Boolean) {
        for (field in fields) {
            field.styleClass.remove(FIELD_ERROR)
            if (on) field.styleClass.add(FIELD_ERROR)
        }
    }
}

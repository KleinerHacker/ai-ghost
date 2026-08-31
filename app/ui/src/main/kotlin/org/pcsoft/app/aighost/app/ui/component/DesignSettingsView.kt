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
import javafx.beans.property.StringProperty
import javafx.beans.value.ObservableValue
import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.scene.Node
import javafx.scene.control.CheckBox
import javafx.scene.control.ComboBox
import javafx.scene.control.Label
import javafx.scene.control.Spinner
import javafx.scene.control.SpinnerValueFactory.DoubleSpinnerValueFactory
import javafx.scene.control.SpinnerValueFactory.IntegerSpinnerValueFactory
import javafx.util.StringConverter
import org.pcsoft.app.aighost.app.Messages
import java.math.RoundingMode
import java.net.URL
import java.util.ResourceBundle

/**
 * View of [DesignSettings].
 *
 * The controls are bound to the view model in both directions: the page size preset, the six
 * millimetre spinners and the two check boxes. A preset other than "custom" sizes the page and locks
 * the width and height spinners. A margin spinner never climbs past a third of the page measure it
 * belongs to, and that ceiling follows the width and height as they change. A width or height that
 * cannot be stored is marked, and the error line below the size group is shown.
 */
class DesignSettingsView : FxmlView<DesignSettingsViewModel>, Initializable {

    private companion object {
        /** Style class put on a spinner whose value cannot be stored. */
        const val FIELD_ERROR: String = "field-error"

        /** Upper bound of a spinner while its ceiling cannot be derived from a page measure. */
        const val NO_LIMIT: Double = 9999.0

        /** Share of a page measure a single margin is allowed to take. */
        const val MARGIN_LIMIT_SHARE: Double = 1.0 / 3.0
    }

    @FXML
    private lateinit var cmbPreset: ComboBox<PagePreset>

    @FXML
    private lateinit var spnWidth: Spinner<Int>

    @FXML
    private lateinit var spnHeight: Spinner<Int>

    @FXML
    private lateinit var spnInner: Spinner<Double>

    @FXML
    private lateinit var spnOuter: Spinner<Double>

    @FXML
    private lateinit var spnTop: Spinner<Double>

    @FXML
    private lateinit var spnBottom: Spinner<Double>

    @FXML
    private lateinit var lblSizeError: Label

    @FXML
    private lateinit var chkStart: CheckBox

    @FXML
    private lateinit var chkEnd: CheckBox

    @InjectViewModel
    private lateinit var viewModel: DesignSettingsViewModel

    override fun initialize(location: URL?, resources: ResourceBundle?) {
        val messages = resources ?: Messages.bundle

        cmbPreset.items.setAll(PagePreset.entries)
        cmbPreset.converter = object : StringConverter<PagePreset>() {
            override fun toString(preset: PagePreset?): String =
                preset?.let { messages.getString(it.bundleKey) } ?: ""

            override fun fromString(string: String?): PagePreset? = null
        }
        cmbPreset.valueProperty().bindBidirectional(viewModel.preset)

        bindInt(spnWidth, viewModel.widthMm)
        bindInt(spnHeight, viewModel.heightMm)
        bindDouble(spnInner, viewModel.innerMm, marginCeiling(viewModel.widthMm))
        bindDouble(spnOuter, viewModel.outerMm, marginCeiling(viewModel.widthMm))
        bindDouble(spnTop, viewModel.topMm, marginCeiling(viewModel.heightMm))
        bindDouble(spnBottom, viewModel.bottomMm, marginCeiling(viewModel.heightMm))

        spnWidth.disableProperty().bind(viewModel.customSize.not())
        spnHeight.disableProperty().bind(viewModel.customSize.not())

        chkStart.selectedProperty().bindBidirectional(viewModel.startWithEmptyPage)
        chkEnd.selectedProperty().bindBidirectional(viewModel.endWithEmptyPage)

        bindError(lblSizeError, viewModel.sizeError, spnWidth, spnHeight)
    }

    /**
     * Wires an integer spinner to [text]: the editor mirrors the millimetre text of the view model,
     * and the step buttons move the same text through the spinner value.
     */
    private fun bindInt(spinner: Spinner<Int>, text: StringProperty) {
        val factory = IntegerSpinnerValueFactory(0, NO_LIMIT.toInt())
        factory.converter = INT_CONVERTER
        factory.value = null
        spinner.valueFactory = factory
        spinner.editor.textProperty().bindBidirectional(text)
        Bindings.bindBidirectional(spinner.editor.textProperty(), factory.valueProperty(), INT_CONVERTER)
    }

    /**
     * Wires a millimetre spinner to [text] the same way as [bindInt], and binds its upper bound to
     * [ceiling] so the margin can never take more than its share of the page.
     */
    private fun bindDouble(spinner: Spinner<Double>, text: StringProperty, ceiling: ObservableValue<Number>) {
        val factory = DoubleSpinnerValueFactory(0.0, NO_LIMIT, 0.0, 1.0)
        factory.converter = MM_CONVERTER
        factory.value = null
        factory.maxProperty().bind(ceiling)
        spinner.valueFactory = factory
        spinner.editor.textProperty().bindBidirectional(text)
        Bindings.bindBidirectional(spinner.editor.textProperty(), factory.valueProperty(), MM_CONVERTER)
    }

    /** A third of the page measure held as millimetre text in [measure], or [NO_LIMIT] while it is unset. */
    private fun marginCeiling(measure: ObservableValue<String>): ObservableValue<Number> =
        Bindings.createDoubleBinding(
            { (mm(measure.value)?.times(MARGIN_LIMIT_SHARE)) ?: NO_LIMIT }, measure
        )

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

/** Reads a millimetre text the way the view model does, ',' accepted for '.'. */
private fun mm(text: String?): Double? =
    text?.trim()?.replace(',', '.')?.toDoubleOrNull()?.takeIf { it.isFinite() }

/** Writes a millimetre value with at most two decimals and no trailing zeros, like the view model. */
private fun mmText(value: Double): String {
    val plain = value.toBigDecimal().setScale(2, RoundingMode.HALF_UP).toPlainString()
    return if ('.' in plain) plain.trimEnd('0').trimEnd('.') else plain
}

/** Null-tolerant converter between the editor text and the whole-millimetre spinner value. */
private val INT_CONVERTER: StringConverter<Int> = object : StringConverter<Int>() {
    override fun toString(value: Int?): String = value?.toString() ?: ""
    override fun fromString(string: String?): Int? = mm(string)?.toInt()
}

/** Null-tolerant converter between the editor text and the millimetre spinner value. */
private val MM_CONVERTER: StringConverter<Double> = object : StringConverter<Double>() {
    override fun toString(value: Double?): String = value?.let(::mmText) ?: ""
    override fun fromString(string: String?): Double? = mm(string)
}

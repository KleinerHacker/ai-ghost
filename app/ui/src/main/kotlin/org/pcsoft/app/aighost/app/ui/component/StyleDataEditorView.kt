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
import javafx.beans.property.DoubleProperty
import javafx.beans.property.IntegerProperty
import javafx.beans.value.ChangeListener
import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.scene.control.ComboBox
import javafx.scene.control.Label
import javafx.scene.control.Spinner
import javafx.scene.control.SpinnerValueFactory.DoubleSpinnerValueFactory
import javafx.scene.control.SpinnerValueFactory.IntegerSpinnerValueFactory
import javafx.scene.control.ToggleButton
import javafx.util.StringConverter
import org.pcsoft.app.aighost.app.Messages
import org.pcsoft.app.aighost.layouting.fx.font.FontCatalog
import org.pcsoft.app.aighost.model.common.Alignment
import java.net.URL
import java.util.ResourceBundle

/**
 * View of [StyleDataEditor].
 *
 * The family field is an editable combo box, so a family stored on another machine but not installed
 * here can still be shown; [FIELD_WARNING] then marks the warning label next to it, following
 * [StyleDataEditorViewModel.familyNotInstalled]. The size and the line spacing are spinners whose
 * editor text is bound to the value factory the same way `DesignSettingsView` binds a millimetre
 * spinner - through `Bindings.bindBidirectional` with a blank-tolerant [StringConverter] - so a
 * cleared or unparsable field commits as soon as it is typed, not only once the field loses focus; a
 * blank field converts to zero, which fails [StyleDataEditorViewModel.valid] right away. A second pair
 * of change listeners, guarded by [updating], mirrors the factory's boxed value onto the model's plain
 * `IntegerProperty`/`DoubleProperty`, since a type `Bindings.bindBidirectional` cannot bridge the two
 * directly because of generics.
 */
class StyleDataEditorView : FxmlView<StyleDataEditorViewModel>, Initializable {

    private companion object {
        /** Style class put on the warning label while the current family is not installed. */
        const val FIELD_WARNING: String = "field-warning"
    }

    @FXML
    private lateinit var cmbFamily: ComboBox<String>

    @FXML
    private lateinit var lblFamilyWarning: Label

    @FXML
    private lateinit var spnSize: Spinner<Int>

    @FXML
    private lateinit var tglBold: ToggleButton

    @FXML
    private lateinit var tglItalic: ToggleButton

    @FXML
    private lateinit var cmbAlignment: ComboBox<Alignment>

    @FXML
    private lateinit var spnLineSpacing: Spinner<Double>

    @InjectViewModel
    private lateinit var viewModel: StyleDataEditorViewModel

    // Re-entrancy guard shared by bindInt and bindDouble, so a push in one direction never triggers
    // an immediate push back.
    private var updating = false

    override fun initialize(location: URL?, resources: ResourceBundle?) {
        val messages = resources ?: Messages.bundle

        cmbFamily.isEditable = true
        cmbFamily.items.setAll(FontCatalog.families)
        // The editable combo box already mirrors a selected item into its editor text, so binding the
        // editor alone keeps both the typed and the picked family in step with the model.
        cmbFamily.editor.textProperty().bindBidirectional(viewModel.familyName)

        lblFamilyWarning.visibleProperty().bind(viewModel.familyNotInstalled)
        lblFamilyWarning.managedProperty().bind(viewModel.familyNotInstalled)
        lblFamilyWarning.styleClass.add(FIELD_WARNING)

        bindInt(spnSize, viewModel.size)
        bindDouble(spnLineSpacing, viewModel.lineSpacing)

        tglBold.selectedProperty().bindBidirectional(viewModel.bold)
        tglItalic.selectedProperty().bindBidirectional(viewModel.italic)

        cmbAlignment.items.setAll(Alignment.entries)
        cmbAlignment.converter = object : StringConverter<Alignment>() {
            override fun toString(alignment: Alignment?): String =
                alignment?.let { messages.getString(bundleKey(it)) } ?: ""

            override fun fromString(string: String?): Alignment? = null
        }
        cmbAlignment.valueProperty().bindBidirectional(viewModel.alignment)
    }

    /** Message bundle key of the label of [alignment]. */
    private fun bundleKey(alignment: Alignment): String = when (alignment) {
        Alignment.LEFT -> "component.styleDataEditor.alignment.left"
        Alignment.CENTER -> "component.styleDataEditor.alignment.center"
        Alignment.RIGHT -> "component.styleDataEditor.alignment.right"
        Alignment.BLOCK -> "component.styleDataEditor.alignment.block"
    }

    /** Wires an integer spinner to [model], see the class KDoc for why a plain binding cannot do it. */
    private fun bindInt(spinner: Spinner<Int>, model: IntegerProperty) {
        val factory = IntegerSpinnerValueFactory(0, 999, model.get().coerceIn(0, 999))
        // Spinner.increment/decrement commit the editor text through the factory's OWN converter
        // before applying the step; without this, that commit would silently re-parse the text with
        // JavaFX's default converter instead of ours and corrupt the value being stepped from.
        factory.converter = INT_CONVERTER
        spinner.valueFactory = factory
        // The editor text starts blank; binding it straight to the factory would let that blank text
        // win the initial sync and overwrite the seeded value with zero, so the text is primed first.
        spinner.editor.text = INT_CONVERTER.toString(factory.value)
        Bindings.bindBidirectional(spinner.editor.textProperty(), factory.valueProperty(), INT_CONVERTER)

        factory.valueProperty().addListener(ChangeListener { _, _, value ->
            if (updating) return@ChangeListener
            updating = true
            try {
                model.set(value ?: 0)
            } finally {
                updating = false
            }
        })
        model.addListener(ChangeListener { _, _, value ->
            if (updating) return@ChangeListener
            updating = true
            try {
                factory.value = value.toInt().coerceIn(0, 999)
            } finally {
                updating = false
            }
        })
    }

    /** Wires a double spinner to [model], see the class KDoc for why a plain binding cannot do it. */
    private fun bindDouble(spinner: Spinner<Double>, model: DoubleProperty) {
        val factory = DoubleSpinnerValueFactory(0.0, 5.0, model.get().coerceIn(0.0, 5.0), 0.05)
        // See bindInt for why the factory's own converter must match ours: increment/decrement commit
        // the editor text through it before stepping, and JavaFX's default converter parses/formats
        // differently, corrupting the value the step is computed from.
        factory.converter = DOUBLE_CONVERTER
        spinner.valueFactory = factory
        // See bindInt for why the editor text is primed before the bidirectional binding is set up.
        spinner.editor.text = DOUBLE_CONVERTER.toString(factory.value)
        Bindings.bindBidirectional(spinner.editor.textProperty(), factory.valueProperty(), DOUBLE_CONVERTER)

        factory.valueProperty().addListener(ChangeListener { _, _, value ->
            if (updating) return@ChangeListener
            updating = true
            try {
                model.set(value ?: 0.0)
            } finally {
                updating = false
            }
        })
        model.addListener(ChangeListener { _, _, value ->
            if (updating) return@ChangeListener
            updating = true
            try {
                factory.value = value.toDouble().coerceIn(0.0, 5.0)
            } finally {
                updating = false
            }
        })
    }
}

/** Null- and blank-tolerant converter between a spinner editor's text and its boxed integer value. */
private val INT_CONVERTER: StringConverter<Int> = object : StringConverter<Int>() {
    override fun toString(value: Int?): String = value?.toString() ?: ""
    override fun fromString(string: String?): Int = string?.trim()?.toIntOrNull() ?: 0
}

/** Null- and blank-tolerant converter between a spinner editor's text and its boxed double value. */
private val DOUBLE_CONVERTER: StringConverter<Double> = object : StringConverter<Double>() {
    override fun toString(value: Double?): String = value?.toString() ?: ""
    override fun fromString(string: String?): Double = string?.trim()?.replace(',', '.')?.toDoubleOrNull() ?: 0.0
}

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

import de.saxsys.mvvmfx.ViewModel
import javafx.beans.InvalidationListener
import javafx.beans.binding.Bindings
import javafx.beans.binding.BooleanBinding
import javafx.beans.property.BooleanProperty
import javafx.beans.property.ObjectProperty
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.beans.property.StringProperty
import javafx.beans.value.ChangeListener
import javafx.beans.value.ObservableValue
import org.pcsoft.app.aighost.fx.model.project.design.DesignProperty
import java.math.RoundingMode
import kotlin.math.abs

/**
 * View model of [DesignSettings].
 *
 * The page geometry is stored in points and shown in millimetres, so this model is the single place
 * the two units meet: every field is a millimetre text, and the conversion happens on the way in and
 * on the way out. The model object it edits is a [DesignProperty] handed in through [bind]; the
 * component owns no state of its own, the millimetre texts are only the current value of the design
 * expressed differently.
 *
 * [valid] reports whether the current input can be stored - a positive width and height and a
 * number of zero or more in every margin. A single margin can never grow past a third of its page
 * measure, so the margins of an axis always leave room for the text between them; the view caps the
 * spinner at that third. While the input is invalid the design keeps the last value that could be
 * parsed.
 */
class DesignSettingsViewModel : ViewModel {

    private companion object {
        /** Points per millimetre, `72 / 25.4`. */
        const val PT_PER_MM: Double = 72.0 / 25.4
    }

    /** Chosen page size preset, [PagePreset.CUSTOM] while width and height are set by hand. */
    val preset: ObjectProperty<PagePreset> = SimpleObjectProperty(this, "preset", PagePreset.CUSTOM)

    /** Page width in millimetres, as text. */
    val widthMm: StringProperty = SimpleStringProperty(this, "widthMm", "")

    /** Page height in millimetres, as text. */
    val heightMm: StringProperty = SimpleStringProperty(this, "heightMm", "")

    /** Inner (spine) margin in millimetres, as text. */
    val innerMm: StringProperty = SimpleStringProperty(this, "innerMm", "")

    /** Outer (open edge) margin in millimetres, as text. */
    val outerMm: StringProperty = SimpleStringProperty(this, "outerMm", "")

    /** Top margin in millimetres, as text. */
    val topMm: StringProperty = SimpleStringProperty(this, "topMm", "")

    /** Bottom margin in millimetres, as text. */
    val bottomMm: StringProperty = SimpleStringProperty(this, "bottomMm", "")

    /** Whether the book begins with a blank page. */
    val startWithEmptyPage: BooleanProperty = SimpleBooleanProperty(this, "startWithEmptyPage", false)

    /** Whether the book ends with a blank page. */
    val endWithEmptyPage: BooleanProperty = SimpleBooleanProperty(this, "endWithEmptyPage", false)

    private val mmFields: List<StringProperty>
        get() = listOf(widthMm, heightMm, innerMm, outerMm, topMm, bottomMm)

    /** Whether the user sizes the page freely, so the width and height fields are open. */
    val customSize: BooleanBinding = preset.isEqualTo(PagePreset.CUSTOM)

    /** Whether the page size is a positive width and a positive height. */
    val sizeValid: BooleanBinding =
        Bindings.createBooleanBinding({ positive(widthMm.get()) && positive(heightMm.get()) }, widthMm, heightMm)

    /** Whether every margin is a number that is zero or greater. */
    val marginsValid: BooleanBinding = Bindings.createBooleanBinding(
        { listOf(innerMm, outerMm, topMm, bottomMm).all { zeroOrMore(it.get()) } },
        innerMm, outerMm, topMm, bottomMm
    )

    /** Whether the whole form can be stored. */
    val valid: BooleanBinding = sizeValid.and(marginsValid)

    /** Whether the page size fields are in error. */
    val sizeError: BooleanBinding = sizeValid.not()

    // The model the form follows right now, and the listeners it follows it with, so both can be
    // released again when another model takes its place.
    private var design: DesignProperty? = null
    private var updating = false

    private val fromModel = InvalidationListener { pullFromModel() }
    private val toModel = ChangeListener<String> { observable, _, _ -> pushToModel(observable) }
    private val presetListener = ChangeListener<PagePreset> { _, _, _ -> applyPreset() }

    /**
     * Lets the form follow [design] and releases the model it followed before.
     *
     * @param design the design property of the working copy
     */
    fun bind(design: DesignProperty) {
        release()

        this.design = design
        val pf = design.pageFormatProperty
        pageMeasures(pf).forEach { it.addListener(fromModel) }

        mmFields.forEach { it.addListener(toModel) }
        preset.addListener(presetListener)

        startWithEmptyPage.bindBidirectional(design.startWithEmptyPageProperty)
        endWithEmptyPage.bindBidirectional(design.endWithEmptyPageProperty)

        pullFromModel()
    }

    /** Drops every listener and binding of the current model, so it can be handed a new one. */
    internal fun release() {
        val design = this.design ?: return
        pageMeasures(design.pageFormatProperty).forEach { it.removeListener(fromModel) }

        mmFields.forEach { it.removeListener(toModel) }
        preset.removeListener(presetListener)

        startWithEmptyPage.unbindBidirectional(design.startWithEmptyPageProperty)
        endWithEmptyPage.unbindBidirectional(design.endWithEmptyPageProperty)

        this.design = null
    }

    private fun pageMeasures(pf: org.pcsoft.app.aighost.fx.model.project.design.PageFormatProperty) = listOf(
        pf.widthProperty, pf.heightProperty,
        pf.innerMarginProperty, pf.outerMarginProperty, pf.topMarginProperty, pf.bottomMarginProperty
    )

    private fun pullFromModel() {
        val pf = design?.pageFormatProperty ?: return
        updating = true
        try {
            setIfChanged(widthMm, pf.width / PT_PER_MM)
            setIfChanged(heightMm, pf.height / PT_PER_MM)
            setIfChanged(innerMm, pf.innerMargin / PT_PER_MM)
            setIfChanged(outerMm, pf.outerMargin / PT_PER_MM)
            setIfChanged(topMm, pf.topMargin / PT_PER_MM)
            setIfChanged(bottomMm, pf.bottomMargin / PT_PER_MM)
            preset.set(PagePreset.match(pf.width / PT_PER_MM, pf.height / PT_PER_MM))
        } finally {
            updating = false
        }
    }

    private fun pushToModel(changed: ObservableValue<out String>? = null) {
        if (updating) return
        val pf = design?.pageFormatProperty ?: return

        fun write(field: StringProperty, set: (Double) -> Unit) {
            if (changed != null && changed !== field) return
            mm(field.get())?.let { set(it * PT_PER_MM) }
        }

        write(widthMm) { pf.width = it }
        write(heightMm) { pf.height = it }
        write(innerMm) { pf.innerMargin = it }
        write(outerMm) { pf.outerMargin = it }
        write(topMm) { pf.topMargin = it }
        write(bottomMm) { pf.bottomMargin = it }
    }

    private fun applyPreset() {
        if (updating) return
        val p = preset.get() ?: return
        if (p == PagePreset.CUSTOM || p.widthMm == null || p.heightMm == null) return
        widthMm.set(format(p.widthMm))
        heightMm.set(format(p.heightMm))
    }

    private fun setIfChanged(prop: StringProperty, valueMm: Double) {
        val current = mm(prop.get())
        if (current == null || abs(current - valueMm) > 1e-6) prop.set(format(valueMm))
    }

    private fun mm(text: String?): Double? =
        text?.trim()?.replace(',', '.')?.toDoubleOrNull()?.takeIf { it.isFinite() }

    private fun positive(text: String?): Boolean = (mm(text) ?: -1.0) > 0.0

    private fun zeroOrMore(text: String?): Boolean = (mm(text) ?: -1.0) >= 0.0

    private fun format(valueMm: Double): String {
        val plain = valueMm.toBigDecimal().setScale(2, RoundingMode.HALF_UP).toPlainString()
        return if ('.' in plain) plain.trimEnd('0').trimEnd('.') else plain
    }
}

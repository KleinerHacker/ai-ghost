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
import javafx.beans.binding.Bindings
import javafx.beans.binding.BooleanBinding
import javafx.beans.property.BooleanProperty
import javafx.beans.property.DoubleProperty
import javafx.beans.property.IntegerProperty
import javafx.beans.property.ObjectProperty
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleDoubleProperty
import javafx.beans.property.SimpleIntegerProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.beans.property.StringProperty
import org.pcsoft.app.aighost.fx.model.common.StyleDataProperty
import org.pcsoft.app.aighost.layouting.fx.font.FontCatalog
import org.pcsoft.app.aighost.model.common.Alignment

/**
 * View model of [StyleDataEditor].
 *
 * Every field of this model - the font family, its size, weight and slant, the line spacing and the
 * alignment - is typed exactly as the underlying [StyleDataProperty] carries it, so [bind] wires each
 * one straight to its counterpart with a plain `bindBidirectional`; unlike the millimetre form of
 * `DesignSettingsViewModel` there is no unit conversion in between. The model object it edits is
 * handed in through [bind], the component owns no state of its own.
 *
 * [familyNotInstalled] reports whether the current family is not among [FontCatalog.families], so the
 * view can show a warning next to the family field without failing the form: a design saved on
 * another machine may name a font this one does not have installed. [valid] reports whether the
 * current input can be stored - a positive size, a positive line spacing and a family that is not
 * blank.
 */
class StyleDataEditorViewModel : ViewModel {

    /** Font family name. */
    val familyName: StringProperty = SimpleStringProperty(this, "familyName", "")

    /** Font size in points. */
    val size: IntegerProperty = SimpleIntegerProperty(this, "size", 0)

    /** Whether the text is drawn in a bold weight. */
    val bold: BooleanProperty = SimpleBooleanProperty(this, "bold", false)

    /** Whether the text is drawn slanted. */
    val italic: BooleanProperty = SimpleBooleanProperty(this, "italic", false)

    /** Horizontal placement of the text. */
    val alignment: ObjectProperty<Alignment?> = SimpleObjectProperty(this, "alignment", null)

    /** Space between the lines of the text, as a factor. */
    val lineSpacing: DoubleProperty = SimpleDoubleProperty(this, "lineSpacing", 0.0)

    /** Whether [familyName] is not among the families this machine has installed. */
    val familyNotInstalled: BooleanBinding = Bindings.createBooleanBinding(
        { familyName.get().let { !it.isNullOrBlank() && !FontCatalog.contains(it) } },
        familyName
    )

    /** Whether the current input can be stored. */
    val valid: BooleanBinding = size.greaterThan(0)
        .and(lineSpacing.greaterThan(0.0))
        .and(Bindings.createBooleanBinding({ !familyName.get().isNullOrBlank() }, familyName))

    // The model this form follows right now, so it can be released again when another one takes its
    // place.
    private var style: StyleDataProperty? = null

    /**
     * Lets the form follow [style] and releases the model it followed before.
     *
     * @param style the style property of the working copy
     */
    fun bind(style: StyleDataProperty) {
        release()

        this.style = style
        familyName.bindBidirectional(style.fontProperty.nameProperty)
        size.bindBidirectional(style.fontProperty.sizeProperty)
        bold.bindBidirectional(style.fontProperty.boldProperty)
        italic.bindBidirectional(style.fontProperty.italicProperty)
        alignment.bindBidirectional(style.alignmentProperty)
        lineSpacing.bindBidirectional(style.textLineSpacingProperty)
    }

    /** Drops every binding of the current model, so it can be handed a new one. */
    internal fun release() {
        val style = this.style ?: return

        familyName.unbindBidirectional(style.fontProperty.nameProperty)
        size.unbindBidirectional(style.fontProperty.sizeProperty)
        bold.unbindBidirectional(style.fontProperty.boldProperty)
        italic.unbindBidirectional(style.fontProperty.italicProperty)
        alignment.unbindBidirectional(style.alignmentProperty)
        lineSpacing.unbindBidirectional(style.textLineSpacingProperty)

        this.style = null
    }
}

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

package org.pcsoft.app.aighost.app.ui.dialog

import de.saxsys.mvvmfx.ViewModel
import javafx.beans.binding.Bindings
import javafx.beans.binding.ObjectBinding
import javafx.beans.binding.StringBinding
import javafx.beans.property.BooleanProperty
import javafx.beans.property.ObjectProperty
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.beans.property.StringProperty
import javafx.scene.image.Image
import org.pcsoft.app.aighost.app.Messages

/**
 * View model of [DetailDialog].
 *
 * Everything the dialog shows is an input of whoever opens it - [caption], [message] and [details]
 * carry text that is already translated, [type] decides the severity. The only state of its own is
 * [detailsVisible], which the user switches with the details button.
 */
class DetailDialogViewModel : ViewModel {

    /** Headline of the dialog, telling in one line what happened. */
    val caption: StringProperty = SimpleStringProperty(this, "caption", "")

    /** Text of the dialog, describing what happened. */
    val message: StringProperty = SimpleStringProperty(this, "message", "")

    /** Detailed report shown in the details pane, empty when there is nothing to unfold. */
    val details: StringProperty = SimpleStringProperty(this, "details", "")

    /** Severity the dialog is shown in. */
    val type: ObjectProperty<DialogType> = SimpleObjectProperty(this, "type", DialogType.ERROR)

    /** Whether the details pane is unfolded. */
    val detailsVisible: BooleanProperty = SimpleBooleanProperty(this, "detailsVisible", false)

    /** Icon of the severity in the colour scheme the theme is dressed in. */
    val icon: ObjectBinding<Image> = Bindings.createObjectBinding({ type.value.icon }, type)

    /** Label of the details button, naming the switch the user gets, not the current state. */
    val detailsButtonText: StringBinding = Bindings.createStringBinding(
        { Messages[if (detailsVisible.value) "dialog.details.hide" else "dialog.details.show"] },
        detailsVisible
    )

    /** Unfolds the details pane while it is folded and folds it again while it is unfolded. */
    fun toggleDetails() {
        detailsVisible.value = !detailsVisible.value
    }
}

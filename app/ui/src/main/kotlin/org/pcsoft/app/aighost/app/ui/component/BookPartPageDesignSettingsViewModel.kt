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
import javafx.beans.binding.BooleanExpression
import org.pcsoft.app.aighost.fx.model.common.StyleDataProperty

/**
 * View model of [BookPartPageDesignSettings].
 *
 * The three style properties handed to [bind] are not mirrored into fields of this model; each one
 * is forwarded straight to the matching embedded [StyleDataEditor] through its public [StyleDataEditor.bindStyle],
 * reached through [titleEditor], [titleAppendixEditor] and [textEditor] which the view wires up once
 * the FXML is loaded. [valid] links the three embedded editors' validity with a logical AND, so the
 * section they sit in is locked as soon as one of them holds input that cannot be stored.
 */
class BookPartPageDesignSettingsViewModel : ViewModel {

    /** Embedded editor for the title style. */
    lateinit var titleEditor: StyleDataEditor

    /** Embedded editor for the style of the further title lines. */
    lateinit var titleAppendixEditor: StyleDataEditor

    /** Embedded editor for the body text style. */
    lateinit var textEditor: StyleDataEditor

    /** Whether the current input of every embedded editor can be stored. */
    val valid: BooleanExpression
        get() = Bindings.and(titleEditor.valid, titleAppendixEditor.valid).and(textEditor.valid)

    /**
     * Lets the three embedded editors follow [titleStyle], [titleAppendixStyle] and [textStyle], and
     * releases the models they followed before.
     *
     * @param titleStyle the style property of the title
     * @param titleAppendixStyle the style property of the further title lines
     * @param textStyle the style property of the body text
     */
    fun bind(titleStyle: StyleDataProperty, titleAppendixStyle: StyleDataProperty, textStyle: StyleDataProperty) {
        titleEditor.bindStyle(titleStyle)
        titleAppendixEditor.bindStyle(titleAppendixStyle)
        textEditor.bindStyle(textStyle)
    }
}

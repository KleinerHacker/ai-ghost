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

import javafx.scene.control.ButtonType

/**
 * Button sets a dialog of the application can be closed with.
 *
 * The set is chosen independently of [DialogType], so a warning can be a plain notice as well as a
 * question. The buttons are the standard ones of JavaFX, which carry their own translation and the
 * keyboard behaviour that belongs to them: the affirmative button answers ENTER, the negative one
 * answers ESCAPE and the window close button.
 *
 * @property buttonTypes buttons of this set, in the order they are shown
 */
enum class DialogButtons(val buttonTypes: List<ButtonType>) {

    /** A single button acknowledging the dialog. */
    OK(listOf(ButtonType.OK)),

    /** An affirmative and a negative button, the affirmative one being the default. */
    YES_NO(listOf(ButtonType.YES, ButtonType.NO))
}

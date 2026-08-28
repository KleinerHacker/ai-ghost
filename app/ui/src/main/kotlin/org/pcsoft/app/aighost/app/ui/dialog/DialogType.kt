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

import javafx.scene.control.Alert
import javafx.scene.image.Image
import org.pcsoft.app.aighost.app.AiGhostIcons

/**
 * Severity a dialog of the application is shown in.
 *
 * The severity decides the icon and the alert type only; which buttons the dialog carries is a
 * separate decision and is expressed by [DialogButtons], because a warning is used both as a plain
 * notice and as a question.
 *
 * @property alertType alert type JavaFX builds the dialog upon
 */
enum class DialogType(val alertType: Alert.AlertType) {

    /** Something went wrong and cannot be undone by the user. */
    ERROR(Alert.AlertType.ERROR),

    /** Something needs the attention of the user before the application carries on. */
    WARNING(Alert.AlertType.WARNING);

    /**
     * Reads the icon of this severity in the colour scheme the theme is currently dressed in.
     *
     * @return the icon shown next to the caption of the dialog
     */
    val icon: Image
        get() = when (this) {
            ERROR -> AiGhostIcons.error()
            WARNING -> AiGhostIcons.warning()
        }
}

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
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotSame
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.pcsoft.app.aighost.app.AiGhostIcons
import org.testfx.framework.junit5.ApplicationExtension

/**
 * Developer tests for [DialogType].
 */
@ExtendWith(ApplicationExtension::class)
class DialogTypeTest {

    /**
     * Use case: the severity decides which alert JavaFX builds, so a failure is reported as an error
     * and a question as a warning.
     */
    @Test
    fun mapsEverySeverityToItsAlertType() {
        assertEquals(Alert.AlertType.ERROR, DialogType.ERROR.alertType)
        assertEquals(Alert.AlertType.WARNING, DialogType.WARNING.alertType)
    }

    /**
     * Use case: every severity is recognised by its own icon, and both icons are shipped, so no
     * dialog is opened without its graphic.
     */
    @Test
    fun carriesItsOwnIcon() {
        val error = DialogType.ERROR.icon
        val warning = DialogType.WARNING.icon

        assertFalse(error.isError, "the error icon could not be loaded")
        assertFalse(warning.isError, "the warning icon could not be loaded")
        assertEquals(AiGhostIcons.DIALOG_ICON_STORED_SIZE.toDouble(), error.width)
        assertEquals(AiGhostIcons.DIALOG_ICON_STORED_SIZE.toDouble(), warning.width)
        assertNotSame(error, warning, "both severities show the same icon")
    }
}

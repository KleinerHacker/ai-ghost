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

import javafx.geometry.Insets
import javafx.scene.control.Label
import javafx.scene.layout.BorderPane
import javafx.scene.layout.VBox
import org.pcsoft.app.aighost.app.Messages

/**
 * Stand-in for a settings section whose editor is not built yet.
 *
 * The design sections of the project settings tree are filled in by IP-13. Until then this component
 * shows the name of the picked section and a short note that it is configured in a later step, so the
 * tree already carries every node the finished dialog will.
 */
class PlaceholderSettings : BorderPane() {

    private val title = Label().apply { styleClass += "settings-placeholder-title" }
    private val note = Label(Messages["dialog.projectSettings.placeholder.note"]).apply {
        styleClass += "settings-placeholder-note"
        isWrapText = true
    }

    init {
        styleClass += "placeholder-settings"
        center = VBox(8.0, title, note).apply { padding = Insets(16.0) }
    }

    /**
     * Names the section this placeholder currently stands for.
     *
     * @param section the picked section, or `null` to clear the placeholder
     */
    fun setSection(section: ProjectSettingsSection?) {
        title.text = section?.let { Messages[it.bundleKey] } ?: ""
    }
}

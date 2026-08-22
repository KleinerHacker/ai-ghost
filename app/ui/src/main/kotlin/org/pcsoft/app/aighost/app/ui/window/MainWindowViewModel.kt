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

package org.pcsoft.app.aighost.app.ui.window

import de.saxsys.mvvmfx.ViewModel
import javafx.application.Platform
import javafx.beans.property.ListProperty
import javafx.beans.property.ObjectProperty
import javafx.beans.property.SimpleListProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.collections.FXCollections
import org.pcsoft.app.aighost.model.PreferencesStorage
import org.pcsoft.app.aighost.model.ProjectStorage
import org.pcsoft.app.aighost.model.pref.Preferences
import org.pcsoft.app.aighost.model.pref.RecentOpened
import org.pcsoft.app.aighost.model.project.Project
import java.io.File

/**
 * View model of the application main window.
 *
 * It follows the preferences while the window is shown: [openRecent] holds the files of the "open
 * recent" menu and is kept in step with the preferences through a listener that is registered in
 * [onShow] and removed again in [onHide].
 */
class MainWindowViewModel : ViewModel {

    /** Files the user opened last, the most recent one first. */
    val openRecent: ListProperty<File> = SimpleListProperty(FXCollections.observableArrayList())
    val project: ObjectProperty<Project> = SimpleObjectProperty(ProjectStorage.current)

    // Kept as a field: a method reference would create a new instance on every call, so removing
    // the listener again would not find the one that was added.
    private val preferencesListener = Preferences.Listener { preferences, property ->
        if (property == Preferences.Property.RECENT_OPENED) {
            onRecentOpenedChanged(preferences.recentOpened)
        }
    }

    internal fun onShow() {
        onRecentOpenedChanged(PreferencesStorage.current.recentOpened)
        PreferencesStorage.current.addListener(preferencesListener)
    }

    internal fun onHide() {
        PreferencesStorage.current.removeListener(preferencesListener)
    }

    private fun onRecentOpenedChanged(recentOpened: RecentOpened) {
        val entries = recentOpened.entries.map(::File)

        // The preferences notify on the thread that changed them, which is not necessarily the FX
        // thread, while the list is bound to the menu.
        if (Platform.isFxApplicationThread()) {
            openRecent.setAll(entries)
        } else {
            Platform.runLater { openRecent.setAll(entries) }
        }
    }
}

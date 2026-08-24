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
import javafx.beans.property.ListProperty
import javafx.beans.property.ObjectProperty
import javafx.beans.property.ReadOnlyBooleanProperty
import javafx.beans.property.SimpleListProperty
import javafx.beans.property.StringProperty
import javafx.collections.FXCollections
import org.pcsoft.app.aighost.fx.model.FXProjectStorage
import org.pcsoft.app.aighost.model.PreferencesStorage
import org.pcsoft.app.aighost.model.project.Project
import java.io.File

/**
 * View model of the application main window.
 *
 * [openRecent] holds the files of the "open recent" menu. The preferences are a plain value object
 * that reports nothing, so the menu is filled from them whenever the window comes on screen.
 */
class MainWindowViewModel : ViewModel {

    /** Files the user opened last, the most recent one first. */
    val openRecent: ListProperty<File> = SimpleListProperty(FXCollections.observableArrayList())
    val openRecentDisabled: ReadOnlyBooleanProperty = openRecent.emptyProperty()
    val project: ObjectProperty<Project> = FXProjectStorage.current

    /**
     * Name of the open project, the text the window title is built from.
     *
     * The name sits in the meta part of the project, which is a plain value object reporting nothing,
     * so it is taken from the property model instead of read off the project itself.
     */
    val projectName: StringProperty = FXProjectStorage.current.nameProperty

    /** Takes the recently opened files of the preferences over into [openRecent]. */
    internal fun onShow() {
        openRecent.setAll(PreferencesStorage.current.recentOpened.entries.map(::File))
    }
}

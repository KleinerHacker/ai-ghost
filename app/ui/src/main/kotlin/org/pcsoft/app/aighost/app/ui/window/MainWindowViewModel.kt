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
import javafx.beans.Observable
import javafx.beans.property.*
import javafx.collections.FXCollections
import org.pcsoft.app.aighost.app.controller.IoController
import org.pcsoft.app.aighost.fx.model.FXProjectStorage
import org.pcsoft.app.aighost.model.PreferencesStorage
import org.pcsoft.app.aighost.model.project.Project
import java.io.File

/**
 * View model of the application main window.
 *
 * [openRecent] holds the files of the "open recent" menu. The preferences are a plain value object
 * that reports nothing, so the menu is filled from them whenever the window comes on screen and is
 * written back when the window leaves it - but only when something was actually added meanwhile,
 * otherwise leaving the window would replace the stored list with whatever this one happens to hold.
 */
class MainWindowViewModel : ViewModel {

    /**
     * Files the user opened last, the most recent one first.
     *
     * The set keeps the order the files were put in, so the menu shows them in the order the
     * preferences carry them instead of in the order of their hash values.
     */
    val openRecent: SetProperty<File> = SimpleSetProperty(FXCollections.observableSet(LinkedHashSet()))
    val openRecentDisabled: ReadOnlyBooleanProperty = openRecent.emptyProperty()
    val project: ObjectProperty<Project> = FXProjectStorage.current

    // Whether the set was touched since it was last filled from the preferences. Only then is there
    // something of this window to write back; without it, leaving the window would overwrite the
    // stored list with an empty one whenever nothing was opened.
    private var openRecentChanged = false

    init {
        openRecent.addListener { _: Observable -> openRecentChanged = true }
    }

    /**
     * Name of the open project, the text the window title is built from.
     *
     * The name sits in the meta part of the project, which is a plain value object reporting nothing,
     * so it is taken from the property model instead of read off the project itself.
     */
    val projectName: StringProperty = FXProjectStorage.current.nameProperty

    /**
     * Takes the recently opened files of the preferences over into [openRecent].
     *
     * The set counts as untouched afterwards, so leaving the window again without having opened
     * anything writes nothing back.
     */
    internal fun onShow() {
        openRecent.clear()
        openRecent.addAll(PreferencesStorage.current.recentOpened.entries.map(::File))
        openRecentChanged = false
    }

    /**
     * Writes the recently opened files back into the preferences and stores them.
     *
     * Only a set that was actually added to reaches the preferences: the window is filled from them
     * and would otherwise hand back what it happens to hold, which erases the list of the user when
     * the window leaves the screen without a project having been opened.
     */
    internal fun onHide() {
        if (!openRecentChanged)
            return

        val limit = PreferencesStorage.current.recentOpened.max
        PreferencesStorage.current.recentOpened.entries = openRecent.value.take(limit).map { it.absolutePath }
        IoController.savePreferences()
        openRecentChanged = false
    }
}

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
import javafx.beans.binding.BooleanBinding
import javafx.beans.property.*
import javafx.collections.FXCollections
import org.pcsoft.app.aighost.app.controller.IoController
import org.pcsoft.app.aighost.fx.model.project.ProjectProperty
import org.pcsoft.app.aighost.model.project.Project
import java.io.File

/**
 * View model of the application main window.
 *
 * The window owns the project the user works on: [project] is that project as a property model, so
 * every control below the window follows a field of it, and [currentFile] is the document it belongs
 * to. The storages of `ai-ghost-model` keep nothing themselves - they are asked to read and to write
 * through [IoController], and the answer lands here.
 *
 * [openRecent] holds the files of the "open recent" menu. The settings are a plain value object that
 * reports nothing, so the menu is filled from them whenever the window comes on screen and is written
 * back when the window leaves it - but only when something was actually added meanwhile, otherwise
 * leaving the window would replace the stored list with whatever this one happens to hold.
 */
class MainWindowViewModel : ViewModel {

    //region Project Part
    /**
     * The project the user works on, with every part and field of it as a property of its own.
     *
     * Carries a fresh project until one is opened, so the window has something to show from the
     * first moment on.
     */
    val project: ProjectProperty = ProjectProperty(Project())

    /** The document [project] belongs to, absent while it was never saved. */
    val currentFile: ObjectProperty<File?> = SimpleObjectProperty(this, "currentFile", null)

    /** Whether the open project already has a document, which decides between save and save as. */
    val alreadySaved: BooleanBinding = currentFile.isNotNull

    /**
     * Name of the open project, the text the window title is built from.
     *
     * The name sits in the meta part of the project, which is a plain value object reporting nothing,
     * so it is taken from the property model instead of read off the project itself.
     */
    val projectName: StringProperty = project.metaProperty.nameProperty
    //endregion

    //region Preferences Part
    /**
     * Files the user opened last, the most recent one first.
     *
     * The set keeps the order the files were put in, so the menu shows them in the order the
     * preferences carry them instead of in the order of their hash values.
     */
    val openRecent: SetProperty<File> = SimpleSetProperty(FXCollections.observableSet(LinkedHashSet()))

    /**
     * A read-only Boolean property that indicates whether the "Open Recent" functionality
     * is disabled. It is `true` when the list of recently opened files is empty.
     */
    val openRecentDisabled: ReadOnlyBooleanProperty = openRecent.emptyProperty()

    // Whether the set was touched since it was last filled from the preferences. Only then is there
    // something of this window to write back; without it, leaving the window would overwrite the
    // stored list with an empty one whenever nothing was opened.
    private var openRecentChanged = false
    //endregion

    init {
        openRecent.addListener { _: Observable -> openRecentChanged = true }
    }

    //region Project Part
    /**
     * Closes the open project and starts a fresh one.
     *
     * The fresh project has no document, so the next save needs a file the user names.
     */
    fun newProject() {
        project.value = Project()
        currentFile.value = null
    }

    /**
     * Opens the project of [file], as far as it could be read.
     *
     * A document that could not be read leaves the open project where it is - the user was told
     * about it by [IoController] already. A document that was read becomes the open one and is
     * remembered in [openRecent].
     *
     * @param file The document to open.
     * @return `true` when the project was opened.
     */
    fun openProject(file: File): Boolean {
        val loaded = IoController.loadProject(file) ?: return false

        project.value = loaded
        currentFile.value = file
        openRecent.add(file)

        return true
    }

    /**
     * Writes the open project to [file], or to the document it belongs to when none is named.
     *
     * A successful write makes [file] the document of the open project, which turns the first save
     * of a fresh project into a "save as". A project without a document and without a named file is
     * not written at all; the window asks the user for a path instead.
     *
     * @param file The document to write to, `null` for the one the project belongs to.
     * @return `true` when the project was written.
     */
    fun saveProject(file: File? = null): Boolean {
        val target = file ?: currentFile.value ?: return false

        if (!IoController.saveProject(project.value, target))
            return false

        currentFile.value = target

        return true
    }
    //endregion

    /**
     * Takes the recently opened files of the preferences over into [openRecent].
     *
     * The set counts as untouched afterwards, so leaving the window again without having opened
     * anything writes nothing back.
     */
    internal fun onShow() {
        openRecent.clear()
        openRecent.addAll(IoController.preferences.recentOpened.entries.map(::File))
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

        val recentOpened = IoController.preferences.recentOpened
        recentOpened.entries = openRecent.value.take(recentOpened.max).map { it.absolutePath }
        IoController.preferences.refresh()
        IoController.savePreferences()
        openRecentChanged = false
    }
}

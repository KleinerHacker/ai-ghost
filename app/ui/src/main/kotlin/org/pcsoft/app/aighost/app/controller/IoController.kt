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

package org.pcsoft.app.aighost.app.controller

import javafx.application.Application
import javafx.scene.control.Alert
import javafx.scene.control.ButtonType
import org.pcsoft.app.aighost.app.AiGhostTheme
import org.pcsoft.app.aighost.app.Messages
import org.pcsoft.app.aighost.app.util.logger
import org.pcsoft.app.aighost.fx.model.FXPreferencesStorage
import org.pcsoft.app.aighost.fx.model.FXProjectStorage
import org.pcsoft.app.aighost.model.PreferencesStorage
import org.pcsoft.app.aighost.model.ProjectStorage
import java.io.File

object IoController {
    private val log = logger<IoController>()

    //region Project
    /**
     * Saves the current project to persistent storage or to a specified file.
     *
     * If no file is provided, the project is saved to the default persistent storage.
     * If a file is provided, the project is saved to the specified file.
     *
     * @param file The file to save the project to. If `null`, defaults to saving to persistent storage.
     */
    fun saveProject(file: File? = null): Boolean {
        log.debug("Saving project {}", file?.absolutePath ?: FXProjectStorage.currentFile?.absolutePath ?: "")

        return (if (file == null) FXProjectStorage.save() else FXProjectStorage.save(file)).onLeft { error ->
            log.warn("Failed to save project: {}", error::class.simpleName)
            showFailure(
                Messages["project.error.save.title"],
                Messages["project.error.save.header"],
                reasonOfProjectError(error),
            )
        }.fold({false}, {true})
    }

    /**
     * Loads a project from the specified file and attempts to process it.
     * If loading fails, a warning is logged and the user is told what went wrong.
     *
     * @param file The file from which the project will be loaded.
     */
    fun loadProject(file: File): Boolean {
        log.debug("Loading project {}", file.absolutePath)

        return FXProjectStorage.load(file).fold({ error ->
            when (error) {
                is ProjectStorage.Error.Incomplete -> rescueProject(error)
                else -> {
                    log.warn("Failed to load project: {}", error::class.simpleName)
                    showFailure(
                        Messages["project.error.load.title"],
                        Messages["project.error.load.header"],
                        reasonOfProjectError(error),
                    )
                    false
                }
            }
        }, { true })
    }

    /**
     * Opens a project that lost parts beyond the standard ones, but only when the user accepts the
     * loss.
     *
     * The user is told plainly what the document lost and that the loss becomes final with the next
     * save, because the project is written without those parts from then on.
     *
     * @param error The failure that carries the project that could still be read.
     * @return `true` when the project was opened, `false` when the user did not accept the loss.
     */
    private fun rescueProject(error: ProjectStorage.Error.Incomplete): Boolean {
        log.warn("Project {} lost the part(s) {}", error.file.absolutePath, error.lostParts)

        val openButton = ButtonType(Messages["project.incomplete.button"])
        val cancelButton = ButtonType(Messages["button.cancel"])

        val result = Alert(Alert.AlertType.WARNING).apply {
            title = Messages["project.incomplete.title"]
            headerText = Messages["project.incomplete.header"]
            contentText = listOf(
                Messages["project.incomplete.content"],
                Messages["project.incomplete.parts"],
                error.lostParts.sorted().joinToString(System.lineSeparator()) { "- $it" },
                Messages["project.incomplete.hint"]
            ).joinToString(System.lineSeparator() + System.lineSeparator())

            buttonTypes.setAll(openButton, cancelButton)
            decorate(this)
        }.showAndWait()

        if (!result.isPresent || result.get() != openButton) {
            log.info("The incomplete project was not opened")
            return false
        }

        FXProjectStorage.open(error.recovered, error.file)
        return true
    }

    /**
     * Determines the reason for a project storage error and provides a user-facing message.
     *
     * @param error The project storage error that occurred.
     * @return A localized string explaining the reason for the error.
     */
    internal fun reasonOfProjectError(error: ProjectStorage.Error): String = when (error) {
        is ProjectStorage.Error.NoFile -> Messages["project.error.noFile"]
        is ProjectStorage.Error.NotFound -> Messages["project.error.notFound"]
        is ProjectStorage.Error.NotAFile -> Messages["project.error.notAFile"]
        is ProjectStorage.Error.Unreadable -> Messages["project.error.unreadable"]
        is ProjectStorage.Error.Malformed -> Messages["project.error.malformed"]
        is ProjectStorage.Error.Corrupt -> Messages["project.error.corrupt"]
        is ProjectStorage.Error.Incomplete -> Messages["project.error.incomplete"]
    }
    //endregion

    //region Preferences
    /**
     * Loads the preferences and handles a failure according to [handlePreferencesError].
     *
     * A successful read needs no further work, because the storage keeps the loaded preferences.
     *
     * @param app the running application, stopped when a failure leaves nothing to work with
     */
    fun loadPreferences(app: Application) {
        log.debug("Loading preferences")

        FXPreferencesStorage.load().onLeft { error ->
            log.warn("Failed to load preferences: {}", error::class.simpleName)
            handlePreferencesError(error, app)
        }
    }

    /**
     * Writes the preferences to their file and tells the user when that did not work.
     *
     * A failed write is reported but leaves the application running, because the settings the user
     * made are still in memory and another attempt may succeed.
     *
     * @return `true` when the preferences were written
     */
    fun savePreferences(): Boolean {
        log.debug("Saving preferences")

        return FXPreferencesStorage.save().onLeft { error ->
            log.warn("Failed to save preferences: {}", error::class.simpleName)
            showFailure(
                Messages["preferences.error.title"],
                Messages["preferences.error.save.header"],
                reasonOfPreferencesError(error),
            )
        }.fold({false}, {true})
    }

    /**
     * Decides how a read failure is answered.
     *
     * A missing file is the normal case of a first start and is answered by the defaults. Content
     * the application cannot understand and a file it cannot read may hold settings the user made,
     * so overwriting them is left to the user. A directory in place of the file is nothing the
     * application may remove on its own.
     *
     * @param error the failure reported by the storage
     * @return the reaction the failure deserves
     */
    internal fun handlePreferencesError(error: PreferencesStorage.Error, app: Application) = when (error) {
        is PreferencesStorage.Error.NotFound -> resetPreferences(app)
        is PreferencesStorage.Error.Malformed -> askForResetPreferences(app)
        is PreferencesStorage.Error.Unreadable -> askForResetPreferences(app)
        is PreferencesStorage.Error.NotAFile -> handleComplexFailure(reasonOfPreferencesError(error), app)
    }

    /**
     * Determines the reason for a preferences storage error and provides a user-facing message.
     *
     * This function maps a specific instance of a preferences storage error into a message string
     * that can be used to inform the user about the nature of the issue. For example, if the error
     * is related to a path not being a regular file, it will return a corresponding localized message.
     *
     * @param error The preferences storage error that occurred.
     * @return A localized string explaining the reason for the error.
     */
    internal fun reasonOfPreferencesError(error: PreferencesStorage.Error): String = when (error) {
        is PreferencesStorage.Error.NotAFile -> Messages["preferences.error.notAFile"]
        is PreferencesStorage.Error.Malformed -> Messages["preferences.error.malformed"]
        is PreferencesStorage.Error.NotFound -> Messages["preferences.error.notFound"]
        is PreferencesStorage.Error.Unreadable -> Messages["preferences.error.unreadable"]
    }

    /**
     * Asks the user whether the preferences may be replaced by their defaults.
     *
     * Everything but an explicit confirmation stops the application, because working with settings
     * that could not be read would silently overwrite them on the next save.
     *
     * @param app the running application, stopped when the user does not confirm
     */
    private fun askForResetPreferences(app: Application) {
        log.debug("Asking for reset preferences")

        val resetButton = ButtonType(Messages["preferences.reset.button"])
        val cancelButton = ButtonType(Messages["button.cancel"])

        val result = Alert(Alert.AlertType.CONFIRMATION).apply {
            title = Messages["preferences.reset.title"]
            headerText = Messages["preferences.reset.header"]
            contentText = Messages["preferences.reset.content"]

            buttonTypes.setAll(resetButton, cancelButton)
            decorate(this)
        }.showAndWait()

        if (result.isPresent && result.get() == resetButton) {
            resetPreferences(app)
        } else {
            exitingApp(app)
        }
    }

    /** Replaces the preferences by their defaults. */
    private fun resetPreferences(app: Application) {
        log.info("Resetting preferences")

        FXPreferencesStorage.reset()
        FXPreferencesStorage.save().onLeft {
            handleComplexFailure(reasonOfPreferencesError(it), app)
        }
    }

    /**
     * Reports a failure the application cannot recover from and stops afterwards.
     *
     * @param reason already translated description of what went wrong
     * @param app the running application, stopped once the report was acknowledged
     */
    private fun handleComplexFailure(reason: String, app: Application) {
        log.debug("Inform about complex error")

        showFailure(Messages["preferences.error.title"], Messages["preferences.error.header"], reason)

        exitingApp(app)
    }
    //endregion

    /**
     * Reports a failure of an input or output operation and leaves the application running.
     *
     * @param title already translated title of the report
     * @param header already translated headline of the report
     * @param reason already translated description of what went wrong
     */
    private fun showFailure(title: String, header: String, reason: String) {
        log.debug("Inform about error: {}", header)

        Alert(Alert.AlertType.ERROR).apply {
            this.title = title
            headerText = header
            contentText = reason

            buttonTypes.setAll(ButtonType(Messages["button.ok"]))
            decorate(this)
        }.showAndWait()
    }

    /**
     * Exits the application.
     */
    private fun exitingApp(app: Application) {
        log.info("Exiting application")
        app.stop()
    }

    /**
     * Dresses the dialog in the application theme, so an alert of the start up looks like every
     * other window.
     *
     * @param alert the alert to decorate
     */
    private fun decorate(alert: Alert) {
        alert.dialogPane.scene?.let(AiGhostTheme::apply)
    }

}

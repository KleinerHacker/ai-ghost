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
import org.pcsoft.app.aighost.app.Messages
import org.pcsoft.app.aighost.app.font.FontIdentity
import org.pcsoft.app.aighost.app.font.FontIdentityCheck
import org.pcsoft.app.aighost.app.ui.AiGhostDialog
import org.pcsoft.app.aighost.app.util.logger
import org.pcsoft.app.aighost.fx.model.pref.PreferencesProperty
import org.pcsoft.app.aighost.model.PreferencesStorage
import org.pcsoft.app.aighost.model.ProjectStorage
import org.pcsoft.app.aighost.model.pref.Preferences
import org.pcsoft.app.aighost.model.project.Project
import java.io.File
import java.text.MessageFormat

/**
 * Reads and writes the documents of the application and tells the user when that did not work.
 *
 * The storages of `ai-ghost-model` keep nothing, so the settings of the user live here: [preferences]
 * is the one instance the application works on, offered as a property model so a control follows a
 * setting that was changed elsewhere. The settings belong to the process and not to a window - they
 * are read before the first window exists and are read by the theme - which is why they sit here and
 * not in the view model of a window.
 *
 * The open project is not kept here: it belongs to the window showing it, so
 * [org.pcsoft.app.aighost.app.ui.window.MainWindowViewModel] holds it and asks this controller to
 * read and write it.
 */
object IoController {
    private val log = logger<IoController>()

    /**
     * The settings the application works on, with every field of them as a property of its own.
     *
     * Carries the defaults until [loadPreferences] read the file of the user.
     */
    val preferences: PreferencesProperty = PreferencesProperty(Preferences())

    //region Project
    /**
     * Writes [project] to [file] and tells the user when that did not work.
     *
     * Before the project is written, every font of its design that carries no fingerprint yet gets
     * one, so a manuscript records what its fonts measured like on the machine it was written on.
     *
     * @param project The project to store.
     * @param file The file to store it in.
     * @return `true` when the project was written.
     */
    fun saveProject(project: Project, file: File): Boolean {
        log.debug("Saving project {}", file.absolutePath)

        FontIdentityCheck.stamp(project.design)

        return ProjectStorage.save(project, file).onLeft { error ->
            log.warn("Failed to save project: {}", error::class.simpleName)
            showFailure(
                Messages["text.project.error.save.title"],
                Messages["text.project.error.save.header"],
                reasonOfProjectError(error),
            )
        }.fold({ false }, { true })
    }

    /**
     * Reads the project of [file] and hands it out.
     *
     * A document that lost a part beyond the standard ones is only handed out once the user accepted
     * that loss, which [rescueProject] asks about. Every other failure is reported and answered with
     * nothing, so the project the window shows stays where it is.
     *
     * A project that was read is checked against the fonts of this machine, which
     * [reportFontIdentity] reports. The project is handed out either way: a font that is not the one
     * it was written in changes how the manuscript is set, not whether it can be worked on.
     *
     * @param file The file to read the project from.
     * @return The project that was read, `null` when it was not.
     */
    fun loadProject(file: File): Project? {
        log.debug("Loading project {}", file.absolutePath)

        return ProjectStorage.load(file).fold({ error ->
            when (error) {
                is ProjectStorage.Error.Incomplete -> rescueProject(error)
                else -> {
                    log.warn("Failed to load project: {}", error::class.simpleName)
                    showFailure(
                        Messages["text.project.error.load.title"],
                        Messages["text.project.error.load.header"],
                        reasonOfProjectError(error),
                    )
                    null
                }
            }
        }, { it }).also { project ->
            project?.let { reportFontIdentity(it) }
        }
    }

    /**
     * Hands out a project that lost parts beyond the standard ones, but only when the user accepts
     * the loss.
     *
     * The user is told plainly what the document lost and that the loss becomes final with the next
     * save, because the project is written without those parts from then on. The parts themselves
     * are handed to the details pane of the dialog, so a long list does not push the question out of
     * sight.
     *
     * @param error The failure that carries the project that could still be read.
     * @return The project when the user accepted the loss, `null` when not.
     */
    private fun rescueProject(error: ProjectStorage.Error.Incomplete): Project? {
        log.warn("Project {} lost the part(s) {}", error.file.absolutePath, error.lostParts)

        val confirmed = AiGhostDialog.showWarningConfirmDetails(
            Messages["text.project.incomplete.title"],
            Messages["text.project.incomplete.header"],
            listOf(
                Messages["text.project.incomplete.content"],
                Messages["text.project.incomplete.hint"]
            ).joinToString(System.lineSeparator() + System.lineSeparator()),
            error.lostParts.sorted().joinToString(System.lineSeparator()) { "- $it" },
        )

        if (!confirmed) {
            log.info("The incomplete project was not opened")
            return null
        }

        return error.recovered
    }

    /**
     * Tells the user which elements of [project] are not set in the font they were written in.
     *
     * One report for all of them: six elements could otherwise mean six dialogs in a row, and the
     * elements themselves go into the details pane, the way a lost part does. A project written on
     * this machine and a project older than the fingerprint report nothing at all.
     *
     * @param project The project that was read.
     */
    private fun reportFontIdentity(project: Project) {
        val findings = FontIdentityCheck.check(project.design)
        if (findings.isEmpty()) {
            return
        }

        log.info("The project is set in {} substituted or deviating font(s)", findings.size)

        AiGhostDialog.showWarningDetails(
            Messages["text.font.substitution.title"],
            Messages["text.font.substitution.header"],
            listOf(
                Messages["text.font.substitution.content"],
                Messages["text.font.substitution.hint"]
            ).joinToString(System.lineSeparator() + System.lineSeparator()),
            findings.joinToString(System.lineSeparator()) { "- " + describe(it) },
        )
    }

    /**
     * Puts one finding into the line the details pane shows.
     *
     * @param finding The element and what is wrong with its font.
     * @return The already translated line, naming the element, the expected font and the used one.
     */
    internal fun describe(finding: FontIdentityCheck.Finding): String = when (val identity = finding.identity) {
        is FontIdentity.Substituted -> MessageFormat.format(
            Messages["text.font.substitution.substituted"],
            Messages[finding.elementKey],
            identity.requestedFamily,
            identity.substituteFamily,
        )

        is FontIdentity.Deviates -> MessageFormat.format(
            Messages["text.font.substitution.deviates"],
            Messages[finding.elementKey],
            identity.family,
        )

        // A finding is only ever built for a substituted or a deviating font.
        else -> Messages[finding.elementKey]
    }

    /**
     * Determines the reason for a project storage error and provides a user-facing message.
     *
     * @param error The project storage error that occurred.
     * @return A localized string explaining the reason for the error.
     */
    internal fun reasonOfProjectError(error: ProjectStorage.Error): String = when (error) {
        is ProjectStorage.Error.NotFound -> Messages["text.project.error.notFound"]
        is ProjectStorage.Error.NotAFile -> Messages["text.project.error.notAFile"]
        is ProjectStorage.Error.Unreadable -> Messages["text.project.error.unreadable"]
        is ProjectStorage.Error.Malformed -> Messages["text.project.error.malformed"]
        is ProjectStorage.Error.Corrupt -> Messages["text.project.error.corrupt"]
        is ProjectStorage.Error.Incomplete -> Messages["text.project.error.incomplete"]
    }
    //endregion

    //region Preferences
    /**
     * Reads the settings of the user into [preferences] and handles a failure according to
     * [handlePreferencesError].
     *
     * @param app the running application, stopped when a failure leaves nothing to work with
     */
    fun loadPreferences(app: Application) {
        log.debug("Loading preferences")

        PreferencesStorage.load().fold({ error ->
            log.warn("Failed to load preferences: {}", error::class.simpleName)
            handlePreferencesError(error, app)
        }, { loaded ->
            preferences.value = loaded
        })
    }

    /**
     * Writes [preferences] to their file and tells the user when that did not work.
     *
     * A failed write is reported but leaves the application running, because the settings the user
     * made are still in memory and another attempt may succeed.
     *
     * @return `true` when the preferences were written
     */
    fun savePreferences(): Boolean {
        log.debug("Saving preferences")

        return PreferencesStorage.save(preferences.value).onLeft { error ->
            log.warn("Failed to save preferences: {}", error::class.simpleName)
            showFailure(
                Messages["text.preferences.error.title"],
                Messages["text.preferences.error.save.header"],
                reasonOfPreferencesError(error),
            )
        }.fold({ false }, { true })
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
        is PreferencesStorage.Error.NotAFile -> Messages["text.preferences.error.notAFile"]
        is PreferencesStorage.Error.Malformed -> Messages["text.preferences.error.malformed"]
        is PreferencesStorage.Error.NotFound -> Messages["text.preferences.error.notFound"]
        is PreferencesStorage.Error.Unreadable -> Messages["text.preferences.error.unreadable"]
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

        val confirmed = AiGhostDialog.showWarningConfirm(
            Messages["text.preferences.reset.title"],
            Messages["text.preferences.reset.header"],
            Messages["text.preferences.reset.content"],
        )

        if (confirmed) {
            resetPreferences(app)
        } else {
            exitingApp(app)
        }
    }

    /** Replaces the preferences by their defaults and writes them. */
    private fun resetPreferences(app: Application) {
        log.info("Resetting preferences")

        preferences.value = Preferences()
        PreferencesStorage.save(preferences.value).onLeft {
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

        showFailure(Messages["text.preferences.error.title"], Messages["text.preferences.error.header"], reason)

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

        AiGhostDialog.showError(title, header, reason)
    }

    /**
     * Exits the application.
     */
    private fun exitingApp(app: Application) {
        log.info("Exiting application")
        app.stop()
    }

}

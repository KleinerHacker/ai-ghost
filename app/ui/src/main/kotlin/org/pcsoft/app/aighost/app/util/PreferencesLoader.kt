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

package org.pcsoft.app.aighost.app.util

import javafx.application.Application
import javafx.scene.control.Alert
import javafx.scene.control.ButtonType
import org.pcsoft.app.aighost.app.AiGhostTheme
import org.pcsoft.app.aighost.app.Messages
import org.pcsoft.app.aighost.fx.model.FXPreferencesStorage
import org.pcsoft.app.aighost.model.PreferencesStorage

/**
 * Reads the preferences of the user before the first window is shown and keeps the application
 * usable when the preferences file cannot be read.
 *
 * The loader is the only place that turns a [PreferencesStorage.Error] into something the user sees.
 * The decision which reaction an error deserves is separated from the dialogs in [reactionFor], so
 * the mapping can be checked without a UI; the dialogs themselves only carry it out.
 */
internal object PreferencesLoader {
    private val log = logger<PreferencesLoader>()

    /**
     * Loads the preferences and handles a failure according to [reactionFor].
     *
     * A successful read needs no further work, because the storage keeps the loaded preferences.
     *
     * @param app the running application, stopped when a failure leaves nothing to work with
     */
    fun loadPreferences(app: Application) {
        log.debug("Loading preferences")

        FXPreferencesStorage.load().onLeft { error ->
            log.warn("Failed to load preferences: {}", error::class.simpleName)

            when (reactionFor(error)) {
                Reaction.RESET -> resetPreferences(app)
                Reaction.ASK_FOR_RESET -> askForResetPreferences(app)
                Reaction.FAIL -> handleComplexFailure(Messages[reasonKeyOf(error)], app)
            }
        }
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
    internal fun reactionFor(error: PreferencesStorage.Error): Reaction = when (error) {
        is PreferencesStorage.Error.NotFound -> Reaction.RESET
        is PreferencesStorage.Error.Malformed -> Reaction.ASK_FOR_RESET
        is PreferencesStorage.Error.Unreadable -> Reaction.ASK_FOR_RESET
        is PreferencesStorage.Error.NotAFile -> Reaction.FAIL
    }

    /**
     * Names the bundle key describing a failure that stops the application.
     *
     * @param error the failure reported by the storage
     * @return key of the text shown as reason of the failure
     */
    internal fun reasonKeyOf(error: PreferencesStorage.Error): String = when (error) {
        is PreferencesStorage.Error.NotAFile -> "preferences.error.notAFile"
        else -> "preferences.error.unknown"
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
            val reason = when (it) {
                is PreferencesStorage.Error.Malformed -> "Dateistruktur ist fehlerhaft" // Normally not happen
                is PreferencesStorage.Error.NotAFile -> "Datei kann nicht geschrieben werden, da dieser Name als Ordner verwendet wird"
                is PreferencesStorage.Error.NotFound -> "Datei konnte nicht gefunden werden" // Normally not happen
                is PreferencesStorage.Error.Unreadable -> "Datei kann nicht gelesen / geschrieben werden"
            }
            handleComplexFailure(reason, app)
        }
    }

    /**
     * Exits the application.
     */
    private fun exitingApp(app: Application) {
        log.info("Exiting application")
        app.stop()
    }

    /**
     * Reports a failure the application cannot recover from and stops afterwards.
     *
     * @param reason already translated description of what went wrong
     * @param app the running application, stopped once the report was acknowledged
     */
    private fun handleComplexFailure(reason: String, app: Application) {
        log.debug("Inform about complex error")

        Alert(Alert.AlertType.ERROR).apply {
            title = Messages["preferences.error.title"]
            headerText = Messages["preferences.error.header"]
            contentText = reason

            buttonTypes.setAll(ButtonType(Messages["button.ok"]))
            decorate(this)
        }.showAndWait()

        exitingApp(app)
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

    /**
     * Way the loader answers a failed read of the preferences file.
     *
     * The entries are ordered by how much they cost the user: silently starting over, asking before
     * starting over, giving up.
     */
    internal enum class Reaction {
        /** The preferences are replaced by their defaults without asking; nothing can be lost. */
        RESET,

        /** The user decides whether the unreadable preferences are replaced by their defaults. */
        ASK_FOR_RESET,

        /** The application cannot help itself and stops after the failure was reported. */
        FAIL
    }
}

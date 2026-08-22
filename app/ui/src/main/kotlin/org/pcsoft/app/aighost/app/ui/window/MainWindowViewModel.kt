package org.pcsoft.app.aighost.app.ui.window

import de.saxsys.mvvmfx.ViewModel
import javafx.application.Platform
import javafx.beans.property.SimpleListProperty
import javafx.collections.FXCollections
import org.pcsoft.app.aighost.model.PreferencesStorage
import org.pcsoft.app.aighost.model.pref.Preferences
import java.io.File

/**
 * View model of the application main window.
 *
 * It follows the stored preferences while the window is shown: [openRecent] holds the files of the
 * "open recent" menu and is kept in step with the preferences through a listener that is registered
 * in [onShow] and removed again in [onHide].
 */
class MainWindowViewModel : ViewModel {

    /** Files the user opened last, the most recent one first. */
    val openRecent: SimpleListProperty<File> = SimpleListProperty(FXCollections.observableArrayList())

    // Kept as a field: a method reference would create a new instance on every call, so removing
    // the listener again would not find the one that was added.
    private val preferencesListener = PreferencesStorage.Listener { _, new -> onPreferencesChanged(new) }

    internal fun onShow() {
        onPreferencesChanged(PreferencesStorage.current)
        PreferencesStorage.addListener(preferencesListener)
    }

    internal fun onHide() {
        PreferencesStorage.removeListener(preferencesListener)
    }

    private fun onPreferencesChanged(preferences: Preferences) {
        val entries = preferences.recentOpened.entries.map(::File)

        // The storage notifies on the thread that changed the preferences, which is not necessarily
        // the FX thread, while the list is bound to the menu.
        if (Platform.isFxApplicationThread()) {
            openRecent.setAll(entries)
        } else {
            Platform.runLater { openRecent.setAll(entries) }
        }
    }
}

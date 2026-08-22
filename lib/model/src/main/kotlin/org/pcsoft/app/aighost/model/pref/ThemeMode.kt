package org.pcsoft.app.aighost.model.pref

/**
 * Visual appearance the user selected for the application.
 *
 * The value is stored inside the [Preferences] and is written to JSON by its constant name, so a
 * stored preferences file stays readable when new constants are added.
 */
enum class ThemeMode {

    /** Bright appearance, independent of the operating system setting. */
    LIGHT,

    /** Dark appearance, independent of the operating system setting. */
    DARK,

    /** Follows the appearance the operating system reports. */
    SYSTEM
}

package org.pcsoft.app.aighost.model.pref

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

/**
 * Application wide settings of a single user.
 *
 * The preferences are persisted as JSON. Unknown properties are ignored so a file written by a newer
 * version of the application can still be read, and every property carries a default so an older
 * file stays readable as well.
 *
 * @property recentOpened Files the user opened last, ten at most by default.
 * @property themeMode Visual appearance of the application, [ThemeMode.SYSTEM] by default.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class Preferences(
    val recentOpened: RecentOpened = RecentOpened(max = 10),
    val themeMode: ThemeMode = ThemeMode.SYSTEM
)

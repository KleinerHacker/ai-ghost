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

package org.pcsoft.app.aighost.model.pref

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonPropertyOrder

/**
 * Application wide settings of a single user.
 *
 * The preferences are a plain mutable value object: a setting is changed on the object itself and
 * nobody is notified about it, so whoever needs a setting reads it when it is needed.
 *
 * The preferences are persisted as JSON. Unknown properties are ignored so a file written by a newer
 * version of the application can still be read, and every property carries a default so an older
 * file stays readable as well.
 *
 * @property recentOpened Files the user opened last, ten at most by default.
 * @property themeMode Visual appearance of the application, [ThemeMode.SYSTEM] by default.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonPropertyOrder("recentOpened", "themeMode")
data class Preferences(
    var recentOpened: RecentOpened = RecentOpened(max = 10),
    var themeMode: ThemeMode = ThemeMode.SYSTEM
)

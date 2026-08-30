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

/**
 * Stores appearance-related settings for the application.
 *
 * The configuration is serialized and deserialized as JSON. Unknown fields in the JSON are ignored
 * to ensure forward compatibility with newer versions. All properties have default values to
 * maintain backward compatibility with older settings files.
 *
 * @property themeMode Specifies the visual theme to use. Defaults to [ThemeMode.SYSTEM], which follows
 * the operating system's theme setting.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class Appearance(
    var themeMode: ThemeMode = ThemeMode.SYSTEM
)

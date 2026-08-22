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

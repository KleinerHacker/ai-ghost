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

import org.pcsoft.app.aighost.plugin.manager.AiProviderRegistry

/**
 * Holds the [AiProviderRegistry] the plugin mechanism builds while the splash screen is shown.
 *
 * `PluginLoadStartupStep` fills [registry] once, on the startup background thread, before the first
 * window is shown; nothing else in the application writes to it. Until that step has run, [registry]
 * is empty rather than absent, so a caller never has to check for `null`.
 */
object PluginController {
    /** Every AI provider plugin discovered at startup, empty until `PluginLoadStartupStep` ran. */
    var registry: AiProviderRegistry = AiProviderRegistry()
        internal set
}

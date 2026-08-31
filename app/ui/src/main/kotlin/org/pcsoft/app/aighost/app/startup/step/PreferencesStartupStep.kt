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

package org.pcsoft.app.aighost.app.startup.step

import org.pcsoft.app.aighost.app.controller.IoController
import org.pcsoft.app.aighost.app.startup.StartupContext
import org.pcsoft.app.aighost.app.startup.StartupOrder
import org.pcsoft.app.aighost.app.startup.StartupStep

/**
 * Reads the settings of the user into [IoController] while the splash screen is shown.
 *
 * The file is read on the background thread. Applying the outcome - and asking the user what to do
 * when the file could not be read - happens on the FX thread, because it may show a dialog and may
 * stop the application.
 *
 * Runs first: the theme and every window read the preferences, so they must be in place before any
 * later step or the main window.
 */
@StartupOrder(0)
class PreferencesStartupStep : StartupStep {

    override val name: String = "preferences"

    override fun execute(context: StartupContext) {
        val result = IoController.readPreferences()
        context.onFxThread { IoController.applyLoadedPreferences(result, context.app) }
    }
}

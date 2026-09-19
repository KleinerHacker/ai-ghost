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

import org.pcsoft.app.aighost.app.controller.PluginController
import org.pcsoft.app.aighost.app.startup.StartupContext
import org.pcsoft.app.aighost.app.startup.StartupOrder
import org.pcsoft.app.aighost.app.startup.StartupStep
import org.pcsoft.app.aighost.app.util.logger
import org.pcsoft.app.aighost.plugin.manager.PluginManager
import java.nio.file.Path

/**
 * Discovers and loads every AI provider plugin while the splash screen is shown.
 *
 * Two directories are scanned, the built-in one first: `plugins` inside the installation directory
 * (fixed, resolved from the running JVM's own image - see [builtInPluginDirectory]) and, for now, the
 * fixed user directory `~/.ai-ghost/plugins` - making it configurable through the preferences is
 * IP-03's job, once the preferences carry the setting. [PluginManager.load] never throws: a missing
 * directory and a defective plugin are both skipped, so this step cannot fail the startup on its own.
 *
 * Runs after [PreferencesStartupStep], anticipating the day the user directory is read from the
 * preferences.
 */
@StartupOrder(1)
class PluginLoadStartupStep : StartupStep {
    private val log = logger<PluginLoadStartupStep>()

    override val name: String = "plugins"

    override fun execute(context: StartupContext) {
        val builtIn = builtInPluginDirectory()
        val user = userPluginDirectory()
        log.debug("Plugin directories: built-in '{}', user '{}'", builtIn, user)

        val registry = PluginManager.load(listOf(builtIn, user))
        context.onFxThread { PluginController.registry = registry }
    }

    /**
     * `plugins` inside the running application's own image directory.
     *
     * A jlink image carries its own minimal Java runtime, so `java.home` already points at the image
     * root - the same directory the `bin` and `lib` folders of the installation sit in.
     */
    private fun builtInPluginDirectory(): Path =
        Path.of(System.getProperty("java.home")).resolve("plugins")

    /** `~/.ai-ghost/plugins`, fixed until IP-03 makes it a preference. */
    private fun userPluginDirectory(): Path =
        Path.of(System.getProperty("user.home"), ".ai-ghost", "plugins")
}

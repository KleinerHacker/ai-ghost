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
import org.pcsoft.app.aighost.app.plugin.AiProviderRegistry
import org.pcsoft.app.aighost.app.startup.StartupContext
import org.pcsoft.app.aighost.app.startup.StartupOrder
import org.pcsoft.app.aighost.app.startup.StartupStep
import org.pcsoft.app.aighost.app.util.logger
import org.pcsoft.app.aighost.plugin.system.AiProviderExtensionConfig
import org.pcsoft.framework.pluggiat.PluginManagerConfiguration
import org.pcsoft.framework.pluggiat.scanner.PluginLocationType
import org.pcsoft.framework.pluggiat.scanner.SingleJarScanStrategy
import org.pcsoft.framework.pluggiat.security.InsecureSecurityStrategy
import java.nio.file.Files
import java.nio.file.Path
import org.pcsoft.framework.pluggiat.PluginManager as PluggiatPluginManager

/**
 * Discovers and loads every AI provider plugin while the splash screen is shown.
 *
 * Two directories are scanned, the built-in one first: `plugins` inside the installation directory
 * (fixed, resolved from the running JVM's own image - see [builtInPluginDirectory]) and, for now, the
 * fixed user directory `~/.ai-ghost/plugins` - making it configurable through the preferences is
 * IP-03's job, once the preferences carry the setting.
 *
 * Discovery, isolated loading (a parent-last class loader per plugin) and per-plugin error isolation
 * are delegated to **pluggiat**'s own [PluggiatPluginManager]; this step configures it for the two
 * plugin locations and the `"ai"` extension point declared by [AiProviderExtensionConfig]
 * (`lib/plugin/system`), and projects the scan result into an [AiProviderRegistry] via
 * [AiProviderRegistry.buildFrom]. Every location uses [InsecureSecurityStrategy] - FP-002 does not
 * call for a signature or checksum check on a provider plugin, but pluggiat requires an explicit
 * security chain for every location regardless. pluggiat's own scan throws when a configured location
 * does not exist, so [execute] filters out a missing directory itself before it ever reaches pluggiat.
 * This never throws: a missing directory and a defective plugin are both skipped, so this step cannot
 * fail the startup on its own.
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

        val registry = loadRegistry(listOf(builtIn, user))
        context.onFxThread { PluginController.registry = registry }
    }

    /**
     * Scans [directories] in order and loads every plugin JAR found in them.
     *
     * [directories] are handed to pluggiat as locations in the given order - the first one is treated
     * as [PluginLocationType.BUILTIN], every following one as [PluginLocationType.EXTERNAL] - so an
     * earlier directory's plugin wins an id collision over a later one, per
     * [AiProviderRegistry.register].
     *
     * @param directories The directories to scan, in the order their plugins take precedence. The
     *                     first entry is the built-in directory, every other entry a user directory.
     * @return A registry of every [org.pcsoft.app.aighost.plugin.api.provider.AiProvider] that could
     *         be loaded.
     */
    private fun loadRegistry(directories: List<Path>): AiProviderRegistry {
        log.debug("Searching for plugins in {}", directories)

        // The type is tied to the directory's position in the original list, not its position after
        // filtering, so a missing built-in directory never promotes a later user directory to BUILTIN.
        val existingLocations = directories
            .mapIndexed { index, directory ->
                directory to (if (index == 0) PluginLocationType.BUILTIN else PluginLocationType.EXTERNAL)
            }
            .filter { (directory, _) -> Files.isDirectory(directory) }

        val configuration = PluginManagerConfiguration().apply {
            existingLocations.forEach { (directory, locationType) ->
                location {
                    path = directory
                    type = locationType
                    scanStrategy = SingleJarScanStrategy()
                }
            }
            defaultSecurityChain {
                type = PluginLocationType.BUILTIN
                addStrategy(InsecureSecurityStrategy())
            }
            defaultSecurityChain {
                type = PluginLocationType.EXTERNAL
                addStrategy(InsecureSecurityStrategy())
            }
            extensionPoint(AiProviderExtensionConfig::class)
        }

        val pluginManager = PluggiatPluginManager(configuration)
        pluginManager.scan()

        val registry = AiProviderRegistry.buildFrom(pluginManager)
        log.info(
            "Loaded {} AI provider(s) from {} plugin director(y/ies): {}",
            registry.all.size, directories.size, registry.all.map { it.id }
        )
        return registry
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

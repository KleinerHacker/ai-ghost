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

package org.pcsoft.app.aighost.plugin.manager

import org.pcsoft.app.aighost.plugin.manager.util.logger
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.name

/**
 * Entry point of the plugin mechanism: scans a set of directories for plugin JARs and returns the
 * [AiProviderRegistry] of everything that could be loaded from them.
 *
 * This object knows neither the installation layout nor the user preferences - the directories to
 * scan are handed to [load] by its caller (the `PluginLoadStartupStep` of `app/ui`). A directory
 * that does not exist is skipped, not treated as an error; a defective or incompatible plugin is
 * logged and skipped as well, the way [PluginLoader] and [AiProviderRegistry] already do, so a
 * single broken JAR never stops the others - or the application - from starting.
 */
object PluginManager {
    private val log = logger<PluginManager>()

    /**
     * Scans [directories] in order and loads every plugin JAR found in them.
     *
     * Directories are scanned in the given order, and within a directory the JARs are scanned in
     * name order, so the result is reproducible; an earlier JAR wins an id collision over a later
     * one, per [AiProviderRegistry.register].
     *
     * @param directories The directories to scan, in the order their plugins take precedence.
     * @return A registry of every [org.pcsoft.app.aighost.plugin.api.provider.AiProvider] that could
     *         be loaded.
     */
    fun load(directories: List<Path>): AiProviderRegistry {
        log.debug("Searching for plugins in {}", directories)

        val registry = AiProviderRegistry()
        for (directory in directories) {
            if (!Files.isDirectory(directory)) {
                log.debug("Plugin directory '{}' does not exist, skipping", directory)
                continue
            }

            val jars = Files.list(directory).use { stream ->
                stream.filter { it.name.endsWith(".jar") }.sorted().toList()
            }
            log.debug("Found {} plugin JAR(s) in '{}': {}", jars.size, directory, jars.map { it.name })
            for (jar in jars) {
                loadInto(registry, jar)
            }
        }

        log.info(
            "Loaded {} AI provider(s) from {} plugin director(y/ies): {}",
            registry.all.size, directories.size, registry.all.map { it.id }
        )
        return registry
    }

    private fun loadInto(registry: AiProviderRegistry, jarPath: Path) {
        try {
            val loaded = PluginLoader.load(jarPath)
            log.info(
                "Loaded plugin '{}' from '{}' with AI provider(s) {}",
                loaded.manifest.id, jarPath, loaded.providers.map { it.id }
            )
            loaded.providers.forEach(registry::register)
        } catch (e: PluginLoadException) {
            log.warn("Skipping plugin '{}': {}", jarPath, e.message)
        } catch (e: Exception) {
            log.warn("Skipping plugin '{}': unexpected error", jarPath, e)
        }
    }
}

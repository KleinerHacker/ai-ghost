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

import org.pcsoft.app.aighost.plugin.api.manifest.PluginManifest
import org.pcsoft.app.aighost.plugin.api.provider.AiProvider
import org.pcsoft.app.aighost.plugin.api.provider.ConfigField
import org.pcsoft.app.aighost.plugin.manager.util.logger

/**
 * An [AiProvider] the plugin manager discovered and instantiated, together with what it needed to
 * do so.
 *
 * @property id Id declared in the plugin manifest's `providers.ai` entry for this provider.
 * @property provider The instantiated provider.
 * @property manifest Manifest of the plugin JAR the provider was loaded from.
 * @property configFields The provider's effective configuration field list, per
 *                         [AiProvider.configSchema] or [ConfigModelReader].
 */
data class RegisteredAiProvider(
    val id: String,
    val provider: AiProvider,
    val manifest: PluginManifest,
    val configFields: List<ConfigField>,
)

/**
 * Every [AiProvider] the plugin manager has discovered, keyed by [RegisteredAiProvider.id].
 *
 * Providers are registered in the order their plugin JARs are loaded - the built-in directory
 * before the user directory, and within a directory in the order its JARs are found. On an id
 * collision, [register] keeps the first entry and reports the second one as skipped, so a built-in
 * provider always wins over a user plugin claiming the same id, and between two user plugins the one
 * loaded first wins.
 */
class AiProviderRegistry {
    private val log = logger<AiProviderRegistry>()

    private val byId = LinkedHashMap<String, RegisteredAiProvider>()

    /** Every registered provider, in the order it was registered. */
    val all: List<RegisteredAiProvider>
        get() = byId.values.toList()

    /** The registered provider with the given [id], or `null` when no provider was registered under it. */
    fun find(id: String): RegisteredAiProvider? = byId[id]

    /**
     * Adds [entry] to the registry, unless its [RegisteredAiProvider.id] is already taken - in which
     * case the existing entry is kept and this call is a no-op beyond the log entry.
     *
     * @param entry The provider to register.
     */
    fun register(entry: RegisteredAiProvider) {
        val existing = byId.putIfAbsent(entry.id, entry)
        if (existing != null) {
            log.warn(
                "Skipping AI provider '{}' from plugin '{}': the id is already registered by plugin '{}'",
                entry.id, entry.manifest.id, existing.manifest.id
            )
        }
    }
}

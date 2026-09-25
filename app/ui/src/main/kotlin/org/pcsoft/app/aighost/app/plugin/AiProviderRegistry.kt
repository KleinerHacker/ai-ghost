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

package org.pcsoft.app.aighost.app.plugin

import org.pcsoft.app.aighost.app.util.logger
import org.pcsoft.app.aighost.plugin.api.provider.AiProvider
import org.pcsoft.app.aighost.plugin.api.provider.AiProviderContract
import org.pcsoft.app.aighost.plugin.api.provider.ConfigField
import org.pcsoft.app.aighost.plugin.system.AiProviderExtensionConfig
import org.pcsoft.framework.pluggiat.scanner.PluginScanStatus
import org.pcsoft.framework.pluggiat.PluginManager as PluggiatPluginManager

/**
 * An [AiProvider] the plugin manager discovered, together with what it needed to build it.
 *
 * Since pluggiat resolves and instantiates the [AiProvider] itself, this no longer carries the
 * plugin's full manifest - only [pluginId], the pluggiat-level id of the plugin JAR the provider was
 * loaded from, for diagnostics.
 *
 * @property id Id declared on the provider's [AiProviderExtensionConfig] entry.
 * @property provider The instantiated provider.
 * @property pluginId Id of the pluggiat plugin the provider was loaded from.
 * @property configFields The provider's effective configuration field list, per
 *                         [AiProvider.configSchema] or [ConfigModelReader].
 */
data class RegisteredAiProvider(
    val id: String,
    val provider: AiProvider,
    val pluginId: String,
    val configFields: List<ConfigField>,
)

/**
 * Every [AiProvider] the plugin manager has discovered, keyed by [RegisteredAiProvider.id].
 *
 * Providers are registered in the order pluggiat resolved their plugins - the built-in location
 * before the user location, and within a location in the order pluggiat loaded its plugins. On an id
 * collision, [register] keeps the first entry and reports the second one as skipped, so a built-in
 * provider always wins over a user plugin claiming the same id, and between two user plugins the one
 * loaded first wins. This is a rule of our own [id] (the provider's business id), independent of
 * whatever id collision handling pluggiat itself applies to its plugin-level ids.
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
                entry.id, entry.pluginId, existing.pluginId
            )
        }
    }

    companion object {
        /**
         * Builds a registry from every `"ai"` extension pluggiat resolved during [pluginManager]'s
         * last [PluggiatPluginManager.scan].
         *
         * A provider whose declared [AiProviderExtensionConfig.contractVersion] does not match
         * [AiProviderContract.VERSION] is skipped and logged, exactly like a defective plugin;
         * everything else pluggiat already isolated (a plugin that failed security, dependency or
         * loading checks) never reaches [PluggiatPluginManager.extensionsByKey] in the first place.
         *
         * @param pluginManager The pluggiat manager, already scanned.
         * @return A registry of every [AiProvider] that could be built.
         */
        fun buildFrom(pluginManager: PluggiatPluginManager): AiProviderRegistry {
            val log = logger<AiProviderRegistry>()
            val registry = AiProviderRegistry()

            // pluggiat itself already isolates a candidate that failed its manifest, security,
            // dependency or load checks - it just never reaches extensionsByKey. Logging every
            // non-LOADED scan result here is what turns pluggiat's silent skip into the "reported"
            // half of "a defective plugin is reported and skipped" (FP-002).
            for (result in pluginManager.scanResults) {
                if (result.status != PluginScanStatus.LOADED) {
                    log.warn(
                        "Skipping plugin candidate at '{}': {} ({})",
                        result.path, result.status, result.errorMessage
                    )
                }
            }

            val extensions = pluginManager.extensionsByKey["ai"].orEmpty()
            for (resolved in extensions) {
                val config = resolved.configuration as AiProviderExtensionConfig
                if (config.contractVersion != AiProviderContract.VERSION) {
                    log.warn(
                        "Skipping AI provider '{}' from plugin '{}': declares contract version {}, " +
                                "but this application supports version {}",
                        config.id, resolved.pluginId, config.contractVersion, AiProviderContract.VERSION
                    )
                    continue
                }

                val provider = resolved.instance as AiProvider
                val fields = ConfigModelReader.fieldsOf(provider)
                registry.register(RegisteredAiProvider(config.id, provider, resolved.pluginId, fields))
            }

            log.info(
                "Loaded {} AI provider(s) via pluggiat: {}", registry.all.size, registry.all.map { it.id }
            )
            return registry
        }
    }
}

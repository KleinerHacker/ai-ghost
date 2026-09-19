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

import org.pcsoft.app.aighost.plugin.api.manifest.AiProviderDeclaration
import org.pcsoft.app.aighost.plugin.api.manifest.PluginManifest
import org.pcsoft.app.aighost.plugin.api.provider.AiProvider
import org.pcsoft.app.aighost.plugin.api.provider.AiProviderContract
import org.pcsoft.app.aighost.plugin.manager.util.logger
import java.net.URLClassLoader
import java.nio.file.Path

/**
 * What [PluginLoader.load] found in a single plugin JAR: its manifest and the [AiProvider]
 * implementations it successfully instantiated.
 */
internal data class LoadedPlugin(
    val manifest: PluginManifest,
    val providers: List<RegisteredAiProvider>,
)

/**
 * Loads a single plugin JAR into an [AiProvider] contribution, isolated from every other plugin.
 *
 * Each call builds its own [URLClassLoader] for the JAR, parented on this class's own class loader
 * so the plugin's classes see [AiProvider] and the rest of `ai-ghost-plugin-api`, but nothing of the
 * application beyond it. Providers are not discovered by scanning the JAR - every
 * [PluginManifest.aiProviders] entry already carries the id, contract version and class name the
 * manager needs, so a class is loaded only once it is known to be worth loading; the class itself is
 * still checked against [PluginClassRequirements] (MUST implement [AiProvider]) before it is
 * instantiated through its public no-argument constructor. Neither a bad manifest, a contract version
 * mismatch, an unsupported configuration field type nor a provider that will not instantiate stops
 * loading altogether; each is reported through [PluginLoadException] with the exact reason and
 * skipped, at either plugin or single-provider granularity.
 */
internal object PluginLoader {
    private val log = logger<PluginLoader>()

    /**
     * Loads the plugin at [jarPath].
     *
     * @param jarPath Path of the plugin JAR.
     * @return The manifest together with every provider that could be built.
     * @throws PluginLoadException The manifest could not be read, or none of its
     *                              [PluginManifest.aiProviders] could be built into an [AiProvider].
     */
    fun load(jarPath: Path): LoadedPlugin {
        log.debug("Loading plugin JAR '{}'", jarPath)
        val classLoader = URLClassLoader(arrayOf(jarPath.toUri().toURL()), PluginLoader::class.java.classLoader)

        val manifest = try {
            PluginManifestReader.read(classLoader)
        } catch (e: PluginLoadException) {
            classLoader.close()
            throw PluginLoadException("plugin JAR '$jarPath': ${e.message}", e)
        }

        val providers = manifest.aiProviders.mapNotNull { declaration ->
            try {
                buildRegisteredProvider(declaration, classLoader, manifest)
            } catch (e: PluginLoadException) {
                log.warn("Skipping AI provider '{}' of plugin '{}': {}", declaration.id, manifest.id, e.message)
                null
            }
        }
        if (providers.isEmpty()) {
            classLoader.close()
            throw PluginLoadException("plugin '${manifest.id}' ($jarPath) has no usable AiProvider implementation")
        }

        return LoadedPlugin(manifest, providers)
    }

    private fun buildRegisteredProvider(
        declaration: AiProviderDeclaration,
        classLoader: ClassLoader,
        manifest: PluginManifest,
    ): RegisteredAiProvider {
        if (declaration.contractVersion != AiProviderContract.VERSION) {
            throw PluginLoadException(
                "provider '${declaration.id}' (class '${declaration.implementation}') declares contract " +
                        "version ${declaration.contractVersion}, but this application supports version " +
                        AiProviderContract.VERSION.toString()
            )
        }

        // A provider class MUST implement AiProvider.
        val providerClass = PluginClassRequirements.load(
            declaration.implementation, classLoader,
            requiredType = AiProvider::class.java,
        )

        val provider = try {
            providerClass.getDeclaredConstructor().newInstance()
        } catch (e: ReflectiveOperationException) {
            throw PluginLoadException("provider class '${declaration.implementation}' needs a public no-argument constructor", e)
        }

        val fields = ConfigModelReader.fieldsOf(provider)
        log.debug(
            "Loaded AI provider '{}' (class '{}') of plugin '{}', contract version {}",
            declaration.id, declaration.implementation, manifest.id, declaration.contractVersion
        )
        return RegisteredAiProvider(declaration.id, provider, manifest, fields)
    }
}

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

package org.pcsoft.app.aighost.plugin.api.manifest

/**
 * One [org.pcsoft.app.aighost.plugin.api.provider.AiProvider] a plugin declares under
 * `providers.ai` of its manifest - see [PluginManifest.aiProviders].
 *
 * A provider class itself carries no annotation naming these values; the manifest is their only
 * source, so the plugin manager never has to load a class before it can already tell what it is.
 *
 * @property id Stable, unique identifier of the provider, e.g. `"openai"`.
 * @property implementation Fully qualified class name of the [org.pcsoft.app.aighost.plugin.api.provider.AiProvider]
 *                           implementation.
 * @property name Resource bundle key of the provider's display name.
 * @property contractVersion Version of the [org.pcsoft.app.aighost.plugin.api.provider.AiProvider]
 *                            contract [implementation] was built against - see
 *                            [org.pcsoft.app.aighost.plugin.api.provider.AiProviderContract.VERSION].
 */
data class AiProviderDeclaration(
    val id: String,
    val implementation: String,
    val name: String,
    val contractVersion: Int,
)

/**
 * Metadata a plugin carries about itself, read from its `META-INF/plugin.yml` (or `.yaml`), the way
 * an IntelliJ plugin carries `plugin.xml`.
 *
 * The manifest is also where a plugin declares its contributions, grouped by kind under `providers` -
 * today only `providers.ai`, the plugin's [org.pcsoft.app.aighost.plugin.api.provider.AiProvider]
 * implementations named by [aiProviders]. The plugin manager loads exactly the classes named there,
 * nothing else; there is no bytecode scan and no annotation to read first. The accompanying JSON
 * Schema shipped in this module's resources documents the same fields for a plugin author's editor.
 *
 * @property id Stable, unique identifier of the plugin.
 * @property name Display name of the plugin.
 * @property description Short description of what the plugin does, `null` when the plugin gives
 *                        none.
 * @property icon The plugin's icon, Base64-encoded; the image format (SVG, PNG or JPG) is not named
 *                 separately, it is recognized from the decoded bytes. `null` when the plugin ships
 *                 no icon.
 * @property author Author of the plugin, `null` when the plugin names none.
 * @property version Version of the plugin itself.
 * @property copyright Copyright notice of the plugin, `null` when the plugin gives none.
 * @property aiProviders The plugin's `providers.ai` declarations, in the order the plugin manager
 *                        tries to load them. Never empty.
 */
data class PluginManifest(
    val id: String,
    val name: String,
    val description: String?,
    val icon: String?,
    val author: String?,
    val version: String,
    val copyright: String?,
    val aiProviders: List<AiProviderDeclaration>,
)

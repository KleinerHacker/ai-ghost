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

package org.pcsoft.app.aighost.plugin.system

import org.pcsoft.app.aighost.plugin.api.provider.AiProvider
import org.pcsoft.framework.pluggiat.extension.ExtensionConfiguration
import org.pcsoft.framework.pluggiat.extension.ExtensionPoint
import kotlin.reflect.KClass

/**
 * The host-side declaration of the `"ai"` extension point pluggiat resolves a plugin's
 * `extensions.ai[]` manifest entries against.
 *
 * A plugin contributes an [AiProvider] by naming its implementation class under `extensions.ai[]` in
 * its `META-INF/plugin.yml`, next to the fields declared here:
 *
 * ```yaml
 * extensions:
 *   ai:
 *     - implementation: com.example.MyProvider
 *       id: my-provider
 *       name: text.myProvider.name
 *       contractVersion: 1
 * ```
 *
 * [id] is the provider's own, stable id (used by [AiProviderRegistry] to detect a collision between
 * two providers, independent of the pluggiat-level plugin id), [name] is the resource bundle key of
 * the provider's display name, and [contractVersion] is the [AiProvider] contract version the
 * provider was built against - checked by [AiProviderRegistry] against
 * [org.pcsoft.app.aighost.plugin.api.provider.AiProviderContract.VERSION], since pluggiat's own
 * `minVersion` field only expresses host-version compatibility for the whole plugin, not a per-entry
 * contract version.
 *
 * Not `exclusive`: several plugins are meant to contribute AI providers side by side.
 *
 * @property implementation The [AiProvider] implementation class, resolved by pluggiat itself from
 *                           the manifest entry's `implementation` field (a fully qualified class
 *                           name) - [ExtensionConfiguration.implementation] is typed as a [KClass],
 *                           not the raw manifest string.
 * @property id Stable, unique identifier of the provider, e.g. `"openai"`.
 * @property name Resource bundle key of the provider's display name.
 * @property contractVersion Version of the [AiProvider] contract [implementation] was built against.
 */
@ExtensionPoint(key = "ai", exclusive = false)
data class AiProviderExtensionConfig(
    override val implementation: KClass<out AiProvider>,
    val id: String,
    val name: String,
    val contractVersion: Int,
) : ExtensionConfiguration<AiProvider>

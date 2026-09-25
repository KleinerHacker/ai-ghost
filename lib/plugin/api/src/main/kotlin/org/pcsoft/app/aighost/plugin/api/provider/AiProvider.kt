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

package org.pcsoft.app.aighost.plugin.api.provider

import kotlin.reflect.KClass

/**
 * The connection a plugin offers to a large language model.
 *
 * A provider takes a system prompt and a user prompt and answers with a stream of text - nothing
 * more. It builds no prompt, knows no paragraph or chapter, checks no character limit and splits no
 * answer into parts; that orchestration lives on the application side of a later feature. An
 * implementing class carries no annotation of its own - its id, display name and contract version
 * come from the plugin's `extensions.ai[]` manifest entry (see `AiProviderExtensionConfig` in
 * `ai-ghost-plugin-system`), so the host can identify it and check its contract version before ever
 * calling into it.
 *
 * A configuration model is named through [configType]; the host reflects on its
 * [AiProviderConfigField]-annotated properties to build the form the application shows for this
 * provider, unless [configSchema] is overridden to build the field list by hand.
 */
interface AiProvider {

    /** The [AiProviderConfig] class describing this provider's configuration. */
    val configType: KClass<out AiProviderConfig>

    /**
     * Builds the configuration field list for this provider by hand.
     *
     * The default of `null` tells the host to derive the list from the
     * [AiProviderConfigField]-annotated properties of [configType] instead. A provider overrides
     * this only when the annotated properties alone cannot express its configuration - a computed
     * default or a field that only makes sense under a condition, for instance.
     *
     * @return The configuration field list, or `null` to derive it from [configType].
     */
    fun configSchema(): List<ConfigField>? = null

    /**
     * Starts answering [userPrompt], reporting the result to [callback] as it streams in.
     *
     * @param systemPrompt The system prompt to send to the model.
     * @param userPrompt The user prompt to send to the model.
     * @param callback Receives the streamed result.
     * @return A handle that can cancel this call.
     */
    fun generate(systemPrompt: String, userPrompt: String, callback: AiProviderCallback): AiProviderHandle
}

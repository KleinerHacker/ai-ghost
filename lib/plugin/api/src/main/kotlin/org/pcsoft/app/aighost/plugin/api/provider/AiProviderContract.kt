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

/**
 * The version of the [AiProvider] contract shipped by this build of `ai-ghost-plugin-api`.
 *
 * [VERSION] is separate from the artefact version of `ai-ghost-plugin-api`, which is not versioned
 * on its own - it only rises when [AiProvider], [AiProviderCallback] or [AiProviderHandle] change in
 * a way that breaks a plugin built against the previous shape. A plugin declares the version its
 * provider was built against as `contractVersion` on its `extensions.ai[]` manifest entry (see
 * `AiProviderExtensionConfig` in `ai-ghost-plugin-system`); the host rejects a provider whose
 * declared version does not match [VERSION].
 */
object AiProviderContract {
    /** Current version of the [AiProvider] contract. */
    const val VERSION: Int = 1
}

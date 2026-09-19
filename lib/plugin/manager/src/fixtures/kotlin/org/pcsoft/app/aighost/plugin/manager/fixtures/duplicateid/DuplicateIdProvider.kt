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

package org.pcsoft.app.aighost.plugin.manager.fixtures.duplicateid

import org.pcsoft.app.aighost.plugin.api.provider.AiProvider
import org.pcsoft.app.aighost.plugin.api.provider.AiProviderCallback
import org.pcsoft.app.aighost.plugin.api.provider.AiProviderConfig
import org.pcsoft.app.aighost.plugin.api.provider.AiProviderHandle
import kotlin.reflect.KClass

/** Minimal configuration model for [DuplicateIdProvider]. */
class DuplicateIdProviderConfig : AiProviderConfig

/**
 * A second, independently built plugin whose manifest declares the same provider id as the "good"
 * fixture (`"good"`), used to prove the "first loaded wins" rule between two plugins of the same
 * directory.
 */
class DuplicateIdProvider : AiProvider {
    override val configType: KClass<out AiProviderConfig> = DuplicateIdProviderConfig::class

    override fun generate(systemPrompt: String, userPrompt: String, callback: AiProviderCallback): AiProviderHandle {
        callback.onComplete()
        return AiProviderHandle { }
    }
}

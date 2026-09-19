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

package org.pcsoft.app.aighost.plugin.manager.fixtures.missingmanifest

import org.pcsoft.app.aighost.plugin.api.provider.AiProvider
import org.pcsoft.app.aighost.plugin.api.provider.AiProviderCallback
import org.pcsoft.app.aighost.plugin.api.provider.AiProviderConfig
import org.pcsoft.app.aighost.plugin.api.provider.AiProviderHandle
import kotlin.reflect.KClass

/** Minimal configuration model for [MissingManifestProvider]. */
class MissingManifestProviderConfig : AiProviderConfig

/**
 * An otherwise valid provider whose plugin JAR ships no `META-INF/plugin.yml` at all - this
 * fixture's Gradle jar task deliberately packages no resources.
 */
class MissingManifestProvider : AiProvider {
    override val configType: KClass<out AiProviderConfig> = MissingManifestProviderConfig::class

    override fun generate(systemPrompt: String, userPrompt: String, callback: AiProviderCallback): AiProviderHandle {
        callback.onComplete()
        return AiProviderHandle { }
    }
}

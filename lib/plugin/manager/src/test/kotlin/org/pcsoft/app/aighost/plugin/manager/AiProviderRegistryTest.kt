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

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Test
import org.pcsoft.app.aighost.plugin.api.manifest.PluginManifest
import org.pcsoft.app.aighost.plugin.api.provider.AiProvider
import org.pcsoft.app.aighost.plugin.api.provider.AiProviderCallback
import org.pcsoft.app.aighost.plugin.api.provider.AiProviderConfig
import org.pcsoft.app.aighost.plugin.api.provider.AiProviderHandle
import kotlin.reflect.KClass

class AiProviderRegistryTest {

    private class DummyConfig : AiProviderConfig
    private class DummyProvider : AiProvider {
        override val configType: KClass<out AiProviderConfig> = DummyConfig::class
        override fun generate(systemPrompt: String, userPrompt: String, callback: AiProviderCallback): AiProviderHandle {
            callback.onComplete()
            return AiProviderHandle { }
        }
    }

    private fun entry(id: String, manifestId: String = id) = RegisteredAiProvider(
        id = id,
        provider = DummyProvider(),
        manifest = PluginManifest(manifestId, manifestId, null, null, null, "1.0", null, emptyList()),
        configFields = emptyList(),
    )

    /** A freshly registered provider is found again by its id and appears in [AiProviderRegistry.all]. */
    @Test
    fun `register makes a provider findable by its id`() {
        val registry = AiProviderRegistry()
        val provider = entry("stub")

        registry.register(provider)

        assertSame(provider, registry.find("stub"))
        assertEquals(listOf(provider), registry.all)
    }

    /** An unknown id is answered with `null`, not an exception. */
    @Test
    fun `find answers null for an unknown id`() {
        val registry = AiProviderRegistry()

        assertNull(registry.find("unknown"))
    }

    /** On an id collision, the entry registered first is kept and the later one is dropped. */
    @Test
    fun `register keeps the first entry on an id collision`() {
        val registry = AiProviderRegistry()
        val first = entry("stub", manifestId = "built-in-stub")
        val second = entry("stub", manifestId = "user-stub")

        registry.register(first)
        registry.register(second)

        assertSame(first, registry.find("stub"))
        assertEquals(1, registry.all.size)
    }
}

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

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.pcsoft.app.aighost.plugin.api.provider.AiProvider
import org.pcsoft.app.aighost.plugin.api.provider.AiProviderCallback
import org.pcsoft.app.aighost.plugin.api.provider.AiProviderConfig
import org.pcsoft.app.aighost.plugin.api.provider.AiProviderConfigField
import org.pcsoft.app.aighost.plugin.api.provider.AiProviderHandle
import org.pcsoft.app.aighost.plugin.api.provider.ConfigField
import org.pcsoft.app.aighost.plugin.api.provider.ConfigFieldType
import kotlin.reflect.KClass

class ConfigModelReaderTest {

    enum class Mode { FAST, SLOW }

    class AnnotatedConfig : AiProviderConfig {
        @AiProviderConfigField(label = "text.name.label", required = true, order = 0)
        var name: String = "default-name"

        @AiProviderConfigField(label = "text.enabled.label", order = 1)
        var enabled: Boolean = true

        @AiProviderConfigField(label = "text.mode.label", order = 2)
        var mode: Mode = Mode.FAST
    }

    class AnnotatedConfigProvider : AiProvider {
        override val configType: KClass<out AiProviderConfig> = AnnotatedConfig::class
        override fun generate(systemPrompt: String, userPrompt: String, callback: AiProviderCallback): AiProviderHandle {
            callback.onComplete()
            return AiProviderHandle { }
        }
    }

    class OverriddenSchemaProvider : AiProvider {
        override val configType: KClass<out AiProviderConfig> = AnnotatedConfig::class
        override fun configSchema(): List<ConfigField> = listOf(
            ConfigField("custom", ConfigFieldType.STRING, "x", "text.custom.label", "", false, false, 0)
        )
        override fun generate(systemPrompt: String, userPrompt: String, callback: AiProviderCallback): AiProviderHandle {
            callback.onComplete()
            return AiProviderHandle { }
        }
    }

    class UnsupportedTypeConfig : AiProviderConfig {
        @AiProviderConfigField(label = "text.values.label")
        var values: List<String> = emptyList()
    }

    class UnsupportedTypeProvider : AiProvider {
        override val configType: KClass<out AiProviderConfig> = UnsupportedTypeConfig::class
        override fun generate(systemPrompt: String, userPrompt: String, callback: AiProviderCallback): AiProviderHandle {
            callback.onComplete()
            return AiProviderHandle { }
        }
    }

    /** Every `@AiProviderConfigField`-annotated property is reflected into a [ConfigField], ordered and typed correctly. */
    @Test
    fun `fieldsOf reflects annotated properties into ordered, typed config fields`() {
        val fields = ConfigModelReader.fieldsOf(AnnotatedConfigProvider())

        assertEquals(
            listOf("name" to ConfigFieldType.STRING, "enabled" to ConfigFieldType.BOOLEAN, "mode" to ConfigFieldType.ENUM),
            fields.map { it.name to it.type }
        )
        assertEquals("default-name", fields.first { it.name == "name" }.defaultValue)
    }

    /** A provider overriding `configSchema()` has that list used as-is, without reflection. */
    @Test
    fun `fieldsOf prefers configSchema over reflection`() {
        val fields = ConfigModelReader.fieldsOf(OverriddenSchemaProvider())

        assertEquals(listOf("custom"), fields.map { it.name })
    }

    /** A property whose type is neither a primitive nor an enum is reported with a detailed reason. */
    @Test
    fun `fieldsOf throws a detailed exception for an unsupported field type`() {
        val exception = assertThrows(PluginLoadException::class.java) {
            ConfigModelReader.fieldsOf(UnsupportedTypeProvider())
        }

        assertEquals(true, exception.message?.contains("values"))
        assertEquals(true, exception.message?.contains("unsupported"))
    }

    /** Filling an instance from a value map applies every given value and falls back to the default otherwise. */
    @Test
    fun `fillInstance applies given values and falls back to defaults`() {
        val fields = ConfigModelReader.fieldsOf(AnnotatedConfigProvider())

        val instance = ConfigModelReader.fillInstance(
            AnnotatedConfig::class, fields, mapOf("name" to "custom-name")
        ) as AnnotatedConfig

        assertEquals("custom-name", instance.name)
        assertEquals(true, instance.enabled)
    }

    /** Reading an instance back produces the same values it was filled with - the inverse of [ConfigModelReader.fillInstance]. */
    @Test
    fun `readInstance is the inverse of fillInstance`() {
        val fields = ConfigModelReader.fieldsOf(AnnotatedConfigProvider())
        val values = mapOf("name" to "roundtrip", "enabled" to false, "mode" to Mode.SLOW)

        val instance = ConfigModelReader.fillInstance(AnnotatedConfig::class, fields, values)
        val readBack = ConfigModelReader.readInstance(AnnotatedConfig::class, fields, instance)

        assertEquals(values, readBack)
    }
}

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

import org.pcsoft.app.aighost.plugin.api.provider.AiProvider
import org.pcsoft.app.aighost.plugin.api.provider.AiProviderConfig
import org.pcsoft.app.aighost.plugin.api.provider.AiProviderConfigField
import org.pcsoft.app.aighost.plugin.api.provider.ConfigField
import org.pcsoft.app.aighost.plugin.api.provider.ConfigFieldType
import kotlin.reflect.KClass
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.KProperty1
import kotlin.reflect.full.createInstance
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.memberProperties

/**
 * Derives a provider's [ConfigField] list, and fills or reads a configuration instance from it,
 * without Jackson - just [kotlin.reflect] over the provider's own [AiProviderConfig] class.
 *
 * [AiProvider.configSchema] is preferred when a provider overrides it; otherwise the
 * [AiProviderConfigField]-annotated properties of [AiProvider.configType] are reflected into the
 * same list.
 */
internal object ConfigModelReader {

    /**
     * The effective configuration field list of [provider].
     *
     * @throws PluginLoadException A property is annotated with [AiProviderConfigField] but its type
     *                              is none of `String`, `Boolean`, `Int`, `Long`, `Double` or an
     *                              enum, or [AiProvider.configType] could not be instantiated.
     */
    fun fieldsOf(provider: AiProvider): List<ConfigField> =
        provider.configSchema() ?: reflectFields(provider.configType)

    /**
     * Builds a fresh [AiProviderConfig] of [configType], with every [fields] property set from
     * [values] - or, when [values] carries no entry for it, from the field's default.
     */
    fun fillInstance(
        configType: KClass<out AiProviderConfig>,
        fields: List<ConfigField>,
        values: Map<String, Any?>,
    ): AiProviderConfig {
        val instance = newInstance(configType)
        val properties = configType.memberProperties.associateBy { it.name }
        for (field in fields) {
            @Suppress("UNCHECKED_CAST")
            val property = properties[field.name] as? KMutableProperty1<Any, Any?> ?: continue
            property.set(instance, if (values.containsKey(field.name)) values[field.name] else field.defaultValue)
        }
        return instance
    }

    /** Reads [fields] back off [instance] into a plain map, the inverse of [fillInstance]. */
    fun readInstance(
        configType: KClass<out AiProviderConfig>,
        fields: List<ConfigField>,
        instance: AiProviderConfig,
    ): Map<String, Any?> {
        val properties = configType.memberProperties.associateBy { it.name }
        return fields.associate { field ->
            @Suppress("UNCHECKED_CAST")
            val property = properties[field.name] as? KProperty1<Any, Any?>
            field.name to property?.get(instance)
        }
    }

    private fun reflectFields(configType: KClass<out AiProviderConfig>): List<ConfigField> {
        val instance = newInstance(configType)
        return configType.memberProperties
            .mapNotNull { property -> property.findAnnotation<AiProviderConfigField>()?.let { property to it } }
            .map { (property, annotation) ->
                @Suppress("UNCHECKED_CAST")
                val value = (property as KProperty1<Any, *>).get(instance)
                ConfigField(
                    name = property.name,
                    type = typeOf(property, configType),
                    defaultValue = value,
                    label = annotation.label,
                    help = annotation.help,
                    secret = annotation.secret,
                    required = annotation.required,
                    order = annotation.order,
                )
            }
            .sortedWith(compareBy({ it.order }, { it.name }))
    }

    private fun typeOf(property: KProperty1<*, *>, configType: KClass<*>): ConfigFieldType {
        val classifier = property.returnType.classifier as? KClass<*>
        return when {
            classifier == String::class -> ConfigFieldType.STRING
            classifier == Boolean::class -> ConfigFieldType.BOOLEAN
            classifier == Int::class -> ConfigFieldType.INT
            classifier == Long::class -> ConfigFieldType.LONG
            classifier == Double::class -> ConfigFieldType.DOUBLE
            classifier != null && classifier.java.isEnum -> ConfigFieldType.ENUM
            else -> throw PluginLoadException(
                "configuration field '${property.name}' of '${configType.qualifiedName}' has an unsupported " +
                        "type; supported types are String, Boolean, Int, Long, Double and enum"
            )
        }
    }

    private fun newInstance(configType: KClass<out AiProviderConfig>): AiProviderConfig =
        try {
            configType.createInstance()
        } catch (e: Exception) {
            throw PluginLoadException(
                "configuration class '${configType.qualifiedName}' could not be instantiated; it needs a " +
                        "public no-argument constructor or a default value for every property",
                e
            )
        }
}

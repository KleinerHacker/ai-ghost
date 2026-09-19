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
 * One field of the generic configuration form of an [AiProvider].
 *
 * An instance is produced either by reflecting on the [AiProviderConfigField]-annotated properties
 * of a provider's `configType`, or handed back directly from [AiProvider.configSchema] when a
 * provider needs more control than the annotation gives it. Either way, the application processes
 * only this list - it never inspects a provider's configuration class itself.
 *
 * @property name Name of the configuration property this field belongs to.
 * @property type Kind of value the field holds.
 * @property defaultValue The value the property carries on a freshly built configuration instance.
 * @property label Resource bundle key of the field's caption.
 * @property help Resource bundle key of the field's help text, empty when the field needs none.
 * @property secret Whether the field holds a secret; rendered as a password field, never stored
 *                   beside the ordinary configuration.
 * @property required Whether the field must carry a value before the configuration is usable.
 * @property order Position of the field within the form, ascending, ties broken by declaration
 *                  order.
 */
data class ConfigField(
    val name: String,
    val type: ConfigFieldType,
    val defaultValue: Any?,
    val label: String,
    val help: String,
    val secret: Boolean,
    val required: Boolean,
    val order: Int,
)

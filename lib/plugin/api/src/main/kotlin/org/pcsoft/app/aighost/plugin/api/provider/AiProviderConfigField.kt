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
 * Marks a property of an [AiProviderConfig] as a field of the generic configuration form.
 *
 * The plugin manager reflects on every annotated property of a provider's `configType` to build its
 * [ConfigField] list; the property's own type decides the [ConfigFieldType]. [label] and [help] are
 * looked up in a `ResourceBundle` the plugin brings along, falling back to the key itself when no
 * bundle or entry is found.
 *
 * @property label Resource bundle key of the field's caption.
 * @property help Resource bundle key of the field's help text, empty when the field needs none.
 * @property secret Whether the field holds a secret; it is rendered as a password field and is
 *                   never stored beside the ordinary configuration.
 * @property required Whether the field must carry a value before the configuration is usable.
 * @property order Position of the field within the form, ascending, ties broken by declaration
 *                  order.
 */
@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
annotation class AiProviderConfigField(
    val label: String,
    val help: String = "",
    val secret: Boolean = false,
    val required: Boolean = false,
    val order: Int = 0,
)

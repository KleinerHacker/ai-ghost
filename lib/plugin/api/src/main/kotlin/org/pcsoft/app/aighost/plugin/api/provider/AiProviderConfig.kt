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
 * Marks a plain Kotlin class as the configuration model of an [AiProvider].
 *
 * A configuration class needs a public no-argument constructor, or every property with a default
 * value, so the plugin manager can build an empty instance and fill it. Every property meant to
 * appear in the generic configuration form carries [AiProviderConfigField].
 */
interface AiProviderConfig

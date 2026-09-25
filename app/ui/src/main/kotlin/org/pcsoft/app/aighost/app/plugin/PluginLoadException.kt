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

/**
 * Reason a single AI provider's configuration could not be built.
 *
 * Everything about loading, scanning and isolating a plugin JAR itself is pluggiat's own concern and
 * reported through its own types; this exception is left for what stays app-specific -
 * [ConfigModelReader] finding a configuration property of an unsupported type, or its configuration
 * class failing to instantiate.
 */
class PluginLoadException(message: String, cause: Throwable? = null) : Exception(message, cause)

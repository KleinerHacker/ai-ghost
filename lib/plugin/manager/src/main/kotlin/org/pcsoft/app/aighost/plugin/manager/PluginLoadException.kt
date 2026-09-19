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

/**
 * Reason a single plugin JAR could not be loaded or registered.
 *
 * Thrown by [PluginLoader] and [PluginManifestReader] for anything specific to the plugin being
 * loaded - a missing manifest, a bad field type, a provider that would not instantiate - as opposed
 * to a lower level [java.io.IOException] from the JAR itself. [PluginManager] catches both alike and
 * skips the plugin, but [message] is always detailed enough to tell a user or a log reader exactly
 * what was wrong with it.
 */
class PluginLoadException(message: String, cause: Throwable? = null) : Exception(message, cause)

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

module org.pcsoft.app.aighost.plugin.manager {
    requires kotlin.stdlib;
    // ConfigModelReader reflects on a provider's configuration class.
    requires kotlin.reflect;

    // AiProviderRegistry hands out AiProvider and PluginManifest instances, so consumers need the
    // plugin API as well.
    requires transitive org.pcsoft.app.aighost.plugin.api;

    // PluginManifestReader parses a plugin's META-INF/plugin.yml; kept internal, not re-exported.
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.dataformat.yaml;
    requires com.fasterxml.jackson.kotlin;

    requires org.slf4j;

    exports org.pcsoft.app.aighost.plugin.manager;
}

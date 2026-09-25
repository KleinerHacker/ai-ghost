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

module org.pcsoft.app.aighost.plugin.system {
    requires kotlin.stdlib;

    // AiProviderExtensionConfig hands out AiProvider instances, so consumers need the plugin API too.
    requires transitive org.pcsoft.app.aighost.plugin.api;

    // AiProviderExtensionConfig is annotated with pluggiat's extension point mechanism (IP-10). The
    // published JAR carries no Automatic-Module-Name manifest attribute and no module-info.class of
    // its own, so its automatic module name is derived from the JAR file name (`pluggiat-<version>.jar`
    // minus the version suffix), not from the Maven group.
    requires pluggiat;

    exports org.pcsoft.app.aighost.plugin.system;
}

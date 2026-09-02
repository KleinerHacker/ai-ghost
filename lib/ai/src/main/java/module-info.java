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

@SuppressWarnings("requires-transitive-automatic")
module org.pcsoft.app.aighost.ai {
    requires kotlin.stdlib;

    // The limit check takes Preferences.Ai, so consumers need the model as well.
    requires transitive org.pcsoft.app.aighost.model;

    // Arrow's Either appears in the signatures of the action port.
    requires transitive arrow.core;

    requires org.slf4j;

    exports org.pcsoft.app.aighost.ai.util;
    exports org.pcsoft.app.aighost.ai.action;
}

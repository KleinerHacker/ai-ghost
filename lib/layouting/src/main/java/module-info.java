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

module org.pcsoft.app.aighost.layouting {
    requires kotlin.stdlib;

    // Everything the layout core offers is its public surface: the measuring interface, the style
    // and block types, the breaking step and the laid out result.
    exports org.pcsoft.app.aighost.layouting;
}

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

module org.pcsoft.app.aighost.layouting.fx {
    requires kotlin.stdlib;

    // The blocks and the laid out result of the core are handed in and read back out by every
    // renderer, so a consumer sees those types in the signatures.
    requires transitive org.pcsoft.app.aighost.layouting;

    // The surface drawn onto and the nodes handed out are JavaFX types.
    requires transitive javafx.graphics;
    requires transitive javafx.controls;

    // Nothing is exported yet: the renderer and its package arrive with the following plan, this
    // module carries the toolkit resolution and the headless test setup only.
}

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

module org.pcsoft.app.aighost.layouting.model {
    requires kotlin.stdlib;

    // The document model goes into the builders, the pages of the simPlay raw model come out of them,
    // so consumers see both types in every signature.
    requires transitive org.pcsoft.app.aighost.model;

    // simPlay's raw layout model (Document, Page, TextBlock, TextStyle, geometry). The name below is
    // the `Automatic-Module-Name` simPlay's engine jar sets in its manifest - it must stay in sync
    // with simPlay's build. No `requires` for kotlinx.serialization is needed: the raw model's
    // PlatformSerializable is `java.io.Serializable` on the JVM, and kotlinx-serialization-json is a
    // runtime-scoped transitive dependency only.
    requires transitive org.pcsoft.framework.simplay.engine;

    // The translation of a stored style into a layout style, and of a stored page format into a page
    // layout.
    exports org.pcsoft.app.aighost.layouting.model.common;

    // The block builders, mirroring the packages of the model parts they read.
    exports org.pcsoft.app.aighost.layouting.model.project.book;
    exports org.pcsoft.app.aighost.layouting.model.project.meta;

    // The document builder that turns a whole book into one simPlay Document.
    exports org.pcsoft.app.aighost.layouting.model.project;
}

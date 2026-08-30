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

package org.pcsoft.app.aighost.fx.model.project

import javafx.beans.property.SimpleObjectProperty
import org.pcsoft.app.aighost.plugin.api.model.project.ProjectPart

/**
 * Property wrapping one part of a project and the base every property model of a part is built on.
 *
 * The parts the application ships with use it, and so does a plugin that brings a part of its own: it
 * derives from this class, offers every field of its part as a plain Java FX property tied to that
 * field, and hands the derived class to [ProjectProperty.attachPart]. From then on the part behaves
 * like a standard one - a change reaches the project object, is reported up to the project property,
 * and an exchanged project is passed down to it.
 *
 * A derived class ties its field properties to the part by listening to itself and rebuilding the
 * adapters of `javafx.beans.property.adapter` for the part it carries now, which is what the standard
 * parts do as well.
 *
 * The wrapped part may be absent as long as no project sits above this property, so a derived class
 * answers with a neutral value and drops what is written to it until then.
 */
open class ProjectPartProperty<P : ProjectPart> : SimpleObjectProperty<P?>() {

    /**
     * Reads every field of the wrapped part again and hands what changed to the field properties.
     *
     * This is what a caller uses after writing on the part past this model: a plain part reports
     * nothing, so nobody would notice such a write otherwise. A part without fields of its own has
     * nothing to read again.
     */
    open fun refresh() = Unit

}

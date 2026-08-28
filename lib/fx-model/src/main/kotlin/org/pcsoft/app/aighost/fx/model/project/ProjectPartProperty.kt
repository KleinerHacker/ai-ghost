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

import org.pcsoft.app.aighost.fx.model.property.common.OverrideObjectProperty
import org.pcsoft.app.aighost.plugin.api.model.project.ProjectPart

/**
 * Property wrapping one part of a project and the base every property model of a part is built on.
 *
 * The parts the application ships with use it, and so does a plugin that brings a part of its own: it
 * derives from this class, offers every field of its part as a property built with the wrappers from
 * `org.pcsoft.app.aighost.fx.model.property.common`, and hands the derived class to
 * [ProjectProperty.attachPart]. From then on the part behaves like a standard one - a change reaches
 * the project object, is reported up to the project property, and an exchanged project is passed down
 * to it.
 *
 * A derived class that carries field properties of its own has to refresh them whenever the wrapped
 * part is exchanged, which means overriding both [invalidated] and [refresh].
 *
 * The wrapped part may be absent as long as no project sits above this property, so a derived class
 * answers with a neutral value and drops what is written to it until then.
 */
open class ProjectPartProperty<P : ProjectPart>(
    setter: (P?) -> Unit,
    getter: () -> P?,
    fireEvent: () -> Unit
) : OverrideObjectProperty<P?>(setter, getter, fireEvent)

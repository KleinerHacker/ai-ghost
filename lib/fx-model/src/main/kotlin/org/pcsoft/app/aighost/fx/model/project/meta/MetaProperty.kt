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

package org.pcsoft.app.aighost.fx.model.project.meta

import javafx.beans.property.SimpleStringProperty
import javafx.beans.property.StringProperty
import org.pcsoft.app.aighost.fx.model.internal.BeanFields
import org.pcsoft.app.aighost.fx.model.project.ProjectPartProperty
import org.pcsoft.app.aighost.model.project.meta.Meta

/**
 * Property wrapping the meta data of a project and offering every field of it as a property of its
 * own.
 *
 * The wrapped object may be absent as long as no project sits above this property, so every field
 * property answers with a neutral value and drops what is written to it until then.
 *
 * The version of the part is not offered as a property: it names the shape of the stored document,
 * is never written by the user and never changes while a project is open. The list of the additional
 * parts is left out for the same reason: it is bookkeeping of the storage, written on every save out
 * of the parts the project carries, and nothing the user edits.
 *
 * This property model is handed out with its own type, so a caller reaches every field of the meta
 * data directly; it is built by the project alone and therefore carries an internal constructor.
 */
class MetaProperty internal constructor() : ProjectPartProperty<Meta>() {

    private val fields = BeanFields<Meta> { fireValueChangedEvent() }

    /** Name of the project as shown to the user, as a property of its own. */
    val nameProperty: StringProperty = SimpleStringProperty()

    // A property carries a name of its own, so the accessors of the wrapped field are given another
    // name on the JVM side - otherwise they would silently replace the one of the base class.
    /** Name of the project as shown to the user. */
    var name: String?
        @JvmName("getProjectName") get() = nameProperty.get()
        @JvmName("setProjectName") set(value) {
            nameProperty.set(value)
        }

    /** Author printed in the manuscript, as a property of its own. */
    val authorProperty: StringProperty = SimpleStringProperty()

    /** Author printed in the manuscript. */
    var author: String?
        get() = authorProperty.get()
        set(value) {
            authorProperty.set(value)
        }

    init {
        fields.string(nameProperty, "name")
        fields.string(authorProperty, "author")

        // The field properties belong to another object after every exchange, so they are tied to the
        // one this property carries now.
        addListener { _, _, newValue -> fields.rebind(newValue) }
        fields.rebind(get())
    }

    override fun refresh() = fields.refresh()

}

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

import org.pcsoft.app.aighost.fx.model.internal.OverrideObjectProperty
import org.pcsoft.app.aighost.fx.model.internal.OverrideStringProperty
import org.pcsoft.app.aighost.model.project.meta.Meta

/**
 * Property wrapping the meta data of a project and offering every field of it as a property of its
 * own.
 *
 * The wrapped object may be absent as long as no project sits above this property, so every field
 * property answers with a neutral value and drops what is written to it until then.
 *
 * The version of the part is not offered as a property: it names the shape of the stored document,
 * is never written by the user and never changes while a project is open.
 */
internal class MetaProperty(
    setter: (Meta?) -> Unit,
    getter: () -> Meta?,
    fireEvent: () -> Unit
) : OverrideObjectProperty<Meta?>(setter, getter, fireEvent) {

    /** Name of the project as shown to the user, as a property of its own. */
    val nameProperty: OverrideStringProperty = OverrideStringProperty(
        { newValue -> value?.also { it.name = newValue ?: "" } },
        { value?.name },
        { fireValueChangedEvent() }
    )

    // A property carries a name of its own, so the accessors of the wrapped field are given another
    // name on the JVM side - otherwise they would silently replace the one of the base class.
    /** Name of the project as shown to the user. */
    var name: String?
        @JvmName("getProjectName") get() = nameProperty.get()
        @JvmName("setProjectName") set(value) {
            nameProperty.set(value)
        }

    /** Author printed in the manuscript, as a property of its own. */
    val authorProperty: OverrideStringProperty = OverrideStringProperty(
        { newValue -> value?.also { it.author = newValue ?: "" } },
        { value?.author },
        { fireValueChangedEvent() }
    )

    /** Author printed in the manuscript. */
    var author: String?
        get() = authorProperty.get()
        set(value) {
            authorProperty.set(value)
        }

    /** Copyright notice printed in the manuscript, as a property of its own. */
    val copyrightProperty: OverrideStringProperty = OverrideStringProperty(
        { newValue -> value?.also { it.copyright = newValue ?: "" } },
        { value?.copyright },
        { fireValueChangedEvent() }
    )

    /** Copyright notice printed in the manuscript. */
    var copyright: String?
        get() = copyrightProperty.get()
        set(value) {
            copyrightProperty.set(value)
        }

    /**
     * Called whenever the wrapped object itself is exchanged, so the properties of its fields belong
     * to another object afterwards and have to take over its values.
     */
    override fun invalidated() {
        super.invalidated()
        refreshFields()
    }

    override fun refresh() {
        super.refresh()
        refreshFields()
    }

    /**
     * Lets every field property take over the value of the object the property carries now. A field
     * whose value did not change reports nothing, so an exchanged object is not announced as a change
     * of every one of its fields.
     */
    private fun refreshFields() {
        nameProperty.refresh()
        authorProperty.refresh()
        copyrightProperty.refresh()
    }

}

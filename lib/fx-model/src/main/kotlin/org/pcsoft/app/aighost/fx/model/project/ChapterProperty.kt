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

import org.pcsoft.app.aighost.fx.model.internal.OverrideStringProperty
import org.pcsoft.app.aighost.model.project.Chapter

/**
 * Property wrapping a single chapter of a book - the one the user is working on for instance - and
 * offering every field of it as a property of its own.
 *
 * The wrapped object may be absent as long as no chapter is picked, so every field property answers
 * with a neutral value and drops what is written to it until a chapter sits behind this property.
 */
internal class ChapterProperty(
    setter: (Chapter?) -> Unit,
    getter: () -> Chapter?,
    fireEvent: () -> Unit
) : BookPartProperty<Chapter?>(setter, getter, fireEvent) {

    /** Name of the chapter as shown in the project tree, as a property of its own. */
    val nameProperty: OverrideStringProperty = OverrideStringProperty(
        { newValue -> value?.also { it.name = newValue ?: "" } },
        { value?.name },
        { fireValueChangedEvent() }
    )

    // A property carries a name of its own, so the accessors of the wrapped field are given another
    // name on the JVM side - otherwise they would silently replace the one of the base class.
    /** Name of the chapter as shown in the project tree. */
    var name: String?
        @JvmName("getChapterName") get() = nameProperty.get()
        @JvmName("setChapterName") set(value) {
            nameProperty.set(value)
        }

    override fun refreshFields() {
        super.refreshFields()
        nameProperty.refresh()
    }

}

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

package org.pcsoft.app.aighost.fx.model.project.book

import javafx.beans.property.BooleanProperty
import javafx.beans.property.SimpleBooleanProperty
import org.pcsoft.app.aighost.model.project.book.Epilog

/**
 * Property wrapping the epilog of a book and offering every field of it as a property of its own.
 *
 * A book always carries its epilog; whether that epilog belongs to the book is told by the switch
 * alone. The wrapped object is absent only as long as no book sits behind the property standing for
 * it, and every field property answers with a neutral value until then.
 *
 * This property model is handed out with its own type, so a caller reaches every field of the epilog
 * directly; it is built by the book alone and therefore carries an internal constructor.
 */
class EpilogProperty internal constructor() : BookPartProperty<Epilog?>() {

    /** Whether the epilog belongs to the book, as a property of its own. */
    val includedProperty: BooleanProperty = SimpleBooleanProperty()

    /** Whether the epilog belongs to the book. */
    var included: Boolean
        get() = includedProperty.get()
        set(value) {
            includedProperty.set(value)
        }

    init {
        fields.boolean(includedProperty, "included")
    }

}

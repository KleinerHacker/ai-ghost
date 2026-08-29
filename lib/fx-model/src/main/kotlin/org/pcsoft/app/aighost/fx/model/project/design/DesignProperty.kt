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

package org.pcsoft.app.aighost.fx.model.project.design

import javafx.beans.property.BooleanProperty
import javafx.beans.property.SimpleBooleanProperty
import org.pcsoft.app.aighost.fx.model.internal.BeanFields
import org.pcsoft.app.aighost.fx.model.project.ProjectPartProperty
import org.pcsoft.app.aighost.model.project.design.AuthorDesign
import org.pcsoft.app.aighost.model.project.design.ChapterDesign
import org.pcsoft.app.aighost.model.project.design.CopyrightDesign
import org.pcsoft.app.aighost.model.project.design.Design
import org.pcsoft.app.aighost.model.project.design.TextDesign
import org.pcsoft.app.aighost.model.project.design.TitleDesign

/**
 * Property wrapping the typographic and page settings of a project and offering every field of it -
 * and every field of the design parts nested in it - as a property of its own.
 *
 * The wrapped object may be absent as long as no project sits above this property, so every field
 * property answers with a neutral value and drops what is written to it until then.
 *
 * The version of the part is not offered as a property: it names the shape of the stored document,
 * is never written by the user and never changes while a project is open.
 */
internal class DesignProperty : ProjectPartProperty<Design>() {

    private val fields = BeanFields<Design> { fireValueChangedEvent() }

    /** Typographic settings for the author name, as a property of its own. */
    val authorDesignProperty: AuthorDesignProperty = AuthorDesignProperty()

    /** Typographic settings for the author name. */
    var authorDesign: AuthorDesign?
        get() = authorDesignProperty.get()
        set(value) {
            authorDesignProperty.set(value)
        }

    /** Typographic settings for the copyright page, as a property of its own. */
    val copyrightDesignProperty: CopyrightDesignProperty = CopyrightDesignProperty()

    /** Typographic settings for the copyright page. */
    var copyrightDesign: CopyrightDesign?
        get() = copyrightDesignProperty.get()
        set(value) {
            copyrightDesignProperty.set(value)
        }

    /** Typographic settings for the title page, as a property of its own. */
    val titleDesignProperty: TitleDesignProperty = TitleDesignProperty()

    /** Typographic settings for the title page. */
    var titleDesign: TitleDesign?
        get() = titleDesignProperty.get()
        set(value) {
            titleDesignProperty.set(value)
        }

    /** Typographic settings for chapter headings, as a property of its own. */
    val chapterDesignProperty: ChapterDesignProperty = ChapterDesignProperty()

    /** Typographic settings for chapter headings. */
    var chapterDesign: ChapterDesign?
        get() = chapterDesignProperty.get()
        set(value) {
            chapterDesignProperty.set(value)
        }

    /** Typographic settings for the body text, as a property of its own. */
    val textDesignProperty: TextDesignProperty = TextDesignProperty()

    /** Typographic settings for the body text. */
    var textDesign: TextDesign?
        get() = textDesignProperty.get()
        set(value) {
            textDesignProperty.set(value)
        }

    /** Whether the manuscript starts with an empty page, as a property of its own. */
    val startWithEmptyPageProperty: BooleanProperty = SimpleBooleanProperty()

    /** Whether the manuscript starts with an empty page. */
    var startWithEmptyPage: Boolean
        get() = startWithEmptyPageProperty.get()
        set(value) {
            startWithEmptyPageProperty.set(value)
        }

    /** Whether the manuscript ends with an empty page, as a property of its own. */
    val endWithEmptyPageProperty: BooleanProperty = SimpleBooleanProperty()

    /** Whether the manuscript ends with an empty page. */
    var endWithEmptyPage: Boolean
        get() = endWithEmptyPageProperty.get()
        set(value) {
            endWithEmptyPageProperty.set(value)
        }

    init {
        fields.model(authorDesignProperty, "authorDesign", authorDesignProperty::refresh)
        fields.model(copyrightDesignProperty, "copyrightDesign", copyrightDesignProperty::refresh)
        fields.model(titleDesignProperty, "titleDesign", titleDesignProperty::refresh)
        fields.model(chapterDesignProperty, "chapterDesign", chapterDesignProperty::refresh)
        fields.model(textDesignProperty, "textDesign", textDesignProperty::refresh)
        fields.boolean(startWithEmptyPageProperty, "startWithEmptyPage")
        fields.boolean(endWithEmptyPageProperty, "endWithEmptyPage")

        // The field properties belong to another object after every exchange, so they are tied to the
        // one this property carries now.
        addListener { _, _, newValue -> fields.rebind(newValue) }
        fields.rebind(get())
    }

    override fun refresh() = fields.refresh()

}

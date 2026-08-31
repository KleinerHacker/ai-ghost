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
import org.pcsoft.app.aighost.model.project.design.BlurbPageDesign
import org.pcsoft.app.aighost.model.project.design.ChapterPageDesign
import org.pcsoft.app.aighost.model.project.design.CopyrightPageDesign
import org.pcsoft.app.aighost.model.project.design.Design
import org.pcsoft.app.aighost.model.project.design.EpilogPageDesign
import org.pcsoft.app.aighost.model.project.design.PageFormat
import org.pcsoft.app.aighost.model.project.design.PrologPageDesign
import org.pcsoft.app.aighost.model.project.design.TitlePageDesign

/**
 * Property wrapping the typographic and page settings of a project and offering every field of it -
 * and every field of the page format and the page designs nested in it - as a property of its own.
 *
 * The settings are grouped by the page they apply to: the title page, the copyright page, the prolog,
 * the blurb, the chapters and the epilog each carry the styles of their texts, and every one of them
 * is reached through a property model of its own.
 *
 * The wrapped object may be absent as long as no project sits above this property, so every field
 * property answers with a neutral value and drops what is written to it until then.
 *
 * The version of the part is not offered as a property: it names the shape of the stored document,
 * is never written by the user and never changes while a project is open.
 *
 * This property model is handed out with its own type, so a caller reaches every page design below it
 * directly; it is built by the project alone and therefore carries an internal constructor.
 */
class DesignProperty internal constructor() : ProjectPartProperty<Design>() {

    private val fields = BeanFields<Design> { fireValueChangedEvent() }

    /** Size of a page and the empty space on its four sides, as a property of its own. */
    val pageFormatProperty: PageFormatProperty = PageFormatProperty()

    /** Size of a page and the empty space on its four sides. */
    var pageFormat: PageFormat?
        get() = pageFormatProperty.get()
        set(value) {
            pageFormatProperty.set(value)
        }

    /** Typographic settings for the title page, as a property of its own. */
    val titlePageProperty: TitlePageDesignProperty = TitlePageDesignProperty()

    /** Typographic settings for the title page. */
    var titlePage: TitlePageDesign?
        get() = titlePageProperty.get()
        set(value) {
            titlePageProperty.set(value)
        }

    /** Typographic settings for the copyright page, as a property of its own. */
    val copyrightPageProperty: CopyrightPageDesignProperty = CopyrightPageDesignProperty()

    /** Typographic settings for the copyright page. */
    var copyrightPage: CopyrightPageDesign?
        get() = copyrightPageProperty.get()
        set(value) {
            copyrightPageProperty.set(value)
        }

    /** Typographic settings for the prolog page, as a property of its own. */
    val prologPageProperty: PrologPageDesignProperty = PrologPageDesignProperty()

    /** Typographic settings for the prolog page. */
    var prologPage: PrologPageDesign?
        get() = prologPageProperty.get()
        set(value) {
            prologPageProperty.set(value)
        }

    /** Typographic settings for the blurb page, as a property of its own. */
    val blurbPageProperty: BlurbPageDesignProperty = BlurbPageDesignProperty()

    /** Typographic settings for the blurb page. */
    var blurbPage: BlurbPageDesign?
        get() = blurbPageProperty.get()
        set(value) {
            blurbPageProperty.set(value)
        }

    /** Typographic settings for the chapter pages, as a property of its own. */
    val chapterPageProperty: ChapterPageDesignProperty = ChapterPageDesignProperty()

    /** Typographic settings for the chapter pages. */
    var chapterPage: ChapterPageDesign?
        get() = chapterPageProperty.get()
        set(value) {
            chapterPageProperty.set(value)
        }

    /** Typographic settings for the epilog page, as a property of its own. */
    val epilogPageProperty: EpilogPageDesignProperty = EpilogPageDesignProperty()

    /** Typographic settings for the epilog page. */
    var epilogPage: EpilogPageDesign?
        get() = epilogPageProperty.get()
        set(value) {
            epilogPageProperty.set(value)
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
        fields.model(pageFormatProperty, "pageFormat", pageFormatProperty::refresh)
        fields.model(titlePageProperty, "titlePage", titlePageProperty::refresh)
        fields.model(copyrightPageProperty, "copyrightPage", copyrightPageProperty::refresh)
        fields.model(prologPageProperty, "prologPage", prologPageProperty::refresh)
        fields.model(blurbPageProperty, "blurbPage", blurbPageProperty::refresh)
        fields.model(chapterPageProperty, "chapterPage", chapterPageProperty::refresh)
        fields.model(epilogPageProperty, "epilogPage", epilogPageProperty::refresh)
        fields.boolean(startWithEmptyPageProperty, "startWithEmptyPage")
        fields.boolean(endWithEmptyPageProperty, "endWithEmptyPage")

        // The field properties belong to another object after every exchange, so they are tied to the
        // one this property carries now.
        addListener { _, _, newValue -> fields.rebind(newValue) }
        fields.rebind(get())
    }

    override fun refresh() = fields.refresh()

}

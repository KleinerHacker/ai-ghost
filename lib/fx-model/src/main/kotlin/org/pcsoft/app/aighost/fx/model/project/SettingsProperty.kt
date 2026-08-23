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

import org.pcsoft.app.aighost.fx.model.common.StyleDataProperty
import org.pcsoft.app.aighost.fx.model.internal.OverrideBooleanProperty
import org.pcsoft.app.aighost.fx.model.internal.OverrideObjectProperty
import org.pcsoft.app.aighost.model.common.StyleData
import org.pcsoft.app.aighost.model.project.Project

/**
 * Property wrapping the typographic and page settings of a project and offering every field of it -
 * and every field of the styles nested in it - as a property of its own.
 *
 * The wrapped object may be absent as long as no project sits above this property, so every field
 * property answers with a neutral value and drops what is written to it until then.
 */
internal class SettingsProperty(
    setter: (Project.Settings?) -> Unit,
    getter: () -> Project.Settings?,
    fireEvent: () -> Unit
) : OverrideObjectProperty<Project.Settings?>(setter, getter, fireEvent) {

    /** Style of the author name, as a property of its own. */
    val authorFontProperty: StyleDataProperty = styleProperty(
        { settings, style -> settings.authorFont = style },
        { it.authorFont }
    )

    /** Style of the author name. */
    var authorFont: StyleData?
        get() = authorFontProperty.get()
        set(value) {
            authorFontProperty.set(value)
        }

    /** Style of the copyright notice, as a property of its own. */
    val copyrightFontProperty: StyleDataProperty = styleProperty(
        { settings, style -> settings.copyrightFont = style },
        { it.copyrightFont }
    )

    /** Style of the copyright notice. */
    var copyrightFont: StyleData?
        get() = copyrightFontProperty.get()
        set(value) {
            copyrightFontProperty.set(value)
        }

    /** Style of the book title, as a property of its own. */
    val titleFontProperty: StyleDataProperty = styleProperty(
        { settings, style -> settings.titleFont = style },
        { it.titleFont }
    )

    /** Style of the book title. */
    var titleFont: StyleData?
        get() = titleFontProperty.get()
        set(value) {
            titleFontProperty.set(value)
        }

    /** Style of the further title lines, as a property of its own. */
    val titleAppendixFontProperty: StyleDataProperty = styleProperty(
        { settings, style -> settings.titleAppendixFont = style },
        { it.titleAppendixFont }
    )

    /** Style of the further title lines. */
    var titleAppendixFont: StyleData?
        get() = titleAppendixFontProperty.get()
        set(value) {
            titleAppendixFontProperty.set(value)
        }

    /** Style of a chapter heading, as a property of its own. */
    val chapterFontProperty: StyleDataProperty = styleProperty(
        { settings, style -> settings.chapterFont = style },
        { it.chapterFont }
    )

    /** Style of a chapter heading. */
    var chapterFont: StyleData?
        get() = chapterFontProperty.get()
        set(value) {
            chapterFontProperty.set(value)
        }

    /** Style of the further chapter heading lines, as a property of its own. */
    val chapterAppendixFontProperty: StyleDataProperty = styleProperty(
        { settings, style -> settings.chapterAppendixFont = style },
        { it.chapterAppendixFont }
    )

    /** Style of the further chapter heading lines. */
    var chapterAppendixFont: StyleData?
        get() = chapterAppendixFontProperty.get()
        set(value) {
            chapterAppendixFontProperty.set(value)
        }

    /** Style of the chapter text, as a property of its own. */
    val textFontProperty: StyleDataProperty = styleProperty(
        { settings, style -> settings.textFont = style },
        { it.textFont }
    )

    /** Style of the chapter text. */
    var textFont: StyleData?
        get() = textFontProperty.get()
        set(value) {
            textFontProperty.set(value)
        }

    /** Whether a separate copyright page is printed, as a property of its own. */
    val copyrightPageProperty: OverrideBooleanProperty = OverrideBooleanProperty(
        { newValue -> value?.also { it.copyrightPage = newValue } },
        { value?.copyrightPage ?: false },
        { fireValueChangedEvent() }
    )

    /** Whether a separate copyright page is printed. */
    var copyrightPage: Boolean
        get() = copyrightPageProperty.get()
        set(value) {
            copyrightPageProperty.set(value)
        }

    /** Whether the manuscript starts with an empty page, as a property of its own. */
    val startWithEmptyPageProperty: OverrideBooleanProperty = OverrideBooleanProperty(
        { newValue -> value?.also { it.startWithEmptyPage = newValue } },
        { value?.startWithEmptyPage ?: false },
        { fireValueChangedEvent() }
    )

    /** Whether the manuscript starts with an empty page. */
    var startWithEmptyPage: Boolean
        get() = startWithEmptyPageProperty.get()
        set(value) {
            startWithEmptyPageProperty.set(value)
        }

    /** Whether the manuscript ends with an empty page, as a property of its own. */
    val endWithEmptyPageProperty: OverrideBooleanProperty = OverrideBooleanProperty(
        { newValue -> value?.also { it.endWithEmptyPage = newValue } },
        { value?.endWithEmptyPage ?: false },
        { fireValueChangedEvent() }
    )

    /** Whether the manuscript ends with an empty page. */
    var endWithEmptyPage: Boolean
        get() = endWithEmptyPageProperty.get()
        set(value) {
            endWithEmptyPageProperty.set(value)
        }

    /**
     * Builds the property of one of the styles the settings carry. Every one of them is wrapped the
     * same way, so only the field it works on differs.
     */
    private fun styleProperty(
        setter: (Project.Settings, StyleData) -> Unit,
        getter: (Project.Settings) -> StyleData
    ): StyleDataProperty = StyleDataProperty(
        { newValue -> value?.also { setter(it, newValue ?: StyleData()) } },
        { value?.let(getter) },
        { fireValueChangedEvent() }
    )

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
        authorFontProperty.refresh()
        copyrightFontProperty.refresh()
        titleFontProperty.refresh()
        titleAppendixFontProperty.refresh()
        chapterFontProperty.refresh()
        chapterAppendixFontProperty.refresh()
        textFontProperty.refresh()
        copyrightPageProperty.refresh()
        startWithEmptyPageProperty.refresh()
        endWithEmptyPageProperty.refresh()
    }

}

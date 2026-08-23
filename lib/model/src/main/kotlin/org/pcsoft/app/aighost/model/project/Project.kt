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

package org.pcsoft.app.aighost.model.project

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonPropertyOrder
import org.pcsoft.app.aighost.model.common.StyleData

/**
 * A writing project of the user, the root object that is persisted as one JSON document.
 *
 * Beside the manuscript itself it carries the publishing data and the typographic settings the
 * manuscript is rendered with.
 *
 * The project is a plain mutable value object: it is changed on the object itself, one property at a
 * time, and it reports nothing to anybody. Whoever shows a project reads it when it needs the value.
 *
 * @property name Name of the project as shown to the user.
 * @property author Author printed in the manuscript.
 * @property copyright Copyright notice printed in the manuscript.
 * @property settings Typographic and page settings of the manuscript.
 * @property book The manuscript with its title and chapters.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonPropertyOrder("name", "author", "copyright", "settings", "book")
data class Project(
    var name: String = "New Project",
    var author: String = "",
    var copyright: String = "",

    var settings: Settings = Settings(),
    var book: Book = Book()
) {

    /**
     * Typographic and page settings a [Project] is rendered with.
     *
     * @property authorFont Style of the author name.
     * @property copyrightFont Style of the copyright notice.
     * @property titleFont Style of the book title.
     * @property titleAppendixFont Style of the further title lines.
     * @property chapterFont Style of a chapter heading.
     * @property chapterAppendixFont Style of the further chapter heading lines.
     * @property textFont Style of the chapter text.
     * @property copyrightPage Whether a separate copyright page is printed.
     * @property startWithEmptyPage Whether the manuscript starts with an empty page.
     * @property endWithEmptyPage Whether the manuscript ends with an empty page.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    data class Settings(
        var authorFont: StyleData = StyleData(),
        var copyrightFont: StyleData = StyleData(),
        var titleFont: StyleData = StyleData(),
        var titleAppendixFont: StyleData = StyleData(),
        var chapterFont: StyleData = StyleData(),
        var chapterAppendixFont: StyleData = StyleData(),
        var textFont: StyleData = StyleData(),

        var copyrightPage: Boolean = false,
        var startWithEmptyPage: Boolean = true,
        var endWithEmptyPage: Boolean = true
    )
}

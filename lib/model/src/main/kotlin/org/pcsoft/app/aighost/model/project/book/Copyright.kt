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

package org.pcsoft.app.aighost.model.project.book

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * The copyright page of a [org.pcsoft.app.aighost.model.project.Project]'s book.
 *
 * It carries the copyright notice and any further lines printed below it. Whether the page belongs
 * to the book is a switch of its own; the text is kept no matter how that switch stands.
 *
 * @property copyright The copyright notice, prefilled with the year the project was created.
 * @property copyrightAppendix Further lines printed below the copyright notice, empty by default.
 * @property included Whether the copyright page is printed in the book, true by default.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class Copyright(
    var copyright: String = "Copyright " + LocalDate.now().format(DateTimeFormatter.ISO_DATE),
    var copyrightAppendix: List<String> = emptyList(),

    var included: Boolean = true
)

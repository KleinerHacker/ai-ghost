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

/**
 * The blurb of a [Book], the advertising text printed on the cover.
 *
 * Unlike the written parts of the manuscript the blurb carries no heading of its own: it is text
 * only. Every book carries its blurb, [Book.blurb] is never empty. Whether the blurb belongs to the
 * book is told by [included] alone; what was written into it stays untouched while it does not, so
 * switching it off and on again gives the text back unchanged.
 *
 * @property prompt Prompt for the blurb, empty by default.
 * @property paragraph Paragraphs of the blurb in their order, empty by default.
 * @property included Whether the blurb belongs to the book, `false` by default.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class Blurb(
    var prompt: String = "",
    var paragraph: List<String> = emptyList(),

    var included: Boolean = false
)

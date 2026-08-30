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
import org.pcsoft.app.aighost.model.project.common.AIPrompt

/**
 * The epilog of a [Book], printed after the last chapter.
 *
 * Every book carries its epilog, [Book.epilog] is never empty. Whether the epilog belongs to the book
 * is told by [included] alone; what was written into it stays untouched while it does not, so
 * switching it off and on again gives the text back unchanged.
 *
 * @property title Heading of the epilog, empty by default.
 * @property titleAppendix Further heading lines shown below the title, empty by default.
 * @property prompts Prompts for the epilog, empty by default.
 * @property paragraph Paragraphs of the epilog in their order, empty by default.
 * @property included Whether the epilog belongs to the book, `false` by default.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class Epilog(
    override var title: String = "",
    override var titleAppendix: List<String> = listOf(),

    override var prompts: AIPrompt = AIPrompt(),

    override var paragraph: List<String> = emptyList(),

    var included: Boolean = false
) : BookPart

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
 * The prolog of a [Book], printed before the first chapter.
 *
 * A book has at most one prolog, and only if the user created it, so [Book.prolog] stays empty until
 * then.
 *
 * @property title Heading of the prolog.
 * @property titleAppendix Further heading lines shown below the title, empty by default.
 * @property paragraph Paragraphs of the prolog in their order, empty by default.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class Prolog(
    override var title: String,
    override var titleAppendix: List<String> = listOf(),

    override var paragraph: List<String> = emptyList()
) : BookPart

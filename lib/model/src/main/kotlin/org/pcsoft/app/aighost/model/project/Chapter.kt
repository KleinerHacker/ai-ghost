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

/**
 * A single chapter of a [Book].
 *
 * A chapter is the smallest unit the user writes in: it carries its heading and the written text,
 * split into paragraphs. The text may be empty while the chapter is only outlined. Unlike the other
 * parts of a book a chapter carries a [name] as well, because the user works with many of them and
 * needs to tell them apart before their headings are written.
 *
 * @property name Name of the chapter as shown in the project tree.
 * @property title Heading of the chapter as printed in the manuscript.
 * @property titleAppendix Further heading lines shown below the title, empty by default.
 * @property paragraph Paragraphs of the chapter in their order, empty by default.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class Chapter(
    var name: String,
    override var title: String,
    override var titleAppendix: List<String> = listOf(),

    override var paragraph: List<String> = emptyList()
) : BookPart

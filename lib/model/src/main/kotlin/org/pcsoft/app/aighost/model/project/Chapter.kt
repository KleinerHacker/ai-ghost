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
 * split into paragraphs. The text may be empty while the chapter is only outlined.
 *
 * @property title Heading of the chapter as shown in the chapter list.
 * @property titleAppendix Further heading lines shown below the title, empty by default.
 * @property paragraph Paragraphs of the chapter in their order, empty by default.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class Chapter(
    val title: String,
    val titleAppendix: List<String> = listOf(),

    val paragraph: List<String> = emptyList()
)

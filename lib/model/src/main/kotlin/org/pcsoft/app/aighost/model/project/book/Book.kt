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
import org.pcsoft.app.aighost.model.project.Project
import org.pcsoft.app.aighost.model.project.common.AIPrompt
import org.pcsoft.app.aighost.plugin.api.model.project.ProjectPart
import org.pcsoft.app.aighost.plugin.api.model.project.ProjectPartInfo

/**
 * Defines the current version of the project structure or model.
 *
 * This constant is used to track changes in the data model to ensure compatibility
 * when handling serialization, deserialization, or migrations in related components.
 */
private const val VERSION = 1

/**
 * The manuscript of a [org.pcsoft.app.aighost.model.project.Project]: its title and all parts that make it up.
 *
 * Every project holds a book, even an empty one that has not been written yet. The chapter order is
 * part of the data: the list is stored and read back in exactly the order the user arranged it in.
 * [prolog], [epilog] and [blurb] exist at most once and only after the user created them, so they
 * are empty until then.
 *
 * @property version Version of the project metadata structure.
 * @property title Main title of the book.
 * @property titleAppendix Further title lines shown below the main title, empty by default.
 * @property prompts Prompts for the book, empty by default.
 * @property prolog Prolog printed before the first chapter, absent by default.
 * @property chapters Chapters of the book in their user defined order, empty by default.
 * @property epilog Epilog printed after the last chapter, absent by default.
 * @property blurb Advertising text printed on the cover, absent by default.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@ProjectPartInfo(identifier = Project.PART_BOOK)
data class Book(
    override val version: Int = VERSION,

    var title: String = "My Book",
    var titleAppendix: List<String> = listOf(),
    var prompts: AIPrompt = AIPrompt(),

    var prolog: Prolog? = null,
    var chapters: List<Chapter> = emptyList(),
    var epilog: Epilog? = null,
    var blurb: Blurb? = null
) : ProjectPart

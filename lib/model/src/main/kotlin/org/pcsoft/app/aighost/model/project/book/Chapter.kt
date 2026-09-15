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
import java.util.UUID

/**
 * A single chapter of a [Book].
 *
 * A chapter is the smallest unit the user writes in. Unlike the other parts of a book a chapter
 * carries a [name] as well, because the user works with many of them and needs to tell them apart
 * before their headings are written.
 *
 * [id] is handed out once, when the chapter is created, and never changes afterwards - not when the
 * chapter is renamed, not when its prompts change, not across a save and reload. It is the anchor id
 * the chapter's flowing text is addressed by in the simPlay `Document` of [Book] (IP-38); the heading
 * and the written text themselves no longer live on this class, they live in that document.
 *
 * @property name Name of the chapter as shown in the project tree.
 * @property id Stable id of the chapter, assigned once at creation and never changed afterwards.
 * @property prompts Prompts for the chapter, empty by default.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class Chapter(
    var name: String,
    val id: UUID = UUID.randomUUID(),

    override var prompts: AIPrompt = AIPrompt()
) : BookPart

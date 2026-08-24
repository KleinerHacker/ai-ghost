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

package org.pcsoft.app.aighost.model.project.design

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import org.pcsoft.app.aighost.model.project.Project
import org.pcsoft.app.aighost.plugin.api.model.project.ProjectPart
import org.pcsoft.app.aighost.plugin.api.model.project.ProjectPartInfo

/**
 * Defines the current version of the design metadata structure.
 * This value represents the default version used for project parts
 * adhering to the `ProjectPart` interface.
 */
private const val VERSION = 1

/**
 * Typographic and page layout settings of a [org.pcsoft.app.aighost.model.project.Project].
 *
 * Every project holds design settings that control how its manuscript is rendered. The settings
 * define the visual appearance of various book elements and page structure options.
 *
 * @property version Version of the design metadata structure.
 * @property authorDesign Typographic settings for the author name.
 * @property copyrightDesign Typographic settings for the copyright page.
 * @property titleDesign Typographic settings for the title page.
 * @property chapterDesign Typographic settings for chapter headings and content.
 * @property textDesign Typographic settings for regular body text.
 * @property startWithEmptyPage Whether to begin the book with a blank page, true by default.
 * @property endWithEmptyPage Whether to end the book with a blank page, true by default.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@ProjectPartInfo(identifier = Project.PART_DESIGN)
data class Design(
    override val version: Int = VERSION,

    var authorDesign: AuthorDesign = AuthorDesign(),
    var copyrightDesign: CopyrightDesign = CopyrightDesign(),
    var titleDesign: TitleDesign = TitleDesign(),
    var chapterDesign: ChapterDesign = ChapterDesign(),
    var textDesign: TextDesign = TextDesign(),

    var startWithEmptyPage: Boolean = true,
    var endWithEmptyPage: Boolean = true
) : ProjectPart

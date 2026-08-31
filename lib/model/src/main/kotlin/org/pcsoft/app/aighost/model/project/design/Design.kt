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
 * define the visual appearance of various book elements, the geometry of a page and page structure
 * options.
 *
 * The settings are grouped by the page they apply to. Each page design carries the styles of its
 * texts, and a style carries its own line spacing - a factor on the line height of the font, where
 * `1.0` sets the lines as tightly as the font asks for and a larger value spreads them apart.
 *
 * @property version Version of the design metadata structure.
 * @property pageFormat Size of a page and the empty space on its four sides.
 * @property titlePage Typographic settings for the title page.
 * @property copyrightPage Typographic settings for the copyright page.
 * @property prologPage Typographic settings for the prolog page.
 * @property blurbPage Typographic settings for the blurb page.
 * @property chapterPage Typographic settings for the chapter pages.
 * @property epilogPage Typographic settings for the epilog page.
 * @property startWithEmptyPage Whether to begin the book with a blank page, true by default.
 * @property endWithEmptyPage Whether to end the book with a blank page, true by default.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@ProjectPartInfo(identifier = Project.PART_DESIGN)
data class Design(
    override val version: Int = VERSION,

    var pageFormat: PageFormat = PageFormat(),

    var titlePage: TitlePageDesign = TitlePageDesign(),
    var copyrightPage: CopyrightPageDesign = CopyrightPageDesign(),

    var prologPage: PrologPageDesign = PrologPageDesign(),
    var blurbPage: BlurbPageDesign = BlurbPageDesign(),
    var chapterPage: ChapterPageDesign = ChapterPageDesign(),
    var epilogPage: EpilogPageDesign = EpilogPageDesign(),

    var startWithEmptyPage: Boolean = true,
    var endWithEmptyPage: Boolean = true
) : ProjectPart

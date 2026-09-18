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
import org.pcsoft.app.aighost.model.common.StyleData

/**
 * Typographic settings for the chapter pages.
 *
 * A chapter is a written part like the prolog or the epilog: it carries a heading, the further
 * heading lines below it and the body text, each set with its own style. Beyond that a chapter may
 * push its heading onto a page of its own.
 *
 * @property titleStyle Appearance of the chapter heading.
 * @property titleAppendixStyle Appearance of the further heading lines of the chapter.
 * @property textStyle Appearance of the chapter body text.
 * @property titleOnSeparatePage Whether the chapter heading stands on a page of its own, false by default.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class ChapterPageDesign(
    override var titleStyle: StyleData = StyleData(),
    override var titleAppendixStyle: StyleData = StyleData(),
    override var textStyle: StyleData = StyleData(),

    var titleOnSeparatePage: Boolean = false
) : BookPartPageDesign

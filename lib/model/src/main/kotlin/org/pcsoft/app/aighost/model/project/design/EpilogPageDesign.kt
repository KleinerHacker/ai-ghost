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
 * Typographic settings for the epilog page.
 *
 * The epilog is a written part like a chapter or the prolog: it carries a heading, the further
 * heading lines below it and the body text, each set with its own style.
 *
 * @property titleStyle Appearance of the epilog heading.
 * @property titleAppendixStyle Appearance of the further heading lines of the epilog.
 * @property textStyle Appearance of the epilog body text.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class EpilogPageDesign(
    override var titleStyle: StyleData = StyleData(),
    override var titleAppendixStyle: StyleData = StyleData(),
    override var textStyle: StyleData = StyleData()
) : BookPartPageDesign

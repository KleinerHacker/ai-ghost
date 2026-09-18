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
 * Typographic settings for the blurb page.
 *
 * The blurb carries no heading of its own, so it is its paragraphs and nothing else. They are set
 * with a single text style, the same way the body of a chapter is.
 *
 * @property textStyle Appearance of the blurb text.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class BlurbPageDesign(
    var textStyle: StyleData = StyleData()
)

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
 * Represents the design settings for chapter headings and content.
 *
 * This class captures the stylistic configuration for chapter headings and the body text within chapters,
 * defining how elements like font, alignment, and other typographic properties are rendered.
 *
 * @property titleStyle Stylistic attributes for chapter headings.
 * @property titleAppendixStyle Stylistic attributes for any additional text or notes following chapter headings.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class ChapterDesign(
    var titleStyle: StyleData = StyleData(),
    var titleAppendixStyle: StyleData = StyleData()
)

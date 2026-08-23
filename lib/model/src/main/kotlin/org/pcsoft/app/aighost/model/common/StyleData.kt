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

package org.pcsoft.app.aighost.model.common

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

/**
 * Appearance of a single piece of text: how it is drawn and how it is placed.
 *
 * @property font Font the text is rendered with.
 * @property alignment Horizontal placement of the text, [Alignment.LEFT] by default.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class StyleData(
    var font: FontData = FontData(),
    var alignment: Alignment = Alignment.LEFT
)

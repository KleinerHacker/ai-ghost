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
 * Font a piece of text is rendered with, stored independently of any UI toolkit.
 *
 * @property name Family name of the font, as the rendering side resolves it.
 * @property size Font size in points.
 * @property bold Whether the text is drawn in a bold weight.
 * @property italic Whether the text is drawn slanted.
 * @property metrics Fingerprint of the family as it measured when it was written down. `null` says
 *   that it has never been taken - it never says that the family measures differently now.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class FontData(
    var name: String = "Arial",
    var size: Int = 12,
    var bold: Boolean = false,
    var italic: Boolean = false,
    var metrics: FontMetricsData? = null
)

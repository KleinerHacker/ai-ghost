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
 * Fingerprint of a font family - the way it measured on the machine the project was written on.
 *
 * It is taken once and kept as it was taken, so the same family can be recognised again later: it
 * serves recognition alone and is never used for laying anything out.
 *
 * @property widths Shortened SHA-256 digest over the widths of a fixed reference set of characters.
 * @property ascent Distance the family reaches above the base line, in points.
 * @property descent Distance the family reaches below the base line, in points.
 * @property leading Gap the family asks for between two lines, in points.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class FontMetricsData(
    var widths: String = "",
    var ascent: Double = 0.0,
    var descent: Double = 0.0,
    var leading: Double = 0.0
)

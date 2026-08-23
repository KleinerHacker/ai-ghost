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

package org.pcsoft.app.aighost.model.project

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

/**
 * The blurb of a [Book], the advertising text printed on the cover.
 *
 * Unlike the written parts of the manuscript the blurb carries no heading of its own: it is text
 * only. A book has at most one blurb, and only if the user created it, so [Book.blurb] stays empty
 * until then.
 *
 * @property paragraph Paragraphs of the blurb in their order, empty by default.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class Blurb(
    var paragraph: List<String> = emptyList()
)

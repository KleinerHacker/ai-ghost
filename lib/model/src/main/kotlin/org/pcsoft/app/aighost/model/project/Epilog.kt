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
 * The epilog of a [Book], printed after the last chapter.
 *
 * A book has at most one epilog, and only if the user created it, so [Book.epilog] stays empty until
 * then.
 *
 * @property title Heading of the epilog.
 * @property titleAppendix Further heading lines shown below the title, empty by default.
 * @property paragraph Paragraphs of the epilog in their order, empty by default.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class Epilog(
    override val title: String,
    override val titleAppendix: List<String> = listOf(),

    override val paragraph: List<String> = emptyList()
) : BookPart

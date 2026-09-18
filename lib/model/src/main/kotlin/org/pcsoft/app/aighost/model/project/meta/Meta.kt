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

package org.pcsoft.app.aighost.model.project.meta

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import org.pcsoft.app.aighost.model.project.Project
import org.pcsoft.app.aighost.plugin.api.model.project.ProjectPart
import org.pcsoft.app.aighost.plugin.api.model.project.ProjectPartInfo

/**
 * Defines the current version of the project metadata structure.
 * This value represents the default version used for project parts
 * adhering to the `ProjectPart` interface.
 */
private const val VERSION = 1

/**
 * Represents the metadata for the project.
 *
 * This class captures the essential information about the project,
 * including its name and author.
 *
 * Beside what the user typed, the meta data carries the list of the parts the document holds beyond
 * the three standard ones. It is written on every save and is read to tell a complete document from
 * one that lost an entry on its way, which the entries alone cannot answer: a part that is gone is
 * simply not there any more.
 *
 * @property version Version of the project metadata structure.
 * @property name The name of the project.
 * @property author The author of the project.
 * @property additionalParts The identifiers of the parts stored beside the three standard ones.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@ProjectPartInfo(identifier = Project.PART_META)
data class Meta(
    override val version: Int = VERSION,

    var name: String = "New Project",
    var author: String = "",

    var additionalParts: List<String> = emptyList(),
) : ProjectPart

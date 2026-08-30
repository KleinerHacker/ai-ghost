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

package org.pcsoft.app.aighost.model.project.common

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

/**
 * Represents a prompt used within an AI-driven context.
 *
 * This class can be utilized to define structured input prompts for various generative tasks, enabling
 * more meaningful and contextually appropriate outputs based on both content and desired style.
 *
 * @param contentPrompt Describes the primary content of a given prompt, such as a question or request.
 * @param stylePrompt Specifies styling, tone, or additional context for refining the AI's response.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class AIPrompt(
    var contentPrompt: String = "",
    var stylePrompt: String = ""
)

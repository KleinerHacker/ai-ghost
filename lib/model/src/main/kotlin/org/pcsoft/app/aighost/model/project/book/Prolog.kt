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

package org.pcsoft.app.aighost.model.project.book

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import org.pcsoft.app.aighost.model.project.common.AIPrompt

/**
 * The prolog of a [Book], printed before the first chapter.
 *
 * Every book carries its prolog, [Book.prolog] is never empty. Whether the prolog belongs to the book
 * is told by [included] alone. The heading and the written text that used to sit on this class moved
 * to the simPlay `Document` of [Book] (IP-36/37/38); the prolog contributes to that document through
 * its anchor instead.
 *
 * @property prompts Prompts for the prolog, empty by default.
 * @property included Whether the prolog belongs to the book, `false` by default.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class Prolog(
    override var prompts: AIPrompt = AIPrompt(),

    var included: Boolean = false
) : BookPart

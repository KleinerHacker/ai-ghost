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

import org.pcsoft.app.aighost.model.project.common.AIPrompt

/**
 * A written part of a [Book] that is generated from prompts.
 *
 * [Prolog], [Chapter] and [Epilog] share exactly this shape, so everything that works with the
 * prompts of a written part works on this interface instead of on each of them.
 *
 * The heading, its further lines and the flowing text that used to sit on this interface moved to
 * the simPlay `Document` of [Book] (IP-36/37/38); a part contributes to that document through its
 * anchor instead of carrying text fields of its own.
 *
 * @property prompts Prompts for the part.
 */
interface BookPart {
    var prompts: AIPrompt
}

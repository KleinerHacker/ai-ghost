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

/**
 * A written part of a [Book] that carries a heading and its text.
 *
 * [Prolog], [Chapter] and [Epilog] share exactly this shape, so everything that renders or exports
 * written text works on this interface instead of on each of them.
 *
 * @property title Heading of the part.
 * @property titleAppendix Further heading lines shown below the title.
 * @property paragraph Paragraphs of the part in their order.
 */
interface BookPart {
    val title: String
    val titleAppendix: List<String>
    val paragraph: List<String>
}

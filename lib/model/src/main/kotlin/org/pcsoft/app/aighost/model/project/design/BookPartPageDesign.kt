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

package org.pcsoft.app.aighost.model.project.design

import org.pcsoft.app.aighost.model.common.StyleData

/**
 * The typographic settings a written part of the book shares - a prolog, a chapter or an epilog.
 *
 * All three are set the same way: a heading, the further heading lines below it and the body text.
 * The concrete page design of each carries these three styles and may add options of its own, so a
 * builder that handles the three parts alike takes this view on them and nothing more.
 *
 * @property titleStyle Appearance of the heading of the part.
 * @property titleAppendixStyle Appearance of the further heading lines of the part.
 * @property textStyle Appearance of the body text of the part.
 */
interface BookPartPageDesign {
    val titleStyle: StyleData
    val titleAppendixStyle: StyleData
    val textStyle: StyleData
}

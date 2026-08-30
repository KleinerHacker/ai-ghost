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

package org.pcsoft.app.aighost.layouting

/**
 * One run of text that is set as a unit, in one style.
 *
 * There is exactly this one kind of block and it carries no role: the core cannot tell a title from
 * a paragraph and must not be able to. What a block means is decided by whoever builds it, and it
 * reaches the core as text plus a style - the difference between a heading and body text is then
 * nothing but a different [TextStyle].
 *
 * A block whose [text] is empty is kept and set as a single empty line, so an empty paragraph still
 * takes its vertical space.
 *
 * @property text Text of the block, without line breaks of its own.
 * @property style Style the text is set in.
 */
data class TextBlock(
    val text: String,
    val style: TextStyle
)

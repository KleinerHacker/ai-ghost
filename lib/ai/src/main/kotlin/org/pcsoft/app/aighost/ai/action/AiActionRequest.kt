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

package org.pcsoft.app.aighost.ai.action

/**
 * Input of a single call to [AiAction].
 *
 * Every variant carries exactly the text an implementation needs to answer the request - a paragraph
 * or heading for the three text actions, a content and a style prompt for a generated chapter - and
 * nothing that describes how the answer is produced. That decision belongs to the implementation
 * chosen by the caller.
 */
sealed interface AiActionRequest {

    /**
     * Rewrites [text] while keeping its meaning.
     *
     * @property text The paragraph or heading to rewrite.
     */
    data class Rewrite(val text: String) : AiActionRequest

    /**
     * Expands [text] into a longer version.
     *
     * @property text The paragraph or heading to expand.
     */
    data class Expand(val text: String) : AiActionRequest

    /**
     * Shortens [text] while keeping its meaning.
     *
     * @property text The paragraph or heading to shorten.
     */
    data class Shorten(val text: String) : AiActionRequest

    /**
     * Generates a whole chapter from a content and a style prompt.
     *
     * @property contentPrompt What the chapter should be about.
     * @property stylePrompt How the chapter should be written.
     */
    data class GenerateChapter(val contentPrompt: String, val stylePrompt: String) : AiActionRequest
}

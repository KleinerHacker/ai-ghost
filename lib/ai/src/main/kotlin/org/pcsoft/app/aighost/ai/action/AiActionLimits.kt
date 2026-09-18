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

import arrow.core.Either
import arrow.core.flatMap
import arrow.core.left
import arrow.core.right
import org.pcsoft.app.aighost.ai.util.TokenUtils
import org.pcsoft.app.aighost.model.pref.Ai

/**
 * Checks an [AiActionRequest] against the character limits configured in [Ai], before it is ever
 * handed to an [AiAction].
 *
 * The check is a function of its own rather than part of [AiAction], because no implementation of the
 * port exists yet; a future implementation is not expected to repeat what every caller already did.
 * [Ai.maxStoryCharacters] bounds the text an action works on - the paragraph, heading or content
 * prompt - and [Ai.maxStyleCharacters] bounds the style prompt of [AiActionRequest.GenerateChapter].
 */
object AiActionLimits {

    /**
     * Checks [request] against [preferences].
     *
     * @param request The request to check.
     * @param preferences The AI preferences the limits are read from.
     * @return [Unit] on the right when every text of [request] is within its limit, or the first
     * [AiActionError.LimitExceeded] found on the left.
     */
    fun check(request: AiActionRequest, preferences: Ai): Either<AiActionError.LimitExceeded, Unit> =
        when (request) {
            is AiActionRequest.Rewrite -> checkText(request.text, preferences.maxStoryCharacters)
            is AiActionRequest.Expand -> checkText(request.text, preferences.maxStoryCharacters)
            is AiActionRequest.Shorten -> checkText(request.text, preferences.maxStoryCharacters)
            is AiActionRequest.GenerateChapter ->
                checkText(request.contentPrompt, preferences.maxStoryCharacters)
                    .flatMap { checkText(request.stylePrompt, preferences.maxStyleCharacters) }
        }

    /** Checks a single [text] against [limit], in characters. */
    private fun checkText(text: String, limit: Long): Either<AiActionError.LimitExceeded, Unit> {
        val actualCharacters = text.length.toLong()
        if (actualCharacters <= limit) {
            return Unit.right()
        }

        return AiActionError.LimitExceeded(
            characterLimit = limit,
            actualCharacters = actualCharacters,
            estimatedTokens = TokenUtils.estimateTokens(text)
        ).left()
    }
}

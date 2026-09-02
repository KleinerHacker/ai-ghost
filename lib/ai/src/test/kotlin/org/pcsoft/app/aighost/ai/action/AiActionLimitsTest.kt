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
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.pcsoft.app.aighost.ai.util.TokenUtils
import org.pcsoft.app.aighost.model.pref.Ai

/**
 * Developer tests for [AiActionLimits].
 */
class AiActionLimitsTest {

    /**
     * A rewrite whose text fits within [Ai.maxStoryCharacters] must be allowed to run.
     */
    @Test
    fun `rewrite within the story limit passes`() {
        val ai = Ai(maxStoryCharacters = 10, maxStyleCharacters = 10)

        val result = AiActionLimits.check(AiActionRequest.Rewrite("short text"), ai)

        assertTrue(result.isRight())
    }

    /**
     * An expand request longer than [Ai.maxStoryCharacters] must be refused before any implementation
     * of [AiAction] is asked, since none exists to repeat the check.
     */
    @Test
    fun `expand longer than the story limit is refused`() {
        val ai = Ai(maxStoryCharacters = 5, maxStyleCharacters = 10)

        val result = AiActionLimits.check(AiActionRequest.Expand("too long"), ai)

        val error = (result as Either.Left).value
        assertEquals(5L, error.characterLimit)
        assertEquals(8L, error.actualCharacters)
    }

    /**
     * A shorten request exactly at [Ai.maxStoryCharacters] must pass, since the limit bounds the
     * allowed length rather than falling one character short of it.
     */
    @Test
    fun `shorten exactly at the story limit passes`() {
        val ai = Ai(maxStoryCharacters = 9, maxStyleCharacters = 10)

        val result = AiActionLimits.check(AiActionRequest.Shorten("9 letters"), ai)

        assertTrue(result.isRight())
    }

    /**
     * A generated chapter checks its content prompt against [Ai.maxStoryCharacters] and only reaches
     * the style prompt once the content prompt passed.
     */
    @Test
    fun `generate chapter checks the content prompt against the story limit`() {
        val ai = Ai(maxStoryCharacters = 5, maxStyleCharacters = 100)

        val result = AiActionLimits.check(
            AiActionRequest.GenerateChapter(contentPrompt = "too long a prompt", stylePrompt = "ok"),
            ai
        )

        val error = (result as Either.Left).value
        assertEquals(5L, error.characterLimit)
    }

    /**
     * A generated chapter checks its style prompt against [Ai.maxStyleCharacters], separately from
     * the content prompt.
     */
    @Test
    fun `generate chapter checks the style prompt against the style limit`() {
        val ai = Ai(maxStoryCharacters = 100, maxStyleCharacters = 5)

        val result = AiActionLimits.check(
            AiActionRequest.GenerateChapter(contentPrompt = "ok", stylePrompt = "too long a style"),
            ai
        )

        val error = (result as Either.Left).value
        assertEquals(5L, error.characterLimit)
    }

    /**
     * The reported error carries the same token estimate [TokenUtils] would produce for the checked
     * text, so a caller can show it without estimating a second time.
     */
    @Test
    fun `limit exceeded carries the estimated token cost`() {
        val ai = Ai(maxStoryCharacters = 1, maxStyleCharacters = 1)
        val text = "12345678"

        val result = AiActionLimits.check(AiActionRequest.Rewrite(text), ai)

        val error = (result as Either.Left).value
        assertEquals(TokenUtils.estimateTokens(text), error.estimatedTokens)
    }

    /**
     * The generic result type of [AiActionLimits.check] is asserted to actually carry
     * [AiActionError.LimitExceeded] on its left side, matching the contract callers rely on.
     */
    @Test
    fun `exceeded limit is reported as LimitExceeded`() {
        val ai = Ai(maxStoryCharacters = 1, maxStyleCharacters = 1)

        val result = AiActionLimits.check(AiActionRequest.Rewrite("too long"), ai)

        assertInstanceOf(AiActionError.LimitExceeded::class.java, (result as Either.Left).value)
    }
}

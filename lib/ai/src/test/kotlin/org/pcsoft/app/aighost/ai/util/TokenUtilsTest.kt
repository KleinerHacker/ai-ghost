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

package org.pcsoft.app.aighost.ai.util

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

/**
 * Developer tests for [TokenUtils].
 */
class TokenUtilsTest {

    /**
     * An empty text is sent to no model at all, so it must not be reported as a cost of its own.
     */
    @Test
    fun `empty text costs no token`() {
        assertEquals(0L, TokenUtils.estimateTokens(""))
    }

    /**
     * A text shorter than one token still occupies a token, because a model cannot split a token
     * further - the estimation therefore rounds up instead of reporting nothing.
     */
    @Test
    fun `text shorter than one token costs one token`() {
        assertEquals(1L, TokenUtils.estimateTokens("a"))
    }

    /**
     * A text whose length is an exact multiple of [TokenUtils.CHARS_PER_TOKEN] costs exactly that
     * many tokens, without the rounding adding one on top.
     */
    @Test
    fun `text of full token length costs exactly that many tokens`() {
        assertEquals(2L, TokenUtils.estimateTokens("12345678"))
    }

    /**
     * A text that does not fill its last token completely still pays for it, so the estimation is
     * rounded up to the next whole token.
     */
    @Test
    fun `partly filled token is rounded up`() {
        assertEquals(3L, TokenUtils.estimateTokens("123456789"))
    }

    /**
     * The estimation counts characters, no matter which ones - white space and line breaks of a
     * written prompt are part of the text a model receives.
     */
    @Test
    fun `white space counts like any other character`() {
        // 18 characters including the blank and the line break, which is 4.5 tokens rounded up.
        assertEquals(5L, TokenUtils.estimateTokens("Write a\nshort text"))
    }
}

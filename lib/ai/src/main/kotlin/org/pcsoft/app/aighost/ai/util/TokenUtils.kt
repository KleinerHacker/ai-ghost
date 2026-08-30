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

import kotlin.math.ceil

/**
 * Estimation of the number of tokens a text costs when it is sent to a language model.
 *
 * The estimation is deliberately a rough one: every model brings its own tokenizer, and the exact
 * count is only known to the model itself. What is calculated here is the rule of thumb the common
 * models share - roughly [CHARS_PER_TOKEN] characters make one token - so the user gets a feeling
 * for the size of a prompt without the application shipping a tokenizer of its own.
 *
 * Every value this object returns is therefore an approximation and MUST be presented as one.
 */
object TokenUtils {

    /** Number of characters a single token holds on average. */
    const val CHARS_PER_TOKEN: Int = 4

    /**
     * Estimates how many tokens the given text costs.
     *
     * The length of the text is divided by [CHARS_PER_TOKEN] and rounded up, so that any text that
     * is not empty costs at least one token.
     *
     * @param text the text to estimate, may be empty
     * @return the estimated number of tokens, `0` for an empty text
     */
    fun estimateTokens(text: String): Long {
        if (text.isEmpty()) {
            return 0L
        }

        return ceil(text.length.toDouble() / CHARS_PER_TOKEN).toLong()
    }
}

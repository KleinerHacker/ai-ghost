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
 * Reason why an [AiAction] did not deliver a result.
 *
 * The action never throws for these cases: [LimitExceeded] is reported through
 * [AiActionLimits.check] before an implementation is even asked, while [Cancelled] and [Failed]
 * are reported through [AiActionCallback.onError] or, in the case of [Cancelled], through
 * [AiActionCallback.onCancelled] instead.
 */
sealed interface AiActionError {

    /**
     * The request text is longer than the limit configured in the preferences.
     *
     * @property characterLimit The configured limit, in characters.
     * @property actualCharacters The actual length of the checked text, in characters.
     * @property estimatedTokens The estimated token cost of the checked text, per [org.pcsoft.app.aighost.ai.util.TokenUtils].
     */
    data class LimitExceeded(
        val characterLimit: Long,
        val actualCharacters: Long,
        val estimatedTokens: Long
    ) : AiActionError

    /** The request was cancelled through [AiActionHandle.cancel] before it completed. */
    data object Cancelled : AiActionError

    /**
     * The request failed for a reason outside the caller's control.
     *
     * @property cause The underlying failure.
     */
    data class Failed(val cause: Throwable) : AiActionError
}

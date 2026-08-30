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

package org.pcsoft.app.aighost.app.ui.component.base

/**
 * How far the written prompt has eaten into the number of characters it is allowed to use.
 *
 * The step decides how the character counter of [AiPromptArea] is coloured, so that the user notices
 * a filling prompt before the area stops accepting input.
 */
enum class AiPromptUsage(
    /** Style class the character counter carries in this step, `null` while nothing stands out. */
    val styleClass: String?
) {

    /** Less than [WARN_THRESHOLD] of the allowed characters are used, or no limit is set at all. */
    NORMAL(null),

    /** At least [WARN_THRESHOLD] of the allowed characters are used, but the limit is not reached. */
    WARN("prompt-counter-warn"),

    /** Every allowed character is used, so no further input is taken. */
    LIMIT("prompt-counter-limit");

    companion object {

        /** Share of the allowed characters from which the counter warns. */
        const val WARN_THRESHOLD: Double = 0.9

        /**
         * Resolves the step the given prompt length sits in.
         *
         * A limit of zero or less means the prompt may grow freely, which never stands out.
         *
         * @param length number of characters the prompt holds
         * @param maxCharacters number of characters the prompt may hold, zero or less for no limit
         * @return the step the counter is shown in
         */
        fun of(length: Long, maxCharacters: Long): AiPromptUsage = when {
            maxCharacters <= 0L -> NORMAL
            length >= maxCharacters -> LIMIT
            length >= maxCharacters * WARN_THRESHOLD -> WARN
            else -> NORMAL
        }
    }
}

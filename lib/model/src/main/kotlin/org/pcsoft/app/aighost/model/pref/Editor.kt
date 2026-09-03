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

package org.pcsoft.app.aighost.model.pref

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

/**
 * Settings of the writing surface.
 *
 * This data class is stored inside the [Preferences] and is serialized as YAML. Unknown properties
 * are ignored during deserialization and every field carries a default, so a file written by another
 * version of the application stays readable.
 *
 * @property paragraphMergePauseMillis Length of the typing pause, in milliseconds, after which
 *   consecutive edits of the same paragraph or heading stop being folded into a single undo entry.
 *   Defaults to [DEFAULT_PARAGRAPH_MERGE_PAUSE_MILLIS].
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class Editor(
    var paragraphMergePauseMillis: Long = DEFAULT_PARAGRAPH_MERGE_PAUSE_MILLIS
) {

    init {
        require(paragraphMergePauseMillis in MIN_PARAGRAPH_MERGE_PAUSE_MILLIS..MAX_PARAGRAPH_MERGE_PAUSE_MILLIS) {
            "paragraphMergePauseMillis must be within " +
                "$MIN_PARAGRAPH_MERGE_PAUSE_MILLIS..$MAX_PARAGRAPH_MERGE_PAUSE_MILLIS, but was $paragraphMergePauseMillis"
        }
    }

    companion object {

        /** Typing pause used when no other value is configured. */
        const val DEFAULT_PARAGRAPH_MERGE_PAUSE_MILLIS: Long = 600

        /** Smallest typing pause a settings file may carry. */
        const val MIN_PARAGRAPH_MERGE_PAUSE_MILLIS: Long = 100

        /** Largest typing pause a settings file may carry. */
        const val MAX_PARAGRAPH_MERGE_PAUSE_MILLIS: Long = 5000
    }
}

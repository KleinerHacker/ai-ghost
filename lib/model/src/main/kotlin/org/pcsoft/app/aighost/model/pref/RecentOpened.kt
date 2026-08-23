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
 * Tracks recently opened files with a maximum limit.
 *
 * This data class is stored inside the [Preferences] and is serialized to JSON. Unknown properties
 * in the JSON document are ignored during deserialization to maintain forward compatibility when
 * new fields are added.
 *
 * [add], [remove] and [clear] return a new instance instead of changing this one, so a list handed
 * around is never modified behind someone's back. The most recently opened file is always the first
 * entry.
 *
 * A file written by another version may carry more entries than [max] allows; the limit is applied
 * again as soon as [add] is called.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class RecentOpened(
    /** Maximum number of entries to keep in the recent files list. */
    var max: Int = DEFAULT_MAX,
    /** List of file paths that were recently opened, limited to [max] entries. */
    var entries: List<String> = emptyList()
) {

    init {
        require(max > 0) { "max must be greater than zero, but was $max" }
    }

    /**
     * Returns a copy in which [path] is the most recent entry.
     *
     * A path that is already known is moved to the front instead of being listed twice, and entries
     * beyond [max] are dropped from the end.
     */
    fun add(path: String): RecentOpened =
        copy(entries = (listOf(path) + entries.filterNot { it == path }).take(max))

    /**
     * Returns a copy without [path], for a file the user deleted or that no longer exists.
     *
     * A path that is not listed leaves the entries unchanged.
     */
    fun remove(path: String): RecentOpened =
        copy(entries = entries.filterNot { it == path })

    /** Returns a copy without any entry, keeping the configured [max]. */
    fun clear(): RecentOpened =
        copy(entries = emptyList())

    companion object {

        /** Number of entries kept when no other limit is configured. */
        const val DEFAULT_MAX: Int = 10
    }
}

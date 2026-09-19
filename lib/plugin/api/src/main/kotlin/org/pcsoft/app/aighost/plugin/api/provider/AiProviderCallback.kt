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

package org.pcsoft.app.aighost.plugin.api.provider

/**
 * Receives the streamed outcome of a single [AiProvider.generate] call.
 *
 * Exactly one of [onComplete] or [onError] is called last, after zero or more calls to [onChunk].
 * Nothing is called from the caller's own thread by contract - an implementation may call back from
 * any thread the underlying model client uses.
 */
interface AiProviderCallback {

    /**
     * A batch of generated text arrived.
     *
     * @param text The batch of text that arrived.
     */
    fun onChunk(text: String)

    /** The call finished and every chunk has been handed to [onChunk]. */
    fun onComplete()

    /**
     * The call failed, was cancelled, or could not even be started.
     *
     * @param error The reason the call did not complete.
     */
    fun onError(error: Throwable)
}

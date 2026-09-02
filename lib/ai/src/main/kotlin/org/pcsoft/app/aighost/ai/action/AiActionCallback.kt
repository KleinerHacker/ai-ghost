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
 * Receives the streamed outcome of a single [AiAction.execute] call.
 *
 * Exactly one of [onComplete], [onError] or [onCancelled] is called last, after zero or more calls to
 * [onChunk]. Nothing is called from the caller's own thread by contract - an implementation may call
 * back from any thread, so a caller that touches UI state has to switch back itself.
 *
 * A caller that needs the result as paragraphs collects the chunks handed to [onChunk] until
 * [onComplete] and applies [ParagraphSplitter] to the collected text, rather than splitting chunk by
 * chunk.
 */
interface AiActionCallback {

    /**
     * A batch of text arrived.
     *
     * Batches, not single tokens, are handed over, so a consumer measuring and showing the text is not
     * called for every token a model produces.
     *
     * @param text The batch of text that arrived.
     */
    fun onChunk(text: String)

    /** The request finished and every chunk has been handed to [onChunk]. */
    fun onComplete()

    /**
     * The request failed.
     *
     * @param error The reason the request did not complete.
     */
    fun onError(error: AiActionError)

    /** The request was cancelled through [AiActionHandle.cancel] before it completed. */
    fun onCancelled()
}

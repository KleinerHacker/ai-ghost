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
 * Port through which the application asks an AI provider to rewrite, expand or shorten a paragraph or
 * heading, or to generate a whole chapter.
 *
 * The interface carries streaming, cancellation and error reporting from the start, because adding
 * any of the three later would change the signature every caller depends on. A caller checks
 * [AiActionLimits] before calling [execute], since an implementation is not expected to repeat that
 * check.
 *
 * TODO No implementation of this interface exists yet - neither a stub nor a real provider. A
 * provider, built-in or user supplied, is wired in through the plugin system of a later, dedicated
 * feature; this port is only the contract it will implement.
 */
interface AiAction {

    /**
     * Starts answering [request], reporting the result to [callback] as it streams in.
     *
     * @param request What to do and the text to do it on.
     * @param callback Receives the streamed result.
     * @return A handle that can cancel this call.
     */
    fun execute(request: AiActionRequest, callback: AiActionCallback): AiActionHandle
}

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
 * Handle to a single running [AiAction.execute] call.
 *
 * The handle carries nothing but the ability to cancel the call it belongs to, so a caller can hold
 * on to it without depending on which implementation is behind [AiAction].
 */
interface AiActionHandle {

    /**
     * Cancels the request this handle belongs to.
     *
     * Calling this after the request already finished, failed or was cancelled has no effect. A
     * successful cancellation is reported through [AiActionCallback.onCancelled], never immediately by
     * this method, since the underlying work may need a moment to stop.
     */
    fun cancel()
}

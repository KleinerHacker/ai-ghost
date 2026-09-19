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
 * Handle to a single running [AiProvider.generate] call.
 *
 * The handle carries nothing but the ability to cancel the call it belongs to, so a caller can hold
 * on to it without depending on which provider is behind [AiProvider].
 */
fun interface AiProviderHandle {

    /**
     * Cancels the call this handle belongs to.
     *
     * Calling this after the call already finished or failed has no effect. A provider reports a
     * successful cancellation through [AiProviderCallback.onError], never immediately by this
     * method, since the underlying model client may need a moment to stop.
     */
    fun cancel()
}

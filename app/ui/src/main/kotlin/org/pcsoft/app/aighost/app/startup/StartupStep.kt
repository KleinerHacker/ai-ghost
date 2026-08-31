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

package org.pcsoft.app.aighost.app.startup

/**
 * One piece of work carried out while the splash screen is shown.
 *
 * Every background job the application needs before its first window - reading the preferences
 * today, loading plugins later - is a [StartupStep]. [Startup] finds the steps by scanning the
 * package `org.pcsoft.app.aighost.app.startup.step`, so a new step is added by dropping a class
 * there; nothing has to be registered by hand.
 *
 * An implementation MUST
 * * live in the package `org.pcsoft.app.aighost.app.startup.step`,
 * * be a concrete class with a public no-argument constructor, so it can be built by the scan,
 * * carry a [StartupOrder] when it has to run before or after another step.
 *
 * A step runs on the startup background thread; work that needs the FX thread goes through
 * [StartupContext.onFxThread], and the running application is reached through [StartupContext.app].
 * A step that throws aborts the whole startup.
 */
interface StartupStep {

    /** Short name of the step, used for logging. */
    val name: String

    /**
     * Carries out the step. Called on the startup background thread.
     *
     * @param context the running application and the bridge to the FX thread
     */
    fun execute(context: StartupContext)
}

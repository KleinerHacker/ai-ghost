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
 * The place of a [StartupStep] in the startup sequence.
 *
 * The steps are found by scanning their package, which gives no order of its own, so a step that has
 * to run before or after another one states its rank here. [Startup] runs the steps sorted by this
 * value, lowest first; steps that share a value keep a stable order by their class name. A step
 * without this annotation runs at [DEFAULT].
 *
 * @property value the rank of the step, lower runs earlier
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
annotation class StartupOrder(val value: Int) {
    companion object {
        /** The rank a step without a [StartupOrder] annotation runs at. */
        const val DEFAULT: Int = 0
    }
}

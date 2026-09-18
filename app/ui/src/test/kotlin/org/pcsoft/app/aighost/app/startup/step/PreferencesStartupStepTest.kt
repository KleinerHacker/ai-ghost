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

package org.pcsoft.app.aighost.app.startup.step

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.pcsoft.app.aighost.app.startup.Startup
import org.pcsoft.app.aighost.app.startup.StartupOrder

/**
 * Developer tests for [PreferencesStartupStep].
 *
 * The step is glue over [org.pcsoft.app.aighost.app.controller.IoController], whose read path ends in
 * a dialog and a file of the real user and is therefore not exercised from a test - the same line
 * [org.pcsoft.app.aighost.app.controller.IoControllerTest] draws. What is proven here is the part a
 * test can own: the step is built by the scan through a no-argument constructor, carries the name the
 * log relies on, and is ranked to run first.
 */
class PreferencesStartupStepTest {

    /**
     * Use case: the startup logs which step it runs, so the preferences step carries the stable name
     * `preferences`.
     */
    @Test
    fun isRegisteredAsThePreferencesStep() {
        assertEquals("preferences", PreferencesStartupStep().name)
    }

    /**
     * Use case: the theme and every window read the preferences, so the preferences step is ranked
     * ahead of every step that runs at the default rank.
     */
    @Test
    fun runsBeforeTheDefaultRank() {
        val order = PreferencesStartupStep::class.java.getAnnotation(StartupOrder::class.java)

        assertNotNull(order, "the preferences step states no rank")
        assertEquals(0, Startup.orderOf(PreferencesStartupStep()), "the preferences step is not ranked first")
    }
}

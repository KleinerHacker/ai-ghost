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

import javafx.application.Application
import javafx.application.Platform
import javafx.stage.Stage
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNotSame
import org.junit.jupiter.api.Test
import org.pcsoft.app.aighost.app.controller.PluginController
import org.pcsoft.app.aighost.app.startup.Startup
import org.pcsoft.app.aighost.app.startup.StartupContext
import org.pcsoft.app.aighost.app.startup.StartupOrder
import org.testfx.framework.junit5.ApplicationTest

/**
 * Developer tests for [PluginLoadStartupStep].
 *
 * pluggiat's own discovery, isolated loading and error isolation are pluggiat's own tested
 * responsibility; what belongs here is the wiring this step is responsible for - it is found by the
 * scan, is built with a no-argument constructor, runs after [PreferencesStartupStep], and hands the
 * loaded registry to [org.pcsoft.app.aighost.app.controller.PluginController] through the FX thread.
 */
class PluginLoadStartupStepTest : ApplicationTest() {

    override fun start(stage: Stage) {
        // Nothing to show: the test only needs the FX thread the TestFX runtime started.
    }

    /** Use case: the startup logs which step it runs, so this step carries the stable name `plugins`. */
    @Test
    fun isRegisteredAsThePluginsStep() {
        assertEquals("plugins", PluginLoadStartupStep().name)
    }

    /**
     * Use case: `PreferencesStartupStep` is ranked first because later steps may need the
     * preferences - the plugin step anticipates reading the user plugin directory from them in
     * IP-03 and therefore ranks itself right after it, not at the default rank.
     */
    @Test
    fun runsAfterThePreferencesStep() {
        val order = PluginLoadStartupStep::class.java.getAnnotation(StartupOrder::class.java)

        assertNotNull(order, "the plugin step states no rank")
        assertEquals(1, Startup.orderOf(PluginLoadStartupStep()), "the plugin step does not rank right after the preferences step")
    }

    /**
     * Use case: neither plugin directory exists in the test environment, so loading finds nothing -
     * but the step still replaces [PluginController.registry] with a fresh registry through the FX
     * thread, proving the marshalling and the wiring work even when nothing was found.
     */
    @Test
    fun assignsALoadedRegistryToThePluginControllerOnTheFxThread() {
        assertFalse(Platform.isFxApplicationThread(), "the test itself already runs on the FX thread")
        val before = PluginController.registry

        PluginLoadStartupStep().execute(StartupContext(StubApplication()))

        assertNotSame(before, PluginController.registry, "the plugin step did not replace the registry")
    }

    /** An application that is never started, only handed to the context as its owner. */
    private class StubApplication : Application() {
        override fun start(stage: Stage) = Unit
    }
}

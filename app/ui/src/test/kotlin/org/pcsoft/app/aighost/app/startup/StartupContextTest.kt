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

import javafx.application.Application
import javafx.application.Platform
import javafx.stage.Stage
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.testfx.framework.junit5.ApplicationTest

/**
 * Developer tests for [StartupContext].
 *
 * The context is what a startup step is given: the running application and the only bridge it has to
 * the FX thread. What is proven here is that a block handed to it really runs on the FX thread, that
 * it still runs when the caller already is on the FX thread, that a failure of the block reaches the
 * caller, and that the application is handed through unchanged.
 */
class StartupContextTest : ApplicationTest() {

    private val application: Application = StubApplication()
    private val context = StartupContext(application)

    override fun start(stage: Stage) {
        // Nothing to show: the tests only need the FX thread the TestFX runtime started.
    }

    /**
     * Use case: a step runs on the startup background thread and wraps toolkit work in
     * [StartupContext.onFxThread], so that work runs on the FX thread and its result comes back to
     * the background thread.
     */
    @Test
    fun runsTheBlockOnTheFxThreadFromABackgroundThread() {
        assertFalse(Platform.isFxApplicationThread(), "the test itself already runs on the FX thread")

        var ranOnFxThread = false
        val result = context.onFxThread {
            ranOnFxThread = Platform.isFxApplicationThread()
            "loaded"
        }

        assertTrue(ranOnFxThread, "the block did not run on the FX thread")
        assertEquals("loaded", result, "the result of the block did not come back")
    }

    /**
     * Use case: a step that is already on the FX thread hands a block to [StartupContext.onFxThread]
     * anyway, so the block runs straight away instead of deadlocking on a second dispatch.
     */
    @Test
    fun runsTheBlockDirectlyWhenAlreadyOnTheFxThread() {
        lateinit var result: String
        interact {
            result = context.onFxThread {
                assertTrue(Platform.isFxApplicationThread(), "the block left the FX thread")
                "direct"
            }
        }

        assertEquals("direct", result, "the block was not run directly on the FX thread")
    }

    /**
     * Use case: the toolkit work of a step fails, so the exception is carried back to the startup
     * thread and aborts the startup rather than being lost on the FX thread.
     */
    @Test
    fun carriesAFailureOfTheBlockBackToTheCaller() {
        val failure = assertThrows(IllegalStateException::class.java) {
            context.onFxThread { throw IllegalStateException("startup step failed") }
        }

        assertEquals("startup step failed", failure.message, "another failure than the one thrown came back")
    }

    /**
     * Use case: a step needs the running application - to stop it, to read its parameters - and takes
     * it from the context instead of its own constructor, so the context hands through the very
     * application it was built with.
     */
    @Test
    fun handsThroughTheRunningApplication() {
        assertSame(application, context.app, "the context handed through another application")
    }

    /** An application that is never started, only handed to the context as its owner. */
    private class StubApplication : Application() {
        override fun start(stage: Stage) = Unit
    }
}

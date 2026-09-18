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
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.pcsoft.app.aighost.app.startup.step.PreferencesStartupStep
import org.testfx.framework.junit5.ApplicationTest
import java.util.Collections
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

/**
 * Developer tests for [Startup].
 *
 * The wiring is driven through the internal entry point with steps of its own and without the one
 * second wait, so what is proven here is the contract the real startup relies on: the steps run in
 * order, control is handed back on the FX thread only once every step finished, the hold passes
 * first, and the steps are discovered from their package and ranked by their [StartupOrder].
 */
class StartupTest : ApplicationTest() {

    private val application: Application = RecordingApplication()

    override fun start(stage: Stage) {
        // Nothing to show: the tests only need the FX thread the TestFX runtime started.
    }

    /**
     * Use case: several startup jobs are supplied, so they run one after another in the order they
     * are handed over and none of them is skipped or reordered.
     */
    @Test
    fun runsTheStepsInTheGivenOrder() {
        val calls = Collections.synchronizedList(mutableListOf<String>())
        val done = CountDownLatch(1)

        Startup.run(
            app = application,
            holdMillis = 0L,
            steps = {
                listOf(
                    RecordingStep("first", calls),
                    RecordingStep("second", calls),
                    RecordingStep("third", calls),
                )
            },
        ) { done.countDown() }

        assertTrue(done.await(5, TimeUnit.SECONDS), "the startup did not finish in time")
        assertEquals(listOf("first", "second", "third"), calls, "the steps did not run in the given order")
    }

    /**
     * Use case: the startup finished, so the caller is called back on the FX thread and can build
     * and show the main window straight away.
     */
    @Test
    fun handsControlBackOnTheFxThread() {
        var onFxThread = false
        val done = CountDownLatch(1)

        Startup.run(application, holdMillis = 0L, steps = { emptyList() }) {
            onFxThread = Platform.isFxApplicationThread()
            done.countDown()
        }

        assertTrue(done.await(5, TimeUnit.SECONDS), "the startup did not finish in time")
        assertTrue(onFxThread, "the caller was not called back on the FX thread")
    }

    /**
     * Use case: the splash screen is meant to rest for a moment before the work starts, so the hold
     * time passes before the first step runs.
     */
    @Test
    fun holdsBeforeTheFirstStep() {
        val calls = Collections.synchronizedList(mutableListOf<String>())
        val done = CountDownLatch(1)
        val start = System.nanoTime()

        Startup.run(application, holdMillis = 300L, steps = { listOf(RecordingStep("only", calls)) }) {
            done.countDown()
        }

        assertTrue(done.await(5, TimeUnit.SECONDS), "the startup did not finish in time")
        val elapsedMillis = (System.nanoTime() - start) / 1_000_000
        assertTrue(elapsedMillis >= 300, "the hold time did not pass before the first step (was ${elapsedMillis}ms)")
        assertEquals(listOf("only"), calls, "the step did not run after the hold")
    }

    /**
     * Use case: a step is added by dropping a class into the step package, so the scan finds it
     * without any registration - proven here through the one step that exists today.
     */
    @Test
    fun discoversTheStepsFromTheStepPackage() {
        val steps = Startup.discoverSteps()

        assertTrue(
            steps.any { it is PreferencesStartupStep },
            "the preferences step was not discovered in the step package",
        )
    }

    /**
     * Use case: the scan gives no order of its own, so the discovered steps come back sorted by the
     * rank of their [StartupOrder], lowest first.
     */
    @Test
    fun ordersTheDiscoveredStepsByTheirRank() {
        val ranks = Startup.discoverSteps().map(Startup::orderOf)

        assertEquals(ranks.sorted(), ranks, "the discovered steps were not ordered by their rank")
    }

    /**
     * Use case: a step states its place in the sequence with [StartupOrder], so its rank is read
     * from that annotation.
     */
    @Test
    fun readsTheRankFromTheOrderAnnotation() {
        assertEquals(7, Startup.orderOf(RankedStep()), "the rank was not read from the annotation")
    }

    /**
     * Use case: a step without [StartupOrder] does not care when it runs, so it is ranked at the
     * default.
     */
    @Test
    fun ranksAnUnannotatedStepAtTheDefault() {
        assertEquals(StartupOrder.DEFAULT, Startup.orderOf(UnrankedStep()), "an unannotated step was not ranked at the default")
    }

    /** A step that only records that it ran, so the order of the steps can be read back. */
    private class RecordingStep(override val name: String, private val calls: MutableList<String>) : StartupStep {
        override fun execute(context: StartupContext) {
            calls.add(name)
        }
    }

    /** A step that states a rank, to prove the rank is read from the annotation. */
    @StartupOrder(7)
    private class RankedStep : StartupStep {
        override val name: String = "ranked"
        override fun execute(context: StartupContext) = Unit
    }

    /** A step without a rank, to prove the default is used. */
    private class UnrankedStep : StartupStep {
        override val name: String = "unranked"
        override fun execute(context: StartupContext) = Unit
    }

    /** An application that records a stop instead of ending the JVM. */
    private class RecordingApplication : Application() {
        override fun start(stage: Stage) = Unit
    }
}

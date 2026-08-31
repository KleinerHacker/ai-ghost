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

import io.github.classgraph.ClassGraph
import javafx.application.Application
import javafx.concurrent.Task
import org.pcsoft.app.aighost.app.Messages
import org.pcsoft.app.aighost.app.ui.AiGhostDialog
import org.pcsoft.app.aighost.app.util.logger

/**
 * The background area that runs behind the splash screen.
 *
 * This is the single place for every job the application has to do before its first window. The
 * splash screen holds still for [HOLD_MILLIS] first, then the steps run one after another on a
 * background thread. When they are done, [run] hands control back on the FX thread so the caller can
 * build and show the main window and fade the splash out.
 *
 * The steps are not kept in a list here: [discoverSteps] scans the package
 * [STEP_PACKAGE] for [StartupStep] implementations, builds each one and sorts them by their
 * [StartupOrder]. A new startup job is added by dropping a class into that package - nothing here
 * changes for it.
 */
object Startup {
    private val log = logger<Startup>()

    /** How long the splash screen is held before the first step runs. */
    private const val HOLD_MILLIS = 1_000L

    /** The package every [StartupStep] implementation lives in. */
    private const val STEP_PACKAGE = "org.pcsoft.app.aighost.app.startup.step"

    /**
     * Runs every startup step in the background and calls [onDone] on the FX thread afterwards.
     *
     * A step that throws stops the startup: the failure is reported and the application is stopped.
     * A step that reports a problem of its own - the preferences step does - handles it itself and
     * does not reach here.
     *
     * @param app the running application
     * @param onDone run on the FX thread once every step finished
     */
    fun run(app: Application, onDone: () -> Unit) {
        run(app, HOLD_MILLIS, ::discoverSteps, onDone)
    }

    /**
     * The wiring behind [run], with the hold time and the step source laid open so a test can drive
     * it with steps of its own and without the one second wait.
     *
     * @param app the running application
     * @param holdMillis how long the splash is held before the first step
     * @param steps supplies the steps to run, in order; evaluated on the background thread
     * @param onDone run on the FX thread once every step finished
     */
    internal fun run(app: Application, holdMillis: Long, steps: () -> List<StartupStep>, onDone: () -> Unit) {
        val task = object : Task<Unit>() {
            override fun call() {
                Thread.sleep(holdMillis)

                val context = StartupContext(app)
                for (step in steps()) {
                    log.debug("Running startup step: {} (order {})", step.name, orderOf(step))
                    step.execute(context)
                }
            }
        }
        task.setOnSucceeded {
            log.debug("Startup finished")
            onDone()
        }
        task.setOnFailed {
            val error = task.exception
            log.error("Startup failed", error)
            AiGhostDialog.showError(
                Messages["text.startup.error.title"],
                Messages["text.startup.error.header"],
                error?.localizedMessage ?: Messages["text.startup.error.unknown"],
            )
            app.stop()
        }

        Thread(task, "ai-ghost-startup").apply { isDaemon = true }.start()
    }

    /**
     * Scans [STEP_PACKAGE] for [StartupStep] implementations, builds each one with its no-argument
     * constructor and returns them sorted by [StartupOrder], lowest first, class name breaking a tie.
     *
     * @return the discovered steps in the order they are to run
     */
    internal fun discoverSteps(): List<StartupStep> {
        ClassGraph()
            .enableClassInfo()
            .acceptPackages(STEP_PACKAGE)
            .scan()
            .use { result ->
                return result.getClassesImplementing(StartupStep::class.java.name)
                    .standardClasses
                    .filter { !it.isAbstract }
                    .map { build(it.loadClass(StartupStep::class.java)) }
                    .sortedWith(compareBy({ orderOf(it) }, { it.javaClass.name }))
            }
    }

    /** Reads the rank of [step] from its [StartupOrder], or [StartupOrder.DEFAULT] when it has none. */
    internal fun orderOf(step: StartupStep): Int =
        step.javaClass.getAnnotation(StartupOrder::class.java)?.value ?: StartupOrder.DEFAULT

    /** Builds a step through its public no-argument constructor. */
    private fun build(type: Class<out StartupStep>): StartupStep =
        try {
            type.getDeclaredConstructor().newInstance()
        } catch (e: ReflectiveOperationException) {
            throw IllegalStateException("Startup step ${type.name} needs a public no-argument constructor", e)
        }
}

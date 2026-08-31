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
import java.util.concurrent.CountDownLatch
import java.util.concurrent.atomic.AtomicReference

/**
 * Handed to every [StartupStep] so it can reach the running application and the FX thread from the
 * startup background thread.
 *
 * A step is built by [Startup] with a no-argument constructor, so whatever it needs from the
 * application - to stop it, to read its parameters - it takes from here rather than from its own
 * constructor.
 *
 * A step itself runs off the FX thread. Whenever it has to touch the toolkit - show a dialog, build
 * a stage, install the theme - it wraps that work in [onFxThread], which runs it on the FX thread
 * and blocks the step until it is done.
 *
 * @property app the running application
 */
class StartupContext(val app: Application) {

    /**
     * Runs [block] on the FX thread and returns its result.
     *
     * When the caller already is on the FX thread, [block] is run directly. Otherwise the calling
     * thread waits until the FX thread ran it. An exception of [block] is rethrown to the caller.
     *
     * @param block the work that needs the FX thread
     * @return whatever [block] returned
     */
    fun <T> onFxThread(block: () -> T): T {
        if (Platform.isFxApplicationThread()) {
            return block()
        }

        val outcome = AtomicReference<Result<T>>()
        val latch = CountDownLatch(1)
        Platform.runLater {
            outcome.set(runCatching(block))
            latch.countDown()
        }
        latch.await()
        return outcome.get().getOrThrow()
    }
}

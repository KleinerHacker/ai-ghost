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

package org.pcsoft.app.aighost.app.ui.splash

import javafx.scene.image.ImageView
import javafx.scene.paint.Color
import javafx.stage.Stage
import javafx.stage.StageStyle
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.testfx.framework.junit5.ApplicationTest
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

/**
 * Developer tests for [SplashStage].
 *
 * The splash is meant to float on the desktop without a frame and to disappear once the startup is
 * done, so what is proven here is that the window carries no decoration and no background, that it
 * shows nothing but the motive, that it reports back only once it stands fully - so the startup work
 * does not begin during the fade in - and that [SplashStage.fadeOut] both closes the window and
 * calls back.
 */
class SplashStageTest : ApplicationTest() {

    private lateinit var splash: SplashStage

    override fun start(stage: Stage) {
        splash = SplashStage()
    }

    /**
     * Use case: the motive is to appear to float directly on the desktop, so the window has the
     * transparent style, a transparent scene fill, stays on top, cannot be resized and starts
     * invisible for the fade in.
     */
    @Test
    fun hasNoFrameAndNoBackground() {
        assertEquals(StageStyle.TRANSPARENT, splash.style, "the splash carries a window frame")
        assertEquals(Color.TRANSPARENT, splash.scene.fill, "the splash paints a background")
        assertTrue(splash.isAlwaysOnTop, "the splash can be covered by other windows")
        assertFalse(splash.isResizable, "the splash can be resized")
        assertEquals(0.0, splash.opacity, 0.001, "the splash does not start invisible for the fade in")
    }

    /**
     * Use case: the splash shows the application motive and nothing else - no label, no control.
     */
    @Test
    fun showsOnlyTheMotive() {
        val root = splash.scene.root
        val images = root.lookupAll(".image-view").filterIsInstance<ImageView>()

        assertEquals(1, images.size, "the splash shows something other than a single motive")
        assertNotNull(images.first().image, "the motive image was not loaded")
        assertTrue(images.first().image.width > 0.0, "the motive image is empty")
    }

    /**
     * Use case: the startup work must not begin while the motive is still fading in, so [reveal]
     * reports back only once the window stands fully opaque.
     */
    @Test
    fun reportsOnlyOnceItStandsFully() {
        val fullyShown = CountDownLatch(1)
        interact { splash.reveal { fullyShown.countDown() } }

        assertTrue(fullyShown.await(5, TimeUnit.SECONDS), "the splash never reported that it stands fully")
        assertTrue(splash.isShowing, "the splash is not on screen when it reports fully shown")
        assertEquals(1.0, splash.opacity, 0.001, "the window had not finished fading in when it reported back")
    }

    /**
     * Use case: the startup is done, so the splash fades the whole window out, closes it and calls
     * back so the caller can bring the main window forward.
     */
    @Test
    fun fadeOutClosesTheWindowAndCallsBack() {
        val shown = CountDownLatch(1)
        interact { splash.reveal { shown.countDown() } }
        assertTrue(shown.await(5, TimeUnit.SECONDS), "the splash was not shown in time")
        assertTrue(splash.isShowing, "the splash did not open")

        val closed = CountDownLatch(1)
        interact { splash.fadeOut { closed.countDown() } }

        assertTrue(closed.await(5, TimeUnit.SECONDS), "the fade out did not finish in time")
        assertFalse(splash.isShowing, "the splash stayed open after the fade out")
        assertEquals(0.0, splash.opacity, 0.05, "the window did not fade out before closing")
    }
}

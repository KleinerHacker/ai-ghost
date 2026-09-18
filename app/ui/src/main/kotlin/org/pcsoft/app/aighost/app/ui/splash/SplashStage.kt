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

import javafx.animation.*
import javafx.geometry.Insets
import javafx.scene.Scene
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.layout.StackPane
import javafx.scene.paint.Color
import javafx.stage.Screen
import javafx.stage.Stage
import javafx.stage.StageStyle
import javafx.util.Duration
import org.pcsoft.app.aighost.app.AiGhostIcons

/**
 * The splash shown while the application starts up.
 *
 * It is a borderless, fully transparent window that carries nothing but the application motive, so
 * the motive appears to float directly on the desktop. It is not decorated with the application
 * theme: it shows no styled control and the colour scheme is not resolved yet while it is on screen.
 *
 * The whole window is faded in and out through its own opacity, not the opacity of the motive, so
 * the transparent window never composites a bare surface around the motive for a frame. While it is
 * up, a subtle endless animation keeps the motive alive - it breathes in opacity, drifts up and down
 * and pulses in size - so the user sees that the start is still working.
 *
 * [reveal] fades the window in and reports once it stands fully; [fadeOut] fades it out again and
 * closes it. The startup work is meant to begin only from the [reveal] callback, so it never runs
 * while the motive is still fading in.
 */
class SplashStage : Stage(StageStyle.TRANSPARENT) {

    /** Edge length in pixels the motive is rendered at. */
    private val motiveSize = 320.0

    /** Room around the motive so the drift and the pulse are not clipped by the window. */
    private val motivePadding = 32.0

    /** Duration of the fade in and the fade out of the window. */
    private val fadeDuration: Duration = Duration.millis(300.0)

    /** Duration of one half cycle of the endless breathing, drifting and pulsing. */
    private val breathDuration: Duration = Duration.millis(1200.0)

    private val motive: ImageView = ImageView(Image(loadMotive())).apply {
        fitWidth = motiveSize
        fitHeight = motiveSize
        isPreserveRatio = true
        isSmooth = true
    }

    private val root: StackPane = StackPane(motive).apply {
        // No background is set, so the region stays transparent around the motive.
        isPickOnBounds = false
        StackPane.setMargin(motive, Insets(motivePadding))
    }

    /** The endless breathing, drifting and pulsing of the motive, running while the splash is up. */
    private val idleAnimation: ParallelTransition = ParallelTransition(
        motive,
        FadeTransition(breathDuration).apply {
            fromValue = 0.7
            toValue = 0.5
        },
        TranslateTransition(breathDuration).apply {
            fromY = -5.0
            toY = 5.0
        },
        ScaleTransition(breathDuration).apply {
            fromX = 0.9
            fromY = 0.9
            toX = 1.1
            toY = 1.1
        },
    ).apply {
        interpolator = Interpolator.LINEAR
        cycleCount = Animation.INDEFINITE
        isAutoReverse = true
    }

    init {
        isAlwaysOnTop = true
        isResizable = false
        icons.setAll(AiGhostIcons.application)
        scene = Scene(root, Color.TRANSPARENT)
        motive.opacity = 0.0
        motive.scaleX = 0.9
        motive.scaleY = 0.9
        motive.translateY = -5.0
    }

    /**
     * Shows the splash centred on the primary screen, fades the whole window in and, once it stands
     * fully, starts the endless breathing and reports back.
     *
     * Named apart from [show] because [javafx.stage.Stage.show] is final and cannot be overridden.
     *
     * @param onFullyShown run on the FX thread once the fade in finished - the startup work belongs here
     */
    fun reveal(onFullyShown: () -> Unit = {}) {
        show()

        sizeToScene()
        centerOnPrimaryScreen()

        fade(to = 0.7) {
            idleAnimation.play()
            onFullyShown()
        }
    }

    /**
     * Stops the endless breathing, fades the whole window out and closes it afterwards.
     *
     * @param onFinished run on the FX thread once the window is closed
     */
    fun fadeOut(onFinished: () -> Unit) {
        idleAnimation.stop()

        fade(to = 0.0) {
            close()
            onFinished()
        }
    }

    /** Animates the window opacity to [to] over [fadeDuration] and runs [onFinished] afterwards. */
    private fun fade(to: Double, onFinished: () -> Unit) {
        FadeTransition(fadeDuration, motive).apply {
            toValue = to
            setOnFinished { onFinished() }
        }.play()
    }

    private fun centerOnPrimaryScreen() {
        val bounds = Screen.getPrimary().visualBounds
        x = bounds.minX + (bounds.width - width) / 2.0
        y = bounds.minY + (bounds.height - height) / 2.0
    }

    private fun loadMotive() = requireNotNull(SplashStage::class.java.getResourceAsStream(MOTIVE_PATH)) {
        "Splash motive resource not found: $MOTIVE_PATH"
    }

    private companion object {
        /** Resource path of the floating start-up motive, derived from `docs/docs/assets/images/icon.png`. */
        const val MOTIVE_PATH = "/images/splash.png"
    }
}

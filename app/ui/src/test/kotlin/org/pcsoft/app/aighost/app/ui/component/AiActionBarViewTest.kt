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

package org.pcsoft.app.aighost.app.ui.component

import de.saxsys.mvvmfx.FluentViewLoader
import de.saxsys.mvvmfx.MvvmFX
import javafx.geometry.Pos
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.scene.control.Button
import javafx.scene.layout.HBox
import javafx.scene.layout.StackPane
import javafx.stage.Stage
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.pcsoft.app.aighost.app.Messages
import org.testfx.framework.junit5.ApplicationTest
import org.testfx.util.WaitForAsyncUtils
import java.util.Locale
import java.util.ResourceBundle
import java.util.concurrent.TimeUnit

/**
 * Developer tests for [AiActionBarView] - the three AI buttons of the floating action bar (IP-18).
 *
 * Every button is only wired to an empty method for now, exactly as the IP-18 plan asks for: no
 * wiring to `lib/ai`, no busy state, no undo entry. These tests prove no more than that the bar is
 * reachable for the user, that its target methods mark themselves as not implemented, and that the
 * hover fade actually moves the opacity.
 */
class AiActionBarViewTest : ApplicationTest() {

    private lateinit var controller: AiActionBarView
    private lateinit var view: Parent
    private lateinit var root: HBox

    private val rewriteButton: Button get() = view.lookup("#btnRewrite") as Button
    private val expandButton: Button get() = view.lookup("#btnExpand") as Button
    private val shortenButton: Button get() = view.lookup("#btnShorten") as Button

    override fun start(stage: Stage) {
        // No fallback, so the English base bundle is used no matter which locale the build runs under.
        MvvmFX.setGlobalResourceBundle(
            ResourceBundle.getBundle(
                Messages.BUNDLE_NAME,
                Locale.ROOT,
                ResourceBundle.Control.getNoFallbackControl(ResourceBundle.Control.FORMAT_PROPERTIES)
            )
        )

        // AiActionBarView's FXML is an fx:root document, so the root instance has to be supplied
        // upfront - exactly what AiActionBar does in production.
        root = HBox()
        val tuple = FluentViewLoader.fxmlView(AiActionBarView::class.java).root(root).load()
        controller = tuple.codeBehind
        view = tuple.view

        // The bar sits in a corner of a much larger scene, so there is empty space to move the mouse
        // into for fadesBackToHalfOpacityWhenTheMouseLeaves - the bar itself never fills its container.
        val container = StackPane(view).apply { alignment = Pos.TOP_LEFT }
        stage.scene = Scene(container, 600.0, 400.0)
        stage.show()
    }

    /** Use case: the bar shows all three AI buttons, so the user can reach every action. */
    @Test
    fun showsAllThreeButtons() {
        assertNotNull(rewriteButton, "the rewrite button is missing")
        assertNotNull(expandButton, "the expand button is missing")
        assertNotNull(shortenButton, "the shorten button is missing")
    }

    /**
     * Use case: every button is icon-only - its meaning comes from its tooltip, not from a word next
     * to it, so the compact floating bar stays as small as its three icons.
     */
    @Test
    fun everyButtonIsIconOnly() {
        assertNotNull(rewriteButton.graphic, "the rewrite button must carry an icon")
        assertNotNull(expandButton.graphic, "the expand button must carry an icon")
        assertNotNull(shortenButton.graphic, "the shorten button must carry an icon")
        assertTrue(rewriteButton.text.isNullOrEmpty(), "the rewrite button must carry no text")
        assertTrue(expandButton.text.isNullOrEmpty(), "the expand button must carry no text")
        assertTrue(shortenButton.text.isNullOrEmpty(), "the shorten button must carry no text")
    }

    /**
     * Use case: the target method of the "Rewrite" button is not implemented yet - the actual
     * rewriting belongs to a future feature that wires up `lib/ai` - so calling it marks that clearly.
     */
    @Test
    fun rewriteThrowsNotImplemented() {
        assertThrows(NotImplementedError::class.java) { controller.actionRewrite() }
    }

    /**
     * Use case: the target method of the "Expand" button is not implemented yet - the actual expansion
     * belongs to a future feature that wires up `lib/ai` - so calling it marks that clearly.
     */
    @Test
    fun expandThrowsNotImplemented() {
        assertThrows(NotImplementedError::class.java) { controller.actionExpand() }
    }

    /**
     * Use case: the target method of the "Shorten" button is not implemented yet - the actual
     * shortening belongs to a future feature that wires up `lib/ai` - so calling it marks that clearly.
     */
    @Test
    fun shortenThrowsNotImplemented() {
        assertThrows(NotImplementedError::class.java) { controller.actionShorten() }
    }

    /**
     * Use case: the bar starts half-transparent, so it stays out of the way of reading while the
     * mouse is elsewhere.
     */
    @Test
    fun startsFaded() {
        assertEquals(0.5, root.opacity, 0.001)
    }

    /**
     * Use case: the user moves the mouse directly over the bar, so it fades to full opacity instead of
     * staying half-transparent.
     */
    @Test
    fun fadesToFullOpacityOnHover() {
        moveTo(root)

        WaitForAsyncUtils.waitFor(2, TimeUnit.SECONDS) { root.opacity >= 0.999 }
        assertEquals(1.0, root.opacity, 0.001)
    }

    /**
     * Use case: the user moves the mouse away from the bar again, so it fades back to half-transparent
     * instead of staying fully opaque.
     */
    @Test
    fun fadesBackToHalfOpacityWhenTheMouseLeaves() {
        moveTo(root)
        WaitForAsyncUtils.waitFor(2, TimeUnit.SECONDS) { root.opacity >= 0.999 }

        // The bar sits in the top-left corner of a much larger scene (see start()), so the bottom-right
        // corner is always empty space, no matter the bar's own size.
        moveTo(root).moveBy(400.0, 300.0)
        WaitForAsyncUtils.waitFor(2, TimeUnit.SECONDS) { root.opacity <= 0.501 }
        assertEquals(0.5, root.opacity, 0.001)
    }
}

package org.pcsoft.app.aighost.app.ui

import javafx.beans.binding.BooleanBinding
import javafx.scene.Scene
import javafx.scene.control.Label
import javafx.scene.layout.StackPane
import javafx.stage.Stage
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.testfx.framework.junit5.ApplicationTest
import org.testfx.util.WaitForAsyncUtils

/**
 * Developer tests for the component bound view of the screen state, [showingBinding].
 */
class FxNodesTest : ApplicationTest() {

    private lateinit var stage: Stage
    private lateinit var parent: StackPane
    private lateinit var component: Label

    // Kept as a field, as the documentation of the binding demands.
    private lateinit var showing: BooleanBinding

    override fun start(stage: Stage) {
        this.stage = stage

        component = Label("component")
        parent = StackPane(component)
        showing = component.showingBinding()

        stage.scene = Scene(parent)
        stage.show()
    }

    /** Reads the binding on the FX thread, after every pending event was processed. */
    private fun isShowing(): Boolean =
        WaitForAsyncUtils.asyncFx<Boolean> { showing.get() }.get()

    /**
     * Use case: a visible component inside a window that is shown counts as being on screen.
     */
    @Test
    fun componentInAShownWindowIsOnScreen() {
        assertTrue(isShowing())
    }

    /**
     * Use case: hiding the component alone takes it off screen, even though its window stays open,
     * and showing it again brings it back.
     */
    @Test
    fun hidingTheComponentTakesItOffScreen() {
        interact { component.isVisible = false }
        assertFalse(isShowing())

        interact { component.isVisible = true }
        assertTrue(isShowing())
    }

    /**
     * Use case: closing the window takes the component off screen as well, so a registration bound
     * to the binding is released instead of outliving the window.
     */
    @Test
    fun hidingTheWindowTakesTheComponentOffScreen() {
        interact { stage.hide() }
        assertFalse(isShowing())

        interact { stage.show() }
        assertTrue(isShowing())
    }

    /**
     * Use case: a component that is taken out of the scene is off screen, and it is on screen again
     * once it is added back.
     */
    @Test
    fun removingTheComponentFromTheSceneTakesItOffScreen() {
        interact { parent.children.remove(component) }
        assertFalse(isShowing())

        interact { parent.children.add(component) }
        assertTrue(isShowing())
    }
}

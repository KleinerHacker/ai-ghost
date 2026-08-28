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

package org.pcsoft.app.aighost.app.ui.dialog

import de.saxsys.mvvmfx.FxmlView
import de.saxsys.mvvmfx.InjectViewModel
import javafx.animation.Interpolator
import javafx.animation.KeyFrame
import javafx.animation.KeyValue
import javafx.animation.Timeline
import javafx.beans.property.DoubleProperty
import javafx.beans.property.SimpleDoubleProperty
import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.control.TextArea
import javafx.scene.image.ImageView
import javafx.scene.layout.VBox
import javafx.stage.Stage
import javafx.util.Duration
import java.net.URL
import java.util.ResourceBundle

/**
 * View of [DetailDialog]: icon and caption on top, the message below it and a details pane the user
 * unfolds with a button.
 *
 * The details pane grows and shrinks in height only. It asks for no width of its own - it is
 * stretched to the width the header above it needs - so unfolding it never makes the dialog wider.
 *
 * The animation drives [detailsHeight], and every step of it hands the new height to the pane and to
 * the window in the same breath. The window is *not* asked to size itself to its content while the
 * animation runs: that measures the whole dialog again in the middle of the layout pass the
 * animation just triggered, which drops frames and leaves the window standing at a height of the
 * animation. Everything here happens on the JavaFX application thread - the button action, the
 * timeline and the listener alike.
 */
class DetailDialogView : FxmlView<DetailDialogViewModel>, Initializable {

    private companion object {
        /** Height in pixels the unfolded details pane grows to. */
        const val DETAILS_HEIGHT: Double = 160.0

        /** How long the details pane takes to grow or to shrink. */
        val DETAILS_DURATION: Duration = Duration.millis(180.0)
    }

    @FXML
    private lateinit var pnlRoot: VBox

    @FXML
    private lateinit var imgIcon: ImageView

    @FXML
    private lateinit var lblCaption: Label

    @FXML
    private lateinit var lblMessage: Label

    @FXML
    private lateinit var btnDetails: Button

    @FXML
    private lateinit var txaDetails: TextArea

    @InjectViewModel
    private lateinit var viewModel: DetailDialogViewModel

    /** Height of the details pane, the single value the animation moves. */
    private val detailsHeight: DoubleProperty = SimpleDoubleProperty(this, "detailsHeight", 0.0)

    private val detailsAnimation = Timeline()

    /** Height of the window without the details pane, taken when an animation starts. */
    private var foldedWindowHeight: Double = 0.0

    override fun initialize(location: URL?, resources: ResourceBundle?) {
        imgIcon.imageProperty().bind(viewModel.icon)
        lblCaption.textProperty().bind(viewModel.caption)
        lblMessage.textProperty().bind(viewModel.message)
        txaDetails.textProperty().bind(viewModel.details)

        btnDetails.textProperty().bind(viewModel.detailsButtonText)
        btnDetails.setOnAction { viewModel.toggleDetails() }

        detailsHeight.addListener { _, _, height ->
            txaDetails.prefHeight = height.toDouble()
            // The width is left untouched, so the window grows downwards and nowhere else.
            pnlRoot.scene?.window?.height = foldedWindowHeight + height.toDouble()
        }

        viewModel.detailsVisible.addListener { _, _, visible -> animateDetails(visible) }
    }

    /**
     * Grows the details pane into view or shrinks it away again.
     *
     * The pane takes part in the layout for the whole animation, so its height is what moves; it
     * leaves the layout only once it has shrunk to nothing, and it joins again before it grows. A
     * running animation is turned around from where it stands, so a second click does not jump.
     *
     * @param visible whether the pane is being unfolded
     */
    private fun animateDetails(visible: Boolean) {
        detailsAnimation.stop()

        // The height the window has without the pane - the pane may still stand somewhere in
        // between, because the animation before this one was turned around.
        val window = pnlRoot.scene?.window
        foldedWindowHeight = (window?.height ?: 0.0) - detailsHeight.value

        // A dialog that is not resizable measures itself against its content on every pulse and
        // would take the height of the animation away again, so it is resizable while the animation
        // runs and is nailed to its content again as soon as it stands still.
        (window as? Stage)?.isResizable = true

        if (visible) {
            txaDetails.isVisible = true
            txaDetails.isManaged = true
        }

        detailsAnimation.keyFrames.setAll(
            KeyFrame(
                DETAILS_DURATION,
                KeyValue(detailsHeight, if (visible) DETAILS_HEIGHT else 0.0, Interpolator.EASE_BOTH)
            )
        )
        detailsAnimation.setOnFinished {
            if (!visible) {
                txaDetails.isVisible = false
                txaDetails.isManaged = false
            }
            // The window measures itself against its content again and is nailed to it, so the
            // dialog cannot be dragged to another size once it stands still.
            pnlRoot.scene?.window?.sizeToScene()
            (pnlRoot.scene?.window as? Stage)?.isResizable = false
        }
        detailsAnimation.playFromStart()
    }
}

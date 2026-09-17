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

import de.saxsys.mvvmfx.FxmlView
import de.saxsys.mvvmfx.InjectViewModel
import javafx.animation.FadeTransition
import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.scene.layout.HBox
import javafx.util.Duration
import java.net.URL
import java.util.ResourceBundle

/**
 * View of [AiActionBar], holding the three buttons of the floating AI action bar.
 *
 * The bar is half-transparent while the mouse is elsewhere and fades to full opacity while the mouse
 * sits directly over it, so it stays out of the way of reading without disappearing entirely. No
 * button carries any logic beyond marking itself as not implemented (IP-18): the real actions are the
 * job of a later feature, once `lib/ai`'s action port is actually wired up.
 */
class AiActionBarView : FxmlView<AiActionBarViewModel>, Initializable {

    @FXML
    private lateinit var pnlRoot: HBox

    @InjectViewModel
    private lateinit var viewModel: AiActionBarViewModel

    override fun initialize(location: URL?, resources: ResourceBundle?) {
        pnlRoot.opacity = FADED_OPACITY
        pnlRoot.hoverProperty().addListener { _, _, hovering ->
            fade(if (hovering) FULL_OPACITY else FADED_OPACITY)
        }
    }

    private fun fade(to: Double) {
        FadeTransition(FADE_DURATION, pnlRoot).apply {
            toValue = to
        }.play()
    }

    /**
     * Bound to the "Rewrite" button.
     *
     * The actual rewriting belongs to a future feature that wires up `lib/ai`'s action port; this
     * method is intentionally left unimplemented until that infrastructure exists.
     */
    @FXML
    fun actionRewrite() {
        TODO("AI action: rewrite")
    }

    /**
     * Bound to the "Expand" button.
     *
     * The actual expansion belongs to a future feature that wires up `lib/ai`'s action port; this
     * method is intentionally left unimplemented until that infrastructure exists.
     */
    @FXML
    fun actionExpand() {
        TODO("AI action: expand")
    }

    /**
     * Bound to the "Shorten" button.
     *
     * The actual shortening belongs to a future feature that wires up `lib/ai`'s action port; this
     * method is intentionally left unimplemented until that infrastructure exists.
     */
    @FXML
    fun actionShorten() {
        TODO("AI action: shorten")
    }

    private companion object {
        /** Opacity of the bar while the mouse is not directly over it. */
        const val FADED_OPACITY: Double = 0.5

        /** Opacity of the bar while the mouse sits over it. */
        const val FULL_OPACITY: Double = 1.0

        /** Duration of the fade between [FADED_OPACITY] and [FULL_OPACITY]. */
        val FADE_DURATION: Duration = Duration.millis(150.0)
    }
}

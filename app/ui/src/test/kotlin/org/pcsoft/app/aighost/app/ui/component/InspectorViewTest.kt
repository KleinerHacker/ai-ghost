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
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.scene.control.Button
import javafx.scene.layout.BorderPane
import javafx.stage.Stage
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.pcsoft.app.aighost.app.Messages
import org.testfx.framework.junit5.ApplicationTest
import java.util.Locale
import java.util.ResourceBundle

/**
 * Developer tests for the "Generate chapter" AI button of [InspectorView].
 *
 * The button is only wired to an empty method for now - see [InspectorView.generatePart] - so these
 * tests prove no more than that it is reachable for the user and that its target method still marks
 * itself as not implemented, exactly as the IP-19 plan asks for.
 */
class InspectorViewTest : ApplicationTest() {

    private lateinit var controller: InspectorView
    private lateinit var view: Parent

    private val generateButton: Button
        get() = view.lookup("#btnGenerateChapter") as Button

    override fun start(stage: Stage) {
        // No fallback, so the English base bundle is used no matter which locale the build runs under.
        MvvmFX.setGlobalResourceBundle(
            ResourceBundle.getBundle(
                Messages.BUNDLE_NAME,
                Locale.ROOT,
                ResourceBundle.Control.getNoFallbackControl(ResourceBundle.Control.FORMAT_PROPERTIES)
            )
        )

        // InspectorView's FXML is an fx:root document, so the root instance has to be supplied
        // upfront - exactly what Inspector does in production.
        val tuple = FluentViewLoader.fxmlView(InspectorView::class.java).root(BorderPane()).load()
        controller = tuple.codeBehind
        view = tuple.view

        stage.scene = Scene(view, 400.0, 800.0)
        stage.show()
    }

    /**
     * Use case: the "Chapter" section offers the "Generate chapter" AI button, and it is enabled so the
     * user can actually press it once a chapter is picked and the section shows its fields.
     */
    @Test
    fun generateChapterButtonIsPresentAndEnabled() {
        assertNotNull(generateButton, "the button is missing")
        assertFalse(generateButton.isDisable, "the button is disabled")
    }

    /**
     * Use case: the button's target method is not implemented yet - the actual generation belongs to
     * the future plugin system feature - so calling it marks that clearly instead of doing nothing or
     * pretending to work.
     */
    @Test
    fun generatePartThrowsNotImplemented() {
        assertThrows(NotImplementedError::class.java) { controller.generatePart() }
    }
}

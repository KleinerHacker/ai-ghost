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

import javafx.application.Application
import javafx.geometry.Insets
import javafx.scene.Scene
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.layout.VBox
import javafx.stage.Stage
import org.pcsoft.app.aighost.app.AiGhostIcons
import org.pcsoft.app.aighost.app.AiGhostTheme
import org.pcsoft.app.aighost.app.ui.AiGhostDialog

/**
 * Opens every dialog of the application on demand, for the eye of a human being.
 *
 * This is no test and it is disabled as far as the build is concerned: it lives in the source set
 * `demo`, which is never started by `build` or `test` and is not shipped with the application - only
 * compiled along the way, so a demo that no longer matches its dialogs is noticed. It exists for the
 * check the automated tests cannot do - whether a dialog *looks*
 * right: the contrast of its icon in both colour schemes, the wrapping of its texts, and how the
 * window grows when the details pane is unfolded.
 *
 * Start it on purpose with `gradlew :app:ai-ghost-ui:runDialogDemo`. The colour scheme is the one of
 * the preferences and is read while starting, so checking the other appearance means changing
 * `appearance.themeMode` in `preferences.json` and starting the demo again.
 */
class DialogDemo : Application() {

    override fun start(stage: Stage) {
        AiGhostTheme.install()

        val details = listOf("- outline", "- plugin: mind map", "- plugin: timeline")
            .joinToString(System.lineSeparator())

        val buttons = listOf(
            Button("Simple error").apply {
                setOnAction {
                    AiGhostDialog.showError(
                        "Error (Open project)",
                        "The project could not be opened.",
                        "The structure of the project file is broken.",
                        stage
                    )
                }
            },
            Button("Simple warning (OK)").apply {
                setOnAction {
                    AiGhostDialog.showWarning(
                        "Warning (Open project)",
                        "Attention: this project is incomplete!",
                        "The project can be opened, but parts of its content could not be read.",
                        stage
                    )
                }
            },
            Button("Simple warning (Yes / No)").apply {
                setOnAction {
                    val answer = AiGhostDialog.showWarningConfirm(
                        "Reset preferences",
                        "The preferences could not be loaded.",
                        "Do you want to reset the preferences?",
                        stage
                    )
                    println("simple warning answered with $answer")
                }
            },
            Button("Detailed error").apply {
                setOnAction {
                    AiGhostDialog.showErrorDetails(
                        "Error (Save project)",
                        "The project could not be saved.",
                        "The project file could not be written. The parts below were not stored.",
                        details,
                        stage
                    )
                }
            },
            Button("Detailed warning (OK)").apply {
                setOnAction {
                    AiGhostDialog.showWarningDetails(
                        "Warning (Open project)",
                        "Attention: this project is incomplete!",
                        "The project can be opened, but parts of its content could not be read and are lost.",
                        details,
                        stage
                    )
                }
            },
            Button("Detailed warning (Yes / No)").apply {
                setOnAction {
                    val answer = AiGhostDialog.showWarningConfirmDetails(
                        "Warning (Open project)",
                        "Attention: this project is incomplete!",
                        "The project can be opened, but parts of its content could not be read and are lost. " +
                                "Saving removes them from the file for good.",
                        details,
                        stage
                    )
                    println("detailed warning answered with $answer")
                }
            }
        )

        val root = VBox(10.0, Label("Colour scheme: ${AiGhostTheme.colorScheme}")).apply {
            padding = Insets(16.0)
            children += buttons
        }

        stage.icons.setAll(AiGhostIcons.application)
        stage.title = "AI Ghost - dialog demo"
        stage.scene = Scene(root, 320.0, 300.0).also(AiGhostTheme::apply)
        stage.show()
    }
}

/** Entry point of the demo, started by the Gradle task `runDialogDemo` only. */
fun main() {
    Application.launch(DialogDemo::class.java)
}

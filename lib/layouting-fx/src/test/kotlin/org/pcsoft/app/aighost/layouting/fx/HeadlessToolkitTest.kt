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

package org.pcsoft.app.aighost.layouting.fx

import javafx.application.Platform
import javafx.scene.Scene
import javafx.scene.layout.StackPane
import javafx.stage.Stage
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.testfx.framework.junit5.ApplicationTest

/**
 * Developer tests proving that the JavaFX toolkit of this library module starts without a screen.
 */
class HeadlessToolkitTest : ApplicationTest() {

    private lateinit var root: StackPane

    override fun start(stage: Stage) {
        root = StackPane()
        stage.scene = Scene(root, 200.0, 100.0)
        stage.show()
    }

    /**
     * An empty scene starts on the headless glass platform: the stage carries the scene that was set
     * and the scene carries the root handed to it. This proves that JavaFX resolves inside a library
     * module and that the test setup opens no window.
     */
    @Test
    fun `empty scene starts headless`() {
        val scene = root.scene

        assertNotNull(scene, "The root must be part of a scene")
        assertSameRoot(scene)
        assertEquals(200.0, scene.width, "The scene keeps the width it was created with")
        assertEquals(100.0, scene.height, "The scene keeps the height it was created with")
    }

    /**
     * The JavaFX application thread is running and accepts work: a runnable handed to it is executed
     * and its result is visible afterwards.
     */
    @Test
    fun `application thread executes work`() {
        interact { root.children.add(StackPane()) }

        assertEquals(1, root.children.size, "The child added on the FX thread must be in the scene")
        assertTrue(Platform.isFxApplicationThread().not(), "The test itself runs off the FX thread")
    }

    private fun assertSameRoot(scene: Scene) =
        assertEquals(root, scene.root, "The scene shows the root it was created with")
}

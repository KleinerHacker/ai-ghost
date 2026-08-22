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

package org.pcsoft.app.aighost.app

import javafx.scene.image.Image
import javafx.scene.image.ImageView
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotSame
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.testfx.framework.junit5.ApplicationExtension

/**
 * Developer tests for [AiGhostIcons].
 */
@ExtendWith(ApplicationExtension::class)
class AiGhostIconsTest {

    companion object {
        /**
         * Supplies every menu icon of [AiGhostIcons] together with its property name, so a failure
         * names the icon that could not be loaded.
         *
         * @return pairs of property name and image
         */
        @JvmStatic
        fun menuIcons(): List<Array<Any>> = listOf(
            arrayOf("epilog", AiGhostIcons.epilog),
            arrayOf("prolog", AiGhostIcons.prolog),
            arrayOf("chapter", AiGhostIcons.chapter),
            arrayOf("blurb", AiGhostIcons.blurb),
            arrayOf("open", AiGhostIcons.open),
            arrayOf("save", AiGhostIcons.save),
            arrayOf("saveAs", AiGhostIcons.saveAs),
            arrayOf("preferences", AiGhostIcons.preferences),
            arrayOf("projectSettings", AiGhostIcons.projectSettings),
            arrayOf("export", AiGhostIcons.export),
            arrayOf("helpOnline", AiGhostIcons.helpOnline),
        )

        /**
         * Supplies every menu graphic factory of [AiGhostIcons] together with its method name, so a
         * failure names the factory that did not deliver a usable image view.
         *
         * @return pairs of method name and image view
         */
        @JvmStatic
        fun menuGraphics(): List<Array<Any>> = listOf(
            arrayOf("menuEpilog", AiGhostIcons.menuEpilog()),
            arrayOf("menuProlog", AiGhostIcons.menuProlog()),
            arrayOf("menuChapter", AiGhostIcons.menuChapter()),
            arrayOf("menuBlurb", AiGhostIcons.menuBlurb()),
            arrayOf("menuOpen", AiGhostIcons.menuOpen()),
            arrayOf("menuSave", AiGhostIcons.menuSave()),
            arrayOf("menuSaveAs", AiGhostIcons.menuSaveAs()),
            arrayOf("menuPreferences", AiGhostIcons.menuPreferences()),
            arrayOf("menuProjectSettings", AiGhostIcons.menuProjectSettings()),
            arrayOf("menuExport", AiGhostIcons.menuExport()),
            arrayOf("menuHelpOnline", AiGhostIcons.menuHelpOnline()),
        )
    }

    /**
     * Use case: every menu icon resource exists and can be decoded, so no menu entry ends up
     * without its graphic at runtime.
     */
    @ParameterizedTest(name = "menu icon {0}")
    @MethodSource("menuIcons")
    fun menuIconIsLoadable(name: String, image: Image) {
        assertFalse(image.isError, "icon $name could not be loaded")
        assertTrue(image.width > 0.0 && image.height > 0.0, "icon $name is empty")
    }

    /**
     * Use case: the application icon is available in every declared size, so the window manager can
     * pick the size it needs.
     */
    @Test
    fun applicationIconIsAvailableInEverySize() {
        assertEquals(AiGhostIcons.APPLICATION_ICON_SIZES.size, AiGhostIcons.application.size)
        AiGhostIcons.APPLICATION_ICON_SIZES.forEachIndexed { index, size ->
            val image = AiGhostIcons.application[index]
            assertFalse(image.isError, "application icon of size $size could not be loaded")
            assertEquals(size.toDouble(), image.width, "application icon has the wrong width")
            assertEquals(size.toDouble(), image.height, "application icon has the wrong height")
        }
    }

    /**
     * Use case: an icon put into a menu is scaled to the menu icon size while keeping its ratio.
     */
    @Test
    fun menuGraphicScalesToMenuIconSize() {
        val view = AiGhostIcons.menuSave()

        assertEquals(AiGhostIcons.MENU_ICON_SIZE, view.fitWidth)
        assertEquals(AiGhostIcons.MENU_ICON_SIZE, view.fitHeight)
        assertTrue(view.isPreserveRatio)
    }

    /**
     * Use case: the FXML files attach their menu graphics through `fx:factory`, so every factory
     * method must deliver a fresh image view scaled to the menu icon size.
     */
    @ParameterizedTest(name = "menu graphic {0}")
    @MethodSource("menuGraphics")
    fun menuGraphicIsScaledImageView(name: String, view: ImageView) {
        assertFalse(view.image.isError, "graphic $name could not be loaded")
        assertEquals(AiGhostIcons.MENU_ICON_SIZE, view.fitWidth, "graphic $name has the wrong width")
        assertEquals(AiGhostIcons.MENU_ICON_SIZE, view.fitHeight, "graphic $name has the wrong height")
        assertTrue(view.isPreserveRatio, "graphic $name does not keep its ratio")
    }

    /**
     * Use case: every call of a menu graphic factory returns its own image view, so the same icon
     * can be shown by several menu entries at once.
     */
    @Test
    fun menuGraphicFactoryReturnsANewInstancePerCall() {
        val first = AiGhostIcons.menuSave()
        val second = AiGhostIcons.menuSave()

        assertNotSame(first, second)
        assertSame(first.image, second.image)
    }
}

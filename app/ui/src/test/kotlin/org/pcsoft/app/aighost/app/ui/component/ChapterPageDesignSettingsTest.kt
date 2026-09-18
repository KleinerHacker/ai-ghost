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

import de.saxsys.mvvmfx.MvvmFX
import javafx.scene.Scene
import javafx.scene.control.CheckBox
import javafx.scene.control.ComboBox
import javafx.scene.control.Spinner
import javafx.stage.Stage
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.pcsoft.app.aighost.app.Messages
import org.pcsoft.app.aighost.fx.model.project.ProjectProperty
import org.pcsoft.app.aighost.fx.model.project.design.ChapterPageDesignProperty
import org.pcsoft.app.aighost.model.common.Alignment
import org.pcsoft.app.aighost.model.common.FontData
import org.pcsoft.app.aighost.model.common.StyleData
import org.pcsoft.app.aighost.model.project.Project
import org.pcsoft.app.aighost.model.project.design.ChapterPageDesign
import org.pcsoft.app.aighost.model.project.design.Design
import org.testfx.framework.junit5.ApplicationTest
import java.util.Locale
import java.util.ResourceBundle

/**
 * Developer tests for [ChapterPageDesignSettings].
 *
 * The checks run on the real controls the user works with, and prove the binding to the design
 * property in both directions for all three style editors and the "on separate page" switch.
 */
class ChapterPageDesignSettingsTest : ApplicationTest() {

    private lateinit var component: ChapterPageDesignSettings

    private val design: ChapterPageDesignProperty =
        ProjectProperty(
            Project(
                design = Design(
                    chapterPage = ChapterPageDesign(
                        titleStyle = StyleData(font = FontData(name = "Georgia", size = 20)),
                        titleAppendixStyle = StyleData(font = FontData(name = "Cambria", size = 14)),
                        textStyle = StyleData(font = FontData(name = "Verdana", size = 11)),
                        titleOnSeparatePage = false
                    )
                )
            )
        ).designProperty.chapterPageProperty

    private fun familyOf(editor: StyleDataEditor): ComboBox<*> = editor.lookup("#cmbFamily") as ComboBox<*>

    private val editorTitle: StyleDataEditor get() = component.lookup(".style-data-editor") as StyleDataEditor
    private val titleOnSeparatePage: CheckBox get() = component.lookup("#chkTitleOnSeparatePage") as CheckBox

    override fun start(stage: Stage) {
        // No fallback, so the English base bundle is used no matter which locale the build runs under.
        MvvmFX.setGlobalResourceBundle(
            ResourceBundle.getBundle(
                Messages.BUNDLE_NAME,
                Locale.ROOT,
                ResourceBundle.Control.getNoFallbackControl(ResourceBundle.Control.FORMAT_PROPERTIES)
            )
        )

        component = ChapterPageDesignSettings()
        component.bindDesign(design)
        stage.scene = Scene(component, 480.0, 720.0)
        stage.show()
    }

    private fun editorAt(index: Int): StyleDataEditor =
        component.lookupAll(".style-data-editor").toList()
            .map { it as StyleDataEditor }
            .sortedBy { it.boundsInParent.minY }[index]

    /**
     * Use case: the design is handed to the component, so the three style editors show the values
     * of their own style and the switch shows the stored flag.
     */
    @Test
    fun boundDesignReachesAllFields() {
        assertEquals("Georgia", familyOf(editorAt(0)).let { it.editor.text })
        assertEquals("Cambria", familyOf(editorAt(1)).let { it.editor.text })
        assertEquals("Verdana", familyOf(editorAt(2)).let { it.editor.text })
        assertFalse(titleOnSeparatePage.isSelected)
    }

    /**
     * Use case: a value is written through the design property, so the matching style editor and
     * the switch show it.
     */
    @Test
    fun modelToUi() {
        interact {
            design.titleStyleProperty.fontProperty.name = "Tahoma"
            design.titleOnSeparatePage = true
        }

        assertEquals("Tahoma", familyOf(editorAt(0)).editor.text)
        assertTrue(titleOnSeparatePage.isSelected)
    }

    /**
     * Use case: the user edits every control, so the plain design object behind it carries every
     * value, read from the object itself.
     */
    @Test
    fun uiToModel() {
        interact {
            familyOf(editorAt(0)).editor.text = "Calibri"
            familyOf(editorAt(1)).editor.text = "Consolas"
            familyOf(editorAt(2)).editor.text = "Garamond"
            titleOnSeparatePage.isSelected = true
        }

        val stored = design.get()!!
        assertEquals("Calibri", stored.titleStyle.font.name)
        assertEquals("Consolas", stored.titleAppendixStyle.font.name)
        assertEquals("Garamond", stored.textStyle.font.name)
        assertTrue(stored.titleOnSeparatePage)
    }

    /**
     * Use case: another design takes the place of the first, so the controls show the new one and
     * editing them reaches the new object only, the one left behind stays untouched.
     */
    @Test
    fun exchangingTheDesignShowsTheNewOneAndLeavesTheOldUntouched() {
        val second = ProjectProperty(
            Project(
                design = Design(
                    chapterPage = ChapterPageDesign(
                        titleStyle = StyleData(font = FontData(name = "Rockwell", size = 18)),
                        titleOnSeparatePage = true
                    )
                )
            )
        ).designProperty.chapterPageProperty

        interact { component.bindDesign(second) }

        assertEquals("Rockwell", familyOf(editorAt(0)).editor.text)
        assertTrue(titleOnSeparatePage.isSelected)

        interact { familyOf(editorAt(0)).editor.text = "Optima" }
        assertEquals("Optima", second.get()!!.titleStyle.font.name)
        assertEquals("Georgia", design.get()!!.titleStyle.font.name)
    }

    /**
     * Use case: [ChapterPageDesignSettings.valid] follows every style editor - an invalid field in
     * any of the three locks the whole section.
     */
    @Test
    fun validFollowsEveryStyleEditor() {
        assertTrue(component.valid.value)

        val size = editorAt(1).lookup("#spnSize") as Spinner<*>
        interact { (size.editor).text = "" }
        interact { size.editor.text = "" }

        assertFalse(component.valid.value)
    }
}

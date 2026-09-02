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
import javafx.stage.Stage
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.pcsoft.app.aighost.app.Messages
import org.pcsoft.app.aighost.fx.model.project.ProjectProperty
import org.pcsoft.app.aighost.fx.model.project.design.TitlePageDesignProperty
import org.pcsoft.app.aighost.model.common.FontData
import org.pcsoft.app.aighost.model.common.StyleData
import org.pcsoft.app.aighost.model.project.Project
import org.pcsoft.app.aighost.model.project.design.Design
import org.pcsoft.app.aighost.model.project.design.TitlePageDesign
import org.testfx.framework.junit5.ApplicationTest
import java.util.Locale
import java.util.ResourceBundle

/**
 * Developer tests for [TitlePageDesignSettings].
 *
 * The checks run on the real controls the user works with, and prove the binding to the title page
 * design property in both directions, for all four embedded styles and the "show author" switch.
 */
class TitlePageDesignSettingsTest : ApplicationTest() {

    private lateinit var component: TitlePageDesignSettings

    private val design: TitlePageDesignProperty =
        ProjectProperty(
            Project(
                design = Design(
                    titlePage = TitlePageDesign(
                        titleStyle = StyleData(font = FontData(name = "Georgia", size = 14)),
                        titleAppendixStyle = StyleData(font = FontData(name = "Cambria", size = 12)),
                        showAuthor = false,
                        authorStyle = StyleData(font = FontData(name = "Tahoma", size = 11))
                    )
                )
            )
        ).designProperty.titlePageProperty

    private fun family(editor: StyleDataEditor): ComboBox<*> = editor.lookup("#cmbFamily") as ComboBox<*>
    private val showAuthor: CheckBox get() = component.lookup("#chkShowAuthor") as CheckBox
    private val titleEditor: StyleDataEditor get() = component.lookup("#titleEditor") as StyleDataEditor
    private val titleAppendixEditor: StyleDataEditor get() = component.lookup("#titleAppendixEditor") as StyleDataEditor
    private val authorEditor: StyleDataEditor get() = component.lookup("#authorEditor") as StyleDataEditor

    override fun start(stage: Stage) {
        // No fallback, so the English base bundle is used no matter which locale the build runs under.
        MvvmFX.setGlobalResourceBundle(
            ResourceBundle.getBundle(
                Messages.BUNDLE_NAME,
                Locale.ROOT,
                ResourceBundle.Control.getNoFallbackControl(ResourceBundle.Control.FORMAT_PROPERTIES)
            )
        )

        component = TitlePageDesignSettings()
        component.bindDesign(design)
        stage.scene = Scene(component, 480.0, 700.0)
        stage.show()
    }

    /**
     * Use case: the design is handed to the component, so the three embedded editors and the switch
     * show the values it carries.
     */
    @Test
    fun boundDesignReachesTheFields() {
        assertEquals("Georgia", family(titleEditor).editor.text)
        assertEquals("Cambria", family(titleAppendixEditor).editor.text)
        assertFalse(showAuthor.isSelected)
        assertEquals("Tahoma", family(authorEditor).editor.text)
    }

    /**
     * Use case: a value is written through the design property, so the matching embedded editor
     * shows it.
     */
    @Test
    fun modelToUi() {
        interact { design.titleStyleProperty.fontProperty.name = "Verdana" }

        assertEquals("Verdana", family(titleEditor).editor.text)
    }

    /**
     * Use case: the user edits the family field of an embedded editor, so the plain design object
     * behind it carries the value, read from the object itself.
     */
    @Test
    fun uiToModel() {
        interact { family(titleAppendixEditor).editor.text = "Calibri" }

        assertEquals("Calibri", design.get()!!.titleAppendixStyle.font.name)
    }

    /**
     * Use case: the "show author" check box is ticked, so the plain design object carries the change,
     * and the other way round a value set through the model reaches the box.
     */
    @Test
    fun showAuthorCheckBoxBindsBothWays() {
        interact { showAuthor.isSelected = true }
        assertTrue(design.get()!!.showAuthor)

        interact { design.showAuthor = false }
        assertFalse(showAuthor.isSelected)
    }

    /**
     * Use case: the "show author" check box is off, so the author style editor is disabled but still
     * reachable and still writes to the model when edited past the disabled state.
     */
    @Test
    fun authorEditorDisablesWithTheSwitchButStaysWritable() {
        interact { showAuthor.isSelected = false }
        assertTrue(authorEditor.isDisabled)

        interact { showAuthor.isSelected = true }
        assertFalse(authorEditor.isDisabled)

        interact { family(authorEditor).editor.text = "Georgia Pro" }
        assertEquals("Georgia Pro", design.get()!!.authorStyle.font.name)

        interact { showAuthor.isSelected = false }
        assertTrue(authorEditor.isDisabled)
        assertEquals(
            "Georgia Pro", design.get()!!.authorStyle.font.name,
            "disabling the switch must not drop the author style already entered"
        )
    }
}

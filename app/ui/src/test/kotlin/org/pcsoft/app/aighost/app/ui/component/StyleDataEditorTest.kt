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
import javafx.scene.control.ComboBox
import javafx.scene.control.Label
import javafx.scene.control.Spinner
import javafx.scene.control.ToggleButton
import javafx.stage.Stage
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.pcsoft.app.aighost.app.Messages
import org.pcsoft.app.aighost.fx.model.common.StyleDataProperty
import org.pcsoft.app.aighost.fx.model.project.ProjectProperty
import org.pcsoft.app.aighost.model.common.Alignment
import org.pcsoft.app.aighost.model.common.FontData
import org.pcsoft.app.aighost.model.common.StyleData
import org.pcsoft.app.aighost.model.project.Project
import org.pcsoft.app.aighost.model.project.design.Design
import org.pcsoft.app.aighost.model.project.design.TitlePageDesign
import org.testfx.framework.junit5.ApplicationTest
import java.util.Locale
import java.util.ResourceBundle

/**
 * Developer tests for [StyleDataEditor].
 *
 * The checks run on the real controls the user works with, and prove the binding to the style
 * property in both directions.
 */
class StyleDataEditorTest : ApplicationTest() {

    private companion object {
        const val DELTA: Double = 1e-9
    }

    private lateinit var component: StyleDataEditor

    private val style: StyleDataProperty =
        ProjectProperty(
            Project(
                design = Design(
                    titlePage = TitlePageDesign(
                        titleStyle = StyleData(
                            font = FontData(name = "Georgia", size = 14, bold = false, italic = false),
                            textLineSpacing = 1.2,
                            alignment = Alignment.LEFT
                        )
                    )
                )
            )
        ).designProperty.titlePageProperty.titleStyleProperty

    @Suppress("UNCHECKED_CAST")
    private val family: ComboBox<String> get() = component.lookup("#cmbFamily") as ComboBox<String>
    private val familyWarning: Label get() = component.lookup("#lblFamilyWarning") as Label
    private val size: Spinner<*> get() = component.lookup("#spnSize") as Spinner<*>
    private val bold: ToggleButton get() = component.lookup("#tglBold") as ToggleButton
    private val italic: ToggleButton get() = component.lookup("#tglItalic") as ToggleButton

    @Suppress("UNCHECKED_CAST")
    private val alignment: ComboBox<Alignment> get() = component.lookup("#cmbAlignment") as ComboBox<Alignment>
    private val lineSpacing: Spinner<*> get() = component.lookup("#spnLineSpacing") as Spinner<*>

    override fun start(stage: Stage) {
        // No fallback, so the English base bundle is used no matter which locale the build runs under.
        MvvmFX.setGlobalResourceBundle(
            ResourceBundle.getBundle(
                Messages.BUNDLE_NAME,
                Locale.ROOT,
                ResourceBundle.Control.getNoFallbackControl(ResourceBundle.Control.FORMAT_PROPERTIES)
            )
        )

        component = StyleDataEditor()
        component.bindStyle(style)
        stage.scene = Scene(component, 420.0, 420.0)
        stage.show()
    }

    /**
     * Use case: the style is handed to the component, so every field shows the value it carries.
     */
    @Test
    fun boundStyleReachesTheFields() {
        assertEquals("Georgia", family.editor.text)
        assertEquals(14, size.value)
        assertFalse(bold.isSelected)
        assertFalse(italic.isSelected)
        assertEquals(Alignment.LEFT, alignment.value)
        assertEquals(1.2, lineSpacing.value as Double, DELTA)
    }

    /**
     * Use case: a value is written through the style property, so the matching controls show it.
     */
    @Test
    fun modelToUi() {
        interact {
            style.fontProperty.name = "Cambria"
            style.fontProperty.size = 22
            style.fontProperty.bold = true
            style.alignment = Alignment.RIGHT
            style.textLineSpacing = 1.6
        }

        assertEquals("Cambria", family.editor.text)
        assertEquals(22, size.value)
        assertTrue(bold.isSelected)
        assertEquals(Alignment.RIGHT, alignment.value)
        assertEquals(1.6, lineSpacing.value as Double, DELTA)
    }

    /**
     * Use case: the user edits every control, so the plain style object behind it carries every
     * value, read from the object itself.
     */
    @Test
    fun uiToModel() {
        interact {
            family.editor.text = "Verdana"
            (size as Spinner<Int>).increment(1)
            bold.isSelected = true
            italic.isSelected = true
            alignment.selectionModel.select(Alignment.BLOCK)
            (lineSpacing as Spinner<Double>).increment(1)
        }

        val stored = style.get()!!
        assertEquals("Verdana", stored.font.name)
        assertEquals(15, stored.font.size)
        assertTrue(stored.font.bold)
        assertTrue(stored.font.italic)
        assertEquals(Alignment.BLOCK, stored.alignment)
        assertEquals(1.25, stored.textLineSpacing, DELTA)
    }

    /**
     * Use case: a family that is not installed on this machine is bound, so the warning label is
     * shown next to the family field; an installed family hides it again.
     */
    @Test
    fun notInstalledFamilyShowsTheWarning() {
        assertFalse(familyWarning.isVisible, "an installed family must not show the warning")

        interact { family.editor.text = "A Family That Definitely Does Not Exist On This Machine" }
        assertTrue(familyWarning.isVisible, "a missing family must show the warning")

        interact { family.editor.text = "Georgia" }
        assertFalse(familyWarning.isVisible, "an installed family must hide the warning again")
    }

    /**
     * Use case: another style takes the place of the first, so the controls show the new one and
     * editing them reaches the new object only, the one left behind stays untouched.
     */
    @Test
    fun exchangingTheStyleShowsTheNewOneAndLeavesTheOldUntouched() {
        val second = ProjectProperty(
            Project(
                design = Design(
                    titlePage = TitlePageDesign(
                        titleStyle = StyleData(font = FontData(name = "Tahoma", size = 11))
                    )
                )
            )
        ).designProperty.titlePageProperty.titleStyleProperty

        interact { component.bindStyle(second) }

        assertEquals("Tahoma", family.editor.text)

        interact { family.editor.text = "Calibri" }
        assertEquals("Calibri", second.get()!!.font.name)
        assertEquals("Georgia", style.get()!!.font.name)
    }
}

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
import javafx.scene.control.TextField
import javafx.stage.Stage
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.pcsoft.app.aighost.app.Messages
import org.pcsoft.app.aighost.fx.model.project.ProjectProperty
import org.pcsoft.app.aighost.fx.model.project.design.DesignProperty
import org.pcsoft.app.aighost.model.project.Project
import org.pcsoft.app.aighost.model.project.design.Design
import org.pcsoft.app.aighost.model.project.design.PageFormat
import org.testfx.framework.junit5.ApplicationTest
import java.util.Locale
import java.util.ResourceBundle

/**
 * Developer tests for [GeneralSettings].
 *
 * The checks run on the real controls the user works with, and prove the binding to the design
 * property in both directions.
 */
class GeneralSettingsTest : ApplicationTest() {

    private companion object {
        const val PT_PER_MM: Double = 72.0 / 25.4
        const val DELTA: Double = 1e-6
    }

    private lateinit var component: GeneralSettings

    private val design: DesignProperty =
        ProjectProperty(Project(design = Design(pageFormat = a5()))).designProperty

    private fun mmToPt(mm: Double): Double = mm * PT_PER_MM

    private fun a5(): PageFormat = PageFormat(
        width = 148.0 * (72.0 / 25.4), height = 210.0 * (72.0 / 25.4),
        innerMargin = 20.0 * (72.0 / 25.4), outerMargin = 15.0 * (72.0 / 25.4),
        topMargin = 15.0 * (72.0 / 25.4), bottomMargin = 18.0 * (72.0 / 25.4)
    )

    private val width: TextField get() = component.lookup("#txtWidth") as TextField
    private val height: TextField get() = component.lookup("#txtHeight") as TextField
    private val inner: TextField get() = component.lookup("#txtInner") as TextField

    @Suppress("UNCHECKED_CAST")
    private val preset: ComboBox<PagePreset>
        get() = component.lookup("#cmbPreset") as ComboBox<PagePreset>
    private val startEmptyPage: CheckBox get() = component.lookup("#chkStart") as CheckBox

    override fun start(stage: Stage) {
        // No fallback, so the English base bundle is used no matter which locale the build runs under.
        MvvmFX.setGlobalResourceBundle(
            ResourceBundle.getBundle(
                Messages.BUNDLE_NAME,
                Locale.ROOT,
                ResourceBundle.Control.getNoFallbackControl(ResourceBundle.Control.FORMAT_PROPERTIES)
            )
        )

        component = GeneralSettings()
        component.bindDesign(design)
        stage.scene = Scene(component, 420.0, 460.0)
        stage.show()
    }

    /**
     * Use case: the design is handed to the component, so the fields show its point values converted
     * to millimetres.
     */
    @Test
    fun boundDesignReachesTheFields() {
        assertEquals("148", width.text)
        assertEquals("210", height.text)
        assertEquals("20", inner.text)
    }

    /**
     * Use case: a value is written through the design property, so the matching field shows it in
     * millimetres.
     */
    @Test
    fun modelToUi() {
        interact { design.pageFormatProperty.width = mmToPt(150.0) }

        assertEquals("150", width.text)
    }

    /**
     * Use case: the user types a millimetre value into a field, so the design behind it carries the
     * value in points, read from the plain model object.
     */
    @Test
    fun uiToModel() {
        interact { inner.text = "22" }

        assertEquals(mmToPt(22.0), design.get()!!.pageFormat.innerMargin, DELTA)
    }

    /**
     * Use case: the user picks a page size preset, so the page is sized to it, the design follows and
     * the width and height fields are locked.
     */
    @Test
    fun choosingAPresetSizesAndLocksTheFields() {
        interact { preset.selectionModel.select(PagePreset.A4) }

        assertEquals("210", width.text)
        assertEquals("297", height.text)
        assertEquals(mmToPt(210.0), design.get()!!.pageFormat.width, DELTA)
        assertTrue(width.isDisabled, "the width field stays open under a preset")
        assertTrue(height.isDisabled, "the height field stays open under a preset")
    }

    /**
     * Use case: the empty page check box binds both ways - a value set through the model reaches the
     * box, and ticking the box reaches the plain design object.
     */
    @Test
    fun emptyPageCheckBoxBindsBothWays() {
        interact { design.startWithEmptyPage = false }
        assertFalse(startEmptyPage.isSelected)

        interact { startEmptyPage.isSelected = true }
        assertTrue(design.get()!!.startWithEmptyPage)
    }

    /**
     * Use case: the user types margins that do not fit the page, so the component reports that its
     * input cannot be stored and marks the field.
     */
    @Test
    fun impossibleMarginsAreRejected() {
        assertTrue(component.valid.value)

        interact { inner.text = "200" }

        assertFalse(component.valid.value)
        assertTrue(inner.styleClass.contains("field-error"), "the field in error is not marked")
    }
}

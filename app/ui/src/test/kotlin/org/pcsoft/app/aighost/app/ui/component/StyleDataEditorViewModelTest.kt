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

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.pcsoft.app.aighost.fx.model.common.StyleDataProperty
import org.pcsoft.app.aighost.fx.model.project.ProjectProperty
import org.pcsoft.app.aighost.model.common.Alignment
import org.pcsoft.app.aighost.model.common.FontData
import org.pcsoft.app.aighost.model.common.StyleData
import org.pcsoft.app.aighost.model.project.Project
import org.pcsoft.app.aighost.model.project.design.Design
import org.pcsoft.app.aighost.model.project.design.TitlePageDesign

/**
 * Developer tests for [StyleDataEditorViewModel].
 *
 * Every field of the model is typed exactly as the underlying [StyleDataProperty] carries it, so
 * every check writes and reads the plain typed value, without a millimetre-style conversion.
 */
class StyleDataEditorViewModelTest {

    private val viewModel = StyleDataEditorViewModel()

    private fun styleOf(style: StyleData): StyleDataProperty {
        val design = ProjectProperty(
            Project(design = Design(titlePage = TitlePageDesign(titleStyle = style)))
        ).designProperty
        return design.titlePageProperty.titleStyleProperty
    }

    private fun titleStyle(): StyleData =
        FontData(name = "Georgia", size = 14, bold = false, italic = false)
            .let { StyleData(font = it, textLineSpacing = 1.2, alignment = Alignment.LEFT) }

    /**
     * Use case: a style is handed to the view model, so every field shows the value the style
     * carries.
     */
    @Test
    fun boundStyleReachesEveryField() {
        viewModel.bind(styleOf(titleStyle()))

        assertEquals("Georgia", viewModel.familyName.value)
        assertEquals(14, viewModel.size.value)
        assertFalse(viewModel.bold.value)
        assertFalse(viewModel.italic.value)
        assertEquals(Alignment.LEFT, viewModel.alignment.value)
        assertEquals(1.2, viewModel.lineSpacing.value, 1e-9)
    }

    /**
     * Use case: the user writes a value into a field, so the plain style object behind it carries
     * the same value, read from the object itself.
     */
    @Test
    fun writtenValuesReachThePlainStyle() {
        val style = styleOf(titleStyle())
        viewModel.bind(style)

        viewModel.familyName.value = "Cambria"
        viewModel.size.value = 18
        viewModel.bold.value = true
        viewModel.italic.value = true
        viewModel.alignment.value = Alignment.CENTER
        viewModel.lineSpacing.value = 1.5

        val stored = style.get()!!
        assertEquals("Cambria", stored.font.name)
        assertEquals(18, stored.font.size)
        assertTrue(stored.font.bold)
        assertTrue(stored.font.italic)
        assertEquals(Alignment.CENTER, stored.alignment)
        assertEquals(1.5, stored.textLineSpacing, 1e-9)
    }

    /**
     * Use case: a value is written on the plain style past the property model, so a
     * [StyleDataProperty.refresh] carries it into the form.
     */
    @Test
    fun valueWrittenPastTheModelReachesTheFieldAfterRefresh() {
        val style = styleOf(titleStyle())
        viewModel.bind(style)

        style.get()!!.font.size = 21
        style.refresh()

        assertEquals(21, viewModel.size.value)
    }

    /**
     * Use case: another style takes the place of the first, so the form shows the new one and
     * writing into a field reaches the new object only, the one left behind stays untouched.
     */
    @Test
    fun exchangingTheStyleShowsTheNewOneAndLeavesTheOldUntouched() {
        val first = styleOf(titleStyle())
        viewModel.bind(first)
        viewModel.familyName.value = "Cambria"

        val second = styleOf(StyleData(font = FontData(name = "Verdana", size = 11)))
        viewModel.bind(second)

        assertEquals("Verdana", viewModel.familyName.value)

        viewModel.familyName.value = "Tahoma"
        assertEquals("Tahoma", second.get()!!.font.name)
        assertEquals("Cambria", first.get()!!.font.name)
    }

    /**
     * Use case: the size, the line spacing and the family are all set to a value that can be stored,
     * so the form reports itself valid; clearing any one of them reports it invalid.
     */
    @Test
    fun validReflectsEveryRequiredField() {
        viewModel.bind(styleOf(titleStyle()))
        assertTrue(viewModel.valid.value)

        viewModel.size.value = 0
        assertFalse(viewModel.valid.value)
        viewModel.size.value = 14

        viewModel.lineSpacing.value = 0.0
        assertFalse(viewModel.valid.value)
        viewModel.lineSpacing.value = 1.2

        viewModel.familyName.value = ""
        assertFalse(viewModel.valid.value)
    }

    /**
     * Use case: the bound family is not among the installed families, so the warning is reported;
     * an installed family or a blank one reports no warning.
     */
    @Test
    fun familyNotInstalledFollowsTheFamilyName() {
        viewModel.bind(styleOf(titleStyle()))

        viewModel.familyName.value = "A Family That Definitely Does Not Exist On This Machine"
        assertTrue(viewModel.familyNotInstalled.value)

        viewModel.familyName.value = ""
        assertFalse(viewModel.familyNotInstalled.value)
    }
}

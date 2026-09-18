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
import org.pcsoft.app.aighost.fx.model.project.ProjectProperty
import org.pcsoft.app.aighost.fx.model.project.design.DesignProperty
import org.pcsoft.app.aighost.model.project.Project
import org.pcsoft.app.aighost.model.project.design.Design
import org.pcsoft.app.aighost.model.project.design.PageFormat

/**
 * Developer tests for [DesignSettingsViewModel].
 *
 * The model stores points and the form shows millimetres, so every check converts one into the other
 * with the same factor the view model uses.
 */
class DesignSettingsViewModelTest {

    private companion object {
        const val PT_PER_MM: Double = 72.0 / 25.4
        const val DELTA: Double = 1e-6
    }

    private val viewModel = DesignSettingsViewModel()

    private fun mmToPt(mm: Double): Double = mm * PT_PER_MM

    private fun designOf(pageFormat: PageFormat): DesignProperty =
        ProjectProperty(Project(design = Design(pageFormat = pageFormat))).designProperty

    private fun a5(): PageFormat = PageFormat(
        width = mmToPt(148.0), height = mmToPt(210.0),
        innerMargin = mmToPt(20.0), outerMargin = mmToPt(15.0),
        topMargin = mmToPt(15.0), bottomMargin = mmToPt(18.0)
    )

    /**
     * Use case: a design is handed to the view model, so every field shows the stored point value
     * converted to millimetres.
     */
    @Test
    fun boundDesignIsShownInMillimetres() {
        viewModel.bind(designOf(a5()))

        assertEquals("148", viewModel.widthMm.value)
        assertEquals("210", viewModel.heightMm.value)
        assertEquals("20", viewModel.innerMm.value)
        assertEquals("15", viewModel.outerMm.value)
        assertEquals("15", viewModel.topMm.value)
        assertEquals("18", viewModel.bottomMm.value)
    }

    /**
     * Use case: the user types a millimetre value, so the design behind the form carries it in
     * points, read from the plain model object.
     */
    @Test
    fun writtenMillimetresReachTheDesignInPoints() {
        val design = designOf(a5())
        viewModel.bind(design)

        viewModel.widthMm.value = "150"
        viewModel.innerMm.value = "22.5"

        val pageFormat = design.get()!!.pageFormat
        assertEquals(mmToPt(150.0), pageFormat.width, DELTA)
        assertEquals(mmToPt(22.5), pageFormat.innerMargin, DELTA)
    }

    /**
     * Use case: a value is written on the page format past the property model, so a [DesignProperty.refresh]
     * carries it into the form.
     */
    @Test
    fun valueWrittenPastTheModelReachesTheFieldAfterRefresh() {
        val design = designOf(a5())
        viewModel.bind(design)

        design.get()!!.pageFormat.height = mmToPt(123.0)
        design.refresh()

        assertEquals("123", viewModel.heightMm.value)
    }

    /**
     * Use case: another design takes the place of the first, so the form shows the new one and
     * writing into a field reaches the new object only, the one left behind stays untouched.
     */
    @Test
    fun exchangingTheDesignShowsTheNewOneAndLeavesTheOldUntouched() {
        val first = designOf(a5())
        viewModel.bind(first)
        viewModel.widthMm.value = "150"

        val second = designOf(PageFormat(width = mmToPt(210.0), height = mmToPt(297.0)))
        viewModel.bind(second)

        assertEquals("210", viewModel.widthMm.value)

        viewModel.widthMm.value = "160"
        assertEquals(mmToPt(160.0), second.get()!!.pageFormat.width, DELTA)
        assertEquals(mmToPt(150.0), first.get()!!.pageFormat.width, DELTA)
    }

    /**
     * Use case: the stored size is exactly a preset, so that preset is selected; a size typed by
     * hand that matches nothing switches the selection to custom.
     */
    @Test
    fun presetFollowsTheDimensions() {
        viewModel.bind(designOf(a5()))
        assertEquals(PagePreset.A5, viewModel.preset.value)

        viewModel.widthMm.value = "300"
        assertEquals(PagePreset.CUSTOM, viewModel.preset.value)
    }

    /**
     * Use case: the user picks a preset, so the page is sized to it in both the form and the design.
     */
    @Test
    fun choosingAPresetSizesThePage() {
        val design = designOf(a5())
        viewModel.bind(design)

        viewModel.preset.value = PagePreset.A4

        assertEquals("210", viewModel.widthMm.value)
        assertEquals("297", viewModel.heightMm.value)
        assertEquals(mmToPt(210.0), design.get()!!.pageFormat.width, DELTA)
        assertEquals(mmToPt(297.0), design.get()!!.pageFormat.height, DELTA)
    }

    /**
     * Use case: the two empty page flags bind both ways - a value set through the model reaches the
     * form, and a value set on the form reaches the plain design object.
     */
    @Test
    fun emptyPageFlagsBindBothWays() {
        val design = designOf(a5())
        viewModel.bind(design)

        design.startWithEmptyPage = false
        assertFalse(viewModel.startWithEmptyPage.value)

        viewModel.endWithEmptyPage.value = false
        assertFalse(design.get()!!.endWithEmptyPage)
    }

    /**
     * Use case: a margin that is not a number of zero or more keeps the form from being stored.
     */
    @Test
    fun validIsFalseWhenAMarginIsNotANumber() {
        viewModel.bind(designOf(a5()))
        assertTrue(viewModel.valid.value)

        viewModel.innerMm.value = ""
        assertFalse(viewModel.valid.value)
    }

    /**
     * Use case: a field is left empty, so the form cannot be stored and the size group is marked as
     * being in error.
     */
    @Test
    fun validIsFalseWhenANumberIsMissing() {
        viewModel.bind(designOf(a5()))

        viewModel.widthMm.value = ""

        assertFalse(viewModel.valid.value)
        assertTrue(viewModel.sizeError.value)
    }
}

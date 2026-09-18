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
 * Developer tests for [ProjectSettingsDialogViewModel].
 */
class ProjectSettingsDialogViewModelTest {

    private companion object {
        const val DELTA: Double = 1e-6
    }

    private val viewModel = ProjectSettingsDialogViewModel()

    private fun targetOf(design: Design): DesignProperty =
        ProjectProperty(Project(design = design)).designProperty

    private fun design(): Design = Design(
        pageFormat = PageFormat(
            width = 100.0, height = 200.0,
            innerMargin = 10.0, outerMargin = 11.0, topMargin = 12.0, bottomMargin = 13.0
        ),
        startWithEmptyPage = true,
        endWithEmptyPage = true
    )

    /**
     * Use case: the dialog opens, so the working copy shows the same page geometry as the design of
     * the open project.
     */
    @Test
    fun workingCopyStartsFromTheTarget() {
        viewModel.bindTarget(targetOf(design()))

        val working = viewModel.workingDesign.pageFormatProperty
        assertEquals(100.0, working.width, DELTA)
        assertEquals(200.0, working.height, DELTA)
        assertEquals(13.0, working.bottomMargin, DELTA)
        assertTrue(viewModel.workingDesign.startWithEmptyPage)
    }

    /**
     * Use case: the user changes the working copy but cancels the dialog, so the design of the open
     * project is never touched.
     */
    @Test
    fun editingTheWorkingCopyLeavesTheTargetUntouched() {
        val target = targetOf(design())
        viewModel.bindTarget(target)

        viewModel.workingDesign.pageFormatProperty.width = 222.0
        viewModel.workingDesign.startWithEmptyPage = false

        assertEquals(100.0, target.get()!!.pageFormat.width, DELTA)
        assertTrue(target.get()!!.startWithEmptyPage)
    }

    /**
     * Use case: the user presses OK or APPLY, so every page format value and both empty page flags
     * of the working copy are written into the design of the open project.
     */
    @Test
    fun applyWritesEveryValueBack() {
        val target = targetOf(design())
        viewModel.bindTarget(target)

        with(viewModel.workingDesign.pageFormatProperty) {
            width = 222.0
            height = 333.0
            innerMargin = 21.0
            outerMargin = 22.0
            topMargin = 23.0
            bottomMargin = 24.0
        }
        viewModel.workingDesign.startWithEmptyPage = false
        viewModel.workingDesign.endWithEmptyPage = false

        viewModel.apply()

        val pageFormat = target.get()!!.pageFormat
        assertEquals(222.0, pageFormat.width, DELTA)
        assertEquals(333.0, pageFormat.height, DELTA)
        assertEquals(21.0, pageFormat.innerMargin, DELTA)
        assertEquals(22.0, pageFormat.outerMargin, DELTA)
        assertEquals(23.0, pageFormat.topMargin, DELTA)
        assertEquals(24.0, pageFormat.bottomMargin, DELTA)
        assertFalse(target.get()!!.startWithEmptyPage)
        assertFalse(target.get()!!.endWithEmptyPage)
    }
}

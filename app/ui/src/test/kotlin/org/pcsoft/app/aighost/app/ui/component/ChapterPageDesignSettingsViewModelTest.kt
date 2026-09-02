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

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.pcsoft.app.aighost.fx.model.project.ProjectProperty
import org.pcsoft.app.aighost.fx.model.project.design.ChapterPageDesignProperty
import org.pcsoft.app.aighost.model.project.Project
import org.pcsoft.app.aighost.model.project.design.ChapterPageDesign
import org.pcsoft.app.aighost.model.project.design.Design

/**
 * Developer tests for [ChapterPageDesignSettingsViewModel].
 *
 * The model carries only [ChapterPageDesignSettingsViewModel.titleOnSeparatePage]; the three styles
 * are followed by the [StyleDataEditor] instances directly and are therefore proven on the real
 * controls in [ChapterPageDesignSettingsTest].
 */
class ChapterPageDesignSettingsViewModelTest {

    private val viewModel = ChapterPageDesignSettingsViewModel()

    private fun designOf(chapterPage: ChapterPageDesign): ChapterPageDesignProperty =
        ProjectProperty(Project(design = Design(chapterPage = chapterPage))).designProperty.chapterPageProperty

    /**
     * Use case: a design is handed to the view model, so the switch shows its stored value.
     */
    @Test
    fun boundDesignIsShownInTheField() {
        viewModel.bind(designOf(ChapterPageDesign(titleOnSeparatePage = true)))

        assertTrue(viewModel.titleOnSeparatePage.value)
    }

    /**
     * Use case: the user flips the switch, so the design behind the form carries the value, read
     * from the plain model object.
     */
    @Test
    fun writtenValueReachesTheDesign() {
        val design = designOf(ChapterPageDesign(titleOnSeparatePage = false))
        viewModel.bind(design)

        viewModel.titleOnSeparatePage.value = true

        assertTrue(design.get()!!.titleOnSeparatePage)
    }

    /**
     * Use case: a value is written on the design past the property model, so a
     * [ChapterPageDesignProperty.refresh] carries it into the field.
     */
    @Test
    fun valueWrittenPastTheModelReachesTheFieldAfterRefresh() {
        val design = designOf(ChapterPageDesign(titleOnSeparatePage = false))
        viewModel.bind(design)

        design.get()!!.titleOnSeparatePage = true
        design.refresh()

        assertTrue(viewModel.titleOnSeparatePage.value)
    }

    /**
     * Use case: another design takes the place of the first, so the form shows the new one and
     * writing into the field reaches the new object only, the one left behind stays untouched.
     */
    @Test
    fun exchangingTheDesignShowsTheNewOneAndLeavesTheOldUntouched() {
        val first = designOf(ChapterPageDesign(titleOnSeparatePage = true))
        viewModel.bind(first)

        val second = designOf(ChapterPageDesign(titleOnSeparatePage = false))
        viewModel.bind(second)

        assertFalse(viewModel.titleOnSeparatePage.value)

        viewModel.titleOnSeparatePage.value = true
        assertTrue(second.get()!!.titleOnSeparatePage)
        assertTrue(first.get()!!.titleOnSeparatePage)
    }
}

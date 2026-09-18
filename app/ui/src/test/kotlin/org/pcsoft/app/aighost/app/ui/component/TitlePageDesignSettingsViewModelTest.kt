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
import org.pcsoft.app.aighost.fx.model.project.design.TitlePageDesignProperty
import org.pcsoft.app.aighost.model.project.Project
import org.pcsoft.app.aighost.model.project.design.Design
import org.pcsoft.app.aighost.model.project.design.TitlePageDesign

/**
 * Developer tests for [TitlePageDesignSettingsViewModel].
 *
 * The three embedded style editors of [TitlePageDesignSettings] are wired up by
 * [TitlePageDesignSettingsView] directly through the public API of [StyleDataEditor]; this view model
 * only owns [TitlePageDesignSettingsViewModel.showAuthor], so these checks cover that switch and its
 * binding lifecycle. The disable behaviour of the author style editor and the combined [valid]
 * expression are proven on the real controls in [TitlePageDesignSettingsTest] instead.
 */
class TitlePageDesignSettingsViewModelTest {

    private val viewModel = TitlePageDesignSettingsViewModel()

    private fun designOf(design: TitlePageDesign): TitlePageDesignProperty =
        ProjectProperty(Project(design = Design(titlePage = design))).designProperty.titlePageProperty

    /**
     * Use case: a title page design is handed to the view model, so [TitlePageDesignSettingsViewModel.showAuthor]
     * shows the value the design carries.
     */
    @Test
    fun boundDesignReachesTheSwitch() {
        viewModel.bind(designOf(TitlePageDesign(showAuthor = true)))

        assertTrue(viewModel.showAuthor.value)
    }

    /**
     * Use case: the "show author" switch is written through the model, so the property this view
     * model exposes follows it, and the other way round.
     */
    @Test
    fun showAuthorBindsBothWays() {
        val model = designOf(TitlePageDesign(showAuthor = false))
        viewModel.bind(model)
        assertFalse(viewModel.showAuthor.value)

        model.showAuthor = true
        assertTrue(viewModel.showAuthor.value)

        viewModel.showAuthor.value = false
        assertFalse(model.showAuthor)
    }

    /**
     * Use case: another design takes the place of the first, so the switch follows the new one and
     * writing into it reaches the new object only, the one left behind stays untouched.
     */
    @Test
    fun exchangingTheDesignShowsTheNewOneAndLeavesTheOldUntouched() {
        val first = designOf(TitlePageDesign(showAuthor = true))
        viewModel.bind(first)

        val second = designOf(TitlePageDesign(showAuthor = false))
        viewModel.bind(second)

        assertFalse(viewModel.showAuthor.value)

        viewModel.showAuthor.value = true
        assertTrue(second.showAuthor)
        assertTrue(first.showAuthor, "the design left behind must not be touched by the new binding")
    }
}

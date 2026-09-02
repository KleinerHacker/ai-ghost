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
import org.pcsoft.app.aighost.fx.model.project.design.CopyrightPageDesignProperty
import org.pcsoft.app.aighost.model.project.Project
import org.pcsoft.app.aighost.model.project.design.CopyrightPageDesign
import org.pcsoft.app.aighost.model.project.design.Design

/**
 * Developer tests for [CopyrightPageDesignSettingsViewModel].
 *
 * The three embedded style editors of [CopyrightPageDesignSettings] are wired up by
 * [CopyrightPageDesignSettingsView] directly through the public API of [StyleDataEditor]; this view
 * model only owns [CopyrightPageDesignSettingsViewModel.showAuthor], so these checks cover that
 * switch and its binding lifecycle. The disable behaviour of the author style editor and the
 * combined [valid] expression are proven on the real controls in [CopyrightPageDesignSettingsTest]
 * instead.
 */
class CopyrightPageDesignSettingsViewModelTest {

    private val viewModel = CopyrightPageDesignSettingsViewModel()

    private fun designOf(design: CopyrightPageDesign): CopyrightPageDesignProperty =
        ProjectProperty(Project(design = Design(copyrightPage = design))).designProperty.copyrightPageProperty

    /**
     * Use case: a copyright page design is handed to the view model, so
     * [CopyrightPageDesignSettingsViewModel.showAuthor] shows the value the design carries.
     */
    @Test
    fun boundDesignReachesTheSwitch() {
        viewModel.bind(designOf(CopyrightPageDesign(showAuthor = true)))

        assertTrue(viewModel.showAuthor.value)
    }

    /**
     * Use case: the "show author" switch is written through the model, so the property this view
     * model exposes follows it, and the other way round.
     */
    @Test
    fun showAuthorBindsBothWays() {
        val model = designOf(CopyrightPageDesign(showAuthor = false))
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
        val first = designOf(CopyrightPageDesign(showAuthor = true))
        viewModel.bind(first)

        val second = designOf(CopyrightPageDesign(showAuthor = false))
        viewModel.bind(second)

        assertFalse(viewModel.showAuthor.value)

        viewModel.showAuthor.value = true
        assertTrue(second.showAuthor)
        assertTrue(first.showAuthor, "the design left behind must not be touched by the new binding")
    }
}

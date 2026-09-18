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

import de.saxsys.mvvmfx.ViewModel
import org.pcsoft.app.aighost.fx.model.project.ProjectProperty
import org.pcsoft.app.aighost.fx.model.project.design.DesignProperty
import org.pcsoft.app.aighost.model.project.Project
import org.pcsoft.app.aighost.model.project.design.Design

/**
 * View model of [ProjectSettingsDialog].
 *
 * The dialog edits a working copy, not the design of the open project: [workingDesign] wraps a
 * detached [Project] whose design is a deep copy of the target. The editors below the dialog bind to
 * that copy and change it freely; only [apply] carries the values the dialog actually edits back into
 * the real design. Cancelling the dialog leaves the working copy behind untouched.
 */
class ProjectSettingsDialogViewModel : ViewModel {

    private val workingProject: ProjectProperty = ProjectProperty(Project())

    private var target: DesignProperty? = null

    /** The design of the working copy, the model every editor of the dialog binds to. */
    val workingDesign: DesignProperty get() = workingProject.designProperty

    /**
     * Fills the working copy from [design] and remembers it as the design to write back.
     *
     * @param design the design of the open project
     */
    fun bindTarget(design: DesignProperty) {
        target = design
        val source = design.get() ?: Design()
        workingProject.value = Project(
            design = source.copy(
                pageFormat = source.pageFormat.copy(),
                titlePage = source.titlePage.copy(),
                copyrightPage = source.copyrightPage.copy(),
                prologPage = source.prologPage.copy(),
                chapterPage = source.chapterPage.copy(),
                epilogPage = source.epilogPage.copy(),
                blurbPage = source.blurbPage.copy()
            )
        )
    }

    /**
     * Writes the page geometry, the two empty page flags and every page design of the working copy
     * back into the design of the open project.
     */
    fun apply() {
        val target = this.target ?: return
        val working = workingDesign.pageFormatProperty
        with(target.pageFormatProperty) {
            width = working.width
            height = working.height
            innerMargin = working.innerMargin
            outerMargin = working.outerMargin
            topMargin = working.topMargin
            bottomMargin = working.bottomMargin
        }
        target.startWithEmptyPage = workingDesign.startWithEmptyPage
        target.endWithEmptyPage = workingDesign.endWithEmptyPage
        target.titlePage = workingDesign.titlePage?.copy()
        target.copyrightPage = workingDesign.copyrightPage?.copy()
        target.prologPage = workingDesign.prologPage?.copy()
        target.chapterPage = workingDesign.chapterPage?.copy()
        target.epilogPage = workingDesign.epilogPage?.copy()
        target.blurbPage = workingDesign.blurbPage?.copy()
    }
}

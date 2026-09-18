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

import de.saxsys.mvvmfx.ViewModel
import javafx.beans.property.ReadOnlyObjectProperty
import javafx.beans.property.ReadOnlyObjectWrapper

/**
 * View model of [ProjectSettingsTree].
 *
 * The tree carries no state beyond the selection: the shape of it is fixed and built by the view.
 * [selectedSection] is the single output and reports which node the user picked.
 */
class ProjectSettingsTreeViewModel : ViewModel {

    private val selectedSectionWrapper: ReadOnlyObjectWrapper<ProjectSettingsSection?> =
        ReadOnlyObjectWrapper(this, "selectedSection", null)

    /** The section the user picked in the tree, absent while nothing is selected. */
    val selectedSection: ReadOnlyObjectProperty<ProjectSettingsSection?> get() = selectedSectionWrapper.readOnlyProperty

    /**
     * Takes over the section the user picked in the tree.
     *
     * Called by [ProjectSettingsTreeView] only; the outside world reads [selectedSection].
     *
     * @param section the picked section, `null` when the selection was cleared
     */
    internal fun select(section: ProjectSettingsSection?) {
        selectedSectionWrapper.value = section
    }
}

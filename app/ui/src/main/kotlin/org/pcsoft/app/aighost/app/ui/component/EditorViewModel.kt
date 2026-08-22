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
import javafx.beans.property.ObjectProperty
import javafx.beans.property.SimpleObjectProperty
import org.pcsoft.app.aighost.model.project.Project

/**
 * View model of [Editor].
 *
 * [project] is the single input of the component and is bound by whoever shows it. The view hands it
 * on to the project tree unchanged, so both sides of the split always show the same project.
 */
class EditorViewModel : ViewModel {

    /** The project being edited, absent while no project is open. */
    val project: ObjectProperty<Project?> = SimpleObjectProperty(this, "project", null)
}

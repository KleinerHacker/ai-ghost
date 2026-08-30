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
import javafx.beans.binding.Bindings
import javafx.beans.binding.BooleanBinding
import javafx.beans.property.ObjectProperty
import javafx.beans.property.SimpleObjectProperty
import org.pcsoft.app.aighost.fx.model.project.ProjectProperty

/**
 * View model of [Editor].
 *
 * The property model of the project is the single input of the component and is handed over by
 * whoever shows it. It is taken as the model itself and not as the value of a property of its own,
 * so a property never carries another property; the project inside it may be exchanged as often as
 * the user opens another one.
 *
 * [onProjectBound] is what [EditorView] uses to pass the model on to the parts of the editor, because
 * the view is built before the window hands the project over.
 */
class EditorViewModel : ViewModel {

    /** The project being edited, absent until the surrounding window handed it over. */
    var project: ProjectProperty? = null
        private set

    val selectedProjectTreeItem: ObjectProperty<ProjectListItem?> = SimpleObjectProperty()
    val showBookEditor: BooleanBinding = Bindings.createBooleanBinding(
        { selectedProjectTreeItem.value is ProjectListItem.Root },
        selectedProjectTreeItem
    )


    /**
     * Called with the project model as soon as it is handed over.
     *
     * Set by [EditorView], which passes the model on to the project tree and the manuscript editor.
     */
    internal var onProjectBound: ((ProjectProperty) -> Unit)? = null

    /**
     * Takes the property model of the project over and lets the view pass it on.
     *
     * @param project the project model of the surrounding window
     */
    internal fun bind(project: ProjectProperty) {
        this.project = project
        onProjectBound?.invoke(project)
    }
}

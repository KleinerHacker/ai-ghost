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

package org.pcsoft.app.aighost.fx.model

import arrow.core.Either
import org.pcsoft.app.aighost.fx.model.project.ProjectProperty
import org.pcsoft.app.aighost.model.ProjectStorage
import org.pcsoft.app.aighost.model.project.Project
import java.io.File

/**
 * JavaFX wrapper for [ProjectStorage] that provides the current project as a property
 * and delegates new, load, and save operations to the underlying storage while keeping the
 * property synchronized.
 */
object FXProjectStorage {
    /**
     * Property holding the current project and offering every field of that object
     * as a property of its own. Automatically synchronized when a project is created or loaded.
     */
    val current: ProjectProperty = ProjectProperty(ProjectStorage.current)

    /**
     * The file from which the current project was loaded, or null if the project
     * has not been saved yet.
     */
    val currentFile: File?
        get() = ProjectStorage.currentFile

    /**
     * Indicates whether the current project has already been saved to a file.
     */
    val alreadySaved: Boolean
        get() = ProjectStorage.alreadySaved

    /**
     * Creates a new project with default values and updates the [current] property accordingly.
     */
    fun new() = ProjectStorage.new().run {
        current.set(ProjectStorage.current)
    }

    /**
     * Loads a project from the specified file and updates the [current] property with the
     * loaded values.
     *
     * @param file The file to load the project from
     * @return Either [ProjectStorage.Error] if loading failed or [Unit] on success
     */
    fun load(file: File): Either<ProjectStorage.Error, Unit> = ProjectStorage.load(file).onRight {
        current.set(ProjectStorage.current)
    }

    /**
     * Opens the given project as the project of the given file and updates the [current] property.
     *
     * This is what a caller does with [ProjectStorage.Error.Incomplete]: the project it carries is
     * opened once the user accepted the parts the document lost.
     *
     * @param project The project to open
     * @param file The file the project was read from
     */
    fun open(project: Project, file: File) = ProjectStorage.open(project, file).run {
        current.set(ProjectStorage.current)
    }

    /**
     * Saves the current project to persistent storage.
     *
     * @return Either [ProjectStorage.Error] if saving failed or [Unit] on success
     */
    fun save(): Either<ProjectStorage.Error, Unit> = ProjectStorage.save()

    /**
     * Saves the current project to the specified file.
     *
     * @param file The file to save the project to
     * @return Either [ProjectStorage.Error] if saving failed or [Unit] on success
     */
    fun save(file: File): Either<ProjectStorage.Error, Unit> = ProjectStorage.save(file)
}

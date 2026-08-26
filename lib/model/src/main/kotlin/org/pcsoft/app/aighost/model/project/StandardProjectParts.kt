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

package org.pcsoft.app.aighost.model.project

import org.pcsoft.app.aighost.model.project.book.Book
import org.pcsoft.app.aighost.model.project.design.Design
import org.pcsoft.app.aighost.model.project.meta.Meta
import org.pcsoft.app.aighost.plugin.api.model.project.ProjectPartRegistry

/**
 * The parts the application ships with, announced to the [ProjectPartRegistry].
 *
 * The registry is what the storage asks for the class behind an entry of a project archive, and it
 * knows nothing on its own: the three standard parts are registered here, a plugin registers the ones
 * it brings along. Registering happens before the first project is read.
 */
object StandardProjectParts {

    /**
     * Registers [Meta], [Design] and [Book] at the [ProjectPartRegistry].
     *
     * Calling this more than once does nothing beyond the first call, so it is safe wherever the
     * order of initialization is not obvious.
     */
    fun register() {
        ProjectPartRegistry.register(Meta::class)
        ProjectPartRegistry.register(Design::class)
        ProjectPartRegistry.register(Book::class)
    }

}

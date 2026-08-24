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
import org.pcsoft.app.aighost.plugin.api.model.project.ProjectPart

/**
 * A writing project of the user, the root object that is persisted as one archive.
 *
 * A project is nothing but the parts it is made of: every part is stored under its own identifier and
 * is written into an entry of its own, so a part added later - by a plugin for instance - travels
 * with the document without the root object knowing it. The three parts the application ships with
 * are reachable through [meta], [design] and [book].
 *
 * The project is a plain mutable value object: it is changed on the object itself, one part at a
 * time, and it reports nothing to anybody. Whoever shows a project reads it when it needs the value.
 *
 * @property parts The parts of the project by their identifier, the three built in ones by default.
 */
data class Project(
    var parts: Map<String, ProjectPart> = mapOf(
        PART_META to Meta(),
        PART_DESIGN to Design(),
        PART_BOOK to Book()
    )
) {

    /**
     * Meta data of the project - its name, its author and its copyright notice.
     *
     * Reading the part falls back to a fresh [Meta] as long as the project does not carry one, so a
     * document that lost the entry is shown with the defaults instead of failing.
     */
    var meta: Meta
        get() = parts[PART_META] as? Meta ?: Meta()
        set(value) {
            parts = parts + (PART_META to value)
        }

    /**
     * Typographic and page settings the manuscript is rendered with.
     *
     * Reading the part falls back to a fresh [Design] as long as the project does not carry one, so a
     * document that lost the entry is shown with the defaults instead of failing.
     */
    var design: Design
        get() = parts[PART_DESIGN] as? Design ?: Design()
        set(value) {
            parts = parts + (PART_DESIGN to value)
        }

    /**
     * The manuscript with its title and chapters.
     *
     * Reading the part falls back to a fresh [Book] as long as the project does not carry one, so a
     * document that lost the entry is shown with the defaults instead of failing.
     */
    var book: Book
        get() = parts[PART_BOOK] as? Book ?: Book()
        set(value) {
            parts = parts + (PART_BOOK to value)
        }

    companion object {
        /**
         * Identifier for the metadata part of the project.
         *
         * This constant is used as a unique key to reference the metadata section within the project's structure.
         * It corresponds to the `Meta` class, which encapsulates essential project information
         * such as the name, author, and copyright details. The association between this key
         * and the `Meta` class is established through the `@ProjectPartInfo` annotation.
         */
        const val PART_META = "meta"

        /**
         * Identifier for the design part of the project.
         *
         * This constant is used as a unique key to reference the design section within the project's structure.
         * It corresponds to the `Design` class, which encapsulates typographic and page settings
         * for the manuscript. The association between this key and the `Design` class is established
         * through the `@ProjectPartInfo` annotation.
         */
        const val PART_DESIGN = "design"

        /**
         * Identifier for the book part of the project.
         *
         * This constant is used as a unique key to reference the book section within the project's structure.
         * It corresponds to the `Book` class, which encapsulates the manuscript with its title and chapters.
         * The association between this key and the `Book` class is established through the `@ProjectPartInfo` annotation.
         */
        const val PART_BOOK = "book"
    }
}

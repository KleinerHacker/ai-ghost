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
 * The three parts the application ships with are fields of the project: [meta], [design] and [book]
 * are always there, are never `null` and are reached without a lookup. Everything a plugin adds sits
 * beside them in [extensionParts], stored under the identifier the part declares. [parts] shows both
 * as one map, which is what the storage writes into the archive - one entry per part - so a part
 * added later travels with the document without the root object knowing it.
 *
 * The project is a plain mutable value object: it is changed on the object itself, one part at a
 * time, and it reports nothing to anybody. Whoever shows a project reads it when it needs the value.
 *
 * @property meta Meta data of the project - its name, its author and its copyright notice.
 * @property design Typographic and page settings the manuscript is rendered with.
 * @property book The manuscript with its title and chapters.
 * @property extensionParts The parts beyond the three standard ones, by their identifier.
 */
data class Project(
    var meta: Meta = Meta(),
    var design: Design = Design(),
    var book: Book = Book(),
    var extensionParts: Map<String, ProjectPart> = emptyMap()
) {

    /**
     * Every part of the project by the identifier it is stored under, the three standard ones first.
     *
     * The map is built on every read and is not the storage of the project: a part is written through
     * [putPart] or through the field of the standard part it belongs to.
     */
    val parts: Map<String, ProjectPart>
        get() = linkedMapOf<String, ProjectPart>(
            PART_META to meta,
            PART_DESIGN to design,
            PART_BOOK to book
        ) + extensionParts

    /**
     * The part stored under [identifier], or `null` when the project carries none.
     *
     * The three standard identifiers always answer with the part behind their field.
     *
     * @param identifier The identifier a part is stored under.
     */
    fun part(identifier: String): ProjectPart? = when (identifier) {
        PART_META -> meta
        PART_DESIGN -> design
        PART_BOOK -> book
        else -> extensionParts[identifier]
    }

    /**
     * Puts [part] into the project under [identifier], replacing what was stored there before.
     *
     * A standard identifier writes the field of that part, so it keeps its type - everything else
     * lands beside the standard parts and is written into the archive just the same.
     *
     * @param identifier The identifier the part is stored under.
     * @param part The part to store.
     * @throws IllegalArgumentException When a standard identifier is given a part of another type.
     */
    fun putPart(identifier: String, part: ProjectPart) {
        when (identifier) {
            PART_META -> meta = part as? Meta ?: throw mismatch(identifier, "Meta", part)
            PART_DESIGN -> design = part as? Design ?: throw mismatch(identifier, "Design", part)
            PART_BOOK -> book = part as? Book ?: throw mismatch(identifier, "Book", part)
            else -> extensionParts = extensionParts + (identifier to part)
        }
    }

    /**
     * Removes the part stored under [identifier] from the project.
     *
     * The three standard parts belong to every project and cannot be taken out of it.
     *
     * @param identifier The identifier of the part to remove.
     * @return `true` when the project carried such a part, `false` otherwise.
     * @throws IllegalArgumentException When [identifier] names one of the standard parts.
     */
    fun removePart(identifier: String): Boolean {
        require(identifier !in STANDARD_IDENTIFIERS) {
            "The standard project part '$identifier' belongs to every project and cannot be removed."
        }

        if (identifier !in extensionParts)
            return false

        extensionParts = extensionParts - identifier
        return true
    }

    /**
     * The failure reported when a standard identifier is handed a part of another type: writing it
     * would take the standard part out of the project, which a project cannot be without.
     */
    private fun mismatch(identifier: String, expected: String, part: ProjectPart) =
        IllegalArgumentException(
            "The project part '$identifier' has to be a $expected, not a ${part::class.simpleName}."
        )

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

        /** The identifiers of the parts every project carries, whatever a plugin adds beside them. */
        val STANDARD_IDENTIFIERS: Set<String> = setOf(PART_META, PART_DESIGN, PART_BOOK)

        /**
         * Builds a project from the parts a document was read from.
         *
         * The three standard parts are taken out of [parts] and fall back to their defaults when the
         * document does not carry them, so a file that lost an entry opens with the defaults instead
         * of failing. Every other part is kept as it is, so a part of a plugin that is not installed
         * here survives the next save.
         *
         * @param parts The parts by the identifier they were stored under.
         */
        fun fromParts(parts: Map<String, ProjectPart>): Project = Project(
            meta = parts[PART_META] as? Meta ?: Meta(),
            design = parts[PART_DESIGN] as? Design ?: Design(),
            book = parts[PART_BOOK] as? Book ?: Book(),
            extensionParts = parts.filterKeys { it !in STANDARD_IDENTIFIERS }
        )
    }
}

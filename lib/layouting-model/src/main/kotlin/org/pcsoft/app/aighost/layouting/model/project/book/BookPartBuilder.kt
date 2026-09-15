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

package org.pcsoft.app.aighost.layouting.model.project.book

import org.pcsoft.framework.simplay.engine.model.Document
import org.pcsoft.framework.simplay.engine.model.TextBlock
import org.pcsoft.framework.simplay.engine.model.TextStyle

/**
 * Builds the blocks of a written part - a prolog, a chapter or an epilog - from its anchor in the
 * book's simPlay [Document].
 *
 * IP-36 removed the heading and the paragraphs from every book part; their text lives only in
 * [Document] since IP-37, addressed through a `${anchorId}` token (a simPlay `TextAnchor`) at the
 * start of the part's first block. This builder is the single place both directions of that anchor
 * meet: a part whose anchor already has a page in [Document] hands that page's blocks back unchanged
 * (the editor's read path), while a part with no such page yet - a freshly created project, or a
 * chapter just added to the tree - gets a fresh, empty block carrying nothing but the anchor (the
 * seed a new part starts writing into).
 */
object BookPartBuilder {

    /**
     * Builds one written part, identified by [anchorId].
     *
     * @param document The book's document, searched for a page whose id is [anchorId].
     * @param anchorId The part's anchor id - `"prolog"`, `"epilog"` or a chapter's
     * `id.toString()` - used both as the token embedded in the first block and as the id of the page
     * that carries it.
     * @param style The style a freshly seeded block is built with; ignored when [document] already
     * carries the part's page.
     * @return The existing blocks of the part's page, or - when there is none yet - a single block
     * holding only the `${anchorId}` anchor.
     */
    fun build(document: Document, anchorId: String, style: TextStyle): List<TextBlock> {
        val existing = document.pages.firstOrNull { it.id == anchorId }?.blocks
        if (!existing.isNullOrEmpty()) {
            return existing
        }

        return listOf(TextBlock.of("\${$anchorId}", style))
    }
}

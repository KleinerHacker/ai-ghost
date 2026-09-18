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

package org.pcsoft.app.aighost.app.controller

/**
 * What a single block of the current layout writes its text back to.
 *
 * A block of the sheet does not know which field of the manuscript it stands for; the view model
 * keeps the list of targets in block order, and [BookPartEditorController.readModel] and
 * [BookPartEditorController.writeModel] turn one of these back into the actual model field.
 *
 * [Paragraph] resolves for the blurb, whose paragraphs still live on
 * [org.pcsoft.app.aighost.model.project.book.Blurb] itself. [AnchorBlock] resolves for a prolog, a
 * chapter or an epilog (IP-38): their text lives only in the book's simPlay `Document`, so the target
 * names the anchor id of the part's page and the position of the block inside it instead of a plain
 * list index.
 */
sealed interface PartTarget {

    /**
     * A paragraph of the part.
     *
     * @property index Index into the part's paragraph list.
     */
    data class Paragraph(val index: Int) : PartTarget

    /**
     * A block of a prolog, a chapter or an epilog, addressed through its part's anchor.
     *
     * @property anchorId The anchor id of the part's page in the book's `Document` - `"prolog"`,
     * `"epilog"` or a chapter's `id.toString()`.
     * @property blockIndex Index into that page's block list.
     */
    data class AnchorBlock(val anchorId: String, val blockIndex: Int) : PartTarget
}

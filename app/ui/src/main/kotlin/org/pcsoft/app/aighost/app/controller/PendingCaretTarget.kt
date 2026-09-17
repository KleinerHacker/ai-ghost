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
 * A caret placement to consume the next time the whole-book document is rebuilt and pushed to
 * `PaperSheetView`, taking precedence over the ordinary linear-position restore
 * [org.pcsoft.app.aighost.app.ui.component.BookPartEditorViewModel] otherwise performs.
 *
 * A paragraph structure operation (split, remove, move) knows exactly which block the caret belongs
 * on afterwards, in terms that survive the rebuild - the anchor id of the page and the block's index
 * within it - rather than a linear position, which a changed block count would shift underneath it.
 * [org.pcsoft.app.aighost.app.controller.BookPartEditorController.documentBlockIndex] resolves
 * [anchorId] and [blockIndex] into the document-wide ordinal `CaretModel.moveIntoBlock` expects,
 * against the freshly rebuilt document, never a cached one.
 *
 * @property anchorId the anchor id of the target page
 * @property blockIndex the block's index within that page's own block list
 * @property charOffset the character offset within the block, counted like `CaretModel.position`
 */
data class PendingCaretTarget(
    val anchorId: String,
    val blockIndex: Int,
    val charOffset: Int,
)

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
 */
sealed interface PartTarget {

    /** The heading of the part. */
    data object Title : PartTarget

    /**
     * A further heading line of the part.
     *
     * @property modelIndex Index into the part's `titleAppendix` list, blank lines included.
     */
    data class AppendixLine(val modelIndex: Int) : PartTarget

    /**
     * A paragraph of the part.
     *
     * @property index Index into the part's `paragraph` list.
     */
    data class Paragraph(val index: Int) : PartTarget
}

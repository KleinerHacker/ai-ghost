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

import org.pcsoft.app.aighost.model.project.book.Blurb
import org.pcsoft.app.aighost.model.project.book.Chapter
import org.pcsoft.app.aighost.model.project.book.Epilog
import org.pcsoft.app.aighost.model.project.book.Prolog

/**
 * One entry of the project tree shown by [ProjectList].
 *
 * The tree mixes structural nodes that always exist with nodes that stand for a part of the book,
 * so a consumer of [ProjectList.selectedItem] tells them apart in an exhaustive `when` instead of
 * comparing labels. The part carrying nodes hold the model object they show, which is absent as
 * long as the user has not created that part.
 */
sealed interface ProjectListItem {

    /** The root of the tree, standing for the project itself. */
    data object Root : ProjectListItem

    /**
     * The prolog of the book.
     *
     * @property prolog the prolog, absent as long as the user has not created one
     */
    data class PrologItem(val prolog: Prolog?) : ProjectListItem

    /** The branch collecting every chapter of the book. */
    data object Chapters : ProjectListItem

    /**
     * A single chapter of the book, shown under [Chapters].
     *
     * @property chapter the chapter this node stands for
     */
    data class ChapterItem(val chapter: Chapter) : ProjectListItem

    /**
     * The epilog of the book.
     *
     * @property epilog the epilog, absent as long as the user has not created one
     */
    data class EpilogItem(val epilog: Epilog?) : ProjectListItem

    /**
     * The blurb of the book.
     *
     * @property blurb the blurb, absent as long as the user has not created one
     */
    data class BlurbItem(val blurb: Blurb?) : ProjectListItem
}

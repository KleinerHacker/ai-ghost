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

import org.pcsoft.app.aighost.model.project.book.BookPart
import org.pcsoft.app.aighost.model.project.design.BookPartPageDesign
import org.pcsoft.framework.simplay.engine.model.TextBlock

/**
 * Builds the blocks of a written part - a prolog, a chapter or an epilog.
 *
 * IP-36 removed the heading and the paragraphs from [BookPart]: that text now lives only in the
 * simPlay `Document` a book carries (IP-37/IP-38), addressed through the part's anchor. Until IP-38
 * rebuilds this builder around that anchor, a written part contributes no block of its own here.
 */
object BookPartBuilder {

    /**
     * Builds one written part.
     *
     * @param part Part the blocks would be built from.
     * @param pageDesign Page design of the part - the styles of the heading, its further lines and the text.
     * @return Always empty until IP-38 rebuilds this method around the part's anchor in the book's
     * `Document`.
     */
    fun build(part: BookPart, pageDesign: BookPartPageDesign): List<TextBlock> {
        // TODO(IP-38): read the part's blocks from the book's Document through its anchor id instead.
        return emptyList()
    }
}

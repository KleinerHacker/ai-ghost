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

package org.pcsoft.app.aighost.layouting.model.project.meta

import org.pcsoft.app.aighost.layouting.model.common.toTextStyle
import org.pcsoft.app.aighost.model.project.book.Copyright
import org.pcsoft.app.aighost.model.project.design.Design
import org.pcsoft.app.aighost.model.project.meta.Meta
import org.pcsoft.framework.simplay.engine.model.TextBlock

/**
 * Builds the blocks of the copyright page.
 *
 * IP-36 removed the copyright notice and its further lines from [Copyright]: that text now lives only
 * in the simPlay `Document` the book carries (IP-37/IP-38), addressed through the copyright page's
 * anchor. Until IP-38 rebuilds this builder around that anchor, only the author name - which still
 * lives in [Meta] - is built here, and only while the page still belongs to the book.
 */
object CopyrightPageBuilder {

    /**
     * Builds the copyright page.
     *
     * @param copyright Copyright page of the book - no longer a source of text, kept for its switch
     * and for the future anchor lookup of IP-38.
     * @param meta Meta data the author name is taken from.
     * @param design Design the copyright page styles are taken from.
     * @return The author block, when the page is included, the design shows it and one was typed;
     * otherwise empty until IP-38 rebuilds this method around the copyright page's anchor.
     */
    fun build(copyright: Copyright, meta: Meta, design: Design): List<TextBlock> {
        if (!copyright.included) {
            return emptyList()
        }

        // TODO(IP-38): read the notice and its further lines from the book's Document through the
        //  "copyright" anchor id instead.
        val copyrightPage = design.copyrightPage
        val blocks = ArrayList<TextBlock>()

        if (copyrightPage.showAuthor && meta.author.isNotBlank()) {
            blocks += TextBlock.of(meta.author, copyrightPage.authorStyle.toTextStyle())
        }

        return blocks
    }
}
